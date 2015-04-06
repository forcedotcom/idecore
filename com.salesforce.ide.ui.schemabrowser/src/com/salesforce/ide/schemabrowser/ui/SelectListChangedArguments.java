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
package com.salesforce.ide.schemabrowser.ui;

import java.util.ArrayList;

public class SelectListChangedArguments {

	private String tableName;

	private ArrayList<String> selectedFields;

	public SelectListChangedArguments() {

	}

	public SelectListChangedArguments(String tableName, ArrayList<String> selectedFields) {
		this.tableName = tableName;
		this.selectedFields = selectedFields;
	}

	public void addField(String fieldName) {
		if (selectedFields == null) {
			selectedFields = new ArrayList<>();
		}
		selectedFields.add(fieldName);
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String name) {
		tableName = name;
	}

	public ArrayList<String> getSelectedFields() {
		return selectedFields;
	}

	public void setSelectedFields(String[] fields) {
		selectedFields = new ArrayList<>();
		for (String element : fields) {
			selectedFields.add(element);
		}
	}
}
