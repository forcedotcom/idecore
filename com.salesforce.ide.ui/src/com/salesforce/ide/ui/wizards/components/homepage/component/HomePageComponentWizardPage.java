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
package com.salesforce.ide.ui.wizards.components.homepage.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.generic.GenericComponentWizardPage;

public class HomePageComponentWizardPage extends GenericComponentWizardPage {

    public HomePageComponentWizardPage(ComponentWizard componentWizard) {
        super(componentWizard);
    }

    @Override
    public String getComponentName() {
        return componentWizardComposite.getComponentName();
    }

    @Override
    public void createComposite(Composite parent) {
        Component component = componentWizard.getComponentController().getComponent();
        componentWizardComposite = new HomePageComponentWizardComposite(parent, SWT.NULL, component.getDisplayName());
        componentWizardComposite.setComponentWizardPage(this);
    }

    public HomePageComponentWizardComposite getCustomObjectWizardComposite() {
        return (HomePageComponentWizardComposite) componentWizardComposite;
    }

    public HomePageComponentWizard getCustomObjectWizard() {
        return (HomePageComponentWizard) componentWizard;
    }

    @Override
    public void saveUserInput() throws InstantiationException, IllegalAccessException {
        if (componentWizardComposite == null) {
            throw new IllegalArgumentException("Component composite cannot be null");
        }

        // nothing saved to metadata instance at this time

        /*HomePageComponentWizardComposite homePageComponentWizardComposite =
                (HomePageComponentWizardComposite) componentWizardComposite;
        HomePageComponentWizardModel homePageComponentWizardModel =
                (HomePageComponentWizardModel) componentWizard.getComponentWizardModel();
        Component component = homePageComponentWizardModel.getComponent();

        // create metadata instance and save metadata input values
        com.salesforce.ide.api.metadata.types.HomePageComponent homePageComponent =
                (com.salesforce.ide.api.metadata.types.HomePageComponent) component.getDefaultMetadataExtInstance();

        if (logger.isDebugEnabled()) {
            logger.debug("Loaded '" + homePageComponent.getClass().getName() + "' with user input");
        }*/
    }
}
