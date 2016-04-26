/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.core.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.OperationStats;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.MetadataStubExt;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.core.remote.metadata.RetrieveMessageExt;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.RetrieveRequest;
import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.soap.metadata.RetrieveStatus;

/**
 * Class encapsulates all retrieve functionality.
 * 
 * @author cwall
 */
public class PackageRetrieveService extends BasePackageService {
    static final Logger logger = Logger.getLogger(PackageRetrieveService.class);

    protected static final String OPERATION = "Retrieve";
    private static OperationStats operationStats = null;

    static {
        if (logger.isDebugEnabled()) {
            operationStats = new OperationStats(OPERATION);
        }
    }

    public PackageRetrieveService() {}

    public static OperationStats getOperationStats() {
        return operationStats;
    }

    public RetrieveRequest getRetrieveRequest() {
        String lastSupportedEndpointVersion = null;
        if (Utils.isNotEmpty(Utils.getDefaultSystemApiVersion())) {
            lastSupportedEndpointVersion = Utils.getDefaultSystemApiVersion();
        } else {
            lastSupportedEndpointVersion = getProjectService().getLastSupportedEndpointVersion();
        }

        logger.info("RetrieveRequest's api version is set to '" + lastSupportedEndpointVersion + "'");
        return getRetrieveRequest(lastSupportedEndpointVersion);
    }

