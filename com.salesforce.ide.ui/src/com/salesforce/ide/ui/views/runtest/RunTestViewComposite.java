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
package com.salesforce.ide.ui.views.runtest;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.salesforce.ide.core.internal.utils.LoggingInfo;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ApexCodeLocation;
import com.salesforce.ide.ui.views.LoggingComposite;

/**
 * Legacy class
 * 
 * @author cwall
 */
public class RunTestViewComposite extends Composite {

    private SashForm sashForm = null;
    private Composite leftHandComposite = null;
    private Composite rightHandComposite = null;
    private Button btnClear = null, btnRun = null;
    protected Tree resultsTree = null;
    protected CLabel cLabel = null, cLabel1 = null;
    protected Scale scale = null;
    private Text textArea = null;
    private Text userLogsTextArea = null;
    protected RunTestView runView;
    protected IProject project = null;
    private LoggingComposite loggingComposite;

    public RunTestViewComposite(Composite parent, int style, RunTestView view) {
        super(parent, style);
        this.runView = view;
        initialize();
    }

    private void initialize() {
        createSashForm();
        setSize(new Point(568, 344));
        setLayout(new GridLayout());
    }

    // This method initializes sashForm
    private void createSashForm() {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = GridData.FILL;
        sashForm = new SashForm(this, SWT.NONE);
        sashForm.setLayoutData(gridData);
        createLeftHandComposite();
        createRightHandComposite();
    }

    private void createLeftHandComposite() {
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.horizontalAlignment = GridData.FILL;
        gridData1.verticalAlignment = GridData.FILL;
        gridData1.horizontalSpan = 3;
        gridData1.grabExcessVerticalSpace = true;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        leftHandComposite = new Composite(sashForm, SWT.NONE);
        leftHandComposite.setLayout(gridLayout);
        // clear button
        btnClear = new Button(leftHandComposite, SWT.NONE);
        btnClear.setText("Clear");
        btnClear.setEnabled(false);
        btnClear.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
            @Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
                resultsTree.removeAll();
                if (textArea != null) {
                    textArea.setText("");
                    userLogsTextArea.setText("");
                }
            }
        });

        // run again button
        btnRun = new Button(leftHandComposite, SWT.NONE);
        btnRun.setText("Re-Run");
        btnRun.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
            @Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent event) {
                runView.reRunTests();
            }
        });
        btnRun.setEnabled(false);
        resultsTree = new Tree(leftHandComposite, SWT.BORDER);
        resultsTree.setLinesVisible(true);
        resultsTree.setLayoutData(gridData1);
        resultsTree.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent arg0) {
                if (arg0.getSource() != null && arg0.getSource() instanceof Tree
                        && Utils.isNotEmpty(((Tree) arg0.getSource()).getSelection())) {
                    Object data = ((Tree) arg0.getSource()).getSelection()[0].getData("location");
                    if (data != null && data instanceof ApexCodeLocation) {
                        if (((Tree) arg0.getSource()).getSelection()[0].getData("file") != null) {
                            runView.highlightLine((ApexCodeLocation) data);
                        }
                    }
                }
            }

            @Override
            public void mouseDown(MouseEvent arg0) {}

            @Override
            public void mouseUp(MouseEvent arg0) {
                if (arg0.getSource() != null && arg0.getSource() instanceof Tree
                        && Utils.isNotEmpty(((Tree) arg0.getSource()).getSelection())) {
                    Object data = ((Tree) arg0.getSource()).getSelection()[0].getData("location");
                    if (data != null && data instanceof ApexCodeLocation) {
                        if (((Tree) arg0.getSource()).getSelection()[0].getData("line") != null) {
                            runView.highlightLine((ApexCodeLocation) data);
                        }
                    }
                }
            }
        });
    }

    private void createRightHandComposite() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        rightHandComposite = new Composite(sashForm, SWT.NONE);
        rightHandComposite.setLayout(gridLayout);

        loggingComposite =
                new LoggingComposite(rightHandComposite, runView.getLoggingService(), SWT.NONE, false,
                        LoggingInfo.SupportedFeatureEnum.RunTest);

        GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.grabExcessVerticalSpace = true;
        gridData2.horizontalSpan = 2;
        gridData2.minimumHeight=200;
        @SuppressWarnings("unused")
        Label filler1 = new Label(rightHandComposite, SWT.NONE);
        textArea = new Text(rightHandComposite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
        textArea.setLayoutData(gridData2);
        
        GridData gridData3 = new GridData(GridData.FILL_HORIZONTAL);
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.grabExcessVerticalSpace = true;
        gridData3.horizontalSpan = 2;
        gridData3.minimumHeight=200;
        @SuppressWarnings("unused")
        Label filler2 = new Label(rightHandComposite, SWT.NONE);
        userLogsTextArea = new Text(rightHandComposite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
        userLogsTextArea.setLayoutData(gridData3);
    }

    public Tree getTree() {
        return resultsTree;
    }

    public Text getTextArea() {
        return textArea;
    }
    
    public Text getUserLogsTextArea() {
        return userLogsTextArea;
    }

    public void enableComposite() {
        btnClear.setEnabled(true);
        loggingComposite.enable(runView.getProject());
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public Button getBtnRun() {
        return btnRun;
    }

    public void setBtnRun(Button btnRun) {
        this.btnRun = btnRun;
    }
} // @jve:decl-index=0:visual-constraint="10,10"
