package com.salesforce.ide.resourcebundles.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.salesforce.ide.resourcebundles.Activator;

/**
 * Ensures jobs run one after another per project.
 */
public class Scheduler {
    
    public static final Object JOB_FAMILY = new Object();
    
    /**
     * Schedule work in a WorkspaceJob.
     */
    public static void schedule(Changes changes, boolean userInitiated) {
        
        for (IProject project : changes.getFilesToUnzipByProject().keySet()) {
            schedule(project, new Operations.Unzip(changes.getFilesToUnzipByProject().get(project)), userInitiated);
        }
        for (IProject project : changes.getFoldersToZipByProject().keySet()) {
            schedule(project, new Operations.Zip(changes.getFoldersToZipByProject().get(project)), userInitiated);
        }
        for (IProject project : changes.getDeletedFilesToUnzipByProject().keySet()) {
            schedule(project, new Operations.DeleteUnzipped(changes.getDeletedFilesToUnzipByProject().get(project)), userInitiated);
        }
        for (IProject project : changes.getDeletedFoldersToZipByProject().keySet()) {
            schedule(project, new Operations.DeleteZipped(changes.getDeletedFoldersToZipByProject().get(project)), userInitiated);
        }
    }
    
    /**
     * Cancel all matching scheduled jobs.
     */
    public static void cancel() {
        
        Job.getJobManager().cancel(JOB_FAMILY);
    }
    
    private static void schedule(IProject project, final Operations.Runnable runnable, boolean userInitiated) {
        
        WorkspaceJob job = new WorkspaceJob(runnable.getNameForProgress()) {
            
            @Override public boolean belongsTo(Object family) {
                return family == JOB_FAMILY;
            }

            @Override public IStatus runInWorkspace(IProgressMonitor monitor) {
                runnable.run(monitor);
                return Status.OK_STATUS;
            }
        };
        
        // Ensure jobs are run sequentially on a project which in turn makes the JobChangeListener work properly
        job.setRule(project);
        job.addJobChangeListener(Activator.getDefault().getJobChangeListener());
        
        // Affects UI presentation and priority
        job.setUser(userInitiated);
        job.setPriority(userInitiated ? Job.INTERACTIVE : Job.SHORT);
        
        // Go
        job.schedule();
    }
}
