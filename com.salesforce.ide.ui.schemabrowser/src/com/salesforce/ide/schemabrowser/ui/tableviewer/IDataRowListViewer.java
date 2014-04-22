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

public interface IDataRowListViewer {

	/**
	 * Update the view to reflect the fact that a task was added to the task list
	 * 
	 * @param task
	 */
	public void addTask(DataRow task);

	/**
	 * Update the view to reflect the fact that a task was removed from the task list
	 * 
	 * @param task
	 */
	public void removeTask(DataRow task);

	/**
	 * Update the view to reflect the fact that one of the tasks was modified
	 * 
	 * @param task
	 */
	public void updateTask(DataRow task);
}
