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

import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import com.salesforce.ide.ui.editors.intro.IntroPlugin;

/**
 * This is used to prevent multiple instances of the IntroEditor from being opened
 *
 */
public class IntroEditorInputFactory {
    private static Logger logger = Logger.getLogger(IntroEditorInputFactory.class);

    private static IntroEditorInput input;

    public static IntroEditorInput getIntroEditorInput() {
        if (input == null) {
            input = new IntroEditorInput(getURL());
        }

        return input;
    }

    private static URL getURL() {
        URL url = null;
        try {
            Bundle bundle = IntroPlugin.getDefault().getBundle();
            URL rootContents = FileLocator.find(bundle, new Path("content"), null); //$NON-NLS-1$
            FileLocator.toFileURL(rootContents);
            url = FileLocator.find(bundle, new Path("content/startpage.html"), null); //$NON-NLS-1$
            url = FileLocator.toFileURL(url);
            logger.debug("Setting Intro Editor URL to " + url.toString()); //$NON-NLS-1$
        } catch (Exception e) {
            logger.warn("Unable to find startpage content", e); //$NON-NLS-1$
        }
        return url;
    }

}
