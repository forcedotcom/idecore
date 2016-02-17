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
package com.salesforce.ide.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.factories.FactoryLocator;
import com.salesforce.ide.core.factories.PackageManifestFactory;
import com.salesforce.ide.core.factories.ProjectPackageFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.core.services.PackageRetrieveService;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.core.services.ServiceLocator;
import com.salesforce.ide.ui.handlers.SynchronizeHandler;
import com.salesforce.ide.ui.internal.utils.UIMessages;

public abstract class ActionController {

    private static final Logger logger = Logger.getLogger(ActionController.class);

    protected IProject project = null;
    protected IWorkbenchWindow workbenchWindow = null;
    protected ISelection selection = null;
    protected List<IResource> selectedResources = null;
    protected boolean isInSync = true;

    //   C O N S T R U C T O R S
    public ActionController() {
        super();
    }

    //   M E T H O D S
    public ISelection getSelection() {
        return selection;
    }

    public void setSelection(ISelection selection) {
        this.selection = selection;
    }

    public IResource getSelectedResource() {
        if (Utils.isNotEmpty(selectedResources)) {
            return selectedResources.get(0);
        }
        return null;
    }

    public List<IResource> getSelectedResources() {
        return selectedResources;
    }

    public void setSelectedResources(List<IResource> selectedResources, boolean filter) {
        if (filter) {
            this.selectedResources = getProjectService().filterChildren(selectedResources);
        } else {
            this.selectedResources = selectedResources;
        }
    }

    public void setSelectedResources(List<IResource> selectedResources) {
        setSelectedResources(selectedResources, true);
    }

