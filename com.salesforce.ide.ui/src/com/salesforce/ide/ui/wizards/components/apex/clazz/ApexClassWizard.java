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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import com.salesforce.ide.core.internal.components.apex.clazz.ApexClassComponentController;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.wizards.components.ComponentWizardPage;
import com.salesforce.ide.ui.wizards.components.apex.ApexCodeWizard;

/**
 * Wizard to create new Apex Class.
 * 
 * @author cwall
 */
public class ApexClassWizard extends ApexCodeWizard {

    public ApexClassWizard() throws ForceProjectException {
        super();
        controller = new ApexClassComponentController();
    }

    public ApexClassWizard(IFolder folder) {
        super(folder, folder.getProject());
    }

    public ApexClassWizard(IFolder folder, IProject project) {
        super(folder, project);
    }

    @Override
    protected ComponentWizardPage getComponentWizardPageInstance() {
        return new ApexClassWizardPage(this);
    }
}
