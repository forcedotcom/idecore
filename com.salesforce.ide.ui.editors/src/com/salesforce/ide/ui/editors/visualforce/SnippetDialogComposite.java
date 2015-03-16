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
package com.salesforce.ide.ui.editors.visualforce;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.composite.BaseComposite;

/**
 * Legacy class
 *
 * @author dcarroll
 */
public class SnippetDialogComposite extends BaseComposite {
    static final Logger logger = Logger.getLogger(SnippetDialogComposite.class);

    private Group groupMergeFields = null;
    protected List listObjects = null;
    protected List listFields = null;
    private Button btnInsertField = null;
    protected List listSnippets = null;
    private Button btnInsertSnippet = null;
    protected String selectedField;
    protected String selectedSnippet;
    private StructuredTextEditor editor;
    protected SnippetDialog hostDialog;
    private CLabel cLabel = null;
    private CLabel cLabel1 = null;

    public SnippetDialogComposite(Composite parent, int style, SnippetDialog hostDialog) {
        super(parent, style);
        this.hostDialog = hostDialog;
        this.editor = this.hostDialog.getEditor();
        initialize();
    }

    private void initialize() {
        initializeComposite();
        getSnippetDialogController().initializeLists(listObjects, listSnippets);
    }

    private void initializeComposite() {
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 2;
        createGroupMergeFields();
        setLayout(gridLayout1);
    }

    protected SnippetDialogController getSnippetDialogController() {
        return hostDialog.getSnippetDialogController();
    }

    public void setEditor(StructuredTextEditor editor) {
        this.editor = editor;
    }

    private boolean addTextToEditor(String text) {
        if (Utils.isEmpty(text)) {
            Utils.openWarn("No Selection Found", "Please select a field or snippet to insert");
            return false;
        }
        editor.getTextViewer().getTextWidget().insert(text);
        return true;
    }

    boolean addFieldToDocument() {
        return addTextToEditor(selectedField);
    }

    boolean addSnippetToDocument() {
        return addTextToEditor(selectedSnippet);
    }

    // This method initializes groupMergeFields
    private void createGroupMergeFields() {
        GridData gridData12 = new GridData();
        gridData12.horizontalSpan = 3;
        gridData12.verticalAlignment = GridData.CENTER;
        gridData12.grabExcessHorizontalSpace = true;
        gridData12.heightHint = 2;
        gridData12.horizontalAlignment = GridData.FILL;
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.CENTER;
        gridData3.horizontalSpan = 3;
        gridData3.verticalAlignment = GridData.CENTER;
        GridData gridData21 = new GridData();
        gridData21.grabExcessHorizontalSpace = true;
        gridData21.verticalAlignment = GridData.FILL;
        gridData21.grabExcessVerticalSpace = true;
        gridData21.horizontalSpan = 3;
        gridData21.heightHint = 120;
        gridData21.horizontalAlignment = GridData.FILL;
        GridData gridData11 = new GridData();
        gridData11.widthHint = 100;
        gridData11.verticalAlignment = GridData.CENTER;
        gridData11.horizontalSpan = 2;
        gridData11.horizontalAlignment = GridData.CENTER;
        GridData gridData2 = new GridData();
        gridData2.widthHint = 150;
        gridData2.verticalSpan = 2;
        gridData2.heightHint = 150;
        GridData gridData1 = new GridData();
        gridData1.horizontalSpan = 2;
        gridData1.heightHint = 125;
        gridData1.horizontalAlignment = GridData.CENTER;
        gridData1.verticalAlignment = GridData.BEGINNING;
        gridData1.widthHint = 250;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.widthHint = -1;
        gridData.horizontalSpan = 2;
        gridData.verticalAlignment = GridData.FILL;
        groupMergeFields = new Group(this, SWT.NONE);
        groupMergeFields.setLayoutData(gridData);
        groupMergeFields.setLayout(gridLayout);
        groupMergeFields.setText("Merge Fields");
        listObjects = new List(groupMergeFields, SWT.V_SCROLL);
        listObjects.setLayoutData(gridData2);
        listObjects.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    getSnippetDialogController().loadFieldList(listFields, listObjects.getSelection()[0]);
                } catch (Exception ex) {
                    logger.error("Unable to create group merge field.", ForceExceptionUtils.getRootCause(ex));
                    Utils.openError(ForceExceptionUtils.getRootCause(ex), true, "Unable to create group merge field.");
                }
            }
        });
        listFields = new List(groupMergeFields, SWT.V_SCROLL);
        listFields.setLayoutData(gridData1);
        listFields.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedField = assembleMergeField(listFields.getSelection()[0]);
            }
        });
        listFields.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                selectedField = assembleMergeField(listFields.getSelection()[0]);
                if (addFieldToDocument()) {
                    hostDialog.close();
                }
            }

            @Override
            public void mouseDown(MouseEvent e) {}

            @Override
            public void mouseUp(MouseEvent e) {}
        });

        btnInsertField = new Button(groupMergeFields, SWT.NONE);
        btnInsertField.setText("Insert Field");
        btnInsertField.setLayoutData(gridData11);
        cLabel1 = new CLabel(groupMergeFields, SWT.NONE);
        cLabel1.setText("");
        cLabel1.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
        cLabel1.setLayoutData(gridData12);
        cLabel = new CLabel(groupMergeFields, SWT.NONE);
        cLabel.setText("Snippet Controls");
        @SuppressWarnings("unused")
        Label filler = new Label(groupMergeFields, SWT.NONE);
        @SuppressWarnings("unused")
        Label filler1 = new Label(groupMergeFields, SWT.NONE);
        btnInsertField.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {}

            @Override
            public void mouseDown(MouseEvent e) {}

            @Override
            public void mouseUp(MouseEvent e) {
                if (addFieldToDocument()) {
                    hostDialog.close();
                }
            }
        });
        listSnippets = new List(groupMergeFields, SWT.V_SCROLL);
        listSnippets.setLayoutData(gridData21);
        listSnippets.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                String snippet = getSnippetDialogController().getSnippet(listSnippets.getSelection()[0]);
                selectedSnippet = "{!INCLUDE(" + snippet + ")}";
            }
        });
        listSnippets.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                String snippet = getSnippetDialogController().getSnippet(listSnippets.getSelection()[0]);
                selectedSnippet = "{!INCLUDE(" + snippet + ")}";
                if (addSnippetToDocument()) {
                    hostDialog.close();
                }
            }

            @Override
            public void mouseDown(MouseEvent e) {}

            @Override
            public void mouseUp(MouseEvent e) {}
        });
        btnInsertSnippet = new Button(groupMergeFields, SWT.NONE);
        btnInsertSnippet.setText("Insert Snippet");
        btnInsertSnippet.setLayoutData(gridData3);
        btnInsertSnippet.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {}

            @Override
            public void mouseDown(MouseEvent e) {}

            @Override
            public void mouseUp(MouseEvent e) {
                if (addSnippetToDocument()) {
                    hostDialog.close();
                }
            }
        });
    }

    protected String assembleMergeField(String field) {
        StringBuffer strBuff = new StringBuffer("{!");
        if (Utils.isNotEmpty(listObjects.getSelection()[0]) && !listObjects.getSelection()[0].startsWith("$")) {
            strBuff.append(listObjects.getSelection()[0]).append(".");
        }

        strBuff.append((String) listFields.getData(listFields.getSelection()[0])).append("}");

        return strBuff.toString();
    }

    @Override
    public void validateUserInput() {

    }
}
