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
package com.salesforce.ide;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 *
 *
 * @author cwall
 */
public class ForceIdePlugin extends AbstractUIPlugin {

    public static final String PLUGIN_IN = "com.salesforce.ide";

    private static ForceIdePlugin plugin = null;

    //   C O N S T R U C T O R
    public ForceIdePlugin() {
        plugin = this;
    }

    //   M E T H O D S
    // called upon plug-in activation
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    // called when the plug-in is stopped
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
     public static ForceIdePlugin getDefault() {
         return plugin;
     }
}
