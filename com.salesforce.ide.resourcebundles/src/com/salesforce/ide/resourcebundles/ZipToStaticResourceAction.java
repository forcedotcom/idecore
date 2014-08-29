package com.salesforce.ide.resourcebundles;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.salesforce.ide.resourcebundles.internal.Changes;
import com.salesforce.ide.resourcebundles.internal.ResourceTester;
import com.salesforce.ide.resourcebundles.internal.Scheduler;

/**
 * Manually instigated action to ZIP resource bundle folder(s) of files to static resource(s).
 */
public class ZipToStaticResourceAction implements IWorkbenchWindowActionDelegate {
    
    private ISelection selection;
    
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }
    
    @Override
    public void run(IAction action) {
        try {
            List<IFolder> candidates = selectedCandidates();
            if (candidates.size() > 0) {
                Changes c = new Changes();
                for (IFolder candidate : candidates) {
                    c.addFolderToZip(candidate);
                }
                Scheduler.schedule(c, true);
            }
        } catch (Exception e) {
            Activator.log(e);
        }
    }
    
    private List<IFolder> selectedCandidates() throws Exception {
        if (selection instanceof IStructuredSelection) {
            return ResourceTester.getFoldersToZip((IStructuredSelection) selection);
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
