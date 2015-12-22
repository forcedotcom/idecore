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

package com.salesforce.ide.core.remote.tooling.ApexCodeCoverageAggregate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "attributes", "ApexClassOrTriggerId",
		"ApexClassOrTrigger", "NumLinesCovered", "NumLinesUncovered",
		"Coverage" })
public class Record {

	@JsonProperty("attributes")
	public Attributes attributes;
	@JsonProperty("ApexClassOrTriggerId")
	public String ApexClassOrTriggerId;
	@JsonProperty("ApexClassOrTrigger")
	public com.salesforce.ide.core.remote.tooling.ApexCodeCoverageAggregate.ApexClassOrTrigger ApexClassOrTrigger;
	@JsonProperty("NumLinesCovered")
	public int NumLinesCovered;
	@JsonProperty("NumLinesUncovered")
	public int NumLinesUncovered;
	@JsonProperty("Coverage")
	public com.salesforce.ide.core.remote.tooling.ApexCodeCoverageAggregate.Coverage Coverage;

}