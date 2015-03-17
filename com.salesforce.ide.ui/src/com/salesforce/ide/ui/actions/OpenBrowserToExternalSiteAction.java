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

import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.core.internal.utils.Utils;

/**
 *
 * 
 * @author cwall
 */
public class OpenBrowserToExternalSiteAction extends BaseAction implements IWorkbenchWindowActionDelegate {

    private static final Logger logger = Logger.getLogger(OpenBrowserToExternalSiteAction.class);

    private String url = null;

    //   C O N S T R U C T O R S
    public OpenBrowserToExternalSiteAction() {
        super();
    }

    //  M E T H O D S
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void init(IWorkbenchWindow window) {
        setShell(window.getShell());
    }

    public void setSelection(IStructuredSelection selection) {
        this.selection = selection;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void execute(IAction action) {
    	openURL(getUrl());
    }

    protected void openURL(String urlStr) {
        try {
            URL url = new URL(urlStr);

            if (logger.isDebugEnabled()) {
                logger.debug("Opening browser to '" + url.toString() + "'");
            }

            PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
        } catch (Exception e) {
            logger.error("Unable to open default browser to " + urlStr, e);
            Utils.openError(e, "Unable to open default browser to " + urlStr + ":\n\n" + e.getMessage(), null);
        }
    }

    @Override
    public void init() {

    }
}
