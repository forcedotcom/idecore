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

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.salesforce.ide.ui.wizards.components.apex.ApexCodeWizardComposite;

public class ApexPageWizardComposite extends ApexCodeWizardComposite {

    public ApexPageWizardComposite(Composite parent, int style, String componentTypeDisplayName, Set<String> supportedApiVesions) {
        super(parent, style, componentTypeDisplayName);
        initialize(supportedApiVesions);
    }

    protected void initialize(Set<String> supportedApiVesions) {
        setLayout(new GridLayout());
        setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Group grpProperties = createPropertiesGroup(this);
        createLabelAndNameText(null, grpProperties);
        createApiVersionCombo(grpProperties, supportedApiVesions);
        initSize();
    }
}
