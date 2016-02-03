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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IProject;
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
				throwErrorMsg(Messages.LaunchDelegate_CannotLaunchAnotherConfig);
			} else {
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
				
				boolean shouldCreateTraceFlag = shouldCreateTraceFlag(configuration);
				boolean hasExistingTraceFlag = runTestsView.hasExistingTraceFlag(project);
				setExistingTraceFlag(configuration, hasExistingTraceFlag);
				
				if (shouldCreateTraceFlag && hasExistingTraceFlag) {
					// Check for existing Trace Flags that would hinder with
					// the new one
					if (!confirmExistingTraceFlag()) {
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
				IProject project = materializeForceProject(configuration);
				boolean shouldUseSuites = shouldUseSuites(configuration);
				String tests = getTests(configuration, shouldUseSuites);
				boolean isAsync = getTestMode(configuration);
				boolean isDebugging = isProjectDebugging(project);
				boolean shouldCreateTraceFlag = shouldCreateTraceFlag(configuration);
				boolean hasExistingTraceFlag = hasExistingTraceFlag(configuration);
				@SuppressWarnings("unchecked")
				Map<LogCategory, ApexLogLevel> logLevels = (Map<LogCategory, ApexLogLevel>) (shouldCreateTraceFlag ? getLogLevels(configuration)
						: Collections.emptyMap());

				// Run the tests and update UI
				runTestsView.runTests(project, tests, shouldUseSuites, isAsync, isDebugging,
						hasExistingTraceFlag, shouldCreateTraceFlag,
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
			throwErrorMsg(Messages.LaunchDelegate_CannotLaunchDebugModeErrorMessage);
		}
	}
	
	@VisibleForTesting
	public IProject materializeForceProject(ILaunchConfiguration configuration) throws CoreException {
		String forceProjectName = getProjectName(configuration);
		IProject project = getProjectFromName(forceProjectName);

        if (Utils.isEmpty(project)) {
        	throwErrorMsg(Messages.LaunchDelegate_CannotLaunchInvalidForceProject);
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
	public boolean shouldUseSuites(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(RunTestsConstants.ATTR_USE_SUITES, false);
	}
	
	@VisibleForTesting
	public String getTests(ILaunchConfiguration configuration, boolean shouldUseSuites) throws CoreException {
		if (shouldUseSuites) {
			// If user wants to use suites, get the suites JSON
			return configuration.getAttribute(RunTestsConstants.ATTR_SUITES, "");
		} else {
			// Otherwise, get the tests json
			return configuration.getAttribute(RunTestsConstants.ATTR_TESTS_ARRAY, "");
		}
	}
	
	@VisibleForTesting
	public boolean getTestMode(ILaunchConfiguration configuration) throws CoreException {
		// Async = true, sync = false
		return configuration.getAttribute(RunTestsConstants.ATTR_TEST_MODE, true);
	}
	
	@VisibleForTesting
	public boolean shouldCreateTraceFlag(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(RunTestsConstants.ATTR_ENABLE_LOGGING, false);
	}
	
	private boolean hasExistingTraceFlag(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(RunTestsConstants.ATTR_EXISTING_TF, false);
	}
	
	@VisibleForTesting
	public void setExistingTraceFlag(ILaunchConfiguration configuration, boolean existing) throws CoreException {
		ILaunchConfigurationWorkingCopy realConfig = configuration.getWorkingCopy();
		realConfig.setAttribute(RunTestsConstants.ATTR_EXISTING_TF, existing);
		realConfig.doSave();
	}
	
	@VisibleForTesting
	public Map<LogCategory, ApexLogLevel> getLogLevels(ILaunchConfiguration configuration) throws CoreException {
		Map<LogCategory, ApexLogLevel> finalLogLevels = new LinkedHashMap<LogCategory, ApexLogLevel>();
		
		if (Utils.isEmpty(configuration)) {
			return finalLogLevels;
		}
		
		Map<String, String> savedLogLevels = (Map<String, String>) configuration.getAttribute(RunTestsConstants.ATTR_LOG_LEVELS, Collections.emptyMap());
		
		for (String categoryName : savedLogLevels.keySet()) {
			ApexLogLevel logLevel = ApexLogLevel.valueOf(savedLogLevels.get(categoryName));
			
			if (categoryName.equals(Messages.Tab_LogCategoryDatabase)) {
				finalLogLevels.put(LogCategory.Db, logLevel);
			} else if (categoryName.equals(Messages.Tab_LogCategoryWorkflow)) {
				finalLogLevels.put(LogCategory.Workflow, logLevel);
			} else if (categoryName.equals(Messages.Tab_LogCategoryValidation)) {
				finalLogLevels.put(LogCategory.Validation, logLevel);
			} else if (categoryName.equals(Messages.Tab_LogCategoryCallout)) {
				finalLogLevels.put(LogCategory.Callout, logLevel);
			} else if (categoryName.equals(Messages.Tab_LogCategoryApexCode)) {
				finalLogLevels.put(LogCategory.Apex_code, logLevel);
			} else if (categoryName.equals(Messages.Tab_LogCategoryApexProfiling)) {
				finalLogLevels.put(LogCategory.Apex_profiling, logLevel);
			} else if (categoryName.equals(Messages.Tab_LogCategoryVisualforce)) {
				finalLogLevels.put(LogCategory.Visualforce, logLevel);
			} else if (categoryName.equals(Messages.Tab_LogCategorySystem)) {
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
				choice.set(DialogUtils.getInstance().cancelContinueMessage(
						Messages.LaunchDelegate_ConfirmDialogTitle, 
						Messages.LaunchDelegate_CannotLaunchAsyncWhileDebugging, 
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
				choice.set(DialogUtils.getInstance().cancelContinueMessage(
						Messages.LaunchDelegate_ConfirmDialogTitle, 
						Messages.LaunchDelegate_ExistingTraceFlag, 
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
