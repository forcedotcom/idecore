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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.annotations.VisibleForTesting;
import com.salesforce.ide.apex.internal.core.ApexTestsUtils;
import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.services.hooks.DebugListener;
import com.sforce.soap.tooling.ApexLogLevel;
import com.sforce.soap.tooling.LogCategory;

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
				if (!confirmAsyncTestRunWhileDebugging()) {
					// If they want to abort, re-open the launch configuration
					reopenLaunchConfig(configuration, mode);
					return false;
				}
			}
			
			boolean shouldEnableLogging = shouldEnableLogging(configuration);
			if (shouldEnableLogging) {
				// Check for existing Trace Flags that would hinder with
				// the new one
				boolean hasExistingTraceFlag = runTestsView.hasExistingTraceFlag(project);
				if (hasExistingTraceFlag) {
					if (confirmExistingTraceFlag()) {
						// If they want to continue, disable logging and proceed
						disableLogging(configuration);
					} else {
						// If they want to abort, re-open the launch configuration
						reopenLaunchConfig(configuration, mode);
						return false;
					}
				}
			}
		}
		
		return secondPhasePreLaunchCheck(configuration, mode, monitor);
	}
	
	@VisibleForTesting
	public boolean secondPhasePreLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return super.preLaunchCheck(configuration, mode, monitor);
	}
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			// Get the Apex Test Results view
			RunTestsView runTestsView = getRunTestView();
			
			if (runTestsView != null) {
				// The tests array and number of total tests were calculated in
				// RunTestsTab.java and saved as string & int respectively in
				// the launch config so we can grab them from this launch config
				// delegate.
				IProject project = materializeForceProject(configuration);
				String tests = getTestsArray(configuration);
				int totalTests = getTotalTests(configuration);
				boolean isAsync = getTestMode(configuration);
				boolean isDebugging = isProjectDebugging(project);
				boolean shouldEnableLogging = shouldEnableLogging(configuration);
				@SuppressWarnings("unchecked")
				Map<LogCategory, ApexLogLevel> logLevels = (Map<LogCategory, ApexLogLevel>) (shouldEnableLogging ? getLogLevels(configuration)
						: Collections.emptyMap());

				// Get the test files in the selected project
				Map<IResource, List<String>> testResources = findTestClasses(project);

				// Run the tests and update UI
				runTestsView.runTests(project, testResources, tests,
						totalTests, isAsync, isDebugging, shouldEnableLogging,
						logLevels, monitor);
			}
		} finally {
			removeLaunch(launch);
		}
	}
	
	@VisibleForTesting
	public void throwErrorMsg(String msg) throws CoreException {
		if (Utils.isNotEmpty(msg)) {
			throw new CoreException(
					new Status(IStatus.ERROR, ForceIdeCorePlugin.PLUGIN_ID, 0, msg, null));
		}
	}
	
	@VisibleForTesting
	public boolean isProjectDebugging(IProject project) {
		return DebugListener.isDebugging(project);
	}
	
	@VisibleForTesting
	public void checkMode(String mode) throws CoreException {
		if (!mode.equals(ILaunchManager.RUN_MODE)) {
			throwErrorMsg(Messages.RunTestsLaunchConfigurationDelegate_CannotLaunchDebugModeErrorMessage);
		}
	}
	
	@VisibleForTesting
	public IProject materializeForceProject(ILaunchConfiguration configuration) throws CoreException {
		String forceProjectName = getProjectName(configuration);
		IProject project = getProjectFromName(forceProjectName);

        if (Utils.isEmpty(project)) {
        	throwErrorMsg(Messages.RunTestsLaunchConfigurationDelegate_CannotLaunchInvalidForceProject);
        }
        
        return project;
    }
	
	@VisibleForTesting
	public String getProjectName(ILaunchConfiguration configuration) throws CoreException {
        return configuration.getAttribute(RunTestsConstants.ATTR_PROJECT_NAME, "");
    }
	
	@VisibleForTesting
	public IProject getProjectFromName(String name) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }
	
	@VisibleForTesting
	public String getTestsArray(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(RunTestsConstants.ATTR_TESTS_ARRAY, "");
	}
	
	@VisibleForTesting
	public int getTotalTests(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(RunTestsConstants.ATTR_TESTS_TOTAL, 0);
	}
	
	@VisibleForTesting
	public boolean getTestMode(ILaunchConfiguration configuration) throws CoreException {
		// Async = true, sync = false
		return configuration.getAttribute(RunTestsConstants.ATTR_TEST_MODE, true);
	}
	
	@VisibleForTesting
	public boolean shouldEnableLogging(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(RunTestsConstants.ATTR_ENABLE_LOGGING, false);
	}
	
	@VisibleForTesting
	public void disableLogging(ILaunchConfiguration configuration) throws CoreException {
		ILaunchConfigurationWorkingCopy realConfig = configuration.getWorkingCopy();
		realConfig.setAttribute(RunTestsConstants.ATTR_ENABLE_LOGGING, false);
		realConfig.doSave();
	}
	
	@VisibleForTesting
	public Map<LogCategory, ApexLogLevel> getLogLevels(ILaunchConfiguration configuration) throws CoreException {
		Map<LogCategory, ApexLogLevel> finalLogLevels = new LinkedHashMap<LogCategory, ApexLogLevel>();
		
		if (Utils.isEmpty(configuration)) {
			return finalLogLevels;
		}
		
		@SuppressWarnings("unchecked")
		Map<String, String> savedLogLevels = (Map<String, String>) configuration.getAttribute(RunTestsConstants.ATTR_LOG_LEVELS, Collections.emptyMap());
		
		for (String categoryName : savedLogLevels.keySet()) {
			ApexLogLevel logLevel = ApexLogLevel.valueOf(savedLogLevels.get(categoryName));
			
			if (categoryName.equals(Messages.RunTestsTab_LogCategoryDatabase)) {
				finalLogLevels.put(LogCategory.Db, logLevel);
			} else if (categoryName.equals(Messages.RunTestsTab_LogCategoryWorkflow)) {
				finalLogLevels.put(LogCategory.Workflow, logLevel);
			} else if (categoryName.equals(Messages.RunTestsTab_LogCategoryValidation)) {
				finalLogLevels.put(LogCategory.Validation, logLevel);
			} else if (categoryName.equals(Messages.RunTestsTab_LogCategoryCallout)) {
				finalLogLevels.put(LogCategory.Callout, logLevel);
			} else if (categoryName.equals(Messages.RunTestsTab_LogCategoryApexCode)) {
				finalLogLevels.put(LogCategory.Apex_code, logLevel);
			} else if (categoryName.equals(Messages.RunTestsTab_LogCategoryApexProfiling)) {
				finalLogLevels.put(LogCategory.Apex_profiling, logLevel);
			} else if (categoryName.equals(Messages.RunTestsTab_LogCategoryVisualforce)) {
				finalLogLevels.put(LogCategory.Visualforce, logLevel);
			} else if (categoryName.equals(Messages.RunTestsTab_LogCategorySystem)) {
				finalLogLevels.put(LogCategory.System, logLevel);
			}
		}
		
		return finalLogLevels;
	}
	
	@VisibleForTesting
	public RunTestsView getRunTestView() {
		return RunTestsView.getInstance();
	}
	
	@VisibleForTesting
	public Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}
	
	@VisibleForTesting
	public Map<IResource, List<String>> findTestClasses(IProject project) {
		return ApexTestsUtils.INSTANCE.findTestClassesInProject(project);
	}
	
	@VisibleForTesting
	public void removeLaunch(ILaunch launch) {
		DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
	}
	
	/**
     * Pop up a confirmation dialog regarding asynchronous test run while debugging
     * @return False if abort, true if continue
     */
	@VisibleForTesting
	public boolean confirmAsyncTestRunWhileDebugging() {
    	Display display = getDisplay();
    	final AtomicInteger choice = new AtomicInteger(0);
    	
    	display.syncExec(new Runnable() {
			@Override
			public void run() {
				choice.set(DialogUtils.getInstance().abortContinueMessage(
						Messages.RunTestsLaunchConfigurationDelegate_ConfirmDialogTitle, 
						Messages.RunTestsLaunchConfigurationDelegate_CannotLaunchAsyncWhileDebugging, 
						MessageDialog.WARNING));
			}
    	});
    	
    	return choice.get() == 1;
    }
    
    /**
     * Inform user there is an existing Trace Flag.
     * @return False if abort, true if continue
     */
	@VisibleForTesting
	public boolean confirmExistingTraceFlag() {
    	Display display = getDisplay();
    	final AtomicInteger choice = new AtomicInteger(0);
    	
    	display.syncExec(new Runnable() {
			@Override
			public void run() {
				choice.set(DialogUtils.getInstance().abortContinueMessage(
						Messages.RunTestsLaunchConfigurationDelegate_ConfirmDialogTitle, 
						Messages.RunTestsLaunchConfigurationDelegate_ExistingTraceFlag, 
						MessageDialog.INFORMATION));
			}
    	});
    	
    	return choice.get() == 1;
    }
	
    /**
     * Re-open config window with the specified launch config
     * @param configuration
     * @param mode
     */
    @VisibleForTesting
	public void reopenLaunchConfig(final ILaunchConfiguration configuration, final String mode) {
		if (Utils.isNotEmpty(configuration) && Utils.isNotEmpty(mode)) {
			Display display = getDisplay();
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchWindow aww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					ILaunchGroup launchGroup = DebugUITools.getLaunchGroup(configuration, mode);
					DebugUITools.openLaunchConfigurationDialog(aww.getShell(), configuration, launchGroup.getIdentifier(), null);
				}
			});
		}
	}
}
