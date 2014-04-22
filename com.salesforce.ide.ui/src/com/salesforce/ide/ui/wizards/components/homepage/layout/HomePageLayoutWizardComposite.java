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
package com.salesforce.ide.ui.wizards.components.homepage.layout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.salesforce.ide.ui.wizards.components.generic.GenericComponentWizardComposite;

public class HomePageLayoutWizardComposite extends GenericComponentWizardComposite {

    public HomePageLayoutWizardComposite(Composite parent, int style, String componentTypeDisplayName) {
        super(parent, style, componentTypeDisplayName);
        initialize();
    }

    @Override
    protected void initializeControls() {
        Group grpProperties = createPropertiesGroup(this);
        createNameGroup(null, grpProperties);
    }
}
