package com.salesforce.ide.resourcebundles.internal;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

import com.salesforce.ide.resourcebundles.Activator;

/**
 * Detect changes to the static resource and resource bundle folders and run a job to process those changes.
 * To avoid infinite update loops, this listener does nothing if called between "running" and "done" calls
 * (its an IJobChangeListener too) on the same thread.
 */
public class ResourceChangeListener implements IResourceChangeListener, IJobChangeListener {
    
    // Multiple jobs may be running at once so keep the flag tied to the thread
    private ThreadLocal<Boolean> enabled = new ThreadLocal<Boolean>();
    
    public ResourceChangeListener() {
        enabled.set(true);
    }

    /**
     * Entry point.
     */
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        
        // Avoid infinite loops where files -> folders -> files -> folders and so on
        if (enabled.get() != null && !enabled.get()) {
            return;
        }
        
        try {
            if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
                
                // Synchronous part: do as quickly as possible (so may be files found that eventually won't be processed)
                final ResourceTester.ResourceDeltaVisitor v = new ResourceTester.ResourceDeltaVisitor();
                event.getDelta().accept(v);
                
                // Asynchronous part
                Scheduler.schedule(v.changes, false);
            }
        } catch (Exception e) {
            Activator.log(e);
        }
    }
    
    /**
     * Disable this listener once a job has started.
     */
    @Override
    public void running(IJobChangeEvent event) {
        enabled.set(false);
    }
    
    /**
     * Enable this listener once a job has finished.
     */
    @Override
    public void done(IJobChangeEvent event) {
        enabled.set(true);
    }
    
    @Override
    public void aboutToRun(IJobChangeEvent event) {
    }
    
    @Override
    public void awake(IJobChangeEvent event) {
    }

    @Override
    public void scheduled(IJobChangeEvent event) {
    }

    @Override
    public void sleeping(IJobChangeEvent event) { 
    }
}
