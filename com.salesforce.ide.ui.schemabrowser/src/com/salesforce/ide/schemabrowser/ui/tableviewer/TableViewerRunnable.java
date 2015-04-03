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

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.swt.widgets.Shell;

public class TableViewerRunnable implements Runnable {

    protected Shell shell = null;
    protected Hashtable<String, String> columnNames;
    protected Vector<Hashtable<String, Object>> rows;
    protected String dialogTitle;
    protected Runnable showRecords = new Runnable() {
        @Override
        public void run() {
            TableViewerDialog dialog = new TableViewerDialog(null);
            dialog.setData(rows, columnNames, dialogTitle);
            dialog.open();
        }
    };

    public TableViewerRunnable(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void run() {
        showRecords.run();
    }

    public void init(Vector<Hashtable<String, Object>> rows, Hashtable<String, String> columnNames, String dialogTitle) {
        this.rows = rows;
        this.columnNames = columnNames;
        this.dialogTitle = dialogTitle;
    }
}
