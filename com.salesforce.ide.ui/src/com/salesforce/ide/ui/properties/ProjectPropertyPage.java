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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.project.ProjectModel;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public class ProjectPropertyPage extends BasePropertyPage {
    private static final Logger logger = Logger.getLogger(ProjectPropertyPage.class);

    private ProjectPropertyComposite projectPropertiesComposite = null;
    private ProjectController projectController = null;

    //   C O N S T R U C T O R
    public ProjectPropertyPage() {
        super();
        projectController = new ProjectController();
    }

    //   M E T H O D S
    public ProjectPropertyComposite getProjectPropertiesComposite() {
        return projectPropertiesComposite;
    }

    public ProjectController getProjectController() {
        return projectController;
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        enableButtons(false);
        UIUtils.setHelpContext(getControl(), this.getClass().getSimpleName());
    }

    protected IProject getProject() {
        return (IProject) getElement();
    }

    @Override
    protected Control createContents(Composite parent) {
        projectPropertiesComposite = new ProjectPropertyComposite(parent, SWT.NULL, this);
        initialize();

        return projectPropertiesComposite;
    }

    private void initialize() {
        projectPropertiesComposite.getTxtProjectName().setText(getProject().getName());
        projectPropertiesComposite.getTxtProjectName().setEnabled(false);
        loadFromPreferences();
    }

    // load saved connection settings from project preferences.
    private void loadFromPreferences() {
        ForceProject forceProject = getProjectService().getForceProject(getProject());
        if (forceProject != null) {
            projectPropertiesComposite.getTxtUsername().setText(forceProject.getUserName());
            projectPropertiesComposite.getTxtPassword().setText(forceProject.getPassword());
            projectPropertiesComposite.getTxtToken().setText(forceProject.getToken());
            if (projectPropertiesComposite.getTxtSessionId() != null) {
                projectPropertiesComposite.getTxtSessionId().setText(forceProject.getSessionId());
            }
            selectCombo(projectPropertiesComposite.getCmbEnvironment(), forceProject.getEndpointEnvironment());
            selectCombo(projectPropertiesComposite.getCmbEndpointServer(), forceProject.getEndpointServer());
            projectPropertiesComposite.getChkBoxResetEndpoint().setSelection(forceProject.isKeepEndpoint());
            projectPropertiesComposite.getSpnReadTimeout().setSelection(forceProject.getReadTimeoutSecs());
            if (projectPropertiesComposite.getChkBoxProtocol() != null) {
                projectPropertiesComposite.getChkBoxProtocol().setSelection(forceProject.isHttpsProtocol());
            }
            projectController.getProjectModel().setForceProject(forceProject);
        }
        // set visibility of advanced server stuff
        projectPropertiesComposite.enableServerEntryControls();
    }

    public void validateUserInput() {
        setValid(false);

        if (!validateOrganization(projectPropertiesComposite)) {
            enableButtons(false);
            return;
        }

        enableApplyButton(true);

        setValid(true);
    }

    public void saveUserInput() {
        ProjectModel projectModel = projectController.getProjectModel();
        saveProjectUserInput(projectModel.getForceProject(), projectPropertiesComposite, getSalesforceEndpoints());
        String environment = projectPropertiesComposite.getCmbEnvironmentString();
        projectModel.setEnvironment(environment);
        projectModel.setProjectName(projectPropertiesComposite.getTxtProjectNameString());
    }

    @Override
    protected void performDefaults() {
        return;
    }

    @Override
    protected void performApply() {
        boolean result = updateProject();
        enableButtons(result);
        setValid(result);
    }

    @Override
    public boolean performOk() {
        return updateProject();
    }

    private boolean updateProject() {
        try {
            // get input from user
            saveUserInput();

            // create connection operation to perform new project creation
            //projectController.setProject(getProject());
            ProjectUpdateOperation updateProjectOperation =
                    new ProjectUpdateOperation(projectController, getContainer());

            // perform project creation
            updateProjectOperation.update(new NullProgressMonitor());
            return true;
        } catch (InsufficientPermissionsException ex) {
            // log failure; option enabling occurs downstream
            logger.warn("Insufficient permissions to update project: " + ForceExceptionUtils.getRootCauseMessage(ex));

            // occurred during project create/update which determines message text
            ex.setShowUpdateCredentialsMessage(false);

            // show dialog
            DialogUtils.getInstance().presentInsufficientPermissionsDialog(ex);

            updateErrorStatus(Messages.getString("InsufficientPermissions.User.OrganizationSettings.message",
                new String[] { ex.getConnection().getUsername() }));

            return false;
        } catch (InvalidLoginException ex) {
            // log failure; option enabling occurs downstream
            logger.warn("Invalid login credentials: " + ForceExceptionUtils.getRootCauseMessage(ex));

            // occurred during project create/update which determines message text
            ex.setShowUpdateCredentialsMessage(false);

            // show dialog
            DialogUtils.getInstance().invalidLoginDialog(ForceExceptionUtils.getRootCauseMessage(ex));

            updateErrorStatus(UIMessages.getString(
                "ProjectCreateWizard.OrganizationPage.InvalidConnection.WithHost.message",
                new String[] { projectController.getProjectModel().getForceProject().getEndpointServer() }));

            return false;
        } catch (Exception e) {
            logger.error("Unable to update project properties.", e);
            Utils.openError("Project Update Error", "Unable to update project properties:\n\n"
                    + ForceExceptionUtils.getRootCauseMessage(e));
            return false;
        }
    }
}
