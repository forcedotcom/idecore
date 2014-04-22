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
package com.salesforce.ide.ui.internal.wizards;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.SalesforceEndpoints;
import com.salesforce.ide.ui.internal.composite.BaseOrganizationComposite;
import com.salesforce.ide.ui.internal.composite.BaseProjectComposite;
import com.salesforce.ide.ui.internal.utils.UIConstants;
import com.salesforce.ide.ui.internal.utils.UIMessages;

public abstract class BaseOrgWizardPage extends BaseWizardPage {
    private static final Logger logger = Logger.getLogger(BaseOrgWizardPage.class);

    protected static final String OTHER_LABEL_NAME =
            UIMessages.getString("ProjectCreateWizard.OrganizationPage.OtherEnvironment.label");

    //   C O N S T R U C T O R S
    public BaseOrgWizardPage(String wizardName) {
        super(wizardName);
    }

    protected boolean requireUserNameAndPassword(BaseOrganizationComposite organizationComposite) {
        if (Utils.isEmpty(organizationComposite.getTxtUsernameString())) {
            updateInfoStatus(UIMessages.getString(UIConstants.MSG_USERNAME_EMPTY));
            return false;
        }

        if (Utils.isEmpty(organizationComposite.getTxtPasswordString())) {
            updateInfoStatus(UIMessages.getString(UIConstants.MSG_PASSWORD_EMPTY));
            return false;
        }
        return true;
    }

    //   M E T H O D S
    // validates org settings
    protected boolean validateOrganization(BaseOrganizationComposite organizationComposite) {
    	if (Utils.isInternalMode()) {
    		// Check to see if there's a session id, if so, we don't require a username/password combo
    		if (Utils.isEmpty(organizationComposite.getTxtSessionIdString())) {
        		if (!requireUserNameAndPassword(organizationComposite)) return false;
    		} else {
    			// Must be in the "other" environment if using session id
    			if (!isOtherEnvironment(organizationComposite)) {
    	            updateInfoStatus("You must specify the hostname for 'other environment' if using a sessionId");
    	            return false;
    			}
    		}
    	} else {
    		if (!requireUserNameAndPassword(organizationComposite)) return false;
    	}

        // test for empty, complete endpoint url, and supported version
        String serverName = organizationComposite.getCmbEndpointServerString();
        if (isOtherEnvironment(organizationComposite) && Utils.isEmpty(serverName)) {
            updateErrorStatus(UIMessages.getString("OrganizationSettings.HostnameNotSpecified.message"));
            organizationComposite.getChkBoxResetEndpoint().setEnabled(false);
            return false;
        } else if (serverName.startsWith(Constants.HTTP + ":") || serverName.startsWith(Constants.HTTPS + ":")) {
            updateErrorStatus(UIMessages.getString("OrganizationSettings.HostnameNotValid.ContainsProtocol.message"));
            organizationComposite.getChkBoxResetEndpoint().setEnabled(false);
            return false;
        } else if (serverName.contains("/services/")) {
            updateErrorStatus(UIMessages.getString("OrganizationSettings.HostnameOnlyNotFullEndpoint.message"));
            organizationComposite.getChkBoxResetEndpoint().setEnabled(false);
            return false;
        } else if (Utils.containsInvalidHostPortChars(serverName)) {
            updateErrorStatus(UIMessages.getString("OrganizationSettings.HostnameNotValid.message",
                new String[] { serverName }));
            organizationComposite.getChkBoxResetEndpoint().setEnabled(false);
            return false;
        } else {
            organizationComposite.getChkBoxResetEndpoint().setEnabled(true);
        }

        updateInfoStatus(null);

        return true;
    }

    // saving force project user input
    protected void saveUserInput(ForceProject forceProject, BaseOrganizationComposite organizationComposite) {
        forceProject.setUserName(organizationComposite.getTxtUsernameString());
        forceProject.setPassword(organizationComposite.getTxtPasswordString());
        forceProject.setToken(organizationComposite.getTxtTokenString());
        forceProject.setSessionId(organizationComposite.getTxtSessionIdString());
    }

