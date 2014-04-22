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
package com.salesforce.ide.ui.wizards.components;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;

import com.salesforce.ide.core.internal.components.ComponentController;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;

/**
 * Base class for creating components.  Subclasses perform service operation to create component type object.
 *
 * @author cwall
 */
public class ComponentCreateOperation extends WorkspaceModifyOperation {

    private static final Logger logger = Logger.getLogger(ComponentCreateOperation.class);

    protected ComponentController componentController = null;
    protected ComponentWizard componentWizard = null;

    public ComponentCreateOperation(ComponentController componentController, ComponentWizard componentWizard) {
        this.componentController = componentController;
        this.componentWizard = componentWizard;
    }

    @Override
    protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
    InterruptedException {
        try {
            // create component
            componentController.finish(monitor);

            openEditor(componentController.getComponent().getFileResource(), componentController.getComponent(),
                monitor);
            if (monitor != null) {
                monitor.worked(1);
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (InsufficientPermissionsException e) {
            DialogUtils.getInstance().presentInsufficientPermissionsDialog(e);
        } catch (Exception e) {
            logger.error("Unable to save or retrieve component", e);
            Utils.openError(e, true, "Unable to create new " + componentController.getComponent().getFullDisplayName()
                + ".");
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }
    }

    protected void openEditor(final IFile file, Component component, IProgressMonitor monitor) {
        final IFile editableFile = file;

        monitor.setTaskName("Opening " + component.getFullDisplayName() + " for editing...");

        if (componentWizard.getShell() == null || componentWizard.getShell().getDisplay() == null) {
            logger.warn("Unable to open default component editor - shell and/or display is null");
            return;
        }

        componentWizard.getShell().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try {
                    IDE.openEditor(page, editableFile, true);
                } catch (PartInitException ex) {
                    logger.warn("Unable to open file '" + file.getProjectRelativePath().toPortableString() + "'", ex);
                }
            }
        });
    }
}
