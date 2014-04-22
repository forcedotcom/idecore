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

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.IComponentWizardPage;
import com.salesforce.ide.ui.wizards.components.generic.GenericComponentWizardPage;

public abstract class ApexCodeWizardPage extends GenericComponentWizardPage {

    private static final Logger logger = Logger.getLogger(ApexCodeWizardPage.class);

    protected boolean apexEnabled = false;

    public ApexCodeWizardPage(ComponentWizard baseComponentWizard) {
        super(baseComponentWizard);
    }

    @Override
    protected void additionalInitialize(Composite parent) {
        try {
            apexEnabled = componentWizard.getComponentController().isComponentEnabled();
        } catch (InsufficientPermissionsException e) {
            DialogUtils.getInstance().presentInsufficientPermissionsDialog(e);
        } catch (Exception e) {
            logger.warn(
                "Unable to check " + getComponentWizardModel().getComponent().getDisplayName() + " permissions", e);
            Utils
            .openError("Permission Check Failed", "Unable to check "
                    + getComponentWizardModel().getComponent().getDisplayName() + " permissions:\n\n "
                    + e.getMessage());
        }

        if (!apexEnabled) {
            handleNoApexPerms();
        }
    }

    @Override
    public boolean initialDialogChanged(IComponentWizardPage componentWizardPage) {
        if (!apexEnabled) {
            handleNoApexPerms();
            pageComplete = false;
            return false;
        }
        return true;
    }

    protected void handleNoApexPerms() {
        updateErrorStatus(UIMessages.getString("ApexCodeWizard.ApexEnabled.error", new String[] { "Apex Code" }));
        // disable generic controls
        componentWizardComposite.disableAllControls();
        // disable common apex controls
        ((ApexCodeWizardComposite) componentWizardComposite).disableComponentApiField();
        // disable child controls
        disableAllApexControls();
    }

    protected abstract void disableAllApexControls();
}
