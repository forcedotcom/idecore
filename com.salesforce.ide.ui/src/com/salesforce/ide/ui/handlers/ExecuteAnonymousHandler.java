package com.salesforce.ide.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.salesforce.ide.ui.views.executeanonymous.ExecuteAnonymousDialog;

public class ExecuteAnonymousHandler extends BaseHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final Shell shell = HandlerUtil.getActiveShellChecked(event);
        final IProject project = getProjectChecked(event);
        new ExecuteAnonymousDialog(project, shell).open();
        return null;
    }

}
