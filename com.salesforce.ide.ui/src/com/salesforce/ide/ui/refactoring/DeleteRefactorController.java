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
package com.salesforce.ide.ui.refactoring;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.PackageManifestDocumentUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.DeployMessageExt;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceTimeoutException;
import com.sforce.soap.metadata.DeployMessage;

/**
 * Service methods for refactoring functionality.
 *
 * @author cwall
 */
public class DeleteRefactorController extends BaseRefactorController {
    
    private static final Logger logger = Logger.getLogger(DeleteRefactorController.class);
    
    public DeleteRefactorController() {
        super();
        refactorModel = new RefactorModel();
    }
    
    /*
     * Validate delete candidates
     */
    public RefactoringStatus validateDelete(IProgressMonitor monitor)
        throws OperationCanceledException, InterruptedException {
        // REVIEWME: what to do w/ ResourceChangeChecker and/or ValidateEditChecker
        monitorCheck(monitor);
        
        RefactoringStatus refactoringStatus = new RefactoringStatus();
        ProjectPackageList projectPackageList = refactorModel.getProjectPackageList();
        if (projectPackageList == null) {
            logger.error("Project package list not prepared for delete candidates");
            refactoringStatus.addEntry(
                createFatalRefactoringStatusEntry(Constants.PLUGIN_NAME + " does not support deleting resource(s)"));
            return refactoringStatus;
        }
        
        Set<IResource> deleteCandidates = refactorModel.getChangeResources();
        if (Utils.isEmpty(deleteCandidates)) {
            refactoringStatus.addEntry(createInfoRefactoringStatusEntry("No resource(s) found to delete"));
            return refactoringStatus;
        }
        
        // for now, we assume there is only one delete candidate
        for (IResource deleteCandidate : deleteCandidates) {
            monitorCheck(monitor);
            if (Constants.PACKAGE_MANIFEST_FILE_NAME.equals(deleteCandidate.getName())) {
                logger.error(
                    Constants.PLUGIN_NAME + " does not support deleting resource '"
                        + deleteCandidate.getFullPath().toPortableString());
                refactoringStatus.addEntry(
                    createFatalRefactoringStatusEntry(
                        Constants.PLUGIN_NAME + " does not support deleting resource '"
                            + deleteCandidate.getFullPath().toPortableString()));
                continue;
            }
            
            monitorCheck(monitor);
            boolean forceManaged = ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                .isManagedResource(deleteCandidate);
            if (!forceManaged) {
                continue;
            }
            
            // evaluate based on resource type
            if (deleteCandidate.getType() == IResource.FILE) {
                // restrict deleting of force and package essential files
                if (Constants.SCHEMA_FILENAME.equals(deleteCandidate.getName())
                    || Constants.PACKAGE_MANIFEST_FILE_NAME.equals(deleteCandidate.getName())
                    || Constants.DESTRUCTIVE_MANIFEST_FILE_NAME.equals(deleteCandidate.getName())) {
                    logger.error(
                        Constants.PLUGIN_NAME + " does not support deleting '" + deleteCandidate.getName() + "'");
                    refactoringStatus.addEntry(
                        createFatalRefactoringStatusEntry(
                            Constants.PLUGIN_NAME + " does not support deleting '" + deleteCandidate.getName() + "'"));
                }
                
                continue;
            } else if (deleteCandidate.getType() == IResource.FOLDER) {
                // cannot delete package or "referenced packages" folders
                if (ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                    .isReferencedPackageResource(deleteCandidate)
                    || ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                        .isReferencedPackagesFolder(deleteCandidate)) {
                    logger.error(
                        Constants.PLUGIN_NAME + " does not support deleting 'Referenced Package' resource '"
                            + deleteCandidate.getName() + "'");
                    refactoringStatus.addEntry(
                        createFatalRefactoringStatusEntry(
                            Constants.PLUGIN_NAME + " does not support deleting 'Referenced Package' resource '"
                                + deleteCandidate.getName() + "'"));
                }
                
                // cannot delete src folder
                if (ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                    .isSourceFolder(deleteCandidate)) {
                    logger.error(
                        Constants.PLUGIN_NAME + " does not support deleting package source folder '"
                            + deleteCandidate.getName() + "'");
                    refactoringStatus.addEntry(
                        createFatalRefactoringStatusEntry(
                            Constants.PLUGIN_NAME + " does not support deleting package source folder '"
                                + deleteCandidate.getName() + "'"));
                }
                
                continue;
            } else if (deleteCandidate.getType() == IResource.PROJECT) {
                if (logger.isInfoEnabled()) {
                    logger.info("Delete request for resource '" + deleteCandidate.getFullPath().toPortableString());
                }
                continue;
            }
        }
        
        return refactoringStatus;
    }
    
