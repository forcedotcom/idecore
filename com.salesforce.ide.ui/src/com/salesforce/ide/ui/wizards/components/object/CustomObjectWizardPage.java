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
package com.salesforce.ide.ui.wizards.components.object;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.components.object.CustomObjectModel;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.generic.GenericComponentWizardPage;

public class CustomObjectWizardPage extends GenericComponentWizardPage {

    private static final Logger logger = Logger.getLogger(CustomObjectWizardPage.class);

    public CustomObjectWizardPage(ComponentWizard componentWizard) {
        super(componentWizard);
    }

    @Override
    public String getComponentName() {
        return componentWizardComposite.getComponentName();
    }

    @Override
    public void createComposite(Composite parent) {
        Component component = componentWizard.getComponentController().getComponent();
        componentWizardComposite = new CustomObjectWizardComposite(parent, SWT.NULL, component.getDisplayName());
        componentWizardComposite.setComponentWizardPage(this);
    }

    public CustomObjectWizardComposite getCustomObjectWizardComposite() {
        return (CustomObjectWizardComposite) componentWizardComposite;
    }

    public CustomObjectWizard getCustomObjectWizard() {
        return (CustomObjectWizard) componentWizard;
    }

    @Override
    public void saveUserInput() throws InstantiationException, IllegalAccessException {
        if (componentWizardComposite == null) {
            throw new IllegalArgumentException("Component composite cannot be null");
        }

        CustomObjectWizardComposite customObjectWizardComposite =
                (CustomObjectWizardComposite) componentWizardComposite;
        CustomObjectModel customObjectWizardModel = (CustomObjectModel) componentWizard.getComponentWizardModel();
        Component component = customObjectWizardModel.getComponent();

        // create metadata instance and save metadata input values
        com.salesforce.ide.api.metadata.types.CustomObject customObject =
                (com.salesforce.ide.api.metadata.types.CustomObject) component.getDefaultMetadataExtInstance();
        customObject.setLabel(customObjectWizardComposite.getLabelString());
        String plural =
                Utils.isNotEmpty(customObjectWizardComposite.getPluralLabelString()) ? customObjectWizardComposite
                        .getPluralLabelString() : Utils.getPlural(customObjectWizardComposite.getLabelString());
        customObject.setPluralLabel(plural);

        if (logger.isDebugEnabled()) {
            logger.debug("Created and loaded instance of '" + customObject.getClass().getName() + "' with user input");
        }
    }
}
