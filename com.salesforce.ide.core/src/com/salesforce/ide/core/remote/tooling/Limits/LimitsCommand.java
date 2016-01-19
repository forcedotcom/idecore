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

package com.salesforce.ide.core.remote.tooling.Limits;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.ide.core.remote.BaseCommand;
import com.salesforce.ide.core.remote.HTTPAdapter;

/**
 * A Job to fetch limits (remaining and max).
 * 
 * @author jwidjaja
 *
 */
public class LimitsCommand extends BaseCommand<String> {

	public enum Type {
		ConcurrentAsyncGetReportInstances,
		ConcurrentSyncReportRuns,
		DailyApiRequests,
		DailyAsyncApexExecutions,
		DailyBulkApiRequests,
		DailyGenericStreamingApiEvents,
		DailyStreamingApiEvents,
		DailyWorkflowEmails,
		DataStorageMB,
		FileStorageMB,
		HourlyAsyncReportRuns,
		HourlyDashboardRefreshes,
		HourlyDashboardResults,
		HourlyDashboardStatuses,
		HourlySyncReportRuns,
		HourlyTimeBasedWorkflow,
		MassEmail,
		SingleEmail,
		StreamingApiConcurrentClients
	}
	
	private static final String GETTING_LIMITS = "Acquiring limits";
		
	public LimitsCommand(HTTPAdapter<String> transport) {
		super(GETTING_LIMITS);
		this.transport = transport;
	}
	
	/**
	 * Execute the HTTP request and return a map of limit names and their limits (remaining, max).
	 */
	@Override
	protected String execute(IProgressMonitor monitor) throws Throwable {
		monitor.beginTask(GETTING_LIMITS, 2);
		
		transport.send("");
		monitor.worked(1);
		
		String response = transport.receive();
		monitor.worked(1);
		
		return response;
	}
	
	public Map<String, Limit> parseLimits(String json) {
		// Return a tree of Limit objects whose key is the Limit's name
		TreeMap<String, Limit> limits = new TreeMap<String, Limit>();
		
		// The response is in JSON so we need to parse and convert using Limit.java POJO
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode limitsJson = mapper.readTree(json);
			Iterator<String> limitsNames = limitsJson.fieldNames();

			while (limitsNames.hasNext()) {
				String limitName = limitsNames.next();
				JsonNode aLimit = limitsJson.get(limitName);
				int remaining = aLimit.findValue("Remaining").asInt();
				int max = aLimit.findValue("Max").asInt();
				
				Limit limit = new Limit();
				limit.setName(limitName);
				limit.setRemaining(remaining);
				limit.setMax(max);
				limits.put(limitName, limit);
			}
		} catch (IOException e) {}
		
		return limits;
	}
}