    protected void saveEndpointInput(ForceProject forceProject, BaseOrganizationComposite organizationComposite,
            SalesforceEndpoints salesforceEndpoints) {

        // connection setting stuff - not all project pages have connection input
        if (organizationComposite.getSpnReadTimeout() != null) {
            forceProject.setReadTimeoutSecs(organizationComposite.getSpnReadTimeout().getSelection());
        }

        // handle the endpoint stuff
        String environment = organizationComposite.getCmbEnvironmentString();

        String endpointServer = null;
        if (isOtherEnvironment(organizationComposite)) {
            endpointServer = organizationComposite.getCmbEndpointServerString();
            forceProject.setKeepEndpoint(organizationComposite.getChkBoxResetEndpoint().getSelection());
            // protocol is not initialized if not in sfdc mode
            if (organizationComposite.getChkBoxProtocol() != null) {
                forceProject.setHttpsProtocol(organizationComposite.getChkBoxProtocol().getSelection());
            }
        } else {
            endpointServer = salesforceEndpoints.getEndpointServerForLabel(environment);
        }

        forceProject.setEndpointServer(endpointServer);
        forceProject.setEndpointEnvironment(environment);
        forceProject.setMetadataFormatVersion(salesforceEndpoints.getDefaultApiVersion());
        forceProject.setEndpointApiVersion(salesforceEndpoints.getDefaultApiVersion());
    }

    protected boolean isOtherEnvironment(BaseOrganizationComposite organizationComposite) {
        String environment = organizationComposite.getCmbEnvironmentString();
        return OTHER_LABEL_NAME.equals(environment);
    }

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

    protected void setPropertyFileBasedOrgInputs(BaseOrganizationComposite organizationComposite, String prefix) {
        Properties props = Utils.getDefaultProperties();
        if (props == null || props.isEmpty()) {
            return;
        }

        if (Utils.isNotEmpty(props.getProperty(prefix + ".password"))) {
            organizationComposite.getTxtPassword().setText(props.getProperty(prefix + ".password"));
        }

        if (Utils.isNotEmpty(props.getProperty(prefix + ".username"))) {
            organizationComposite.getTxtUsername().setText(props.getProperty(prefix + ".username"));
        }

        if (Utils.isNotEmpty(props.getProperty(prefix + ".environment"))) {
            selectCombo(organizationComposite.getCmbEnvironment(), props.getProperty(prefix + ".environment"));
        }

        if (Utils.isNotEmpty(props.getProperty(prefix + ".endpoint.server"))) {
            selectCombo(organizationComposite.getCmbEndpointServer(), props.getProperty(prefix + ".endpoint.server"));
        }

        String keepEndpoint = props.getProperty(prefix + ".keep.endpoint");
        if (Utils.isNotEmpty(keepEndpoint)) {
            organizationComposite.getChkBoxResetEndpoint().setSelection(Boolean.parseBoolean(keepEndpoint));
        }

        String useHttps = props.getProperty(prefix + ".use.https");
        if (Utils.isNotEmpty(useHttps)) {
            organizationComposite.getChkBoxProtocol().setSelection(Boolean.parseBoolean(useHttps));
        }

        organizationComposite.enableServerEntryControls();

        if (logger.isDebugEnabled()) {
            logger.debug("Set default org properties from prop file");
        }

        setPageComplete(true);
    }

    // U T I L I T I E S
    @Override
    protected String getText(Text txt) {
        return txt != null ? txt.getText() : null;
    }

    @Override
    protected IPath getPath(Text txtDir) {
        String text = getText(txtDir);
        if (Utils.isEmpty(text)) {
            return null;
        }
        return new Path(text);
    }

    @Override
    protected boolean isValidDirectory(Text txtDir) {
        IPath path = getPath(txtDir);
        return path == null || !path.toFile().isDirectory() || !path.toFile().exists();
    }

    @Override
    protected void selectCombo(Combo combo, String text) {
        if (combo == null || Utils.isEmpty(text)) {
            return;
        }

        String[] options = combo.getItems();
        if (Utils.isNotEmpty(options)) {
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(text)) {
                    combo.select(i);
                    return;
                }
            }
        }

        combo.add(text, 0);
        combo.select(0);
    }
}
