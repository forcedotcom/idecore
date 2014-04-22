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
package com.salesforce.ide.ui.editors.intro;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.salesforce.ide.ui.editors.intro.browser.IntroStartup;

/**
 * The activator class controls the plug-in life cycle
 */
public class IntroPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.salesforce.ide.ui.editors.intro"; //$NON-NLS-1$

    // The shared instance
    private static IntroPlugin plugin;

    /**
     * The constructor
     */
    public IntroPlugin() {}

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        IntroStartup.removePerspectiveListener();
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static IntroPlugin getDefault() {
        return plugin;
    }
}
