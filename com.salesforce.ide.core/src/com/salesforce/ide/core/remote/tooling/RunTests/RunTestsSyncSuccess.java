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
@JsonPropertyOrder({ "namespace", "name", "methodName", "id", "time",
		"seeAllData" })
public class RunTestsSyncSuccess {

	@JsonProperty("namespace")
	private String namespace;
	@JsonProperty("name")
	private String name;
	@JsonProperty("methodName")
	private String methodName;
	@JsonProperty("id")
	private String id;
	@JsonProperty("time")
	private double time;
	@JsonProperty("seeAllData")
	private boolean seeAllData;

	@JsonCreator
	public RunTestsSyncSuccess(
			@JsonProperty("namespace") String namespace,
			@JsonProperty("name") String name,
			@JsonProperty("methodName") String methodName,
			@JsonProperty("id") String id,
			@JsonProperty("time") double time,
			@JsonProperty("seeAllData") boolean seeAllData) {
		this.namespace = namespace;
		this.name = name;
		this.methodName = methodName;
		this.id = id;
		this.time = time;
		this.seeAllData = seeAllData;
	}

	/**
	 * 
	 * @return The namespace
	 */
	@JsonProperty("namespace")
	public String getNamespace() {
		return namespace;
	}

	/**
	 * 
	 * @param namespace
	 *            The namespace
	 */
	@JsonProperty("namespace")
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * 
	 * @return The name
	 */
	@JsonProperty("name")
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 *            The name
	 */
	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return The methodName
	 */
	@JsonProperty("methodName")
	public String getMethodName() {
		return methodName;
	}

	/**
	 * 
	 * @param methodName
	 *            The methodName
	 */
	@JsonProperty("methodName")
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * 
	 * @return The id
	 */
	@JsonProperty("id")
	public String getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 *            The id
	 */
	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
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

	/**
	 * 
	 * @return The seeAllData
	 */
	@JsonProperty("seeAllData")
	public boolean isSeeAllData() {
		return seeAllData;
	}

	/**
	 * 
	 * @param seeAllData
	 *            The seeAllData
	 */
	@JsonProperty("seeAllData")
	public void setSeeAllData(boolean seeAllData) {
		this.seeAllData = seeAllData;
	}
}
