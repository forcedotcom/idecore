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

        boolean response = Utils.openQuestion("Confirm Work Offline",
            UIMessages.getString("WorkOfflineAction.General.message"));

        if (response) {
            OnlineNature.removeNature(project, null);
            updateDecorators(workbench);
        }

        return null;
    }

}
