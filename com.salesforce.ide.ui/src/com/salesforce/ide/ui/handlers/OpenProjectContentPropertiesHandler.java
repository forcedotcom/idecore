package com.salesforce.ide.ui.handlers;

import static com.salesforce.ide.core.internal.utils.Constants.*;
import static org.eclipse.ui.handlers.HandlerUtil.*;
import static org.eclipse.ui.internal.dialogs.PropertyDialog.*;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Shell;

/**
 * Re-direct popMenu request to project property page with default selection on Force.com/Project Content property page.
 */
public final class OpenProjectContentPropertiesHandler extends BaseHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IProject project = getProjectChecked(event);
        final Shell shell = getActiveShellChecked(event);
        createDialogOn(shell, PROJECT_CONTENT_PROPERTIES_PAGE_ID, project).open();
        return null;
    }

}
