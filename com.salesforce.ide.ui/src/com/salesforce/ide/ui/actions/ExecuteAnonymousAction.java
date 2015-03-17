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

import com.salesforce.ide.ui.views.executeanonymous.ExecuteAnonymousDialog;

public class ExecuteAnonymousAction extends BaseAction {
    
    protected ExecuteAnonymousDialog dialog = null;

    public ExecuteAnonymousAction() {
        super();
    }


    @Override
    public void execute(IAction action) {
        prepareDialog();
        dialog.open();
    }

    protected void prepareDialog() {
        if (targetPart != null && targetPart.getSite() != null) {
            dialog = new ExecuteAnonymousDialog(project, targetPart.getSite().getShell());
        } else {
            dialog = new ExecuteAnonymousDialog(project, getShell());
        }
    }

    public ExecuteAnonymousDialog getDialog() {
        return dialog;
    }
}
