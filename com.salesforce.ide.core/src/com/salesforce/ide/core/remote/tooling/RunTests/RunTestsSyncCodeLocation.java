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
@JsonPropertyOrder({ "line" })
public class RunTestsSyncCodeLocation {

	@JsonProperty("line")
	private int line;

	@JsonCreator
	public RunTestsSyncCodeLocation(@JsonProperty("line") int line) {
		this.line = line;
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
}
