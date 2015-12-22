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

package com.salesforce.ide.core.remote.tooling.RunTests;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "successes", "failures", "totalTime",
		"codeCoverageWarnings", "numTestsRun", "codeCoverage", "numFailures" })
public class RunTestsSyncResponse {

	@JsonProperty("successes")
	private List<RunTestsSyncSuccess> successes = new ArrayList<RunTestsSyncSuccess>();
	@JsonProperty("failures")
	private List<RunTestsSyncFailure> failures = new ArrayList<RunTestsSyncFailure>();
	@JsonProperty("totalTime")
	private double totalTime;
	@JsonProperty("codeCoverageWarnings")
	private List<RunTestsSyncCodeCoverageWarning> codeCoverageWarnings = new ArrayList<RunTestsSyncCodeCoverageWarning>();
	@JsonProperty("numTestsRun")
	private int numTestsRun;
	@JsonProperty("codeCoverage")
	private List<RunTestsSyncCodeCoverage> codeCoverage = new ArrayList<RunTestsSyncCodeCoverage>();
	@JsonProperty("numFailures")
	private int numFailures;
	@JsonProperty("apexLogId")
	private String apexLogId;

	@JsonCreator
	public RunTestsSyncResponse(
			@JsonProperty("successes") List<RunTestsSyncSuccess> successes,
			@JsonProperty("failures") List<RunTestsSyncFailure> failures,
			@JsonProperty("totalTime") double totalTime,
			@JsonProperty("codeCoverageWarnings") List<RunTestsSyncCodeCoverageWarning> codeCoverageWarnings,
			@JsonProperty("numTestsRun") int numTestsRun,
			@JsonProperty("codeCoverage") List<RunTestsSyncCodeCoverage> codeCoverage,
			@JsonProperty("numFailures") int numFailures,
			@JsonProperty("apexLogId") String apexLogId) {
		this.successes = successes;
		this.failures = failures;
		this.totalTime = totalTime;
		this.codeCoverageWarnings = codeCoverageWarnings;
		this.numTestsRun = numTestsRun;
		this.codeCoverage = codeCoverage;
		this.numFailures = numFailures;
		this.apexLogId = apexLogId;
	}

	/**
	 * 
	 * @return The successes
	 */
	@JsonProperty("successes")
	public List<RunTestsSyncSuccess> getSuccesses() {
		return successes;
	}

	/**
	 * 
	 * @param successes
	 *            The successes
	 */
	@JsonProperty("successes")
	public void setSuccesses(List<RunTestsSyncSuccess> successes) {
		this.successes = successes;
	}

	/**
	 * 
	 * @return The failures
	 */
	@JsonProperty("failures")
	public List<RunTestsSyncFailure> getFailures() {
		return failures;
	}

	/**
	 * 
	 * @param failures
	 *            The failures
	 */
	@JsonProperty("failures")
	public void setFailures(List<RunTestsSyncFailure> failures) {
		this.failures = failures;
	}

	/**
	 * 
	 * @return The totalTime
	 */
	@JsonProperty("totalTime")
	public double getTotalTime() {
		return totalTime;
	}

	/**
	 * 
	 * @param totalTime
	 *            The totalTime
	 */
	@JsonProperty("totalTime")
	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}

	/**
	 * 
	 * @return The codeCoverageWarnings
	 */
	@JsonProperty("codeCoverageWarnings")
	public List<RunTestsSyncCodeCoverageWarning> getCodeCoverageWarnings() {
		return codeCoverageWarnings;
	}

	/**
	 * 
	 * @param codeCoverageWarnings
	 *            The codeCoverageWarnings
	 */
	@JsonProperty("codeCoverageWarnings")
	public void setCodeCoverageWarnings(
			List<RunTestsSyncCodeCoverageWarning> codeCoverageWarnings) {
		this.codeCoverageWarnings = codeCoverageWarnings;
	}

	/**
	 * 
	 * @return The numTestsRun
	 */
	@JsonProperty("numTestsRun")
	public int getNumTestsRun() {
		return numTestsRun;
	}

	/**
	 * 
	 * @param numTestsRun
	 *            The numTestsRun
	 */
	@JsonProperty("numTestsRun")
	public void setNumTestsRun(int numTestsRun) {
		this.numTestsRun = numTestsRun;
	}

	/**
	 * 
	 * @return The codeCoverage
	 */
	@JsonProperty("codeCoverage")
	public List<RunTestsSyncCodeCoverage> getCodeCoverage() {
		return codeCoverage;
	}

	/**
	 * 
	 * @param codeCoverage
	 *            The codeCoverage
	 */
	@JsonProperty("codeCoverage")
	public void setCodeCoverage(List<RunTestsSyncCodeCoverage> codeCoverage) {
		this.codeCoverage = codeCoverage;
	}

	/**
	 * 
	 * @return The numFailures
	 */
	@JsonProperty("numFailures")
	public int getNumFailures() {
		return numFailures;
	}

	/**
	 * 
	 * @param numFailures
	 *            The numFailures
	 */
	@JsonProperty("numFailures")
	public void setNumFailures(int numFailures) {
		this.numFailures = numFailures;
	}
	
	/**
	 * 
	 * @return The apexLogId
	 */
	@JsonProperty("apexLogId")
	public String getApexLogId() {
		return apexLogId;
	}
	
	/**
	 * 
	 * @param apexLogId
	 *            The apexLogId
	 */
	@JsonProperty("apexLogId")
	public void setApexLogId(String apexLogId) {
		this.apexLogId = apexLogId;
	}
}