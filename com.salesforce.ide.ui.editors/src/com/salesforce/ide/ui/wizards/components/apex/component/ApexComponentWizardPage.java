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
package com.salesforce.ide.ui.wizards.components.apex.component;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.components.apex.component.ApexComponentModel;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.apex.CodeWizardPage;

public class ApexComponentWizardPage extends CodeWizardPage {

    private static final Logger logger = Logger.getLogger(ApexComponentWizardPage.class);

    public ApexComponentWizardPage(ComponentWizard componentWizard) {
        super(componentWizard);
    }

    @Override
    public String getComponentName() {
        return componentWizardComposite.getComponentName();
    }

    @Override
    public void createComposite(Composite parent) {
        Component component = componentWizard.getComponentController().getComponent();
        componentWizardComposite =
                new ApexComponentWizardComposite(parent, SWT.NULL, component.getDisplayName(), component
                        .getSupportedApiVersions());
        componentWizardComposite.setComponentWizardPage(this);
    }

    public ApexComponentWizardComposite getApexComponentWizardComposite() {
        return (ApexComponentWizardComposite) componentWizardComposite;
    }

    public ApexComponentWizard getApexComponentWizard() {
        return (ApexComponentWizard) componentWizard;
    }

    @Override
    protected void handleNoApexPerms() {
        super.handleNoApexPerms();
        Component component = componentWizard.getComponentController().getComponent();
        updateErrorStatus(UIMessages.getString("ApexCodeWizard.ApexEnabled.error", new String[] { component
                .getDisplayName() }));
    }

    @Override
    public void saveUserInput() throws InstantiationException, IllegalAccessException {
        if (componentWizardComposite == null) {
            throw new IllegalArgumentException("Component composite cannot be null");
        }

        ApexComponentWizardComposite apexComponentWizardComposite =
                (ApexComponentWizardComposite) componentWizardComposite;
        ApexComponentModel apexComponentWizardModel = (ApexComponentModel) componentWizard.getComponentWizardModel();
        Component component = apexComponentWizardModel.getComponent();

        // create metadata instance and save metadata input values
        com.salesforce.ide.api.metadata.types.ApexComponent apexComponent =
                (com.salesforce.ide.api.metadata.types.ApexComponent) component.getDefaultMetadataExtInstance();
        apexComponent.setLabel(apexComponentWizardComposite.getLabelString());

        if (logger.isDebugEnabled()) {
            logger.debug("Loaded '" + apexComponent.getClass().getName() + "' with user input");
        }
    }
}
