/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/

package com.salesforce.ide.ui.views.runtest;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.apex.internal.core.ApexTestsUtils;
import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.services.hooks.DebugListener;

/**
 * A launch configuration delegate for Apex Test. This gets called when executing
 * a launch config for Apex Test.
 * 
 * @see RunTestsLaunchConfigurationTab.java
 * @author jwidjaja
 *
 */
public class RunTestsLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@Override
	public boolean preLaunchCheck(final ILaunchConfiguration configuration, final String mode, IProgressMonitor monitor) throws CoreException {
		// Only supported in run mode
		checkMode(mode);
		
		RunTestsView runTestsView = getRunTestView();
		if (runTestsView != null) {
			// Only allow one run at a time
			if (!runTestsView.canRun()) {
				throwErrorMsg(Messages.RunTestsLaunchConfigurationDelegate_CannotLaunchAnotherConfig);
			}
			
			IProject project = materializeForceProject(configuration);
			// Check if user has an Apex Debugging session and wants to run tests asynchronously
			boolean isAsync = getTestMode(configuration);
			boolean isDebugging = isProjectDebugging(project);
			if (isDebugging && isAsync) {
				// If yes, inform user that asynchronous Apex tests are not debuggable.
				if (!runTestsView.confirmAsyncTestRunWhileDebugging()) {
					// If they want to abort, re-open the launch configuration
					Display display = getDisplay();
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							IWorkbenchWindow aww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							ILaunchGroup launchGroup = DebugUITools.getLaunchGroup(configuration, mode);
							DebugUITools.openLaunchConfigurationDialog(aww.getShell(), configuration, launchGroup.getIdentifier(), null);
						}
					});
					return false;
				}
			}
		}
		
		return secondPhasePreLaunchCheck(configuration, mode, monitor);
	}
	
	protected boolean secondPhasePreLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return super.preLaunchCheck(configuration, mode, monitor);
	}
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			// Get the Apex Test Results view
			RunTestsView runTestsView = getRunTestView();

			// The tests array and number of total tests were calculated in
			// RunTestsTab.java and saved as string & int respectively in the 
			// launch config so we can grab them from this launch config delegate.
			IProject project = materializeForceProject(configuration);
			String tests = getTestsArray(configuration);
			int totalTests = getTotalTests(configuration);
			boolean isAsync = getTestMode(configuration);
			boolean isDebugging = isProjectDebugging(project);

			// Get the test files in the selected project
			Map<IResource, List<String>> testResources = findTestClasses(project);
			if (runTestsView != null) {
				// Run the tests and update UI
				runTestsView.runTests(project, testResources, tests,
						totalTests, isAsync, isDebugging, monitor);
			}
		} finally {
			removeLaunch(launch);
		}
	}
	
	protected void throwErrorMsg(String msg) throws CoreException {
		if (msg != null && !msg.isEmpty()) {
			throw new CoreException(
					new Status(IStatus.ERROR, ForceIdeCorePlugin.PLUGIN_ID, 0, msg, null));
		}
	}
	
	protected boolean isProjectDebugging(IProject project) {
		return DebugListener.isDebugging(project);
	}
	
	protected void checkMode(String mode) throws CoreException {
		if (!mode.equals(ILaunchManager.RUN_MODE)) {
			throwErrorMsg(Messages.RunTestsLaunchConfigurationDelegate_CannotLaunchDebugModeErrorMessage);
		}
	}
	
	protected IProject materializeForceProject(ILaunchConfiguration configuration) throws CoreException {
		String forceProjectName = getProjectName(configuration);
		IProject project = getProjectFromName(forceProjectName);

        if (project == null) {
        	throwErrorMsg(Messages.LaunchConfigurationDelegate_CannotLaunchInvalidForceProject);
        }
        
        return project;
    }
	
	protected String getProjectName(ILaunchConfiguration configuration) throws CoreException {
        return configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, "");
    }

	protected IProject getProjectFromName(String name) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }
	
	protected String getTestsArray(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TESTS_ARRAY, "");
	}
	
	protected int getTotalTests(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TESTS_TOTAL, 0);
	}
	
	protected boolean getTestMode(ILaunchConfiguration configuration) throws CoreException {
		// Async = true, sync = false
		return configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, true);
	}
	
	protected RunTestsView getRunTestView() {
		return RunTestsView.getInstance();
	}
	
	protected Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}
	
	protected Map<IResource, List<String>> findTestClasses(IProject project) {
		return ApexTestsUtils.INSTANCE.findTestClassesInProject(project);
	}
	
	protected void removeLaunch(ILaunch launch) {
		DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
	}
}
