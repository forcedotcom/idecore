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

import com.salesforce.ide.ui.internal.wizards.BaseWizardPage;
import com.salesforce.ide.upgrade.internal.UpgradeController;

/**
 *
 * 
 * @author cwall
 */
public abstract class BaseUpgradePage extends BaseWizardPage {

    protected UpgradeWizard upgradeWizard = null;
    protected UpgradeController upgradeController = null;

    //   C O N S T R U C T O R S
    public BaseUpgradePage(String wizardId, UpgradeWizard upgradeWizard) {
        super(wizardId);
        this.upgradeWizard = upgradeWizard;
        this.upgradeController = upgradeWizard.getUpgradeController();
    }
}
