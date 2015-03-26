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

import com.salesforce.ide.core.internal.components.homepage.layout.HomePageLayoutComponentController;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.wizards.components.ComponentWizardPage;
import com.salesforce.ide.ui.wizards.components.generic.GenericComponentWizard;

/**
 * Wizard to create new Custom Object.
 *
 * @author cwall
 */
public class HomePageLayoutWizard extends GenericComponentWizard {

    public HomePageLayoutWizard() throws ForceProjectException {
        super();
        controller = new HomePageLayoutComponentController();
    }

    @Override
    protected ComponentWizardPage getComponentWizardPageInstance() {
        return new HomePageLayoutWizardPage(this);
    }
}
