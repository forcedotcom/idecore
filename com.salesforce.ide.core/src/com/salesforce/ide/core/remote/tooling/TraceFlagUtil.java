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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.HTTPConnection;
import com.salesforce.ide.core.remote.ToolingStubExt;
import com.sforce.soap.tooling.ApexLogLevel;
import com.sforce.soap.tooling.sobject.DebugLevel;
import com.sforce.soap.tooling.DeleteResult;
import com.sforce.soap.tooling.LogCategory;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.sobject.SObject;
import com.sforce.soap.tooling.SaveResult;
import com.sforce.soap.tooling.sobject.TraceFlag;
import com.sforce.soap.tooling.TraceFlagType;

/**
 * Utility class for TraceFlag related operations.
 * 
 * @author jwidjaja
 *
 */
public class TraceFlagUtil {
	private static final Logger logger = Logger.getLogger(TraceFlagUtil.class);
	
	private static final String TOOLING_ENDPOINT = "/services/data/";
	
	private static final String QUERY_USER_ID = "SELECT Id, Username FROM User WHERE Username = '%s'";
	private static final String QUERY_TRACEFLAG_BY_ID = "SELECT Id, ExpirationDate FROM TraceFlag WHERE Id = '%s'";
	private static final String QUERY_TRACEFLAG_BY_TYPE = "SELECT Id, LogType, StartDate, ExpirationDate FROM TraceFlag "
			+ "WHERE LogType = 'DEVELOPER_LOG' AND (StartDate = NULL OR StartDate <= %s) AND ExpirationDate >= %s LIMIT 1";
	
	private final ForceProject forceProject;
	private final LinkedHashMap<String, TraceFlagRenewer> traceFlagJobs;
	
	private HTTPConnection toolingRESTConnection = null;
	private ToolingStubExt toolingStubExt = null;
	
	public TraceFlagUtil(ForceProject forceProject) {
		this.forceProject = forceProject;
		traceFlagJobs = new LinkedHashMap<String, TraceFlagRenewer>();
	}
	
