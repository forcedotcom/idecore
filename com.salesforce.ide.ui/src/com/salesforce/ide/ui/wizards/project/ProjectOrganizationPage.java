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
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.ui.internal.composite.BaseProjectComposite;
import com.salesforce.ide.ui.internal.utils.UIConstants;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.ui.packagemanifest.PackageManifestController;

public class ProjectOrganizationPage extends BaseProjectCreatePage {

    private static final Logger logger = Logger.getLogger(ProjectOrganizationPage.class);
    protected static final String OTHER_LABEL_NAME =
            UIMessages.getString("ProjectCreateWizard.OrganizationPage.OtherEnvironment.label");
    public static final String WIZARDPAGE_ID = "projectOrganizationWizardPage";
    private ProjectOrganizationComposite projectOrganizationComposite = null;

    public ProjectOrganizationPage(ProjectCreateWizard projectCreateWizard) {
        super(WIZARDPAGE_ID, projectCreateWizard);
    }

    /**
     * Assemble connection page wizard.
     */
    @Override
    public void createControl(Composite parent) {
        setWizardCosmetics(parent);
        projectOrganizationComposite = new ProjectOrganizationComposite(parent, SWT.NULL, this);
        setControl(projectOrganizationComposite);
        initialize();

        UIUtils.setHelpContext(projectOrganizationComposite, this.getClass().getSimpleName());
    }

    private void initialize() {
        initEnviornmentDefaults();
        projectOrganizationComposite.getTxtProjectName().setFocus();
        setComplete(false);
        if (Utils.isInternalMode() && Utils.hasDefaultProperties()) {
            setPropertyFileBasedProjectInputs(projectOrganizationComposite, "project");
            setPropertyFileBasedOrgInputs(projectOrganizationComposite, "project");
            validateUserInput();
            if (isPageComplete()) {
                getProjectController().setCanComplete(true);
            }
        }
    }

    @Override
    protected void setPropertyFileBasedProjectInputs(BaseProjectComposite projectComposite, String prefix) {
        Properties props = Utils.getDefaultProperties();
        if (props == null || props.isEmpty()) {
            return;
        }

        if (Utils.isNotEmpty(props.getProperty(prefix + ".name"))) {
            projectComposite.getTxtProjectName().setText(props.getProperty(prefix + ".name"));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Set default project properties from prop file");
        }

        setPageComplete(true);
    }

