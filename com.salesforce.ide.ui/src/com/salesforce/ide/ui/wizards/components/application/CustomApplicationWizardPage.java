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
package com.salesforce.ide.ui.wizards.components.application;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.components.application.CustomApplicationModel;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.generic.GenericComponentWizardPage;

public class CustomApplicationWizardPage extends GenericComponentWizardPage {

    private static final Logger logger = Logger.getLogger(CustomApplicationWizardPage.class);

    public CustomApplicationWizardPage(ComponentWizard componentWizard) {
        super(componentWizard);
    }

    @Override
    public String getComponentName() {
        return componentWizardComposite.getComponentName();
    }

    @Override
    public void createComposite(Composite parent) {
        Component component = componentWizard.getComponentController().getComponent();
        componentWizardComposite = new CustomApplicationWizardComposite(parent, SWT.NULL, component.getDisplayName());
        componentWizardComposite.setComponentWizardPage(this);
    }

    public CustomApplicationWizardComposite getCustomApplicationWizardComposite() {
        return (CustomApplicationWizardComposite) componentWizardComposite;
    }

    public CustomApplicationWizard getCustomApplicationWizard() {
        return (CustomApplicationWizard) componentWizard;
    }

    @Override
    public void saveUserInput() throws InstantiationException, IllegalAccessException {
        if (componentWizardComposite == null) {
            throw new IllegalArgumentException("Component composite cannot be null");
        }

        CustomApplicationWizardComposite customApplicationWizardComposite =
                (CustomApplicationWizardComposite) componentWizardComposite;
        CustomApplicationModel customApplicationWizardModel =
                (CustomApplicationModel) componentWizard.getComponentWizardModel();
        Component component = customApplicationWizardModel.getComponent();

        // create metadata instance and save metadata input values
        com.salesforce.ide.api.metadata.types.CustomApplication customApp =
                (com.salesforce.ide.api.metadata.types.CustomApplication) component.getDefaultMetadataExtInstance();
        // customApp.getTab().add("Home");
        customApp.setLabel(customApplicationWizardComposite.getLabelString());

        if (logger.isDebugEnabled()) {
            logger.debug("Created and loaded instance of '" + customApp.getClass().getName() + "'");
        }
    }
}
