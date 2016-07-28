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
package com.salesforce.ide.ui.wizards.components.apex.clazz;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.components.apex.clazz.ApexClassModel;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.apex.CodeWizardPage;

public class ApexClassWizardPage extends CodeWizardPage {

    public ApexClassWizardPage(ComponentWizard componentWizard) {
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
                new ApexClassWizardComposite(parent, SWT.NULL, component.getDisplayName(), component
                        .getSupportedApiVersions());
        componentWizardComposite.setComponentWizardPage(this);
    }

    public ApexClassWizardComposite getApexClassWizardComposite() {
        return (ApexClassWizardComposite) componentWizardComposite;
    }

    public ApexClassWizard getApexClassWizard() {
        return (ApexClassWizard) componentWizard;
    }

    @Override
    public void saveUserInput() throws InstantiationException, IllegalAccessException {
        // nothing saved to metadata instance at this time
        ApexClassModel apexClassModel = (ApexClassModel) componentWizard.getComponentWizardModel();
        Component component = apexClassModel.getComponent();
        component.setBody("");
    }
}
