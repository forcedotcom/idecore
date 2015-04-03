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
package com.salesforce.ide.core.services;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.MetadataStubExt;
import com.salesforce.ide.core.remote.metadata.CustomObjectNameResolver;
import com.salesforce.ide.core.remote.metadata.DescribeMetadataObjectExt;
import com.salesforce.ide.core.remote.metadata.DescribeMetadataResultExt;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;

/**
 *
 * @author cwall
 */
public class MetadataService extends BaseService {
    private static final Logger logger = Logger.getLogger(MetadataService.class);

    public MetadataService() {}

    public DescribeMetadataResultExt getDescribeMetadata(MetadataStubExt metadataStubExt, IProgressMonitor monitor)
            throws ForceRemoteException, InterruptedException {
        if (metadataStubExt == null) {
            throw new IllegalArgumentException("MetadataStubExt cannot be null");
        }
        monitorCheck(monitor);
        DescribeMetadataResult describeMetadataResult = metadataStubExt.describeMetadata();
        if (describeMetadataResult == null) {
            logger.warn("Returned DescribeMetadataResult is null");
            return null;
        }
        DescribeMetadataResultExt describeMetadataResultExt = new DescribeMetadataResultExt(describeMetadataResult);
        if (logger.isDebugEnabled()) {
            logger.debug("Got describe metadata:\n  " + describeMetadataResultExt.toString());
        }
        return describeMetadataResultExt;
    }

    public boolean isApexClassEnabled(Connection connection) throws ForceConnectionException, ForceRemoteException,
            InterruptedException {
        return isComponentTypeEnabled(connection, Constants.APEX_CLASS);
    }

    public boolean isApexTriggerEnabled(Connection connection) throws ForceConnectionException, ForceRemoteException,
            InterruptedException {
        return isComponentTypeEnabled(connection, Constants.APEX_TRIGGER);
    }

    public boolean isApexPageEnabled(Connection connection) throws ForceConnectionException, ForceRemoteException,
            InterruptedException {
        return isComponentTypeEnabled(connection, Constants.APEX_PAGE);
    }

    public boolean isComponentTypeEnabled(Connection connection, String componentType) throws ForceConnectionException,
            ForceRemoteException, InterruptedException {
        return isComponentTypeEnabled(connection, new String[] { componentType });
    }

    public boolean isComponentTypeEnabled(Connection connection, String[] componentTypes)
            throws ForceConnectionException, ForceRemoteException, InterruptedException {
        if (connection == null || Utils.isEmpty(componentTypes)) {
            throw new IllegalArgumentException("Connection and/or object types cannot be null");
        }

        String[] enabledComponentTypes = getEnabledComponentTypes(connection);
        if (Utils.isEmpty(enabledComponentTypes)) {
            logger.warn("Unable to determine privileges on object types on " + connection.getLogDisplay()
                    + " - enabled types are null or empty");
            return false;
        }

        boolean enabled = false;
        for (String componentType : componentTypes) {
            for (String enabledComponentType : enabledComponentTypes) {
                if (componentType.equals(enabledComponentType)) {
                    enabled = true;
                    break;
                }
                enabled = false;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Object types " + (enabled ? "are" : "are not") + " enabled on " + connection.getLogDisplay());
        }
        return enabled;
    }

    public String[] getEnabledComponentTypes(IProject project) throws ForceConnectionException, ForceRemoteException,
            InterruptedException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        Connection connection = getConnectionFactory().getConnection(project);
        return getEnabledComponentTypes(connection);
    }

    public String[] getEnabledComponentTypes(Connection connection) throws ForceConnectionException,
            ForceRemoteException, InterruptedException {
        return getEnabledComponentTypes(connection, false);
    }

    public String[] getEnabledComponentTypes(Connection connection, boolean addChildren)
            throws ForceConnectionException, ForceRemoteException, InterruptedException {
        return getEnabledComponentTypes(connection, addChildren, false);
    }

    public String[] getEnabledComponentTypes(Connection connection, boolean addChildren, boolean addInternal)
            throws ForceConnectionException, ForceRemoteException, InterruptedException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        MetadataStubExt metadataStubExt = getMetadataFactory().getMetadataStubExt(connection);
        DescribeMetadataResultExt describeMetadataResultExt =
                getDescribeMetadata(metadataStubExt, new NullProgressMonitor());
        if (describeMetadataResultExt == null || Utils.isEmpty(describeMetadataResultExt.getMetadataObjects())) {
            logger.warn("Unable to determine object type privileges on " + metadataStubExt.getLogDisplay()
                    + " - describeMetadataResultExt is null or empty");
            return null;
        }

