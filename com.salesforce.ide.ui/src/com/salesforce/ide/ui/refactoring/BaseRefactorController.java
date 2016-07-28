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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.swt.widgets.Display;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.MessageDialogRunnable;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.DeployMessageExt;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.services.DeployException;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceTimeoutException;
import com.salesforce.ide.ui.ForceIdeUIPlugin;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.sforce.soap.metadata.DeployMessage;

/**
 * Service methods for refactoring functionality.
 *
 * @author cwall
 */
public abstract class BaseRefactorController extends Controller {
    private static final Logger logger = Logger.getLogger(BaseRefactorController.class);

    protected RefactorModel refactorModel = null;

    public BaseRefactorController() {
        super();
    }

    public RefactorModel getRefactorModel() {
        return refactorModel;
    }

    public void setRefactorModel(RefactorModel refactorModel) {
        this.refactorModel = refactorModel;
    }

    /**
     * Evaluates copy candidate and aggregates sub-elements into a project package container.
     *
     * @param element
     * @param changeElements
     * @return
     * @throws CoreException
     * @throws FactoryException
     */
    protected ProjectPackageList loadProjectPackageList(IResource resource, IProgressMonitor monitor)
            throws FactoryException, CoreException, InterruptedException {
        return ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageFactory().loadProjectPackageList(resource, monitor);
    }

    protected ProjectPackageList addToProjectPackageList(IResource resource, IProgressMonitor monitor)
            throws FactoryException, CoreException, InterruptedException {
        return ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageFactory().loadProjectPackageList(resource, refactorModel.getProjectPackageList(), true,
            monitor);
    }

    protected String[] getEnabledComponentTypes(IProject project, IProgressMonitor monitor)
            throws InterruptedException, ForceConnectionException, ForceRemoteException {

        monitorCheck(monitor);
        // abort if online nature is not applied
        if (!ContainerDelegate.getInstance().getServiceLocator().getProjectService().isManagedOnlineProject(project)) {
            logger.error("Unable to get enabled types for project '" + project.getName()
                    + "' - project is not online enabled");
            List<String> registeredTypes = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getEnabledRegisteredComponentTypes();
            return registeredTypes.toArray(new String[registeredTypes.size()]);
        }

        monitorCheck(monitor);

        return ContainerDelegate.getInstance().getServiceLocator().getMetadataService().getEnabledComponentTypes(project);
    }

    protected boolean isEnabledComponentType(String[] enabledComponentTypes, Component component) {
        if (component.isPackageManifest()) {
            return true;
        }

        if (Utils.isEmpty(enabledComponentTypes)) {
            return false;
        }

        for (String enabledComponentType : enabledComponentTypes) {
            if (enabledComponentType.equals(component.getComponentType())) {
                return true;
            }
        }

        return false;
    }

