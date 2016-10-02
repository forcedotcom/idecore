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
package com.salesforce.ide.core.project;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.w3c.dom.Document;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.api.metadata.types.PackageTypeMembers;
import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.jobs.LoadSObjectsJob;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.QuietCloseable;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.MetadataStubExt;
import com.salesforce.ide.core.remote.metadata.DescribeMetadataResultExt;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.services.ForceProjectRefreshJob;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceTimeoutException;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.RetrieveMessage;

public class ProjectController extends Controller {
    private static final Logger logger = Logger.getLogger(ProjectController.class);
    
    public static final int ALL_CONTENT = 0;
    public static final int ALL_DEV_CODE_CONTENT = 1;
    public static final int SPECIFIC_PACKAGE = 2;
    public static final int ALL_PACKAGES = 3;
    public static final int CUSTOM_COMPONENTS = 4;
    public static final int NONE = 5;
    public static final int REFRESH = 6;
    
    private int retryMax = 2;
    
    private ListMetadataQuery[] listMetadataQueries = null;
    
    private static class Holder {
        private final static ProjectContentSummaryAssembler projectContentSummaryAssembler =
            new ProjectContentSummaryAssembler(ContainerDelegate.getInstance().getFactoryLocator());
    }
    
    public ProjectController() {
        this(new ProjectModel());
    }
    
    public ProjectController(IProject project) {
        this(new ProjectModel(project));
    }
    
    private ProjectController(ProjectModel model) {
        super();
        this.model = model;
    }
    
    public ProjectModel getProjectModel() {
        return (ProjectModel) model;
    }
    
    public int getRetryMax() {
        return retryMax;
    }
    
    public void setRetryMax(int retryMax) {
        this.retryMax = retryMax;
    }
    
    public ProjectContentSummaryAssembler getProjectContentSummaryAssembler() {
        return Holder.projectContentSummaryAssembler;
    }
    
    public int queriesCount() {
        return Utils.isNotEmpty(listMetadataQueries) ? listMetadataQueries.length : 0;
    }
    
    // called by container
    @Override
    public void init() {
        model = new ProjectModel();
    }
    
    public void setPackageManifest(Document manifestDocument) throws JAXBException {
        if (getProjectModel() == null || manifestDocument == null) {
            logger.warn("Unable to generate package manifest from document - project model and/or document is null");
            return;
        }
        
        Package packageManifest = ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .getPackageManifestFactory().createPackageManifest(manifestDocument);
        PackageManifestModel packageManifestModel = getProjectModel().getPackageManifestModel();
        packageManifestModel.setPackageManifest(packageManifest);
        packageManifestModel.setManifestDocument(manifestDocument);
    }
    
