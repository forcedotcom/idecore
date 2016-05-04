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

package com.salesforce.ide.ui.views.runtest.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.ToolingStubExt;
import com.salesforce.ide.core.remote.tooling.TraceFlagUtil;
import com.salesforce.ide.core.remote.tooling.Limits.Limit;
import com.salesforce.ide.core.remote.tooling.Limits.LimitsCommand;
import com.salesforce.ide.core.remote.tooling.RunTests.RunTestsCommand;
import com.salesforce.ide.core.remote.tooling.RunTests.RunTestsSyncResponse;
import com.salesforce.ide.ui.views.runtest.RunTestsConstants;
import com.salesforce.ide.ui.views.runtest.RunTestsView;
import com.sforce.soap.tooling.ApexLogLevel;
import com.sforce.soap.tooling.sobject.ApexTestQueueItem;
import com.sforce.soap.tooling.sobject.ApexTestResult;
import com.sforce.soap.tooling.AsyncApexJobStatus;
import com.sforce.soap.tooling.LogCategory;
import com.sforce.soap.tooling.sobject.SObject;
import com.sforce.soap.tooling.QueryResult;
import junit.framework.TestCase;

@SuppressWarnings("unchecked")
public class RunTestsViewTest_unit extends TestCase {

	private RunTestsView mockedView;
	
	@Before
    @Override
    public void setUp() throws Exception {
		mockedView = mock(RunTestsView.class);
		mockedView.lock = new ReentrantLock();
		mockedView.forceProject = mock(ForceProject.class);
		mockedView.toolingStubExt = mock(ToolingStubExt.class);
	}
	
	@After
	@Override
	public void tearDown() {
		mockedView = null;
	}
	
	@Test
	public void testCanRunWithoutLock() {
		mockedView.lock = null;
		doCallRealMethod().when(mockedView).canRun();
		
		assertFalse(mockedView.canRun());
	}
	
	@Test
	public void testCanRunWhileLocked() {
		mockedView.lock.lock();
		doCallRealMethod().when(mockedView).canRun();
		
		assertFalse(mockedView.canRun());
	}
	
	@Test
	public void testCanRunWhileNotLocked() {
		doCallRealMethod().when(mockedView).canRun();
		
		assertTrue(mockedView.canRun());
	}
	
