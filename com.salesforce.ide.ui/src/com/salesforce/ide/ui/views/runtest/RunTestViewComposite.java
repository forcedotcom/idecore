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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.salesforce.ide.core.internal.utils.LoggingInfo;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.services.LoggingService;
import com.salesforce.ide.ui.views.LoggingComposite;
import com.sforce.soap.metadata.LogInfo;

/**
 * The UI manager for Apex Test Runner view. All this does is generate and
 * update texts.
 * 
 * @author jwidjaja
 *
 */
public class RunTestViewComposite extends Composite {
	
    private SashForm sashForm = null;
    private Button btnClear = null;
    private Tree resultsTree = null;
    private TabFolder tabFolder = null;
    private Table codeCovArea = null;
    private Text stackTraceArea = null;
    private Text systemLogsTextArea = null;
    private Text userLogsTextArea = null;
    private ProgressBar progressBar = null;
    private String progressText = "%d out of %d tests finished";
    protected RunTestView runView = null;
    protected IProject project = null;
    private LoggingComposite loggingComposite = null;

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
    
    /**
     * Create a resizeable view with left and right columns.
     */
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
    
    /**
     * Create the left side which consists of a Clear button,
     * the logging controls, and the test results tree.
     */
    private void createLeftHandComposite() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 5;
        Composite leftHandComposite = new Composite(sashForm, SWT.NONE);
        leftHandComposite.setLayout(gridLayout);
        
