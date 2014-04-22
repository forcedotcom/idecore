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

import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.ui.internal.composite.BaseOrganizationComposite;

public class DeploymentDestinationSettingsComposite extends BaseOrganizationComposite {

    public DeploymentDestinationSettingsComposite(Composite parent, int style,
            DeploymentDestinationSettingsPage destinationSettingsPage) {
        super(parent, style, destinationSettingsPage, ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getSalesforceEndpoints());
    }

    // Monitors user input and reports messages.
    @Override
    public void validateUserInput() {
        ((DeploymentDestinationSettingsPage) dialogPage).validateUserInput();
    }

    @Override
    protected void handleOrgChange() {
        super.handleOrgChange();
        if (dialogPage != null && dialogPage instanceof DeploymentDestinationSettingsPage) {
            DeploymentDestinationSettingsPage destinationPage = (DeploymentDestinationSettingsPage) dialogPage;
            destinationPage.getController().setCanComplete(false);
            if (destinationPage.getWizard() != null && destinationPage.getWizard().getContainer() != null
                    && destinationPage.getWizard().getContainer().getCurrentPage() != null) {
                destinationPage.getWizard().getContainer().updateButtons();
            }
        }
    }
}
