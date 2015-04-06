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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.SoqlEnum;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.XmlConstants;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.sforce.soap.partner.wsc.QueryResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

/**
 * Legacy class
 *
 * @author dcarroll
 */
public class QueryTableViewer {

    private String[] columnNames = new String[] { "completed", "description", "owner", "percent" };
    private IProject project = null;
    private Composite parentComposite = null;
    protected Table table;
    protected TableViewer tableViewer;
    protected DataRowList taskList = new DataRowList();
    protected ConnectionFactory connectionFactory = null;

    //   C O N S T R U C T O R S
    public QueryTableViewer() {
        super();
    }

    //   M E T H O D S
    public ConnectionFactory getConnectionFactory() {
        if (connectionFactory == null) {
            connectionFactory = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory();
        }
        return connectionFactory;
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public Composite getParentComposite() {
        return parentComposite;
    }

    public void setParentComposite(Composite parentComposite) {
        this.parentComposite = parentComposite;
    }

    public void initialize(Composite parent) throws ForceConnectionException, ForceRemoteException {
        addChildControls(parent);
    }

    /**
     * Release resources
     */
    public void dispose() {
        // Tell the label provider to release its resources
        tableViewer.getLabelProvider().dispose();
    }

    /**
     * Create a new shell, add the widgets, open the shell
     *
     * @return the shell that was created
     * @throws ConnectionException
     * @throws ConnectionException
     * @throws ForceConnectionException
     * @throws ForceRemoteException 
     */
    private void addChildControls(final Composite composite) throws ForceConnectionException, ForceRemoteException {
        parentComposite = composite;
        Connection connection = getConnectionFactory().getConnection(project);
        QueryResult qr = connection.query(SoqlEnum.getSchemaInitalizationQuery());
        Table table = createTable(composite, qr);

        // Create and setup the TableViewer
        createTableViewer(table);
        tableViewer.setContentProvider(new SchemaContentProvider());
        tableViewer.setLabelProvider(new CellLabelProvider());

        // The input for the table viewer is the instance of ExampleTaskList
        taskList = new DataRowList(qr);
        tableViewer.setInput(taskList);
    }

    public void loadTable(QueryResult qr) {
        table = createTable(parentComposite, qr);

        createTableViewer(table);
        tableViewer.setContentProvider(new SchemaContentProvider());
        tableViewer.setLabelProvider(new CellLabelProvider());

        // The input for the table viewer is the instance of ExampleTaskList
        taskList = new DataRowList(qr);
        if (taskList.getTasks().size() > 0) {
            tableViewer.setInput(taskList);
            parentComposite.update();
            parentComposite.redraw();
            parentComposite.layout(true);
        }
    }

    Table createTable(QueryResult qr) {
        return createTable(parentComposite, qr);
    }

    Table createTable(Composite parent, QueryResult qr) {

        if (table != null) {
            table.dispose();
            table = null;
        }
        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
        table = new Table(parent, style);

        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setToolTipText("Double click on parent or child cells (when present) to see the related records");

        ArrayList<String> _columnNames = new ArrayList<>();

        if (qr != null && qr.getSize() > 0) {
            if (Utils.isNotEmpty(qr.getRecords())) {
                Iterator<XmlObject> it = qr.getRecords()[0].getChildren();
                int columnTally = 0;
                while (it.hasNext()) {
                    XmlObject field = it.next();
                    String fieldNameLC = field.getName().getLocalPart();
                    if (("Id".equalsIgnoreCase(fieldNameLC) && (field.getValue() == null))
                            || ("Id".equalsIgnoreCase(fieldNameLC) && _columnNames.contains(field.getName()
                                    .getLocalPart()))) {
                        // Skip this
                    } else {
                        if (XmlConstants.ELEM_TYPE.equals(field.getName().getLocalPart())) {
                            // skip this too
                        } else {
                            if (!_columnNames.contains(field.getName().getLocalPart())) {
                                // Create a new column
                                TableColumn col = new TableColumn(table, SWT.LEFT, columnTally);
                                col.setText(field.getName().getLocalPart());
                                col.setWidth(120);
                                _columnNames.add(field.getName().getLocalPart());
                                columnTally++;
                            }
                        }

                    }
                }
            } else {
                // W-519894
            }
            columnNames = _columnNames.toArray(new String[_columnNames.size()]);
        }

        ((SashForm) parent).setWeights(new int[] { 20, 80 });
        // parent.pack();
        return table;
    }

    /**
     * Create the TableViewer
     */
    private void createTableViewer(Table table) {

        tableViewer = new TableViewer(table);

        tableViewer.setUseHashlookup(true);
        tableViewer.setColumnProperties(columnNames);
        
        final Table t = table;
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Point pt = new Point(e.x, e.y);
                TableItem item = t.getItem(pt);
                for (int column = 0; column < t.getColumnCount(); column++) {
                    Rectangle rect = item.getBounds(column);
                    if (rect.contains(pt)) {
                        DataRow dr = (DataRow) item.getData();
                        XmlObject val = dr.getRecord().get(column);
                        if (val != null && val.getXmlType() != null) {
                            showDialog(val);
                        }
                    }
                }
            }
        });
    }
    
    // This code was in the (deleted) CellModifier class
    private static void showDialog(XmlObject val) {
        
        Hashtable<String, String> columnNames = new Hashtable<>();
        Vector<Hashtable<String, Object>> rows = new Vector<>();

        String dialogTitle = null;
        if ("sObject".equals(val.getXmlType().getLocalPart())) {
            dialogTitle = "Parent Record " + val.getName().getLocalPart();
            Iterator<XmlObject> iter = val.getChildren();
            Hashtable<String, Object> row = new Hashtable<>();
            ArrayList<String> rowsNames = new ArrayList<>();
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
                    Hashtable<String, Object> row = new Hashtable<>();
                    ArrayList<String> rowsNames = new ArrayList<>();
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

    /**
     * InnerClass that acts as a proxy for the ExampleTaskList providing content for the Table. It implements the
     * ITaskListViewer interface since it must register changeListeners with the ExampleTaskList
     */
    class SchemaContentProvider implements IStructuredContentProvider, IDataRowListViewer {
        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
            if (newInput != null) {
                ((DataRowList) newInput).addChangeListener(this);
            }
            if (oldInput != null) {
                ((DataRowList) oldInput).removeChangeListener(this);
            }
        }

        @Override
        public void dispose() {
            taskList.removeChangeListener(this);
        }

        // Return the tasks as an array of Objects
        @Override
        public Object[] getElements(Object parent) {
            return taskList.getTasks().toArray();
        }

        /*
         * (non-Javadoc)
         *
         * @see ITaskListViewer#addTask(ExampleTask)
         */
        @Override
        public void addTask(DataRow task) {
            tableViewer.add(task);
        }

        /*
         * (non-Javadoc)
         *
         * @see ITaskListViewer#removeTask(ExampleTask)
         */
        @Override
        public void removeTask(DataRow task) {
            tableViewer.remove(task);
        }

        /*
         * (non-Javadoc)
         *
         * @see ITaskListViewer#updateTask(ExampleTask)
         */
        @Override
        public void updateTask(DataRow task) {
            tableViewer.update(task, null);
        }
    }

    /**
     * Return the column names in a collection
     *
     * @return List containing column names
     */
    public java.util.List<String> getColumnNames() {
        return Arrays.asList(columnNames);
    }

    /**
     * @return currently selected item
     */
    public ISelection getSelection() {
        return tableViewer.getSelection();
    }

    /**
     * Return the ExampleTaskList
     */
    public DataRowList getTaskList() {
        return taskList;
    }

    /**
     * Return the parent composite
     */
    public Control getControl() {
        return tableViewer.getTable().getParent();
    }

    public Table getTable() {
        return table;
    }
}
