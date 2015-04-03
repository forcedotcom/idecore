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

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;

import com.salesforce.ide.core.internal.utils.Utils;

public final class OpenBrowserToExternalSiteHandler extends AbstractHandler {

    private static final Logger logger = Logger.getLogger(OpenBrowserToExternalSiteHandler.class);
	private static final String PARAM_ID_URL = "url"; //$NON-NLS-1$

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event).getWorkbench();
        final String urlText = StringUtils.defaultString(event.getParameter(PARAM_ID_URL));

        try {
            URL url = new URL(urlText);

            if (logger.isDebugEnabled()) {
                logger.debug("Opening browser to '" + url + "'");
            }

            workbench.getBrowserSupport().getExternalBrowser().openURL(url);
        } catch (Exception e) {
            logger.error("Unable to open default browser to " + urlText, e);
            Utils.openError(e, "Unable to open default browser to " + urlText + ":\n\n" + e.getMessage(), null);
        }

        return null;
    }

}