        String[] componentTypes = getEnabledComponentTypes(describeMetadataResultExt, addChildren);
        if (addInternal) {
            List<String> internalComponentTypes = getComponentFactory().getInternalComponentTypes();
            internalComponentTypes.addAll(Arrays.asList(componentTypes));
            return internalComponentTypes.toArray(new String[internalComponentTypes.size()]);
        }
		return componentTypes;
    }

    public String[] getEnabledComponentTypes(DescribeMetadataResultExt describeMetadataResultExt) {
        return getEnabledComponentTypes(describeMetadataResultExt, false);
    }

    public String[] getEnabledComponentTypes(DescribeMetadataResultExt describeMetadataResultExt, boolean addChildren) {
        if (describeMetadataResultExt == null) {
            throw new IllegalArgumentException("DescribeMetadataResultExt cannot be null");
        }

        DescribeMetadataObjectExt[] describeMetadataObjectExts = describeMetadataResultExt.getMetadataObjects();
        if (Utils.isEmpty(describeMetadataObjectExts)) {
            logger.warn("No object types found");
            return null;
        }

        Set<String> componentTypeList = new HashSet<>();
        for (DescribeMetadataObjectExt describeMetadataObjectExt : describeMetadataObjectExts) {
            String componentType = describeMetadataObjectExt.getName();
            if (componentType != null && !getComponentFactory().isDisabledComponentType(componentType)) {
                componentTypeList.add(componentType);
                if (logger.isDebugEnabled()) {
                    logger.debug("'" + componentType + "' is enabled object type in org");
                }
            }

            if (addChildren && Utils.isNotEmpty(describeMetadataObjectExt.getChildren())) {
                for (String child : describeMetadataObjectExt.getChildren()) {
                    if (child != null) {
                        componentTypeList.add(child);
                    }
                }
            }
        }

        String[] componentTypes = componentTypeList.toArray(new String[componentTypeList.size()]);
        if (Utils.isNotEmpty(componentTypes)) {
            Arrays.sort(componentTypes);
        }
        return componentTypes;
    }

    public ComponentList getEnabledComponents(DescribeMetadataResultExt describeMetadataResultExt) {
        if (describeMetadataResultExt == null) {
            throw new IllegalArgumentException("DescribeMetadataResultExt cannot be null");
        }
        DescribeMetadataObjectExt[] describeMetadataObjectExts = describeMetadataResultExt.getMetadataObjects();
        if (Utils.isEmpty(describeMetadataObjectExts)) {
            logger.warn("No object types found");
            return null;
        }

        String[] componentTypes = getEnabledComponentTypes(describeMetadataResultExt);
        ComponentList componentList = getComponentFactory().getComponentListInstance();
        for (String componentType : componentTypes) {
            if (getComponentFactory().isRegisteredComponentType(componentType)) {
                Component component = getComponentFactory().getComponentByComponentType(componentType);
                if (component != null) {
                    componentList.add(component);
                }
            }
        }

        return componentList;
    }

    public boolean isTestRequired(Connection connection) throws ForceConnectionException, ForceRemoteException,
            InterruptedException {
        MetadataStubExt metadataStubExt = getMetadataFactory().getMetadataStubExt(connection);
        DescribeMetadataResultExt describeMetadata = getDescribeMetadata(metadataStubExt, new NullProgressMonitor());
        return describeMetadata.isTestRequired();
    }

    // L I S T M E T A D A T A

    /**
     * This method will returned all qualified results back with given query in batch (not component specific), then
     * apply filtering logic which is used to streamline the difference between package manifest wildcard result and
     * listMetadata result.
     *
     * @param connection
     * @param query
     * @param filter
     * @return
     * @throws ForceConnectionException
     * @throws ForceRemoteException
     * @throws InterruptedException
     * @throws RemoteException
     * @throws RemoteException
     */
    public FileMetadataExt listMetadata(Connection connection, ListMetadataQuery[] query, boolean filter,
            IProgressMonitor monitor) throws ForceConnectionException, ForceRemoteException, InterruptedException {
        FileMetadataExt fileMetadataExt = listMetadata(connection, query, monitor);
        if (filter) {
            fileMetadataExt = filterStandardObjectsFromFileProperties(fileMetadataExt);
        }
        return fileMetadataExt;
    }

    public FileMetadataExt listMetadata(Connection connection, IProgressMonitor monitor)
            throws ForceConnectionException, ForceRemoteException, InterruptedException {
        return listMetadata(connection, true, monitor);
    }

    public FileMetadataExt listMetadata(Connection connection, boolean filter, IProgressMonitor monitor)
            throws ForceConnectionException, ForceRemoteException, InterruptedException {
        FileMetadataExt fileMetadataExt = new FileMetadataExt();

        String[] componentTypes = getEnabledComponentTypes(connection, filter);
        if (Utils.isNotEmpty(componentTypes)) {
            if (logger.isDebugEnabled()) {
                StringBuffer strBuff = new StringBuffer("Retrieved all components for component types [");
                strBuff.append(componentTypes.length).append("] for connection [").append(connection.getLogDisplay())
                        .append("]:");
                for (String componentType : componentTypes) {
                    strBuff.append("\n  ").append(componentType);
                }
                logger.debug(strBuff.toString());
            }
            ListMetadataQuery[] listMetadataQueryArray = getListMetadataQueryArray(connection, componentTypes, monitor);
            fileMetadataExt = listMetadata(connection, listMetadataQueryArray, monitor);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(fileMetadataExt.toString());
        }

        return fileMetadataExt;
    }

    public FileMetadataExt listMetadata(Connection connection, String[] componentTypes, IProgressMonitor monitor)
            throws ForceConnectionException, ForceRemoteException, InterruptedException {
        return listMetadata(connection, componentTypes, null, monitor);
    }

    public FileMetadataExt listMetadata(Connection connection, String[] componentTypes, String[] filterComponentTypes,
            IProgressMonitor monitor) throws ForceConnectionException, ForceRemoteException, InterruptedException {
        FileMetadataExt fileMetadataExt = new FileMetadataExt();

        if (Utils.isNotEmpty(componentTypes)) {
            ListMetadataQuery[] listMetadataQueryArray = getListMetadataQueryArray(connection, componentTypes, monitor);
            fileMetadataExt = listMetadata(connection, listMetadataQueryArray, monitor);
        }

        if (Utils.isNotEmpty(filterComponentTypes)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Pre-filter FileProperties:\n" + fileMetadataExt.toString());
            }
            fileMetadataExt = filterComponentTypesFromFileProperties(fileMetadataExt, filterComponentTypes);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(fileMetadataExt.toString());
        }

        return fileMetadataExt;
    }

    public FileMetadataExt listMetadata(Connection connection, Component component, IProgressMonitor monitor)
            throws ForceConnectionException, ForceRemoteException, InterruptedException {

        ListMetadataQuery query = new ListMetadataQuery();
        String type = component.getComponentType();
        String folder = null;
        if (component.isWithinFolder()) {
            type = component.getFolderNameIfFolderTypeMdComponent();
        }

        query.setType(type);
        query.setFolder(folder);

        FileMetadataExt fileMetadataExt = listMetadata(connection, new ListMetadataQuery[] { query }, monitor);
        if (logger.isDebugEnabled()) {
            logger.debug(fileMetadataExt.toString());
        }

        return fileMetadataExt;
    }

    public FileMetadataExt getStandardObjectFileProperties(Connection connection, IProgressMonitor monitor)
            throws ForceConnectionException, ForceRemoteException, InterruptedException {
        return getObjectFileProperties(connection, monitor, CustomObjectNameResolver.getCheckerForStandardObject());
    }

    public FileMetadataExt getCustomObjectFileProperties(Connection connection, IProgressMonitor monitor)
            throws ForceConnectionException, ForceRemoteException, InterruptedException {
        return getObjectFileProperties(connection, monitor, CustomObjectNameResolver.getCheckerForCustomObject());
    }

    protected FileMetadataExt getObjectFileProperties(Connection connection, IProgressMonitor monitor,
            CustomObjectNameResolver objectNameResolver) throws ForceConnectionException, ForceRemoteException,
            InterruptedException {
        List<FileProperties> list = new ArrayList<>();

        FileMetadataExt fileMetadataExtArray =
                listMetadata(connection, getComponentFactory().getComponentByComponentType(Constants.CUSTOM_OBJECT),
                    monitor);

        if (fileMetadataExtArray != null && Utils.isNotEmpty(fileMetadataExtArray.getFileProperties())) {
            for (FileProperties prop : fileMetadataExtArray.getFileProperties()) {
                if (objectNameResolver.check(new Path(prop.getFullName()).lastSegment(), Constants.CUSTOM_OBJECT)) {
                    list.add(prop);
                }
            }
        }

        return new FileMetadataExt(list.toArray(new FileProperties[list.size()]));
    }

    public FileMetadataExt listMetadata(Connection connection, ListMetadataQuery[] queries, IProgressMonitor monitor)
            throws ForceConnectionException, ForceRemoteException, InterruptedException {
        MetadataStubExt metadataStubExt = getMetadataFactory().getMetadataStubExt(connection);

        if (Utils.isEmpty(queries)) {
            queries = getListMetadataQueryArray(connection, true, monitor);
        }

        return new FileMetadataExt(metadataStubExt.listMetadata(queries, monitor));
    }

    public ListMetadataQuery[] getListMetadataQueryArray(Connection connection, boolean filter, IProgressMonitor monitor)
            throws ForceConnectionException, ForceRemoteException, InterruptedException {

        ListMetadataQuery[] listMetadataQueryArray = null;
        String[] componentTypes = getEnabledComponentTypes(connection, filter);
        if (Utils.isNotEmpty(componentTypes)) {
            if (logger.isDebugEnabled()) {
                StringBuffer strBuff = new StringBuffer("Retrieved all components for component types [");
                strBuff.append(componentTypes.length).append("] for connection [").append(connection.getLogDisplay())
                        .append("]:");
                for (String componentType : componentTypes) {
                    strBuff.append("\n  ").append(componentType);
                }
                logger.debug(strBuff.toString());
            }
            listMetadataQueryArray = getListMetadataQueryArray(connection, componentTypes, monitor);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Generated [" + (null != listMetadataQueryArray ? listMetadataQueryArray.length : 0)
                    + "] file metadata queries");
        }

        return listMetadataQueryArray;
    }

    /**
     * Assemble list metadata queries for enabled org types excluding given types.
     *
     * @param connection
     * @param excludedComponentTypes
     *            excluded types
     * @param filter
     * @param monitor
     * @return
     * @throws ForceConnectionException
     * @throws ForceRemoteException
     * @throws InterruptedException
     */
    public ListMetadataQuery[] getListMetadataQueryArray(Connection connection, Set<String> excludedComponentTypes,
            boolean filter, IProgressMonitor monitor) throws ForceConnectionException, ForceRemoteException,
            InterruptedException {

        ListMetadataQuery[] listMetadataQueryArray = null;
        String[] componentTypes = getEnabledComponentTypes(connection, filter);

        if (Utils.isNotEmpty(componentTypes)) {
            if (Utils.isNotEmpty(excludedComponentTypes)) {
                List<String> filteredComponentTypes = new ArrayList<>(Arrays.asList(componentTypes));
                filteredComponentTypes.removeAll(excludedComponentTypes);
                componentTypes = filteredComponentTypes.toArray(new String[filteredComponentTypes.size()]);
            }

            if (logger.isDebugEnabled()) {
                StringBuffer strBuff = new StringBuffer("Retrieved all components for component types [");
                strBuff.append(componentTypes.length).append("] for connection [").append(connection.getLogDisplay())
                        .append("]:");
                for (String componentType : componentTypes) {
                    strBuff.append("\n  ").append(componentType);
                }
                logger.debug(strBuff.toString());
            }
            listMetadataQueryArray = getListMetadataQueryArray(connection, componentTypes, monitor);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Generated [" + (null != listMetadataQueryArray ? listMetadataQueryArray.length : 0)
                    + "] file metadata queries");
        }

        return listMetadataQueryArray;
    }

    public ListMetadataQuery[] getListMetadataQueryArray(Connection connection, String[] componentTypes,
            IProgressMonitor monitor) {
        if (Utils.isEmpty(componentTypes)) {
            return null;
        }

        ComponentList folderComponents = getComponentFactory().getFolderComponents();
        List<ListMetadataQuery> listMetadataQueryArray = new ArrayList<>(componentTypes.length);
        for (int i = 0; i < componentTypes.length; i++) {
            if (monitor.isCanceled()) {
                break;
            }

            if (folderComponents.hasComponentType(componentTypes[i])) {
                Component component = folderComponents.getComponentByType(componentTypes[i]);

                ListMetadataQuery folderQuery = new ListMetadataQuery();
                folderQuery.setType(component.getFolderNameIfFolderTypeMdComponent());

                try {
                    FileMetadataExt ext = listMetadata(connection, new ListMetadataQuery[] { folderQuery }, monitor);
                    if (ext != null && Utils.isNotEmpty(ext.getFileProperties())) {
                        for (FileProperties file : ext.getFileProperties()) {
                            ListMetadataQuery query = new ListMetadataQuery();
                            query.setType(componentTypes[i]);
                            query.setFolder(file.getFullName());
                            listMetadataQueryArray.add(query);
                        }
                    }
                } catch (Exception e) {
                    logger.error("An error occured while querying for " + component.getComponentType() + " folders", e);
                }
            //Don't query for abstract types
            } else if (!Constants.ABSTRACT_SHARING_RULE_TYPES.contains(componentTypes[i])) {
                ListMetadataQuery query = new ListMetadataQuery();
                query.setType(componentTypes[i]);
                listMetadataQueryArray.add(query);
            }
        }

        return listMetadataQueryArray.toArray(new ListMetadataQuery[listMetadataQueryArray.size()]);
    }

    protected FileMetadataExt filterStandardObjectsFromFileProperties(FileMetadataExt fileMetadataExt) {
        List<FileProperties> listMetadataList = new ArrayList<>();
        if (fileMetadataExt.hasFileProperties()) {
            for (FileProperties fileProperties : fileMetadataExt.getFileProperties()) {
                if (CustomObjectNameResolver.getCheckerForStandardObject().check(fileProperties.getFullName(),
                    fileProperties.getType())) {
                    logger.info("Filter out component name '" + fileProperties.getFullName() + "' of '"
                            + fileProperties.getType() + "' component type");
                    continue;
                }
                listMetadataList.add(fileProperties);
            }
        }
        return new FileMetadataExt(listMetadataList.toArray(new FileProperties[listMetadataList.size()]));
    }

    protected FileMetadataExt filterComponentTypesFromFileProperties(FileMetadataExt fileMetadataExt,
            String... filterComponentTypes) {
    	if (Utils.isEmpty(filterComponentTypes) || !fileMetadataExt.hasFileProperties()) {
            return fileMetadataExt;
        }

        fileMetadataExt.sort();
        final HashSet<String> filterComponentTypesSet = Sets.newHashSet(filterComponentTypes);
        final Collection<FileProperties> withoutCustomObjects =
                Collections2.filter(new ArrayList<>(Arrays.asList(fileMetadataExt.getFileProperties())), new Predicate<FileProperties>() {

                    @Override
                    public boolean apply(FileProperties fp) {
                        final String type = fp.getType();
                        return !(filterComponentTypesSet.contains(type)
                                && CustomObjectNameResolver.getCheckerForCustomObject().check(fp.getFullName(), type));

                }
                });

        final Collection<FileProperties> withoutCustomAndStandardObject =
                Collections2.filter(withoutCustomObjects, new Predicate<FileProperties>() {

                    @Override
                    public boolean apply(FileProperties fp) {
                        final String type = fp.getType();
                        return !(filterComponentTypesSet.contains(type)
                                && CustomObjectNameResolver.getCheckerForStandardObject().check(fp.getFullName(), type));

            }
                });
        final Collection<FileProperties> withoutOtherToBeFilteredTypes =
            Collections2.filter(withoutCustomAndStandardObject, new Predicate<FileProperties>() {

                @Override
                public boolean apply(FileProperties fp) {
                    final String type = fp.getType();
                    return !(filterComponentTypesSet.contains(type));

        }
            });
        return new FileMetadataExt(withoutOtherToBeFilteredTypes.toArray(new FileProperties[withoutOtherToBeFilteredTypes.size()]));

    }
}
