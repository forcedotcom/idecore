/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/

package com.salesforce.ide.ui.views.runtest;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ApexCodeLocation;
import com.salesforce.ide.core.project.MarkerUtils;

/**
 * The UI manager for Apex Test Results view. All this does is generate and
 * update texts.
 * 
 * @author jwidjaja
 *
 */
public class RunTestsViewComposite extends Composite {
	
    private SashForm sashForm = null;
    private Button btnClear = null;
    private Tree resultsTree = null;
    private TabFolder tabFolder = null;
    private Table codeCovArea = null;
    private Text stackTraceArea = null;
    private Text systemLogsTextArea = null;
    private Text userLogsTextArea = null;
    private ProgressBar progressBar = null;
    protected RunTestsView runView = null;
    protected IProject project = null;
    
    private static String[] codeCovColumnNames = new String[] { Messages.View_CodeCoverageClass, 
		Messages.View_CodeCoveragePercent, 
		Messages.View_CodeCoverageLines};

    public RunTestsViewComposite(Composite parent, int style, RunTestsView view) {
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
     * and the test results tree.
     */
    private void createLeftHandComposite() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        final Composite leftHandComposite = new Composite(sashForm, SWT.NONE);
        leftHandComposite.setLayout(gridLayout);
        
        // Clear button
        btnClear = new Button(leftHandComposite, SWT.NONE);
        btnClear.setText(Messages.View_Clear);
        btnClear.setEnabled(true);
        btnClear.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
            @Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
            	clearAll();
            }
        });
        
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
        	// Double click to open the test class. Each test class has
        	// an expand button to show the test method names, so we don't
        	// want to use single click to open the test class because that
        	// will also expand the row (which users found annoying).
            @Override
            public void mouseDoubleClick(MouseEvent e) {
            	TreeItem selectedTreeItem = null;
            	String selectedTabName = null;
            	
            	// Get the selected tree item
            	if (e.getSource() != null && e.getSource() instanceof Tree
                        && Utils.isNotEmpty(((Tree) e.getSource()).getSelection())) {
            		selectedTreeItem = ((Tree) e.getSource()).getSelection()[0];
                }
            	// Get the selected tab name
            	if (tabFolder != null && tabFolder.getSelection() != null && tabFolder.getSelection().length > 0) {
            		TabItem selectedTabItem = tabFolder.getSelection()[0];
            		selectedTabName = selectedTabItem.getText();
            	}
            	// Open the source because an item in the tree was selected so
            	// we want to take user to the class/method
            	runView.updateView(selectedTreeItem, selectedTabName, true);
            }

            @Override
            public void mouseDown(MouseEvent arg0) {}

            @Override
            public void mouseUp(MouseEvent arg0) {}
        });
        
        // Default one column in the tree
        new TreeColumn(resultsTree, SWT.LEFT);
        leftHandComposite.addControlListener(new ControlAdapter() {
        	@Override
    		public void controlResized(ControlEvent e) {
        		TreeColumn col = resultsTree.getColumn(0);
        		if (col != null) {
        			col.setWidth(leftHandComposite.getClientArea().width);
        		}
        	}
        });
    }
    
    /**
     * Create the right side which consists of a progress bar and four tabs for
     * Code Coverage, Stack Trace, System Debug Log, and User Debug Log.
     */
    private void createRightHandComposite() {
    	GridLayout gridLayout = new GridLayout();
        Composite rightHandComposite = new Composite(sashForm, SWT.NONE);
        rightHandComposite.setLayout(gridLayout);
        
    	// A folder with three tabs
    	tabFolder = new TabFolder(rightHandComposite, SWT.NONE);
    	
    	// Code coverage
    	TabItem tab1 = new TabItem(tabFolder, SWT.NONE);
    	tab1.setText(Messages.View_CodeCoverage);
    	codeCovArea = createTableForTabItem(tabFolder, tab1);
    	// Stack trace
    	TabItem tab2 = new TabItem(tabFolder, SWT.NONE);
    	tab2.setText(Messages.View_StackTrace);
    	stackTraceArea = createTextAreaForTabItem(tabFolder, tab2);
    	// System debug log
    	TabItem tab3 = new TabItem(tabFolder, SWT.NONE);
    	tab3.setText(Messages.View_SystemLog);
    	systemLogsTextArea = createTextAreaForTabItem(tabFolder, tab3);
    	// User debug log
    	TabItem tab4 = new TabItem(tabFolder, SWT.NONE);
    	tab4.setText(Messages.View_UserLog);
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
            	
            	// No need to open the source because a tab was selected
            	runView.updateView(selectedTreeItem, selectedTabName, false);
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
    }
    
    /**
     * Create a default text area for a tab item.
     * @param parent TabFolder
     * @param tab TabItem
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
     * Create a default code coverage table
     * @param parent TabFolder
     * @param tab TabItem
     * @return Table widget
     */
    private Table createTableForTabItem(final Composite parent, TabItem tab) {
    	final Table table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
    	table.setLinesVisible(true);
    	table.setHeaderVisible(true);
    	GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    	table.setLayoutData(gridData);
    	tab.setControl(table);
    	
    	Listener sortListener = getCodeCovSortListener();
    	
    	for (String columnName : codeCovColumnNames) {
    		TableColumn column = new TableColumn(table, SWT.NONE);
    		column.setText(columnName);
    		column.addListener(SWT.Selection, sortListener);
    		column.setData(RunTestsConstants.TABLE_CODE_COV_COL_DIR, SWT.UP);
    	}
    	
    	parent.addControlListener(new ControlAdapter() {
    		@Override
    		public void controlResized(ControlEvent e) {
    			packCodeCovArea(table);
    		}
    	});
    	
    	return table;
    }
    
    /**
     * Sort listener for code coverage columns
     * @return Listener widget
     */
    private Listener getCodeCovSortListener() {
    	return new Listener() {
    		public void handleEvent(Event e) {
    			@SuppressWarnings("unchecked")
				List<CodeCovResult> testResults = (List<CodeCovResult>) codeCovArea.getData(RunTestsConstants.TABLE_CODE_COV_RESULT);
    			TableColumn column = (TableColumn) e.widget;
    			
    			if (testResults == null || testResults.isEmpty()) return;
    			
    			if (column.getText().equals(Messages.View_CodeCoveragePercent)) {
    				if ((int)column.getData(RunTestsConstants.TABLE_CODE_COV_COL_DIR) == SWT.DOWN) {
    					Collections.sort(testResults, CodeCovComparators.PERCENT_ASC);
    					column.setData(RunTestsConstants.TABLE_CODE_COV_COL_DIR, SWT.UP);
    				} else {
    					Collections.sort(testResults, CodeCovComparators.PERCENT_DESC);
    					column.setData(RunTestsConstants.TABLE_CODE_COV_COL_DIR, SWT.DOWN);
    				}
    			} else if (column.getText().equals(Messages.View_CodeCoverageLines)) {
    				if ((int)column.getData(RunTestsConstants.TABLE_CODE_COV_COL_DIR) == SWT.DOWN) {
    					Collections.sort(testResults, CodeCovComparators.LINES_ASC);
    					column.setData(RunTestsConstants.TABLE_CODE_COV_COL_DIR, SWT.UP);
    				} else {
    					Collections.sort(testResults, CodeCovComparators.LINES_DESC);
    					column.setData(RunTestsConstants.TABLE_CODE_COV_COL_DIR, SWT.DOWN);
    				}
    			} else {
    				if ((int)column.getData(RunTestsConstants.TABLE_CODE_COV_COL_DIR) == SWT.DOWN) {
    					Collections.sort(testResults, CodeCovComparators.CLASSNAME_ASC);
    					column.setData(RunTestsConstants.TABLE_CODE_COV_COL_DIR, SWT.UP);
    				} else {
    					Collections.sort(testResults, CodeCovComparators.CLASSNAME_DESC);
    					column.setData(RunTestsConstants.TABLE_CODE_COV_COL_DIR, SWT.DOWN);
    				}
    			}
    			
    			setCodeCoverage(testResults);
    		}
    	};
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
    
    /**
     * Update progress bar
     * @param min
     * @param max
     * @param cur
     */
    public void setProgress(int min, int max, int cur) {
    	if (progressBar != null && !progressBar.isDisposed()) {
    		if (!progressBar.isVisible()) {
    			progressBar.setVisible(true);
    		}
    		
    		progressBar.setMinimum(min);
    		progressBar.setMaximum(max);
    		progressBar.setSelection(cur);
    	}
    }
    
    /**
     * Update code coverage table
     * @param ccResults
     */
    public void setCodeCoverage(List<CodeCovResult> ccResults) {
    	if (codeCovArea != null && ccResults != null && !ccResults.isEmpty()) {
    		clearCodeCov();
    		
    		// Double click opens the Apex class/trigger
    		codeCovArea.addMouseListener(new MouseListener() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					Point pt = new Point(e.x, e.y);
					TableItem item = codeCovArea.getItem(pt);
					if (item != null) {
						ApexCodeLocation location = (ApexCodeLocation) item.getData(RunTestsConstants.TREEDATA_CODE_LOCATION);
						runView.highlightLine(location);
					}
				}

				@Override
				public void mouseDown(MouseEvent e) {}

				@Override
				public void mouseUp(MouseEvent e) {}
    			
    		});
    		
    		codeCovArea.setData(RunTestsConstants.TABLE_CODE_COV_RESULT, ccResults);
    		
    		FontRegistry registry = new FontRegistry();
	        Font boldFont = registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());
	        
    		for (CodeCovResult res : ccResults) {
    			TableItem ccItem = new TableItem(codeCovArea, SWT.NONE);
    			String lines = String.format("%d/%d", res.getLinesCovered(), res.getLinesTotal());
    			// Overall code coverage row
    			if (res.getClassOrTriggerName().equals(Messages.View_CodeCoverageOverall)) {
    				ccItem.setFont(boldFont);
    				lines = "";
    			}
    			// Apply code coverage info and save the apex code location
    			ccItem.setText(new String[] {res.getClassOrTriggerName(), res.getPercent() + "%", lines});
    			ccItem.setData(RunTestsConstants.TREEDATA_CODE_LOCATION, res.getLoc());
    		}
    		
    		packCodeCovArea(codeCovArea);
    	}
    }
    
    /**
     * Resize code coverage columns
     */
    private void packCodeCovArea(Table table) {
    	if (table == null) return;
    	
    	for (TableColumn col : table.getColumns()) {
			col.pack();
		}
		
		Rectangle r = table.getClientArea();
		table.getColumn(0).setWidth(r.width * 70 / 100);
		table.getColumn(1).setWidth(r.width * 15 / 100);
		table.getColumn(2).setWidth(r.width * 15 / 100);
    }

    public void setClearButton(boolean enabled) {
        btnClear.setEnabled(enabled);
    }

    public void setProject(IProject project) {
        this.project = project;
    }
    
    public void clearAll() {
    	clearResultsTree();
    	clearTabs();
    	clearProgress();
    	clearCodeCov();
    	MarkerUtils.getInstance().clearCodeCoverageWarningMarkers(project);
    }
    
    public void clearAllExceptProgress() {
    	clearResultsTree();
    	clearTabs();
    	clearCodeCov();
    	MarkerUtils.getInstance().clearCodeCoverageWarningMarkers(project);
    }
    
    public void clearResultsTree() {
    	if (resultsTree != null) {
    		resultsTree.removeAll();
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
    
    public void clearCodeCov() {
    	if (codeCovArea != null) {
    		codeCovArea.setData(RunTestsConstants.TABLE_CODE_COV_RESULT, null);
    		codeCovArea.removeAll();
    		codeCovArea.clearAll();
    	}
    }
}
