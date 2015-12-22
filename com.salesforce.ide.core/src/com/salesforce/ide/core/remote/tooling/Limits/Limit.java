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

package com.salesforce.ide.core.remote.tooling.Limits;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * POJO for a Limit object which has the name, remaining number, and max number.
 * 
 * @author jwidjaja
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "Name", "Remaining", "Max" })
public class Limit {

	@JsonProperty("Name")
	private String Name;
	@JsonProperty("Remaining")
	private Integer Remaining;
	@JsonProperty("Max")
	private Integer Max;

	/**
	 * 
	 * @return The Name
	 */
	@JsonProperty("Name")
	public String getName() {
		return Name;
	}

	/**
	 * 
	 * @param Name
	 *            The Name
	 */
	@JsonProperty("Name")
	public void setName(String Name) {
		this.Name = Name;
	}

	/**
	 * 
	 * @return The Remaining
	 */
	@JsonProperty("Remaining")
	public Integer getRemaining() {
		return Remaining;
	}

	/**
	 * 
	 * @param Remaining
	 *            The Remaining
	 */
	@JsonProperty("Remaining")
	public void setRemaining(Integer Remaining) {
		this.Remaining = Remaining;
	}

	/**
	 * 
	 * @return The Max
	 */
	@JsonProperty("Max")
	public Integer getMax() {
		return Max;
	}

	/**
	 * 
	 * @param Max
	 *            The Max
	 */
	@JsonProperty("Max")
	public void setMax(Integer Max) {
		this.Max = Max;
	}
}