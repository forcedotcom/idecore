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

import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.ui.internal.composite.BaseComposite;
import com.salesforce.ide.upgrade.internal.UpgradeController;
import com.salesforce.ide.upgrade.internal.UpgradeModel;

public abstract class BaseUpgradeComposite extends BaseComposite {

    protected UpgradeController upgradeController = null;
    protected UpgradeModel upgradeModel = null;

    public BaseUpgradeComposite(Composite parent, int style, UpgradeController upgradeController) {
        super(parent, style);
        this.upgradeController = upgradeController;
        this.upgradeModel = upgradeController.getUpgradeModel();
    }
}
