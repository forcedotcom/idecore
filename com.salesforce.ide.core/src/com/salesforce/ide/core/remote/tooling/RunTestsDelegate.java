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

package com.salesforce.ide.core.remote.tooling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.HTTPAdapter;
import com.salesforce.ide.core.remote.HTTPAdapter.HTTPMethod;
import com.salesforce.ide.core.remote.HTTPConnection;
import com.salesforce.ide.core.remote.PromiseableJob;
import com.salesforce.ide.core.remote.ToolingStubExt;
import com.salesforce.ide.core.services.BaseService;
import com.sforce.soap.metadata.LogCategory;
import com.sforce.soap.metadata.LogCategoryLevel;
import com.sforce.soap.metadata.LogInfo;
import com.sforce.soap.tooling.AggregateResult;
import com.sforce.soap.tooling.ApexLog;
import com.sforce.soap.tooling.ApexLogLevel;
import com.sforce.soap.tooling.ApexTestResult;
import com.sforce.soap.tooling.DeleteResult;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.SObject;
import com.sforce.soap.tooling.SaveResult;
import com.sforce.soap.tooling.TraceFlag;

/**
 * Handles execution, test results, and Apex Log retrieval of Apex Tests.
 * This delegate is for Tooling API's runTestsAsynchronous.
 * 
 * @author jwidjaja
 *
 */
public class RunTestsDelegate extends BaseService {
	
	private final Logger logger = Logger.getLogger(RunTestsDelegate.class);
	
	private final String TOOLING_ENDPOINT = "/services/data/";
	
	private final String QUERY_USER_ID = "SELECT Id, Username FROM User WHERE Username = '%s'";
	private final String QUERY_TESTRESULT_COUNT = "SELECT COUNT(Id) FROM ApexTestResult WHERE AsyncApexJobId = '%s'";
	private final String QUERY_TESTRESULT = "SELECT ApexClassId, ApexLogId, AsyncApexJobId, Message, "
			+ "MethodName, Outcome, QueueItemId, StackTrace, TestTimestamp "
			+ "FROM ApexTestResult WHERE AsyncApexJobId = '%s'";
	private final String QUERY_APEX_LOG = "SELECT Id, Application, DurationMilliseconds, Location, LogLength, LogUserId, Operation, Request, StartTime, Status FROM ApexLog WHERE Id = '%s'";
	
	private final int POLL_FAST = 5000;
	private final int POLL_MED = 10000;
	private final int POLL_SLOW = 15000;
	
	private final int totalTestMethods;
	
	private HTTPConnection toolingRESTConnection;
	private ToolingStubExt toolingStubExt;
	
	/**
	 * This delegate assumes you've calculated the total test methods in the desired
	 * test run. It uses this number when retrieving test results.
	 * 
	 * @param totalTestMethods
	 */
	public RunTestsDelegate(int totalTestMethods) {
		this.totalTestMethods = (totalTestMethods > 0 ? totalTestMethods : 0);
	}
	
	/**
	 * @return Total number of test methods given to the constructor.
	 */
	public int getTotalTestMethods() {
		return totalTestMethods;
	}
	
