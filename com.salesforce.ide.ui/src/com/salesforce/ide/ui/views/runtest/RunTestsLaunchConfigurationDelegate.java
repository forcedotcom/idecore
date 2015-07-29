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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import com.salesforce.ide.core.services.hooks.DebugListenerBroadcaster;

/**
 * A launch configuration delegate for Apex Test. This gets called when executing
 * a launch config for Apex Test.
 * 
 * @see RunTestsTab.java
 * @author jwidjaja
 *
 */
public class RunTestsLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@Override
	public boolean preLaunchCheck(final ILaunchConfiguration configuration, final String mode, IProgressMonitor monitor) throws CoreException {
		// Only supported in run mode
		checkMode(mode);
		
		RunTestView runTestsView = RunTestView.getInstance();
		if (runTestsView != null) {
			// Only allow one run at a time
			if (!runTestsView.canRun()) {
				throw new CoreException(new Status(IStatus.ERROR, ForceIdeCorePlugin.PLUGIN_ID, 0, 
						Messages.RunTestsLaunchConfigurationDelegate_CannotLaunchAnotherConfig, null));
			}
			
			IProject project = materializeForceProject(configuration);
			// Check if user has an Apex Debugging session and wants to run tests asynchronously
			boolean isAsync = getTestMode(configuration);
			boolean isDebugging = DebugListenerBroadcaster.isDebugging(project);
			if (isDebugging && isAsync) {
				// If yes, inform user that asynchronous Apex tests are not debuggable.
				if (!runTestsView.confirmAsyncTestRunWhileDebugging()) {
					// If they want to abort, re-open the launch configuration
					Display display = PlatformUI.getWorkbench().getDisplay();
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							IWorkbenchWindow aww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							ILaunchGroup launchGroup=DebugUITools.getLaunchGroup(configuration, mode);
							DebugUITools.openLaunchConfigurationDialog(aww.getShell(), configuration, launchGroup.getIdentifier(), null);
						}
					});
					return false;
				}
			}
		}
		
		return super.preLaunchCheck(configuration, mode, monitor);
	}
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// Get the Apex Test Runner view
		RunTestView runTestsView = RunTestView.getInstance();
		
		// The tests array and number of total tests were calculated in RunTestsTab.java
		// and saved as string & int respectively in the launch config so we can grab
		// them from this launch config delegate.
		IProject project = materializeForceProject(configuration);
		String tests = getTestsArray(configuration);
		int totalTests = getTotalTests(configuration);
		boolean isAsync = getTestMode(configuration);
		
		// Get the test files in the selected project
		Map<String, IResource> testResources = ApexTestsUtils.INSTANCE.findTestClassesInProject(project);
		if (runTestsView != null) {
			// Run the tests and update UI
			runTestsView.runTests(project, testResources, tests, totalTests,
					isAsync, monitor);
		}
	}
	
	private void checkMode(String mode) throws CoreException {
		if (!mode.equals(ILaunchManager.RUN_MODE)) {
			throw new CoreException(new Status(IStatus.ERROR, ForceIdeCorePlugin.PLUGIN_ID, 0, 
					Messages.RunTestsLaunchConfigurationDelegate_CannotLaunchDebugModeErrorMessage, null));
		}
	}
	
	private IProject materializeForceProject(ILaunchConfiguration configuration) throws CoreException {
		IProject project = retrieveProject(configuration);

        if (project == null) {
            throw new CoreException(new Status(IStatus.ERROR, ForceIdeCorePlugin.PLUGIN_ID, 0, 
            		Messages.LaunchConfigurationDelegate_CannotLaunchInvalidForceProject, null));
        } else {
            return project;
        }
    }
	
	private IProject retrieveProject(ILaunchConfiguration configuration) throws CoreException {
		String forceProjectName = getProjectName(configuration);
        IProject project = getProjectFromName(forceProjectName);

        return project;
    }
	
	private String getProjectName(ILaunchConfiguration configuration) throws CoreException {
        return configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, "");
    }

	private IProject getProjectFromName(String name) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }
	
	private String getTestsArray(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TESTS_ARRAY, "");
	}
	
	private int getTotalTests(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TESTS_TOTAL, 0);
	}
	
	private boolean getTestMode(ILaunchConfiguration configuration) throws CoreException {
		// Async = true, sync = false
		return configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, true);
	}
}