    // set defaults and/or last used
    private void initEnviornmentDefaults() {
        Combo cmbEndpointServers = projectOrganizationComposite.getCmbEndpointServer();
        Combo cmbEnvironment = projectOrganizationComposite.getCmbEnvironment();

        // environment
        String lastEnvironmentSelected = ForceIdeCorePlugin.getPreferenceString(Constants.LAST_ENV_SELECTED);
        String selectedEndpointLabel = getSalesforceEndpoints().getDefaultEndpointLabel();
        if (Utils.isNotEmpty(lastEnvironmentSelected)
                && (getSalesforceEndpoints().isValidEndpointLabel(lastEnvironmentSelected) || OTHER_LABEL_NAME
                        .equals(lastEnvironmentSelected))) {
            selectCombo(cmbEnvironment, lastEnvironmentSelected);
        } else if (Utils.isNotEmpty(selectedEndpointLabel)) {
            selectCombo(cmbEnvironment, selectedEndpointLabel);
        } else {
            cmbEnvironment.select(0);
        }

        // server
        String lastServerSelected = ForceIdeCorePlugin.getPreferenceString(Constants.LAST_SERVER_SELECTED);
        if (Utils.isNotEmpty(lastServerSelected)) {
            selectCombo(cmbEndpointServers, lastServerSelected);
        } else {
            cmbEndpointServers.select(0);
        }

        // keep endpoint and protocol
        boolean lastKeepEndpointSelected =
                ForceIdeCorePlugin.getPreferenceBoolean(Constants.LAST_KEEP_ENDPOINT_SELECTED);
        projectOrganizationComposite.getChkBoxResetEndpoint().setSelection(lastKeepEndpointSelected);

        String username = ForceIdeCorePlugin.getPreferenceString(Constants.LAST_USERNAME_SELECTED);
        if (Utils.isNotEmpty(username)) {
            projectOrganizationComposite.setTxtUsername(username);
        }

        // set visibility of advanced server stuff
        projectOrganizationComposite.enableServerEntryControls();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setTitleAndDescription(UIMessages.getString("ProjectCreateWizard.OrganizationPage.title"), UIMessages
                .getString("ProjectCreateWizard.OrganizationPage.description"));
        }
        super.setVisible(visible);
    }

    // validate user input
    public void validateUserInput() {
        boolean valid = validateProjectSettings(projectOrganizationComposite);
        setPageComplete(valid);
    }

    // validates project and org settings
    protected boolean validateProjectSettings(BaseProjectComposite baseProjectComposite) {
        return validateProject(baseProjectComposite) && validateOrganization(baseProjectComposite);
    }

    // validates project settings
    protected boolean validateProject(BaseProjectComposite projectComposite) {
        String projectName = projectComposite.getTxtProjectNameString();
        if (projectComposite.getTxtProjectName().getEnabled() && Utils.isEmpty(projectName)) {
            updateInfoStatus(UIMessages.getString(UIConstants.MSG_PROJECT_NAME_EMPTY));
            return false;
        }

        if (Utils.containsInvalidChars(projectName)) {
            updateErrorStatus(UIMessages.getString("ProjectCreateWizard.OrganizationPage.InvalidChar.message"));
            return false;
        }

        IResource container = null;
        if (projectComposite.getTxtProjectName().getEnabled() && Utils.isNotEmpty(projectName)) {
            container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(projectName));
            if (container != null) {
                updateErrorStatus(UIMessages.getString(UIConstants.MSG_PROJECT_NAME_UNIQUE));
                return false;
            }
        }

        updateInfoStatus(null);

        return true;
    }

    // saves valid user input
    public void saveUserInput() {
        ForceProject forceProject = getProjectModel().getForceProject();
        if (forceProject == null) {
            forceProject = new ForceProject();
        }

        // generic save of project and org stuff
        saveUserInput(forceProject, projectOrganizationComposite);
        saveEndpointInput(forceProject, projectOrganizationComposite, getSalesforceEndpoints());

        // save to model to be worked on later
        String environment = projectOrganizationComposite.getCmbEnvironmentString();
        getProjectModel().setEnvironment(environment);
        getProjectModel().setForceProject(forceProject);
        getProjectModel().setProjectName(projectOrganizationComposite.getTxtProjectNameString());
        getProjectModel().clearConnections();
    }

    @Override
    public IWizardPage getNextPage() {
        // only perform remote operations if org settings changed
        if (projectOrganizationComposite.isOrgModified()) {
            // reset modify flag
            projectOrganizationComposite.setOrgModified(false);

            // save updated input
            saveUserInput();

            new PackageManifestController().clearCache();

            // pre-load org's packages and enabled component types
            if (!prepareNextPage()) {
                return this;
            }
        }

        return super.getNextPage();
    }

    protected boolean prepareNextPage() {
        try {
            projectWizard.getProjectProjectContentPage().enableServerContentOptions();
            loadOrgDetails();
        } catch (InvocationTargetException e) {

            // notify if connection failed
            projectWizard.getProjectProjectContentPage().disableServerContentOptions();

            // reset finish ability
            setComplete(false);
            projectOrganizationComposite.setOrgModified(true);

            // cancel/prevent further project create operations for in InsufficientOrgPermissions exceptions
            if (e.getTargetException() instanceof InsufficientPermissionsException) {
                // log failure; option enabling occurs downstream
                logger
                .warn("Insufficient permissions to create project: "
                        + ForceExceptionUtils.getRootCauseMessage(e));

                InsufficientPermissionsException ex = (InsufficientPermissionsException) e.getTargetException();
                // occurred during project create/update which determines message text
                ex.setShowUpdateCredentialsMessage(false);

                // show dialog
                DialogUtils.getInstance().presentInsufficientPermissionsDialog(ex);

                updateErrorStatus(Messages.getString("InsufficientPermissions.User.OrganizationSettings.message",
                    new String[] { ex.getConnection().getUsername() }));
                return false;
            } else if (e.getTargetException() instanceof InvalidLoginException) {
                // log failure
                logger.warn("Unable to login: " + ForceExceptionUtils.getRootCauseMessage(e.getTargetException()));

                updateErrorStatus(UIMessages.getString(
                    "ProjectCreateWizard.OrganizationPage.InvalidConnection.WithHost.message",
                    new String[] { getProjectModel().getForceProject().getEndpointServer() }));

                DialogUtils.getInstance().abortMessage(
                    "Login Failed",
                    UIMessages.getString(
                    "ProjectCreateWizard.OrganizationPage.InvalidConnection.WithHostAndException.message",
                    new String[] { getProjectModel().getForceProject().getEndpointServer(),
                            ForceExceptionUtils.getRootExceptionMessage(e) }));
                return false;
            } else {
                // log failure; ask user what he/she wants to do
                logger.warn("Unable to load org details - package names and component enablement", ForceExceptionUtils
                    .getRootCause(e));

                // choose further project create direction
                String[] params =
                        new String[] { getProjectModel().getForceProject().getUserName(),
                        ForceExceptionUtils.getRootExceptionMessage(e) };
                String message =
                        UIMessages.getString(
                            "ProjectCreateWizard.OrganizationPage.FetchOrgDetails.GenericError.message", params);

                updateErrorStatus(UIMessages.getString(
                    "ProjectCreateWizard.OrganizationPage.FetchOrgDetails.GenericError.Status.message",
                    new String[] { getProjectModel().getForceProject().getUserName() }));

                int action = createOfflineAbortRetryMessage("Unknown Error", message);
                switch (action) {
                case 0: /* create offline */
                    if (logger.isDebugEnabled()) {
                        logger.debug("Continue offline project create");
                    }
                    return true;
                case 1: /* abort */
                    if (logger.isDebugEnabled()) {
                        logger.debug("Aborting connection validation in prep of project content selection page");
                    }
                    return false;
                default: /* retry */
                    if (logger.isDebugEnabled()) {
                        logger.debug("Retry connection validation");
                    }
                    return prepareNextPage();
                }
            }

        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        }

        return true;
    }

    private int createOfflineAbortRetryMessage(String title, String message) {
        // retry is the default
        MessageDialog dialog =
                new MessageDialog(getShell(), title, null, message, MessageDialog.ERROR, new String[] {
                    "Create Offline", IDialogConstants.ABORT_LABEL, IDialogConstants.RETRY_LABEL },
                    IDialogConstants.RETRY_ID);
        return dialog.open();
    }

    private void loadOrgDetails() throws InvocationTargetException, InterruptedException {
        final IProgressService service = PlatformUI.getWorkbench().getProgressService();
        service.run(false, false, new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                monitor.beginTask("Fetching organization details...", 3);
                monitor.worked(1);

                try {
                    // clear previous data, if applicable
                    getProjectController().clearOrgDetails();

                    // get package names
                    monitor.subTask("Fetching packages...");
                    getProjectController().loadRemotePackageNames(new SubProgressMonitor(monitor, 3));
                    monitor.worked(1);

                    monitor.subTask("Preparing to fetch component metadata...");
                    getProjectController().prepareFileMetadataQueries(monitor);
                    monitor.worked(1);

                    monitor.beginTask("Fetching component metadata...", getProjectController().queriesCount() > 0
                        ? getProjectController().queriesCount() + 1 : IProgressMonitor.UNKNOWN);
                    monitor.worked(1);
                    getProjectController().loadFileMetadata(monitor);

                } catch (InterruptedException e) {
                    logger.warn("Operation cancelled: " + e.getMessage());
                } catch (Throwable e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        });
    }

    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }
}
