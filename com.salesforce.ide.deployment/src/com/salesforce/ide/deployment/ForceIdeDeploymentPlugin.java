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
package com.salesforce.ide.deployment;

import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.salesforce.ide.core.internal.utils.StopWatch;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.deployment.internal.utils.DeploymentConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class ForceIdeDeploymentPlugin extends AbstractUIPlugin {

    private static Logger logger = Logger.getLogger(ForceIdeDeploymentPlugin.class);

    private static final StopWatch stopWatch = new StopWatch(ForceIdeDeploymentPlugin.class.getSimpleName());

    // The plug-in ID
    public static final String PLUGIN_ID = DeploymentConstants.PLUGIN_PREFIX;

    // The shared instance
    private static ForceIdeDeploymentPlugin plugin;

    //  C O N S T R U C T O R
    public ForceIdeDeploymentPlugin() {
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
            stopWatch.start("ForceIdeDeploymentPlugin.start");
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
                stopWatch.stop("ForceIdeDeploymentPlugin.start");
            }
        }
    }

    // called when the plug-in is stopped
    @Override
    public void stop(BundleContext context) throws Exception {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info("Stopping " + PLUGIN_ID + " plugin");
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

    //   U T I L S
    public static String getPluginId() {
        return getDefault() != null && getDefault().getBundle() != null ? getDefault().getBundle().getSymbolicName() : PLUGIN_ID;
    }

    public static ForceIdeDeploymentPlugin getDefault() {
        return plugin;
    }

    public static URL getUrlEntry(String resource) {
        if (plugin == null || Utils.isEmpty(resource)) {
            return null;
        }
        return getDefault().getBundle().getEntry(resource);
    }

    public static String getBundleVersion() {
        return getDefault().getBundle().getHeaders().get("Bundle-Version").toString();
    }

    public static Shell getActiveWorkbenchShell() {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window != null) {
            return window.getShell();
        }
        return null;
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    //   P R E F E R E N C E S
    public static void savePreference(String name, String value) {
        if (Utils.isNotEmpty(name) && Utils.isNotEmpty(value)) {
            getDefault().getPreferenceStore().setValue(name, value);
        }
    }

    public static void savePreference(String name, boolean value) {
        if (Utils.isNotEmpty(name)) {
            getDefault().getPreferenceStore().setValue(name, value);
        }
    }

    public static String getPreferenceString(String name) {
        if (Utils.isEmpty(name)) {
            return null;
        }
        return getDefault().getPreferenceStore().getString(name);
    }

    public static boolean getPreferenceBoolean(String name) {
        if (Utils.isEmpty(name)) {
            return false;
        }
        return getDefault().getPreferenceStore().getBoolean(name);
    }

    //   S T A T S
    public static void logStats() {
        logger.debug(PLUGIN_ID + " plugin profiling result:\n" + stopWatch.prettyPrint());
    }
}
