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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.factories.FactoryLocator;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.core.services.ServiceLocator;

public abstract class BaseAction extends Action implements IObjectActionDelegate {
    private static final Logger logger = Logger.getLogger(BaseAction.class);

    protected IProject project = null;
    protected IWorkbenchPart targetPart = null;
    protected ISelection selection = null;
    protected List<IResource> selectedResources = null;
    protected IWorkbenchWindow workbenchWindow = null;
    protected ActionController actionController = null;
    protected ServiceLocator serviceLocator = null;
    protected FactoryLocator factoryLocator = null;
    protected Shell shell = null;

    //   C O N S T R U C T O R
    public BaseAction() {
        super();

        serviceLocator = ContainerDelegate.getInstance().getServiceLocator();
        factoryLocator = ContainerDelegate.getInstance().getFactoryLocator();
    }

    //   M E T H O D S
    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public FactoryLocator getFactoryLocator() {
        return factoryLocator;
    }

    public IResource getSelectedResource() {
		return (Utils.isNotEmpty(selectedResources))? selectedResources.get(0):null;
    }

    public List<IResource> getSelectedResources() {
        return selectedResources;
    }

    public void addSelectedResource(IResource selectedResource) {
        if (selectedResources == null) {
            selectedResources = new ArrayList<>();
        }
        selectedResources.add(selectedResource);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            setSelection(selection);
        } else {
            this.selection = StructuredSelection.EMPTY;
        }

        if (actionController != null) {
            actionController.setSelection(selection);
        }

        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            List<IResource> selectedResources = new ArrayList<>();
            for (Iterator<?> iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
                Object selectedObject = iterator.next();
                if (!(selectedObject instanceof IResource)) {
                    continue;
                }

                IResource selectedResource = (IResource) selectedObject;
                selectedResources.add(selectedResource);

                project = selectedResource.getProject();
            }

            this.selectedResources = filter(selectedResources);
            if (actionController != null) {
                actionController.setProject(project);
                actionController.setSelectedResources(this.selectedResources, false);
            }
        }
    }

    protected List<IResource> filter(List<IResource> selectedResources) {
        return getProjectService().filterChildren(selectedResources);
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
        this.workbenchWindow = targetPart.getSite().getWorkbenchWindow();
        if (actionController != null) {
            actionController.setWorkbenchWindow(workbenchWindow);
        }
    }

    public void setShell(Shell shell) {
        this.shell = shell;
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public IWorkbenchPart getTargetPart() {
        return targetPart;
    }

    public void setTargetPart(IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    public ISelection getSelection() {
        return selection;
    }

    public void setSelection(ISelection selection) {
        this.selection = selection;
    }

    public IWorkbenchWindow getWorkbenchWindow() {
        return workbenchWindow;
    }

    public void setWorkbenchWindow(IWorkbenchWindow window) {
        this.workbenchWindow = window;
    }

    public IWorkbench getWorkbench() {
        return (workbenchWindow != null)?workbenchWindow.getWorkbench():null;
    }

    public ActionController getActionController() {
        return actionController;
    }

    public void setActionController(ActionController actionController) {
        this.actionController = actionController;
    }

    // service helpers
    public ProjectService getProjectService() {
        return serviceLocator.getProjectService();
    }

    public ConnectionFactory getConnectionFactory() {
        return factoryLocator.getConnectionFactory();
    }

    public ComponentFactory getComponentFactory() {
        return factoryLocator.getComponentFactory();
    }

    // actual work execution; subclasses impl lifecycle methods
    @Override
    public final void run(IAction action) {
        init();

        boolean execute = actionController != null ? actionController.preRun() : true;
        if (!execute) {
            logger.warn("Pre-run failed.  Action not executed.");
            return;
        }

        execute(action);

        if (actionController != null) {
            actionController.postRun();
        }
    }

    //   L I F E C Y C L E   M E T H O D S
    public void init() {
    // may be implemented by subclasses
    }

    public abstract void execute(IAction action);

    // utils
    protected Shell getShell() {
        return workbenchWindow != null ? workbenchWindow.getShell() : Display.getDefault().getActiveShell();
    }

    protected void centerOnScreen(WizardDialog pDialog) {
        Shell shell = pDialog.getShell();
        Point size = shell.getSize();
        Rectangle screenBounds = Display.getDefault().getBounds();

        int x = (screenBounds.width - size.y) / 2;
        int y = (screenBounds.height - size.y) / 2;
        Rectangle bounds = new Rectangle(x, y, size.x, size.y);
        shell.setBounds(bounds);
    }

    protected void monitorWorkCheck(IProgressMonitor monitor, String subtask) throws InterruptedException {
        monitorCheck(monitor);
        monitorWork(monitor, subtask);
    }

    protected void monitorCheck(IProgressMonitor monitor) throws InterruptedException {
        if (monitor != null) {
            monitor.worked(1);
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
}
