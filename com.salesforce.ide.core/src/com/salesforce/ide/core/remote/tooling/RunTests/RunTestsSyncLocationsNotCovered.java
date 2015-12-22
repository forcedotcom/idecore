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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "line", "numExecutions", "column", "time" })
public class RunTestsSyncLocationsNotCovered {

	@JsonProperty("line")
	private int line;
	@JsonProperty("numExecutions")
	private int numExecutions;
	@JsonProperty("column")
	private int column;
	@JsonProperty("time")
	private double time;

	@JsonCreator
	public RunTestsSyncLocationsNotCovered(
			@JsonProperty("line") int line,
			@JsonProperty("numExecutions") int numExecutions,
			@JsonProperty("column") int column,
			@JsonProperty("time") double time) {
		this.line = line;
		this.numExecutions = numExecutions;
		this.column = column;
		this.time = time;
	}

	/**
	 * 
	 * @return The line
	 */
	@JsonProperty("line")
	public int getLine() {
		return line;
	}

	/**
	 * 
	 * @param line
	 *            The line
	 */
	@JsonProperty("line")
	public void setLine(int line) {
		this.line = line;
	}

	/**
	 * 
	 * @return The numExecutions
	 */
	@JsonProperty("numExecutions")
	public int getNumExecutions() {
		return numExecutions;
	}

	/**
	 * 
	 * @param numExecutions
	 *            The numExecutions
	 */
	@JsonProperty("numExecutions")
	public void setNumExecutions(int numExecutions) {
		this.numExecutions = numExecutions;
	}

	/**
	 * 
	 * @return The column
	 */
	@JsonProperty("column")
	public int getColumn() {
		return column;
	}

	/**
	 * 
	 * @param column
	 *            The column
	 */
	@JsonProperty("column")
	public void setColumn(int column) {
		this.column = column;
	}

	/**
	 * 
	 * @return The time
	 */
	@JsonProperty("time")
	public double getTime() {
		return time;
	}

	/**
	 * 
	 * @param time
	 *            The time
	 */
	@JsonProperty("time")
	public void setTime(double time) {
		this.time = time;
	}
}
