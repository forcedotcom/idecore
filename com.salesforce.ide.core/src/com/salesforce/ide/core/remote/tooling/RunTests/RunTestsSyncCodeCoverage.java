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
@JsonPropertyOrder({ "locationsNotCovered", "soqlInfo",
		"numLocationsNotCovered", "soslInfo", "dmlInfo", "numLocations",
		"methodInfo", "namespace", "name", "id", "type" })
public class RunTestsSyncCodeCoverage {

	@JsonProperty("locationsNotCovered")
	private List<RunTestsSyncLocationsNotCovered> locationsNotCovered = new ArrayList<RunTestsSyncLocationsNotCovered>();
	@JsonProperty("soqlInfo")
	private List<RunTestsSyncCodeLocation> soqlInfo = new ArrayList<RunTestsSyncCodeLocation>();
	@JsonProperty("numLocationsNotCovered")
	private int numLocationsNotCovered;
	@JsonProperty("soslInfo")
	private List<RunTestsSyncCodeLocation> soslInfo = new ArrayList<RunTestsSyncCodeLocation>();
	@JsonProperty("dmlInfo")
	private List<RunTestsSyncCodeLocation> dmlInfo = new ArrayList<RunTestsSyncCodeLocation>();
	@JsonProperty("numLocations")
	private int numLocations;
	@JsonProperty("methodInfo")
	private List<RunTestsSyncCodeLocation> methodInfo = new ArrayList<RunTestsSyncCodeLocation>();
	@JsonProperty("namespace")
	private String namespace;
	@JsonProperty("name")
	private String name;
	@JsonProperty("id")
	private String id;
	@JsonProperty("type")
	private String type;

	@JsonCreator
	public RunTestsSyncCodeCoverage(
			@JsonProperty("locationsNotCovered") List<RunTestsSyncLocationsNotCovered> locationsNotCovered,
			@JsonProperty("soqlInfo") List<RunTestsSyncCodeLocation> soqlInfo,
			@JsonProperty("numLocationsNotCovered") int numLocationsNotCovered,
			@JsonProperty("soslInfo") List<RunTestsSyncCodeLocation> soslInfo,
			@JsonProperty("dmlInfo") List<RunTestsSyncCodeLocation> dmlInfo,
			@JsonProperty("numLocations") int numLocations,
			@JsonProperty("methodInfo") List<RunTestsSyncCodeLocation> methodInfo,
			@JsonProperty("namespace") String namespace,
			@JsonProperty("name") String name,
			@JsonProperty("id") String id,
			@JsonProperty("type") String type) {
		this.locationsNotCovered = locationsNotCovered;
		this.soqlInfo = soqlInfo;
		this.numLocationsNotCovered = numLocationsNotCovered;
		this.soslInfo = soslInfo;
		this.dmlInfo = dmlInfo;
		this.numLocations = numLocations;
		this.methodInfo = methodInfo;
		this.namespace = namespace;
		this.name = name;
		this.id = id;
		this.type = type;
	}

	/**
	 * 
	 * @return The locationsNotCovered
	 */
	@JsonProperty("locationsNotCovered")
	public List<RunTestsSyncLocationsNotCovered> getLocationsNotCovered() {
		return locationsNotCovered;
	}

	/**
	 * 
	 * @param locationsNotCovered
	 *            The locationsNotCovered
	 */
	@JsonProperty("locationsNotCovered")
	public void setLocationsNotCovered(
			List<RunTestsSyncLocationsNotCovered> locationsNotCovered) {
		this.locationsNotCovered = locationsNotCovered;
	}

	/**
	 * 
	 * @return The soqlInfo
	 */
	@JsonProperty("soqlInfo")
	public List<RunTestsSyncCodeLocation> getSoqlInfo() {
		return soqlInfo;
	}

	/**
	 * 
	 * @param soqlInfo
	 *            The soqlInfo
	 */
	@JsonProperty("soqlInfo")
	public void setSoqlInfo(List<RunTestsSyncCodeLocation> soqlInfo) {
		this.soqlInfo = soqlInfo;
	}

	/**
	 * 
	 * @return The numLocationsNotCovered
	 */
	@JsonProperty("numLocationsNotCovered")
	public int getNumLocationsNotCovered() {
		return numLocationsNotCovered;
	}

	/**
	 * 
	 * @param numLocationsNotCovered
	 *            The numLocationsNotCovered
	 */
	@JsonProperty("numLocationsNotCovered")
	public void setNumLocationsNotCovered(int numLocationsNotCovered) {
		this.numLocationsNotCovered = numLocationsNotCovered;
	}

	/**
	 * 
	 * @return The soslInfo
	 */
	@JsonProperty("soslInfo")
	public List<RunTestsSyncCodeLocation> getSoslInfo() {
		return soslInfo;
	}

	/**
	 * 
	 * @param soslInfo
	 *            The soslInfo
	 */
	@JsonProperty("soslInfo")
	public void setSoslInfo(List<RunTestsSyncCodeLocation> soslInfo) {
		this.soslInfo = soslInfo;
	}

	/**
	 * 
	 * @return The dmlInfo
	 */
	@JsonProperty("dmlInfo")
	public List<RunTestsSyncCodeLocation> getDmlInfo() {
		return dmlInfo;
	}

	/**
	 * 
	 * @param dmlInfo
	 *            The dmlInfo
	 */
	@JsonProperty("dmlInfo")
	public void setDmlInfo(List<RunTestsSyncCodeLocation> dmlInfo) {
		this.dmlInfo = dmlInfo;
	}

	/**
	 * 
	 * @return The numLocations
	 */
	@JsonProperty("numLocations")
	public int getNumLocations() {
		return numLocations;
	}

	/**
	 * 
	 * @param numLocations
	 *            The numLocations
	 */
	@JsonProperty("numLocations")
	public void setNumLocations(int numLocations) {
		this.numLocations = numLocations;
	}

	/**
	 * 
	 * @return The methodInfo
	 */
	@JsonProperty("methodInfo")
	public List<RunTestsSyncCodeLocation> getMethodInfo() {
		return methodInfo;
	}

	/**
	 * 
	 * @param methodInfo
	 *            The methodInfo
	 */
	@JsonProperty("methodInfo")
	public void setMethodInfo(List<RunTestsSyncCodeLocation> methodInfo) {
		this.methodInfo = methodInfo;
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
}
