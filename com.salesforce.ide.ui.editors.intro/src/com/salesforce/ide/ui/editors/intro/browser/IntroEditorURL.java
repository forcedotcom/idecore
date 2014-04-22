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
package com.salesforce.ide.ui.editors.intro.browser;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;
import org.eclipse.ui.intro.config.IIntroURL;
import org.eclipse.ui.intro.config.IntroURLFactory;

import com.salesforce.ide.ui.internal.utils.UIUtils;

public class IntroEditorURL {
    private static Logger logger = Logger.getLogger(IntroEditorURL.class);

    public static final String ACTION_SHOW_UPDATES = "showUpdates"; //$NON-NLS-1$
    public static final String ACTION_SHOW_HELPTOPIC = "showHelpTopic"; //$NON-NLS-1$
    public static final String ACTION_SHOW_CHEATSHEET = "showCheatSheet"; //$NON-NLS-1$
    public static final String ACTION_SHOW_HELPSEARCH = "showHelpSearch"; //$NON-NLS-1$
    public static final String ACTION_SHOW_OPENBROWSER = "openBrowser"; //$NON-NLS-1$

    public static final String PARAM_ID = "id"; //$NON-NLS-1$
    public static final String PARAM_CONTEXT_ID = "cid"; //$NON-NLS-1$
    public static final String PARAM_URL = "url"; //$NON-NLS-1$
    public static final String PARAM_SEARCHTERMS = "searchTerms"; //$NON-NLS-1$

    private static boolean helpFailure = false;

    public static boolean parseUrl(String url) {
        IIntroURL introUrl = IntroURLFactory.createIntroURL(url);
        if (introUrl != null) {
            try {
            	if (introUrl.getAction().equals(ACTION_SHOW_HELPTOPIC)) {
                    if (helpFailure) {
                        showHelpError();
                        return true;
                    }

                    if (introUrl.getParameter(PARAM_ID) != null) {
                        PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(introUrl.getParameter(PARAM_ID));
                    }

                    else if (introUrl.getParameter(PARAM_CONTEXT_ID) != null) {
                        UIUtils.displayExternalHelp(introUrl.getParameter(PARAM_CONTEXT_ID));
                    }
                }

                else if (introUrl.getAction().equals(ACTION_SHOW_CHEATSHEET) && introUrl.getParameter(PARAM_ID) != null) {
                    OpenCheatSheetAction action = new OpenCheatSheetAction(introUrl.getParameter(PARAM_ID));
                    action.run();
                }

                else if (introUrl.getAction().equals(ACTION_SHOW_HELPSEARCH)) {
                    PlatformUI.getWorkbench().getHelpSystem().search(introUrl.getParameter(PARAM_SEARCHTERMS));
                }

                else {
                    introUrl.execute();
                }
            } catch (ExceptionInInitializerError e) {
                helpFailure = true;
                showHelpError();
            } catch (Exception e) {
                helpFailure = true;
                showHelpError();
            }

            return true;
        }

        return false;
    }

    private static void showHelpError() {
        MessageDialog.openError(null, IntroMessages.Intro_helpError_title, IntroMessages.Intro_helpError_message);
    }

    public static String getStatusText(String url) {
        IIntroURL introUrl = IntroURLFactory.createIntroURL(url);
        if (introUrl != null) {
            if (introUrl.getAction().equals(ACTION_SHOW_UPDATES)) {
                return IntroMessages.Intro_updates_statusLine;
            }

            else if (introUrl.getAction().equals(ACTION_SHOW_OPENBROWSER)) {
                return introUrl.getParameter(PARAM_URL);
            }

            else if (introUrl.getAction().equals(ACTION_SHOW_HELPTOPIC)
                    || introUrl.getAction().equals(ACTION_SHOW_CHEATSHEET)) {
                url = introUrl.getParameter(PARAM_ID);

                if (url == null) {
                    url = UIUtils.getHelpUrl(introUrl.getParameter(PARAM_CONTEXT_ID));
                }

                if (url != null) {
                    Path path = new Path(url);
                    return path.lastSegment();
                }
            }
        }

        return ""; //$NON-NLS-1$
    }
}
