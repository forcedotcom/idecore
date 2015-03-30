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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;

import com.salesforce.ide.ui.ForceIdeUIPlugin;
import com.salesforce.ide.ui.internal.utils.UIConstants;

public class OpenForcePerspectiveAction extends BaseAction {
    private static final Logger logger = Logger.getLogger(OpenForcePerspectiveAction.class);

    public OpenForcePerspectiveAction() {
        super();
    }

    @Override
    public void run() {
        execute(null);
    }

    @Override
    public void execute(IAction action) {
        IWorkbench workbench= ForceIdeUIPlugin.getDefault().getWorkbench();
        IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
        IWorkbenchPage page= window.getActivePage();
        IAdaptable input;

        if (page != null) {
            input= page.getInput();
        } else {
            input= ResourcesPlugin.getWorkspace().getRoot();
        }

        try {
            workbench.showPerspective(UIConstants.FORCE_PERSPECTIVE_ID, window, input);
        } catch (WorkbenchException e) {
            logger.error("Unable to open Force.com Perspective", e);
        }
    }

    @Override
    public void init() {

    }
}
