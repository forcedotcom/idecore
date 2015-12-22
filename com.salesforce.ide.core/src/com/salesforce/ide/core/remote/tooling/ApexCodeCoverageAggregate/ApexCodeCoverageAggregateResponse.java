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

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * SFDC-WSC has a bug in which it cannot deserialize an array of
 * primitives. That functionality is needed to query for
 * ApexCodeCoverageAggregate through SOAP because it has a
 * couple of integer arrays that contain covered and uncovered
 * lines. The workarounhd is to query the code coverage
 * through Tooling API using REST and Jackson's ObjectMapper,
 * so we need these Json types.
 * 
 * @author jwidjaja
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "size", "totalSize", "done", "queryLocator",
		"entityTypeName", "records" })
public class ApexCodeCoverageAggregateResponse {
	
	@JsonProperty("size")
	public int size;
	@JsonProperty("totalSize")
	public int totalSize;
	@JsonProperty("done")
	public boolean done;
	@JsonProperty("queryLocator")
	public Object queryLocator;
	@JsonProperty("entityTypeName")
	public String entityTypeName;
	@JsonProperty("records")
	public List<Record> records = new ArrayList<Record>();

}
