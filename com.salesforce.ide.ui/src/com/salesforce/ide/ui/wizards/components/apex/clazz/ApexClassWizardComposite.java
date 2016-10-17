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

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.salesforce.ide.ui.wizards.components.apex.ApexCodeWizardComposite;

public class ApexClassWizardComposite extends ApexCodeWizardComposite {

    public ApexClassWizardComposite(Composite parent, int style, String componentTypeDisplayName,
            Set<String> supportedApiVesions, Set<String> templateNames) {
        super(parent, style, componentTypeDisplayName);
        initialize(supportedApiVesions, templateNames);
    }

    protected void initialize(Set<String> supportedApiVesions, Set<String> templateNames) {
        setLayout(new GridLayout());
        setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Group grpProperties = createPropertiesGroup(this);
        createNameText(grpProperties);
        createApiVersionCombo(grpProperties, supportedApiVesions);
        createTemplateCombo(grpProperties, templateNames);
        initSize();
    }
}