    protected void testDeploy(IProgressMonitor monitor)
        throws OperationCanceledException, InterruptedException, ServiceException, ForceRemoteException {
        DeployResultExt deployResultExt = null;
        ProjectPackageList projectPackageList = refactorModel.getProjectPackageList();
        
        monitorCheck(monitor);
        
        try {
            try {
                deployResultExt = ContainerDelegate.getInstance().getServiceLocator().getPackageDeployService()
                    .deployDelete(projectPackageList, true, monitor);
            } catch (ServiceTimeoutException ex) {
                deployResultExt = ContainerDelegate.getInstance().getServiceLocator().getPackageDeployService()
                    .handleDeployServiceTimeoutException(ex, "test deploy", monitor);
            }
            
            if (!deployResultExt.isSuccess()) {
                deployResultExt.getMessageHandler().sort(DeployMessageExt.SORT_RESULT);
                DeployMessage[] deployMessages = deployResultExt.getMessageHandler().getMessages();
                for (DeployMessage deployMessage : deployMessages) {
                    if (!deployMessage.isSuccess()) {
                        logger.warn(
                            "Component '" + deployMessage.getFullName() + "' failed test delete: '"
                                + deployMessage.getProblem() + "'. will be deleted locally only.");
                                
                        projectPackageList.removeComponentByFilePath(deployMessage.getFileName(), true, true);
                        monitorCheck(monitor);
                        MessageDialogRunnable messageDialogRunnable = new MessageDialogRunnable(
                            "Remote Delete Error",
                            null,
                            UIMessages.getString(
                                "Refactor.Delete.Complete.CannotDelete.message",
                                new String[] { deployMessage.getFileName(), deployMessage.getProblem() }),
                            MessageDialog.WARNING,
                            new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL },
                            0);
                        Display.getDefault().syncExec(messageDialogRunnable);

                        if (messageDialogRunnable.getAction() == 1) {
                            logger.warn("Canceling delete operation");
                            throw new OperationCanceledException("Delete operation canceled");
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new OperationCanceledException(e.getMessage());
        } catch (DeployException e) {
            logger.error("Unable to perform server delete", e);
        } catch (ForceConnectionException e) {
            logger.warn("Unable to perform server delete", e);
        }
    }
    
    /**
     * Creates a RefactoringStatus of type INFO.
     *
     * @param message
     * @param destination
     * @param data
     * @return
     */
    protected RefactoringStatus createInfoRefactoringStatus(String message) {
        return createRefactoringStatus(RefactoringStatus.INFO, message);
    }

    /**
     * Creates a RefactoringStatus of type WARNING.
     *
     * @param message
     * @param destination
     * @param data
     * @return
     */
    protected RefactoringStatus createWarningRefactoringStatus(String message) {
        return createRefactoringStatus(RefactoringStatus.WARNING, message);
    }

    /**
     * Creates a RefactoringStatus of type ERROR.
     *
     * @param message
     * @param destination
     * @param data
     * @return
     */
    protected RefactoringStatus createErrorRefactoringStatus(String message) {
        return createRefactoringStatus(RefactoringStatus.ERROR, message);
    }

    /**
     * Creates a RefactoringStatus of type FATAL. FATAL status cancels refactor operation.
     *
     * @param message
     * @param destination
     * @param data
     * @return
     */
    protected RefactoringStatus createFatalRefactoringStatus(String message) {
        return createRefactoringStatus(RefactoringStatus.FATAL, message);
    }

    /**
     * Creates a RefactoringStatus of given severity. FATAL status cancels refactor operation.
     *
     * @param severity
     * @param message
     * @param destination
     * @param data
     * @return
     */
    protected RefactoringStatus createRefactoringStatus(int severity, String message) {
        ResourceRefactoringStatusContext status = null;
        if (refactorModel instanceof ChangeRefactorModel) {
            status = new ChangeRefactoringStatusContext((ChangeRefactorModel) refactorModel, message);
        } else {
            status = new ResourceRefactoringStatusContext(refactorModel, message);
        }

        return RefactoringStatus.createStatus(severity, message, status, ForceIdeUIPlugin.getPluginId(),
            RefactoringStatusEntry.NO_CODE, null);
    }

    protected RefactoringStatusEntry createInfoRefactoringStatusEntry(String message) {
        return createRefactoringStatusEntry(RefactoringStatus.INFO, message);
    }

    protected RefactoringStatusEntry createWarningRefactoringStatusEntry(String message) {
        return createRefactoringStatusEntry(RefactoringStatus.WARNING, message);
    }

    protected RefactoringStatusEntry createErrorRefactoringStatusEntry(String message) {
        return createRefactoringStatusEntry(RefactoringStatus.ERROR, message);
    }

    protected RefactoringStatusEntry createFatalRefactoringStatusEntry(String message) {
        return createRefactoringStatusEntry(RefactoringStatus.FATAL, message);
    }

    protected RefactoringStatusEntry createRefactoringStatusEntry(int severity, String message) {
        if (logger.isInfoEnabled()) {
            logger.info("Adding '" + getSeverityString(severity) + "' refactoring entry: " + message);
        }

        ResourceRefactoringStatusContext status = null;
        if (refactorModel instanceof ChangeRefactorModel) {
            status = new ChangeRefactoringStatusContext((ChangeRefactorModel) refactorModel, message);
        } else {
            status = new ResourceRefactoringStatusContext(refactorModel, message);
        }

        return new RefactoringStatusEntry(severity, message, status, ForceIdeUIPlugin.getPluginId(),
                RefactoringStatusEntry.NO_CODE, null);
    }

    protected String getSeverityString(int severity) {
        switch (severity) {
        case RefactoringStatus.ERROR:
            return "error";
        case RefactoringStatus.WARNING:
            return "warning";
        case RefactoringStatus.FATAL:
            return "fatal";
        default:
            return "info";
        }
    }

    protected void deleteSavedProjectContent(IResource resource) {
        if (resource.getType() != IResource.PROJECT) {
            return;
        }

        ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().removeConnection(resource.getName());
    }

    protected void clearCaches(ProjectPackageList projectPackageList) {
        if (Utils.isEmpty(projectPackageList)) {
            return;
        }

        ComponentList customObjects = projectPackageList.getComponentsByType(Constants.CUSTOM_OBJECT);
        if (Utils.isNotEmpty(customObjects) && projectPackageList.getProject() != null) {

            if (logger.isDebugEnabled()) {
                logger.debug("Clearing deleted custom object from cache");
            }

            for (Component customObject : customObjects) {
                ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getDescribeObjectRegistry().removeObject(
                    projectPackageList.getProject().getName(), customObject.getName());
            }
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public void init() throws ForceProjectException {

    }
}