	@Test
	public void testRunTestsNoForceProject() throws Exception {
		IProject project = mock(IProject.class);
		Map<IResource, List<String>> testResources = new HashMap<IResource, List<String>>();
		String testsInJson = "";
		boolean shouldUseSuites = false;
		boolean isAsync = true;
		boolean isDebugging = false;
		boolean hasExistingTraceFlag = false;
		boolean enableLogging = false;
		Map<LogCategory, ApexLogLevel> logLevels = Collections.emptyMap();
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		doCallRealMethod().when(mockedView).runTests(eq(project),eq(testsInJson), eq(shouldUseSuites),
				eq(isAsync), eq(isDebugging), eq(hasExistingTraceFlag), eq(enableLogging), eq(logLevels), eq(monitor));
		when(mockedView.materializeForceProject(project)).thenReturn(null);
		
		mockedView.runTests(project, testsInJson, shouldUseSuites, isAsync, isDebugging,
				hasExistingTraceFlag, enableLogging, logLevels, monitor);
		
		verify(mockedView, times(1)).materializeForceProject(project);
		verify(mockedView, never()).getTraceFlagUtil(any(ForceProject.class));
		verify(mockedView, never()).prepareForRunningTests(any(IProject.class));
		verify(mockedView, never()).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class), any(Boolean.class));
		verify(mockedView, never()).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		verify(mockedView, never()).processAsyncTestResults(any(Map.class), any(List.class), any(Boolean.class));
		verify(mockedView, never()).displayCodeCoverage();
		verify(mockedView, never()).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		verify(mockedView, never()).processSyncTestResults(eq(project), eq(testResources), any(com.salesforce.ide.core.remote.tooling.RunTests.RunTestsSyncResponse.class));
		verify(mockedView, never()).finishRunningTests();
	}
	
	@Test
	public void testRunTestsAborted() throws Exception {
		IProject project = mock(IProject.class);
		String testsInJson = "";
		boolean shouldUseSuites = false;
		boolean isAsync = true;
		boolean isDebugging = false;
		boolean hasExistingTraceFlag = false;
		boolean enableLogging = false;
		Map<LogCategory, ApexLogLevel> logLevels = Collections.emptyMap();
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		doCallRealMethod().when(mockedView).runTests(eq(project), eq(testsInJson), eq(shouldUseSuites),
				eq(isAsync), eq(isDebugging), eq(hasExistingTraceFlag), eq(enableLogging), eq(logLevels), eq(monitor));
		ForceProject fp = mock(ForceProject.class);
		when(fp.getUserName()).thenReturn("");
		when(mockedView.materializeForceProject(project)).thenReturn(fp);
		
		TraceFlagUtil tfUtil = mock(TraceFlagUtil.class);
		when(mockedView.getTraceFlagUtil(fp)).thenReturn(tfUtil);
		
		doNothing().when(mockedView).prepareForRunningTests(project);
		doNothing().when(mockedView).finishRunningTests();
		
		when(monitor.isCanceled()).thenReturn(true);
		
		mockedView.runTests(project, testsInJson, shouldUseSuites, isAsync, isDebugging,
				hasExistingTraceFlag, enableLogging, logLevels, monitor);
		
		verify(mockedView, times(1)).materializeForceProject(project);
		verify(mockedView, times(1)).getTraceFlagUtil(any(ForceProject.class));
		verify(mockedView, times(1)).prepareForRunningTests(project);
		verify(mockedView, never()).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class), any(Boolean.class));
		verify(mockedView, never()).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		verify(mockedView, never()).processAsyncTestResults(any(Map.class), any(List.class), any(Boolean.class));
		verify(mockedView, never()).displayCodeCoverage();
		verify(mockedView, never()).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		verify(mockedView, never()).processSyncTestResults(any(IProject.class), any(Map.class), any(RunTestsSyncResponse.class));
		verify(mockedView, times(1)).finishRunningTests();
	}
	
	@Test
	public void testRunTestsEmptyResponseFromServer() throws Exception {
		IProject project = mock(IProject.class);
		String testsInJson = "";
		boolean shouldUseSuites = false;
		boolean isAsync = true;
		boolean isDebugging = false;
		boolean hasExistingTraceFlag = false;
		boolean enableLogging = false;
		Map<LogCategory, ApexLogLevel> logLevels = Collections.emptyMap();
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		doCallRealMethod().when(mockedView).runTests(eq(project), eq(testsInJson), eq(shouldUseSuites),
				eq(isAsync), eq(isDebugging), eq(hasExistingTraceFlag), eq(enableLogging), eq(logLevels), eq(monitor));
		
		ForceProject fp = mock(ForceProject.class);
		when(fp.getUserName()).thenReturn("");
		when(mockedView.materializeForceProject(project)).thenReturn(fp);
		
		TraceFlagUtil tfUtil = mock(TraceFlagUtil.class);
		when(mockedView.getTraceFlagUtil(fp)).thenReturn(tfUtil);
		
		doNothing().when(mockedView).prepareForRunningTests(project);
		doNothing().when(mockedView).finishRunningTests();
		
		when(monitor.isCanceled()).thenReturn(false);
		
		when(mockedView.enqueueTests(testsInJson, shouldUseSuites, isAsync, isDebugging)).thenReturn("");
		
		when(mockedView.findTestClasses(project)).thenReturn(Collections.EMPTY_MAP);
				
		mockedView.runTests(project, testsInJson, shouldUseSuites, isAsync, isDebugging,
				hasExistingTraceFlag, enableLogging, logLevels, monitor);
		
		verify(mockedView, times(1)).materializeForceProject(project);
		verify(mockedView, times(1)).getTraceFlagUtil(any(ForceProject.class));
		verify(mockedView, times(1)).prepareForRunningTests(project);
		verify(mockedView, times(1)).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class), any(Boolean.class));
		verify(mockedView, times(1)).findTestClasses(project);
		verify(mockedView, never()).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		verify(mockedView, never()).processAsyncTestResults(any(Map.class), any(List.class), any(Boolean.class));
		verify(mockedView, never()).displayCodeCoverage();
		verify(mockedView, never()).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		verify(mockedView, never()).processSyncTestResults(any(IProject.class), any(Map.class), any(RunTestsSyncResponse.class));
		verify(mockedView, times(1)).finishRunningTests();
	}
	
	@Test
	public void testRunTestsAsync() throws Exception {
		IProject project = mock(IProject.class);
		String testsInJson = "";
		boolean shouldUseSuites = true;
		boolean isAsync = true;
		boolean isDebugging = false;
		boolean hasExistingTraceFlag = false;
		boolean enableLogging = false;
		Map<LogCategory, ApexLogLevel> logLevels = Collections.emptyMap();
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		doCallRealMethod().when(mockedView).runTests(eq(project), eq(testsInJson), eq(shouldUseSuites),
				eq(isAsync), eq(isDebugging), eq(hasExistingTraceFlag), eq(enableLogging), eq(logLevels), eq(monitor));
		
		ForceProject fp = mock(ForceProject.class);
		when(fp.getUserName()).thenReturn("");
		when(mockedView.materializeForceProject(project)).thenReturn(fp);
		
		TraceFlagUtil tfUtil = mock(TraceFlagUtil.class);
		when(mockedView.getTraceFlagUtil(fp)).thenReturn(tfUtil);
		
		doNothing().when(mockedView).prepareForRunningTests(project);
		doNothing().when(mockedView).finishRunningTests();
		
		when(monitor.isCanceled()).thenReturn(false);
		
		when(mockedView.enqueueTests(testsInJson, shouldUseSuites, isAsync, isDebugging)).thenReturn("Amazing");
		
		when(mockedView.findTestClasses(project)).thenReturn(Collections.EMPTY_MAP);
				
		doNothing().when(mockedView).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		doNothing().when(mockedView).displayCodeCoverage();
		
		mockedView.runTests(project, testsInJson, shouldUseSuites, isAsync, isDebugging,
				hasExistingTraceFlag, enableLogging, logLevels, monitor);
		
		verify(mockedView, times(1)).materializeForceProject(project);
		verify(mockedView, times(1)).getTraceFlagUtil(eq(fp));
		verify(mockedView, times(1)).prepareForRunningTests(project);
		verify(mockedView, times(1)).enqueueTests(eq(testsInJson), eq(shouldUseSuites), eq(isAsync), eq(isDebugging));
		verify(mockedView, times(1)).findTestClasses(project);
		verify(mockedView, times(1)).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		verify(mockedView, times(1)).displayCodeCoverage();
		verify(mockedView, never()).processAsyncTestResults(any(Map.class), any(List.class), any(Boolean.class));
		verify(mockedView, never()).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		verify(mockedView, never()).processSyncTestResults(any(IProject.class), any(Map.class), any(RunTestsSyncResponse.class));
		verify(mockedView, times(1)).finishRunningTests();
	}
	
	@Test
	public void testRunTestsSync() throws Exception {
		IProject project = mock(IProject.class);
		Map<IResource, List<String>> testResources = new HashMap<IResource, List<String>>();
		String testsInJson = "";
		boolean shouldUseSuites = true;
		boolean isAsync = false;
		boolean isDebugging = false;
		boolean hasExistingTraceFlag = false;
		boolean enableLogging = false;
		Map<LogCategory, ApexLogLevel> logLevels = Collections.emptyMap();
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		doCallRealMethod().when(mockedView).runTests(eq(project), eq(testsInJson), eq(shouldUseSuites),
				eq(isAsync), eq(isDebugging), eq(hasExistingTraceFlag), eq(enableLogging), eq(logLevels), eq(monitor));
		
		ForceProject fp = mock(ForceProject.class);
		when(fp.getUserName()).thenReturn("");
		when(mockedView.materializeForceProject(project)).thenReturn(fp);
		
		TraceFlagUtil tfUtil = mock(TraceFlagUtil.class);
		when(mockedView.getTraceFlagUtil(fp)).thenReturn(tfUtil);
		
		doNothing().when(mockedView).prepareForRunningTests(project);
		doNothing().when(mockedView).finishRunningTests();
		
		when(monitor.isCanceled()).thenReturn(false);
		
		when(mockedView.enqueueTests(testsInJson, shouldUseSuites, isAsync, isDebugging)).thenReturn("{}");
		
		when(mockedView.findTestClasses(project)).thenReturn(Collections.EMPTY_MAP);
		
		doNothing().when(mockedView).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		doNothing().when(mockedView).processSyncTestResults(eq(project), eq(testResources), any(RunTestsSyncResponse.class));
		doNothing().when(mockedView).displayCodeCoverage();
		
		mockedView.runTests(project, testsInJson, shouldUseSuites, isAsync, isDebugging,
				hasExistingTraceFlag, enableLogging, logLevels, monitor);
		
		verify(mockedView, times(1)).materializeForceProject(project);
		verify(mockedView, times(1)).getTraceFlagUtil(any(ForceProject.class));
		verify(mockedView, times(1)).prepareForRunningTests(project);
		verify(mockedView, times(1)).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class), any(Boolean.class));
		verify(mockedView, times(1)).findTestClasses(project);
		verify(mockedView, never()).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		verify(mockedView, never()).processAsyncTestResults(any(Map.class), any(List.class), any(Boolean.class));
		verify(mockedView, times(1)).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		verify(mockedView, times(1)).processSyncTestResults(eq(project), eq(testResources), any(RunTestsSyncResponse.class));
		verify(mockedView, times(1)).displayCodeCoverage();
		verify(mockedView, times(1)).finishRunningTests();
	}
	
	@Test
	public void testRunTestsWithLogging() throws Exception {
		IProject project = mock(IProject.class);
		Map<IResource, List<String>> testResources = new HashMap<IResource, List<String>>();
		String testsInJson = "";
		boolean shouldUseSuites = false;
		boolean isAsync = true;
		boolean isDebugging = false;
		boolean hasExistingTraceFlag = false;
		boolean enableLogging = true;
		Map<LogCategory, ApexLogLevel> logLevels = Collections.emptyMap();
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		doCallRealMethod().when(mockedView).runTests(eq(project), eq(testsInJson), eq(shouldUseSuites),
				eq(isAsync), eq(isDebugging), eq(hasExistingTraceFlag), eq(enableLogging), eq(logLevels), eq(monitor));
		
		ForceProject fp = mock(ForceProject.class);
		when(fp.getUserName()).thenReturn("");
		when(mockedView.materializeForceProject(project)).thenReturn(fp);
		
		TraceFlagUtil tfUtil = mock(TraceFlagUtil.class);
		when(tfUtil.getUserId(any(String.class))).thenReturn("");
		when(tfUtil.insertDebugLevel(any(String.class), any(Map.class))).thenReturn("");
		when(tfUtil.insertTraceFlag(any(String.class), any(Integer.class), any(String.class))).thenReturn("");
		doNothing().when(tfUtil).automateTraceFlagExtension(any(String.class), any(Integer.class), any(Integer.class));
		doNothing().when(tfUtil).removeTraceFlagJobs();
		doNothing().when(tfUtil).deleteTraceflagAndDebugLevel(any(String.class), any(String.class));
		when(mockedView.getTraceFlagUtil(any(ForceProject.class))).thenReturn(tfUtil);
		
		doNothing().when(mockedView).prepareForRunningTests(project);
		doNothing().when(mockedView).finishRunningTests();
		
		when(monitor.isCanceled()).thenReturn(false);
		
		when(mockedView.enqueueTests(testsInJson, shouldUseSuites, isAsync, isDebugging)).thenReturn("Amazing");
		
		when(mockedView.findTestClasses(project)).thenReturn(Collections.EMPTY_MAP);
		
		doNothing().when(mockedView).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		doNothing().when(mockedView).displayCodeCoverage();
		
		mockedView.runTests(project, testsInJson, shouldUseSuites, isAsync, isDebugging,
				hasExistingTraceFlag, enableLogging, logLevels, monitor);
		
		verify(mockedView, times(1)).materializeForceProject(project);
		verify(mockedView, times(1)).getTraceFlagUtil(any(ForceProject.class));
		verify(mockedView, times(1)).prepareForRunningTests(project);
		verify(mockedView, times(1)).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class), any(Boolean.class));
		verify(mockedView, times(1)).findTestClasses(project);
		verify(mockedView, times(1)).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		verify(mockedView, never()).processAsyncTestResults(any(Map.class), any(List.class), any(Boolean.class));
		verify(mockedView, times(1)).displayCodeCoverage();
		verify(mockedView, never()).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		verify(mockedView, never()).processSyncTestResults(eq(project), eq(testResources), any(RunTestsSyncResponse.class));
		verify(mockedView, times(1)).finishRunningTests();
		verify(tfUtil, times(1)).getUserId(any(String.class));
		verify(tfUtil, times(1)).insertDebugLevel(any(String.class), any(Map.class));
		verify(tfUtil, times(1)).insertTraceFlag(any(String.class), any(Integer.class), any(String.class));
		verify(tfUtil, times(1)).automateTraceFlagExtension(any(String.class), any(Integer.class), any(Integer.class));
		verify(tfUtil, times(1)).removeTraceFlagJobs();
		verify(tfUtil, times(1)).deleteTraceflagAndDebugLevel(any(String.class), any(String.class));
	}
	
	@Test
	public void testMaterializeNullForceProject() {
		doCallRealMethod().when(mockedView).materializeForceProject(any(IProject.class));
		
		assertNull(mockedView.materializeForceProject(null));
	}
	
	@Test
	public void testMaterializeNonExistingForceProject() {
		doCallRealMethod().when(mockedView).materializeForceProject(any(IProject.class));
		IProject project = mock(IProject.class);
		
		assertNull(mockedView.materializeForceProject(project));
	}
	
	@Test
	public void testEnqueueTestsWithNullProject() throws Exception {
		doCallRealMethod().when(mockedView).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class), any(Boolean.class));
		mockedView.forceProject = null;
		
		assertNull(mockedView.enqueueTests("", true, true, false));
	}
	
	@Test
	public void testEnqueueTests() throws Exception {
		doCallRealMethod().when(mockedView).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class), any(Boolean.class));
		doCallRealMethod().when(mockedView).getConnTimeoutVal(any(Boolean.class), any(Boolean.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class), any(Integer.class));
		
		String testsInJson = "{}";
		boolean shouldUseSuites = true;
		boolean isAsync = false;
		boolean isDebugging = true;
		String response = "tests";
		RunTestsCommand job = mock(RunTestsCommand.class);
		when(job.getAnswer()).thenReturn(response);
		when(job.wasError()).thenReturn(false);
		when(mockedView.getRunTestsCommand(testsInJson, isAsync)).thenReturn(job);
		
		assertEquals(response, mockedView.enqueueTests(testsInJson, shouldUseSuites, isAsync, isDebugging));
		
		verify(mockedView, times(1)).getConnTimeoutVal(isAsync, isDebugging);
		verify(mockedView, times(1)).initializeConnection(mockedView.forceProject, RunTestsConstants.SYNC_WITH_DEBUG_TIMEOUT);
		verify(mockedView, times(1)).getRunTestsCommand(testsInJson, isAsync);
	}
	
	@Test
	public void testGetConnTimeoutValWithAsyncWithoutDebug() {
		doCallRealMethod().when(mockedView).getConnTimeoutVal(any(Boolean.class), any(Boolean.class));
		
		assertEquals(RunTestsConstants.ASYNC_TIMEOUT, mockedView.getConnTimeoutVal(true, false));
	}
	
	@Test
	public void testGetConnTimeoutValWithAsyncWithtDebug() {
		doCallRealMethod().when(mockedView).getConnTimeoutVal(any(Boolean.class), any(Boolean.class));
		
		assertEquals(RunTestsConstants.ASYNC_TIMEOUT, mockedView.getConnTimeoutVal(true, true));
	}
	
	@Test
	public void testGetConnTimeoutValWithSyncWithoutDebug() {
		doCallRealMethod().when(mockedView).getConnTimeoutVal(any(Boolean.class), any(Boolean.class));
		
		assertEquals(RunTestsConstants.SYNC_WITHOUT_DEBUG_TIMEOUT, mockedView.getConnTimeoutVal(false, false));
	}
	
	@Test
	public void testGetConnTimeoutValWithSyncWithDebug() {
		doCallRealMethod().when(mockedView).getConnTimeoutVal(any(Boolean.class), any(Boolean.class));
		
		assertEquals(RunTestsConstants.SYNC_WITH_DEBUG_TIMEOUT, mockedView.getConnTimeoutVal(false, true));
	}
	
	@Test
	public void testGetAsyncTestResultsNullTestRunId() throws Exception {
		doCallRealMethod().when(mockedView).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		
		mockedView.getAsyncTestResults(null, mock(Map.class), mock(IProgressMonitor.class));
	}
	
	@Test
	public void testGetAsyncTestResultsEmptyTestRunId() throws Exception {
		doCallRealMethod().when(mockedView).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		
		mockedView.getAsyncTestResults("", mock(Map.class), mock(IProgressMonitor.class));
	}
	
	@Test
	public void testGetAsyncTestResultsNullApiLimit() throws Exception {
		doCallRealMethod().when(mockedView).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		when(mockedView.getApiLimit(any(ForceProject.class), eq(LimitsCommand.Type.DailyApiRequests))).thenReturn(null);
		
		mockedView.getAsyncTestResults("123", mock(Map.class), mock(IProgressMonitor.class));
	}
	
	@Test
	public void testGetAsyncTestResultsNotEnoughApiRequestsRemaining() throws Exception {
		doCallRealMethod().when(mockedView).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		Limit dailyRemaining = mock(Limit.class);
		when(dailyRemaining.getRemaining()).thenReturn(0);
		when(dailyRemaining.getMax()).thenReturn(10);
		when(mockedView.getApiLimit(any(ForceProject.class), eq(LimitsCommand.Type.DailyApiRequests))).thenReturn(dailyRemaining);
		
		mockedView.getAsyncTestResults("123", mock(Map.class), mock(IProgressMonitor.class));
	}
	
	@Test
	public void testGetAsyncTestResultsAborted() throws Exception {
		doCallRealMethod().when(mockedView).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		Limit dailyRemaining = mock(Limit.class);
		when(dailyRemaining.getRemaining()).thenReturn(5);
		when(dailyRemaining.getMax()).thenReturn(10);
		when(mockedView.getApiLimit(any(ForceProject.class), eq(LimitsCommand.Type.DailyApiRequests))).thenReturn(dailyRemaining);
		
		String testRunId = "123";
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		when(monitor.isCanceled()).thenReturn(true);
		when(mockedView.abortTestRun(testRunId)).thenReturn(true);
		
		when(mockedView.toolingStubExt.query(String.format(RunTestsConstants.QUERY_TESTRESULT, testRunId))).thenReturn(null);
		when(mockedView.queryTotalQueueItems(testRunId)).thenReturn(1);
		
		mockedView.getAsyncTestResults(testRunId, mock(Map.class), monitor);
		
		verify(mockedView, times(1)).abortTestRun(testRunId);
		verify(mockedView, never()).getPollInterval(any(Integer.class), any(Float.class));
	}
	
	@Test
	public void testGetAsyncTestResults() throws Exception {
		doCallRealMethod().when(mockedView).getAsyncTestResults(any(String.class), any(Map.class), any(IProgressMonitor.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		Limit dailyRemaining = mock(Limit.class);
		when(dailyRemaining.getRemaining()).thenReturn(5);
		when(dailyRemaining.getMax()).thenReturn(10);
		when(mockedView.getApiLimit(any(ForceProject.class), eq(LimitsCommand.Type.DailyApiRequests))).thenReturn(dailyRemaining);
		
		String testRunId = "123";
		int totalTestMethods = 1;
		int totalTestDone = totalTestMethods;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		when(monitor.isCanceled()).thenReturn(false);
		
		ApexTestResult tr = mock(ApexTestResult.class);
		QueryResult qr = mock(QueryResult.class);
		when(qr.getSize()).thenReturn(1);
		when(qr.getRecords()).thenReturn(new SObject[] { tr });
		when(mockedView.toolingStubExt.query(String.format(RunTestsConstants.QUERY_TESTRESULT, testRunId))).thenReturn(qr);
		when(mockedView.queryTotalQueueItems(testRunId)).thenReturn(1);
		when(mockedView.queryProcessedQueueItem(testRunId)).thenReturn(1);
		
		doNothing().when(mockedView).updateProgress(eq(0), eq(totalTestMethods), eq(totalTestDone));
		
		when(mockedView.getPollInterval(any(Integer.class), any(Float.class))).thenReturn(0);
				
		mockedView.getAsyncTestResults(testRunId, mock(Map.class), monitor);
		
		verify(mockedView, never()).abortTestRun(testRunId);
		verify(mockedView, times(1)).getPollInterval(any(Integer.class), any(Float.class));
	}
	
	@Test
	public void testAbortTestRunNullTestRunId() {
		doCallRealMethod().when(mockedView).abortTestRun(any(String.class));
		
		assertFalse(mockedView.abortTestRun(null));
	}
	
	@Test
	public void testAbortTestRunEmptyTestRunId() {
		doCallRealMethod().when(mockedView).abortTestRun(any(String.class));
		
		assertFalse(mockedView.abortTestRun(""));
	}
	
	@Test
	public void testAbortTestRunNullQueryResult() throws Exception {
		doCallRealMethod().when(mockedView).abortTestRun(any(String.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		String testRunId = "123";
		when(mockedView.toolingStubExt.query(String.format(RunTestsConstants.QUERY_APEX_TEST_QUEUE_ITEM, testRunId))).thenReturn(null);
		
		assertFalse(mockedView.abortTestRun(testRunId));
	}
	
	@Test
	public void testAbortTestRunEmptyQueryResult() throws Exception {
		doCallRealMethod().when(mockedView).abortTestRun(any(String.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		String testRunId = "123";
		QueryResult qr = mock(QueryResult.class);
		when(qr.getSize()).thenReturn(0);
		when(mockedView.toolingStubExt.query(String.format(RunTestsConstants.QUERY_APEX_TEST_QUEUE_ITEM, testRunId))).thenReturn(qr);
		
		assertFalse(mockedView.abortTestRun(testRunId));
	}
	
	@Test
	public void testAbortTestRun() throws Exception {
		doCallRealMethod().when(mockedView).abortTestRun(any(String.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		String testRunId = "123";
		
		ApexTestQueueItem test1 = mock(ApexTestQueueItem.class);
		when(test1.getStatus()).thenReturn(AsyncApexJobStatus.Queued);
		doNothing().when(test1).setStatus(AsyncApexJobStatus.Aborted);
		
		ApexTestQueueItem test2 = mock(ApexTestQueueItem.class);
		when(test2.getStatus()).thenReturn(AsyncApexJobStatus.Completed);
		
		QueryResult qr = mock(QueryResult.class);
		when(qr.getSize()).thenReturn(2);
		when(qr.getRecords()).thenReturn(new SObject[] { test1, test2 });
		when(mockedView.toolingStubExt.query(String.format(RunTestsConstants.QUERY_APEX_TEST_QUEUE_ITEM, testRunId))).thenReturn(qr);
		
		when(mockedView.toolingStubExt.update(any(SObject[].class))).thenReturn(null);
		
		assertTrue(mockedView.abortTestRun(testRunId));
	}
}
