/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;

import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.utils.UIMessages;

public final class AddOnlineNatureHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(AddOnlineNatureHandler.class);

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event).getWorkbench();
        final IProject project = getProjectChecked(event);

        addOnlineNature(workbench, project);
        return null;
    }

    public static void addOnlineNature(final IWorkbench workbench, final IProject project) {
        try {
            getConnectionFactory().getConnection(project);
        } catch (Exception e) {
            logger.error("Unable to apply Force.com Online Nature to project '" + project.getName() + "'.", e);
            Utils.openError(
                e,
                "Force.com Online Nature Error",
                UIMessages.getString("WorkOnlineAction.ConnectionError.message",
                    new String[] { ForceExceptionUtils.getRootCauseMessage(e) }));
            return;
        }

        try {
            getProjectService().applyOnlineNature(project, null);
            updateDecorators(workbench);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.error("Unable to apply Force.com Online Nature to project '" + project.getName() + "': "
                    + logMessage, e);
            Utils.openError(e, "Force.com Online Nature Error", "Unable to apply Force.com Online Nature to project '"
                    + project.getName() + "': " + e.getMessage());
            return;
        }
    }

}