	/**
	 * Queries the user ID based on the given user name.
	 * @param userName
	 * @return User ID if valid. Null otherwise.
	 */
	public String getUserId(String userName) {
		String userId = null;
		
		if (Utils.isEmpty(forceProject) || Utils.isEmpty(userName)) {
			return userId;
		}
		
		try {
			initializeConnection(forceProject);
			
			QueryResult qr = toolingStubExt.query(String.format(QUERY_USER_ID, userName));
			if (qr != null && qr.getTotalSize() == 1) {
				userId = qr.getRecords()[0].getId();
			} else {
				logger.error("QueryResult is empty");
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
		
		return userId;
	}
	
	/**
	 * Insert a DebugLevel
	 * @param debugLevelName
	 * @param logLevels
	 * @return ID of the DebugLevel
	 */
	public String insertDebugLevel(String debugLevelName, Map<LogCategory, ApexLogLevel> logLevels) {
		String debugLevelId = null;
		
		if (Utils.isEmpty(forceProject) || Utils.isEmpty(debugLevelName) || Utils.isEmpty(logLevels)) {
			return debugLevelId;
		}
		
		try {
			initializeConnection(forceProject);
			
			// Set the DeveloperName, MasterLabel, & log levels
			DebugLevel dl = new DebugLevel();
			dl.setDeveloperName(debugLevelName);
			dl.setMasterLabel(debugLevelName);
			dl.setDatabase(logLevels.get(LogCategory.Db));
			dl.setWorkflow(logLevels.get(LogCategory.Workflow));
			dl.setValidation(logLevels.get(LogCategory.Validation));
			dl.setCallout(logLevels.get(LogCategory.Callout));
			dl.setApexCode(logLevels.get(LogCategory.Apex_code));
			dl.setApexProfiling(logLevels.get(LogCategory.Apex_profiling));
			dl.setVisualforce(logLevels.get(LogCategory.Visualforce));
			dl.setSystem(logLevels.get(LogCategory.System));
			
			SaveResult[] srs = toolingStubExt.create(new SObject[] { dl });
			if (srs != null && srs.length == 1) {
				if (srs[0].isSuccess()) {
					debugLevelId = srs[0].getId();
					logger.info(String.format("Created DebugLevel %s", debugLevelId));
				} else {
					logger.error(String.format("Failed to create DebugLevel: %s", srs[0].getErrors()[0].toString()));
				}
			} else {
				logger.error("SaveResult is empty");
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
		
		return debugLevelId;
	}
	
	/**
	 * Inserts a User TraceFlag
	 * @param userId
	 * @param lengthInMins
	 * @param debugLevelId
	 * @return ID of the TraceFlag
	 */
	public String insertTraceFlag(String userId, int lengthInMins, String debugLevelId) {
		String traceFlagId = null;
		
		if (Utils.isEmpty(forceProject) || Utils.isEmpty(userId) || lengthInMins <= 0 || Utils.isEmpty(debugLevelId)) {
			return traceFlagId;
		}
		
		try {
			initializeConnection(forceProject);
			
			// Set user ID, start date, expiration date, & DebugLevel
			TraceFlag tf = new TraceFlag();
			tf.setTracedEntityId(userId);
			tf.setLogType(TraceFlagType.DEVELOPER_LOG);
			Calendar start = GregorianCalendar.getInstance();
			Calendar end = GregorianCalendar.getInstance();
			end.add(Calendar.MINUTE, lengthInMins);
			tf.setStartDate(start);
			tf.setExpirationDate(end);
			tf.setDebugLevelId(debugLevelId);
			
			SaveResult[] srs = toolingStubExt.create(new SObject[] { tf });
			if (srs != null && srs.length == 1) {
				if (srs[0].isSuccess()) {
					traceFlagId = srs[0].getId();
					logger.info(String.format("Created TraceFlag %s", traceFlagId));
				} else {
					logger.error(String.format("Failed to create TraceFlag: %s", srs[0].getErrors()[0].toString()));
				}
			} else {
				logger.error("SaveResult[] is empty");
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
		
		return traceFlagId;
	}
	
	/**
	 * Delete a DebugLevel & TraceFlag
	 * @param debugLevelId
	 * @param traceFlagId
	 */
	public void deleteTraceflagAndDebugLevel(String traceFlagId, String debugLevelId) {
		// TraceFlag has to be deleted first because there is a foreign key to DebugLevel
		deleteTraceFlag(traceFlagId);
		deleteDebugLevel(debugLevelId);
	}
	
	/**
	 * Delete a TraceFlag
	 * @param traceFlagId
	 */
	public void deleteTraceFlag(String traceFlagId) {
		if (Utils.isEmpty(forceProject)) {
			return;
		}
		
		try {
			initializeConnection(forceProject);
			
			DeleteResult[] drs = toolingStubExt.delete(new String[] { traceFlagId });
			if (drs != null && drs.length == 1) {
				if (drs[0].isSuccess()) {
					logger.info(String.format("Deleted TraceFlag %s", traceFlagId));
				} else {
					logger.error(String.format("Failed to delete TraceFlag %s: %s", traceFlagId, drs[0].getErrors()[0].toString()));
				}
			} else {
				logger.error("DeleteResult[] is empty");
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
	}
	
	/**
	 * Delete a DebugLevel
	 * @param debugLevelId
	 */
	public void deleteDebugLevel(String debugLevelId) {
		if (Utils.isEmpty(forceProject)) {
			return;
		}
		
		try {
			initializeConnection(forceProject);
			
			DeleteResult[] drs = toolingStubExt.delete(new String[] { debugLevelId });
			if (drs != null && drs.length == 1) {
				if (drs[0].isSuccess()) {
					logger.info(String.format("Deleted DebugLevel %s", debugLevelId));
				} else {
					logger.error(String.format("Failed to delete DebugLevel %s: %s", debugLevelId, drs[0].getErrors()[0].toString()));
				}
			} else {
				logger.error("DeleteResult[] is empty");
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
	}
	
	/**
	 * Check if there is an active Trace Flag
	 * @return True if yes. False otherwise.
	 */
	public boolean hasActiveTraceFlag() {
		if (Utils.isEmpty(forceProject)) {
			return true;
		}
		
		try {
			initializeConnection(forceProject);
			
			SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
			String now = dateFormatGmt.format(new Date());
			QueryResult qr = toolingStubExt.query(String.format(QUERY_TRACEFLAG_BY_TYPE, now, now));
			if (qr != null && qr.getSize() > 0) {
				return true;
			}
		} catch (ForceConnectionException | ForceRemoteException e) {
			logger.error("Failed to connect to Tooling API", e);
		}
		
		return false;
	}
	
	/**
	 * Create a background job to extend expiration of TraceFlag
	 * @param traceFlagId
	 * @param intervalMins
	 * @param additionalMins
	 */
	public void automateTraceFlagExtension(String traceFlagId, int intervalMins, int additionalMins) {
		if (Utils.isEmpty(traceFlagId) || intervalMins <= 0 || additionalMins <= 0) {
			return;
		}
		
		// Cancel and delete existing renewer
		if (traceFlagJobs.containsKey(traceFlagId)) {
			traceFlagJobs.get(traceFlagId).cancel();
			traceFlagJobs.remove(traceFlagId);
		}
		
		// Create the renewer
		TraceFlagRenewer tfRenew = new TraceFlagRenewer(traceFlagId, intervalMins, additionalMins);
		tfRenew.setSystem(true);
		tfRenew.setPriority(Job.SHORT);
		tfRenew.schedule(intervalMins * 60_000);
		traceFlagJobs.put(traceFlagId, tfRenew);
	}
	
	/**
	 * Cancel all background jobs related to TraceFlag
	 */
	public void removeTraceFlagJobs() {
		if (Utils.isNotEmpty(traceFlagJobs)) {
			for (TraceFlagRenewer job : traceFlagJobs.values()) {
				job.cancel();
			}
			
			traceFlagJobs.clear();
		}
	}
	
	/**
	 * Initialize Tooling connection.
	 * @param forceProject
	 * @throws ForceConnectionException
	 * @throws ForceRemoteException
	 */
	private void initializeConnection(ForceProject forceProject) throws ForceConnectionException, ForceRemoteException {
		toolingRESTConnection = new HTTPConnection(forceProject, TOOLING_ENDPOINT);
        toolingRESTConnection.initialize();
        toolingStubExt = ContainerDelegate.getInstance().getFactoryLocator().getToolingFactory().getToolingStubExt(forceProject);
	}
	
	public class TraceFlagRenewer extends Job {
		
		private final String traceFlagId;
		private final int intervalMins;
		private final int additionalMins;
		
		/**
		 * Extend the expiration date of a TraceFlag
		 * @param traceFlagId
		 * @param intervalMins
		 * @param additionalMins
		 */
		public TraceFlagRenewer(String traceFlagId, int intervalMins, int additionalMins) {
			super("Trace Flag Renew");
			this.traceFlagId = traceFlagId;
			this.intervalMins = intervalMins;
			this.additionalMins = additionalMins;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			schedule(intervalMins * 60_000);
			
			if (Utils.isEmpty(forceProject) || Utils.isEmpty(traceFlagId) || additionalMins <= 0) {
				return Status.CANCEL_STATUS;
			}
			
			try {
				initializeConnection(forceProject);
				
				QueryResult qr = toolingStubExt.query(String.format(QUERY_TRACEFLAG_BY_ID, traceFlagId));
				if (qr != null && qr.getTotalSize() == 1) {
					TraceFlag tf = (TraceFlag) qr.getRecords()[0];
					Calendar end = GregorianCalendar.getInstance();
					end.add(Calendar.MINUTE, additionalMins);
					tf.setExpirationDate(end);
					
					SaveResult[] srs = toolingStubExt.update(new SObject[] { tf });
					if (srs != null && srs.length == 1 && srs[0].isSuccess()) {
						logger.info(String.format("Extended expiration of TraceFlag %s by %d mins", traceFlagId, additionalMins));
						return Status.OK_STATUS;
					} else {
						logger.error(String.format("Failed to extend expiration of TraceFlag %s", traceFlagId));
					}
				} else {
					logger.error(String.format("Could not find TraceFlag %s", traceFlagId));
				}
			} catch (ForceConnectionException | ForceRemoteException e) {
				logger.error("Failed to connect to Tooling API", e);
			}
			
			return Status.CANCEL_STATUS;
		}
	}
}
