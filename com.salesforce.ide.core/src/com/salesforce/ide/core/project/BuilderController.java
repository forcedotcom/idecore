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
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.services.PackageDeployService;
import com.salesforce.ide.core.services.ServiceException;
import com.sforce.soap.metadata.DeployOptions;

/**
 * Main controller for the build process.
 * 
 * @author cwall
 * 
 */
public class BuilderController extends Controller {

    private static final double FIRST_TOOLING_API_VERSION = 27.0;

    private static final Logger logger = Logger.getLogger(BuilderController.class);

    protected boolean bubbleExceptions = false;
    protected boolean success = false;

    public BuilderController() {
        super();
    }

    public BuilderPayload getBuilderPayloadInstance() {
        return new BuilderPayload();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DeltaComponentSynchronizer getDeltaSynchronizer() {
        return new DeltaComponentSynchronizer();
    }

    public boolean isBubbleExceptions() {
        return bubbleExceptions;
    }

    public void setBubbleExceptions(boolean bubbleExceptions) {
        this.bubbleExceptions = bubbleExceptions;
    }

    public void build(ComponentList saveComponentList, IProject project, IProgressMonitor monitor) throws Exception {
        ForceProject forceProject =
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProject(project);

        // If the user has opted for Tooling API and it's applicable, use the faster Tooling API route
        if (forceProject.getPreferToolingDeployment() && isDeployableThroughToolingAPI(saveComponentList, forceProject)) {
            buildThroughTooling(saveComponentList, project, forceProject, monitor);
        } else { // Fallback to the Metadata API that supports all types
            buildThroughMetadata(saveComponentList, project, forceProject, monitor);
        }
    }

    private static boolean isDeployableThroughToolingAPI(ComponentList saveComponentList, ForceProject forceProject) {
        return saveComponentList.isDeployableThroughContainerAsyncRequest()
                && Float.parseFloat(forceProject.getEndpointApiVersion()) >= FIRST_TOOLING_API_VERSION;
    }

    private static void buildThroughTooling(ComponentList saveComponentList, IProject project, ForceProject forceProject,
            IProgressMonitor monitor) {
        if (saveComponentList.isNotEmpty()) {
            //TODO: Check for conflicts
            ContainerDelegate.getInstance().getServiceLocator().getToolingDeployService()
            .deploy(forceProject, saveComponentList, monitor);
        }
    }

    public void buildThroughMetadata(ComponentList saveComponentList, IProject project, ForceProject forceProject,
            IProgressMonitor monitor) throws Exception {
        if (saveComponentList.isNotEmpty()) {
            // set files to-be-saved in payload and filter out conflicts
            BuilderPayload savePayload = getBuilderPayloadInstance();
            savePayload.setProject(project);
            try {
                savePayload.loadPayload(saveComponentList, monitor);
                handleSaves(savePayload, monitor);
            } catch (Exception e) {
                handleException(savePayload, "Unable to perform saves", e);
            }
        }
    }

    private void handleSaves(BuilderPayload savePayload, IProgressMonitor monitor)
        throws ForceRemoteException, ForceConnectionException, ServiceException, InterruptedException, CoreException,
        IOException, InvocationTargetException, Exception {
        if (savePayload == null || savePayload.isEmpty() || !savePayload.hasSaveableComponents()) {
            if (logger.isInfoEnabled()) {}
            return;
        }
        ProjectPackageList loadedProjectPackageList = savePayload.getLoadedProjectPackageList();
        final PackageDeployService packageDeployService =
            ContainerDelegate.getInstance().getServiceLocator().getPackageDeployService();
        final DeployOptions deployOptions = makeDeployOptions(packageDeployService);
        
        DeployResultExt deployResultHandler =
            packageDeployService.deploy(loadedProjectPackageList, monitor, deployOptions);
        ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .handleDeployResult(loadedProjectPackageList, deployResultHandler, true, monitor);
    }

    protected DeployOptions makeDeployOptions(PackageDeployService packageDeployService) {
        final DeployOptions deployOptions = packageDeployService.makeDefaultDeployOptions(false);
        deployOptions.setIgnoreWarnings(true);
        return deployOptions;
    }

    private static void markAllDirty(IFile[] files, String msg) {
        if (Utils.isNotEmpty(files)) {
            for (IFile file : files) {
                MarkerUtils markerUtils = MarkerUtils.getInstance();
                markerUtils.clearAll(file);
                markerUtils.applyDirty(file);
                markerUtils.applySaveErrorMarker(file, 1, 1, 0, msg);
            }
        }
    }

    private void handleException(BuilderPayload payload, String message, Exception e) throws Exception {
        markAllDirty(
            payload.getFiles(),
            "Unable to perform save on all files: " + ForceExceptionUtils.getStrippedRootCauseMessage(e));
        if (bubbleExceptions) {
            throw e;
        }
    }

    @Override
    public void finish(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {}

    @Override
    public void init() {}
    
    @Override
    public void dispose() {}
    
    public class DeltaComponentSynchronizer implements IResourceDeltaVisitor {
        ComponentList saveComponentList = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getComponentListInstance();

        public DeltaComponentSynchronizer() {}

        @Override
        public boolean visit(IResourceDelta delta) {
            IResource resource = delta.getResource();

            if (delta.getKind() != IResourceDelta.ADDED && delta.getKind() != IResourceDelta.CHANGED) {
                logger.debug(
                    "Build does not support '" 
                    + delta.getKind() 
                    + "' delta on '"
                    + resource.getProjectRelativePath().toPortableString() 
                    + "'");
                return true;
            }
            
            // Don't track changes anywhere except the "src" folder
            if (resource.getType() == IResource.FOLDER
                    && resource.getParent() instanceof IProject
                    && !resource.getName().equals(Constants.SOURCE_FOLDER_NAME)
                    ) {
                logger.debug(
                    "Resource '"
                    + resource.getProjectRelativePath().toPortableString()
                    + "' and children excluded from build.");
                // Skip children
                return false;
            }

            if (!ContainerDelegate.getInstance().getServiceLocator().getProjectService().isBuildableResource(resource)) {
                logger.debug(
                    "Resource '"
                    + (resource.getType() == IResource.PROJECT ? resource.getName() : resource.getFullPath().toPortableString()) 
                    + "' is not a " 
                    + Constants.PLUGIN_NAME
                    + " managed file.  Excluding from build.");
                return true;
            }

            IFile file = (IFile) resource;
            try {
                switch (delta.getKind()) {
                case IResourceDelta.ADDED:
                    logger.info(
                        "Resource '" 
                        + file.getFullPath().toPortableString() 
                        + "' was added to project '"
                        + file.getProject().getName() 
                        + "'");
                    add(saveComponentList, file);
                    break;
                case IResourceDelta.CHANGED:
                    logger.info(
                        "Resource '" 
                        + file.getFullPath().toPortableString() 
                        + "' changed");
                    add(saveComponentList, file);
                    break;
                case IResourceDelta.REMOVED:
                    logger.info(
                        "Resource '" 
                        + file.getFullPath().toPortableString()
                        + "' was removed from project '" 
                        + file.getProject().getName() 
                        + "'");
                    logger.warn("Deletes not handled by builder");
                    break;
                default:
                    logger.warn(
                        "Delta '" 
                        + delta.getKind() 
                        + "' not handled for resource '"
                        + file.getProjectRelativePath().toPortableString());
                    break;
                }
            } catch (FactoryException e) {
                logger.error(
                    "Unable to handle file '" 
                    + file.getProjectRelativePath().toPortableString() 
                    + "': "
                    + e.getMessage());
                return false;
            }
            return true;
        }

        private void add(ComponentList componentList, IFile file) throws FactoryException {
            Component component =
                    ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
                    .getComponentFromFile(file);

            if (component.isInstalled()) {
                return;
            }

            componentList.add(component);
        }

        public ComponentList getSaveComponentList() {
            return saveComponentList;
        }

        public boolean isSaveEmpty() {
            return Utils.isEmpty(saveComponentList);
        }
    }
}
