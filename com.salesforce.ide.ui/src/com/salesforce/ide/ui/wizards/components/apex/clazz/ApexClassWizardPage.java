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
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.apex.ApexCodeWizardPage;

public class ApexClassWizardPage extends ApexCodeWizardPage {

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
                        .getSupportedApiVersions(), component.getTemplateNames(true));
        componentWizardComposite.setComponentWizardPage(this);
        selectCombo(componentWizardComposite.getCmbTemplateNames(), Constants.DEFAULT_TEMPLATE_NAME);
    }

    public ApexClassWizardComposite getApexClassWizardComposite() {
        return (ApexClassWizardComposite) componentWizardComposite;
    }

    public ApexClassWizard getApexClassWizard() {
        return (ApexClassWizard) componentWizard;
    }

    @Override
    protected void disableAllApexControls() {}

    @Override
    public void saveUserInput() throws InstantiationException, IllegalAccessException {
        if (componentWizardComposite == null) {
            throw new IllegalArgumentException("Component composite cannot be null");
        }

        // nothing saved to metadata instance at this time
        ApexClassWizardComposite apexClassWizardComposite = (ApexClassWizardComposite) componentWizardComposite;
        ApexClassModel apexClassModel = (ApexClassModel) componentWizard.getComponentWizardModel();
        Component component = apexClassModel.getComponent();

        // from given template name, macro-sub vars and save body
        String templateName = apexClassWizardComposite.getCmbTemplateNamesName();
        component.setBodyFromTemplateName(templateName);
    }
}
