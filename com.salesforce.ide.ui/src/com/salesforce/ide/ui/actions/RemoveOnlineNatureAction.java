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
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.project.OnlineNature;
import com.salesforce.ide.ui.internal.utils.UIMessages;

public class RemoveOnlineNatureAction extends BaseChangeNatureAction {
    private static final Logger logger = Logger.getLogger(RemoveOnlineNatureAction.class);

    public RemoveOnlineNatureAction() throws ForceProjectException {
        super();
    }

    public RemoveOnlineNatureAction(IProject project) throws ForceProjectException {
        super();
        this.project = project;
    }

    @Override
    public void init() {
    }

    @Override
    public void execute(IAction action) {
        boolean response = Utils.openQuestion("Confirm Work Offline",
                UIMessages.getString("WorkOfflineAction.General.message"));

        if (response) {
            OnlineNature.removeNature(project, null);
            updateDecorators();

            if (logger.isDebugEnabled()) {
                logger.debug("***   O F F L I N E   ***");
            }
        }
    }
}