    public RetrieveRequest getRetrieveRequest(String apiVersion) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.setApiVersion(Double.valueOf(apiVersion));
        retrieveRequest.setSinglePackage(true);
        return retrieveRequest;
    }

    /**
     * Retrieve all packages and their contents and unpackaged using manifest in project.
     */
    public RetrieveResultExt retrieveAll(
        IProject project,
        boolean includeDefaultManifest,
        IProgressMonitor monitor
    ) throws Exception {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        Connection connection = getConnectionFactory().getConnection(project);
        Package defaultMainfest = null;
        if (includeDefaultManifest) {
            defaultMainfest = getPackageManifestFactory().getDefaultPackageManifest(project);
        }
        RetrieveResultExt retrieveResultHandler = retrieveAll(connection, defaultMainfest, monitor);
        retrieveResultHandler.getProjectPackageList().setProject(project);
        return retrieveResultHandler;
    }

    public RetrieveResultExt retrieveAll(IProject project, IProgressMonitor monitor) 
        throws ForceConnectionException, ServiceException, ForceRemoteException, ForceRemoteException, InterruptedException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        Connection connection = getConnectionFactory().getConnection(project);
        return retrieveAll(connection, monitor);
    }

    /**
     * Retrieve all packages and their contents for a given connection.
     */
    public RetrieveResultExt retrieveAll(Connection connection, IProgressMonitor monitor)
            throws ForceConnectionException, ServiceException, ForceRemoteException, ForceRemoteException, InterruptedException {
        return retrieveAll(connection, null, monitor);
    }

    /**
     * Retrieve all packages and their contents for a given connection.
     */
    public RetrieveResultExt retrieveAll(Connection connection, Package defaultMainfest, IProgressMonitor monitor)
            throws ForceConnectionException, ServiceException, ForceRemoteException, InterruptedException, ForceRemoteException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        ProjectPackageList projectPackageList =
                getProjectPackageFactory().getDevelopmentAndUnmanagedInstalledProjectPackages(connection);

        RetrieveRequest retrieveRequest = getRetrieveRequest();
        retrieveRequest.setPackageNames(projectPackageList.getNamedPackageNames());
        if (defaultMainfest == null) {
            getPackageManifestFactory().setDefaultPackageManifest(connection, retrieveRequest);
        } else {
            retrieveRequest.setUnpackaged(getPackageManifestFactory().convert(defaultMainfest));
        }

        RetrieveResultExt retrieveResultHandler = retrieveWork(connection, retrieveRequest, monitor);
        retrieveResultHandler.setProjectPackageList(projectPackageList);

        return retrieveResultHandler;
    }

    public RetrieveResultExt retrieveManagedInstalledPackages(IProject project, IProgressMonitor monitor)
            throws ForceConnectionException, ServiceException, ForceRemoteException, InterruptedException, ForceRemoteException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        Connection connection = getConnectionFactory().getConnection(project);
        return retrieveManagedInstalledPackages(connection, monitor);
    }

    public RetrieveResultExt retrieveManagedInstalledPackages(Connection connection, IProgressMonitor monitor)
            throws ForceConnectionException, ServiceException, ForceRemoteException,
            InterruptedException, ForceRemoteException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        ProjectPackageList projectPackageList =
                getProjectPackageFactory().getManagedInstalledProjectPackages(connection);

        if (Utils.isEmpty(projectPackageList)) {
            logger.info("No installed, managed packages to retrieve");
            return new RetrieveResultExt();
        }

        RetrieveRequest retrieveRequest = getRetrieveRequest();
        // installed, managed packages are saved to the project w/ package level (<package-name>/<comonent-type>/etc)
        retrieveRequest.setSinglePackage(false);

        if (Utils.isNotEmpty(projectPackageList.getNamedPackageNames())) {
            retrieveRequest.setPackageNames(projectPackageList.getNamedPackageNames());
        }

        RetrieveResultExt resultExt = retrieveWork(connection, retrieveRequest, monitor);
        resultExt.setProjectPackageList(projectPackageList);

        return resultExt;
    }

    /**
     * Support retrieve "selected" installed packages
     */
    public RetrieveResultExt retrieveInstalledPackages(
        IProject project,
        String[] packageNames,
        IProgressMonitor monitor
    ) throws ForceConnectionException, ServiceException, ForceRemoteException, InterruptedException, ForceRemoteException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        if (Utils.isEmpty(packageNames)) {
            logger.info("No installed, managed packages to retrieve");
            return null;
        }

        Connection connection = getConnectionFactory().getConnection(project);

        RetrieveRequest retrieveRequest = getRetrieveRequest();
        // installed, managed packages are saved to the project w/ package level (<package-name>/<comonent-type>/etc)
        retrieveRequest.setSinglePackage(false);
        retrieveRequest.setPackageNames(packageNames);

        RetrieveResultExt resultExt = retrieveWork(connection, retrieveRequest, monitor);
        resultExt.setProjectPackageList(getProjectPackageFactory().getManagedInstalledProjectPackages(connection,
            packageNames));
        return resultExt;
    }

    /**
     * Retrieve non-installed packages and their contents.
     */
    public RetrieveResultExt retrievePackage(IProject project, String packageName, IProgressMonitor monitor)
            throws ForceConnectionException, FactoryException, ServiceException, ForceRemoteException, ForceRemoteException, InterruptedException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        Connection connection = getConnectionFactory().getConnection(project);
        return retrievePackage(connection, project, packageName, monitor);
    }

    public RetrieveResultExt retrievePackage(
        Connection connection,
        IProject project,
        String packageName,
        IProgressMonitor monitor
    ) throws ForceConnectionException, FactoryException, ServiceException, ForceRemoteException, ForceRemoteException, InterruptedException {
        if ((project == null && connection == null) || Utils.isEmpty(packageName)) {
            throw new IllegalArgumentException("Package name and/or project and connection cannot be null");
        }

        if (connection == null) {
            connection = getConnectionFactory().getConnection(project);
        }

        String[] packageNames = null;
        Package defaultPackageManifest = null;
        if (Constants.DEFAULT_PACKAGED_NAME.equals(packageName)) {
            if (project != null) {
                defaultPackageManifest = getPackageManifestFactory().getDefaultPackageManifest(project);
            } else if (connection != null) {
                defaultPackageManifest = getPackageManifestFactory().getDefaultPackageManifest(connection);
            } else {
                throw new IllegalArgumentException(
                        "Unable to generate package manifest - project and connection are null");
            }
        } else {
            packageNames = new String[] { packageName };
        }

        return retrieveWork(connection, packageNames, defaultPackageManifest, monitor);
    }

    /**
     * Retrieve retrieve all type-specific components in a package.
     */
    public RetrieveResultExt retrieveComponentsForComponentTypes(
        Connection connection,
        String packageName,
        String[] componentTypes,
        IProgressMonitor monitor
    ) throws ServiceException, ForceRemoteException, InterruptedException {
        if (connection == null || Utils.isEmpty(packageName) || Utils.isEmpty(componentTypes)) {
            throw new IllegalArgumentException("Connection, package name, and/or object types cannot be null");
        }

        // TODO: currently the metadata api does not support object level selects for a non-default package
        Package packageManifest = null;
        String[] packageNames = null;
        if (Constants.DEFAULT_PACKAGED_NAME.equals(packageName)) {
            packageManifest = getPackageManifestFactory().createPackageManifestForComponentTypes(packageName, componentTypes);
        } else {
            packageNames = new String[] { packageName };
        }

        return retrieve(connection, packageNames, packageManifest, monitor);
    }

    /**
     * Retrieve individual component.
     */
    public RetrieveResultExt retrieveComponent(Component component, IProgressMonitor monitor)
            throws ForceConnectionException, ServiceException, ForceRemoteException, InterruptedException {
        if (component == null || component.getFileResource() == null) {
            throw new IllegalArgumentException("Component and/or file resource cannot be null");
        }
        Connection connection = getConnectionFactory().getConnection(component.getFileResource().getProject());
        return retrieveComponent(connection, component, monitor);
    }

    /**
     * Retrieve individual component.
     */
    public RetrieveResultExt retrieveComponent(IProject project, Component component, IProgressMonitor monitor)
            throws ForceConnectionException, ServiceException, ForceRemoteException, InterruptedException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        Connection connection = getConnectionFactory().getConnection(project);
        return retrieveComponent(connection, component, monitor);
    }

    /**
     * Retrieve individual component.
     */
    public RetrieveResultExt retrieveComponent(Connection connection, Component component, IProgressMonitor monitor)
            throws ServiceException, ForceRemoteException, InterruptedException {
        return retrieveComponent(connection, component, true, monitor);
    }

    /**
     * Retrieve individual component. Option to include metadata component to retrieve request.
     */
    public RetrieveResultExt retrieveComponent(
        Connection connection,
        Component component,
        boolean includeMetadata,
        IProgressMonitor monitor
    ) throws ServiceException, ForceRemoteException, InterruptedException {
        if (connection == null || component == null) {
            throw new IllegalArgumentException("Component and/or connection cannot be null");
        }

        ProjectPackageList projectPackageList = getProjectPackageListInstance();
        // REVIEWME: should this be an arg requirement?
        if (component.getFileResource() != null) {
            projectPackageList.setProject(component.getFileResource().getProject());
        } else {
            logger.warn("Project not provided, and will not be attached to returned project package");
        }

        projectPackageList.addComponent(component, includeMetadata);

        return retrieveSelective(connection, projectPackageList, true, monitor);
    }

    /**
     * Retrieve default package. Method assembles package manifest based on enabled object types.
     */
    public RetrieveResultExt retrieveDefaultPackage(Connection connection, IProgressMonitor monitor)
            throws ServiceException, ForceRemoteException, InterruptedException, ForceRemoteException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        Package defaultPackageManifest = getPackageManifestFactory().getDefaultPackageManifest(connection);
        return retrieveWork(connection, null, defaultPackageManifest, monitor);
    }

    public RetrieveResultExt retrieveSelective(ProjectPackageList projectPackageList, IProgressMonitor monitor)
            throws ForceConnectionException, ServiceException, ForceRemoteException, InterruptedException {
        return retrieveSelective(projectPackageList, true, monitor);
    }

    /**
     * Retrieve selective packages and their contents for a given project package list.
     */
    public RetrieveResultExt retrieveSelective(
        ProjectPackageList projectPackageList,
        boolean selective, 
        IProgressMonitor monitor
    ) throws ForceConnectionException, ServiceException, ForceRemoteException, InterruptedException {
        if (projectPackageList == null || projectPackageList.getProject() == null) {
            throw new IllegalArgumentException("Project package list and/or containing project cannot be null");
        }
        Connection connection = getConnectionFactory().getConnection(projectPackageList.getProject());
        return retrieveSelective(connection, projectPackageList, selective, monitor);
    }

    /**
     * Retrieve selective packages and their contents for a given project package list.
     */
    public RetrieveResultExt retrieveSelective(
        Connection connection,
        ProjectPackageList projectPackageList,
        boolean selective,
        IProgressMonitor monitor
    ) throws ServiceException, ForceRemoteException, InterruptedException {
        return retrieveSelective(connection, projectPackageList, selective, true, monitor);
    }

    public RetrieveResultExt retrieveSelective(
        Connection connection,
        String[] filePaths,
        String packageName,
        IProject project,
        IProgressMonitor monitor
    ) throws ServiceException, ForceRemoteException, FactoryException, InterruptedException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        logger.debug("Retrieving selective packages/components from " + connection.getLogDisplay());

        if (Utils.isEmpty(filePaths)) {
            logger.warn("Nothing to retrieve - filename list is empty");
            return null;
        }

        RetrieveRequest retrieveRequest = getRetrieveRequest();

        // set unpackaged manifest or package naming; if the former and the project's package.xml refers to a
        // package (name=<something>), adjust on the fly to retrieve unpacakged content using package.xml definitions
        Package packageManifest = null;
        if (Utils.isEmpty(packageName) || Constants.DEFAULT_PACKAGED_NAME.equals(packageName)) {
            if (project != null) {
                packageManifest = getPackageManifestFactory().getPackageManifest(project);
            } else {
                packageManifest = getPackageManifestFactory().createDefaultPackageManifest();
            }

            packageManifest.setFullName(packageName);
            retrieveRequest.setUnpackaged(getPackageManifestFactory().convert(packageManifest));
        } else {
            retrieveRequest.setPackageNames(new String[] { packageName });
        }

        monitorWork(monitor);
        RetrieveResultExt retrieveResultExt = retrieveWork(connection, retrieveRequest, monitor);
        return retrieveResultExt;
    }

    public RetrieveResultExt retrieveSelective(
        Connection connection,
        ProjectPackageList projectPackageList,
        boolean selective,
        boolean singlePackage,
        IProgressMonitor monitor
    ) throws ServiceException, ForceRemoteException, InterruptedException {
        if (connection == null || projectPackageList == null) {
            throw new IllegalArgumentException("Project package list and/or connection cannot be null");
        }

        if (projectPackageList.isEmpty()) {
            logger.warn("Nothing to retrieve - ProjectPackageList is empty");
            return null;
        }

        RetrieveRequest retrieveRequest = getRetrieveRequest();

        // specific packages
        retrieveRequest.setPackageNames(projectPackageList.getNamedPackageNames());

        // default package
        Package defaultPackageManifest = null;
        if (projectPackageList.hasPackage(Constants.DEFAULT_PACKAGED_NAME)) {
            if (selective) {
            	defaultPackageManifest = getPackageManifestFactory().createGenericDefaultPackageManifest();
            	Map<String, List<String>> packageManifestMap = Maps.newHashMap();
            	
            	ComponentList components = projectPackageList.getAllComponents(false);
            	List<String> types = components.getComponentTypes();
            	for (String type : types) {
            		List<String> members = packageManifestMap.get(type);
            		if (members == null) {
            			ComponentList componentListByType = components.getComponentListForComponentType(type);
            			// Using set since some types have two components with the same fullName  
            			Set<String> typeMembers = Sets.newHashSet();
            			
            			for (Component component : componentListByType) {
            				typeMembers.add(component.getFullName());
            			}
            			members = Lists.newArrayList(typeMembers);
            		} else {
            			continue;
            		}
            		packageManifestMap.put(type, members);
            	}
            	
                IProject project = projectPackageList.getProject();
				try {
					Package packageManifest =
					        getPackageManifestFactory().getPackageManifest(project, Constants.DEFAULT_PACKAGED_NAME);
					defaultPackageManifest.setVersion(packageManifest.getVersion());
				} catch (FactoryException e) {
	            	// Do nothing. Let it default to original endpoint version (latest by default)
				}
            	getPackageManifestFactory().addFileNamesToManifest(defaultPackageManifest, packageManifestMap);
            }
            retrieveRequest.setUnpackaged(getPackageManifestFactory().convert(defaultPackageManifest));
        }

        monitorWork(monitor);
        RetrieveResultExt retrieveResultExt = retrieveWork(connection, retrieveRequest, monitor);
        retrieveResultExt.setProjectPackageList(projectPackageList);
        return retrieveResultExt;
    }

    public RetrieveResultExt retrieveSelective(
        Connection connection,
        ProjectPackageList projectPackageList,
        boolean selective,
        Package packageManifest,
        IProgressMonitor monitor
    ) throws ServiceException, ForceRemoteException, InterruptedException {
        if (connection == null || projectPackageList == null) {
            throw new IllegalArgumentException("Project package list and/or connection cannot be null");
        }

        if (projectPackageList.isEmpty()) {
            logger.warn("Nothing to retrieve - ProjectPackageList is empty");
            return null;
        }

        RetrieveRequest retrieveRequest = getRetrieveRequest();

        // specific packages
        retrieveRequest.setPackageNames(projectPackageList.getNamedPackageNames());

        // default package
        if (packageManifest == null) {
            packageManifest = getPackageManifestFactory().getDefaultPackageManifest(projectPackageList);
        }

        retrieveRequest.setUnpackaged(getPackageManifestFactory().convert(packageManifest));

        monitorWork(monitor);
        RetrieveResultExt retrieveResultExt = retrieveWork(connection, retrieveRequest, monitor);
        retrieveResultExt.setProjectPackageList(projectPackageList);
        return retrieveResultExt;
    }

    /**
     * Retrieve selective packages and their contents for a given project package list.
     */
    public RetrieveResultExt retrieveSelective(
        ProjectPackageList projectPackageList,
        String[] componentTypes,
        IProgressMonitor monitor
    ) throws ForceConnectionException, ServiceException, ForceRemoteException, FactoryException, InterruptedException, ForceRemoteException, CoreException {
        if (projectPackageList == null || projectPackageList.getProject() == null) {
            throw new IllegalArgumentException("Package list and/or project cannot be null");
        }

        Connection connection = getConnectionFactory().getConnection(projectPackageList.getProject());
        return retrieveSelective(connection, projectPackageList, componentTypes, monitor);
    }

    public RetrieveResultExt retrieveSelective(
        Connection connection,
        ProjectPackageList projectPackageList,
        String[] componentTypes,
        IProgressMonitor monitor
    ) throws ForceConnectionException, ServiceException, ForceRemoteException, FactoryException, InterruptedException, ForceRemoteException, CoreException {
        if (projectPackageList == null || connection == null) {
            throw new IllegalArgumentException("Package list and/or connection cannot be null");
        }

        RetrieveRequest retrieveRequest = getRetrieveRequest();
        retrieveRequest.setPackageNames(projectPackageList.getNamedPackageNames());
        IProject project = projectPackageList.getProject();
        Package packageManifest = null;
        // using setSpecificFile machanism to refresh component folder(s) when component type(s) has associated
        // component type. Otherwise, refresh using sub-stanzas of package.xml
        if (projectPackageList.hasPackage(Constants.DEFAULT_PACKAGED_NAME)) {
            if (hasComponentTypeWthAssociatedComponentTypes(componentTypes)) {
                String[] filePaths = getFilePathsByComponentTypes(project, connection, componentTypes);
                if (logger.isDebugEnabled()) {
                    logRefreshFilePaths(filePaths);
                }

                if (projectPackageList.hasPackage(Constants.DEFAULT_PACKAGED_NAME)) {
                    packageManifest = getPackageManifestFactory().getDefaultPackageManifest(projectPackageList);

                }
            } else {
                packageManifest = getPackageManifestFactory().getPackageManifestForComponentTypes(
                    project,
                    Constants.DEFAULT_PACKAGED_NAME,
                    componentTypes,
                    true);
            }
            retrieveRequest.setUnpackaged(getPackageManifestFactory().convert(packageManifest));
        }

        RetrieveResultExt retrieveResultExt = retrieveWork(connection, retrieveRequest, monitor);
        retrieveResultExt.setProjectPackageList(projectPackageList);
        return retrieveResultExt;
    }

    private static void logRefreshFilePaths(String[] filePaths) {
        for (int i = 0; i < filePaths.length; i++) {
            logger.debug("file path [" + i + "], " + filePaths[i] + " is added to setSpecificFile for retrieve");
        }
    }

    private boolean hasComponentTypeWthAssociatedComponentTypes(String[] componentTypes) {
        boolean hasAssociatedComponentTypes = false;
        for (String componentType : componentTypes) {
            hasAssociatedComponentTypes = hasAssociatedComponentTypes 
                | getComponentFactory().hasAssociatedComponentTypes(componentType);
        }
        return hasAssociatedComponentTypes;
    }

    private String[] getFilePathsByComponentTypes(
        IProject project,
        Connection connection,
        String[] componentTypes
    ) throws FactoryException, ForceConnectionException, ForceRemoteException, CoreException, InterruptedException {
        List<String> filePathList = new ArrayList<>();
        List<ListMetadataQuery> queryList = new ArrayList<>();
        for (String componentType : componentTypes) {
            if (getComponentFactory().isWildCardSupportedComponentType(componentType)
                    && getPackageManifestFactory().isWildCardUsedForComponentType(project, componentType)) {
                // using listMetadata call to simulate wildcard
                Component componentInfo = getComponentFactory().getComponentByComponentType(componentType);
                ListMetadataQuery listMetadataQuery = new ListMetadataQuery();
                listMetadataQuery.setFolder(componentInfo.getDefaultFolder());
                listMetadataQuery.setType(componentInfo.getComponentType());
                queryList.add(listMetadataQuery);

            }
            // separate if statement for this check due to custom object component type could have * and specific member
            // entry for standard obj in package.xml
            if (getPackageManifestFactory().hasExplicitMemberForComponentType(project, componentType)) {
                List<String> filePathListForComponentType = getPackageManifestFactory().getFilePathsForComponentType(project, componentType);
                filePathList.addAll(filePathListForComponentType);
            }

            // take care of case where sub-component type is supported for retrieve independent from parent component type existence.
            // ex. custom field, validation rule can be retrieved independently from custom object.
            if (getComponentFactory().hasSubComponentTypesForComponentType(componentType)) {
                ProjectPackageList projectPackage = getProjectPackageListInstance();
                IFolder componentFolder = getProjectService().getComponentFolderByComponentType(project, componentType);
                projectPackage = getProjectPackageFactory().loadProjectPackageList(
                    componentFolder,
                    projectPackage,
                    false,
                    new NullProgressMonitor());
                List<String> filePaths = projectPackage.getFilePaths(true);
                filePathList.addAll(filePaths);
            }

        }

        // aggregate all listMetadata call into one call - reduce traffic.
        if (queryList.size() > 0) {
            ListMetadataQuery[] queryArray = queryList.toArray(new ListMetadataQuery[queryList.size()]);
            FileMetadataExt fileMetadataExt = getMetadataService().listMetadata(
                connection,
                queryArray,
                true,
                new NullProgressMonitor());
            if (fileMetadataExt.getFilePropertiesCount() > 0) {
                for (FileProperties fileProperties : fileMetadataExt.getFileProperties()) {
                    filePathList.add(fileProperties.getFileName());
                }
            }

        }
        return filePathList.toArray(new String[filePathList.size()]);
    }

    public RetrieveResultExt retrieveSelectiveSubComponentFolder(
        ProjectPackageList projectPackageList,
        String[] componentTypes,
        String subComponentFolder,
        IProgressMonitor monitor
    ) throws ForceConnectionException, ServiceException, ForceRemoteException, FactoryException, InterruptedException, ForceRemoteException, CoreException {
        if (projectPackageList == null || projectPackageList.getProject() == null) {
            throw new IllegalArgumentException("Package list and/or project cannot be null");
        }

        Connection connection = getConnectionFactory().getConnection(projectPackageList.getProject());
        return retrieveSelective(connection, projectPackageList, componentTypes, monitor);
    }

    public RetrieveResultExt retrieve(
        Connection connection,
        ProjectPackageList projectPackageList,
        IProgressMonitor monitor
    ) throws ServiceException, ForceRemoteException, InterruptedException {
        return retrieveSelective(connection, projectPackageList, false, monitor);
    }

    public RetrieveResultExt retrieve(
        IProject project,
        List<String> packageNames,
        IProgressMonitor monitor
    ) throws ForceConnectionException, ServiceException, ForceRemoteException, ForceRemoteException, InterruptedException, FactoryException {
        if (Utils.isEmpty(packageNames)) {
            return retrieveAll(project, monitor);
        }

        Package packageManifest = null;
        if (packageNames.contains(Constants.DEFAULT_PACKAGED_NAME)) {
            packageManifest = getPackageManifestFactory().getDefaultPackageManifest(project);
            packageNames.remove(Constants.DEFAULT_PACKAGED_NAME);
        }

        Connection connection = getConnectionFactory().getConnection(project);

        return retrieveWork(connection, packageNames.toArray(new String[packageNames.size()]), packageManifest, monitor);
    }

    /**
     * Retrieve packages and their contents for a given connection and project package list.
     */
    public RetrieveResultExt retrieve(
        Connection connection,
        String[] packageNames,
        Package packageManifest,
        IProgressMonitor monitor
    ) throws ServiceException, ForceRemoteException, InterruptedException {
        return retrieveWork(connection, packageNames, packageManifest, monitor);
    }

    /**
     * Retrieves asynchronous result from server for a given operation id.
     */
    public RetrieveResultExt getRetrieveResult(
        RetrieveResultExt retrieveResultExt,
        AsyncResult asyncResult,
        MetadataStubExt metadataStubExt,
        IProgressMonitor monitor
    ) throws ForceRemoteException, ServiceException, ForceRemoteException, InterruptedException {
        if (metadataStubExt == null) {
            throw new IllegalArgumentException("MetadataStubExt cannot be null");
        }

        monitorCheckSubTask(monitor, Messages.getString("Retrieve.PreparingResults"));

        RetrieveResult retrieveResult;
        try {
            IFileBasedResultAdapter result = waitForResult(
                new RetrieveResultAdapter(asyncResult, metadataStubExt),
                metadataStubExt,
                operationStats,
                monitor);
            retrieveResult = ((RetrieveResultAdapter) result).getRetrieveResult();

        } catch (ServiceTimeoutException e) {
            e.setMetadataResultExt(retrieveResultExt);
            throw e;
        }

        if (retrieveResultExt == null) {
            retrieveResultExt = new RetrieveResultExt();
        }

        retrieveResultExt.setRetrieveResult(retrieveResult);

        // log result
        logResult(retrieveResultExt);

        monitorWork(monitor);

        return retrieveResultExt;
    }

    public RetrieveResultExt handleRetrieveServiceTimeoutException(
        ServiceTimeoutException ex,
        String operation,
        IProgressMonitor monitor
    ) throws ForceRemoteException, ServiceException, ForceRemoteException, InterruptedException {
            
        // REVIEWME: ui-stuff (dialog to continue) should be handled outside of services
        boolean proceed = DialogUtils.getInstance().presentCycleLimitExceptionDialog(ex, monitor);
        if (proceed) {
            try {
                return getPackageRetrieveService().getRetrieveResult((RetrieveResultExt) ex.getMetadataResultExt(),
                    ex.getAsyncResult(), ex.getMetadataStubExt(), monitor);
            } catch (ServiceTimeoutException e) {
                return handleRetrieveServiceTimeoutException(e, operation, monitor);
            }
        } else {
            throw new InterruptedException("User canceled " + operation + " due to cycle polling limits reached: "
                    + ex.getMessage());
        }
    }

    // W O R K E R   B E E   R E T R I E V E   M E T H O D S
    private RetrieveResultExt retrieveWork(
        Connection connection,
        String[] packageNames,
        Package packageManifest,
        IProgressMonitor monitor
    ) throws ServiceException, ForceRemoteException, InterruptedException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        RetrieveRequest retrieveRequest = getRetrieveRequest();

        if (Utils.isNotEmpty(packageNames)) {
            retrieveRequest.setPackageNames(packageNames);
        }

        if (packageManifest != null) {
            retrieveRequest.setUnpackaged(getPackageManifestFactory().convert(packageManifest));
        }

        RetrieveResultExt resultExt = retrieveWork(connection, retrieveRequest, monitor);
        ProjectPackageList projectPackageList = getProjectPackageFactory().getProjectPackageListInstance(packageNames);

        resultExt.setProjectPackageList(projectPackageList);
        return resultExt;
    }

    private RetrieveResultExt retrieveWork(
        Connection connection,
        RetrieveRequest retrieveRequest,
        IProgressMonitor monitor
    ) throws ServiceException, ForceRemoteException, InterruptedException {
        if (connection == null || retrieveRequest == null) {
            throw new IllegalArgumentException("Connection and/or RetrieveRequest cannot be null");
        }

        monitorWorkCheck(monitor);

        if (Utils.isNotEmpty(retrieveRequest.getPackageNames())
            && (retrieveRequest.getPackageNames().length > 1 || retrieveRequest.getUnpackaged() != null)) {
            retrieveRequest.setSinglePackage(false);
        }

        // log retrieval details
        logRetrieve(connection, retrieveRequest);

        RetrieveResultExt retrieveResultExt = new RetrieveResultExt();
        try {
            // get metadata stub
            MetadataStubExt metadataStubExt = getMetadataFactory().getMetadataStubExt(connection);

            monitorWorkCheck(monitor);

            AsyncResult asyncResult = metadataStubExt.retrieve(retrieveRequest);
            monitorWork(monitor);

            // get async result
            retrieveResultExt = getRetrieveResult(retrieveResultExt, asyncResult, metadataStubExt, monitor);

        } catch (ServiceTimeoutException | InterruptedException | InsufficientPermissionsException e) {
            throw e;
        } catch (ServiceException e) {
            logger.warn("Unable to retrieve components: " + ForceExceptionUtils.getRootCauseMessage(e));
            throw new RetrieveException(e, connection, retrieveRequest);
        } catch (Exception e) {
            logger.error("Unable to retrieve components: " + ForceExceptionUtils.getRootCauseMessage(e));
            throw new RetrieveException(e, connection, retrieveRequest);
        }

        return retrieveResultExt;
    }

    // L O G G I N G
    private void logRetrieve(Connection connection, RetrieveRequest retrieveRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieve request from " + connection.getLogDisplay());
            StringBuffer strBuff = new StringBuffer();
            boolean defaultPackage = (retrieveRequest.getUnpackaged() != null ? true : false);
            String[] packageNames = retrieveRequest.getPackageNames();
            strBuff.append("Retrieval request of the following");
            if (Utils.isNotEmpty(packageNames)) {
                strBuff
                .append(" [")
                .append(defaultPackage ? packageNames.length + 1 : packageNames.length)
                .append("] packages: ");
                for (String packageName : packageNames) {
                    strBuff.append("'").append(packageName).append("' ");
                }
            } else {
                strBuff.append(" packages: (no named packages) ");
            }

            if (defaultPackage) {
                strBuff.append("'").append(Constants.DEFAULT_PACKAGED_NAME).append("' ");
            }

            String[] componentNames = retrieveRequest.getSpecificFiles();
            int componentCnt = 0;
            if (Utils.isNotEmpty(componentNames)) {
                strBuff.append("\nRequesting retrieval of the following ").append("[").append(componentNames.length)
                        .append("] components: ");
                for (String componentName : componentNames) {
                    strBuff.append("\n (").append(++componentCnt).append(") ").append(componentName);
                }
            }

            strBuff.append("\nsingle package = " + retrieveRequest.isSinglePackage());

            if (retrieveRequest.getUnpackaged() != null) {
                Package packageManifest = getPackageManifestFactory().convert(retrieveRequest.getUnpackaged());
                try {
                    strBuff.append("\npackage.xml =\n" + packageManifest.getXMLString());
                } catch (JAXBException e) {
                    logger.warn("Unable to get package.xml string: " + e.getMessage());
                }
            }

            logger.debug(strBuff.toString());
        } else {
            if (logger.isInfoEnabled()) {
                String[] packageNames = retrieveRequest.getPackageNames();
                int packageCount = 0;
                if (Utils.isNotEmpty(packageNames)) {
                    packageCount = packageNames.length;
                }
                packageCount = packageCount + (retrieveRequest.getUnpackaged() != null ? 1 : 0);
                logger.info("Retrieving [" + packageCount + "] packages from:\n " + connection.getLogDisplay());
            }
        }
    }

    private void logResult(RetrieveResultExt resultExt) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                "Retrieve result contains zip of size ["
                + (resultExt.getZipFile() != null ? resultExt.getZipFile().length : 0) 
                + "]");
            StringBuffer strBuff = new StringBuffer("Retrieved the following components in package(s) '");
            if (resultExt.getProjectPackageList() != null) {
                String[] packageNames = resultExt.getProjectPackageList().getPackageNames();
                for (int i = 0; i < packageNames.length; i++) {
                    strBuff.append(packageNames[i]);
                    if (i < packageNames.length - 1) {
                        strBuff.append(", ");
                    }
                }
            }
            strBuff.append("'");
            logger.debug(getFilePathLog(strBuff.toString(), resultExt.getZipFile()));

            RetrieveMessageExt messageHandler = resultExt.getMessageHandler();
            if (messageHandler != null) {
                messageHandler.logMessage();
            }
        } else if (logger.isInfoEnabled()) {
            int zipFileSize = 0;
            if (Utils.isNotEmpty(resultExt.getZipFile())) {
                zipFileSize = resultExt.getZipFile().length;
            }

            int fileCount = resultExt.getZipFileCount();
            logger.info("Retrieved zip file of size [" + zipFileSize + "] containing [" + fileCount + "] files");

            RetrieveMessageExt messageHandler = resultExt.getMessageHandler();
            if (messageHandler != null) {
                messageHandler.logMessage();
            }
        }
    }

}

