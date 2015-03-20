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
package com.salesforce.ide.ui.properties;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.IPreferencePageContainer;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.project.ProjectModel;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.MetadataStubExt;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.wizards.project.ProjectCreateOperation;
import com.sforce.ws.ConnectionException;

public class ProjectUpdateOperation extends ProjectCreateOperation {

    private static final Logger logger = Logger.getLogger(ProjectUpdateOperation.class);

    private boolean fetchComponents = true;

    public ProjectUpdateOperation(ProjectController projectController, IRunnableContext container) {
        super(projectController, container);
    }

    public ProjectUpdateOperation(ProjectController projectController, IPreferencePageContainer container) {
        super(projectController);
    }

    public void update(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException,
            ForceConnectionException, InsufficientPermissionsException {
        ProjectModel projectModel = projectController.getProjectModel();
        ForceProject updatedProject = projectModel.getForceProject();
        IProject project = projectModel.getProject();
        ForceProject existingProject = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProject(project);

        if (existingProject.isOrgChange(updatedProject)) {
            ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().removeConnection(existingProject);
            ContainerDelegate.getInstance().getFactoryLocator().getMetadataFactory().removeMetadataStubExt(existingProject);
            ContainerDelegate.getInstance().getFactoryLocator().getToolingFactory().removeToolingStubExt(existingProject);
            ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(updatedProject);

            fetchComponents = checkRefreshProject();
            if (progressContainer == null) {
                progressContainer = new ProgressMonitorDialog(getShell());
            }
            progressContainer.run(false, true, this);
        } else if (!updatedProject.equals(existingProject)){
            // adjust timeout, if changed
            if (existingProject.getReadTimeoutSecs() != updatedProject.getReadTimeoutSecs()) {
                try {
                    MetadataStubExt metadataStubExt =
                            ContainerDelegate.getInstance().getFactoryLocator().getMetadataFactory().getMetadataStubExt(existingProject);
                    metadataStubExt.setTimeout(updatedProject.getReadTimeoutSecs());
                } catch (ForceRemoteException e) {
                    logger.warn("Unable to update timeout");
                }
            }

            projectController.saveSettings(monitor);
        }
    }

    public boolean checkRefreshProject() {
        if (projectController.getProjectModel().isSilentUpdate()) {
            return true;
        }

        return Utils.openQuestion("Refresh Project Contents?", UIMessages
                .getString("ProjectUpdate.UpdateOperation.RefreshProject.message")
                + " '" + projectController.getProjectModel().getProjectName() + "'?");
    }

    @Override
    protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("Updating of '" + projectController.getProjectModel().getProjectName()
                            + "' Force.com project");
        }

        boolean onlineNature = true;

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        monitor.beginTask("", 7);

        monitor.subTask("Updating " + projectController.getProjectModel().getProjectName() + " Force.com project");

        monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.SaveSettings.label"));
        projectController.saveSettings(monitor);
        monitorWork(monitor);

        try {
            monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.EstablishConnection.label"));
            projectController.saveConnection(monitor);
            monitorWork(monitor);

            if (fetchComponents) {
                monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.FetchComponents.label"));
                try {
                    projectController.getProjectModel().setContentSelection(ProjectController.REFRESH);
                    fetchComponents(monitor);
                } catch (Exception e) {
                    onlineNature = false;
                    logger.error("Unable to fetch components from Salesforce.", e);
                    Utils.openWarning(e, true, Messages.getString("General.FetchError.message"));
                }
                monitorWork(monitor);
            }

            projectController.disableBuilder();

            monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.GenerateSchema.label"));
            projectController.generateSchemaFile(monitor);
            monitorWork(monitor);
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                onlineNature = false;
            }

            logger.error("Unable to update " + projectController.getProjectModel().getProjectName()
                    + " Force.com project creation.", e);
            Utils.openWarning(e, true, "Unable to update " + projectController.getProjectModel().getProjectName()
                    + " Force.com project creation.");
        } finally {
            if (onlineNature) {
                try {
                    projectController.applyNatures(new NullProgressMonitor());
                } catch (CoreException e) {
                    String logMessage = Utils.generateCoreExceptionLog(e);
                    logger.error("Unable to apply Force.com Online Nature: " + logMessage, e);
                }
            }

            monitor.done();
        }
    }
}
