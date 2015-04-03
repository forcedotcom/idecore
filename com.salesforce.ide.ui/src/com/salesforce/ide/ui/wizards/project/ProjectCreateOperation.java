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
package com.salesforce.ide.ui.wizards.project;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.project.ProjectModel;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.services.ServiceTimeoutException;
import com.salesforce.ide.ui.internal.BaseWorkspaceModifyOperation;
import com.salesforce.ide.ui.internal.startup.ForceStartup;
import com.salesforce.ide.ui.internal.utils.UIMessages;

/**
 * Creates Force.com project by establishing a Force.com connection, creating a project, saving preferences and loading
 * scontrols, packages, triggers, and the schema.
 * 
 * @author cwall
 */
public class ProjectCreateOperation extends BaseWorkspaceModifyOperation {

    private static final Logger logger = Logger.getLogger(ProjectCreateOperation.class);

    //  V A R I A B L E S
    protected ProjectController projectController = null;
    protected IRunnableContext progressContainer = null;
    protected boolean success = true; // determines if fetch components into project
    protected boolean abortProjectCreation = false;

    //  C O N S T R U C T O R S
    public ProjectCreateOperation(ProjectController projectWizardController, IRunnableContext container) {
        this.projectController = projectWizardController;
        this.progressContainer = container;
    }

    public ProjectCreateOperation(ProjectController projectWizardController) {
        this.projectController = projectWizardController;
    }

    //  M E T H O D S
    protected ProjectModel getProjectModel() {
        return projectController.getProjectModel();
    }

    public boolean create() throws InvocationTargetException, InterruptedException {
        // create new project
        if (progressContainer == null) {
            progressContainer = new ProgressMonitorDialog(getShell());
        }
        progressContainer.run(false, true, this);
        return success;
    }

    @Override
    protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
    InterruptedException {

        // start task
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        // disable manifest listener so retreive overwrite of package.xml does not trigger refresh dialog
        ForceStartup.removePackageManifestChangeListener();

        monitor.beginTask("", 20);

        try {
            // test and save connection
            monitorWork(monitor);
            monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.EstablishConnection.label"));
            saveConnection(monitor);
            monitorWork(monitor);

            // create project
            monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.CreateProject.label"));
            projectController.createProject(false, monitor);
            monitorWork(monitor);

            // save settings to project
            monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.SaveSettings.label"));
            projectController.saveSettings(monitor);
            monitorWork(monitor);

            // generate project structure including generic unpackaged/package.xml
            projectController.generateProjectStructure(monitor);
            monitorWork(monitor);

            // fetch components
            monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.FetchComponents.label"));
            fetchComponents(monitor);

            monitorWork(monitor);

            // schema
            monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.GenerateSchema.label"));
            projectController.generateSchemaFile(new SubProgressMonitor(monitor, 2));
            monitorWork(monitor);

            monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.OpenProject.label"));
            monitor.done();

            success = true;

            projectController.postFinish();

        } catch (InterruptedException e) {
            if (logger.isInfoEnabled()) {
                logger.info("Fetch remote components canceled by user");
            }
            success = false;
            monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.DeleteProject.label"));
            deleteProject(monitor);
            throw e;
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to finish '"
                    + (getProjectModel() != null && Utils.isNotEmpty(getProjectModel().getProjectName())
                    ? getProjectModel().getProjectName() : "") + "' project creation: " + logMessage, e);
            success = false;
            monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.DeleteProject.label"));
            deleteProject(monitor);
        } catch (Exception e) {
            logger.warn("Unable to finish '"
                    + (getProjectModel() != null && Utils.isNotEmpty(getProjectModel().getProjectName())
                    ? getProjectModel().getProjectName() : "") + "' project creation", e);
            success = false;
            monitor.subTask(UIMessages.getString("ProjectCreateWizard.CreateOperation.DeleteProject.label"));
            deleteProject(monitor);
        } finally {
            ForceStartup.addPackageManifestChangeListener();
        }
    }

    public void deleteProject(IProgressMonitor monitor) {
        projectController.cleanUp(monitor);
    }

    protected void saveConnection(IProgressMonitor monitor) throws InterruptedException, Exception {
        try {
            projectController.saveConnection(new SubProgressMonitor(monitor, 2));
        } catch (ForceException e) {
            boolean retry = DialogUtils.getInstance().retryConnection(e, monitor);
            if (retry) {
                saveConnection(monitor);
            } else {
                throw new ForceProjectException(e, "Unable to save connection");
            }
        }
    }

    protected void fetchComponents(IProgressMonitor monitor) throws InterruptedException, Exception {
        fetchComponents(null, monitor);
    }

    protected void fetchComponents(ServiceTimeoutException ex, IProgressMonitor monitor) throws InterruptedException,
    Exception {
        try {
            if (ex != null) {
                projectController.fetchComponents(ex, new SubProgressMonitor(monitor, 5));
            } else {
                projectController.fetchComponents(new SubProgressMonitor(monitor, 5));
            }
        } catch (ForceConnectionException e) {
            boolean retry = DialogUtils.getInstance().retryConnection(e, monitor);
            if (retry) {
                fetchComponents(monitor);
            } else {
                throw new ForceProjectException(e, "User cancelled fetch components due to connection exceptions");
            }
        } catch (ServiceTimeoutException e) {
            boolean proceed = DialogUtils.getInstance().presentCycleLimitExceptionDialog(e, monitor);
            if (proceed) {
                fetchComponents(e, monitor);
            } else {
                throw new ForceProjectException(e,
                        "User cancelled fetch components due to cycle polling limits reached");
            }
        } catch (InsufficientPermissionsException e) {
            e.setShowUpdateCredentialsMessage(false);
            DialogUtils.getInstance().presentInsufficientPermissionsDialog(e);
        } catch (Exception e) {
            DialogUtils.getInstance().presentFetchExceptionDialog(e, monitor);
        }
    }
}
