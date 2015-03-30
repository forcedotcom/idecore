package com.salesforce.ide.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;

import com.salesforce.ide.core.internal.utils.Utils;

public final class RemoveNatureHandler extends BaseHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event).getWorkbench();
        final IProject project = getProjectChecked(event);
        boolean response = Utils.openQuestion("Remove Force.com Nature",
                    "Are you sure you want to remove Force.com Nature for project '" + project.getName() + "'?");
        if (response) {
            removeNature(workbench, project);
        }

        return null;
    }

    private static boolean removeNature(final IWorkbench workbench, final IProject project) {
        getProjectService().removeNatures(project, null);
        updateDecorators(workbench);
        return true;
    }

}
