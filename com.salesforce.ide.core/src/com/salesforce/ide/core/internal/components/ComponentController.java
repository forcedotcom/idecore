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
package com.salesforce.ide.core.internal.components;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.PackageManifestDocumentUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.project.MarkerUtils;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.core.remote.registries.DescribeObjectRegistry;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceLocator;
import com.salesforce.ide.core.services.ServiceTimeoutException;
import com.sforce.soap.metadata.FileProperties;

public abstract class ComponentController extends Controller {
    private static final Logger logger = Logger.getLogger(ComponentController.class);

    protected DeployResultExt deployResultHandler = null;

    public ComponentController(ComponentModel componentWizardModel) throws ForceProjectException {
        super();
        model = componentWizardModel;
        init();
    }

    @Override
    public void init() throws ForceProjectException {
        getComponentWizardModel()
            .setComponentFactory(ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory());
        getComponentWizardModel().initComponent();
    }
    
    public ComponentModel getComponentWizardModel() {
        return (ComponentModel) model;
    }

    public void setComponentWizardModel(ComponentModel componentWizardModel) {
        this.model = componentWizardModel;
    }

    public void setResources(IProject project) {
        getComponentWizardModel().setProject(project);
    }

    public String getComponentType() {
        return getComponentWizardModel().getComponent().getComponentType();
    }

    public Component getComponent() {
        return getComponentWizardModel().getComponent();
    }

    public DeployResultExt getDeployResultExt() {
        return deployResultHandler;
    }

    public void setDeployResultExt(DeployResultExt deployResultExt) {
        this.deployResultHandler = deployResultExt;
    }

    public Component getNewComponentByComponentType(String componentType) {
        return ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
            .getComponentByComponentType(componentType);
    }

    public IFile getComponentResoruce() {
        return getComponentWizardModel().getComponent().getFileResource();
    }

    public void clean() {
        model = null;
    }

    public boolean isComponentEnabled() throws ForceConnectionException, ForceRemoteException, InterruptedException {
        final ComponentModel componentWizardModel = getComponentWizardModel();
        
        final IProject project = componentWizardModel.getProject();
        if (project == null) {
            return false;
        }
        
        final Map<String, Boolean> cache = getComponentTypeEnablementCache(project);
        
        final String componentType = componentWizardModel.getComponentType();
        final Boolean cachedResult = cache.get(componentType);
        if (null != cachedResult)
            return cachedResult.booleanValue();
            
        final ServiceLocator serviceLocator = ContainerDelegate.getInstance().getServiceLocator();
        ForceProject forceProject = serviceLocator.getProjectService().getForceProject(project);
        
        Connection connection =
            ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(forceProject);
        boolean componentEnabled =
            serviceLocator.getMetadataService().isComponentTypeEnabled(connection, componentType);
        logger.debug(
            componentWizardModel.getComponent().getDisplayName() 
            + " " 
            + (componentEnabled ? "are" : "are not")
            + " enabled");
                
        cache.put(componentType, componentEnabled);
        return componentEnabled;
    }

    private static final QualifiedName KEY_ENABLEMENT_CACHE = new QualifiedName(Constants.FILE_PROP_PREFIX_ID,
            "componentTypeEnablementCache");

