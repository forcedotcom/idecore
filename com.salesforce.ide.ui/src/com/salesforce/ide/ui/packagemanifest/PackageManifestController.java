/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.packagemanifest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.PackageManifestDocumentUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.OrgModel;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.core.remote.MetadataStubExt;
import com.salesforce.ide.core.remote.metadata.CustomObjectNameResolver;
import com.salesforce.ide.core.remote.metadata.DescribeMetadataResultExt;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.ui.internal.Messages;
import com.salesforce.ide.ui.widgets.MultiCheckboxButton;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;

/**
 *
 * @author ataylor
 */
public class PackageManifestController extends Controller {
    private static Logger logger = Logger.getLogger(PackageManifestController.class);

    public static final String STANDARD_OBJECT_NODE_NAME = "objects - standard"; //$NON-NLS-1$
    public static final String CUSTOM_OBJECT_NODE_NAME = "objects - custom"; //$NON-NLS-1$

    private static final String CACHE_TYPES = "types"; //$NON-NLS-1$
    private static final String CACHE_ISSUBTYPE = "isSubType"; //$NON-NLS-1$
    private static final String CACHE_PARENT = "parent"; //$NON-NLS-1$

    private final Map<String, PackageTreeNode> map = new HashMap<>();
    private PackageTreeNode root;
    private FileMetadataExt ext;
    private Connection connection;
    private Document cache;
    private Document manifestDoc;

    private final List<String> typeList = new ArrayList<>();
    private final List<String> subTypes = new ArrayList<>();
    private final Map<String, String> parentTypes = new HashMap<>();

