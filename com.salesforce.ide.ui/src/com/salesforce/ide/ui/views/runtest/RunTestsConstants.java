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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import com.salesforce.ide.ui.internal.ForceImages;

public final class RunTestsConstants {
	
	private RunTestsConstants() {
	}

	public static final String TOOLING_ENDPOINT = "/services/data/";
	
	public static final String NEW_LINE = System.getProperty("line.separator");
	
	public static final int ASYNC_TIMEOUT = 20_000; // 20 seconds
	public static final int SYNC_WITHOUT_DEBUG_TIMEOUT = 120_000; // 2 minutes
	public static final int SYNC_WITH_DEBUG_TIMEOUT = 3_600_000; // 1 hour
	
	// Launch config attributes
	public static final String ATTR_PROJECT_NAME = "forceComProjectName";
	public static final String ATTR_TESTS_ARRAY = "testsArray";
	public static final String ATTR_TEST_MODE = "isAsync";
	public static final String ATTR_TEST_CLASS = "forceComTestClass";
	public static final String ATTR_TEST_METHOD = "forceComTestMethod";
	public static final String ATTR_ENABLE_LOGGING = "enableLogging";
	public static final String ATTR_EXISTING_TF = "existingTf";
	public static final String ATTR_LOG_LEVELS = "logLevels";
	public static final String ATTR_USE_SUITES = "useLogging";
	public static final String ATTR_SUITES = "suiteJson";
	public static final String ATTR_SUITE_IDS = "suiteIds";
	
    // Keys used to store data in Eclipse widgets
    public static final String TREEDATA_CODE_LOCATION = "ApexCodeLocation";
    public static final String TREEDATA_APEX_LOG = "ApexLog";
    public static final String TREEDATA_APEX_LOG_ID = "ApexLog";
    public static final String TREEDATA_APEX_LOG_USER_DEBUG = "ApexLogUserDebug";
    public static final String TREEDATA_APEX_LOG_BODY = "ApexLogBody";
    public static final String TREEDATA_TEST_METHOD_LOCS = "ApexTestMethodCodeLocations";
    public static final String TREEDATA_RESULT_MESSAGE = "ApexTestResultMessage";
    public static final String TREEDATA_RESULT_STACKTRACE = "ApexTestResultStacktrace";
    public static final String TABLE_CODE_COV_RESULT = "AllCodeCov";
    public static final String TABLE_CODE_COV_COL_DIR = "OneColumnDirection";
    
    // Tooling API queries
    public static final String QUERY_TESTRESULT = "SELECT Id, ApexClassId, ApexClass.Name, ApexLogId, AsyncApexJobId,"
    		+ "Message, MethodName, Outcome, QueueItemId, StackTrace, TestTimestamp "
			+ "FROM ApexTestResult WHERE AsyncApexJobId = '%s' ORDER BY ApexClass.Name, MethodName ASC";
    public static final String QUERY_APEX_LOG = "SELECT Id, Application, DurationMilliseconds, Location, LogLength, LogUserId, Operation, Request, StartTime, Status FROM ApexLog WHERE Id = '%s'";
    public static final String QUERY_APEX_TEST_QUEUE_ITEM_PROCESSED_COUNT = "SELECT COUNT(Id) total FROM ApexTestQueueItem WHERE Status IN ('Completed', 'Failed', 'Aborted') AND ParentJobId = '%s'";
    public static final String QUERY_APEX_TEST_QUEUE_ITEM_COUNT = "SELECT COUNT(Id) total FROM ApexTestQueueItem WHERE ParentJobId = '%s'";
    public static final String QUERY_APEX_TEST_QUEUE_ITEM = "SELECT Id, Status FROM ApexTestQueueItem WHERE ParentJobId = '%s'";
    public static final String QUERY_APEX_CODE_COVERAGE_AGG = "SELECT ApexClassOrTriggerId, ApexClassOrTrigger.Name, "
			+ "NumLinesCovered, NumLinesUncovered, Coverage FROM ApexCodeCoverageAggregate "
			+ "WHERE ApexClassOrTriggerId != NULL AND ApexClassOrTrigger.Name != NULL "
			+ "AND NumLinesCovered != NULL AND NumLinesUncovered != NULL "
			+ "AND (NumLinesCovered > 0 OR NumLinesUncovered > 0) "
			+ "ORDER BY ApexClassOrTrigger.Name";
    public static final String QUERY_APEX_ORG_WIDE_COVERAGE = "SELECT PercentCovered FROM ApexOrgWideCoverage";
    public static final String QUERY_TEST_SUITE_MEMBERSHIP = "SELECT ApexClassId, ApexTestSuiteId FROM TestSuiteMembership";
    
    // Poll intervals
    public static final int POLL_FAST = 5000;
    public static final int POLL_MED = 10000;
    public static final int POLL_SLOW = 15000;
    
    // Images
    public static final Image FAILURE_ICON = ForceImages.get(ForceImages.IMAGE_FAILURE);
    public static final Image WARNING_ICON = ForceImages.get(ForceImages.IMAGE_WARNING);
    public static final Image PASS_ICON = ForceImages.get(ForceImages.IMAGE_CONFIRM);
    
    // Colors
    public static final int FAILURE_COLOR = SWT.COLOR_RED;
    public static final int WARNING_COLOR = SWT.COLOR_DARK_YELLOW;
    public static final int PASS_COLOR = SWT.COLOR_DARK_GREEN;
    
    // TraceFlags
    public static final String DEBUG_LEVEL_NAME = "SFDC_Eclipse_ApexTest_";
    public static final int TF_LENGTH_MINS = 5;
    public static final int TF_INTERVAL_MINS = 4;
}
