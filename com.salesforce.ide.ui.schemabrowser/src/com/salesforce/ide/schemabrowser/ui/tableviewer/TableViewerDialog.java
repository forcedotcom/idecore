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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableViewerDialog extends TitleAreaDialog {

    Hashtable<String, String> columnNames;
    //TODO: why vector of hashtable? can we use collections?
    Vector<Hashtable<String, Object>> rows;

    private String dialogTitle;

    @Override
    protected Control getContents() {
        setShellStyle(getShellStyle() | SWT.RESIZE);
        return super.getContents();
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(newShellStyle | SWT.RESIZE);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(dialogTitle);
        return contents;
    }

    public void setData(Vector<Hashtable<String, Object>> rows, Hashtable<String, String> columnNames, String dialogTitle) {
        this.rows = rows;
        this.columnNames = columnNames;
        this.dialogTitle = dialogTitle;
        setTitle(dialogTitle);
    }

    public TableViewerDialog(Shell parentShell) {
        super(parentShell);

    }

    @Override
    protected Point getInitialSize() {
        Point p = new Point(400, 400);
        // return super.getInitialSize();
        return p;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        // return new TableComp(composite, SWT.EMBEDDED, null, thisProject);
        // Create a table
        Table table = new Table(composite, SWT.FULL_SELECTION | SWT.BORDER);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        Enumeration<String> enumer = columnNames.keys();

        while (enumer.hasMoreElements()) {
            TableColumn one = new TableColumn(table, SWT.LEFT);
            one.setText(enumer.nextElement());
        }

        table.setHeaderVisible(true);

        for (int i = 0; i < rows.size(); i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            Hashtable<String, Object> record = rows.get(i);
            enumer = columnNames.keys();
            int counter = 0;
            while (enumer.hasMoreElements()) {
                String fieldName = enumer.nextElement();
                item.setText(counter, (String) record.get(fieldName));
                counter++;
            }
        }

        for (int i = 0; i < table.getColumns().length; i++) {
            table.getColumn(i).pack();
        }
        table.setLinesVisible(true);

        return composite;
        // return x(parent);

    }

}