    // used by testing framework, i think. otherwise workspace operation calls
    // methods directly
    @Override
    public void finish(IProgressMonitor monitor) throws CoreException {
        if (getProjectModel() == null) {
            throw new IllegalArgumentException("Project model cannot be null");
        }
        
        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    try {
                        performCreateProject(monitor);
                    } catch (Exception e) {
                        throw new CoreException(
                            new Status(IStatus.ERROR, Constants.FORCE_PLUGIN_PREFIX, 0, e.getMessage(), e));
                    }
                }
            }, null, IResource.NONE, monitor);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to perform project create finish: " + logMessage);
            throw e;
        }
    }
    
    // Used by the testing framework to create projects
    // This is similar to the creation path through the wizard except that it turns the project "online" by default
    private void performCreateProject(IProgressMonitor monitor) throws ForceConnectionException, InterruptedException,
        CoreException, ForceRemoteException, InvocationTargetException, FactoryException, ServiceException {
        saveConnection(monitor);
        
        // create project
        createProject(true, monitor);
        
        // save settings to project
        saveSettings(monitor);
        
        // generate project structure including generic unpackaged/package.xml
        generateProjectStructure(monitor);
        
        // fetch components
        fetchComponents(monitor);
        
        // schema
        generateSchemaFile(new SubProgressMonitor(monitor, 2));
    }
    
    public void postFinish() {
        if (getProjectModel().getProject() == null || !getProjectModel().getProject().exists()) {
            logger.warn("Unable to perform post finish jobs - project is null or does not exist");
            return;
        }
        
        // open project in force.com perspective
        Utils.openForcePerspective();
        
        try {
            Connection connection = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory()
                .getConnection(getProjectModel().getForceProject());
            LoadSObjectsJob loadSObjectsJob = new LoadSObjectsJob(
                ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getDescribeObjectRegistry(),
                connection,
                getProjectModel().getProject().getName());
            loadSObjectsJob.setSystem(true);
            loadSObjectsJob.schedule();
        } catch (Exception e) {
            // this is fine because we may load later
            logger.warn("Unable to load custom object cache: " + e.getMessage());
        }
    }
    
    @Override
    public void dispose() {
        model = null;
    }
    
    public void createProject(boolean applyOnlineNature, IProgressMonitor monitor) throws CoreException {
        if (getProjectModel() == null || Utils.isEmpty(getProjectModel().getProjectName())) {
            throw new IllegalArgumentException("Project model and/or project name cannot be null");
        }
        
        String[] forceNatures = applyOnlineNature
            ? new String[] { DefaultNature.NATURE_ID, OnlineNature.NATURE_ID }
            : new String[] { DefaultNature.NATURE_ID };
            
        IProject newProject = ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .createProject(getProjectModel().getProjectName(), forceNatures, monitor);
        getProjectModel().setProject(newProject);
    }
    
    public void saveConnection(IProgressMonitor monitor)
        throws ForceConnectionException, InterruptedException, InsufficientPermissionsException {
        if (getProjectModel() == null || getProjectModel().getForceProject() == null) {
            throw new IllegalArgumentException("Project model and/or force project cannot be null");
        }
        
        ForceProject forceProject = getProjectModel().getForceProject();
        
        try {
            monitorCheck(monitor);
            Connection connection =
                ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(forceProject);
            getProjectModel().setConnection(connection);
            
            getDescribeMetadata(monitor);
            
            // save endpoint, if new
            ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getSalesforceEndpoints()
                .addUserEndpoint(forceProject.getEndpointServer());
            monitor.worked(1);
        } catch (InsufficientPermissionsException e) {
            if (getProjectModel().getContentSelection() != NONE) {
                throw e;
            } else {
                logger.warn(
                    "No project contents seleted, so ignoring unable to save connection: " + e.getExceptionMessage());
            }
        } catch (ForceConnectionException e) {
            if (getProjectModel().getContentSelection() != NONE) {
                throw e;
            } else {
                logger.warn(
                    "No project contents seleted, so ignoring unable to save connection: " + e.getExceptionMessage());
            }
        }
    }
    
    public void generateProjectStructure(IProgressMonitor monitor) throws InterruptedException, CoreException {
        if (getProjectModel() == null || getProjectModel().getProject() == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        
        monitorCheck(monitor);
        
        IProject project = getProjectModel().getProject();
        
        IFolder sourceFolder = project.getFolder(Constants.SOURCE_FOLDER_NAME);
        if (!sourceFolder.exists()) {
            sourceFolder.create(true, true, monitor);
        }
        
        monitorCheck(monitor);
        
        IFolder referencedFolder = project.getFolder(Constants.REFERENCED_PACKAGE_FOLDER_NAME);
        if (!referencedFolder.exists()) {
            referencedFolder.create(true, true, monitor);
        }
        
        // create specifc dir structure; handles case when user selections
        // types, but does not have instances in org
        switch (getProjectModel().getContentSelection()) {
        case ALL_CONTENT:
            break;
        case ALL_DEV_CODE_CONTENT:
            if (!sourceFolder.exists()) {
                break;
            }
            
            List<String> folderNames = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
                .getComponentFolderNames(getEnabledComponentTypes());
            if (Utils.isNotEmpty(folderNames)) {
                for (String folderName : folderNames) {
                    IFolder componentFolder = sourceFolder.getFolder(folderName);
                    if (componentFolder != null && !componentFolder.exists()) {
                        try {
                            componentFolder.create(true, true, monitor);
                        } catch (CoreException e) {
                            String logMessage = Utils.generateCoreExceptionLog(e);
                            // not all that critical if we cannot create
                            logger.error(
                                "Unable to create component folder '" + componentFolder.getName() + "': " + logMessage);
                        }
                    }
                }
                
            }
            
            break;
        case SPECIFIC_PACKAGE:
            break;
        case CUSTOM_COMPONENTS:
            if (logger.isInfoEnabled()) {
                logger.info("Creating all component folders for project structure for custom components");
            }
            
            if (!sourceFolder.exists()) {
                break;
            }
            
            Package packageManifest = getProjectModel().getPackageManifestModel().getPackageManifest();
            List<PackageTypeMembers> types = packageManifest.getTypes();
            if (Utils.isNotEmpty(types)) {
                for (PackageTypeMembers type : types) {
                    try {
                        String folderName = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
                            .getComponentFolderName(type.getName());
                        if (Utils.isNotEmpty(folderName) && !Constants.SHARING_RULE_TYPES.contains(type.getName())) {
                            IFolder componentFolder = sourceFolder.getFolder(folderName);
                            if (componentFolder != null && !componentFolder.exists()) {
                                componentFolder.create(true, true, monitor);
                            }
                        }
                    } catch (CoreException e) {
                        String logMessage = Utils.generateCoreExceptionLog(e);
                        // not all that critical if we cannot create
                        logger.error(
                            "Unable to create component folder for type '" + type.getName() + "': " + logMessage);
                    }
                }
                
            }
            break;
        }
        
        monitorCheck(monitor);
    }
    
    public void savePackageManifest(IProgressMonitor monitor) throws InterruptedException {
        if (getProjectModel() == null) {
            throw new IllegalArgumentException("Project model and/or project cannot be null");
        }
        
        monitorCheck(monitor);
        
        Package packageManifest = null;
        
        switch (getProjectModel().getContentSelection()) {
        case ALL_DEV_CODE_CONTENT:
            packageManifest = ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                .getPackageManifestFactory().createDefaultPackageManifestForComponentTypes(getEnabledComponentTypes());
            break;
        case SPECIFIC_PACKAGE:
            packageManifest = ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                .getPackageManifestFactory().createPackageManifest(getProjectModel().getSelectedPackageName());
            break;
        case CUSTOM_COMPONENTS:
            packageManifest = getProjectModel().getPackageManifestModel().getPackageManifest();
            break;
        default:
            packageManifest = ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                .getPackageManifestFactory().createGenericDefaultPackageManifest();
        }
        
        savePackageManifest(packageManifest, monitor);
    }
    
    public void savePackageManifest(Package packageManifest, IProgressMonitor monitor) throws InterruptedException {
        IProject project = getProjectModel().getProject();
        
        if (project == null) {
            logger.warn("Unable to generate generic package manifest - project is null");
            return;
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Saving generic package manifest for project " + project.getName());
        }
        
        monitorCheck(monitor);
        
        try {
            IFolder sourceFolder = project.getFolder(Constants.SOURCE_FOLDER_NAME);
            if (!sourceFolder.exists()) {
                sourceFolder.create(true, true, monitor);
            }
            
            IFile packageManifestFile = sourceFolder.getFile(Constants.PACKAGE_MANIFEST_FILE_NAME);
            ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                .saveToFile(packageManifestFile, packageManifest.getXMLString(), monitor);
        } catch (Exception e) {
            // REVIEWME: what to do here? it's not paramount to create an unpackaged
            // manifest (retrieve should create one on the fly if not available)
            logger.warn("Unable to save package manifest", e);
        }
    }
    
    public void saveSettings(IProgressMonitor monitor) throws InterruptedException {
        if (getProjectModel() == null || getProjectModel().getProject() == null) {
            throw new IllegalArgumentException("Project model and/or project cannot be null");
        }
        
        monitorCheck(monitor);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Saving project '" + getProjectModel().getProjectName() + "' settings.");
        }
        
        ForceProject forceProject = getProjectModel().getForceProject();
        
        if (Utils.isEmpty(forceProject.getPackageName())) {
            forceProject.setPackageName(Constants.DEFAULT_PACKAGED_NAME);
        }
        
        if (Utils.isEmpty(forceProject.getProjectIdentifier())) {
            forceProject.setProjectIdentifier("IDE" + UUID.randomUUID().getMostSignificantBits());
        }
        
        // set ide version on new project
        forceProject.setIdeVersion(
            ContainerDelegate.getInstance().getServiceLocator().getProjectService().getInstalledIdeVersion());
            
        // save project settings
        ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .saveForceProject(getProjectModel().getProject(), forceProject);
            
        // save workspace settings
        ForceIdeCorePlugin.savePreference(Constants.LAST_ENV_SELECTED, getProjectModel().getEnvironment());
        String otherLabel = Messages.getString("ProjectCreateWizard.OrganizationPage.OtherEnvironment.label");
        if (otherLabel.equals(getProjectModel().getEnvironment())) {
            ForceIdeCorePlugin.savePreference(Constants.LAST_USERNAME_SELECTED, forceProject.getUserName());
            ForceIdeCorePlugin.savePreference(Constants.LAST_SERVER_SELECTED, forceProject.getEndpointServer());
            ForceIdeCorePlugin.savePreference(Constants.LAST_KEEP_ENDPOINT_SELECTED, forceProject.isKeepEndpoint());
            ForceIdeCorePlugin.savePreference(Constants.LAST_PROTOCOL_SELECTED, forceProject.isHttpsProtocol());
        }
    }
    
    public void fetchComponents(IProgressMonitor monitor) throws InterruptedException, ForceConnectionException,
        ForceRemoteException, InvocationTargetException, FactoryException, CoreException, ServiceException {
        if (getProjectModel() == null) {
            throw new IllegalArgumentException("Project model cannot be null");
        }
        
        monitorCheck(monitor);
        IProject theproject = getProjectModel().getProject();
        ProjectPackageList projectPackageList = ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .getProjectPackageFactory().getProjectPackageListInstance(theproject);
        RetrieveResultExt retrieveResultHandler = null;
        
        switch (getProjectModel().getContentSelection()) {
        case ALL_CONTENT:
            Connection connection = getProjectModel().getConnection();
            Package defaultPackageManifest = createPackageForAllContent(monitor, connection);
            savePackageManifest(defaultPackageManifest, monitor);
            retrieveResultHandler = 
                ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrieve(
                    connection,
                    null,
                    defaultPackageManifest,
                    monitor);
            break;
        case ALL_DEV_CODE_CONTENT:
            savePackageManifest(monitor);
            retrieveResultHandler =
                ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrieveSelective(
                    getProjectModel().getConnection(),
                    projectPackageList,
                    getEnabledComponentTypes(),
                    monitor);
            break;
        case SPECIFIC_PACKAGE:
            savePackageManifest(monitor);
            retrieveResultHandler =
                ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrievePackage(
                    getProjectModel().getConnection(),
                    theproject,
                    getProjectModel().getSelectedPackageName(),
                    monitor);
            break;
        case ALL_PACKAGES:
        	savePackageManifest(monitor);
        	IFolder referencePkgFolder = ContainerDelegate.getInstance().getServiceLocator()
        			.getProjectService().getReferencedPackagesFolder(theproject);
            Utils.adjustResourceReadOnly(referencePkgFolder, false, true);
        	// fetchManagedInstalledPackages(monitor) is called at the bottom
        	// of this method. This is a separate case so we don't
        	// fetch the unpackaged code.
        	break;
        case CUSTOM_COMPONENTS:
            savePackageManifest(monitor);
            retrieveResultHandler =
                ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrieve(
                    getProjectModel().getConnection(),
                    null,
                    getProjectModel().getPackageManifestModel().getPackageManifest(),
                    monitor);
            break;
        case REFRESH:
            savePackageManifest(monitor);
            retrieveResultHandler =
                ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrievePackage(
                    getProjectModel().getConnection(),
                    theproject,
                    getProjectModel().getForceProject().getPackageName(),
                    monitor);
            break;
        default:
            savePackageManifest(monitor);
            return;
        }
        
        monitorCheck(monitor);
        try {
            handleRetrieveResults(retrieveResultHandler, monitor);
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
        
        fetchManagedInstalledPackages(monitor);
    }
    
    private Package createPackageForAllContent(IProgressMonitor monitor, Connection connection)
        throws ForceConnectionException, ForceRemoteException, InterruptedException {
        getProjectModel().setSelectedPackageName(Constants.DEFAULT_PACKAGED_NAME);
        
        Package defaultPackageManifest = ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .getPackageManifestFactory().getDefaultPackageManifest(connection);
        List<String> componentTypes = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
            .getEnabledRegisteredComponentTypes();
        List<String> componentTypesForListMetadata = new ArrayList<>();
        Map<String, List<String>> packageManifestMap = new HashMap<>();
        
        // find a list of all non wildcard supported components. This would include folder based components also.
        for (String componentType : componentTypes) {
            if (!ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
                .isWildCardSupportedComponentType(componentType) && !componentType.equalsIgnoreCase("Folder")
                && !componentType.equalsIgnoreCase("PackageManifest")
                && !componentType.equalsIgnoreCase("StandardObject")) {
                componentTypesForListMetadata.add(componentType);
                packageManifestMap.put(componentType, new ArrayList<String>());
            }
        }
        
        String[] listMetaDataComponentTypes =
            componentTypesForListMetadata.toArray(new String[componentTypesForListMetadata.size()]);
        FileMetadataExt fileMetadataExt = ContainerDelegate.getInstance().getServiceLocator().getMetadataService()
            .listMetadata(connection, listMetaDataComponentTypes, monitor);
            
        // Add the fileproperty to the Map based on filepropertyType
        
        //remove any installed components.
        FileProperties[] fileProperties = fileMetadataExt.getFileProperties();
        
        // query describe metadata for organization namespace - don't get from ForceProject due to project might not be created yet.
        MetadataStubExt metadataStubExt =
            ContainerDelegate.getInstance().getFactoryLocator().getMetadataFactory().getMetadataStubExt(connection);
        DescribeMetadataResultExt describeMetadataResultExt = ContainerDelegate.getInstance().getServiceLocator()
            .getMetadataService().getDescribeMetadata(metadataStubExt, monitor);
        String organizationNamespace = describeMetadataResultExt.getOrganizationNamespace();
        
        FileProperties[] removedPackagedFileProperties = Utils.isNotEmpty(fileProperties)
            ? Utils.removePackagedFiles(fileProperties, organizationNamespace)
            : fileProperties;
            
        if (Utils.isNotEmpty(removedPackagedFileProperties)) {
            for (FileProperties fp : removedPackagedFileProperties) {
                
                List<String> listOfComponentNames = packageManifestMap.get(fp.getType());
                String fullName = fp.getFullName();
                //check if this component belongs to a folder.
                String possibleFolderName = null;
                if (fullName.contains("/")) {
                    possibleFolderName = fullName.substring(0, fullName.indexOf("/"));
                }
                
                //add an entry for the component.
                if (!listOfComponentNames.contains(fullName) && Utils.isNotEmpty(fullName)) {
                    listOfComponentNames.add(fullName);
                }
                //add an entry for just the folder.
                if (!listOfComponentNames.contains(possibleFolderName) && Utils.isNotEmpty(possibleFolderName)) {
                    listOfComponentNames.add(possibleFolderName);
                }
                
            }
        }
        
        ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageManifestFactory()
            .addFileNamesToManifest(defaultPackageManifest, packageManifestMap);
        return defaultPackageManifest;
    }
    
    private String[] getEnabledComponentTypes() {
        return getProjectModel().getForceProject().getEnabledComponentTypes();
    }
    
    public void fetchComponents(ServiceTimeoutException ex, IProgressMonitor monitor) throws InterruptedException,
        ForceRemoteException, InvocationTargetException, FactoryException, ServiceException {
        if (getProjectModel() == null || ex == null) {
            throw new IllegalArgumentException("Project model cannot be null");
        }
        
        monitorCheck(monitor);
        RetrieveResultExt retrieveResultHandler =
            ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().getRetrieveResult(
                (RetrieveResultExt) ex.getMetadataResultExt(),
                ex.getAsyncResult(),
                ex.getMetadataStubExt(),
                monitor);
                
        if (retrieveResultHandler != null) {
            ProjectPackageList projectPackageList =
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageFactory()
                    .getProjectPackageListInstance(getProjectModel().getProject());
            retrieveResultHandler.setProjectPackageList(projectPackageList);
        }
        
        monitorCheck(monitor);
        try {
            handleRetrieveResults(retrieveResultHandler, monitor);
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
    }
    
    /**
     * Fetch installed, managed package content
     * 
     * @param monitor
     * @throws InterruptedException
     * @throws ForceConnectionException
     * @throws ForceRemoteException
     * @throws InvocationTargetException
     * @throws ServiceException
     */
    public void fetchManagedInstalledPackages(IProgressMonitor monitor) throws InterruptedException,
        ForceConnectionException, ForceRemoteException, InvocationTargetException, ServiceException {
        if (getProjectModel() == null) {
            throw new IllegalArgumentException("Project model cannot be null");
        }
        
        if (logger.isInfoEnabled()) {
            logger.info(
                "Fetching and saving all installed, managed components for '" + getProjectModel().getProjectName()
                    + "'");
        }
        
        monitorCheckSubTask(monitor, Messages.getString("Component.RetrieveManagedComponents"));
        RetrieveResultExt retrieveResultHandler = ContainerDelegate.getInstance().getServiceLocator()
            .getPackageRetrieveService().retrieveManagedInstalledPackages(getProjectModel().getConnection(), monitor);
            
        monitorCheck(monitor);
        try {
            handleRetrieveResults(retrieveResultHandler, monitor);
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
    }
    
    private void handleRetrieveResults(RetrieveResultExt retrieveResultHandler, IProgressMonitor monitor)
        throws InterruptedException, IOException {
        if (retrieveResultHandler == null) {
            if (logger.isInfoEnabled()) {
                logger.info("No results to handle - retrieveResultHandler is null");
            }
            return;
        }
        
        if (retrieveResultHandler.hasMessages()) {
            for (RetrieveMessage retrieveMessage : retrieveResultHandler.getMessageHandler().getMessages()) {
                logger.warn(
                    "Failed to retrieve: '" + retrieveMessage.getFileName() + "': " + retrieveMessage.getProblem());
            }
        }
        
        final ProjectPackageList projectPackageList = retrieveResultHandler.getProjectPackageList();
        if (Utils.isEmpty(projectPackageList)) {
            if (logger.isDebugEnabled()) {
                logger.debug("No results to handle - project package list is empty");
            }
            return;
        }
        
        monitorCheckSubTask(monitor, Messages.getString("Components.Generating"));
        projectPackageList.setProject(getProjectModel().getProject());
        projectPackageList
            .generateComponents(retrieveResultHandler.getZipFile(), retrieveResultHandler.getFileMetadataHandler());
            
        monitorCheckSubTask(monitor, Messages.getString("Components.Saving"));
        WorkspaceJob job =
            new ForceProjectRefreshJob.ForceProjectRefreshProject(projectPackageList, projectPackageList.getProject());
        job.setRule(projectPackageList.getProject());
        job.schedule();
        
        monitorCheck(monitor);
    }
    
    public void generateSchemaFile(IProgressMonitor monitor) throws CoreException {
        if (getProjectModel() == null) {
            throw new IllegalArgumentException("Project model cannot be null");
        }
        
        if (getProjectModel().getProject() == null) {
            logger.warn("Unable to generate schema file - project is null");
            return;
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching schema for " + getProjectModel().getProjectName());
        }
        
        IProject project = getProjectModel().getProject();
        
        IFile file = project.getFile(Constants.SCHEMA_FILENAME);
        
        if (file.exists()) {
            return;
        }
        
        try (final QuietCloseable<InputStream> c =
            QuietCloseable.make(Utils.openContentStream(Constants.CONTENT_PLACE_HOLDER))) {
            final InputStream stream = c.get();
            
            monitor.worked(1);
            file.create(stream, true, monitor);
            monitor.worked(1);
        }
    }
    
    public void disableBuilder() throws CoreException {
        if (getProjectModel().getProject() != null) {
            ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                .flagSkipBuilder(getProjectModel().getProject());
        }
    }
    
    /**
     * 
     * @param context
     * @param forceProject
     * @return
     * @throws InvocationTargetException
     */
    public boolean reAuthenticate(IRunnableContext context) throws InvocationTargetException {
        final ForceProject forceProject = getProjectModel().getForceProject();
        IRunnableWithProgress resetConnection = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getConnectionFactory()
                    .removeConnection(forceProject);
                try {
                    saveConnection(monitor);
                } catch (Exception e) {
                    logger.warn("Unable to re-authenticate user " + forceProject.getUserName(), e);
                    Utils.openWarning(e, false, "Unable to re-authenticate user " + forceProject.getUserName());
                }
            }
        };
        
        try {
            context.run(false, false, resetConnection);
        } catch (InterruptedException e) {
            ;
        }
        return true;
    }
    
    public void removeNature(IProject project) {
        OnlineNature.removeNature(project, null);
    }
    
    public void applyNatures(IProgressMonitor monitor) throws CoreException {
        if (getProjectModel().getProject() == null) {
            logger.warn("Unable to apply nature - project is null");
            return;
        }
        
        ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .applyNatures(getProjectModel().getProject(), monitor);
    }
    
    public void applyDefaultNature(IProgressMonitor monitor) throws CoreException {
        if (getProjectModel().getProject() == null) {
            logger.warn("Unable to apply nature - project is null");
            return;
        }
        
        ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .applyDefaultNature(getProjectModel().getProject(), monitor);
    }
    
    public void applyOnlineNature(IProgressMonitor monitor) throws CoreException {
        if (getProjectModel().getProject() == null) {
            logger.warn("Unable to apply nature - project is null");
            return;
        }
        
        ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .applyOnlineNature(getProjectModel().getProject(), monitor);
    }
    
    public void cleanUp(IProgressMonitor monitor) {
        if (getProjectModel().getProject() != null && getProjectModel().getProject().exists()) {
            try {
                getProjectModel().getProject().delete(true, true, monitor);
            } catch (CoreException e) {
                String logMessage = Utils.generateCoreExceptionLog(e);
                logger.warn(
                    "Unable to delete project '" + getProjectModel().getProject().getName() + "': " + logMessage,
                    e);
            }
        }
    }
    
    public TreeSet<String> getRemotePackageNames(IProgressMonitor monitor) throws InterruptedException, Exception {
        TreeSet<String> packageNames = new TreeSet<>();
        
        // temporarily set timeout
        int timeoutMillis = getProjectModel().getForceProject().getReadTimeoutMillis();
        Connection connection = null;
        try {
            monitorCheck(monitor);
            connection = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory()
                .getConnection(getProjectModel().getForceProject());
            connection.setTimeoutMillis(Constants.INTERNAL_TIMEOUT_MILLIS);
            getProjectModel().setConnection(connection);
            
            monitorCheck(monitor);
            ProjectPackageList projectPackageList =
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageFactory()
                    .getDevelopmentAndUnmanagedInstalledProjectPackages(connection);
            String[] packageNameArray = projectPackageList.getPackageNames();
            if (Utils.isNotEmpty(packageNameArray)) {
                packageNames.addAll(Arrays.asList(packageNameArray));
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            logger.warn(
                "Unable to get project packages for project '" + getProjectModel().getProjectName() + "': "
                    + ForceExceptionUtils.getRootExceptionMessage(e));
            throw e;
        } finally {
            // set timeout back to users desired limit
            if (connection != null) {
                connection.setTimeoutMillis(timeoutMillis);
            }
        }
        return packageNames;
    }
    
    public void loadRemotePackageNames(IProgressMonitor monitor) throws InterruptedException, Exception {
        getProjectModel().setPackageNames(getRemotePackageNames(monitor));
    }
    
    protected DescribeMetadataResultExt getDescribeMetadata(IProgressMonitor monitor) throws InterruptedException {
        if (getProjectModel() == null || getProjectModel().getForceProject() == null) {
            return null;
        }
        
        if (getProjectModel().getDescribeMetadataResultExt() != null) {
            return getProjectModel().getDescribeMetadataResultExt();
        }
        
        monitorCheck(monitor);
        
        DescribeMetadataResultExt describeMetadataResultExt = null;
        try {
            ForceProject forceProject = getProjectModel().getForceProject();
            MetadataStubExt metadataStubExt = ContainerDelegate.getInstance().getFactoryLocator().getMetadataFactory()
                .getMetadataStubExt(forceProject);
            describeMetadataResultExt = ContainerDelegate.getInstance().getServiceLocator().getMetadataService()
                .getDescribeMetadata(metadataStubExt, monitor);
            getProjectModel().setDescribeMetadataResultExt(describeMetadataResultExt);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            logger.warn(
                "Unable to get enabled component types for project '" + getProjectModel().getProjectName() + "': "
                    + ForceExceptionUtils.getRootExceptionMessage(e));
        }
        return describeMetadataResultExt;
    }
    
    protected FileMetadataExt getFileMetadata(ListMetadataQuery[] listMetadataQueryArray, IProgressMonitor monitor)
        throws InterruptedException, ForceConnectionException, ForceRemoteException {
        if (getProjectModel() == null || getProjectModel().getForceProject() == null) {
            logger.warn("Unable to get enabled component types for project - Force project is null");
            return null;
        }
        
        Connection connection = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory()
            .getConnection(getProjectModel().getForceProject());
        getProjectModel().setConnection(connection);
        return ContainerDelegate.getInstance().getServiceLocator().getMetadataService()
            .listMetadata(connection, listMetadataQueryArray, monitor);
    }
    
    public void loadFileMetadata(IProgressMonitor monitor)
        throws InterruptedException, ForceConnectionException, ForceRemoteException {
        ForceProject forceProject = getProjectModel().getForceProject();
        
        // query describe metadata for organization namespace - don't get from ForceProject due to project might not be created yet.
        MetadataStubExt metadataStubExt =
            ContainerDelegate.getInstance().getFactoryLocator().getMetadataFactory().getMetadataStubExt(
                ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(forceProject));
        DescribeMetadataResultExt describeMetadataResultExt = ContainerDelegate.getInstance().getServiceLocator()
            .getMetadataService().getDescribeMetadata(metadataStubExt, monitor);
        String organizationNamespace = describeMetadataResultExt.getOrganizationNamespace();
        
        // get list metadata for given queries; set to subset as default option is only apex-related
        FileMetadataExt fileMetadata = getFileMetadata(listMetadataQueries, monitor);
        fileMetadata.setSubset(true);
        
        PackageManifestModel packageManifestModel = getProjectModel().getPackageManifestModel();
        fileMetadata
            .setFileProperties(Utils.removePackagedFiles(fileMetadata.getFileProperties(), organizationNamespace));
        packageManifestModel.setFileMetadatExt(fileMetadata);
    }
    
    // get list metadata types that cover default option
    public void prepareFileMetadataQueries(IProgressMonitor monitor)
        throws InsufficientPermissionsException, ForceConnectionException {
        Connection connection = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory()
            .getConnection(getProjectModel().getForceProject());
        getProjectModel().setConnection(connection);
        listMetadataQueries = ContainerDelegate.getInstance().getServiceLocator().getMetadataService()
            .getListMetadataQueryArray(connection, getEnabledComponentTypes(), monitor);
    }
    
    public void clearOrgDetails() {
        getProjectModel().clear();
        if (getProjectModel().getPackageManifestModel() != null) {
            getProjectModel().getPackageManifestModel().setNewManifestDocumentInstance();
        }
    }
}