    public PackageManifestController() {
        model = new OrgModel();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void finish(IProgressMonitor monitor) throws Exception {

    }

    @Override
    public void init() throws ForceProjectException {

    }

    @Override
    public void setProject(IProject project) {
        // if project is null, cache exists in plugin repro
        URL oldLocation = Utils.getCacheUrl(getProject());
        File oldCache = new File(oldLocation.getFile());

        // set project so check below points to project cache location
        super.setProject(project);

        // clear the cache or move to project
        if (oldCache.exists()) {
            // move an old cache if there is one
            URL newLocation = Utils.getCacheUrl(getProject());

            File newCache = new File(newLocation.getFile());
            if (!newCache.exists()) {
                oldCache.renameTo(newCache);
            } else {
                if (logger.isDebugEnabled()) {
                    try {
                        logger.debug("Cleared manifest cache " + oldCache.toURI().toURL().toExternalForm()); //$NON-NLS-1$
                    } catch (MalformedURLException e) {}
                }
                oldCache.delete();
            }
        }
    }

    public void setFileMetadatExt(FileMetadataExt ext) {
        this.ext = ext;
    }

    public void setManifestDoc(Document doc) {
        manifestDoc = doc;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private Connection getConnection() throws ForceConnectionException, InsufficientPermissionsException {
        if (connection == null) {
            connection =
                    ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(
                        getProject());
        }

        return connection;
    }

    /**
     * load cache if not exist, then create PME tree model and init model state.
     */
    public void updateManifest() {

        map.clear();
        if (cache == null) {
        	cache = Utils.loadDocument(Utils.getCacheUrl(getProject()));
        }
        loadAll();
        createModel();
        initModel();
    }

    public void clearModel() {
        root = null;
        if (ext != null) {
            ext.setFileProperties(null);
        }
        parentTypes.clear();
        typeList.clear();
        subTypes.clear();
    }

    public void clearCache() {
        cache = null;
        URL url = Utils.getCacheUrl(getProject());
        if (url != null) {
            File file = new File(url.getFile());
            if (logger.isDebugEnabled()) {
                try {
                    logger.debug("Cleared manifest cache " + file.toURI().toURL().toExternalForm()); //$NON-NLS-1$
                } catch (MalformedURLException e) {}
            }
            file.delete();

        }
    }
    
    public Document getCache() {
    	return this.cache;
    }

    Map<String, Throwable> erroneousComponentTypes = new HashMap<>();
    boolean exceptionOccurred = false;

    /**
     * construct a map retain <type, list of its subtype> either from component factory or from cache. ex. <workflow,
     * sub-component types are WorkflowAlert, WorkflowFieldUpdate,..>
     */
    private void constructTypeToSubTypeCompMap() {
        if (ext != null) {
            String types[];
            try {
                types =
                        ContainerDelegate.getInstance().getServiceLocator().getMetadataService()
                        .getEnabledComponentTypes(getConnection(), true);
                if (Utils.isNotEmpty(types)) {
                    typeList.addAll(Arrays.asList(types));
                } else {
                    logger.debug("Getting no enabled component type for org '"
                            + getConnection().getForceProject().getUserName() + "' on '"
                            + getConnection().getForceProject().getEndpointServer() + "'");
                }

                for (String key : ext.getFilePropertiesMap(typeList).keySet()) {
                    try {
                    	if (key.equals(Constants.STANDARD_OBJECT))
                    		continue;
                        fillTypeStructures(key);
                    } catch (Throwable th) {
                        erroneousComponentTypes.put(key, th);
                    }
                }

            }
            // if exception occurred when getting enabled component, rollback to
            // getting component type from cache.
            catch (InsufficientPermissionsException e) {
                exceptionOccurred = true;
                logger.debug(e);
            } catch (ForceConnectionException e) {
                exceptionOccurred = true;
                logger.debug(e);
            } catch (ForceRemoteException e) {
                exceptionOccurred = true;
                logger.debug(e);
            } catch (InterruptedException e) {
                exceptionOccurred = true;
                logger.debug(e);
            }

        } else if (ext == null || exceptionOccurred) {
            List<Node> componentTypes = getComponentTypesFromCache(cache, false);
            for (Node componentType : componentTypes) {
                String key = PackageManifestDocumentUtils.getComponentName(componentType);
                try {
                	if (key.equals(Constants.STANDARD_OBJECT))
                		continue;
                    fillTypeStructures(key);
                } catch (Throwable th) {
                    erroneousComponentTypes.put(key, th);
                }
            }
        }

        logAndDisplayWarnMsgIfNeeded(erroneousComponentTypes);
    }

    private void fillTypeStructures(String key) {
        if (!ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().isRegisteredComponentType(key)) {
            return;
        }

        List<String> subCompType =
                ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getSubComponentTypes(key);

        if (Utils.isNotEmpty(subCompType)) {
            for (String subType : subCompType) {
                subTypes.add(subType);
                
                if (parentTypes.containsKey(subType) && false == parentTypes.get(subType).equals(key)){
                	logger.error("subType: "+subType+ " has already been assigned a parentType: "+parentTypes.get(subType)+ " and will now be overwritten by parentType: "+key+"\n");
                }
                parentTypes.put(subType, key);
            }
        }
    }

    public void updateCache(final URL cacheUrl) throws InvocationTargetException, InterruptedException {
        final IProgressService service = PlatformUI.getWorkbench().getProgressService();
        service.run(false, false, new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                Set<String> keySet = ext.getFilePropertiesMap(typeList).keySet();

                monitor.beginTask("Updating project cache...", Utils.isNotEmpty(keySet) ? keySet.size() + 4 : 4);
                monitor.worked(1);

                try {
                    updateCacheWork(cacheUrl, monitor);
                } catch (InterruptedException e) {
                    logger.warn("Operation cancelled: " + e.getMessage()); //$NON-NLS-1$
                } catch (Throwable e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        });
    }

    /**
     * Building PME cache DOM document then persist in packageCache.xml.
     *
     * @param cacheUrl
     * @param monitor
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     * @throws ForceConnectionException
     * @throws ForceRemoteException
     * @throws InterruptedException
     */
    protected void updateCacheWork(URL cacheUrl, IProgressMonitor monitor) throws ParserConfigurationException,
    TransformerException, IOException, ForceConnectionException, ForceRemoteException,
    InterruptedException, URISyntaxException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        cache = builder.newDocument();
        monitor.worked(1);

        Node root = cache.createElementNS(Constants.PACKAGE_MANIFEST_NAMESPACE_URI, Constants.PACKAGE_MANIFEST_TYPES);
        cache.appendChild(root);

        constructTypeToSubTypeCompMap();
        monitor.worked(1);

        Set<String> keySet = ext.getFilePropertiesMap(typeList).keySet();
        String[] keys = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keys);

        // query describe metadata for organization namespace - don't get from
        // ForceProject due to project might not be created yet.
        MetadataStubExt metadataStubExt =
                ContainerDelegate.getInstance().getFactoryLocator().getMetadataFactory().getMetadataStubExt(
                    getConnection());
        DescribeMetadataResultExt describeMetadataResultExt =
                ContainerDelegate.getInstance().getServiceLocator().getMetadataService().getDescribeMetadata(
                    metadataStubExt, new NullProgressMonitor());
        String organizationNamespace = describeMetadataResultExt.getOrganizationNamespace();

        for (String key : keys) {
            if (Constants.ABSTRACT_SHARING_RULE_TYPES.contains(key) || Constants.RULE_TYPES.contains(key)) {
                continue;
            }

            Element type =
                    cache.createElementNS(Constants.PACKAGE_MANIFEST_NAMESPACE_URI, Constants.PACKAGE_MANIFEST_TYPES);
            root.appendChild(type);

            if (subTypes.contains(key)) {
                type.setAttribute(CACHE_ISSUBTYPE, Boolean.TRUE.toString());
                type.setAttribute(CACHE_PARENT, parentTypes.get(key));
            } else {
                type.setAttribute(CACHE_ISSUBTYPE, Boolean.FALSE.toString());
            }

            Node name =
                    cache.createElementNS(Constants.PACKAGE_MANIFEST_NAMESPACE_URI,
                        Constants.PACKAGE_MANIFEST_TYPE_NAME);
            type.appendChild(name);
            name.setTextContent(key);

            Component component =
                    ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
                    .getComponentByComponentType(key);
            if (component != null && component.isWithinFolder()) {
                ListMetadataQuery folderQuery = new ListMetadataQuery();
                folderQuery.setType(component.getFolderNameIfFolderTypeMdComponent());
                FileMetadataExt ext =
                        ContainerDelegate.getInstance().getServiceLocator().getMetadataService().listMetadata(
                            connection, new ListMetadataQuery[] { folderQuery }, new NullProgressMonitor());

                if (ext != null && Utils.isNotEmpty(ext.getFileProperties())) {
                    for (FileProperties file : Utils
                            .removePackagedFiles(ext.getFileProperties(), organizationNamespace)) {
                        Node members =
                                cache.createElementNS(Constants.PACKAGE_MANIFEST_NAMESPACE_URI,
                                    Constants.PACKAGE_MANIFEST_TYPE_MEMBERS);
                        type.appendChild(members);
                        members.setTextContent(file.getFullName());
                    }
                }
            }

            List<FileProperties> files = ext.getFilePropertiesMap(typeList).get(key);
            if (Utils.isEmpty(files)) {
                continue;
            }

            Collections.sort(files, new Comparator<FileProperties>() {
                @Override
                public int compare(FileProperties o1, FileProperties o2) {
                    // To guard against component names being returned from the server as 'null'
                    // W-1150256
                    if (o1.getFullName() == null || o2.getFullName() == null) {
                        return 0;
                    }
                    return String.CASE_INSENSITIVE_ORDER.compare(o1.getFullName(), o2.getFullName());
                }
            });

            for (FileProperties file : Utils.removePackagedFiles(files.toArray(new FileProperties[files.size()]),
                organizationNamespace)) {
                Node members =
                        cache.createElementNS(Constants.PACKAGE_MANIFEST_NAMESPACE_URI,
                            Constants.PACKAGE_MANIFEST_TYPE_MEMBERS);
                type.appendChild(members);
                members.setTextContent(file.getFullName());
            }

            monitor.worked(1);
        }

        Utils.saveDocument(cache, cacheUrl.toURI().getPath());
    }

