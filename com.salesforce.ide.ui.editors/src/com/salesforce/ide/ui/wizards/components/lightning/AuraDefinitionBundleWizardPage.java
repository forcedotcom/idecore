/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.wizards.components.lightning;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.apex.CodeWizardPage;

public class AuraDefinitionBundleWizardPage extends CodeWizardPage {
    
    public AuraDefinitionBundleWizardPage(ComponentWizard componentWizard) {
        super(componentWizard);
    }
    
    @Override
    public String getComponentName() {
        return componentWizardComposite.getComponentName();
    }
    
    public LightningBundleType getSelectedBundleType() {
        return ((AuraDefinitionBundleWizardComposite) componentWizardComposite).getSelectedBundleType();
    }
    
    @Override
    public void createComposite(Composite parent) {
        Component component = componentWizard.getComponentController().getComponent();
        componentWizardComposite = new AuraDefinitionBundleWizardComposite(
            parent,
            SWT.NULL,
            component.getDisplayName(),
            component.getSupportedApiVersions());
        componentWizardComposite.setComponentWizardPage(this);
    }
}
