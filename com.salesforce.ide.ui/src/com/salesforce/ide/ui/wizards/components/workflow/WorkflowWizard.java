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
package com.salesforce.ide.ui.wizards.components.workflow;

import com.salesforce.ide.core.internal.components.workflow.WorkflowComponentController;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.ComponentWizardPage;

/**
 * Wizard to create new Workflow.
 *
 * @author cwall
 */
public class WorkflowWizard extends ComponentWizard {

    public WorkflowWizard() throws ForceProjectException {
        super();
        controller = new WorkflowComponentController();
    }

    @Override
    protected ComponentWizardPage getComponentWizardPageInstance() {
        return new WorkflowWizardPage(this);
    }
}
