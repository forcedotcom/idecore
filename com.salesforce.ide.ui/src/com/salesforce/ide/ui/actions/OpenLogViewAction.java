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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import com.salesforce.ide.ui.ForceIdeUIPlugin;
import com.salesforce.ide.ui.internal.utils.UIConstants;

public class OpenLogViewAction implements IWorkbenchWindowActionDelegate {

    private static Logger logger = Logger.getLogger(OpenLogViewAction.class);

    public OpenLogViewAction() {}

    @Override
    public void dispose() {}

    @Override
    public void init(IWorkbenchWindow iworkbenchwindow) {}

    @Override
    public void run(IAction action) {
        try {
            IWorkbenchWindow window = ForceIdeUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
            if (window != null) {
                IWorkbenchPage page = window.getActivePage();
                page.showView(UIConstants.IDE_LOG_VIEW_ID);
            }
        } catch (PartInitException ex) {
            logger.error("Unable to open Log View", ex);
        }
    }

    @Override
    public void selectionChanged(IAction iaction, ISelection iselection) {}

}
