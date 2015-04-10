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
package com.salesforce.ide.ui.internal.composite;

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.salesforce.ide.core.internal.preferences.PreferenceManager;
import com.salesforce.ide.core.internal.preferences.proxy.ProxyManager;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.SalesforceEndpoints;
import com.salesforce.ide.ui.internal.utils.*;
import com.salesforce.ide.ui.properties.ProjectPropertyComposite;

/**
 * Captures project and organization settings.
 *
 * @author cwall
 */
public abstract class BaseOrganizationComposite extends BaseComposite {

    private static final Logger logger = Logger.getLogger(BaseOrganizationComposite.class);

    protected static final String OTHER_LABEL_NAME =
            UIMessages.getString("ProjectCreateWizard.OrganizationPage.OtherEnvironment.label");

    protected Text txtUsername;
    protected Text txtPassword;
    protected Text txtToken;
    protected Text txtSessionId;
    protected Combo cmbEndpointServer;
    protected Button chkBoxResetEndpoint;
    protected Button chkBoxProtocol;
    protected Spinner spnReadTimeout;
    protected Group grpOrg;
    protected Group grpProxy;
    protected DialogPage dialogPage;
    protected Group grpConnectionSettings;
    protected Combo cmbEnvironment;
    protected Label lblHostname;
    protected Label lblAdvEnvFiller2;
    protected Label lblAdvEnvFiller3;
    protected GridData dataHostname;
    protected GridData dataEndpointServer;
    protected GridData dataBoxResetEndpoint;
    protected GridData dataBoxProtocol;
    protected GridData dataAdvEnvFiller2;
    protected GridData dataAdvEnvFiller3;
    protected GridData datafiller11;
    protected Label filler11;
    protected SalesforceEndpoints salesforceEndpoints;
    protected boolean orgModified;

    public BaseOrganizationComposite(Composite parent, int style, DialogPage dialogPage,
            SalesforceEndpoints salesforceEndpoints) {
        super(parent, style);
        this.dialogPage = dialogPage;
        this.salesforceEndpoints = salesforceEndpoints;
        initialize();
        pack();
    }

