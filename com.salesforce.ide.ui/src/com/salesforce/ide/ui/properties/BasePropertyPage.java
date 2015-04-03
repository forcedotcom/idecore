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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.SalesforceEndpoints;
import com.salesforce.ide.core.services.LoggingService;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.ui.internal.composite.BaseProjectComposite;
import com.salesforce.ide.ui.internal.utils.UIConstants;
import com.salesforce.ide.ui.internal.utils.UIMessages;

public abstract class BasePropertyPage extends PropertyPage {

    public BasePropertyPage() {
        super();
    }

    public ProjectService getProjectService() {
        return ContainerDelegate.getInstance().getServiceLocator().getProjectService();
    }

    public LoggingService getLoggingService() {
        return ContainerDelegate.getInstance().getServiceLocator().getLoggingService();
    }

    public ComponentFactory getComponentFactory() {
        return ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory();
    }

    public SalesforceEndpoints getSalesforceEndpoints() {
        return ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getSalesforceEndpoints();
    }

    protected void updateInfoStatus(String message) {
        setMessage(message, IMessageProvider.INFORMATION);
    }

    protected void updateErrorStatus(String message) {
        setMessage(message, IMessageProvider.ERROR);
    }

    // validates project and org settings
    protected boolean validateProjectSettings(BaseProjectComposite baseProjectComposite) {
        return (validateProject(baseProjectComposite) && validateOrganization(baseProjectComposite));
    }

    // validates project settings
    protected boolean validateProject(BaseProjectComposite baseProjectComposite) {
        String projectName = baseProjectComposite.getTxtProjectNameString();
        if (baseProjectComposite.getTxtProjectName().getEnabled() && Utils.isEmpty(projectName)) {
            updateInfoStatus(UIMessages.getString(UIConstants.MSG_PROJECT_NAME_EMPTY));
            return false;
        }

        if (Utils.containsInvalidChars(projectName)) {
            updateErrorStatus(UIMessages.getString("ProjectCreateWizard.OrganizationPage.InvalidChar.message"));
            return false;
        }

        IResource container = null;
        if (baseProjectComposite.getTxtProjectName().getEnabled() && Utils.isNotEmpty(projectName)) {
            container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(projectName));
            if (container != null) {
                updateErrorStatus(UIMessages.getString(UIConstants.MSG_PROJECT_NAME_UNIQUE));
                return false;
            }
        }

        updateInfoStatus(null);

        return true;
    }


    //   M E T H O D S
    // validates org settings

    protected boolean requireUserNameAndPassword(BaseProjectComposite organizationComposite) {
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

    protected boolean validateOrganization(BaseProjectComposite baseProjectComposite) {
    	if (Utils.isInternalMode()) {
    		// Check to see if there's a session id, if so, we don't require a username/password combo
    		if (Utils.isEmpty(baseProjectComposite.getTxtSessionIdString())) {
        		if (!requireUserNameAndPassword(baseProjectComposite)) return false;
    		} else {
    	        String otherLabel = UIMessages.getString("ProjectCreateWizard.OrganizationPage.OtherEnvironment.label");
    	        String environment = baseProjectComposite.getCmbEnvironmentString();
    	        if (!otherLabel.equals(environment)) {
    	            updateInfoStatus("You must specify the hostname for 'other environment' if using a sessionId");
    	        }
    		}
    	} else {
    		if (!requireUserNameAndPassword(baseProjectComposite)) return false;
    	}

        // test for empty, complete endpoint url, and supported version
        String serverName = baseProjectComposite.getCmbEndpointServerString();
        if (Utils.isEmpty(serverName)) {
            updateInfoStatus(UIMessages.getString("OrganizationSettings.HostnameNotSpecified.message"));
            baseProjectComposite.getChkBoxResetEndpoint().setEnabled(false);
            return false;
        } else if (serverName.startsWith(Constants.HTTP + ":") || serverName.startsWith(Constants.HTTPS + ":")) {
            updateInfoStatus(UIMessages.getString("OrganizationSettings.HostnameNotValid.message"));
            baseProjectComposite.getChkBoxResetEndpoint().setEnabled(false);
            return false;
        } /*else if (!Utils.validateDomainName(serverName)) {
                                 updateInfoStatus(Messages.getString("OrganizationSettings.HostnameNotValid.message"));
                                 baseProjectComposite.getChkBoxResetEndpoint().setEnabled(false);
                                 return false;
                             }*/else {
            baseProjectComposite.getChkBoxResetEndpoint().setEnabled(true);
        }

        updateInfoStatus(null);

        return true;
    }

    protected boolean validateTimeout(int timeout) {
        return timeout < Constants.READ_TIMEOUT_IN_SECONDS_MIN || timeout > Constants.READ_TIMEOUT_IN_SECONDS_MAX;
    }

    public void clearMessages() {
        updateInfoStatus(null);
    }

    protected void enableApplyButton(boolean enable) {
        if (getApplyButton() != null) {
            getApplyButton().setEnabled(enable);
        }
    }

    protected void enableButtons(boolean enable) {
        if (getApplyButton() != null) {
            getApplyButton().setEnabled(enable);
        }

        if (getDefaultsButton() != null) {
            getDefaultsButton().setEnabled(enable);
        }
    }

    // saving force project user input
    protected void saveProjectUserInput(ForceProject forceProject, BaseProjectComposite baseProjectComposite,
            SalesforceEndpoints salesforceEndpoints) {
        forceProject.setUserName(baseProjectComposite.getTxtUsernameString());
        forceProject.setPassword(baseProjectComposite.getTxtPasswordString());
        forceProject.setToken(baseProjectComposite.getTxtTokenString());
        forceProject.setSessionId(baseProjectComposite.getTxtSessionIdString());

        // connection setting stuff - not all project pages have connection input
        if (baseProjectComposite.getSpnReadTimeout() != null) {
            forceProject.setReadTimeoutSecs(baseProjectComposite.getSpnReadTimeout().getSelection());
        }

        // handle the endpoint stuff
        String otherLabel = UIMessages.getString("ProjectCreateWizard.OrganizationPage.OtherEnvironment.label");
        String environment = baseProjectComposite.getCmbEnvironmentString();

        String endpointServer = null;
        if (otherLabel.equals(environment)) {
            endpointServer = baseProjectComposite.getCmbEndpointServerString();
            forceProject.setKeepEndpoint(baseProjectComposite.getChkBoxResetEndpoint().getSelection());
            // protocol is not initialized if not in sfdc mode
            if (baseProjectComposite.getChkBoxProtocol() != null) {
                forceProject.setHttpsProtocol(baseProjectComposite.getChkBoxProtocol().getSelection());
            }
        } else {
            endpointServer = salesforceEndpoints.getEndpointServerForLabel(environment);
        }

        forceProject.setEndpointServer(endpointServer);
        forceProject.setEndpointEnvironment(environment);
        forceProject.setMetadataFormatVersion(salesforceEndpoints.getDefaultApiVersion());
        forceProject.setEndpointApiVersion(salesforceEndpoints.getDefaultApiVersion());
    }

    // U T I L I T I E S
    protected String getText(Combo cbo) {
        return cbo != null ? cbo.getText() : null;
    }

    protected String getText(Text txt) {
        return txt != null ? txt.getText().trim() : null;
    }

    protected boolean isEmpty(String str) {
        return Utils.isEmpty(str);
    }

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
        } else {
            combo.add(text);
            combo.select(0);
        }
    }

}
