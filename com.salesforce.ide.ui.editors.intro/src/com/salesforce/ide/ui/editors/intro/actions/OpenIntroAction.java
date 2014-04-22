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
package com.salesforce.ide.ui.editors.intro.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import com.salesforce.ide.ui.editors.intro.IntroPlugin;
import com.salesforce.ide.ui.editors.intro.browser.IntroEditor;
import com.salesforce.ide.ui.editors.intro.browser.IntroEditorInputFactory;

public class OpenIntroAction implements IWorkbenchWindowActionDelegate {
    private static Logger logger = Logger.getLogger(OpenIntroAction.class);

    public OpenIntroAction() {}

    public void dispose() {}

    public void init(IWorkbenchWindow iworkbenchwindow) {}

    public void run(IAction action) {
        try {
            IWorkbenchWindow window = getWorkbenchWindow();
            if (window != null) {
                IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                    openIntroEditor(page);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Opening Intro Editor"); //$NON-NLS-1$
                    }
                }
            }
        } catch (PartInitException ex) {
            logger.warn("Unable to open Force.com IDE Start Page", ex); //$NON-NLS-1$
        }
    }

    protected IWorkbenchWindow getWorkbenchWindow() {
        return IntroPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    protected void openIntroEditor(IWorkbenchPage page) throws PartInitException {
        IDE.openEditor(page, IntroEditorInputFactory.getIntroEditorInput(), IntroEditor.ID);
    }

    public void selectionChanged(IAction iaction, ISelection iselection) {}

}
