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
@JsonPropertyOrder({ "type", "namespace", "name", "methodName", "message",
		"stackTrace", "id", "seeAllData", "time", "packageName" })
public class RunTestsSyncFailure {

	@JsonProperty("type")
	private String type;
	@JsonProperty("namespace")
	private String namespace;
	@JsonProperty("name")
	private String name;
	@JsonProperty("methodName")
	private String methodName;
	@JsonProperty("message")
	private String message;
	@JsonProperty("stackTrace")
	private String stackTrace;
	@JsonProperty("id")
	private String id;
	@JsonProperty("seeAllData")
	private boolean seeAllData;
	@JsonProperty("time")
	private double time;
	@JsonProperty("packageName")
	private String packageName;

	@JsonCreator
	public RunTestsSyncFailure(
			@JsonProperty("type") String type,
			@JsonProperty("namespace") String namespace,
			@JsonProperty("name") String name,
			@JsonProperty("methodName") String methodName,
			@JsonProperty("message") String message,
			@JsonProperty("stackTrace") String stackTrace,
			@JsonProperty("id") String id,
			@JsonProperty("seeAllData") boolean seeAllData,
			@JsonProperty("time") double time,
			@JsonProperty("packageName") String packageName) {
		this.type = type;
		this.namespace = namespace;
		this.name = name;
		this.methodName = methodName;
		this.message = message;
		this.stackTrace = stackTrace;
		this.id = id;
		this.seeAllData = seeAllData;
		this.time = time;
		this.packageName = packageName;
	}

	/**
	 * 
	 * @return The type
	 */
	@JsonProperty("type")
	public String getType() {
		return type;
	}

	/**
	 * 
	 * @param type
	 *            The type
	 */
	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
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
	 * @return The message
	 */
	@JsonProperty("message")
	public String getMessage() {
		return message;
	}

	/**
	 * 
	 * @param message
	 *            The message
	 */
	@JsonProperty("message")
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 
	 * @return The stackTrace
	 */
	@JsonProperty("stackTrace")
	public String getStackTrace() {
		return stackTrace;
	}

	/**
	 * 
	 * @param stackTrace
	 *            The stackTrace
	 */
	@JsonProperty("stackTrace")
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
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
	 * @return The packageName
	 */
	@JsonProperty("packageName")
	public String getPackageName() {
		return packageName;
	}

	/**
	 * 
	 * @param packageName
	 *            The packageName
	 */
	@JsonProperty("packageName")
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}
