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
package com.salesforce.ide.schemabrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.salesforce.ide.core.internal.utils.StopWatch;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.schemabrowser.utils.SchemaConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class ForceIdeSchemaBrowserPlugin extends AbstractUIPlugin {

    private static Logger logger = Logger.getLogger(ForceIdeSchemaBrowserPlugin.class);

    private static final StopWatch stopWatch = new StopWatch(ForceIdeSchemaBrowserPlugin.class.getSimpleName());

    // The plug-in ID
    public static final String PLUGIN_ID = SchemaConstants.PLUGIN_PREFIX;

    // The shared instance
    private static ForceIdeSchemaBrowserPlugin plugin;

    //  C O N S T R U C T O R
    public ForceIdeSchemaBrowserPlugin() {
        plugin = this;
    }

    //   M E T H O D S
    public static StopWatch getStopWatch() {
        return stopWatch;
    }

    // called upon plug-in activation
    @Override
    public void start(BundleContext context) throws Exception {
        if (Utils.isDebugMode()) {
            stopWatch.start("ForceIdeSchemaBrowserPlugin.start");
        }

        try {
            super.start(context);
            init();
        } catch (Exception e) {
            if (logger != null) {
                logger.error("Unable to initialize and start plugin '" + PLUGIN_ID + "'", e);
            } else {
                System.err.println("Unable to initialize and start plugin '" + PLUGIN_ID + "': " + e.getMessage());
                e.printStackTrace();
            }
            throw e;
        } finally {
            if (Utils.isDebugMode()) {
                stopWatch.stop("ForceIdeSchemaBrowserPlugin.start");
            }
        }
    }

    // called when the plug-in is stopped
    @Override
    public void stop(BundleContext context) throws Exception {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info("Stopping " + ForceIdeSchemaBrowserPlugin.PLUGIN_ID + " plugin");
        }

        if (logger != null && logger.isDebugEnabled()) {
            logStats();
        }

        super.stop(context);
    }

    //  P L U G I N   I N I T S
    private static void init() {
        System.out.println("Initiated '" + PLUGIN_ID + "' plugin, version " + getBundleVersion());
    }

    public static ForceIdeSchemaBrowserPlugin getDefault() {
        return plugin;
    }

    public static URL getUrlEntry(String resource) {
        if (plugin == null || Utils.isEmpty(resource)) {
            return null;
        }
        return getDefault().getBundle().getEntry(resource);
    }

    public static InputStream getResourceStreamEntry(String resource) throws IOException {
        if (plugin == null || Utils.isEmpty(resource)) {
            return null;
        }

        URL url = getDefault().getBundle().getEntry(resource);
        return url != null ? url.openStream() : null;
    }

    public static String getBundleVersion() {
        return getDefault().getBundle().getHeaders().get("Bundle-Version").toString();
    }

    //   S T A T S
    public static void logStats() {
        logger.debug(PLUGIN_ID + " plugin profiling result:\n" + stopWatch.prettyPrint());
    }
}