    private static Map<String, Boolean> getComponentTypeEnablementCache(final IProject project) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> cache = Map.class.cast(project.getSessionProperty(KEY_ENABLEMENT_CACHE));
            if (null == cache) {
                cache = new HashMap<>();
                project.setSessionProperty(KEY_ENABLEMENT_CACHE, cache);
            }
            return cache;
        } catch (final CoreException e) {
            logger.error("Unable to access session property: " + KEY_ENABLEMENT_CACHE, e);
            return Collections.emptyMap();
        }
    }

    public boolean isNameUnique(IProgressMonitor monitor) throws ForceConnectionException, ForceRemoteException, InterruptedException {
        return (isNameUniqueLocalCheck() && isNameUniqueRemoteCheck(monitor));
    }

    public boolean isNameUniqueLocalCheck() {
        final Component componentToCheck = getComponent();
        final String dirPath = (componentToCheck != null)
            ? (new StringBuffer(Constants.SOURCE_FOLDER_NAME).append(File.separator)
                .append(componentToCheck.getDefaultFolder())).toString()
            : null;
            
        final String fileName = (componentToCheck != null)
            ? (new StringBuffer(componentToCheck.getName()).append(".").append(componentToCheck.getFileExtension()))
                .toString()
            : null;
            
        final String componentName_Absolute =
            (dirPath != null && fileName != null) ? (new StringBuffer(dirPath).append(fileName)).toString() : null;
            
        logger.info(
            "Ensure local uniqueness for '" 
            + (null == componentName_Absolute ? "" : componentName_Absolute.toString())
            + "'");
                
        return (null != componentToCheck && !componentToCheck.isCaseSensitive())
            ? checkInFolder(dirPath, fileName)
            : true;
    }

    protected boolean checkInFolder(final String dirPath, final String fileName) {
        IFolder folder = getSourceFolder(dirPath.toString());
        if (folder != null && folder.exists()) {
            IResource[] folderMembers = null;
            try {
                folderMembers = folder.members();
            } catch (CoreException e) {
                String logMessage = generateLogMessageFromCoreException(e);
                logger.warn("Unable to get contents for folder '" + folder.getProjectRelativePath().toPortableString()
                        + "': " + logMessage, e);
                return true;
            }

            if (Utils.isNotEmpty(folderMembers)) {
                for (IResource folderMember : folderMembers) {
                    if (folderMember.getType() == IResource.FILE
                            && folderMember.getName().equalsIgnoreCase(fileName.toString())) {
                        logger.warn("Component resource '" + fileName.toString() + "' already exists in project");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    protected String generateLogMessageFromCoreException(CoreException e) {
        String logMessage = Utils.generateCoreExceptionLog(e);
        return logMessage;
    }

    protected IFolder getSourceFolder(final String path) {
        return getComponentWizardModel().getProject().getFolder(path);
    }

    public boolean isNameUniqueRemoteCheck(IProgressMonitor monitor) throws InsufficientPermissionsException,
            ForceConnectionException, ForceRemoteException, InterruptedException {
        if (!isProjectOnlineEnabled()) {
            logger.warn("Remote unique name check aborted - project is not online enabled");
            return true;
        }

        final Component component = getComponent();
        if (Utils.isEmpty(component.getMetadataFilePath())) {
            StringBuffer strBuff = new StringBuffer();
            
            strBuff.append(component.getDefaultFolder()).append("/").append(component.getName()).append(".")
                .append(component.getFileExtension());
                
            if (logger.isDebugEnabled()) {
                logger.debug("Ensure remote uniqueness for '" + strBuff.toString() + "'");
            }
            
            component.setFilePath(strBuff.toString());
        }
        
        return checkIfComponentExistsOnServer(monitor, component);
    }

    /**
     * @return true if it doesn't exist on server
     */
    protected boolean checkIfComponentExistsOnServer(IProgressMonitor monitor, final Component component)
            throws ForceConnectionException, ForceRemoteException, InterruptedException,
            InsufficientPermissionsException {
        final FileMetadataExt listMetadataRetrieveResult = fireListMetadataQuery(monitor, component);
        boolean notFoundOnServer = true;
        if (listMetadataRetrieveResult.hasFileProperties()) {
            final FileProperties[] fileProps = listMetadataRetrieveResult.getFileProperties();
            for (FileProperties fp : fileProps) {
                if (fp.getFullName().equalsIgnoreCase(component.getName())) {
                    notFoundOnServer = false;
                    break;
                }
            }
        }
        return notFoundOnServer;
    }

    protected FileMetadataExt fireListMetadataQuery(IProgressMonitor monitor, final Component component)
            throws ForceConnectionException, ForceRemoteException, InterruptedException,
            InsufficientPermissionsException {
        final FileMetadataExt listMetadataRetrieveResult =
                ContainerDelegate
                        .getInstance()
                        .getServiceLocator()
                        .getMetadataService()
                        .listMetadata(
                            ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory()
                                    .getConnection(getComponentWizardModel().getProject()), component, monitor);
        return listMetadataRetrieveResult;
    }

    protected boolean isProjectOnlineEnabled() {
        return ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                .isManagedOnlineProject(getComponentWizardModel().getProject());
    }

    @Override
    public void finish(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, IOException,
            ForceConnectionException, ForceRemoteException, FactoryException, CoreException, ServiceException,
            JAXBException, Exception {
        if (getComponentWizardModel() == null || getComponentWizardModel().getProject() == null) {
            throw new IllegalArgumentException("Component model and/or project cannot be null");
        }

        logger.debug("***   C R E A T E   C O M P O N E N T   ***");

        monitorWorkCheck(monitor, "Creating new " + getComponentWizardModel().getDisplayName() + "...");

        // create and load components from user input including metadata, if applicable
        ComponentList components = getComponentWizardModel().getLoadedComponents();

        if (Utils.isEmpty(components)) {
            logger.error("Unable to create component.");
            Utils.openError(new Exception("Unable to create component.  Component is null."),
                "Unable to create component", null);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Creating " + components.get(0).getFullDisplayName());
        }

        // prepare container to perform save and deploy ops
        ProjectPackageList projectPackageList =
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageListInstance();
        projectPackageList.setProject(getComponentWizardModel().getProject());
        projectPackageList.addComponents(components, false);

        // perform pre-save actions
        defaultPreSaveProcess(getComponentWizardModel(), monitor);
        preSaveProcess(getComponentWizardModel(), monitor);

        // save to f/s - enables offline work
        monitorSubTask(monitor, "Saving " + getComponentWizardModel().getDisplayName() + " to project...");
        projectPackageList.saveResources(new String[] { getComponentWizardModel().getComponent().getComponentType() },
            monitor);
        monitorWorkCheck(monitor);

        // commit to server
        monitorSubTask(monitor, "Saving " + getComponentWizardModel().getDisplayName() + " to server...");
        deploy(projectPackageList, monitor);

        // performing post-save actions
        postSaveProcess(getComponentWizardModel(), monitor);
    }

    /**
     * Override this method to perform actions before saving component to server and project.
     * 
     * @param componentWizardModel
     * @param monitor
     * @throws InvocationTargetException
     */
    protected abstract void preSaveProcess(ComponentModel componentWizardModel, IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException;

    protected final void defaultPreSaveProcess(ComponentModel componentWizardModel, IProgressMonitor monitor)
            throws InterruptedException {
        // test for remote uniqueness
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Validating name uniquness of " + getComponentWizardModel().getDisplayName());
            }
            if (isProjectOnlineEnabled() && !isNameUniqueRemoteCheck(monitor)) {
                logger.warn("Found remote instance of " + getComponentWizardModel().getDisplayName());
                StringBuffer strBuff = new StringBuffer("Found existing ");
                strBuff.append(getComponentWizardModel().getDisplayName()).append(" instance named '")
                        .append(getComponentWizardModel().getName())
                        .append("' in source organization.\n\nOverwrite remote instance?");
                boolean ovewrite = Utils.openConfirm("Duplicate Component Found", strBuff.toString());
                if (!ovewrite) {
                    logger.warn("User cancelled new component operation - will not overwrite remote instance");
                    throw new InterruptedException(
                            "User cancelled new component operation - will not overwrite remote instance");
                }
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (RuntimeException | ForceConnectionException | ForceRemoteException e) {
            logger.error("Unable to test remote uniqueness", e);
        }

        logger.debug("Saving newly created component(s)");
    }

    /**
     * Override this method to perform actions after saving component to server and project
     * 
     * @param componentWizardModel
     * @param monitor
     */
    protected void postSaveProcess(final ComponentModel componentWizardModel, final IProgressMonitor monitor) {
        // manually update cache inserting new component in appropriate stanza
        Job updateCacheJob = new Job("Update list metadata cache") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                // set cache, may not exists
                URL cacheUrl = Utils.getCacheUrl(getProject());
                Document packageManifestCache = Utils.loadDocument(cacheUrl);
                if (packageManifestCache != null) {
                    Node componentTypeNode =
                            PackageManifestDocumentUtils.addComponentTypeNode(packageManifestCache,
                                componentWizardModel.getComponentType());

                    PackageManifestDocumentUtils.addMemberNode(componentTypeNode, componentWizardModel.getName());

                    try {
                        Utils.saveDocument(packageManifestCache, cacheUrl.getPath());
                    } catch (Exception e) {
                        logger.warn(
                            "Unable to update cache with new component " + componentWizardModel.getDisplayName(), e);
                        return new Status(IStatus.INFO, ForceIdeCorePlugin.PLUGIN_ID, IStatus.INFO,
                                "Unable to update cache with new component " + componentWizardModel.getDisplayName(), e);
                    }

                } else {
                    logger.warn("Unable to update cache with new component " + componentWizardModel.getDisplayName()
                            + " - cache is not found");
                    return new Status(IStatus.INFO, ForceIdeCorePlugin.PLUGIN_ID, IStatus.INFO,
                            "Unable to update cache with new component " + componentWizardModel.getDisplayName()
                                    + " - cache is not found", null);
                }

                return Status.OK_STATUS;
            }

        };

        // perform now
        updateCacheJob.setSystem(true);
        updateCacheJob.schedule();
    }

    protected void deploy(ProjectPackageList projectPackageList, IProgressMonitor monitor)
            throws ForceConnectionException, FactoryException, InterruptedException, CoreException, IOException,
            ServiceException, ForceRemoteException, InvocationTargetException, Exception {

        if (!isProjectOnlineEnabled()) {
            logger.warn("Remote save aborted - project is not online enabled");
            // TODO: apply dirty marker to file?
            ComponentList components = projectPackageList.getAllComponents();
            for (Component component : components) {
                if (!component.isPackageManifest()) {
                    MarkerUtils.getInstance().applyDirty(component.getFileResource(),
                        Messages.getString("Markers.OfflineOnlySavedLocally.message"));
                }
            }
            return;
        }

        // compile and save new component
        try {
            deployResultHandler = ContainerDelegate.getInstance().getServiceLocator().getPackageDeployService()
                .deploy(projectPackageList, monitor);
        } catch (ServiceTimeoutException ex) {
            deployResultHandler =
                    ContainerDelegate
                            .getInstance()
                            .getServiceLocator()
                            .getPackageDeployService()
                            .handleDeployServiceTimeoutException(ex, "create new " + getComponent().getDisplayName(),
                                monitor);
        }

        if (deployResultHandler == null) {
            logger.error("Unable to finalize new component creation - deploy result is null");
            return;
        }

        ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .handleDeployResult(projectPackageList, deployResultHandler, true, monitor);
    }

    // get all object names
    public SortedSet<String> getObjectNames(boolean refresh) throws ForceConnectionException, ForceRemoteException {
        IProject project = getComponentWizardModel().getProject();

        if (project == null) {
            return null;
        }

        Connection connection =
            ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(project);
        DescribeObjectRegistry describeObjectRegistry =
            ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getDescribeObjectRegistry();
        return describeObjectRegistry.getCachedDescribeSObjectNames(connection, project.getName(), refresh);
    }

    @Override
    public void dispose() {}
}
