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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.XmlConstants;
import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.QueryResult;
import com.sforce.ws.bind.XmlObject;

/**
 * Legacy class
 *
 * @author dcarroll
 */
public class DataRowList {
    private static final Logger logger = Logger.getLogger(DataRowList.class);

    private final int COUNT = 10;

    private final Vector<DataRow> dataRows = new Vector<>(COUNT);

    private final Set<IDataRowListViewer> changeListeners = new HashSet<>();

    // Combo box choices
    static final String[] OWNERS_ARRAY = { "?", "Nancy", "Larry", "Joe" };

    /**
     * Constructor
     */
    public DataRowList(QueryResult qr) {
        super();
        this.initData(qr);
    }

    public DataRowList() {
        super();
        this.initData();
    }

    /*
     * Initialize the table data. Create COUNT tasks and add them them to the collection of tasks
     */
    private void initData() {
        DataRow task;
        for (int i = 0; i < COUNT; i++) {
            task = new DataRow("Task " + i);
            task.setOwner(OWNERS_ARRAY[i % 3]);
            dataRows.add(task);
        }
    };

    /**
     * Because we need to use indexes of columns to grab the values from the label provider, we need to marshal this
     * into a vector
     *
     * @param qr
     */
    private void initData(QueryResult qr) {
        if (qr == null) {
            return;
        }

        if (qr.getSize() > 0 && Utils.isEmpty(qr.getRecords())) {
            // prepareCountRow(qr);
        } else {
            prepareRecordRows(qr);
        }
    };

    private void prepareRecordRows(QueryResult qr) {
        if (qr == null) {
            logger.warn("Unable to prepare schema browser rows - query result is null");
            return;
        }

        DataRow task;

        SObject[] records = qr.getRecords();
        if (records == null) {
            logger.warn("Unable to prepare schema browser rows - returned record array null");
            return;
        }

        for (SObject sobject : records) {
            Vector<XmlObject> record = recordToVector(sobject);
            task = new DataRow(record);
            // task.setOwner(OWNERS_ARRAY[i % 3]);
            dataRows.add(task);
        }
    }

    private static Vector<XmlObject> recordToVector(SObject record) {
        // A couple of special cases to handle:
        // If the record.getType() == record.getField(Xml.ELEM_TYPE) then we
        // skip
        // that field.
        // If the field is named id and has a null, we skip that.
        // We only add a field names Id once.
        boolean hasId = record.getId() != null;
        Iterator<XmlObject> it = record.getChildren();
        Vector<XmlObject> vRecord = new Vector<>();
        if (hasId) {
            vRecord.add(record.getChildren("Id").next());
        }
        while (it.hasNext()) {
            XmlObject field = it.next();
            String fieldName = field.getName().getLocalPart();
            if ("Id".equalsIgnoreCase(fieldName) && hasId) {
                // Skip
            } else if (XmlConstants.ELEM_TYPE.equals(fieldName) && record.getType().equals(field.getValue())) {
                // Skip
            } else if (field.getXmlType() != null) {
                // This can be either an sObjet or a queryResults
                javax.xml.namespace.QName xmlType = field.getXmlType();
                if ("sObject".equals(xmlType.getLocalPart())) {
                    // This is a lookup relationship, so we need to add the
                    // sobject which is actually
                    // the array of children, hmm....
                    vRecord.add(field);
                } else {
                    // field.getChild("records").
                    vRecord.add(field);
                }
            } else {
                if ("Id".equalsIgnoreCase(fieldName) && field.getValue() != null) {
                    vRecord.add(field);
                } else if (!"Id".equalsIgnoreCase(fieldName)) {
                    vRecord.add(field);
                }
            }
        }
        return vRecord;
    }

    /**
     * Return the array of owners, used be the cell editor
     */
    public String[] getOwners() {
        return OWNERS_ARRAY;
    }

    /**
     * Return the collection of tasks
     */
    public Vector<DataRow> getTasks() {
        return dataRows;
    }

    /**
     * Add a new task to the collection of tasks, won't use this as we don't add to our result set
     */
    public void addTask() {
        DataRow task = new DataRow("New task");
        dataRows.add(dataRows.size(), task);
        Iterator<IDataRowListViewer> iterator = changeListeners.iterator();
        while (iterator.hasNext()) {
            (iterator.next()).addTask(task);
        }
    }

    /**
     * Won't use this either since we don't delete from our result set
     *
     * @param task
     */
    public void removeTask(DataRow task) {
        dataRows.remove(task);
        Iterator<IDataRowListViewer> iterator = changeListeners.iterator();
        while (iterator.hasNext()) {
            (iterator.next()).removeTask(task);
        }
    }

    /**
     * Won't use this either since the result set is read only
     *
     * @param task
     */
    public void taskChanged(DataRow task) {
        Iterator<IDataRowListViewer> iterator = changeListeners.iterator();
        while (iterator.hasNext()) {
            (iterator.next()).updateTask(task);
        }
    }

    /**
     * Ultimately, will remove this
     *
     * @param viewer
     */
    public void removeChangeListener(IDataRowListViewer viewer) {
        changeListeners.remove(viewer);
    }

    /**
     * Ultimately, will remove this too
     *
     * @param viewer
     */
    public void addChangeListener(IDataRowListViewer viewer) {
        changeListeners.add(viewer);
    }

}
