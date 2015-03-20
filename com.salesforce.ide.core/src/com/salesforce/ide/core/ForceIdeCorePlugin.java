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
package com.salesforce.ide.core;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.preferences.PreferenceManager;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.StopWatch;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ProjectAddEvaluator;
import com.salesforce.ide.core.project.ProjectDeletePreparator;

/**
 * The activator class controls the plug-in life cycle
 */
public class ForceIdeCorePlugin extends AbstractUIPlugin {

    private static Logger logger = Logger.getLogger(ForceIdeCorePlugin.class);

    public static final StopWatch stopWatch = new StopWatch(ForceIdeCorePlugin.class.getSimpleName());
    public static final String PLUGIN_ID = Constants.PLUGIN_PREFIX;
    private static ForceIdeCorePlugin plugin = null;
    private PreferenceManager preferenceManager = null;
    private ProjectDeletePreparator projectDeletePreparator = null;
    private ProjectAddEvaluator projectAddEvaluator = null;

    // C O N S T R U C T O R
    public ForceIdeCorePlugin() {
        plugin = this;
    }

    // M E T H O D S
    public static StopWatch getStopWatch() {
        return stopWatch;
    }

    public ProjectDeletePreparator getProjectDeletePreparator() {
        return projectDeletePreparator;
    }

    // called upon plug-in activation
    @Override
    public void start(BundleContext context) throws Exception {
        if (Utils.isDebugMode()) {
            stopWatch.start("ForceIdeCorePlugin.start");
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
                stopWatch.stop("ForceIdeCorePlugin.start");
            }
        }
    }

    // called when the plug-in is stopped
    @Override
    public void stop(BundleContext context) throws Exception {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info("Stopping " + PLUGIN_ID + " plugin");
        }

        if (preferenceManager != null) {
            preferenceManager.dispose();
        }

        if (projectDeletePreparator != null) {
            projectDeletePreparator.dispose();
        }

        if (projectAddEvaluator != null) {
            projectAddEvaluator.dispose();
        }

        if (logger != null && logger.isDebugEnabled()) {
            logStats();
        }
        ContainerDelegate.getInstance().dispose();

        super.stop(context);
    }

    // P L U G I N I N I T S
    private void init() {
        initialLogger();
        initApplicationContext();
        initPreferences();

        // project delete preparation
        projectDeletePreparator = new ProjectDeletePreparator();

        // evaluate added projects for builder skipping
        projectAddEvaluator = new ProjectAddEvaluator();

        System.out.println("Initiated '" + PLUGIN_ID + "' plugin, version " + getBundleVersion());
    }

    // initialize logger
    private void initialLogger() {
        if (Utils.isDebugMode()) {
            stopWatch.start("ForceIdeCorePlugin.initialLogger");
        }

        try {
            URL logConfig = getUrlEntry(Constants.LOG_CONFIG_FILE);
            if (logConfig != null) {
                logConfig = FileLocator.toFileURL(logConfig);
                DOMConfigurator.configure(logConfig);
            }

            String debug = System.getProperty(Constants.SYS_SETTING_DEBUG);
            if (Utils.isNotEmpty(debug) && Constants.SYS_SETTING_DEBUG_VALUE.equals(debug)) {
                Logger tmpLogger = Logger.getLogger(Constants.FORCE_PLUGIN_PREFIX);
                if (tmpLogger != null) {
                    tmpLogger.setLevel(Level.DEBUG);
                }
            }

            String logLevel = System.getProperty(Constants.SYS_LOG_LEVEL);
            if (Utils.isNotEmpty(logLevel)) {
                Logger tmpLogger = Logger.getLogger(Constants.FORCE_PLUGIN_PREFIX);
                if (tmpLogger != null) {
                    Level derivedLogLevel = Level.toLevel(logLevel, Level.DEBUG);
                    System.out.println("Setting log level to " + derivedLogLevel.toString());
                    tmpLogger.setLevel(derivedLogLevel);
                }
            }
        } catch (Exception e) {
            String message = "Error while initializing log properties." + e.getMessage();
            IStatus status =
                    new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR, message, e);
            getLog().log(status);
            System.err.println("Error while initializing log properties: " + e.getMessage());
            throw new RuntimeException("Error while initializing log properties.", e);
        }

        if (Utils.isDebugMode()) {
            stopWatch.stop("ForceIdeCorePlugin.initialLogger");
        }
    }

    // initialize application container
    private static void initApplicationContext() {
        if (logger != null && logger.isDebugEnabled()) {
            stopWatch.start("ForceIdeCorePlugin.initApplicationContext");
        }

        try {
            ContainerDelegate.init();
        } catch (Exception e) {
            logger.error("Unable to load applicaiton context", e);
        } finally {
            if (logger != null && logger.isDebugEnabled()) {
                stopWatch.stop("ForceIdeCorePlugin.initApplicationContext");
            }
        }
    }

    private void initPreferences() {
        if (logger != null && logger.isDebugEnabled()) {
            stopWatch.start("ForceIdeCorePlugin.initPreferences");
        }

        preferenceManager = PreferenceManager.getInstance();

        if (logger != null && logger.isDebugEnabled()) {
            stopWatch.stop("ForceIdeCorePlugin.initPreferences");
        }
    }

    // U T I L S
    public static String getPluginId() {
        return getDefault() != null && getDefault().getBundle() != null ? getDefault().getBundle().getSymbolicName() : PLUGIN_ID;
    }

    public static ForceIdeCorePlugin getDefault() {
        return plugin;
    }

    public static URL getUrlEntry(String resource) {
        if (plugin == null || Utils.isEmpty(resource)) {
            return null;
        }
        return getDefault().getBundle().getEntry(resource);
    }

    public static URL getFullUrlResource(String resource) {
        if (plugin == null || Utils.isEmpty(resource)) {
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

    public static String getBundleVersion() {
        return getBundleVersion(false);
    }

    public static String getBundleVersion(boolean stripQualifer) {
        if (stripQualifer) {
            String ideVersion = getDefault().getBundle().getHeaders().get("Bundle-Version").toString();
            // FIXME: use matches to test for 3-dot pattern
            if (Utils.isNotEmpty(ideVersion) && ideVersion.contains(".")) {
                ideVersion = ideVersion.substring(0, ideVersion.lastIndexOf("."));
            }

            return ideVersion;
        }
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

    public static String findFile(String file) throws IOException {
        URL url = FileLocator.toFileURL(FileLocator.find(getDefault().getBundle(), new Path(file), null));
        return url != null ? url.getPath().toString() : Constants.EMPTY_STRING;
    }

    // P R E F E R E N C E S
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

    // S T A T S
    public static void logStats() {
        logger.debug(PLUGIN_ID + " plugin profiling result:\n" + stopWatch.prettyPrint());
    }
}
