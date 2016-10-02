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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.project.ForceProject;


/**
 * Listener class to be notified when the Force.comIDE is closed or a workspace
 * is changed to prompt user to delete in potential ISV Debug projects in the
 * current workspace. <br/><br/>
 * 
 * It checks for the existence of session ID in the project which is only
 * available when the project is created using drag-n-drop (which is only used
 * for ISV Debugger). <br/><br/>
 * 
 * The difference between this listener and DebuggerWorkbenchListener is that
 * the former gets registered whenever the IDE is opened/closed or workspace was changed, 
 * so this will remind the user plenty of times to remove the subscriber code. The
 * latter is only registered after ForceProjectRefreshJob runs, so something like re-opening the
 * workbench won't trigger the listener.
 * 
 * @author dbaker
 */
public class WorkbenchShutdownListener implements IWorkbenchListener {

	/**
	 * Utility method to install listener for Workspace change and workbench shutdown
	 */
	public static WorkbenchShutdownListener installWorkbenchShutdownListener() {
		WorkbenchShutdownListener shutDownListener = new WorkbenchShutdownListener();
		PlatformUI.getWorkbench().addWorkbenchListener(shutDownListener);
			
		return shutDownListener;
	}
	
	@Override
	public boolean preShutdown(IWorkbench workbench, boolean forced) {
        promptRemoveDebugProjects(workbench);	
		return true;
	}
	
	@Override
	public void postShutdown(IWorkbench workbench){		
	}
	
	private void promptRemoveDebugProjects(IWorkbench workbench) {
		List<IProject> projects = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProjects();
		for (IProject proj : projects) {
			promptRemoveIfIsvDebugProject(workbench, proj);
		}
	}
	
	
	private void promptRemoveIfIsvDebugProject(IWorkbench workbench, IProject proj) {
		try {
			if (proj.hasNature(DefaultNature.NATURE_ID)) {
				ForceProject fProj = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProject(proj);
				if (!fProj.getSessionId().isEmpty()) {
					String message = NLS.bind(Messages.RemoveSubscriberCode_Question, fProj.getProject().getName());
					MessageDialog md = new MessageDialog(workbench.getDisplay().getActiveShell(),
							Messages.RemoveSubscriberCode_Title, null, message, MessageDialog.WARNING,
							new String[] { Messages.RemoveSubscriberCode_Later, Messages.RemoveSubscriberCode_Now }, 1);

					// If user selected "Delete Now"
					if (md.open() == 1) {
						fProj.getProject().delete(true, true, new NullProgressMonitor());
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
