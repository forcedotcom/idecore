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
package com.salesforce.ide.upgrade.ui.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.salesforce.ide.upgrade.internal.UpgradeController;
import com.salesforce.ide.upgrade.internal.utils.UpgradeMessages;

public class UpgradeIntroComposite extends BaseUpgradeComposite {

    public UpgradeIntroComposite(Composite parent, int style, UpgradeController upgradeController) {
        super(parent, style, upgradeController);
        initialize();
    }

    protected void initialize() {
        setLayout(new GridLayout(2, false));

        Label lblIntro = new Label(this, SWT.WRAP);
        lblIntro.setText(UpgradeMessages.getString("UpgradeWizard.IntroPage.Content.message", new String[] {
                upgradeModel.getProjectName(), upgradeModel.getIdeBrandName(), upgradeModel.getIdeReleaseName(),
                upgradeModel.getPlatformName() }));
        lblIntro.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 0));
    }

    @Override
    public void validateUserInput() {

    }
}