    public void addSelectedResource(IResource selectedResource) {
        if (selectedResources == null) {
            selectedResources = new ArrayList<>();
        }
        selectedResources.add(selectedResource);
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public ServiceLocator getServiceLocator() {
        return ContainerDelegate.getInstance().getServiceLocator();
    }

    public FactoryLocator getFactoryDelegate() {
        return ContainerDelegate.getInstance().getFactoryLocator();
    }

    public ProjectService getProjectService() {
        return ContainerDelegate.getInstance().getServiceLocator().getProjectService();
    }

    public ComponentFactory getComponentFactory() {
        return ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory();
    }

    public PackageManifestFactory getPackageManifestFactory() {
        return ContainerDelegate.getInstance().getFactoryLocator().getPackageManifestFactory();
    }

    public ConnectionFactory getConnectionFactory() {
        return ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory();
    }

    public ProjectPackageFactory getProjectPackageFactory() {
        return ContainerDelegate.getInstance().getFactoryLocator().getProjectPackageFactory();
    }

    public PackageRetrieveService getPackageRetrieveService() {
        return ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService();
    }

    public IWorkbenchWindow getWorkbenchWindow() {
        return workbenchWindow;
    }

    public void setWorkbenchWindow(IWorkbenchWindow workbenchWindow) {
        this.workbenchWindow = workbenchWindow;
    }

    public abstract boolean preRun();

    public abstract void postRun();

    /* (non-Javadoc)
     * @see com.salesforce.ide.ui.actions.IActionController#getWizardDialog()
     */
    public abstract WizardDialog getWizardDialog() throws Exception;

    protected boolean isWorkBenchWindowAvailable() {
        return workbenchWindow != null;
    }

    protected Shell getShell() {
        if (isWorkBenchWindowAvailable()) {
            return workbenchWindow.getShell();
        }
        return null;
    }

    protected boolean syncCheck(boolean cancel) {
        // proactively sync check against org to avoid overwriting updated content
        try {
            syncCheckWork();
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            // any insuff-org perms will cancel, but if not treat as a warning message
            if (cancel && cause instanceof InsufficientPermissionsException) {
                DialogUtils.getInstance()
                        .presentInsufficientPermissionsDialog((InsufficientPermissionsException) cause);
                return false;
            } else if (cancel && cause instanceof InvalidLoginException) {
                // log failure
                logger.warn("Unable to perform sync check: " + ForceExceptionUtils.getRootCauseMessage(cause));
                // choose further project create direction
                DialogUtils.getInstance().invalidLoginDialog(ForceExceptionUtils.getRootCauseMessage(cause));
                return false;
            } else {
                // if cancel and non-insuff org perms, treat as a wanring and cancel further ops, vs. a warning and continue
                if (cancel) {
                    logger.warn("Unable to perform sync check", ForceExceptionUtils.getRootCause(e));
                    DialogUtils.getInstance().cancelMessage(
                        UIMessages.getString("Deployment.SyncCheckError.title"),
                        UIMessages.getString("Deployment.SyncCheckError.message", new String[] { ForceExceptionUtils.getStrippedRootCauseMessage(cause) }),
                        MessageDialog.WARNING);
                } else {
                    logger.warn("Unable to perform sync check: " + ForceExceptionUtils.getRootCauseMessage(e));
                    DialogUtils.getInstance().continueMessage(
                        UIMessages.getString("Deployment.SyncCheckError.title"),
                        UIMessages.getString("Deployment.SyncCheckError.message", new String[] { ForceExceptionUtils.getStrippedRootCauseMessage(cause) }),
                        MessageDialog.WARNING);
                }
            }

        } catch (InterruptedException e) {
            logger.warn("Pre-save sync operation cancelled: " + e.getMessage());
        }

        return presentSyncPerspectiveDialog();
    }

    protected boolean presentSyncPerspectiveDialog() {
        // if determined to be out-of-sync, ask user what he/she wants to do
        if (!isInSync) {
            boolean openSyncAction = openOutOfSyncMsgBox();
            if (openSyncAction) {
            	Display.getDefault().asyncExec(() -> SynchronizeHandler.execute(PlatformUI.getWorkbench(), new StructuredSelection(project)));
                if (logger.isInfoEnabled()) {
                    logger.info("Canceling save to server and opening sync process");
                }
                return false;
            }
        }

        return true;
    }

    protected void syncCheckWork() throws InvocationTargetException, InterruptedException {
        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        service.run(true, true, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("Synchronize check against server", 2);
                try {
                    monitorWork(monitor);
                    boolean tmpInSync = false;
                    for (IResource resource : getSelectedResources()) {
                        ProjectService projectService = ContainerDelegate.getInstance().getServiceLocator().getProjectService();
                        tmpInSync = projectService.isResourceInSync(
                        		resource,
                                new SubProgressMonitor(monitor, 4));
                        if (!tmpInSync) {
                            isInSync = false;
                        }
                    }
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        });
    }

    protected boolean openOutOfSyncMsgBox() {
        return Utils.openQuestion("Project Not in Sync",
            "Project is not synchronized with the associated Salesforce organization.\n\n"
                    + "Do you want to cancel deployment and open Synchronize?");
    }

    //   P R O G R E S S   C H E C K S
    protected void monitorWorkCheck(IProgressMonitor monitor, String subtask) throws InterruptedException {
        monitorCheck(monitor);
        monitorWork(monitor, subtask);
    }

    protected void monitorWorkCheck(IProgressMonitor monitor) throws InterruptedException {
        monitorCheck(monitor);
        monitorWork(monitor);
    }

    protected void monitorCheckSubTask(IProgressMonitor monitor, String subtask) throws InterruptedException {
        monitorCheck(monitor);
        monitorSubTask(monitor, subtask);
    }

    protected void monitorCheck(IProgressMonitor monitor) throws InterruptedException {
        if (monitor != null) {
            if (monitor.isCanceled()) {
                throw new InterruptedException("Operation cancelled");
            }
        }
    }

    protected void monitorWork(IProgressMonitor monitor, String subtask) {
        if (monitor == null) {
            return;
        }

        monitor.subTask(subtask);
        monitor.worked(1);
        if (logger.isDebugEnabled()) {
            logger.debug(subtask);
        }
    }

    protected void monitorSubTask(IProgressMonitor monitor, String subtask) {
        if (monitor == null) {
            return;
        }

        monitor.subTask(subtask);
        if (logger.isDebugEnabled()) {
            logger.debug(subtask);
        }
    }

    protected void monitorWork(IProgressMonitor monitor) {
        if (monitor == null) {
            return;
        }

        monitor.worked(1);
    }

    protected void monitorDone(IProgressMonitor monitor) {
        if (monitor == null) {
            return;
        }

        monitor.done();
    }
}
