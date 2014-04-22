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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.project.ProjectModel;
import com.salesforce.ide.ui.internal.utils.UIMessages;

/**
 * 
 * @author cwall
 */
public class AddNatureAction extends BaseChangeNatureAction {

    private static final Logger logger = Logger.getLogger(AddNatureAction.class);

    protected ProjectController projectController = null;

    // C O N S T R U C T O R
    public AddNatureAction() throws ForceProjectException {
        super();
        projectController = new ProjectController();
    }

    // M E T H O D S
    @Override
    public void init() {}

    @Override
    public void execute(IAction action) {
        try {
            if (project.hasNature(DefaultNature.NATURE_ID)) {
                Utils.openInfo("Force.com Default Nature Exists",
                    "Force.com Default Nature already exists on project '" + project.getName() + "'.");
                return;
            }
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.error("Unable to apply Force.com Online Nature to project '" + project.getName() + "': "
                    + logMessage, e);
            Utils.openError(e, "Force.com Online Nature Error", "Problems adding Force.com Online Nature to project '"
                    + project.getName() + "': " + e.getMessage());
            return;
        }

        applyNature();
        updateDecorators();

        DialogUtils.getInstance().okMessage("Force.com Project Properties",
            UIMessages.getString("AddForceNature.Properties.message"), MessageDialog.WARNING);
        try {
            (new OpenForcePerspectiveAction()).run();
        } catch (ForceProjectException e) {
            logger.error("Unable to open Force Perspective", e);
        }
    }

    public void applyNature() {
        WorkspaceModifyOperation applyNatureOperation = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

                ProjectModel projectModel = new ProjectModel(project);
                projectModel.setContentSelection(ProjectController.NONE);
                projectController.setModel(projectModel);

                monitor.beginTask("Applying Force.com Nature", 4);

                try {
                    monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.CreateProject.label"));
                    projectController.generateProjectStructure(monitor);
                    monitor.worked(1);

                    monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.GenerateSchema.label"));
                    try {
                        projectController.generateSchemaFile(monitor);
                    } catch (IOException e) {
                        logger.error("Unable to generate schema file");
                    }
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

        IProgressService service = PlatformUI.getWorkbench().getProgressService();
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
