/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.schemabrowser.ui.tableviewer;

import java.util.Vector;

import com.sforce.ws.bind.XmlObject;

/**
 * Legacy class
 *
 * @author dcarroll
 */
public class DataRow {

	private boolean completed = false;
	private String description = "";
	private String owner = "?";
	private int percentComplete = 0;
	private Vector<XmlObject> records = null;

	/**
	 * Create a task with an initial description
	 * 
	 * @param string
	 */
	public DataRow(String string) {
		super();
		setDescription(string);
	}

	public DataRow(Vector<XmlObject> record) {
		super();
		setRecord(record);
	}

	/**
	 * @return true if completed, false otherwise
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * @return String task description
	 */
	public String getDescription() {
		return description;
	}

	public Vector<XmlObject> getRecord() {
		return records;
	}

	/**
	 * @return String task owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @return int percent completed
	 * 
	 */
	public int getPercentComplete() {
		return percentComplete;
	}

	/**
	 * Set the 'completed' property
	 * 
	 * @param b
	 */
	public void setCompleted(boolean b) {
		completed = b;
	}

	/**
	 * Set the 'description' property
	 * 
	 * @param string
	 */
	public void setDescription(String string) {
		description = string;
	}

	public void setRecord(Vector<XmlObject> record) {
		this.records = record;
	}

	/**
	 * Set the 'owner' property
	 * 
	 * @param string
	 */
	public void setOwner(String string) {
		owner = string;
	}

	/**
	 * Set the 'percentComplete' property
	 * 
	 * @param i
	 */
	public void setPercentComplete(int i) {
		percentComplete = i;
	}

}
