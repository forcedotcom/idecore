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
package com.salesforce.ide.ui.wizards.components.profile;

import com.salesforce.ide.core.internal.components.profile.ProfileComponentController;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.wizards.components.ComponentWizardPage;
import com.salesforce.ide.ui.wizards.components.generic.GenericComponentWizard;

/**
 * Wizard to create new Profile.
 *
 * @author cwall
 */
public class ProfileWizard extends GenericComponentWizard {

    public ProfileWizard() throws ForceProjectException {
        super();
        controller = new ProfileComponentController();
    }

    @Override
    protected ComponentWizardPage getComponentWizardPageInstance() {
        return new ProfileWizardPage(this);
    }
}
