package com.salesforce.ide.core;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IStartup;
//import org.eclipse.swt.widgets.MessageBox;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.project.ForceProject;
import org.eclipse.core.resources.IResource;



public class WorkbenchShutdownListener implements  IWorkbenchListener,  IResourceChangeListener {

	
	public static WorkbenchShutdownListener installWorkbenchShutdownListener(){
		WorkbenchShutdownListener shutDownListener = new WorkbenchShutdownListener();
		PlatformUI.getWorkbench().addWorkbenchListener(shutDownListener);
		
		installWorkspaceChangeListener(shutDownListener);		
		return shutDownListener;
	}
	/**
	 * @param shutDownListener
	 */
	private static void installWorkspaceChangeListener(WorkbenchShutdownListener shutDownListener) {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(shutDownListener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.POST_CHANGE);
	}
			
	@Override
	public boolean preShutdown(IWorkbench workbench, boolean forced){
		
        promptRemoveDebugProjects(workbench);	
		return true;
	}
	
	@Override 
	public void resourceChanged(IResourceChangeEvent rcEvent){
		if (rcEvent.getType() == IResourceChangeEvent.PRE_CLOSE){
			if (rcEvent.getResource().getType() == IResource.PROJECT){
				promptRemoveDebugProject(PlatformUI.getWorkbench(), (IProject)rcEvent.getResource());				
			}
		}else if (rcEvent.getType() == IResourceChangeEvent.POST_CHANGE){
			if (rcEvent.getDelta().getKind() == IResourceDelta.CHANGED){
				installWorkspaceChangeListener(this);
			}
		}
	}
	/**
	 * @param workbench
	 */
	private void promptRemoveDebugProjects(IWorkbench workbench) {
		List<IProject> projects = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProjects();
        for(IProject proj: projects){
				promptRemoveDebugProject(workbench, proj);
        }
	}
	
	/**
	 * @param workbench
	 * @param proj
	 * @throws CoreException
	 */
	private void promptRemoveDebugProject(IWorkbench workbench, IProject proj) {
		try {
			if (proj.hasNature(DefaultNature.NATURE_ID)){
				ForceProject fProj = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProject(proj);
				if (!fProj.getSessionId().isEmpty()){
			        // The file already exists; asks for confirmation
					org.eclipse.swt.widgets.MessageBox mb = new org.eclipse.swt.widgets.MessageBox(workbench.getDisplay().getActiveShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);

			        // We really should read this string from a resource bundle
			        mb.setMessage(fProj.getProject().getName() + " exists in the local file system. Do you wish to remove it?");

			        // If they click Yes, we're done and we drop out. If they click No, we redisplay the File Dialog
			        if(mb.open() == SWT.YES)
			        	fProj.getProject().delete(true,  true, new NullProgressMonitor());
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void postShutdown(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}


}
