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
package com.salesforce.ide.deployment.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.deployment.ForceIdeDeploymentPlugin;
import com.salesforce.ide.deployment.internal.utils.DeploymentConstants;
import com.salesforce.ide.deployment.internal.utils.DeploymentMessages;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

/**
 *
 * 
 * @author cwall
 */
public class DeploymentDestinationSettingsPage extends BaseDeploymentPage {
    private static final Logger logger = Logger.getLogger(DeploymentDestinationSettingsPage.class);

    public static final String WIZARDPAGE_ID = "deploymentDestinationWizardPage";

    private DeploymentDestinationSettingsComposite destinationSettingsComposite = null;

    //   C O N S T R U C T O R S
    public DeploymentDestinationSettingsPage(DeploymentWizard deploymentWizard) {
        super(WIZARDPAGE_ID, deploymentWizard);
    }

    // M E T H O D S

    // Assemble connection page wizard
    @Override
    public void createControl(Composite parent) {
        setWizardCosmetics(parent);
        destinationSettingsComposite = new DeploymentDestinationSettingsComposite(parent, SWT.NULL, this);
        setControl(destinationSettingsComposite);
        setPageComplete(false);
        initialize();

        UIUtils.setHelpContext(destinationSettingsComposite, this.getClass().getSimpleName());
    }

    @Override
    public void performHelp() {
        super.performHelp();
    }

    private void initialize() {
        initEnviornmentDefaults();
        setPageComplete(false);
        if (logger.isDebugEnabled()) {
            setPropertyFileBasedOrgInputs(destinationSettingsComposite, "deployment");
        }
        validateUserInput();
    }

    // set defaults and/or last used
    private void initEnviornmentDefaults() {
        Combo cmbEndpointServers = destinationSettingsComposite.getCmbEndpointServer();
        Combo cmbEnvironment = destinationSettingsComposite.getCmbEnvironment();

        // environment
        String lastEnvironmentSelected =
                ForceIdeDeploymentPlugin.getPreferenceString(DeploymentConstants.LAST_DEPLOYMENT_ENV_SELECTED);
        String selectedEndpointLabel = getSalesforceEndpoints().getDefaultEndpointLabel();
        if (Utils.isNotEmpty(lastEnvironmentSelected)) {
            selectCombo(cmbEnvironment, lastEnvironmentSelected);
        } else if (Utils.isNotEmpty(selectedEndpointLabel)) {
            selectCombo(cmbEnvironment, selectedEndpointLabel);
        } else {
            cmbEnvironment.select(0);
        }

        // server
        String lastServerSelected =
                ForceIdeDeploymentPlugin.getPreferenceString(DeploymentConstants.LAST_DEPLOYMENT_SERVER_SELECTED);
        if (Utils.isNotEmpty(lastServerSelected)) {
            selectCombo(cmbEndpointServers, lastServerSelected);
        } else {
            cmbEndpointServers.select(0);
        }

        // keep endpoint and protocol
        boolean lastKeepEndpointSelected =
                ForceIdeDeploymentPlugin
                .getPreferenceBoolean(DeploymentConstants.LAST_DEPLOYMENT_KEEP_ENDPOINT_SELECTED);
        destinationSettingsComposite.getChkBoxResetEndpoint().setSelection(lastKeepEndpointSelected);

        String username =
                ForceIdeDeploymentPlugin.getPreferenceString(DeploymentConstants.LAST_DEPLOYMENT_USERNAME_SELECTED);
        if (Utils.isNotEmpty(username)) {
            destinationSettingsComposite.setTxtUsername(username);
        }

        // set visibility of advanced server stuff
        destinationSettingsComposite.enableServerEntryControls();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setTitleAndDescription(DeploymentMessages.getString("DeploymentWizard.DestinationPage.title") + " "
                    + getStepString(), DeploymentMessages.getString("DeploymentWizard.DestinationPage.description"));
        }
        super.setVisible(visible);
    }

    // Monitors user input and reports messages.
    public void validateUserInput() {
        setPageComplete(false);

        if (!validateOrganization(destinationSettingsComposite)) {
            return;
        }

        String username = destinationSettingsComposite.getTxtUsername().getText();
        if (getController().isSameAsProjectOrg(username, destinationSettingsComposite.getCmbEndpointServerString())) {
            updateErrorStatus(DeploymentMessages
                .getString("DeploymentWizard.DestinationPage.DestOrgSameProjectOrg.message"));
            return;
        }

        clearMessages();

        setPageComplete(true);
    }

    public void saveUserInput() {
        ForceProject forceProject = new ForceProject();
        forceProject.setProject(deploymentWizard.getProject());

        saveUserInput(forceProject, destinationSettingsComposite);
        saveEndpointInput(forceProject, destinationSettingsComposite, getSalesforceEndpoints());
        getDeploymentWizardModel().setForceProject(forceProject);

        String environment = destinationSettingsComposite.getCmbEnvironmentString();
        getDeploymentWizardModel().setEnvironment(environment);
        getDeploymentWizardModel().setDestinationOrg(forceProject);
    }

    @Override
    public IWizardPage getNextPage() {
        saveUserInput();
        try {
            testConnection(getController(), getDeploymentWizardModel().getDestinationOrg());
            getController().saveOrgSettings(new NullProgressMonitor());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof InsufficientPermissionsException) {
                // log failure; option enabling occurs downstream
                logger
                .warn("Insufficient permissions to create project: "
                        + ForceExceptionUtils.getRootCauseMessage(e));

                InsufficientPermissionsException ex = (InsufficientPermissionsException) e.getTargetException();
                // occurred during project create/update which determines message text
                ex.setShowUpdateCredentialsMessage(true);

                // show dialog
                DialogUtils.getInstance().presentInsufficientPermissionsDialog(ex);

                updateErrorStatus(Messages.getString("InsufficientPermissions.User.OrganizationSettings.message",
                    new String[] { ex.getConnection().getUsername() }));

                return this;
            } else if (cause instanceof InvalidLoginException) {
                // log failure
                logger.warn("Unable to perform sync check: "
                        + ForceExceptionUtils.getRootCauseMessage(e.getTargetException()));

                InvalidLoginException ex = (InvalidLoginException) e.getTargetException();
                // occurred during project create/update which determines message text
                ex.setShowUpdateCredentialsMessage(false);

                // choose further project create direction
                DialogUtils.getInstance().invalidLoginDialog(ex.getMessage(), null, false);

                updateErrorStatus(UIMessages.getString(
                    "ProjectCreateWizard.OrganizationPage.InvalidConnection.WithHost.message",
                    new String[] { getDeploymentWizardModel().getDestinationOrg().getEndpointServer() }));

                return this;
            } else {
                Throwable th = ForceExceptionUtils.getRootCause(e);
                logger.error("Unable to test connection", th);
                updateErrorStatus(DeploymentMessages.getString("DeploymentWizard.PlanPage.Connection.error"));
                Utils.openError(th, true, DeploymentMessages.getString("DeploymentWizard.PlanPage.Connection.error"));
                return this;
            }

        } catch (InterruptedException e) {
            logger.warn("Operation cancelled by user");
        }

        return deploymentWizard.getNextPage(this);
    }

    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }
}
