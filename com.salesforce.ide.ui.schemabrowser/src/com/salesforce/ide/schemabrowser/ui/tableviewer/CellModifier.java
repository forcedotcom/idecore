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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;

import com.salesforce.ide.core.internal.utils.XmlConstants;
import com.sforce.ws.bind.XmlObject;

/**
 * Called when the user modifes a cell in the tableViewer
 *
 * @author dcarroll
 */
public class CellModifier implements ICellModifier {
	private QueryTableViewer tableViewer;

	/**
	 * Constructor
	 *
	 * @param QueryTableViewer an instance of a TableViewerExample
	 */
	public CellModifier(QueryTableViewer tableViewer) {
		super();
		this.tableViewer = tableViewer;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property) {
		return true;
	}

	public Object getValue(Object element, String property) {

		// Find the index of the column
		int columnIndex = tableViewer.getColumnNames().indexOf(property);

		Object result = null;
		DataRow task = (DataRow) element;
		XmlObject record = task.getRecord().get(columnIndex);
		if (record.getXmlType() != null) {
			Iterator<XmlObject> it = record.getChildren();
			String res = "";
			while (it.hasNext()) {
				XmlObject next = it.next();
				if (next.getValue() != null) {
					res += next.getValue() + "\n";
				}
			}
			showDialog(record);
		}
		if (1 == 1) {
			return null; // String result = "";
		}
		if (((DataRow) element).getRecord() != null) {
			XmlObject field = (task.getRecord()).get(columnIndex);
			result = field.getValue();
		} else {
			// ExampleTask task = (ExampleTask) element;

			switch (columnIndex) {
			case 0: // COMPLETED_COLUMN
				break;
			case 1:
				result = task.getDescription();
				break;
			case 2:
				result = task.getOwner();
				break;
			case 3:
				result = task.getPercentComplete() + "";
				break;
			default:
				break;
			}
		}
		return result;
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value) {

		// Find the index of the column
		int columnIndex = tableViewer.getColumnNames().indexOf(property);

		TableItem item = (TableItem) element;
		DataRow task = (DataRow) item.getData();
		String valueString;

		switch (columnIndex) {
		case 0: // COMPLETED_COLUMN
			task.setCompleted(((Boolean) value).booleanValue());
			break;
		case 1: // DESCRIPTION_COLUMN
			valueString = ((String) value).trim();
			task.setDescription(valueString);
			break;
		case 2: // OWNER_COLUMN
			valueString = "";// tableViewerExample.getChoices(property)[((Integer)
			// value).intValue()].trim();
			if (!task.getOwner().equals(valueString)) {
				task.setOwner(valueString);
			}
			break;
		case 3: // PERCENT_COLUMN
			valueString = ((String) value).trim();
			if (valueString.length() == 0) {
				valueString = "0";
			}
			task.setPercentComplete(Integer.parseInt(valueString));
			break;
		default:
		}
		tableViewer.getTaskList().taskChanged(task);
	}

	void showDialog(XmlObject val) {
		if (val != null) {

			Hashtable<String, String> columnNames = new Hashtable<String, String>();
			Vector<Hashtable<String, Object>> rows = new Vector<Hashtable<String, Object>>();
			String type;
			QName xmlType = val.getXmlType();
			if (xmlType == null) {
				type = "String";
			} else {
				type = xmlType.getLocalPart();
			}
			String dialogTitle = null;
			if ("sObject".equals(type)) {
				dialogTitle = "Lookup to " + val.getName().getLocalPart();
				Iterator<XmlObject> iter = val.getChildren();
				Hashtable<String, Object> row = new Hashtable<String, Object>();
				ArrayList<String> rowsNames = new ArrayList<String>();
				while (iter.hasNext()) {
					XmlObject field = iter.next();
					if (!XmlConstants.ELEM_TYPE.equals(field.getName().getLocalPart())
							&& !rowsNames.contains(field.getName().getLocalPart())) {
						if (field.getValue() != null) {
							columnNames.put(field.getName().getLocalPart(), field.getName().getLocalPart());
						}
						Object fieldVal = field.getValue();
						if (fieldVal == null) {
							fieldVal = "";
						}
						row.put(field.getName().getLocalPart(), fieldVal);
						rowsNames.add(field.getName().getLocalPart());
					}
				}
				rows.add(row);
			} else {
				Iterator<XmlObject> iter = val.getChildren();
				while (iter.hasNext()) {
					XmlObject oneElement = iter.next();
					dialogTitle = "Child records for " + val.getName().getLocalPart();

					if ("records".equals(oneElement.getName().getLocalPart())) {
						Iterator<XmlObject> children = oneElement.getChildren();
						Hashtable<String, Object> row = new Hashtable<String, Object>();
						ArrayList<String> rowsNames = new ArrayList<String>();
						while (children.hasNext()) {
							XmlObject field = children.next();
							if (!XmlConstants.ELEM_TYPE.equals(field.getName().getLocalPart())
									&& !rowsNames.contains(field.getName().getLocalPart())) {
								if (field.getValue() != null) {
									columnNames.put(field.getName().getLocalPart(), field.getName().getLocalPart());
								}
								Object fieldVal = field.getValue();
								if (fieldVal == null) {
									fieldVal = "";
								}
								row.put(field.getName().getLocalPart(), fieldVal);
							}
						}
						rows.add(row);
					}
				}
			}

			TableViewerRunnable mr = new TableViewerRunnable(null);// Display.getDefault().getActiveShell());
			mr.init(rows, columnNames, dialogTitle);
			Display.getDefault().syncExec(mr);

		}
	}
}