        // Clear button
        btnClear = new Button(leftHandComposite, SWT.NONE);
        btnClear.setText(Messages.RunTestView_Clear);
        btnClear.setEnabled(true);
        btnClear.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
            @Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
            	clearAll();
            }
        });
        
        // Logging controls
        loggingComposite =
                new LoggingComposite(leftHandComposite, runView.getLoggingService(), SWT.NONE, false,
                        LoggingInfo.SupportedFeatureEnum.RunTest);
        
        // Test results tree
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.horizontalAlignment = SWT.FILL;
        gridData1.verticalAlignment = SWT.FILL;
        gridData1.horizontalSpan = 4;
        gridData1.grabExcessVerticalSpace = true;
        resultsTree = new Tree(leftHandComposite, SWT.BORDER);
        resultsTree.setLinesVisible(true);
        resultsTree.setLayoutData(gridData1);
        resultsTree.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent arg0) {}

            @Override
            public void mouseDown(MouseEvent arg0) {}

            @Override
            public void mouseUp(MouseEvent arg0) {
            	TreeItem selectedTreeItem = null;
            	String selectedTabName = null;
            	
            	// Get the selected tree item
            	if (arg0.getSource() != null && arg0.getSource() instanceof Tree
                        && Utils.isNotEmpty(((Tree) arg0.getSource()).getSelection())) {
            		selectedTreeItem = ((Tree) arg0.getSource()).getSelection()[0];
                }
            	// Get the selected tab name
            	if (tabFolder != null && tabFolder.getSelection() != null && tabFolder.getSelection().length > 0) {
            		TabItem selectedTabItem = tabFolder.getSelection()[0];
            		selectedTabName = selectedTabItem.getText();
            	}
            	// Let the view do the work
            	runView.updateView(selectedTreeItem, selectedTabName);
            }
        });
    }
    
    /**
     * Create the right side which consists of a progress bar and three tabs for
     * Stack Trace, System Debug Log, and User Debug Log.
     */
    private void createRightHandComposite() {
    	GridLayout gridLayout = new GridLayout();
        Composite rightHandComposite = new Composite(sashForm, SWT.NONE);
        rightHandComposite.setLayout(gridLayout);
        
    	// A folder with three tabs
    	tabFolder = new TabFolder(rightHandComposite, SWT.NONE);
    	
    	// Code coverage
    	TabItem tab1 = new TabItem(tabFolder, SWT.NONE);
    	tab1.setText(Messages.RunTestView_CodeCoverage);
    	codeCovArea = createTableForTabItem(tabFolder, tab1);
    	// Stack trace
    	TabItem tab2 = new TabItem(tabFolder, SWT.NONE);
    	tab2.setText(Messages.RunTestView_StackTrace);
    	stackTraceArea = createTextAreaForTabItem(tabFolder, tab2);
    	// System debug log
    	TabItem tab3 = new TabItem(tabFolder, SWT.NONE);
    	tab3.setText(Messages.RunTestView_SystemLog);
    	systemLogsTextArea = createTextAreaForTabItem(tabFolder, tab3);
    	// User debug log
    	TabItem tab4 = new TabItem(tabFolder, SWT.NONE);
    	tab4.setText(Messages.RunTestView_UserLog);
    	userLogsTextArea = createTextAreaForTabItem(tabFolder, tab4);
    	
    	tabFolder.addSelectionListener(new SelectionAdapter() {
    		public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
    			TreeItem selectedTreeItem = null;
            	String selectedTabName = null;
            	
            	if (resultsTree != null && resultsTree.getSelectionCount() > 0) {
            		selectedTreeItem = resultsTree.getSelection()[0];
            	}
            	
            	if (event.getSource() != null && event.getSource() instanceof TabFolder) {
            		selectedTabName = ((TabFolder) event.getSource()).getSelection()[0].getText();
            	}
            	
            	runView.updateView(selectedTreeItem, selectedTabName);
    		}
    	});
    	
    	GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = true;
        gridData1.horizontalAlignment = SWT.FILL;
        gridData1.verticalAlignment = SWT.FILL;
        gridData1.grabExcessVerticalSpace = true;
        tabFolder.setLayoutData(gridData1);
        
        GridData gridData2 = new GridData();
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.horizontalAlignment = SWT.FILL;
        gridData2.verticalAlignment = SWT.END;
        progressBar = new ProgressBar(rightHandComposite, SWT.SMOOTH);
        progressBar.setLayoutData(gridData2);
        progressBar.setToolTipText(String.format(progressText, 0, 0));
    }
    
    /**
     * Create a default text area for a tab item.
     * @param parent
     * @param tab
     * @return Text widget
     */
    private Text createTextAreaForTabItem(Composite parent, TabItem tab) {
    	Text textArea = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
    	textArea.setEditable(false);
	    GridData gridData = new GridData();
	    gridData.grabExcessHorizontalSpace = true;
	    gridData.grabExcessVerticalSpace = true;
	    textArea.setLayoutData(gridData);
	    tab.setControl(textArea);
	    
	    return textArea;
    }
    
    /**
     * Create a default three column table for code coverage
     * @param parent
     * @param tab
     * @return Table widget
     */
    private Table createTableForTabItem(Composite parent, TabItem tab) {
    	Table table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
    	table.setLinesVisible(true);
    	table.setHeaderVisible(true);
    	GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    	gridData.heightHint = 200;
    	table.setLayoutData(gridData);
    	
    	TableColumn column1 = new TableColumn(table, SWT.NONE);
    	column1.setText(Messages.RunTestView_CodeCoverageClass);
    	
    	TableColumn column2 = new TableColumn(table, SWT.NONE);
    	column2.setText(Messages.RunTestView_CodeCoveragePercent);
    	
    	TableColumn column3 = new TableColumn(table, SWT.NONE);
    	column3.setText(Messages.RunTestView_CodeCoverageLines);
    	
    	tab.setControl(table);
    	
    	return table;
    }

    public Tree getTree() {
        return resultsTree;
    }

    public Text getStackTraceArea() {
        return stackTraceArea;
    }
    
    public void setStackTraceArea(String data) {
    	if (stackTraceArea != null) {
    		stackTraceArea.setText(data);
    	}
    }
    
    public Text getSystemLogsTextArea() {
    	return systemLogsTextArea;
    }
    
    public void setSystemLogsTextArea(String data) {
    	if (systemLogsTextArea != null) {
    		systemLogsTextArea.setText(data);
    	}
    }
    
    public Text getUserLogsTextArea() {
        return userLogsTextArea;
    }
    
    public void setUserLogsTextArea(String data) {
    	if (userLogsTextArea != null) {
    		userLogsTextArea.setText(data);
    	}
    }
    
    public void setProgress(int min, int max, int cur) {
    	if (progressBar != null && !progressBar.isDisposed()) {
    		if (!progressBar.isVisible()) {
    			progressBar.setVisible(true);
    		}
    		
    		progressBar.setMinimum(min);
    		progressBar.setMaximum(max);
    		progressBar.setSelection(cur);
    		progressBar.setToolTipText(String.format(progressText, cur, max));
    	}
    }
    
    public void setCodeCoverage(List<String> classNames, List<String> percent, List<String> lines) {
    	if (codeCovArea != null) {
    		FontRegistry registry = new FontRegistry();
	        Font boldFont = registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());
	        
    		for (int i = 0; i < classNames.size(); i++) {
    			TableItem classItem = new TableItem(codeCovArea, SWT.NONE);
    			classItem.setText(new String[] {classNames.get(i), percent.get(i), lines.get(i)});
    			if (classNames.get(i).equals(Messages.RunTestView_CodeCoverageOverall)) {
    				classItem.setFont(boldFont);
    			}
    		}
    		
    		for (TableColumn col : codeCovArea.getColumns()) {
    			col.pack();
    		}
    		
    		Rectangle r = codeCovArea.getClientArea();
    		codeCovArea.getColumn(0).setWidth(r.width * 50 / 100);
    		codeCovArea.getColumn(1).setWidth(r.width * 25 / 100);
    		codeCovArea.getColumn(2).setWidth(r.width * 25 / 100);
    	}
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
    
    public void clearAll() {
    	clearResultsTree();
    	clearTabs();
    	clearProgress();
    }
    
    public void clearResultsTree() {
    	if (resultsTree != null) {
    		resultsTree.removeAll();
    	}
    	if (codeCovArea != null) {
    		codeCovArea.removeAll();
    		codeCovArea.clearAll();
    	}
    }
    
    public void clearTabs() {
    	if (stackTraceArea != null) {
        	stackTraceArea.setText("");
        }
        if (systemLogsTextArea != null) {
        	systemLogsTextArea.setText("");
        }
        if (userLogsTextArea != null) {
        	userLogsTextArea.setText("");
        }
    }
    
    public void clearProgress() {
    	if (progressBar != null) {
        	setProgress(0, 0, 0);
        }
    }
    
    public LogInfo[] getLogInfoAndType() {
    	if (Utils.isNotEmpty(loggingComposite)) {
    		LoggingService loggingService = runView.getLoggingService();
    		return loggingService.getAllLogInfo(project, LoggingInfo.SupportedFeatureEnum.RunTest);
    	}
    	return null;
    }
}
