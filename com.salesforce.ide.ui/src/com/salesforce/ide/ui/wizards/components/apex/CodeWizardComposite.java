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
package com.salesforce.ide.ui.wizards.components.apex;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.wizards.components.ComponentWizardComposite;

public abstract class CodeWizardComposite extends ComponentWizardComposite {

    protected Combo cmbApiVersions = null;

    public CodeWizardComposite(Composite parent, int style, String componentTypeDisplayName) {
        super(parent, style, componentTypeDisplayName);
    }

    protected final void createApiVersionCombo(Group containingGroup, Set<String> supportedApiVesions) {
        Label lblApiVersion = new Label(containingGroup, SWT.NONE);
        lblApiVersion.setText("Version: ");
        lblApiVersion.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        cmbApiVersions = new Combo(containingGroup, SWT.BORDER | SWT.READ_ONLY);
        cmbApiVersions.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 4, 0));
        setApiVersionComboValues(supportedApiVesions);
    }

    protected String getApiVersion() {
        return Utils.isEmpty(getText(cmbApiVersions)) ? Constants.EMPTY_STRING : getText(cmbApiVersions);
    }
    
    protected Combo getApiVersionCombo() {
        return cmbApiVersions;
    }

    protected void setApiVersionComboValues(Set<String> supportedApiVesions) {
        if (Utils.isNotEmpty(supportedApiVesions)) {
            for (String supportedApiVesion : supportedApiVesions) {
                cmbApiVersions.add(supportedApiVesion);
            }
            cmbApiVersions.select(supportedApiVesions.size() - 1);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (null != cmbApiVersions) {
            cmbApiVersions.setEnabled(enabled);
        }
    }
}
