/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.handlers;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.project.ProjectModel;
import com.salesforce.ide.ui.actions.OpenForcePerspectiveAction;
import com.salesforce.ide.ui.internal.utils.UIMessages;

public final class AddNatureHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(AddNatureHandler.class);

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event).getWorkbench();
        final IProject project = getProjectChecked(event);
        try {
            if (project.hasNature(DefaultNature.NATURE_ID)) {
                Utils.openInfo("Force.com Default Nature Exists",
                    "Force.com Default Nature already exists on project '" + project.getName() + "'.");
                return null;
            }
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.error("Unable to apply Force.com Default Nature to project '" + project.getName() + "': "
                    + logMessage, e);
            Utils.openError(e, "Force.com Default Nature Error", "Problems adding Force.com Default Nature to project '"
                    + project.getName() + "': " + e.getMessage());
            return null;
        }

        applyNature(workbench, project);
        updateDecorators(workbench);

        DialogUtils.getInstance().okMessage("Force.com Project Properties",
            UIMessages.getString("AddForceNature.Properties.message"), MessageDialog.WARNING);
        (new OpenForcePerspectiveAction()).run();

        return null;
    }

    public static void applyNature(final IWorkbench workbench, final IProject project) {
        WorkspaceModifyOperation applyNatureOperation = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

                ProjectModel projectModel = new ProjectModel(project);
                projectModel.setContentSelection(ProjectController.NONE);

                final ProjectController projectController = new ProjectController();
                projectController.setModel(projectModel);

                monitor.beginTask("Applying Force.com Nature", 4);

                try {
                    monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.CreateProject.label"));
                    projectController.generateProjectStructure(monitor);
                    monitor.worked(1);

                    monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.GenerateSchema.label"));
                    projectController.generateSchemaFile(monitor);
                    monitor.worked(1);

                    if (!ContainerDelegate.getInstance().getServiceLocator().getProjectService().hasPackageManifest(project)) {
                        monitor.subTask(UIMessages
                            .getString("ProjectCreateWizard.CreateOperation.GenerateDefaultPackageManifest.label"));
                        projectController.savePackageManifest(monitor);
                        monitor.worked(1);
                    }

                } finally {
                    monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.AddNature.label"));
                    projectController.applyDefaultNature(monitor);
                    monitor.worked(1);
                    monitor.subTask("Done");
                }
            }
        };

        IProgressService service = workbench.getProgressService();
        try {
            service.run(false, true, applyNatureOperation);
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error("Unable to apply Force.com Online Nature to project '" + project.getName() + "'.",
                ForceExceptionUtils.getRootCause(e));
            Utils.openError(e, "Force.com Online Nature Error", "Problems adding Force.com Online Nature to project '"
                    + project.getName() + "': " + ForceExceptionUtils.getRootCauseMessage(e));
        }
    }

}
