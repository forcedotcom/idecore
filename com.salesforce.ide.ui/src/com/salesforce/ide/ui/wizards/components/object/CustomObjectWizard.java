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

import com.salesforce.ide.core.internal.components.object.CustomObjectComponentController;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.ComponentWizardPage;

/**
 * Wizard to create new Custom Object.
 *
 * @author cwall
 */
public class CustomObjectWizard extends ComponentWizard {

    public CustomObjectWizard() throws ForceProjectException {
        super();
        controller = new CustomObjectComponentController();
    }

    @Override
    protected ComponentWizardPage getComponentWizardPageInstance() {
        return new CustomObjectWizardPage(this);
    }
}
