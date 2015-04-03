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

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.salesforce.ide.core.internal.utils.SoqlEnum;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.schemabrowser.ui.tableviewer.QueryTableViewer;
import com.sforce.soap.partner.wsc.QueryResult;

/**
 * Legacy class
 *
 * @author dcarroll
 */
public class SchemaEditorComposite extends Composite {

    private SashForm sashForm = null;
    private Composite composite = null;
    private Composite composite1 = null;
    private CLabel Schema = null;
    private Button buttonRefresh = null;
    private Tree tree = null;
    private CLabel cLabel = null;
    private Button buttonRun = null;
    private SashForm sashForm1 = null;
    private Text textSOQL = null;
    private QueryTableViewer queryTableViewer;
    private final IProject project;
    private Composite composite2 = null;

    public SchemaEditorComposite(Composite parent, int style, IProject project, QueryTableViewer queryTableViewer)
            throws ForceConnectionException, ForceRemoteException {
        super(parent, style);
        this.project = project;
        this.queryTableViewer = queryTableViewer;
        initialize();
    }

    private void initialize() throws ForceConnectionException, ForceRemoteException {
        GridLayout gridLayout = new GridLayout();
        createSashForm();

        setLayout(gridLayout);
    }

    public QueryTableViewer getQueryTableViewer() {
        return queryTableViewer;
    }

    public void setQueryTableViewer(QueryTableViewer queryTableViewer) {
        this.queryTableViewer = queryTableViewer;
    }

    private CLabel cLabel1 = null;

    public Button getButtonRun() {
        return buttonRun;
    }

    public Button getButtonRefresh() {
        return buttonRefresh;
    }

    public Tree getTree() {
        return tree;
    }

    public Text getTextSOQL() {
        return textSOQL;
    }

    // This method initializes sashForm
    private void createSashForm() throws ForceConnectionException, ForceRemoteException {
        GridData gridData = new GridData();
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        sashForm = new SashForm(this, SWT.BORDER);
        sashForm.setLayoutData(gridData);
        createComposite();
        createComposite1();
    }

    // This method initializes composite
    private void createComposite() throws ForceConnectionException, ForceRemoteException {
        GridData gridData3 = new GridData();
        gridData3.grabExcessHorizontalSpace = true;
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.numColumns = 2;
        composite = new Composite(sashForm, SWT.NONE);
        composite.setLayout(gridLayout2);
        cLabel = new CLabel(composite, SWT.NONE);
        cLabel.setText("Query Results");
        cLabel.setLayoutData(gridData3);
        buttonRun = new Button(composite, SWT.NONE);
        buttonRun.setText("Run Me");
        createSashForm1();
    }

    // This method initializes composite1
    private void createComposite1() {
        GridData gridData2 = new GridData();
        gridData2.horizontalSpan = 2;
        gridData2.grabExcessVerticalSpace = true;
        gridData2.horizontalAlignment = GridData.FILL;
        gridData2.verticalAlignment = GridData.FILL;
        gridData2.grabExcessHorizontalSpace = true;
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 2;
        composite1 = new Composite(sashForm, SWT.NONE);
        composite1.setLayout(gridLayout1);
        Schema = new CLabel(composite1, SWT.NONE);
        Schema.setText("Schema");
        Schema.setLayoutData(gridData1);
        buttonRefresh = new Button(composite1, SWT.NONE);
        buttonRefresh.setText("Refresh Schema");

        tree = new Tree(composite1, SWT.NONE);
        tree.setLayoutData(gridData2);
    }

    // This method initializes sashForm1
    private void createSashForm1() throws ForceConnectionException, ForceRemoteException {
        GridData gridData4 = new GridData();
        gridData4.horizontalSpan = 2;
        gridData4.verticalAlignment = GridData.FILL;
        gridData4.grabExcessHorizontalSpace = true;
        gridData4.grabExcessVerticalSpace = true;
        gridData4.horizontalAlignment = GridData.FILL;
        sashForm1 = new SashForm(composite, SWT.BORDER);
        sashForm1.setOrientation(SWT.VERTICAL);

        createComposite2();
        sashForm1.setWeights(new int[] { 20, 80 });
        sashForm1.setLayoutData(gridData4);
    }

    public void loadTable(QueryResult qr) {
        queryTableViewer.loadTable(qr);
    }

    // This method initializes composite2
    private void createComposite2() throws ForceConnectionException, ForceRemoteException {
        GridData gridData6 = new GridData();
        gridData6.horizontalAlignment = GridData.FILL;
        gridData6.grabExcessHorizontalSpace = true;
        gridData6.heightHint = 6;
        gridData6.verticalAlignment = GridData.CENTER;
        GridData gridData5 = new GridData();
        gridData5.horizontalAlignment = GridData.FILL;
        gridData5.grabExcessHorizontalSpace = true;
        gridData5.grabExcessVerticalSpace = true;
        gridData5.verticalAlignment = GridData.FILL;
        composite2 = new Composite(sashForm1, SWT.NONE);
        composite2.setLayout(new GridLayout());
        textSOQL = new Text(composite2, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        textSOQL.setText(SoqlEnum.getSchemaInitalizationQuery());
        textSOQL.setLayoutData(gridData5);
        cLabel1 = new CLabel(composite2, SWT.NONE);
        cLabel1.setText("");
        cLabel1.setLayoutData(gridData6);
        queryTableViewer.setProject(project);
        queryTableViewer.initialize(sashForm1);
    }
}
