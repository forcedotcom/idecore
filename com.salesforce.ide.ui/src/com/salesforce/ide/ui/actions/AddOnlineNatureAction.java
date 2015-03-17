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

import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.utils.UIMessages;

/**
 *
 * 
 * @author cwall
 */
public class AddOnlineNatureAction extends BaseChangeNatureAction {

    private static final Logger logger = Logger.getLogger(AddOnlineNatureAction.class);

    public AddOnlineNatureAction() {
        super();
    }

    public AddOnlineNatureAction(IProject project) {
        super();
        this.project = project;
    }

    @Override
    public void init() {}

    @Override
    public void execute(IAction action) {
        try {
            getConnectionFactory().getConnection(project);
        } catch (Exception e) {
            logger.error("Unable to apply Force.com Online Nature to project '" + project.getName() + "'.", e);
            Utils.openError(e, "Force.com Online Nature Error", UIMessages
                    .getString("WorkOnlineAction.ConnectionError.message", new String[] { ForceExceptionUtils
                            .getRootCauseMessage(e) }));
            return;
        }

        try {
            getProjectService().applyOnlineNature(project, null);
            updateDecorators();
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.error("Unable to apply Force.com Online Nature to project '" + project.getName() + "': "
                    + logMessage, e);
            Utils.openError(e, "Force.com Online Nature Error", "Unable to apply Force.com Online Nature to project '"
                    + project.getName() + "': " + e.getMessage());
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("***   O N L I N E   ***");
        }
    }
}
