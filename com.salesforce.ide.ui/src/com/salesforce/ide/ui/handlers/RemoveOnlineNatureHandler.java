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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.OnlineNature;
import com.salesforce.ide.ui.internal.utils.UIMessages;

public final class RemoveOnlineNatureHandler extends BaseHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event).getWorkbench();
        final IProject project = getProjectChecked(event);

        removeOnlineNature(workbench, project);
        return null;
    }

    public static void removeOnlineNature(final IWorkbench workbench, final IProject project) {
        boolean response = Utils.openQuestion("Confirm Work Offline", UIMessages.getString("WorkOfflineAction.General.message"));

        if (response) {
            OnlineNature.removeNature(project, null);
            updateDecorators(workbench);
        }
    }

}
