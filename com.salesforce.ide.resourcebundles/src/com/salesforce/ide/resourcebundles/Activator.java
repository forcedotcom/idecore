package com.salesforce.ide.resourcebundles;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.salesforce.ide.resourcebundles.internal.ResourceChangeListener;
import com.salesforce.ide.resourcebundles.internal.Scheduler;

/**
 * Controls the life cycle of the static resources plug-in.
 */
public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "com.salesforce.ide.resourcebundles"; //$NON-NLS-1$
    
    private static Activator plugin;
    
    // Could be set from a property page
    private boolean synchronizeAutomatically = true;
    
    private ResourceChangeListener resourceChangeListener = new ResourceChangeListener();
    
    public Activator() {
    }

    public void start(BundleContext context) throws Exception {
        
        // Default
        super.start(context);
        plugin = this;
        
        // Distinct to this plug-in
        if (synchronizeAutomatically) {
            ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
        }
    }

    public void stop(BundleContext context) throws Exception {
        
        // Distinct to this plug-in
        Scheduler.cancel();
        if (synchronizeAutomatically) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
        }
        
        // Default
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }
       
    /**
     * Any resource updating jobs created should add this listener.
     */
    public IJobChangeListener getJobChangeListener() {
        return resourceChangeListener;
    }
    
    /**
     * Log unexpected exceptions.
     */
    public static void log(Throwable t) {
        IStatus status= new Status(IStatus.ERROR, PLUGIN_ID, "Unexpected exception", t); //$NON-NLS-1$
        getDefault().getLog().log(status);
    }
}