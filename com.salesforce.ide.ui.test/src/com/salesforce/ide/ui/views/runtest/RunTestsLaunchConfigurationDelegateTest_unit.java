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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

public class RunTestsLaunchConfigurationDelegateTest_unit extends TestCase {

	private RunTestsLaunchConfigurationDelegate mockedDelegate;
	
	@Before
    @Override
    public void setUp() throws Exception {
		mockedDelegate = mock(RunTestsLaunchConfigurationDelegate.class);
	}
	
	@Test
	public void testAttributeProjectName() throws Exception {
		String expectedProjectName = "Test";
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		when(configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, "")).thenReturn(expectedProjectName);
		when(mockedDelegate.getProjectName(configuration)).thenCallRealMethod();
		
		String actualProjectName = mockedDelegate.getProjectName(configuration);
		
		assertEquals(expectedProjectName, actualProjectName);
	}
	
	@Test
	public void testAttributeTestsArray() throws Exception {
		String expectedTestsArray = "Test";
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		when(configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TESTS_ARRAY, "")).thenReturn(expectedTestsArray);
		when(mockedDelegate.getTestsArray(configuration)).thenCallRealMethod();
		
		String actualTestsArray = mockedDelegate.getTestsArray(configuration);
		
		assertEquals(expectedTestsArray, actualTestsArray);
	}
	
	@Test
	public void testAttributeTotalTests() throws Exception {
		int expectedTotalTests = 5;
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		when(configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TESTS_TOTAL, 0)).thenReturn(expectedTotalTests);
		when(mockedDelegate.getTotalTests(configuration)).thenCallRealMethod();
		
		int actualTotalTests = mockedDelegate.getTotalTests(configuration);
		
		assertEquals(expectedTotalTests, actualTotalTests);
	}
	
	@Test
	public void testAttributeTestMode() throws Exception {
		boolean expectedTestMode = false;
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		when(configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, true)).thenReturn(expectedTestMode);
		when(mockedDelegate.getTestMode(configuration)).thenCallRealMethod();
		
		boolean actualTestMode = mockedDelegate.getTestMode(configuration);
		
		assertEquals(expectedTestMode, actualTestMode);
	}
	
	@Test
	public void testMaterializeForceProjectUsesResourcesPlugin() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		IProject expectedProject = mock(IProject.class);
		when(mockedDelegate.materializeForceProject(configuration)).thenCallRealMethod();
		when(mockedDelegate.getProjectName(configuration)).thenReturn("AnyClass");
		when(mockedDelegate.getProjectFromName(any(String.class))).thenReturn(expectedProject);
		
		IProject actualProject = mockedDelegate.materializeForceProject(configuration);
		
		assertEquals(expectedProject, actualProject);
	}
	
	@Test
	public void testMaterializeForceProjectHandlesNull() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		when(mockedDelegate.materializeForceProject(configuration)).thenCallRealMethod();
		when(mockedDelegate.getProjectName(configuration)).thenReturn("AnyClass");
		when(mockedDelegate.getProjectFromName(any(String.class))).thenReturn(null);
		doNothing().when(mockedDelegate).throwErrorMsg(any(String.class));
		
		IProject actualProject = mockedDelegate.materializeForceProject(configuration);
		
		verify(mockedDelegate, times(1)).throwErrorMsg(Messages.LaunchConfigurationDelegate_CannotLaunchInvalidForceProject);
		assertNull(actualProject);
	}
	
	@Test
	public void testCheckRunMode() throws Exception {
		String mode = ILaunchManager.RUN_MODE;
		doCallRealMethod().when(mockedDelegate).checkMode(mode);
		
		mockedDelegate.checkMode(mode);
		
		verify(mockedDelegate, never()).throwErrorMsg(any(String.class));
	}
	
	@Test
	public void testCheckDebugMode() throws Exception {
		String mode = ILaunchManager.DEBUG_MODE;
		doCallRealMethod().when(mockedDelegate).checkMode(mode);
		doNothing().when(mockedDelegate).throwErrorMsg(any(String.class));
		
		mockedDelegate.checkMode(mode);
		
		verify(mockedDelegate, times(1)).throwErrorMsg(Messages.RunTestsLaunchConfigurationDelegate_CannotLaunchDebugModeErrorMessage);
	}
	
	@Test
	public void testThrowNullErrorMsg() throws Exception {
		doCallRealMethod().when(mockedDelegate).throwErrorMsg(any(String.class));
		
		try {
			mockedDelegate.throwErrorMsg(null);
		} catch (CoreException ce) {
			fail("Should not have thrown an error");
		}
	}
	
	@Test
	public void testThrowEmptyErrorMsg() throws Exception {
		doCallRealMethod().when(mockedDelegate).throwErrorMsg(any(String.class));
		
		try {
			mockedDelegate.throwErrorMsg("");
		} catch (CoreException ce) {
			fail("Should not have thrown an error");
		}
	}
	
	@Test
	public void testPreLaunchWithNullRunTestViewAndSuperReturnsFalse() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		String mode = ILaunchManager.RUN_MODE;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		when(mockedDelegate.preLaunchCheck(configuration, mode, monitor)).thenCallRealMethod();
		doNothing().when(mockedDelegate).checkMode(mode);
		when(mockedDelegate.getRunTestView()).thenReturn(null);
		when(mockedDelegate.secondPhasePreLaunchCheck(configuration, mode, monitor)).thenReturn(false);
		
		boolean prelaunchResult = mockedDelegate.preLaunchCheck(configuration, mode, monitor);
		
		verify(mockedDelegate, times(1)).checkMode(mode);
		assertFalse(prelaunchResult);
	}
	
	@Test
	public void testPreLaunchWithNullRunTestViewAndSuperReturnsTrue() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		String mode = ILaunchManager.RUN_MODE;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		when(mockedDelegate.preLaunchCheck(configuration, mode, monitor)).thenCallRealMethod();
		doNothing().when(mockedDelegate).checkMode(mode);
		when(mockedDelegate.getRunTestView()).thenReturn(null);
		when(mockedDelegate.secondPhasePreLaunchCheck(configuration, mode, monitor)).thenReturn(true);
		
		boolean prelaunchResult = mockedDelegate.preLaunchCheck(configuration, mode, monitor);
		
		verify(mockedDelegate, times(1)).checkMode(mode);
		assertTrue(prelaunchResult);
	}
	
	@Test
	public void testPreLaunchWithExistingRun() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		String mode = ILaunchManager.RUN_MODE;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		RunTestsView runTestsView = mock(RunTestsView.class);
		
		when(mockedDelegate.preLaunchCheck(configuration, mode, monitor)).thenCallRealMethod();
		doNothing().when(mockedDelegate).checkMode(mode);
		when(mockedDelegate.getRunTestView()).thenReturn(runTestsView);
		when(runTestsView.canRun()).thenReturn(false);
		doNothing().when(mockedDelegate).throwErrorMsg(any(String.class));
		when(mockedDelegate.secondPhasePreLaunchCheck(configuration, mode, monitor)).thenReturn(true);
		
		boolean prelaunchResult = mockedDelegate.preLaunchCheck(configuration, mode, monitor);
		
		verify(mockedDelegate, times(1)).checkMode(mode);
		verify(mockedDelegate, times(1)).getRunTestView();
		verify(mockedDelegate, times(1)).throwErrorMsg(Messages.RunTestsLaunchConfigurationDelegate_CannotLaunchAnotherConfig);
		assertTrue(prelaunchResult);
	}
	
	@Test
	public void testPreLaunchAsyncWithDebugUserContinues() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		String mode = ILaunchManager.RUN_MODE;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		RunTestsView runTestsView = mock(RunTestsView.class);
		IProject project = mock(IProject.class);
		
		when(mockedDelegate.preLaunchCheck(configuration, mode, monitor)).thenCallRealMethod();
		doNothing().when(mockedDelegate).checkMode(mode);
		when(mockedDelegate.getRunTestView()).thenReturn(runTestsView);
		when(runTestsView.canRun()).thenReturn(true);
		when(mockedDelegate.materializeForceProject(configuration)).thenReturn(project);
		when(mockedDelegate.getTestMode(configuration)).thenReturn(true);
		when(mockedDelegate.isProjectDebugging(project)).thenReturn(true);
		when(runTestsView.confirmAsyncTestRunWhileDebugging()).thenReturn(true);
		when(mockedDelegate.secondPhasePreLaunchCheck(configuration, mode, monitor)).thenReturn(true);
		
		boolean prelaunchResult = mockedDelegate.preLaunchCheck(configuration, mode, monitor);
		
		verify(mockedDelegate, times(1)).checkMode(mode);
		verify(mockedDelegate, times(1)).getRunTestView();
		verify(mockedDelegate, times(1)).materializeForceProject(configuration);
		verify(mockedDelegate, times(1)).getTestMode(configuration);
		verify(mockedDelegate, times(1)).isProjectDebugging(project);
		verify(runTestsView, times(1)).confirmAsyncTestRunWhileDebugging();
		verify(mockedDelegate, never()).getDisplay();
		assertTrue(prelaunchResult);
	}
	
	@Test
	public void testPreLaunchAsyncWithDebugUserAborts() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		String mode = ILaunchManager.RUN_MODE;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		RunTestsView runTestsView = mock(RunTestsView.class);
		IProject project = mock(IProject.class);
		Display display = mock(Display.class);
		
		when(mockedDelegate.preLaunchCheck(configuration, mode, monitor)).thenCallRealMethod();
		doNothing().when(mockedDelegate).checkMode(mode);
		when(mockedDelegate.getRunTestView()).thenReturn(runTestsView);
		when(runTestsView.canRun()).thenReturn(true);
		when(mockedDelegate.materializeForceProject(configuration)).thenReturn(project);
		when(mockedDelegate.getTestMode(configuration)).thenReturn(true);
		when(mockedDelegate.isProjectDebugging(project)).thenReturn(true);
		when(runTestsView.confirmAsyncTestRunWhileDebugging()).thenReturn(false);
		when(mockedDelegate.getDisplay()).thenReturn(display);
		doNothing().when(display).asyncExec(any(Runnable.class));
		
		boolean prelaunchResult = mockedDelegate.preLaunchCheck(configuration, mode, monitor);
		
		verify(mockedDelegate, times(1)).checkMode(mode);
		verify(mockedDelegate, times(1)).getRunTestView();
		verify(mockedDelegate, times(1)).materializeForceProject(configuration);
		verify(mockedDelegate, times(1)).getTestMode(configuration);
		verify(mockedDelegate, times(1)).isProjectDebugging(project);
		verify(runTestsView, times(1)).confirmAsyncTestRunWhileDebugging();
		verify(mockedDelegate, times(1)).getDisplay();
		verify(display, times(1)).asyncExec(any(Runnable.class));
		assertFalse(prelaunchResult);
	}
	
	@Test
	public void testPreLaunchAsyncWithoutDebug() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		String mode = ILaunchManager.RUN_MODE;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		RunTestsView runTestsView = mock(RunTestsView.class);
		IProject project = mock(IProject.class);
		
		when(mockedDelegate.preLaunchCheck(configuration, mode, monitor)).thenCallRealMethod();
		doNothing().when(mockedDelegate).checkMode(mode);
		when(mockedDelegate.getRunTestView()).thenReturn(runTestsView);
		when(runTestsView.canRun()).thenReturn(true);
		when(mockedDelegate.materializeForceProject(configuration)).thenReturn(project);
		when(mockedDelegate.getTestMode(configuration)).thenReturn(true);
		when(mockedDelegate.isProjectDebugging(project)).thenReturn(false);
		when(mockedDelegate.secondPhasePreLaunchCheck(configuration, mode, monitor)).thenReturn(true);
		
		boolean prelaunchResult = mockedDelegate.preLaunchCheck(configuration, mode, monitor);
		
		verify(mockedDelegate, times(1)).checkMode(mode);
		verify(mockedDelegate, times(1)).getRunTestView();
		verify(mockedDelegate, times(1)).materializeForceProject(configuration);
		verify(mockedDelegate, times(1)).getTestMode(configuration);
		verify(mockedDelegate, times(1)).isProjectDebugging(project);
		verify(runTestsView, never()).confirmAsyncTestRunWhileDebugging();
		verify(mockedDelegate, never()).getDisplay();
		assertTrue(prelaunchResult);
	}
	
	@Test
	public void testPreLaunchSyncWithDebug() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		String mode = ILaunchManager.RUN_MODE;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		RunTestsView runTestsView = mock(RunTestsView.class);
		IProject project = mock(IProject.class);
		
		when(mockedDelegate.preLaunchCheck(configuration, mode, monitor)).thenCallRealMethod();
		doNothing().when(mockedDelegate).checkMode(mode);
		when(mockedDelegate.getRunTestView()).thenReturn(runTestsView);
		when(runTestsView.canRun()).thenReturn(true);
		when(mockedDelegate.materializeForceProject(configuration)).thenReturn(project);
		when(mockedDelegate.getTestMode(configuration)).thenReturn(false);
		when(mockedDelegate.isProjectDebugging(project)).thenReturn(true);
		when(mockedDelegate.secondPhasePreLaunchCheck(configuration, mode, monitor)).thenReturn(true);
		
		boolean prelaunchResult = mockedDelegate.preLaunchCheck(configuration, mode, monitor);
		
		verify(mockedDelegate, times(1)).checkMode(mode);
		verify(mockedDelegate, times(1)).getRunTestView();
		verify(mockedDelegate, times(1)).materializeForceProject(configuration);
		verify(mockedDelegate, times(1)).getTestMode(configuration);
		verify(mockedDelegate, times(1)).isProjectDebugging(project);
		verify(runTestsView, never()).confirmAsyncTestRunWhileDebugging();
		verify(mockedDelegate, never()).getDisplay();
		assertTrue(prelaunchResult);
	}
	
	@Test
	public void testPreLaunchSyncWithoutDebug() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		String mode = ILaunchManager.RUN_MODE;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		RunTestsView runTestsView = mock(RunTestsView.class);
		IProject project = mock(IProject.class);
		
		when(mockedDelegate.preLaunchCheck(configuration, mode, monitor)).thenCallRealMethod();
		doNothing().when(mockedDelegate).checkMode(mode);
		when(mockedDelegate.getRunTestView()).thenReturn(runTestsView);
		when(runTestsView.canRun()).thenReturn(true);
		when(mockedDelegate.materializeForceProject(configuration)).thenReturn(project);
		when(mockedDelegate.getTestMode(configuration)).thenReturn(false);
		when(mockedDelegate.isProjectDebugging(project)).thenReturn(false);
		when(mockedDelegate.secondPhasePreLaunchCheck(configuration, mode, monitor)).thenReturn(true);
		
		boolean prelaunchResult = mockedDelegate.preLaunchCheck(configuration, mode, monitor);
		
		verify(mockedDelegate, times(1)).checkMode(mode);
		verify(mockedDelegate, times(1)).getRunTestView();
		verify(mockedDelegate, times(1)).materializeForceProject(configuration);
		verify(mockedDelegate, times(1)).getTestMode(configuration);
		verify(mockedDelegate, times(1)).isProjectDebugging(project);
		verify(runTestsView, never()).confirmAsyncTestRunWhileDebugging();
		verify(mockedDelegate, never()).getDisplay();
		assertTrue(prelaunchResult);
	}
	
	@Test
	public void testLaunchWithNullRunTestView() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		String mode = ILaunchManager.RUN_MODE;
		ILaunch launch = mock(ILaunch.class);
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		RunTestsView runTestsView = mock(RunTestsView.class);
		IProject project = mock(IProject.class);
		Map<IResource, List<String>> testResources = Collections.<IResource, List<String>> emptyMap();
		String tests = "";
		int totalTests = 0;
		boolean isAsync = false;
		boolean isDebugging = false;
		
		DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
		
		doCallRealMethod().when(mockedDelegate).launch(configuration, mode, launch, monitor);
		when(mockedDelegate.getRunTestView()).thenReturn(null);
		when(mockedDelegate.materializeForceProject(configuration)).thenReturn(project);
		when(mockedDelegate.getTestsArray(configuration)).thenReturn(tests);
		when(mockedDelegate.getTotalTests(configuration)).thenReturn(totalTests);
		when(mockedDelegate.getTestMode(configuration)).thenReturn(isAsync);
		when(mockedDelegate.findTestClasses(project)).thenReturn(testResources);
		doCallRealMethod().when(mockedDelegate).removeLaunch(launch);
		doNothing().when(runTestsView).runTests(project, testResources, tests, totalTests, isAsync, isDebugging, monitor);
		
		mockedDelegate.launch(configuration, mode, launch, monitor);
		
		verify(mockedDelegate, times(1)).getRunTestView();
		verify(mockedDelegate, times(1)).materializeForceProject(configuration);
		verify(mockedDelegate, times(1)).getTestsArray(configuration);
		verify(mockedDelegate, times(1)).getTotalTests(configuration);
		verify(mockedDelegate, times(1)).getTestMode(configuration);
		verify(mockedDelegate, times(1)).findTestClasses(project);
		verify(mockedDelegate, times(1)).removeLaunch(launch);
		verify(runTestsView, never()).runTests(project, testResources, tests, totalTests, isAsync, isDebugging, monitor);
		
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		assertNotNull(launches);
		assertEquals(0, launches.length);
	}
	
	@Test
	public void testLaunchWithValidRunTestView() throws Exception {
		ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
		String mode = ILaunchManager.RUN_MODE;
		ILaunch launch = mock(ILaunch.class);
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		RunTestsView runTestsView = mock(RunTestsView.class);
		IProject project = mock(IProject.class);
		Map<IResource, List<String>> testResources = Collections.<IResource, List<String>> emptyMap();
		String tests = "";
		int totalTests = 0;
		boolean isAsync = false;
		boolean isDebugging = false;
		
		DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
		
		doCallRealMethod().when(mockedDelegate).launch(configuration, mode, launch, monitor);
		when(mockedDelegate.getRunTestView()).thenReturn(runTestsView);
		when(mockedDelegate.materializeForceProject(configuration)).thenReturn(project);
		when(mockedDelegate.getTestsArray(configuration)).thenReturn(tests);
		when(mockedDelegate.getTotalTests(configuration)).thenReturn(totalTests);
		when(mockedDelegate.getTestMode(configuration)).thenReturn(isAsync);
		when(mockedDelegate.findTestClasses(project)).thenReturn(testResources);
		doCallRealMethod().when(mockedDelegate).removeLaunch(launch);
		doNothing().when(runTestsView).runTests(project, testResources, tests, totalTests, isAsync, isDebugging, monitor);
		
		mockedDelegate.launch(configuration, mode, launch, monitor);
		
		verify(mockedDelegate, times(1)).getRunTestView();
		verify(mockedDelegate, times(1)).materializeForceProject(configuration);
		verify(mockedDelegate, times(1)).getTestsArray(configuration);
		verify(mockedDelegate, times(1)).getTotalTests(configuration);
		verify(mockedDelegate, times(1)).getTestMode(configuration);
		verify(mockedDelegate, times(1)).findTestClasses(project);
		verify(mockedDelegate, times(1)).removeLaunch(launch);
		verify(runTestsView, times(1)).runTests(project, testResources, tests, totalTests, isAsync, isDebugging, monitor);
		
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		assertNotNull(launches);
		assertEquals(0, launches.length);
	}
}
