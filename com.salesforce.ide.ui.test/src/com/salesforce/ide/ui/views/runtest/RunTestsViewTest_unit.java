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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.salesforce.ide.core.remote.PromiseableJob;
import com.salesforce.ide.core.remote.ToolingStubExt;
import com.salesforce.ide.core.remote.tooling.Limit;
import com.salesforce.ide.core.remote.tooling.LimitsCommand;
import com.salesforce.ide.core.remote.tooling.RunTestsSyncResponse;
import com.sforce.soap.metadata.LogInfo;
import com.sforce.soap.tooling.AggregateResult;
import com.sforce.soap.tooling.ApexTestQueueItem;
import com.sforce.soap.tooling.ApexTestResult;
import com.sforce.soap.tooling.AsyncApexJobStatus;
import com.sforce.soap.tooling.DeleteResult;
import com.sforce.soap.tooling.SObject;
import com.sforce.soap.tooling.QueryResult;

import junit.framework.TestCase;

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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRunTestsNoForceProject() {
		IProject project = mock(IProject.class);
		Map<IResource, List<String>> testResources = new HashMap<IResource, List<String>>();
		String testsInJson = "";
		int totalTestMethods = 0;
		boolean isAsync = true;
		boolean isDebugging = false;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		doCallRealMethod().when(mockedView).runTests(eq(project), eq(testResources), eq(testsInJson), 
				eq(totalTestMethods), eq(isAsync), eq(isDebugging), eq(monitor));
		doNothing().when(mockedView).prepareForRunningTests();
		when(mockedView.materializeForceProject(project)).thenReturn(null);
		
		mockedView.runTests(project, testResources, testsInJson, totalTestMethods, isAsync, isDebugging, monitor);
		
		verify(mockedView, never()).getUserId(any(String.class));
		verify(mockedView, never()).insertTraceFlag(any(LogInfo[].class), any(String.class));
		verify(mockedView, never()).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class));
		verify(mockedView, never()).getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class));
		verify(mockedView, never()).processAsyncTestResults(any(IProject.class), any(Map.class), any(List.class));
		verify(mockedView, never()).displayAsyncCodeCoverage();
		verify(mockedView, never()).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		verify(mockedView, never()).processSyncTestResults(eq(project), eq(testResources), any(RunTestsSyncResponse.class));
		verify(mockedView, never()).displaySyncCodeCoverage(any(RunTestsSyncResponse.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRunTestsAborted() {
		IProject project = mock(IProject.class);
		Map<IResource, List<String>> testResources = new HashMap<IResource, List<String>>();
		String testsInJson = "";
		int totalTestMethods = 0;
		boolean isAsync = true;
		boolean isDebugging = false;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		doCallRealMethod().when(mockedView).runTests(eq(project), eq(testResources), eq(testsInJson), 
				eq(totalTestMethods), eq(isAsync), eq(isDebugging), eq(monitor));
		doNothing().when(mockedView).prepareForRunningTests();
		ForceProject fp = mock(ForceProject.class);
		when(fp.getUserName()).thenReturn("");
		when(mockedView.materializeForceProject(project)).thenReturn(fp);
		when(mockedView.getUserId(any(String.class))).thenReturn("");
		when(mockedView.insertTraceFlag(any(LogInfo[].class), any(String.class))).thenReturn("");
		
		when(monitor.isCanceled()).thenReturn(true);
		
		mockedView.runTests(project, testResources, testsInJson, totalTestMethods, isAsync, isDebugging, monitor);
		
		verify(mockedView, times(1)).getUserId(any(String.class));
		verify(mockedView, times(1)).insertTraceFlag(any(LogInfo[].class), any(String.class));
		verify(mockedView, never()).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class));
		verify(mockedView, never()).getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class));
		verify(mockedView, never()).processAsyncTestResults(any(IProject.class), any(Map.class), any(List.class));
		verify(mockedView, never()).displayAsyncCodeCoverage();
		verify(mockedView, never()).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		verify(mockedView, never()).processSyncTestResults(eq(project), eq(testResources), any(RunTestsSyncResponse.class));
		verify(mockedView, never()).displaySyncCodeCoverage(any(RunTestsSyncResponse.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRunTestsEmptyResponseFromServer() {
		IProject project = mock(IProject.class);
		Map<IResource, List<String>> testResources = new HashMap<IResource, List<String>>();
		String testsInJson = "";
		int totalTestMethods = 0;
		boolean isAsync = true;
		boolean isDebugging = false;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		doCallRealMethod().when(mockedView).runTests(eq(project), eq(testResources), eq(testsInJson), 
				eq(totalTestMethods), eq(isAsync), eq(isDebugging), eq(monitor));
		doNothing().when(mockedView).prepareForRunningTests();
		ForceProject fp = mock(ForceProject.class);
		when(fp.getUserName()).thenReturn("");
		when(mockedView.materializeForceProject(project)).thenReturn(fp);
		when(mockedView.getUserId(any(String.class))).thenReturn("");
		when(mockedView.insertTraceFlag(any(LogInfo[].class), any(String.class))).thenReturn("");
		
		when(monitor.isCanceled()).thenReturn(false);
		
		when(mockedView.enqueueTests(testsInJson, isAsync, isDebugging)).thenReturn("");
		
		mockedView.runTests(project, testResources, testsInJson, totalTestMethods, isAsync, isDebugging, monitor);
		
		verify(mockedView, times(1)).getUserId(any(String.class));
		verify(mockedView, times(1)).insertTraceFlag(any(LogInfo[].class), any(String.class));
		verify(mockedView, times(1)).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class));
		verify(mockedView, never()).getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class));
		verify(mockedView, never()).processAsyncTestResults(any(IProject.class), any(Map.class), any(List.class));
		verify(mockedView, never()).displayAsyncCodeCoverage();
		verify(mockedView, never()).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		verify(mockedView, never()).processSyncTestResults(eq(project), eq(testResources), any(RunTestsSyncResponse.class));
		verify(mockedView, never()).displaySyncCodeCoverage(any(RunTestsSyncResponse.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRunTestsAsync() {
		IProject project = mock(IProject.class);
		Map<IResource, List<String>> testResources = new HashMap<IResource, List<String>>();
		String testsInJson = "";
		int totalTestMethods = 0;
		boolean isAsync = true;
		boolean isDebugging = false;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		doCallRealMethod().when(mockedView).runTests(eq(project), eq(testResources), eq(testsInJson), 
				eq(totalTestMethods), eq(isAsync), eq(isDebugging), eq(monitor));
		doNothing().when(mockedView).prepareForRunningTests();
		ForceProject fp = mock(ForceProject.class);
		when(fp.getUserName()).thenReturn("");
		when(mockedView.materializeForceProject(project)).thenReturn(fp);
		when(mockedView.getUserId(any(String.class))).thenReturn("");
		when(mockedView.insertTraceFlag(any(LogInfo[].class), any(String.class))).thenReturn("");
		
		when(monitor.isCanceled()).thenReturn(false);
		
		when(mockedView.enqueueTests(testsInJson, isAsync, isDebugging)).thenReturn("Amazing");
		
		when(mockedView.getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class))).thenReturn(null);
		doNothing().when(mockedView).processAsyncTestResults(any(IProject.class), any(Map.class), any(List.class));
		doNothing().when(mockedView).displayAsyncCodeCoverage();
		
		mockedView.runTests(project, testResources, testsInJson, totalTestMethods, isAsync, isDebugging, monitor);
		
		verify(mockedView, times(1)).getUserId(any(String.class));
		verify(mockedView, times(1)).insertTraceFlag(any(LogInfo[].class), any(String.class));
		verify(mockedView, times(1)).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class));
		verify(mockedView, times(1)).getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class));
		verify(mockedView, times(1)).processAsyncTestResults(any(IProject.class), any(Map.class), any(List.class));
		verify(mockedView, times(1)).displayAsyncCodeCoverage();
		verify(mockedView, never()).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		verify(mockedView, never()).processSyncTestResults(eq(project), eq(testResources), any(RunTestsSyncResponse.class));
		verify(mockedView, never()).displaySyncCodeCoverage(any(RunTestsSyncResponse.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRunTestsSync() {
		IProject project = mock(IProject.class);
		Map<IResource, List<String>> testResources = new HashMap<IResource, List<String>>();
		String testsInJson = "";
		int totalTestMethods = 0;
		boolean isAsync = false;
		boolean isDebugging = false;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		
		doCallRealMethod().when(mockedView).runTests(eq(project), eq(testResources), eq(testsInJson), 
				eq(totalTestMethods), eq(isAsync), eq(isDebugging), eq(monitor));
		doNothing().when(mockedView).prepareForRunningTests();
		ForceProject fp = mock(ForceProject.class);
		when(fp.getUserName()).thenReturn("");
		when(mockedView.materializeForceProject(project)).thenReturn(fp);
		when(mockedView.getUserId(any(String.class))).thenReturn("");
		when(mockedView.insertTraceFlag(any(LogInfo[].class), any(String.class))).thenReturn("");
		
		when(monitor.isCanceled()).thenReturn(false);
		
		when(mockedView.enqueueTests(testsInJson, isAsync, isDebugging)).thenReturn("{}");
		
		doNothing().when(mockedView).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		doNothing().when(mockedView).processSyncTestResults(eq(project), eq(testResources), any(RunTestsSyncResponse.class));
		doNothing().when(mockedView).displaySyncCodeCoverage(any(RunTestsSyncResponse.class));
		
		mockedView.runTests(project, testResources, testsInJson, totalTestMethods, isAsync, isDebugging, monitor);
		
		verify(mockedView, times(1)).getUserId(any(String.class));
		verify(mockedView, times(1)).insertTraceFlag(any(LogInfo[].class), any(String.class));
		verify(mockedView, times(1)).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class));
		verify(mockedView, never()).getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class));
		verify(mockedView, never()).processAsyncTestResults(any(IProject.class), any(Map.class), any(List.class));
		verify(mockedView, never()).displayAsyncCodeCoverage();
		verify(mockedView, times(1)).updateProgress(any(Integer.class), any(Integer.class), any(Integer.class));
		verify(mockedView, times(1)).processSyncTestResults(eq(project), eq(testResources), any(RunTestsSyncResponse.class));
		verify(mockedView, times(1)).displaySyncCodeCoverage(any(RunTestsSyncResponse.class));
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
	public void testGetUserIdWithNullUsername() {
		doCallRealMethod().when(mockedView).getUserId(any(String.class));
		
		assertNull(mockedView.getUserId(null));
	}
	
	@Test
	public void testGetUserIdWithEmptyUsername() {
		doCallRealMethod().when(mockedView).getUserId(any(String.class));
		
		assertNull(mockedView.getUserId(""));
	}
	
	@Test
	public void testGetUserIdNullQueryResult() throws Exception {
		doCallRealMethod().when(mockedView).getUserId(any(String.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		when(mockedView.toolingStubExt.query(any(String.class))).thenReturn(null);
		
		assertNull(mockedView.getUserId("Me"));
	}
	
	@Test
	public void testGetUserIdEmptyQueryResult() throws Exception {
		doCallRealMethod().when(mockedView).getUserId(any(String.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		QueryResult qr = mock(QueryResult.class);
		when(qr.getSize()).thenReturn(0);
		when(mockedView.toolingStubExt.query(any(String.class))).thenReturn(qr);
		
		assertNull(mockedView.getUserId("Me"));
	}
	
	@Test
	public void testGetUserId() throws Exception {
		doCallRealMethod().when(mockedView).getUserId(any(String.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		SObject sObj = mock(SObject.class);
		String expectedId = "123";
		when(sObj.getId()).thenReturn(expectedId);
		
		QueryResult qr = mock(QueryResult.class);
		when(qr.getSize()).thenReturn(1);
		when(qr.getRecords()).thenReturn(new SObject[] { sObj });
		
		when(mockedView.toolingStubExt.query(any(String.class))).thenReturn(qr);
		
		assertEquals(expectedId, mockedView.getUserId("Me"));
	}
	
	@Test
	public void testDeleteTraceFlagNullTfId() {
		doCallRealMethod().when(mockedView).deleteTraceFlag(any(String.class));
		
		assertFalse(mockedView.deleteTraceFlag(null));
	}
	
	@Test
	public void testDeleteTraceFlagEmptyTfId() {
		doCallRealMethod().when(mockedView).deleteTraceFlag(any(String.class));
		
		assertFalse(mockedView.deleteTraceFlag(""));
	}
	
	@Test
	public void testDeleteTraceFlagNullQueryResult() throws Exception {
		doCallRealMethod().when(mockedView).deleteTraceFlag(any(String.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		when(mockedView.toolingStubExt.delete(any(String[].class))).thenReturn(null);
		
		assertFalse(mockedView.deleteTraceFlag("123"));
	}
	
	@Test
	public void testDeleteTraceFlagEmptyQueryResult() throws Exception {
		doCallRealMethod().when(mockedView).deleteTraceFlag(any(String.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		DeleteResult[] dr = new DeleteResult[0];
		when(mockedView.toolingStubExt.delete(any(String[].class))).thenReturn(dr);
		
		assertFalse(mockedView.deleteTraceFlag("123"));
	}
	
	@Test
	public void testDeleteTraceFlagFail() throws Exception {
		doCallRealMethod().when(mockedView).deleteTraceFlag(any(String.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		DeleteResult dr = mock(DeleteResult.class);
		when(dr.isSuccess()).thenReturn(false);
		
		when(mockedView.toolingStubExt.delete(any(String[].class))).thenReturn(new DeleteResult[] { dr });
		
		assertFalse(mockedView.deleteTraceFlag("123"));
	}
	
	@Test
	public void testDeleteTraceFlagSuccess() throws Exception {
		doCallRealMethod().when(mockedView).deleteTraceFlag(any(String.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		DeleteResult dr = mock(DeleteResult.class);
		when(dr.isSuccess()).thenReturn(true);
		
		when(mockedView.toolingStubExt.delete(any(String[].class))).thenReturn(new DeleteResult[] { dr });
		
		assertTrue(mockedView.deleteTraceFlag("123"));
	}
	
	@Test
	public void testEnqueueTestsWithNullProject() {
		doCallRealMethod().when(mockedView).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class));
		mockedView.forceProject = null;
		
		assertNull(mockedView.enqueueTests("", true, false));
	}
	
	@Test
	public void testEnqueueTests() throws Exception {
		doCallRealMethod().when(mockedView).enqueueTests(any(String.class), any(Boolean.class), any(Boolean.class));
		doCallRealMethod().when(mockedView).getConnTimeoutVal(any(Boolean.class), any(Boolean.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class), any(Integer.class));
		
		String testsInJson = "{}";
		boolean isAsync = false;
		boolean isDebugging = true;
		String response = "tests";
		@SuppressWarnings("unchecked")
		PromiseableJob<String> job = (PromiseableJob<String>) mock(PromiseableJob.class);
		when(job.getAnswer()).thenReturn(response);
		when(mockedView.getRunTestsCommand(testsInJson, isAsync)).thenReturn(job);
		
		assertEquals(response, mockedView.enqueueTests(testsInJson, isAsync, isDebugging));
		
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
	public void testGetTestResultsNullTestRunId() {
		doCallRealMethod().when(mockedView).getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class));
		
		List<ApexTestResult> testResults = mockedView.getTestResults(null, 0, mock(IProgressMonitor.class));
		
		assertNotNull(testResults);
		assertEquals(0, testResults.size());
	}
	
	@Test
	public void testGetTestResultsEmptyTestRunId() {
		doCallRealMethod().when(mockedView).getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class));
		
		List<ApexTestResult> testResults = mockedView.getTestResults("", 0, mock(IProgressMonitor.class));
		
		assertNotNull(testResults);
		assertEquals(0, testResults.size());
	}
	
	@Test
	public void testGetTestResultsNullApiLimit() throws Exception {
		doCallRealMethod().when(mockedView).getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		when(mockedView.getApiLimit(any(ForceProject.class), eq(LimitsCommand.Type.DailyApiRequests))).thenReturn(null);
		
		List<ApexTestResult> testResults = mockedView.getTestResults("123", 0, mock(IProgressMonitor.class));
		
		assertNotNull(testResults);
		assertEquals(0, testResults.size());
	}
	
	@Test
	public void testGetTestResultsNotEnoughApiRequestsRemaining() throws Exception {
		doCallRealMethod().when(mockedView).getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		Limit dailyRemaining = mock(Limit.class);
		when(dailyRemaining.getRemaining()).thenReturn(0);
		when(dailyRemaining.getMax()).thenReturn(10);
		when(mockedView.getApiLimit(any(ForceProject.class), eq(LimitsCommand.Type.DailyApiRequests))).thenReturn(dailyRemaining);
		
		List<ApexTestResult> testResults = mockedView.getTestResults("123", 0, mock(IProgressMonitor.class));
		
		assertNotNull(testResults);
		assertEquals(0, testResults.size());
	}
	
	@Test
	public void testGetTestResultsAborted() throws Exception {
		doCallRealMethod().when(mockedView).getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		Limit dailyRemaining = mock(Limit.class);
		when(dailyRemaining.getRemaining()).thenReturn(5);
		when(dailyRemaining.getMax()).thenReturn(10);
		when(mockedView.getApiLimit(any(ForceProject.class), eq(LimitsCommand.Type.DailyApiRequests))).thenReturn(dailyRemaining);
		
		String testRunId = "123";
		int totalTestMethods = 5;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		when(monitor.isCanceled()).thenReturn(true);
		when(mockedView.abortTestRun(testRunId)).thenReturn(true);
		
		when(mockedView.toolingStubExt.query(String.format(RunTestsConstants.QUERY_TESTRESULT_COUNT, testRunId))).thenReturn(null);
		when(mockedView.toolingStubExt.query(String.format(RunTestsConstants.QUERY_TESTRESULT, testRunId))).thenReturn(null);
		
		List<ApexTestResult> testResults = mockedView.getTestResults(testRunId, totalTestMethods, monitor);
		
		assertNotNull(testResults);
		assertEquals(0, testResults.size());
		verify(mockedView, times(1)).abortTestRun(testRunId);
		verify(mockedView, never()).getPollInterval(any(Integer.class), any(Float.class));
	}
	
	@Test
	public void testGetTestResults() throws Exception {
		doCallRealMethod().when(mockedView).getTestResults(any(String.class), any(Integer.class), any(IProgressMonitor.class));
		doNothing().when(mockedView).initializeConnection(any(ForceProject.class));
		
		Limit dailyRemaining = mock(Limit.class);
		when(dailyRemaining.getRemaining()).thenReturn(5);
		when(dailyRemaining.getMax()).thenReturn(10);
		when(mockedView.getApiLimit(any(ForceProject.class), eq(LimitsCommand.Type.DailyApiRequests))).thenReturn(dailyRemaining);
		
		String testRunId = "123";
		int totalTestMethods = 5;
		int totalTestDone = totalTestMethods;
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		when(monitor.isCanceled()).thenReturn(false);
		
		AggregateResult ar = mock(AggregateResult.class);
		when(ar.getField("expr0")).thenReturn(totalTestDone);
		QueryResult qr1 = mock(QueryResult.class);
		when(qr1.getSize()).thenReturn(1);
		when(qr1.getRecords()).thenReturn(new SObject[] { ar });
		when(mockedView.toolingStubExt.query(String.format(RunTestsConstants.QUERY_TESTRESULT_COUNT, testRunId))).thenReturn(qr1);
		
		doNothing().when(mockedView).updateProgress(eq(0), eq(totalTestMethods), eq(totalTestDone));
		
		when(mockedView.getPollInterval(any(Integer.class), any(Float.class))).thenReturn(0);
		
		ApexTestResult tr = mock(ApexTestResult.class);
		QueryResult qr2 = mock(QueryResult.class);
		when(qr2.getSize()).thenReturn(1);
		when(qr2.getRecords()).thenReturn(new SObject[] { tr });
		when(mockedView.toolingStubExt.query(String.format(RunTestsConstants.QUERY_TESTRESULT, testRunId))).thenReturn(qr2);
		
		List<ApexTestResult> testResults = mockedView.getTestResults(testRunId, totalTestMethods, monitor);
		
		assertNotNull(testResults);
		assertEquals(1, testResults.size());
		assertEquals(tr, testResults.get(0));
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
