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
package com.salesforce.ide.api;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ForceIdeAPIPlugin extends AbstractUIPlugin {

    private static Logger logger = Logger.getLogger(ForceIdeAPIPlugin.class);

    // The plug-in ID
    public static final String PLUGIN_ID = "com.salesforce.ide.api";

    // The shared instance
    private static ForceIdeAPIPlugin plugin;

    /**
     * The constructor
     */
    public ForceIdeAPIPlugin() {
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
    public static ForceIdeAPIPlugin getDefault() {
        return plugin;
    }

    public static URL getUrlEntry(String resource) {
        if (plugin == null || isEmpty(resource)) {
            return null;
        }
        return getDefault().getBundle().getEntry(resource);
    }

    public static URL getFullUrlResource(String resource) {
        if (plugin == null || isEmpty(resource)) {
            return null;
        }

        URL urlResource = getDefault().getBundle().getResource(resource);
        if (urlResource != null) {
            try {
                urlResource = FileLocator.toFileURL(urlResource);
            } catch (IOException e) {
                logger.error("Unable to get full url for resource '" + resource + "'");
            }
        }
        return urlResource;
    }

    public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }

}
