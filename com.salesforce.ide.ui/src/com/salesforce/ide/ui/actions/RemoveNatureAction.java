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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.internal.utils.UIConstants;

public class RemoveNatureAction extends BaseChangeNatureAction {
    private static final Logger logger = Logger.getLogger(RemoveNatureAction.class);

    public RemoveNatureAction() throws ForceProjectException {
        super();
    }

    public RemoveNatureAction(IProject project) throws ForceProjectException {
        super();
        this.project = project;
    }

    @Override
    public void init() {}

    @Override
    public void execute(IAction action) {
        boolean response =
                Utils.openQuestion("Remove Force.com Nature",
                    "Are you sure you want to remove Force.com Nature for project '" + project.getName() + "'?");
        if (response) {
            removeNature();
        }
    }

    public boolean removeNature() {
        try {
            getProjectService().removeNatures(project, null);
            updateDecorators();
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to remove " + UIConstants.PLUGIN_NAME + " natures: " + logMessage, e);
            return false;
        }
        return true;
    }
}
