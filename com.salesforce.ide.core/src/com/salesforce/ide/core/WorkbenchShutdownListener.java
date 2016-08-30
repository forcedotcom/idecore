/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.core;

import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.project.ForceProject;
import org.eclipse.core.resources.IResource;


/**
 * Listener class to be notified when the Force,.comIDE is closed or a workspace is changed to prompt user to delete in potential 
 * ISV Debug projects in the current workspace
 * @author dbaker
 *
 */
public class WorkbenchShutdownListener implements  IWorkbenchListener,  IResourceChangeListener {

	/**
	 * Utility method to install listener for Workspace change and workbench shutdown
	 * @return
	 */
	public static WorkbenchShutdownListener installWorkbenchShutdownListener(){
		WorkbenchShutdownListener shutDownListener = new WorkbenchShutdownListener();
		PlatformUI.getWorkbench().addWorkbenchListener(shutDownListener);
		
		installWorkspaceChangeListener(shutDownListener);		
		return shutDownListener;
	}
	
	/**
	 * Utility method to be called on startup to install listener to current workspace
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
	public void postShutdown(IWorkbench workbench){		
	}

	/**
	 * Called when changes are made to workspace
	 */
	@Override 
	public void resourceChanged(IResourceChangeEvent rcEvent){
		// If project is being removed from workspace prompt to delete (
		if (rcEvent.getType() == IResourceChangeEvent.PRE_CLOSE){
			if (rcEvent.getResource().getType() == IResource.PROJECT){
				promptRemoveIfIsvDebugProject(PlatformUI.getWorkbench(), (IProject)rcEvent.getResource());				
			}
	    // Install listener in new workspace
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
				promptRemoveIfIsvDebugProject(workbench, proj);
        }
	}
	
	/**
	 * @param workbench
	 * @param proj
	 * @throws CoreException
	 */
	private void promptRemoveIfIsvDebugProject(IWorkbench workbench, IProject proj) {
		try {
			if (proj.hasNature(DefaultNature.NATURE_ID)){
				ForceProject fProj = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProject(proj);
				if (!fProj.getSessionId().isEmpty()){
					MessageDialog md = new MessageDialog(workbench.getDisplay().getActiveShell(), "Delete subscriber's code?", null, 
							"The project <"+ fProj.getProject().getName() + "> contains a subscriber's code. Be sure to delete it when you've finished debugging. Do you want to delete it now?", 
							MessageDialog.WARNING, 
							new String[] {"I'll Delete Later", "Delete Now"}, 1);

					// If user selected "Delete Now"
					if(md.open() == 1)
			        	fProj.getProject().delete(true,  true, new NullProgressMonitor());
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