    @Override
    public void finish(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, IOException {
        try {
            performRemoteDelete(monitor);
        } catch (OperationCanceledException e) {
            throw new InterruptedException(e.getMessage());
        } catch (ForceConnectionException e) {
            throw new InterruptedException(e.getMessage());
        } catch (ServiceException e) {
            throw new InterruptedException(e.getMessage());
        } catch (ForceRemoteException e) {
            throw new InterruptedException(e.getMessage());
        }
    }
    
    public List<RefactoringStatusEntry> performRemoteDelete(IProgressMonitor monitor)
        throws OperationCanceledException, InterruptedException, ForceConnectionException, ServiceException,
        ForceRemoteException, InvocationTargetException {
        if (refactorModel.isChangeResourcesEmpty()) {
            logger.warn("Unable to perform delete - delete resources is empty");
            return null;
        }
        
        List<RefactoringStatusEntry> entries = new ArrayList<>();
        
        // abort if online nature is not applied
        if (!ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .isManagedOnlineProject(refactorModel.getProject())) {
            logger.warn(
                "Unable to delete deploy for project '" + refactorModel.getProject().getName()
                    + "' - project is not online enabled");
            entries.add(
                createWarningRefactoringStatusEntry(
                    "Unable to test deploy for project '" + refactorModel.getProject().getName()
                        + "' - project is not online enabled"));
            return entries;
        }
        
        ProjectPackageList projectPackageList = refactorModel.getProjectPackageList();
        if (Utils.isEmpty(projectPackageList)) {
            logger.error("Unable to perform server delete - project package list is null");
            entries.add(createFatalRefactoringStatusEntry("Cannot perform delete - project package list is null"));
            return entries;
        }
        
        monitorCheck(monitor);
        
        // ensure that each project package has a delete manifest
        for (ProjectPackage projectPackage : projectPackageList) {
            if (projectPackage.getDeleteManifest() == null) {
                try {
                    ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageManifestFactory()
                        .attachDeleteManifest(projectPackage, true);
                } catch (Exception e) {
                    logger.error(
                        "Unable to perform delete - destructive manifest is missing or cannot be "
                            + "generated for package '" + projectPackage.getName() + "'",
                        e);
                    entries.add(
                        createFatalRefactoringStatusEntry(
                            "Unable to perform delete - destructive manifest is missing or cannot be "
                                + "generated for package '" + projectPackage.getName() + "': " + e.getMessage()));
                    return entries;
                }
                
                // and if it's STILL null, abort
                if (projectPackage.getDeleteManifest() == null) {
                    logger.error(
                        "Unable to perform delete - destructive manifest is missing or cannot be "
                            + "generated for package '" + projectPackage.getName() + "'");
                    entries.add(
                        createFatalRefactoringStatusEntry(
                            "Unable to perform delete - destructive manifest is missing or cannot be "
                                + "generated for package '" + projectPackage.getName() + "'"));
                    return entries;
                }
            }
        }
        
        // test delete, if we have any issues deleting, prompt the user to see if she wants to continue with only deleting locally
        testDeploy(monitor);
        
        boolean continueDelete = projectPackageList.hasComponents(false);
        if (!continueDelete) {
            logger.warn("No delete components found.- discontinuing remote delete");
            return entries;
        }
        
        // perform sdelete!!
        monitorCheck(monitor);
        DeployResultExt deployResultExt = null;
        try {
            deployResultExt = ContainerDelegate.getInstance().getServiceLocator().getPackageDeployService()
                .deployDelete(projectPackageList, false, monitor);
            // as part of delete deploy, ide request with auto update package and retrieve for getting updated package.xml
            ContainerDelegate.getInstance().getServiceLocator().getProjectService().handleRetrieveResult(
                projectPackageList,
                deployResultExt.getRetrieveResultHandler(),
                true,
                new String[] { Constants.PACKAGE_MANIFEST },
                monitor);
                
        } catch (ServiceTimeoutException ex) {
            deployResultExt = ContainerDelegate.getInstance().getServiceLocator().getPackageDeployService()
                .handleDeployServiceTimeoutException(ex, "remote delete", monitor);
        } catch (CoreException e) {
            logger.error("Unable to handle retrieve result from delete deploy ", e);
            throw new InvocationTargetException(e);
        } catch (IOException e) {
            logger.error("Unable to handle retrieve result from delete deploy ", e);
            throw new InvocationTargetException(e);
        }
        
        if (!deployResultExt.isSuccess()) {
            deployResultExt.getMessageHandler().sort(DeployMessageExt.SORT_RESULT);
            DeployMessage[] messages = deployResultExt.getMessageHandler().getMessages();
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("Remote delete failed with the following message. Will only delete locally.");
            for (DeployMessage deployMessage : messages) {
                strBuff.append("\n  ").append(deployMessage.getFullName()).append(": ")
                    .append(deployMessage.getProblem());
            }
            logger.error(strBuff.toString());
            entries.add(createWarningRefactoringStatusEntry(strBuff.toString()));
        } else {
            entries.add(createInfoRefactoringStatusEntry("Resources successfully deleted on server"));
            postDeleteProcess(projectPackageList, monitor);
            clearCaches(projectPackageList);
        }
        
        // set to skip builder since new component is validated/compiled during deploy()
        try {
            ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                .flagSkipBuilder(projectPackageList.getProject());
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to set builder skip flag: " + logMessage, e);
        }
        
        return entries;
    }
    
    protected void postDeleteProcess(final ProjectPackageList projectPackageList, final IProgressMonitor monitor) {
        // manually update cache inserting new component in appropriate stanza
        Job updateCacheJob = new Job("Update list metadata cache") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                if (Utils.isEmpty(projectPackageList)) {
                    return Status.OK_STATUS;
                }
                
                // set cache, may not exists
                URL cacheUrl = Utils.getCacheUrl(projectPackageList.getProject());
                Document packageManifestCache = Utils.loadDocument(cacheUrl);
                if (packageManifestCache != null) {
                    ComponentList deleteComponents = projectPackageList.getAllComponents();
                    for (Component deleteComponent : deleteComponents) {
                        Node componentTypeNode = PackageManifestDocumentUtils
                            .getComponentNode(packageManifestCache, deleteComponent.getComponentType());
                        if (componentTypeNode != null && componentTypeNode.hasChildNodes()) {
                            PackageManifestDocumentUtils.removeMemberNode(componentTypeNode, deleteComponent.getName());
                        }
                        
                        if (componentTypeNode != null && !componentTypeNode.hasChildNodes()) {
                            PackageManifestDocumentUtils
                                .removeComponentTypeNode(packageManifestCache, deleteComponent.getComponentType());
                        }
                    }
                    
                    try {
                        Utils.saveDocument(packageManifestCache, cacheUrl.getPath());
                        
                    } catch (Exception e) {
                        logger.warn("Unable to update cache with deleted component(s)", e);
                        return new Status(
                            IStatus.INFO,
                            ForceIdeCorePlugin.PLUGIN_ID,
                            IStatus.INFO,
                            "Unable to update cache with with deleted component(s)",
                            e);
                    }
                } else {
                    logger.warn("Unable to update cache with with deleted component(s)  - cache is not found");
                    return new Status(
                        IStatus.INFO,
                        ForceIdeCorePlugin.PLUGIN_ID,
                        IStatus.INFO,
                        "Unable to update cache with with deleted component(s)  - cache is not found",
                        null);
                }
                
                return Status.OK_STATUS;
            }
            
        };
        
        // perform now
        updateCacheJob.setSystem(true);
        updateCacheJob.schedule();
    }
    
    private ProjectController projectController = null;
    
    protected ProjectController getProjectController() {
        // lazy init
        if (projectController == null) {
            projectController = new ProjectController(getProject());
        }
        return projectController;
    }
}
