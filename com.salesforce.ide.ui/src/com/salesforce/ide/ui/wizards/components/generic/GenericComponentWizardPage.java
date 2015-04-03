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
package com.salesforce.ide.ui.wizards.components.generic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.api.metadata.types.MetadataExt;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.ComponentWizardPage;
import com.salesforce.ide.ui.wizards.components.IComponentWizardPage;

public class GenericComponentWizardPage extends ComponentWizardPage {

    public GenericComponentWizardPage(ComponentWizard componentWizard) {
        super(componentWizard);
    }

    public String getComponentName() {
        return componentWizardComposite.getComponentName();
    }

    @Override
    public void createComposite(Composite parent) {
        Component component = componentWizard.getComponentController().getComponent();
        componentWizardComposite = new GenericComponentWizardComposite(parent, SWT.NULL, component.getDisplayName());
        componentWizardComposite.setComponentWizardPage(this);
    }

    public GenericComponentWizardComposite getGenericComponentWizardComposite() {
        return (GenericComponentWizardComposite) componentWizardComposite;
    }

    @Override
    protected void additionalInitialize(Composite parent) {
        super.additionalInitialize(parent);
    }

    @Override
    protected boolean finalDialogChanged(IComponentWizardPage componentWizardPage) {
        return true;
    }

    @Override
    protected boolean initialDialogChanged(IComponentWizardPage componentWizardPage) {
        return true;
    }

    @Override
    public void saveUserInput() throws InstantiationException, IllegalAccessException {
        // create metadata instance and save metadata input values
        MetadataExt metadataExt =
                componentWizard.getComponentWizardModel().getComponent().getDefaultMetadataExtInstance();
        metadataExt.setFullName(componentWizardComposite.getNameString());
    }

    @Override
    protected boolean isAlphaNumericRequred() {
        return componentWizard.getComponentController().getComponent().isAlphaNumeric();
    }
}
