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
package com.salesforce.ide.ui.actions;

import org.eclipse.jface.action.IAction;

import com.salesforce.ide.core.project.ForceProjectException;

/**
 *
 *
 * @author cwall
 */
public class NewComponentAction extends BaseAction {

    public NewComponentAction() throws ForceProjectException {
        super();
        actionController = new NewComponentActionController();
    }

    @Override
    public void execute(IAction action) {
        ((NewComponentActionController) actionController).openNewComponentWizard();
    }
}
