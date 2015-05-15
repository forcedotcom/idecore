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
package com.salesforce.ide.test;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class ForceIdeTest extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.salesforce.ide.test";

    // The shared instance
    private static ForceIdeTest plugin;

    /**
    * The constructor
    */
    public ForceIdeTest() {
        plugin = this;
    }

    /*
    * (non-Javadoc)
    * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
    */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /*
    * (non-Javadoc)
    * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
    */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
    * Returns the shared instance
    *
    * @return the shared instance
    */
    public static ForceIdeTest getDefault() {
        return plugin;
    }

}