	/**
	 * Queries the user ID based on the given user name.
	 * @param forceProject
	 * @param userName
	 * @return User ID if valid. Null otherwise.
	 */
	public String getUserId(ForceProject forceProject, String userName) {
		String userId = null;
		
		if (Utils.isEmpty(userName)) {
			return userId;
		}
		
		try {
			initializeConnection(forceProject);
			
			QueryResult qr = toolingStubExt.query(String.format(QUERY_USER_ID, userName));
			if (qr != null && qr.getSize() > 0) {
				return qr.getRecords()[0].getId();
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
		
		return userId;
	}
	
	/**
	 * Inserts a TraceFlag. The userId is used for the TraceFlag's entity ID and scope ID.
	 * @param forceProject
	 * @param logInfos
	 * @param userId
	 * @return ID of the TraceFlag
	 */
	public String insertTraceFlag(ForceProject forceProject, LogInfo[] logInfos, String userId) {
		String traceFlagId = null;
		
		if (Utils.isEmpty(logInfos) || Utils.isEmpty(userId)) {
			return traceFlagId;
		}
		
		try {
			initializeConnection(forceProject);
			
			TraceFlag tf = new TraceFlag();
			tf.setTracedEntityId(userId);
			tf.setScopeId(userId);
			
			for (LogInfo logInfo : logInfos) {
				// Translate Metadata's LogInfo into Tooling's ApexLogLevel and LogCategory
				LogCategory logCategory = logInfo.getCategory();
				LogCategoryLevel metadataLogLevel = logInfo.getLevel();
				
				ApexLogLevel toolingLogLevel = translateLogLevel(metadataLogLevel);
				
				if (logCategory.equals(LogCategory.Apex_code)) {
					tf.setApexCode(toolingLogLevel);
				} else if (logCategory.equals(LogCategory.Apex_profiling)) {
					tf.setApexProfiling(toolingLogLevel);
				} else if (logCategory.equals(LogCategory.Callout)) {
					tf.setCallout(toolingLogLevel);
				} else if (logCategory.equals(LogCategory.Db)) {
					tf.setDatabase(toolingLogLevel);
				} else if (logCategory.equals(LogCategory.System)) {
					tf.setSystem(toolingLogLevel);
				} else if (logCategory.equals(LogCategory.Validation)) {
					tf.setValidation(toolingLogLevel);
				} else if (logCategory.equals(LogCategory.Visualforce)) {
					tf.setVisualforce(toolingLogLevel);
				} else if (logCategory.equals(LogCategory.Workflow)) {
					tf.setWorkflow(toolingLogLevel);
				}
			}
			
			SaveResult[] sr = toolingStubExt.create(new SObject[] { tf });
			if (sr != null && sr.length > 0) {
				traceFlagId = sr[0].getId();
				if (sr[0].isSuccess()) {
					logger.debug(String.format("Created TraceFlag %s", traceFlagId));
				} else {
					logger.warn(sr[0].getErrors().toString());
				}
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
		
		return traceFlagId;
	}
	
	/**
	 * Delete a TraceFlag.
	 * @param forceProject
	 * @param traceFlagId
	 */
	public void deleteTraceFlag(ForceProject forceProject, String traceFlagId) {
		if (Utils.isEmpty(traceFlagId)) {
			return;
		}
		
		try {
			initializeConnection(forceProject);
			
			DeleteResult[] dr = toolingStubExt.delete(new String[] { traceFlagId });
			if (dr != null && dr.length > 0) {
				boolean deleteSuccess = dr[0].isSuccess();
				if (deleteSuccess) {
					logger.debug(String.format("Deleted TraceFlag %s", traceFlagId));
				} else {
					logger.warn(String.format("Failed to delete TraceFlag %s", traceFlagId));
				}
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
	}
	
	/**
	 * Translate Metadata's LogCategoryLevel to Tooling's ApexLogLevel.
	 * @param metadataLogLevel
	 * @return The translated log level
	 */
	private ApexLogLevel translateLogLevel(LogCategoryLevel metadataLogLevel) {
		if (metadataLogLevel.equals(LogCategoryLevel.Debug)) {
			return ApexLogLevel.DEBUG;
		} else if (metadataLogLevel.equals(LogCategoryLevel.Error)) {
			return ApexLogLevel.ERROR;
		} else if (metadataLogLevel.equals(LogCategoryLevel.Fine)) {
			return ApexLogLevel.FINE;
		} else if (metadataLogLevel.equals(LogCategoryLevel.Finer)) {
			return ApexLogLevel.FINER;
		} else if (metadataLogLevel.equals(LogCategoryLevel.Finest)) {
			return ApexLogLevel.FINEST;
		} else if (metadataLogLevel.equals(LogCategoryLevel.Info)) {
			return ApexLogLevel.INFO;
		} else if (metadataLogLevel.equals(LogCategoryLevel.Warn)) {
			return ApexLogLevel.WARN;
		} else {
			return ApexLogLevel.NONE;
		}
	}
	
	/**
	 * Enqueue a tests array to Tooling's runTestsAsynchronous.
	 * @param forceProject
	 * @param testsInJson
	 * @return The test run ID if valid. Null otherwise.
	 */
	public String enqueueTests(ForceProject forceProject, String testsInJson) {
		String response = null;
		
		try {
			initializeConnection(forceProject);
			
			PromiseableJob<String> job = new RunTestsCommand(new HTTPAdapter<>(
					String.class, new RunTestsTransport(toolingRESTConnection), HTTPMethod.POST), testsInJson);
			job.schedule();
			
			try {
				job.join();
				response = job.getAnswer();
			} catch (InterruptedException e) {
				logger.error("Failed to enqueue test run", e);
			}
			
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
		
		return response;
	}
	
	/**
	 * Retrieve test results for the given test run ID.
	 * @param forceProject
	 * @param testRunId
	 * @return A list of ApexTestResult, if any.
	 */
	public List<ApexTestResult> getTestResults(ForceProject forceProject, String testRunId)  {
		List<ApexTestResult> testResults = new ArrayList<ApexTestResult>();
		if (forceProject == null || testRunId == null) return testResults;
		
		try {
			initializeConnection(forceProject);
			
			// Get remaining daily API requests
			Limit dailyApiRequests = getApiLimit(forceProject, LimitsCommand.Type.DailyApiRequests);
			if (dailyApiRequests == null) {
				return testResults;
			}
			
			float apiRequestsRemaining = (dailyApiRequests.getRemaining() * 100.0f) / dailyApiRequests.getMax();
			if (apiRequestsRemaining <= 0) {
				return testResults;
			}
			
			// Poll for remaining test cases to be executed
			int totalTestDone = 0;
			QueryResult qr = null;
			// No timeout here because we don't know how long a test run can be.
			// If user wants to exit, then they can cancel the launch config.
			while (totalTestDone < totalTestMethods) {
				// Wait according to the interval
				int wait = getPollInterval(totalTestMethods - totalTestDone, apiRequestsRemaining);
				Thread.sleep(wait);
				
				// Query for number of finished tests in specified test run
				qr = toolingStubExt.query(String.format(QUERY_TESTRESULT_COUNT, testRunId));
				
				// Update finished test counter
				if (qr.getSize() == 1) {
					SObject sObj = qr.getRecords()[0];
					if (sObj instanceof AggregateResult) {
						AggregateResult aggRes = (AggregateResult) sObj;
						Object expr0 = aggRes.getField("expr0");
						int updatedTestDone = (int) expr0;
						totalTestDone = updatedTestDone;
					}
				}
			}
			
			// Get all test results in the specified test run
			qr = toolingStubExt.query(String.format(QUERY_TESTRESULT, testRunId));
			if (qr != null && qr.getSize() > 0) {
				for (SObject sObj : qr.getRecords()) {
					ApexTestResult testResult = (ApexTestResult) sObj;
					testResults.add(testResult);
				}
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		} catch (InterruptedException e) {
			logger.error("Getting test results was interrupted", e);
		} catch (Exception e) {
			logger.error("Something unexpected with getting Apex Test results", e);
		}
		
		return testResults;
	}
	
	/**
	 * Get a specific API Limit
	 * @param forceProject
	 * @param type
	 * @return Limit
	 * @see Limit.java
	 */
	public Limit getApiLimit(ForceProject forceProject, LimitsCommand.Type type) {
		try {
			initializeConnection(forceProject);
			
			PromiseableJob<Map<String, Limit>> job = new LimitsCommand(new HTTPAdapter<>(
					String.class, new LimitsTransport(toolingRESTConnection), HTTPMethod.GET));
			job.schedule();
			
			try {
				job.join();
				Map<String, Limit> limits = job.getAnswer();
				if (limits != null && limits.size() > 0) {
					return limits.get(type.toString());
				}
			} catch (InterruptedException e) {
				logger.error("Failed to enqueue test run", e);
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
		
		return null;
	}
	
	/**
	 * Get the appropriate poll interval depending on the number of tests remaining
	 * and the number of API requests remaining. The higher the number of tests remaining, the slower
	 * we should poll. The higher the number of remaining API requests, the faster we should poll.
	 * @param totalTestRemaining
	 * @param apiRequestsRemaining
	 * @return A poll interval
	 */
	public int getPollInterval(int totalTestRemaining, float apiRequestsRemaining) {
		int intervalA = POLL_SLOW, intervalB = POLL_SLOW;
		
		if (totalTestRemaining <= 10) {
			intervalA = POLL_FAST;
		} else if (totalTestRemaining <= 50) {
			intervalA = POLL_MED;
		} else {
			intervalA = POLL_SLOW;
		}
		
		if (apiRequestsRemaining <= 25f) {
			intervalB = POLL_SLOW;
		} else if (apiRequestsRemaining <= 50f) {
			intervalB = POLL_MED;
		} else {
			intervalB = POLL_FAST;
		}
		
		return (intervalA + intervalB) / 2;
	}
	
	/**
	 * Query an ApexLog with the specified log ID.
	 * @param forceProject
	 * @param logId
	 * @return ApexLog
	 */
	public ApexLog getApexLog(ForceProject forceProject, String logId) {		
		try {
			initializeConnection(forceProject);
			
			QueryResult qr = toolingStubExt.query(String.format(QUERY_APEX_LOG, logId));
			if (qr != null && qr.getSize() == 1) {
				ApexLog apexLog = (ApexLog) qr.getRecords()[0];
				return apexLog;
			}
		} catch (ForceRemoteException | ForceConnectionException e) {}
		
		return null;
	}
	
	/**
	 * Fetch the raw body of an ApexLog with the specified log ID.
	 * @param forceProject
	 * @param logId
	 * @return Raw log. Null if something is wrong.
	 */
	public String getApexLogBody(ForceProject forceProject, String logId) {
		String rawLog = null;
		
		try {
			initializeConnection(forceProject);
			
			PromiseableJob<String> job = new ApexLogCommand(new HTTPAdapter<>(
					String.class, new ApexLogTransport(toolingRESTConnection, logId), HTTPMethod.GET));
			job.schedule();
			
			try {
				job.join();
				rawLog = job.getAnswer();
			} catch (InterruptedException e) {
				logger.error("Failed to get Apex Log", e);
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
		
		return rawLog;
	}
	
	/**
	 * Get a ForceProject from an IProject.
	 * @param project
	 * @return ForceProject
	 */
	public ForceProject materializeForceProject(IProject project) {
		if (project == null || !project.exists())
            return null;

        ForceProject forceProject =
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProject(project);
        return forceProject;
	}
	
	/**
	 * Initialize Tooling connection.
	 * @param forceProject
	 * @throws ForceConnectionException
	 * @throws ForceRemoteException
	 */
	private void initializeConnection(ForceProject forceProject) throws ForceConnectionException, ForceRemoteException {
		if (toolingRESTConnection != null && toolingStubExt != null) return;
		
		toolingRESTConnection = new HTTPConnection(forceProject, TOOLING_ENDPOINT);
        toolingRESTConnection.initialize();
        toolingStubExt = ContainerDelegate.getInstance().getFactoryLocator().getToolingFactory().getToolingStubExt(forceProject);
	}
}
