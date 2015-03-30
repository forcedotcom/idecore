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
package com.salesforce.ide.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.context.IContextHandler;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.StopWatch;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.SharedTextColors;
import com.salesforce.ide.ui.internal.startup.ForceStartup;
import com.salesforce.ide.ui.internal.utils.UIConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class ForceIdeUIPlugin extends AbstractUIPlugin {

    private static Logger logger = Logger.getLogger(ForceIdeUIPlugin.class);

    private static final StopWatch stopWatch = new StopWatch(ForceIdeUIPlugin.class.getSimpleName());

    // The plug-in ID
    public static final String PLUGIN_ID = UIConstants.PLUGIN_PREFIX;

    // The shared instance
    private static ForceIdeUIPlugin plugin;
    private static ISharedTextColors sharedTextColors = null;

    // C O N S T R U C T O R
    public ForceIdeUIPlugin() {
        super();
        plugin = this;
    }

    // M E T H O D S
    public static StopWatch getStopWatch() {
        return stopWatch;
    }

    // called upon plug-in activation
    @Override
    public void start(BundleContext context) throws Exception {
        if (Utils.isDebugMode()) {
            stopWatch.start("ForceIdeUIPlugin.start");
        }

        try {
            super.start(context);
            init();
        } catch (Exception e) {
            if (logger != null) {
                logger.error("Unable to initialize and start ForceIdeUIPlugin '" + PLUGIN_ID + "'", e);
            } else {
                System.err.println("Unable to initialize and start ForceIdeUIPlugin '" + PLUGIN_ID + "': "
                        + e.getMessage());
                e.printStackTrace();
            }
            throw e;
        } finally {
            if (Utils.isDebugMode()) {
                stopWatch.stop("ForceIdeUIPlugin.start");
            }
        }
    }

    // called when the plug-in is stopped
    @Override
    public void stop(BundleContext context) throws Exception {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info("Stopping " + PLUGIN_ID + " ForceIdeUIPlugin");
        }

        try {
            if (ForceImages.getImageRegistry() != null) {
                ForceImages.dispose();
            }

            if (sharedTextColors != null) {
                sharedTextColors.dispose();
            }

            if (logger != null && logger.isDebugEnabled()) {
                logStats();
            }
        } finally {
            ForceStartup.removePackageManifestChangeListener();
            super.stop(context);
        }
    }

    // P L U G I N I N I T S

    private static void init() {
        initApplicationContext();
        System.out.println("Initiated '" + PLUGIN_ID + "' ForceIdeUIPlugin, version " + getBundleVersion());
    }

    // initialize application container
    private static void initApplicationContext() {
        if (logger != null && logger.isDebugEnabled()) {
            stopWatch.start("ForceIdeUIPlugin.initApplicationContext");
        }

        try {
            IContextHandler contextHandler = ContainerDelegate.getInstance().getContextHandler();
            contextHandler.loadApplicationContext(new String[] { UIConstants.APPLICATION_CONTEXT }, true);
        } catch (Exception e) {
            logger.error("Unable to load applicaiton context", e);
        } finally {
            if (logger != null && logger.isDebugEnabled()) {
                stopWatch.stop("ForceIdeUIPlugin.initApplicationContext");
            }
        }
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(getPluginId(), path);
    }

    public static ISharedTextColors getSharedTextColors() {
        if (sharedTextColors == null) {
            sharedTextColors = new SharedTextColors();
        }
        return sharedTextColors;
    }

    // U T I L S
    public static String getPluginId() {
        return getDefault() != null && getDefault().getBundle() != null ? getDefault().getBundle().getSymbolicName() : PLUGIN_ID;
    }

    public static ForceIdeUIPlugin getDefault() {
        return plugin;
    }

    public static URL getUrlEntry(String resource) {
        if (plugin == null || Utils.isEmpty(resource)) {
            return null;
        }
        return getDefault().getBundle().getEntry(resource);
    }

    public static URL getFullUrlEntry(String resource) {
        URL urlResource = getUrlEntry(resource);
        if (urlResource != null) {
            try {
                urlResource = FileLocator.toFileURL(urlResource);
            } catch (IOException e) {
                logger.error("Unable to get full url for resource '" + resource + "'");
            }
        }
        return urlResource;
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
        return url != null ? url.getPath() : Constants.EMPTY_STRING;
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
