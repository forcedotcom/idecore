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
@JsonPropertyOrder({ "id", "name", "namespace", "message" })
public class RunTestsSyncCodeCoverageWarning {

	@JsonProperty("id")
	private String id;
	@JsonProperty("name")
	private String name;
	@JsonProperty("namespace")
	private String namespace;
	@JsonProperty("message")
	private String message;

	@JsonCreator
	public RunTestsSyncCodeCoverageWarning(@JsonProperty("id") String id,
			@JsonProperty("name") String name,
			@JsonProperty("namespace") String namespace,
			@JsonProperty("message") String message) {
		this.id = id;
		this.name = name;
		this.namespace = namespace;
		this.message = message;
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
}
