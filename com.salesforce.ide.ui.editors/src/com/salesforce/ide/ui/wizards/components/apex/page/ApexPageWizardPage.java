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
package com.salesforce.ide.ui.wizards.components.apex.page;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.components.apex.page.ApexPageModel;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.apex.CodeWizardPage;

public class ApexPageWizardPage extends CodeWizardPage {

    private static final Logger logger = Logger.getLogger(ApexPageWizardPage.class);

    public ApexPageWizardPage(ComponentWizard componentWizard) {
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
                new ApexPageWizardComposite(parent, SWT.NULL, component.getDisplayName(), component
                        .getSupportedApiVersions());
        componentWizardComposite.setComponentWizardPage(this);
    }

    public ApexPageWizardComposite getApexPageWizardComposite() {
        return (ApexPageWizardComposite) componentWizardComposite;
    }

    public ApexPageWizard getApexPageWizard() {
        return (ApexPageWizard) componentWizard;
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

        ApexPageWizardComposite apexPageWizardComposite = (ApexPageWizardComposite) componentWizardComposite;
        ApexPageModel apexPageWizardModel = (ApexPageModel) componentWizard.getComponentWizardModel();
        Component component = apexPageWizardModel.getComponent();

        // create metadata instance and save metadata input values
        com.salesforce.ide.api.metadata.types.ApexPage apexPage =
                (com.salesforce.ide.api.metadata.types.ApexPage) component.getDefaultMetadataExtInstance();
        apexPage.setLabel(apexPageWizardComposite.getLabelString());
        apexPage.setFullName(component.getName());

        if (logger.isDebugEnabled()) {
            logger.debug("Created and loaded instance of '" + apexPage.getClass().getName() + "' with user input");
        }
    }
}
