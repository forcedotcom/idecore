package com.salesforce.ide.resourcebundles;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.salesforce.ide.resourcebundles.internal.Changes;
import com.salesforce.ide.resourcebundles.internal.ResourceTester;
import com.salesforce.ide.resourcebundles.internal.Scheduler;

/**
 * Manually instigated action to un-ZIP static resource(s) to resource bundle folder(s).
 */
public class UnzipToResourceBundleAction implements IWorkbenchWindowActionDelegate {

    private ISelection selection;
    
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }
    
    @Override
    public void run(IAction action) {
        try {
            List<IFile> candidates = selectedCandidates();
            if (candidates.size() > 0) {
                Changes c = new Changes();
                for (IFile candidate : candidates) {
                    c.addFileToUnzip(candidate);
                }
                Scheduler.schedule(c, true);
            }
        } catch (Exception e) {
            Activator.log(e);
        }
    }
    
    private List<IFile> selectedCandidates() throws Exception {
        if (selection instanceof IStructuredSelection) {
            return ResourceTester.getFilesToUnzip((IStructuredSelection) selection);
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    public void dispose() {
    }

    @Override
    public void init(IWorkbenchWindow window) {
    }
}