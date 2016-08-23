package com.salesforce.ide.core;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IStartup;
//import org.eclipse.swt.widgets.MessageBox;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.project.ForceProject;



public class WorkbenchShutdownListener implements /*IStartup,*/ IWorkbenchListener {

	
	public static WorkbenchShutdownListener installWorkbenchShutdownListener(){
		WorkbenchShutdownListener shutDownListener = new WorkbenchShutdownListener();
		PlatformUI.getWorkbench().addWorkbenchListener(shutDownListener);
		
		return shutDownListener;
	}
	WorkbenchShutdownListener(){
			
	}
		
		@Override
		public boolean preShutdown(IWorkbench workbench, boolean forced){
			
	        List<IProject> projects = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProjects();
	        for(IProject proj: projects){
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
			return true;
		}

	@Override
	public void postShutdown(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}


}
