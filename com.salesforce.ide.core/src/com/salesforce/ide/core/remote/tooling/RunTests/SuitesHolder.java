/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/

package com.salesforce.ide.core.remote.tooling.RunTests;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.ide.core.internal.utils.Utils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "suiteids" })
public class SuitesHolder {

	@JsonProperty("suiteids")
	private String suiteids;

	/**
	 * 
	 * @return The suiteids
	 */
	@JsonProperty("suiteids")
	public String getSuiteids() {
		return suiteids;
	}

	/**
	 * 
	 * @param suiteids
	 *            The suiteids
	 */
	@JsonProperty("suiteids")
	public void setSuiteids(String suiteids) {
		this.suiteids = suiteids;
	}
	
	/**
     * Convert SuitesHolder to JSON string
     * @param SuitesHolder
     * @return JSON string
     */
    public static String serialize(SuitesHolder sh) {
    	String result = "";
    	if (Utils.isNotEmpty(sh)) {
    		ObjectMapper mapper = new ObjectMapper();
        	try {
        		result = mapper.writeValueAsString(sh);
        	} catch (JsonProcessingException e) {}
    	}
    	
    	return result;
    }
    
    /**
     * Convert JSON string to SuitesHolder
     * @param JSON string
     * @return SuitesHolder
     */
    public static SuitesHolder deserialize(String json) {
    	ObjectMapper mapper = new ObjectMapper();
    	try {
			return mapper.readValue(json, SuitesHolder.class);
		} catch (IOException e) {}
    	
    	return null;
    }
}