class RetrieveResultAdapter implements IFileBasedResultAdapter {

    private final AsyncResult asyncResult;
    private RetrieveResult retrieveResult;
    private final MetadataStubExt metadataStubExt;

    public RetrieveResultAdapter(AsyncResult asyncResult, MetadataStubExt metadataStubExt) {
        this.asyncResult = asyncResult;
        this.metadataStubExt = metadataStubExt;
    }

    @Override
    public AsyncResult getAsyncResult() {
        return asyncResult;
    }

    @Override
    public IFileBasedResultAdapter checkStatus() throws ForceRemoteException {
        retrieveResult = metadataStubExt.checkRetrieveStatus(asyncResult.getId());
        return this;
    }

    @Override
    public boolean isDone() {
        return retrieveResult.isDone();
    }

    @Override
    public boolean isFailure() {
        return retrieveResult.getStatus() == RetrieveStatus.Failed;
    }

    @Override
    public String logStatus(Logger logger) {
        String status = "Retrieve result state is '" 
            + retrieveResult.getStatus().toString() 
            + "' for operation id '"
            + asyncResult.getId() 
            + "'";
        logger.debug(status);
        return status;
    }

    @Override
    public String logFailure(Logger logger) {
        StringBuffer strBuff = new StringBuffer()
            .append(retrieveResult.getErrorMessage())
            .append(" (")
            .append(retrieveResult.getStatus())
            .append(")");
        logger.warn("Retrieve operation from '" + metadataStubExt.getServerName() + "' failed: " + strBuff.toString());
        return strBuff.toString();
    }

    @Override
    public String logResult(Logger logger, OperationStats operationStats) {
        StringBuffer errorMessageBuffer = new StringBuffer()
            .append("\nOperation : ")
            .append(operationStats.getOperationName())
            .append("\nMessage : ")
            .append(retrieveResult.getMessages().toString())
            .append("\nState Detail : ")
            .append(retrieveResult.getStatus())
            .append("\nStatus Code : ")
            .append(retrieveResult.getStatus());

        logger.debug(errorMessageBuffer.toString());
        return errorMessageBuffer.toString();
    }

    @Override
    public String retrieveRealTimeStatusUpdatesIfAny() {
        if (retrieveResult != null && retrieveResult.getStatus() != null) {
            return Messages.getString(
                "Retrieve.ReportingStatus",
                new Object[] { retrieveResult.getStatus(), new Date() });
        }
        return Messages.getString(
            "PackageService.Polling",
            new Object[] { metadataStubExt.getServerName() });
    }

    public RetrieveResult getRetrieveResult() {
        return retrieveResult;
    }
}
