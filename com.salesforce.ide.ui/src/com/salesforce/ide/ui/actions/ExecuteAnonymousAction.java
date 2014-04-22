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

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.views.executeanonymous.ExecuteAnonymousDialog;

public class ExecuteAnonymousAction extends BaseAction {

    private static final Logger logger = Logger.getLogger(ExecuteAnonymousAction.class);
    
    protected ExecuteAnonymousDialog dialog = null;

    public ExecuteAnonymousAction() throws ForceProjectException {
        super();
    }


    @Override
    public void execute(IAction action) {
        try {
            prepareDialog();
        } catch (ForceProjectException e) {
            logger.error("Unable to open Execute Anonymous dialog", e);
            Utils.openError(e, "Dialog Error", "Unable to open Execute Anonymous dialog: " + e.getMessage());
            return;
        }

        dialog.open();
    }

    protected void prepareDialog() throws ForceProjectException {
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
