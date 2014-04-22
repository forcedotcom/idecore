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
package com.salesforce.ide.upgrade;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.salesforce.ide.core.internal.utils.StopWatch;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.upgrade.internal.UpgradeNotifier;
import com.salesforce.ide.upgrade.internal.UpgradeProjectInspector;
import com.salesforce.ide.upgrade.internal.utils.UpgradeConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class ForceIdeUpgradePlugin extends AbstractUIPlugin {

    private static Logger logger = Logger.getLogger(ForceIdeUpgradePlugin.class);

    public static final StopWatch stopWatch = new StopWatch(ForceIdeUpgradePlugin.class.getSimpleName());
    public static final String PLUGIN_ID = UpgradeConstants.PLUGIN_PREFIX;
    private static ForceIdeUpgradePlugin plugin = null;
    private UpgradeProjectInspector upgradeProjectInspector = null;
    private UpgradeNotifier upgradeNotifier = null;

    // C O N S T R U C T O R
    public ForceIdeUpgradePlugin() {
        plugin = this;
    }

    // M E T H O D S
    public UpgradeProjectInspector getUpgradeProjectInspector() {
        return upgradeProjectInspector;
    }

    public UpgradeNotifier getUpgradeNotifier() {
        return upgradeNotifier;
    }

    public static StopWatch getStopWatch() {
        return stopWatch;
    }

    // called upon plug-in activation
    @Override
    public void start(BundleContext context) throws Exception {
        if (Utils.isDebugMode()) {
            stopWatch.start("ForceIdeUpgradePlugin.start");
        }

        try {
            super.start(context);

            if (Utils.isUpgradeEnabled()) {
                init();
            }

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
                stopWatch.stop("ForceIdeUpgradePlugin.start");
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

        if (upgradeProjectInspector != null) {
            upgradeProjectInspector.dispose();
        }

        if (upgradeNotifier != null) {
            upgradeNotifier.dispose();
        }

        super.stop(context);
    }

    // P L U G I N I N I T S
    public void init() {
        // initApplicationContext();
        initUpgradeInspector();
        initUpgradeNotifier();

        System.out.println("Initiated '" + PLUGIN_ID + "' plugin, version " + getBundleVersion());
    }

    private void initUpgradeInspector() {
        if (logger != null && logger.isDebugEnabled()) {
            stopWatch.start("ForceIdeUpgradePlugin.evaluateProjectForUpgradeability");
        }

        upgradeProjectInspector = new UpgradeProjectInspector();
        upgradeProjectInspector.setSystem(true);
        upgradeProjectInspector.schedule();

        if (logger != null && logger.isDebugEnabled()) {
            stopWatch.stop("ForceIdeUpgradePlugin.evaluateProjectForUpgradeability");
        }
    }

    private void initUpgradeNotifier() {
        if (logger != null && logger.isDebugEnabled()) {
            stopWatch.start("ForceIdeUpgradePlugin.initUpgradeNotifier");
        }

        upgradeNotifier = new UpgradeNotifier();

        if (logger != null && logger.isDebugEnabled()) {
            stopWatch.stop("ForceIdeUpgradePlugin.initUpgradeNotifier");
        }
    }

    public void enableUpgrade(boolean enable) {
        if (logger.isDebugEnabled()) {
            logger.debug((enable ? "Enabling" : "Disabling") + " upgrade");
        }

        if (enable) {

            if (upgradeProjectInspector == null) {
                initUpgradeInspector();
            } else {
                upgradeProjectInspector.setEnabled(enable);
                upgradeProjectInspector.inspectProjects(new NullProgressMonitor());
            }

            if (upgradeNotifier == null) {
                initUpgradeNotifier();
            }
        }

        upgradeProjectInspector.setEnabled(enable);
        upgradeNotifier.setEnabled(enable);
    }

    // U T I L S
    public static String getPluginId() {
        return getDefault() != null && getDefault().getBundle() != null ? getDefault().getBundle().getSymbolicName() : PLUGIN_ID;
    }

    public static ForceIdeUpgradePlugin getDefault() {
        return plugin;
    }

    public static URL getFullUrlResource(String resource) {
        if (plugin == null || Utils.isEmpty(resource)) { return null; }

        URL urlResource = getDefault().getBundle().getResource(resource);
        if (urlResource != null) {
            try {
                urlResource = FileLocator.toFileURL(urlResource);
            } catch (IOException e) {
                logger.warn("Unable to get full url for resource '" + resource + "': " + e.getMessage());
            }
        }
        return urlResource;
    }

    public static String getBundleVersion() {
        return getBundleVersion(false);
    }

    public static String getBundleVersion(boolean stripQualifer) {
        if (stripQualifer) {
            String ideVersion = getDefault().getBundle().getHeaders().get("Bundle-Version").toString();

            if (Utils.isNotEmpty(ideVersion) && ideVersion.contains(".")) {
                ideVersion = ideVersion.substring(0, ideVersion.lastIndexOf("."));
            }

            return ideVersion;
        }
		return getDefault().getBundle().getHeaders().get("Bundle-Version").toString();
    }

    // S T A T S
    public static void logStats() {
        logger.debug(PLUGIN_ID + " plugin profiling result:\n" + stopWatch.prettyPrint());
    }
}
