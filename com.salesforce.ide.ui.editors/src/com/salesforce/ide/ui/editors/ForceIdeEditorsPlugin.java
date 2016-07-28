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
package com.salesforce.ide.ui.editors;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.context.IContextHandler;
import com.salesforce.ide.core.internal.utils.StopWatch;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.editors.apex.util.ApexCodeColorProvider;
import com.salesforce.ide.ui.editors.internal.utils.EditorConstants;
import com.salesforce.ide.ui.internal.utils.UIConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class ForceIdeEditorsPlugin extends AbstractUIPlugin {

    private static Logger logger = Logger.getLogger(ForceIdeEditorsPlugin.class);

    private static final StopWatch stopWatch = new StopWatch(ForceIdeEditorsPlugin.class.getSimpleName());

    // The plug-in ID
    public static final String PLUGIN_ID = EditorConstants.PLUGIN_PREFIX;

    /** Key to store custom templates. */
    private static final String CUSTOM_TEMPLATES_KEY = UIConstants.PLUGIN_PREFIX + ".custom_templates"; //$NON-NLS-1$

    // The shared instance
    private static ForceIdeEditorsPlugin plugin = null;
    private static ApexCodeColorProvider apexCodeColorProvider = null;

    private ContextTypeRegistry apexContextTypeRegistry;
    private ContextTypeRegistry visualforceContextTypeRegistry;
    private ContextTypeRegistry lightningContextTypeRegistry;
    private TemplateStore apexTemplateStore;
    private TemplateStore visualforceTemplateStore;
    private TemplateStore lightningTemplateStore;

    // C O N S T R U C T O R
    public ForceIdeEditorsPlugin() {
        super();
        plugin = this;
    }

    // M E T H O D S
    public static StopWatch getStopWatch() {
        return stopWatch;
    }

    public static ApexCodeColorProvider getApexCodeColorProvider() {
        return apexCodeColorProvider;
    }

    // called upon plug-in activation
    @Override
    public void start(BundleContext context) throws Exception {
        stopWatch.start("ForceIdeEditorsPlugin.start"); //$NON-NLS-1$
        try {
            super.start(context);
            init();
        } catch (Exception e) {
            if (logger != null) {
                logger.error("Unable to initialize and start plugin '" //$NON-NLS-1$
                        + PLUGIN_ID + "'", e); //$NON-NLS-1$
            } else {
                String err = "Unable to initialize and start plugin '" //$NON-NLS-1$
                        + PLUGIN_ID + "': " + e.getMessage();
                System.err.println(err); //$NON-NLS-1$
                getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, err, e));
            }
            throw e;
        } finally {
            stopWatch.stop("ForceIdeEditorsPlugin.start"); //$NON-NLS-1$
        }
    }

    // called when the plug-in is stopped
    @Override
    public void stop(BundleContext context) throws Exception {
        if (logger != null && logger.isInfoEnabled()) {
            logger.info("Stopping " + PLUGIN_ID + " plugin"); //$NON-NLS-1$  //$NON-NLS-2$
        }

        if (logger != null && logger.isDebugEnabled()) {
            logStats();
        }

        if (apexCodeColorProvider != null) {
            apexCodeColorProvider.dispose();
        }

        super.stop(context);
    }

    // P L U G I N I N I T S

    private static void init() {
        initApplicationContext();
        initEditorResources();
        System.out.println("Initiated '" + PLUGIN_ID + "' plugin, version " //$NON-NLS-1$  //$NON-NLS-2$
                + getBundleVersion());
    }

    // initialize application container
    private static void initApplicationContext() {
        if (logger != null && logger.isDebugEnabled()) {
            stopWatch.start("ForceIdeEditorsPlugin.initApplicationContext"); //$NON-NLS-1$
        }

        try {
            IContextHandler contextHandler = ContainerDelegate.getInstance().getContextHandler();
            contextHandler.loadApplicationContext(new String[] { EditorConstants.APPLICATION_CONTEXT }, true);
        } catch (Exception e) {
            logger.error("Unable to load applicaiton context", e); //$NON-NLS-1$
            Utils.openError(e, "Application Context Loading Failed", "Unable to load applicaiton context");
        } finally {
            if (logger != null && logger.isDebugEnabled()) {
                stopWatch.stop("ForceIdeEditorsPlugin.initApplicationContext"); //$NON-NLS-1$
            }
        }
    }
    private static void initEditorResources() {
        apexCodeColorProvider = new ApexCodeColorProvider();
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

    // U T I L S
    public static String getPluginId() {
        if (getDefault() != null && getDefault().getBundle() != null) {
            return getDefault().getBundle().getSymbolicName();
        }

        return PLUGIN_ID;
    }

    public static ForceIdeEditorsPlugin getDefault() {
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
                logger.error("Unable to get full url for resource '" + resource //$NON-NLS-1$
                        + "'"); //$NON-NLS-1$
            }
        }
        return urlResource;
    }

    public static String getBundleVersion() {
        return getDefault().getBundle().getHeaders().get("Bundle-Version") //$NON-NLS-1$
                .toString();
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

    // S T A T S
    public static void logStats() {
        logger.debug(PLUGIN_ID + " plugin profiling result:\n" //$NON-NLS-1$
                + stopWatch.prettyPrint());
    }

    /*
     * Collection of listeners for Java element deltas
     */
    public static IElementChangedListener[] elementChangedListeners = new IElementChangedListener[5];
    public static int[] elementChangedListenerMasks = new int[5];
    public static int elementChangedListenerCount = 0;

    /**
     * Adds the given listener for changes to Java elements. Has no effect if an identical listener is already
     * registered.
     * 
     * This listener will only be notified during the POST_CHANGE resource change notification and any reconcile
     * operation (POST_RECONCILE). For finer control of the notification, use
     * <code>addElementChangedListener(IElementChangedListener,int)</code>, which allows to specify a different
     * eventMask.
     * 
     * @param listener
     *            the listener
     * @see ElementChangedEvent
     */
    public static void addElementChangedListener(IElementChangedListener listener) {
        addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE | ElementChangedEvent.POST_RECONCILE);
    }

    /*
     * Need to clone defensively the listener information, in case some listener
     * is reacting to some notification iteration by adding/changing/removing
     * any of the other (for example, if it deregisters itself).
     */
    public static synchronized void addElementChangedListener(IElementChangedListener listener, int eventMask) {
        for (int i = 0; i < elementChangedListenerCount; i++) {
            if (elementChangedListeners[i] == listener) {

                // only clone the masks, since we could be in the middle of
                // notifications and one listener decide to change
                // any event mask of another listeners (yet not notified).
                int cloneLength = elementChangedListenerMasks.length;
                System.arraycopy(elementChangedListenerMasks, 0, elementChangedListenerMasks = new int[cloneLength], 0,
                    cloneLength);
                elementChangedListenerMasks[i] |= eventMask; // could be
                // different
                return;
            }
        }
        // may need to grow, no need to clone, since iterators will have cached
        // original arrays and max boundary and we only add to the end.
        int length;
        if ((length = elementChangedListeners.length) == elementChangedListenerCount) {
            System.arraycopy(elementChangedListeners, 0, elementChangedListeners =
                    new IElementChangedListener[length * 2], 0, length);
            System.arraycopy(elementChangedListenerMasks, 0, elementChangedListenerMasks = new int[length * 2], 0,
                length);
        }
        elementChangedListeners[elementChangedListenerCount] = listener;
        elementChangedListenerMasks[elementChangedListenerCount] = eventMask;
        elementChangedListenerCount++;
    }

    /**
     * Removes the given element changed listener. Has no affect if an identical listener is not registered.
     * 
     * @param listener
     *            the listener
     */
    public static void removeElementChangedListener(IElementChangedListener listener) {
        for (int i = 0; i < elementChangedListenerCount; i++) {

            if (elementChangedListeners[i] == listener) {

                // need to clone defensively since we might be in the middle of
                // listener notifications (#fire)
                int length = elementChangedListeners.length;
                IElementChangedListener[] newListeners = new IElementChangedListener[length];
                System.arraycopy(elementChangedListeners, 0, newListeners, 0, i);
                int[] newMasks = new int[length];
                System.arraycopy(elementChangedListenerMasks, 0, newMasks, 0, i);

                // copy trailing listeners
                int trailingLength = elementChangedListenerCount - i - 1;
                if (trailingLength > 0) {
                    System.arraycopy(elementChangedListeners, i + 1, newListeners, i, trailingLength);
                    System.arraycopy(elementChangedListenerMasks, i + 1, newMasks, i, trailingLength);
                }

                // update manager listener state (#fire need to iterate over
                // original listeners through a local variable to hold onto
                // the original ones)
                elementChangedListeners = newListeners;
                elementChangedListenerMasks = newMasks;
                elementChangedListenerCount--;
                return;
            }
        }
    }

    public static void fire(final ElementChangedEvent extraEvent) {
        for (int i = 0; i < elementChangedListenerCount; i++) {
            if ((elementChangedListenerMasks[i] & extraEvent.getType()) != 0) {
                final IElementChangedListener listener = elementChangedListeners[i];

                // wrap callbacks with Safe runnable for subsequent listeners to
                // be called when some are causing grief
                SafeRunner.run(new ISafeRunnable() {
                    @Override
                    public void handleException(Throwable exception) {
                    // Util.log(exception, "Exception occurred in listener
                    // of Java element change notification"); //$NON-NLS-1$
                    }

                    @Override
                    public void run() throws Exception {
                        listener.elementChanged(extraEvent);
                    }
                });

            }
        }
    }

    /**
     * Returns the template store for the Apex editor templates.
     * 
     * @return the template store for the Apex editor templates
     */
    public TemplateStore getApexTemplateStore() {
        if (null == apexTemplateStore) {
            apexTemplateStore = new ContributionTemplateStore(
                getApexTemplateContextRegistry(),
                getPreferenceStore(),
                CUSTOM_TEMPLATES_KEY
            );
            try {
                apexTemplateStore.load();
            } catch (IOException e) {
                final String msg = "Unable to load Apex template store";
                final IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
                getLog().log(status);
            }
        }
        return apexTemplateStore;
    }

    /**
     * Returns the template store for the Visualforce editor templates.
     * 
     * @return the template store for the Visualforce editor templates
     */
    public TemplateStore getVisualforceTemplateStore() {
        if (null == visualforceTemplateStore) {
            visualforceTemplateStore = new ContributionTemplateStore(
                getVisualforceTemplateContextRegistry(),
                getPreferenceStore(),
                CUSTOM_TEMPLATES_KEY
            );
            try {
                visualforceTemplateStore.load();
            } catch (IOException e) {
                final String msg = "Unable to load Visualforce template store";
                final IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
                getLog().log(status);
            }
        }
        return visualforceTemplateStore;
    }

    public TemplateStore getLightningTemplateStore() {
        if (null == lightningTemplateStore) {
            lightningTemplateStore = new ContributionTemplateStore(
                getLightningTemplateContextRegistry(),
                getPreferenceStore(),
                CUSTOM_TEMPLATES_KEY
            );
            try {
                lightningTemplateStore.load();
            } catch (IOException e) {
                final String msg = "Unable to load Lightning template store";
                final IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
                getLog().log(status);
            }
        }
        return lightningTemplateStore;
    }

    /**
     * Returns the template context type registry for the Apex editor.
     * 
     * @return the template context type registry for the Apex editor
     */
    public ContextTypeRegistry getApexTemplateContextRegistry() {
        if (null == apexContextTypeRegistry) {
            apexContextTypeRegistry = new ContributionContextTypeRegistry("com.salesforce.ide.ui.editors.templates.apexContextTypes");
        }
        return apexContextTypeRegistry;
    }

    /**
     * Returns the template context type registry for the Visualforce editor.
     * 
     * @return the template context type registry for the Visualforce editor
     */
    public ContextTypeRegistry getVisualforceTemplateContextRegistry() {
        if (null == visualforceContextTypeRegistry) {
            visualforceContextTypeRegistry = new ContributionContextTypeRegistry("com.salesforce.ide.ui.editors.templates.visualforceContextTypes");
        }
        return visualforceContextTypeRegistry;
    }

    public ContextTypeRegistry getLightningTemplateContextRegistry() {
        if (null == lightningContextTypeRegistry) {
            lightningContextTypeRegistry = new ContributionContextTypeRegistry("com.salesforce.ide.ui.editors.templates.lightningContextTypes");
        }
        return lightningContextTypeRegistry;
    }

}