    /**
     * get top level component type tree nodes, ex. applications, classes, reports, etc.
     *
     * @return array of componet type tree nodes.
     */
    public Object[] getEnabledCompTypeTreeNodes() {
        if (root == null) {
            updateManifest();
        }

        return root != null ? root.getChildren() : new Object[0];
    }

    public Map<String, PackageTreeNode> getPackageTreeNodeMap() {
        return map;
    }

    public PackageTreeNode getNode(String name) {
        return map.get(name.toLowerCase());
    }

    public String getPath(PackageTreeNode node) {
        StringBuilder builder = new StringBuilder();
        Stack<String> stack = new Stack<>();
        while (node != root) {
            String path = node.getName();
            if (node instanceof ComponentTypeNode) {
                path = ((ComponentTypeNode) node).getComponent().getComponentType();
            }
            stack.push(path.toLowerCase());
            node = (PackageTreeNode) node.getParent();
        }

        while (!stack.isEmpty()) {
            builder.append(stack.pop());
            builder.append(Constants.FOWARD_SLASH);
        }

        return builder.toString();
    }

    public String getPathForComponentType(String componentType) {
        return getComponentTypeName(ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
            .getComponentByComponentType(componentType));
    }

    // load file metadata from server
    public void loadAll() {
        try {
            // retrieve all metadata or is subset needs to be augmented
            if (ext == null || ext.getFileProperties() == null || ext.isSubset()) {
                loadFileMetadata();
            }

            updateCache(Utils.getCacheUrl(getProject()));
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage()); //$NON-NLS-1$
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof InsufficientPermissionsException) {
                DialogUtils.getInstance().presentInsufficientPermissionsDialog(
                    (InsufficientPermissionsException) e
                    .getTargetException());
            } else if (e.getTargetException() instanceof InvalidLoginException) {
                // log failure
                logger.warn("Unable to refresh file metadata: " //$NON-NLS-1$
                    + ForceExceptionUtils.getRootCauseMessage(e.getTargetException()));
                // choose further project create direction
                DialogUtils.getInstance().invalidLoginDialog(
                    ForceExceptionUtils.getRootCauseMessage(e.getTargetException()));
            } else {
                logger.error("Unable to refresh file metadata", ForceExceptionUtils //$NON-NLS-1$
                    .getRootCause(e.getTargetException()));
                StringBuffer strBuff = new StringBuffer();
                strBuff.append("Unable to refresh file metadata:\n\n").append(
                    ForceExceptionUtils.getStrippedRootCauseMessage(e)).append("\n\n ");
                Utils.openError("Refresh Error", strBuff.toString());
            }
        } catch (Exception e) {
            logger.error("Unable to refresh file metadata", ForceExceptionUtils.getRootCause(e)); //$NON-NLS-1$
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("Unable to refresh file metadata:\n\n").append(
                ForceExceptionUtils.getStrippedRootCauseMessage(e)).append("\n\n ");
            Utils.openError("Refresh Error", strBuff.toString());
        }
    }

    private void loadFileMetadata() throws InvocationTargetException, InterruptedException {
        final IProgressService service = PlatformUI.getWorkbench().getProgressService();
        service.run(false, false, new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                monitor.beginTask("Fetching organization details...", 3);
                monitor.worked(1);

                try {
                    monitor.subTask("Preparing to fetch component metadata...");

                    // formulate queries: either just a subset to augment passed
                    // file properties or a full query
                    ListMetadataQuery[] listMetadataQueries = null;
                    if (ext != null && ext.isSubset()) {
                        listMetadataQueries =
                                ContainerDelegate.getInstance().getServiceLocator().getMetadataService()
                                .getListMetadataQueryArray(getConnection(), ext.getComponentTypes(), true,
                                    monitor);
                    } else {
                        listMetadataQueries =
                                ContainerDelegate.getInstance().getServiceLocator().getMetadataService()
                                .getListMetadataQueryArray(getConnection(), true, monitor);
                    }
                    monitor.worked(1);

                    if (listMetadataQueries == null) {
                        logger.warn("ListMetadataQuery for org is empty"); //$NON-NLS-1$
                        return;
                    }

                    // use queries count to determine tick length
                    monitor.beginTask("Fetching component metadata...", Utils.isNotEmpty(listMetadataQueries)
                        ? listMetadataQueries.length + 1 : IProgressMonitor.UNKNOWN);
                    monitor.worked(1);

                    // perform list metadata request w/ queries
                    FileMetadataExt tmpFileMetadataExt =
                            ContainerDelegate.getInstance().getServiceLocator().getMetadataService().listMetadata(
                                getConnection(), listMetadataQueries, monitor);

                    // query describe metadata for organization namespace -
                    // don't get from ForceProject due to project might not be
                    // created yet.
                    MetadataStubExt metadataStubExt =
                            ContainerDelegate.getInstance().getFactoryLocator().getMetadataFactory()
                            .getMetadataStubExt(getConnection());
                    DescribeMetadataResultExt describeMetadataResultExt =
                            ContainerDelegate.getInstance().getServiceLocator().getMetadataService()
                            .getDescribeMetadata(metadataStubExt, new NullProgressMonitor());
                    String organizationNamespace = describeMetadataResultExt.getOrganizationNamespace();

                    // use client provided file properties or augment with fill list
                    // filter out packaged fileproperties from returned
                    // fileproperties (pme should not show packaged content)
                    FileProperties[] removedPackagedFileProps =
                            Utils.removePackagedFiles(tmpFileMetadataExt.getFileProperties(), organizationNamespace);

                    if (null == ext) {
                        ext = tmpFileMetadataExt;
                        ext.setFileProperties(removedPackagedFileProps);
                    } else if (ext.getFileProperties() == null) {
                        ext.setFileProperties(removedPackagedFileProps);

                    } else if (ext != null && ext.isSubset()) {
                        ext.addFileProperties(removedPackagedFileProps);
                        ext.setSubset(false);

                    }

                } catch (InterruptedException e) {
                    logger.warn("Operation cancelled: " + e.getMessage()); //$NON-NLS-1$
                } catch (Throwable e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        });
    }

    // TODO consolidate with ComponentTypeNode.getName()
    String getComponentTypeName(Component component) {
        return component.getComponentType().toLowerCase();
    }

    /**
     * construct a map retain <type, list of its subtype> & set state on node (checked? / isWildcardSupported? )
     */
    private void initModel() {
        constructTypeToSubTypeCompMap();
        List<Node> componentTypes = PackageManifestDocumentUtils.getComponentTypes(manifestDoc);

        Map<String, Throwable> erroneousComponentTypes = new HashMap<>();
        for (Node componentType : componentTypes) {
            String componentTypeName = PackageManifestDocumentUtils.getComponentName(componentType);

            try {
                if (subTypes.contains(componentTypeName)) {
                    String parentComponentName = parentTypes.get(componentTypeName);
                    Component parentComponent =
                            ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
                            .getComponentByComponentType(parentComponentName);

                    String parentPath = getComponentTypeName(parentComponent);
                    List<Node> members = PackageManifestDocumentUtils.getComponentMembers(componentType);

                    for (Node member : members) {
                        String memberName = PackageManifestDocumentUtils.getMemberName(member);

                        int index = memberName.indexOf(Constants.DOT);
                        String parentName = memberName.substring(0, index);
                        String subComponentName = memberName.substring(index + 1);

                        StringBuilder path = null;

                        if (CustomObjectNameResolver.getCheckerForCustomObject().check(parentName,
                            Constants.CUSTOM_OBJECT)) {
                            path = new StringBuilder(Constants.CUSTOM_OBJECT);
                            path.append(Constants.FOWARD_SLASH);
                        } else {
                            path = new StringBuilder(parentPath);
                            path.append(Constants.FOWARD_SLASH);
                        }

                        path.append(parentName).append(Constants.FOWARD_SLASH).append(componentTypeName).append(
                            Constants.FOWARD_SLASH).append(subComponentName).append(Constants.FOWARD_SLASH);

                        PackageTreeNode memberTreeNode = getNode(path.toString());

                        if (memberTreeNode != null) {
                            memberTreeNode.setState(MultiCheckboxButton.getBlackCheckedState());
                        }
                    }
                } else {

                    List<Node> members = PackageManifestDocumentUtils.getComponentMembers(componentType);
                    for (Node member : members) {
                        StringBuilder path = new StringBuilder();
                        if (componentTypeName.equals(Constants.CUSTOM_OBJECT)) {
                            path.append(Constants.CUSTOM_OBJECT);
                        }

                        else {
                            path.append(componentTypeName);
                        }
                        path.append(Constants.FOWARD_SLASH);
                        String memberName = PackageManifestDocumentUtils.getMemberName(member);

                        if (path.toString().startsWith(Constants.CUSTOM_OBJECT + Constants.FOWARD_SLASH)
                                && !memberName.equals(Constants.PACKAGE_MANIFEST_WILDCARD)
                                && CustomObjectNameResolver.getCheckerForStandardObject().check(memberName,
                                    Constants.CUSTOM_OBJECT)) {
                            path.delete(0, path.length());
                            path.append(Constants.STANDARD_OBJECT);
                            path.append(Constants.FOWARD_SLASH);
                        }

                        PackageTreeNode memberTreeNode = getNode(path + memberName + Constants.FOWARD_SLASH);

                        if (memberTreeNode != null) {
                            memberTreeNode.setState(MultiCheckboxButton.getBlackCheckedState());
                        }

                        else if (memberName.equals(Constants.PACKAGE_MANIFEST_WILDCARD)) {
                            if (path.toString().startsWith(Constants.STANDARD_OBJECT)) {
                                path.delete(0, path.length());
                                path.append(Constants.CUSTOM_OBJECT);
                                path.append(Constants.FOWARD_SLASH);
                            }
                            getNode(path.toString()).wildcardSelected = true;
                        }
                    }
                }

            } catch (Throwable th) {
                erroneousComponentTypes.put(componentTypeName, th);
            }
        }

        for (PackageTreeNode node : root.getChildList()) {
            try {
                boolean hasWildCard = node.isWildcardSelected();
                for (PackageTreeNode componentChild : node.getChildList()) {
                    for (PackageTreeNode customObjectChild : componentChild.getChildList()) {
                        for (PackageTreeNode customObjectFolder : customObjectChild.getChildList()) {
                            initParentState(customObjectFolder, hasWildCard);
                        }

                        initParentState(customObjectChild, hasWildCard);
                    }

                    initChildState(componentChild, hasWildCard);
                    initParentState(componentChild, hasWildCard);
                }

                initParentState(node, hasWildCard);
            } catch (Throwable th) {
                erroneousComponentTypes.put(node.getName(), th);
            }
        }

        logAndDisplayWarnMsgIfNeeded(erroneousComponentTypes);
    }

    void logAndDisplayWarnMsgIfNeeded(Map<String, Throwable> erroneousComponentTypes) {
        if (Utils.isNotEmpty(erroneousComponentTypes)) {
            StringBuilder warningDialogMsg = new StringBuilder();
            StringBuilder logMsg = new StringBuilder();
            warningDialogMsg.append("The following component type(s) are not supported, so no component will be added to the package manifest editor for these types. \n");
            logMsg.append("Components skipped: \n");
            for (String compName : erroneousComponentTypes.keySet()) {
                warningDialogMsg.append("* ").append(compName).append("\n");
                Throwable throwable = erroneousComponentTypes.get(compName);
                logMsg.append("* Component type '").append(compName).append("' ");

                if (Utils.isNotEmpty(throwable)) {
                    logger.debug("Component type '" + compName + "' not supported in package manifest editor.", throwable);

                } else {
                    logMsg.append("No exception occurred but the component factory is unable to resolve component type '" + compName + "'");
                }

            }
            warningDialogMsg.append("See log for detailed messages.");
            Utils.openWarn(Messages.PackageManifest_content_Warning_text, warningDialogMsg.toString());
        }
    }

    private void initChildState(PackageTreeNode treeNode, boolean hasWildCard) {
        if (treeNode instanceof CustomObjectTypeNode && treeNode.hasChildren()) {
            if (MultiCheckboxButton.isBlackChecked(treeNode.getState())) {
                initCustomObjectChildState(treeNode, MultiCheckboxButton.getBlackCheckedState());
            }
        }
    }

    private void initCustomObjectChildState(PackageTreeNode node, int state) {
        for (PackageTreeNode child : node.getChildList()) {
            initCustomObjectChildState(child, state);
        }

        node.setState(state);
    }

    private void initParentState(PackageTreeNode treeNode, boolean hasWildCard) {
        if (hasWildCard) {
            treeNode.setState(MultiCheckboxButton.getBlackCheckedState());
        } else {
            if (treeNode instanceof CustomObjectTypeNode && treeNode.hasChildren() && allChildChecked(treeNode)) {
                String componentName = ((ComponentTypeNode) treeNode.getParent()).getComponent().getComponentType();

                if (componentName.equals(Constants.STANDARD_OBJECT)) {
                    componentName = Constants.CUSTOM_OBJECT;
                }

                Node componentNode = PackageManifestDocumentUtils.getComponentNode(manifestDoc, componentName);
                if (PackageManifestDocumentUtils.getMemberNode(componentNode, treeNode.getName()) == null) {
                    treeNode.setState(MultiCheckboxButton.getSchroedingerState());
                }

                else {
                    treeNode.setState(MultiCheckboxButton.getBlackCheckedState());
                }
            } else if (MultiCheckboxButton.isBlackChecked(treeNode.getState()) || allChildChecked(treeNode)) {
                treeNode.setState(MultiCheckboxButton.getBlackCheckedState());
            } else if (anyChildChecked(treeNode) || anyChildSchroedinger(treeNode)) {
                treeNode.setState(MultiCheckboxButton.getSchroedingerState());
            }
        }
    }

    protected boolean anyChildSchroedinger(PackageTreeNode node) {
        for (PackageTreeNode child : node.getChildList()) {
            if (MultiCheckboxButton.isSchroedinger(child.getState())) {
                return true;
            }
        }

        return false;
    }

    protected boolean anyChildChecked(PackageTreeNode node) {
        for (PackageTreeNode child : node.getChildList()) {
            if (MultiCheckboxButton.isBlackChecked(child.getState())) {
                return true;
            }
        }

        return false;
    }

    protected boolean allChildChecked(PackageTreeNode node) {
        boolean flag = node.getChildren().length > 0;
        for (PackageTreeNode child : node.getChildList()) {
            if (!MultiCheckboxButton.isBlackChecked(child.getState())) {
                return false;
            }
        }

        return flag;
    }

    /**
     * Create PME tree model nodes - adding component to component type, sub-component to component, and remove parent
     * component when there is no children and not support wildcard for standard object.
     */
    private void createModel() {
        root = new PackageTreeNode(null);
        List<Node> componentTypes = getComponentTypesFromCache(cache, false);

        Map<String, Throwable> erroneousComponentTypes = new HashMap<>();
        for (Node componentType : componentTypes) {
            Component comp = null;
            try {
                String compName = PackageManifestDocumentUtils.getComponentName(componentType);
                comp =
                        ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
                        .getComponentByComponentType(compName);

                if (!Utils.isEmpty(comp)) {
                    if (comp.getComponentType().equals(Constants.STANDARD_OBJECT)) {
                        comp.setDefaultFolder(STANDARD_OBJECT_NODE_NAME);
                        comp.setWildCardSupported(false);
                    } else if (comp.getComponentType().equals(Constants.CUSTOM_OBJECT)) {
                        comp.setDefaultFolder(CUSTOM_OBJECT_NODE_NAME);
                    }

                    ComponentTypeNode node = new ComponentTypeNode(comp);
                    node.retrieved = true;
                    root.addChild(node);
                    map.put(getPath(node), node);

                    addComponents(componentType, node);
                    addSubComponents(componentType, node);
                    removeUnneededComponents(componentType, node);
                } else {
                    erroneousComponentTypes.put(PackageManifestDocumentUtils.getComponentName(componentType), null);

                }

            } catch (Throwable e) {
                erroneousComponentTypes.put(PackageManifestDocumentUtils.getComponentName(componentType), e);
            }
        }

        logAndDisplayWarnMsgIfNeeded(erroneousComponentTypes);

    }

    private void addComponents(Node node, ComponentTypeNode parentNode) {
        List<Node> members = PackageManifestDocumentUtils.getComponentMembers(node);
        for (Node subComponentMember : members) {
            String memberName = PackageManifestDocumentUtils.getMemberName(subComponentMember);

            if (memberName == null) {
                continue;
            }

            if (parentNode.getComponent().isWithinFolder()) {
                addFolderComponent(memberName, parentNode);
            } else {
                PackageTreeNode componentNode = null;
                if (parentNode.getComponent().hasSubComponentTypes()
                        || parentNode.getComponent().getComponentType().equals(Constants.STANDARD_OBJECT)) {
                    componentNode = new CustomObjectTypeNode(memberName);
                } else {
                    componentNode = new ComponentNode(memberName);
                }
                componentNode.retrieved = true;
                parentNode.addChild(componentNode);
                map.put(getPath(componentNode), componentNode);
            }
        }
    }

    private void addFolderComponent(String memberName, ComponentTypeNode parentNode) {
        int index = memberName.indexOf(Constants.FOWARD_SLASH);

        if (index == -1) {
            PackageTreeNode folderNode = new ComponentFolderNode(memberName);
            parentNode.addChild(folderNode);
            parentNode.retrieved = true;
            map.put(getPath(folderNode), folderNode);
        }

        else {
            String parentName = memberName.substring(0, index);
            String compName = memberName.substring(index + 1);

            String path = getPath(parentNode);
            path += parentName.toLowerCase() + Constants.FOWARD_SLASH;

            PackageTreeNode folderNode = map.get(path);
            // create folder node if it doesn't exist: scenario where folder is
            // managed installed (will be removed from packageCache.xml) but
            // component in folder is not.
            if (Utils.isEmpty(folderNode)) {
                folderNode = new ComponentFolderNode(parentName);
                parentNode.addChild(folderNode);
                parentNode.retrieved = true;
                map.put(getPath(folderNode), folderNode);

            }
            PackageTreeNode componentNode = new ComponentNode(compName);
            folderNode.addChild(componentNode);
            componentNode.retrieved = true;
            map.put(getPath(componentNode), componentNode);
        }
    }

    private void addSubComponents(Node node, ComponentTypeNode parentNode) {
        String parentType = parentNode.getComponent().getComponentType();

        List<Node> subComponentTypes = getSubComponentTypes(cache, parentType.equals(Constants.STANDARD_OBJECT) ? Constants.CUSTOM_OBJECT :  parentType);
        for (Node subComponentType : subComponentTypes) {
            String subName = PackageManifestDocumentUtils.getComponentName(subComponentType);

            List<Node> members = PackageManifestDocumentUtils.getComponentMembers(subComponentType);
            for (Node subComponentMember : members) {
                String memberName = PackageManifestDocumentUtils.getMemberName(subComponentMember);

                String parentName = null;
                String compName = null;
                int index = memberName.indexOf('.');
                if (index == -1) {
                    parentName = parentNode.getChildList().get(0).getName();
                    compName = memberName;
                } else {
                    parentName = memberName.substring(0, index);
                    compName = memberName.substring(index + 1);
                }

                if (Constants.STANDARD_OBJECT.equals(parentType) && CustomObjectNameResolver.getCheckerForCustomObject().check(parentName, Constants.CUSTOM_OBJECT) ||
                	Constants.CUSTOM_OBJECT.equals(parentType) && false == CustomObjectNameResolver.getCheckerForCustomObject().check(parentName, Constants.CUSTOM_OBJECT))
                	continue;
                
                
                String path = getPath(parentNode);
                if (CustomObjectNameResolver.getCheckerForCustomObject().check(parentName, Constants.CUSTOM_OBJECT)) {
                    path = Constants.CUSTOM_OBJECT.toLowerCase() + Constants.FOWARD_SLASH;
                }

                path += parentName.toLowerCase() + Constants.FOWARD_SLASH;

                PackageTreeNode componentFolder = map.get(path);

                if (componentFolder != null) {
                    PackageTreeNode customObjectFolderNode =
                            map.get(path + subName.toLowerCase() + Constants.FOWARD_SLASH);

                    if (customObjectFolderNode == null) {
                        if (index != -1) {
                            customObjectFolderNode = new CustomObjectFolderNode(subName);
                            componentFolder.addChild(customObjectFolderNode);
                            map.put(getPath(customObjectFolderNode), customObjectFolderNode);
                        } else {
                            customObjectFolderNode = componentFolder;
                        }
                    }

                    CustomObjectComponentNode componentNode = new CustomObjectComponentNode(compName);

                    if (ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
                            .isRegisteredComponentType(subName)) {
                        componentNode.setComponent(ContainerDelegate.getInstance().getFactoryLocator()
                            .getComponentFactory().getComponentByComponentType(subName));
                    }

                    customObjectFolderNode.addChild(componentNode);
                    map.put(getPath(componentNode), componentNode);
                }
            }
        }
    }

    private void removeUnneededComponents(Node node, ComponentTypeNode parentNode) {
        if (!parentNode.getComponent().isWildCardSupported() && !parentNode.hasChildren()) {
            root.getChildList().remove(parentNode);
        }
    }

    public static List<Node> getComponentTypesFromCache(Document doc, boolean isSubType) {
        List<Node> list = new ArrayList<>();
        Node packageNode = PackageManifestDocumentUtils.getPackageNode(doc);

        if (packageNode != null) {
            NodeList nodeList = packageNode.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);

                if (item.getNodeName().equals(CACHE_TYPES) &&
                        item.getAttributes().getNamedItem(CACHE_ISSUBTYPE).getNodeValue().equals(("" + isSubType).toLowerCase())) { //$NON-NLS-1$
                    list.add(item);
                }
            }
        }

        return list;
    }

    public static List<Node> getSubComponentTypes(Document doc, String parent) {
        List<Node> componentTypes = getComponentTypesFromCache(doc, true);
        List<Node> retList = new ArrayList<>(componentTypes);
        for (Node componentType : componentTypes) {
            if (!componentType.getAttributes().getNamedItem(CACHE_PARENT).getNodeValue().equals(parent)) {
                retList.remove(componentType);
            }
        }

        return retList;
    }

    public PackageTreeNode getRoot() {
        return root;
    }

}