    protected void initialize() {
        setLayout(new GridLayout(2, false));

        Label lblRequiredFields = new Label(this, SWT.WRAP);
        lblRequiredFields.setText(UIMessages.getString("ProjectCreateWizard.OrganizationPage.RequiredFields.message"));
        lblRequiredFields.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 0));

        Label filler22 = new Label(this, SWT.NONE);
        filler22.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 0));

        createGrpOrganizationSettings(this);

        Label filler1 = new Label(this, SWT.NONE);
        filler1.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 0));
    }

    /**
     * Defines Force.com web service connection settings group.
     *
     * @param container
     */
    protected final void createGrpOrganizationSettings(Composite parent) {

        // determine if the request is from the properties page.
        boolean projectCreateWizardFlag = true;
        if (parent instanceof ProjectPropertyComposite) {
            projectCreateWizardFlag = false;
        }

        grpOrg = new Group(parent, SWT.NONE);
        grpOrg.setText("Organization Settings");
        grpOrg.setLayout(new GridLayout(3, false));
        grpOrg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 0));

        // username input
        Label lblUsername = new Label(grpOrg, SWT.NONE);
        lblUsername.setText(UIMessages.getString(UIConstants.LABEL_USERNAME));
        lblUsername.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtUsername = new Text(grpOrg, SWT.BORDER);
        txtUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
        addValidateModifyListener(txtUsername);
        addOrgModifyListener(txtUsername);

        int horizSpacing = Utils.isInternalMode() ? 4 : 3;
        if (projectCreateWizardFlag) {
            // Signup Text
            Label lblSignup = new Label(grpOrg, SWT.WRAP);
            lblSignup.setText(UIMessages.getString(UIConstants.LABEL_SIGNUP));
            lblSignup.setAlignment(SWT.LEFT);
            GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, horizSpacing);
            gd.widthHint = UIUtils.convertHeightInCharsToPixels(parent, 11);
            lblSignup.setLayoutData(gd);
        } else {
            // do not display the singup text if its a property page.
            Label lblFiller = new Label(grpOrg, SWT.NONE);
            lblFiller.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, horizSpacing));
        }

        // password input
        Label lblPassword = new Label(grpOrg, SWT.NONE);
        lblPassword.setText(UIMessages.getString(UIConstants.LABEL_PASSWORD));
        lblPassword.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtPassword = new Text(grpOrg, SWT.BORDER | SWT.PASSWORD);
        txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
        addValidateModifyListener(txtPassword);
        addOrgModifyListener(txtPassword);

        // token input
        Label lblToken = new Label(grpOrg, SWT.NONE);
        lblToken.setText(UIMessages.getString(UIConstants.LABEL_TOKEN));
        lblToken.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtToken = new Text(grpOrg, SWT.BORDER | SWT.PASSWORD);
        txtToken.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
        txtToken.setSize(30, 1);
        addValidateModifyListener(txtToken);
        addOrgModifyListener(txtToken);

        // SessionId, if using internal mode
        if (Utils.isInternalMode()) {
	        Label lblSessionId = new Label(grpOrg, SWT.NONE);
	        lblSessionId.setText(UIMessages.getString(UIConstants.LABEL_SESSIONID));
	        lblSessionId.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
	        txtSessionId = new Text(grpOrg, SWT.BORDER | SWT.PASSWORD);
	        txtSessionId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
	        txtSessionId.setSize(30, 1);
	        addValidateModifyListener(txtSessionId);
	        addOrgModifyListener(txtSessionId);
        }


        createEnvironmentControls(grpOrg, projectCreateWizardFlag);
    }

    private static void createSignupLink(final Group grpOrg) {
        Link lnkProxySettings = new Link(grpOrg, SWT.NONE);
        lnkProxySettings.setText(UIMessages.getString(UIConstants.LABEL_SIGNUP_LINK));
        lnkProxySettings.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 1, 0));
        final String urlStr = UIConstants.NEW_ORG_CREATE_LINK;
        lnkProxySettings.setData(urlStr);
        lnkProxySettings.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                try {
                    URL url = new URL((String) e.widget.getData());

                    if (logger.isDebugEnabled()) {
                        logger.debug("Opening browser to '" + url.toString() + "'");
                    }

                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
                } catch (Exception ex) {
                    logger.error("Unable to open default browser to " + urlStr, ex);
                }
            }
        });
    }

    private void createEnvironmentControls(final Group grpOrg, boolean projectCreateWizard) {
        Label lblEnvironment = new Label(grpOrg, SWT.NONE);
        lblEnvironment.setText(UIMessages.getString("LabelEnvironment"));
        lblEnvironment.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        cmbEnvironment = new Combo(grpOrg, SWT.BORDER | SWT.READ_ONLY);
        cmbEnvironment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
        initEnvironments();

        if (projectCreateWizard) {
            createSignupLink(grpOrg);
        } else {
            // do not display the singup link if its a property page.
            Label lblFiller = new Label(grpOrg, SWT.NONE);
            lblFiller.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 1, 0));
        }

        cmbEnvironment.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                enableServerEntryControls();
                validateUserInput();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
        addOrgModifyListener(cmbEnvironment);

        createServerEntryControls(grpOrg);
    }

    public void enableServerEntryControls() {
        boolean visible = getCmbEnvironmentString().equals(OTHER_LABEL_NAME);
        dataHostname.exclude = !visible;
        lblHostname.setVisible(visible);

        dataEndpointServer.exclude = !visible;
        cmbEndpointServer.setVisible(visible);

        dataAdvEnvFiller2.exclude = !visible;
        lblAdvEnvFiller2.setVisible(visible);

        dataBoxResetEndpoint.exclude = !visible;
        chkBoxResetEndpoint.setVisible(visible);

        if (Utils.isInternalMode()) {
            if (dataBoxProtocol != null) {
                dataBoxProtocol.exclude = !visible;
            }
            if (chkBoxProtocol != null) {
                chkBoxProtocol.setVisible(visible);
            }
        }

        grpOrg.getParent().layout(true);
    }

    private void createServerEntryControls(final Group grpOrg) {
        lblHostname = new Label(grpOrg, SWT.NONE);
        lblHostname.setText(UIMessages.getString("LabelHostname"));
        dataHostname = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0);
        dataHostname.exclude = true;
        lblHostname.setLayoutData(dataHostname);
        lblHostname.setVisible(false);

        cmbEndpointServer = new Combo(grpOrg, SWT.BORDER);
        dataEndpointServer = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0);
        dataEndpointServer.exclude = true;
        cmbEndpointServer.setLayoutData(dataEndpointServer);
        cmbEndpointServer.setVisible(false);
        initEndpointServers();
        addValidateModifyListener(cmbEndpointServer);
        addOrgModifyListener(cmbEndpointServer);

        @SuppressWarnings("unused")
        Label filler31 = new Label(grpOrg, SWT.NONE);

        lblAdvEnvFiller2 = new Label(grpOrg, SWT.NONE);
        dataAdvEnvFiller2 = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0);
        dataAdvEnvFiller2.exclude = true;
        lblAdvEnvFiller2.setLayoutData(dataAdvEnvFiller2);
        lblAdvEnvFiller2.setVisible(false);

        chkBoxResetEndpoint = new Button(grpOrg, SWT.CHECK);
        chkBoxResetEndpoint.setText(UIMessages.getString(UIConstants.LABEL_RESET));
        chkBoxResetEndpoint.setSelection(false);
        chkBoxResetEndpoint.setEnabled(false);
        chkBoxResetEndpoint.setVisible(false);
        addOrgSelectionListener(chkBoxResetEndpoint);
        addValidateSelectionListener(chkBoxResetEndpoint);

        if (Utils.isInternalMode()) {
            chkBoxProtocol = new Button(grpOrg, SWT.CHECK);
            chkBoxProtocol.setText("Use HTTPS");
            dataBoxProtocol = new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 1, 0);
            dataBoxProtocol.exclude = true;
            chkBoxProtocol.setLayoutData(dataBoxProtocol);
            chkBoxProtocol.setSelection(true);
            chkBoxProtocol.setEnabled(true);
            chkBoxProtocol.setVisible(false);
            addOrgSelectionListener(chkBoxProtocol);
            addValidateSelectionListener(chkBoxProtocol);

            dataBoxResetEndpoint = new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 1, 0);
            dataBoxResetEndpoint.exclude = true;
            chkBoxResetEndpoint.setLayoutData(dataBoxResetEndpoint);
        } else {
            dataBoxResetEndpoint = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0);
            dataBoxResetEndpoint.exclude = true;
            chkBoxResetEndpoint.setLayoutData(dataBoxResetEndpoint);
        }
    }

    protected void createGrpConnectionSettings(Composite parent) {
        grpConnectionSettings = new Group(parent, SWT.NONE);
        grpConnectionSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 0));
        grpConnectionSettings.setLayout(new GridLayout(5, false));
        grpConnectionSettings.setText("Connection Settings");

        Label lblReadTimeout = new Label(grpConnectionSettings, SWT.NONE);
        lblReadTimeout.setText("Timeout (sec):");
        lblReadTimeout.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        spnReadTimeout = new Spinner(grpConnectionSettings, SWT.NONE);
        spnReadTimeout.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        spnReadTimeout.setValues(Constants.READ_TIMEOUT_IN_SECONDS_DEFAULT, Constants.READ_TIMEOUT_IN_SECONDS_MIN,
            Constants.READ_TIMEOUT_IN_SECONDS_MAX, 0, 30, 30);
        Label lblTimeoutMax = new Label(grpConnectionSettings, SWT.NONE);
        lblTimeoutMax.setText("(max 600)");
        lblTimeoutMax.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 3, 0));

        Link lnkProxySettings = new Link(grpConnectionSettings, SWT.NONE);
        lnkProxySettings.setText(UIMessages.getString(UIConstants.PROXY_LABEL));
        lnkProxySettings.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 5, 0));
        lnkProxySettings.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                ProxyManager proxyManager = PreferenceManager.getInstance().getProxyManager();
                PreferenceDialog dialog = null;
                if (proxyManager != null && proxyManager.isCoreNetAvailable()) {
                    dialog =
                            PreferencesUtil.createPreferenceDialogOn(getShell(), Constants.PROXY_PREFERENCE_3_2_X,
                                null, null);
                } else {
                    logger.warn("'" + Constants.PROXY_PREFERENCE_3_2_X + "' preference page not found, opening '"
                            + Constants.PROXY_PREFERENCE_3_3_X + "'");
                    dialog =
                            PreferencesUtil.createPreferenceDialogOn(getShell(), Constants.PROXY_PREFERENCE_3_3_X,
                                null, null);
                }

                if (dialog != null) {
                    dialog.open();
                } else {
                    Utils.openWarn(getShell(), "Proxy Settings Not Found",
                        "Unable to open proxy preference - preference page not found.");
                }
            }
        });
    }

    private void initEnvironments() {
        cmbEnvironment.removeAll();
        Set<String> endpointLabels = salesforceEndpoints.getDefaultEndpointLabels();

        if (Utils.isEmpty(endpointLabels)) {
            return;
        }

        for (String endpointLabel : endpointLabels) {
            cmbEnvironment.add(endpointLabel);
        }

        cmbEnvironment.add(OTHER_LABEL_NAME);
        cmbEnvironment.select(0);
    }

    private void initEndpointServers() {
        cmbEndpointServer.removeAll();
        TreeSet<String> endpointServers = salesforceEndpoints.getAllEndpointServers();

        if (Utils.isEmpty(endpointServers)) {
            return;
        }

        for (String endpointServer : endpointServers) {
            cmbEndpointServer.add(endpointServer);
        }
        cmbEndpointServer.select(0);
    }

    protected void addOrgModifyListener(Control control) {
        control.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                handleOrgChange();
            }
        });
    }

    protected void addOrgModifyListener(Combo combo) {
        combo.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                handleOrgChange();
            }
        });
    }

    protected void addOrgSelectionListener(Button btn) {
        btn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                handleOrgChange();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
    }

    protected void handleOrgChange() {
        setOrgModified(true);
    }

    public Button getChkBoxResetEndpoint() {
        return chkBoxResetEndpoint;
    }

    public void disableChkBoxResetEndpoint() {
        if (chkBoxResetEndpoint != null) {
            chkBoxResetEndpoint.setEnabled(false);
        }
    }

    public void setChkBoxResetEndpoint(Button chkBoxResetEndpoint) {
        this.chkBoxResetEndpoint = chkBoxResetEndpoint;
    }

    public Button getChkBoxProtocol() {
        return chkBoxProtocol;
    }

    public void setChkBoxProtocol(Button chkBoxProtocol) {
        this.chkBoxProtocol = chkBoxProtocol;
    }

    public Spinner getSpnReadTimeout() {
        return spnReadTimeout;
    }

    public void setSpnReadTimeout(Spinner spnReadTimeout) {
        this.spnReadTimeout = spnReadTimeout;
    }

    public Text getTxtPassword() {
        return txtPassword;
    }

    public String getTxtPasswordString() {
        return getText(txtPassword);
    }

    public void setTxtPassword(Text txtPassword) {
        this.txtPassword = txtPassword;
    }

    public Text getTxtToken() {
        return txtToken;
    }

    public String getTxtTokenString() {
        return getText(txtToken);
    }

    public void setTxtToken(Text txtToken) {
        this.txtToken = txtToken;
    }

    public Text getTxtSessionId() {
        return txtSessionId;
    }

    public String getTxtSessionIdString() {
        return getText(txtSessionId);
    }

    public void setTxtSessionId(Text txtSessionId) {
        this.txtSessionId = txtSessionId;
    }


    public String getCmbEndpointServerString() {
        if (isEmpty(getText(cmbEndpointServer))) {
            return Constants.EMPTY_STRING;
        }

        return getText(cmbEndpointServer);
    }

    public String getCmbEnvironmentString() {
        if (isEmpty(getText(cmbEnvironment))) {
            return Constants.EMPTY_STRING;
        }

        return getText(cmbEnvironment);
    }

    public Combo getCmbEndpointServer() {
        return cmbEndpointServer;
    }

    public void selectEndpointLabel(String endpointServer) {
        cmbEndpointServer.setText(endpointServer);
        cmbEndpointServer.redraw();
    }

    public Combo getCmbEnvironment() {
        return cmbEnvironment;
    }

    public void setCmbEnvironment(Combo cmbEnvironment) {
        this.cmbEnvironment = cmbEnvironment;
    }

    public Text getTxtUsername() {
        return txtUsername;
    }

    public String getTxtUsernameString() {
        return getText(txtUsername);
    }

    public void setTxtUsername(Text txtUsername) {
        this.txtUsername = txtUsername;
    }

    public void setTxtUsername(String username) {
        if (txtUsername != null) {
            txtUsername.setText(username);
        }
    }

    public boolean isOrgModified() {
        return orgModified;
    }

    public void setOrgModified(boolean orgModified) {
        this.orgModified = orgModified;
    }
}
