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

import static com.salesforce.ide.core.internal.utils.Constants.*;
import static org.eclipse.ui.handlers.HandlerUtil.*;
import static org.eclipse.ui.internal.dialogs.PropertyDialog.*;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Shell;

/**
 * Re-direct popMenu request to project property page with default selection on Force.com property page.
 */
public final class OpenProjectPropertiesHandler extends BaseHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IProject project = getProjectChecked(event);
        final Shell shell = getActiveShellChecked(event);
        createDialogOn(shell, PROJECT_PROPERTIES_PAGE_ID, project).open();
        return null;
    }

}
