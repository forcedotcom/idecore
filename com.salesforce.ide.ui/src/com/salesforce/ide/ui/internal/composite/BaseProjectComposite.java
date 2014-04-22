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

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.salesforce.ide.core.remote.SalesforceEndpoints;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.wizards.project.BaseProjectCreatePage;

/**
 * Captures project and organization settings.
 * 
 * @author cwall
 */
public abstract class BaseProjectComposite extends BaseOrganizationComposite {

    protected Text txtProjectName;

    public BaseProjectComposite(Composite parent, int style, DialogPage dialogPage,
            SalesforceEndpoints salesforceEndpoints) {
        super(parent, style, dialogPage, salesforceEndpoints);
    }

    @Override
    protected final void initialize() {
        setLayout(new GridLayout(2, false));

        Label lblRequiredFields = new Label(this, SWT.WRAP);
        lblRequiredFields.setText(UIMessages.getString("ProjectCreateWizard.OrganizationPage.RequiredFields.message"));
        lblRequiredFields.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 0));

        Label filler22 = new Label(this, SWT.NONE);
        filler22.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 0));

        createProjectLabel(this);

        createGrpOrganizationSettings(this);
        createGrpConnectionSettings(this);

        Label filler1 = new Label(this, SWT.NONE);
        filler1.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 0));
    }

    protected final void createProjectLabel(Composite parent) {
        Label lblProjectName = new Label(parent, SWT.NONE);
        lblProjectName.setText(UIMessages.getString("LabelProjectName"));
        lblProjectName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtProjectName = new Text(parent, SWT.BORDER);
        txtProjectName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
        addValidateModifyListener(txtProjectName);
    }

    public Text getTxtProjectName() {
        return txtProjectName;
    }

    public String getTxtProjectNameString() {
        return getText(txtProjectName);
    }

    public void setTxtProjectName(Text txtProjectName) {
        this.txtProjectName = txtProjectName;
    }

    @Override
    protected void handleOrgChange() {
        super.handleOrgChange();
        if (dialogPage != null && dialogPage instanceof BaseProjectCreatePage) {
            BaseProjectCreatePage projectCreatePage = (BaseProjectCreatePage) dialogPage;
            projectCreatePage.getProjectController().setCanComplete(false);
            if (projectCreatePage.getWizard() != null && projectCreatePage.getWizard().getContainer() != null
                    && projectCreatePage.getWizard().getContainer().getCurrentPage() != null) {
                projectCreatePage.getWizard().getContainer().updateButtons();
            }
        }
    }
}
