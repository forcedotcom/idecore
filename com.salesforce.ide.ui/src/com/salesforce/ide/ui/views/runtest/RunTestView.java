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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ApexCodeLocation;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.tooling.RunTestsDelegate;
import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.utils.UIConstants;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.ui.views.BaseViewPart;
import com.sforce.soap.metadata.LogInfo;
import com.sforce.soap.tooling.ApexLog;
import com.sforce.soap.tooling.ApexTestOutcome;
import com.sforce.soap.tooling.ApexTestResult;

/**
 * This is the controller for the Apex Test Runner view. It's responsible
 * for updating the UI with the test results. The actual view is controlled by
 * RunTestViewComposite.java. The actual work of running and getting tests is
 * controlled by RunTestsDelegate.java.
 * 
 * @author jwidjaja
 *
 */
public class RunTestView extends BaseViewPart {
    private static final Logger logger = Logger.getLogger(RunTestView.class);
    
    private static RunTestView INSTANCE = null;
    
    // The name that is shown on the view tab
    public static final String VIEW_NAME = "Apex Test Runner";
    
    // Keys used to store data in a TreeItem
    public static final String TREEDATA_TEST_RESULT = "ApexTestResult";
    public static final String TREEDATA_CODE_LOCATION = "ApexCodeLocation";
    public static final String TREEDATA_APEX_LOG = "ApexLog";
    public static final String TREEDATA_APEX_LOG_USER_DEBUG = "ApexLogUserDebug";
    public static final String TREEDATA_APEX_LOG_BODY = "ApexLogBody";
    
    private RunTestViewComposite runTestComposite = null;
    private IProject project = null;
    private ForceProject forceProject = null;
    private RunTestsDelegate delegate = null;
    private LogInfo[] logInfos = null;
    
    private final Image FAILURE_ICON = ForceImages.get(ForceImages.IMAGE_FAILURE);
    private final int FAILURE_COLOR = SWT.COLOR_RED;
    private final Image WARNING_ICON = ForceImages.get(ForceImages.IMAGE_WARNING);
    private final int WARNING_COLOR = SWT.COLOR_DARK_YELLOW;
    private final Image PASS_ICON = ForceImages.get(ForceImages.IMAGE_CONFIRM);
    private final int PASS_COLOR = SWT.COLOR_DARK_GREEN;
    
    private ISelectionListener fPostSelectionListener = null;
    
    public RunTestView() {
        super();
        setSelectionListener();
        
        INSTANCE = this;
    }
    
    public static RunTestView getInstance() {
    	if (Utils.isEmpty(INSTANCE)) {
    		// We use Display.syncExec because getting a view has to be done
    		// on a UI thread.
    		Display display = PlatformUI.getWorkbench().getDisplay();
    		if (Utils.isEmpty(display)) return INSTANCE;
    		
    		display.syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						INSTANCE = (RunTestView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(UIConstants.RUN_TEST_VIEW_ID);
					} catch (PartInitException e) {}
				}
			});
    	}
    	
    	return INSTANCE;
    }
    
    /**
     * Run the tests, get the results, and update the UI.
     * @param project
     * @param testResources
     * @param testsInJson
     * @param delegate
     */
    public void runTests(final IProject project, Map<String, IResource> testResources, String testsInJson, final RunTestsDelegate delegate) {
    	// Prepare the UI and get the log infos
    	Display display = PlatformUI.getWorkbench().getDisplay();
    	display.syncExec(new Runnable() {
			@Override
			public void run() {
				setProject(project, delegate);
				runTestComposite.clearAll();
				logInfos = runTestComposite.getLogInfoAndType();
			}
    	});
    	
    	// Non UI work (get project, get user ID, create TraceFlag, enqueue tests,
    	// get test results, and delete TraceFLag).
    	forceProject = delegate.materializeForceProject(project);
    	String userId = delegate.getUserId(forceProject, forceProject.getUserName());
    	String traceFlagId = delegate.insertTraceFlag(forceProject, logInfos, userId);
    	String testRunId = delegate.enqueueTests(forceProject, testsInJson);
    	List<ApexTestResult> testResults = delegate.getTestResults(forceProject, testRunId);
    	delegate.deleteTraceFlag(forceProject, traceFlagId);
    	
    	// Update UI with test results
    	processTestResults(project, testResources, testResults, delegate);
    }
    
    /**
     * Update the UI with the test results.
     * @param project
     * @param testResources
     * @param testResults
     * @param delegate
     */
    public void processTestResults(final IProject project, final Map<String, IResource> testResources, 
    		final List<ApexTestResult> testResults, final RunTestsDelegate delegate) {
    	Display display = PlatformUI.getWorkbench().getDisplay();
    	
    	display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (Utils.isEmpty(project) || Utils.isEmpty(testResources) || testResults == null || testResults.isEmpty() || Utils.isEmpty(delegate)) {
					return;
				}
				
				// Map of tree items whose key is apex class id and the value is the tree item
		    	Map<String, TreeItem> testClassNodes = new HashMap<String, TreeItem>();
		    	
		    	FontRegistry registry = new FontRegistry();
		        Font boldFont = registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());
		    	
		        // Reset tree
		    	Tree resultsTree = runTestComposite.getTree();
		    	resultsTree.removeAll();
		    	
		    	// Add each test result to the tree
		    	for (ApexTestResult testResult: testResults) {
		    		// Create or find the tree node for the test class
		    		String classId = testResult.getApexClassId();
		    		String className = null;
		    		if (!testClassNodes.containsKey(classId)) {
		    			TreeItem newClassNode = createTestClassTreeItem(resultsTree, boldFont);
		    			// Test result only has test class ID. Find the test class name mapped to that ID to display in UI
		    			className = testResources.containsKey(classId) ? testResources.get(classId).getName() : classId;
		    			newClassNode.setText(className);
		    			// Save the associated file in the tree item
		    			IFile testFile = getFileFromId(testResources, classId);
		    			if (Utils.isNotEmpty(testFile)) {
		    				// For test classes, always point to the first line of the file
		    				ApexCodeLocation location = new ApexCodeLocation(testFile, 1, 1);
		    				newClassNode.setData(TREEDATA_CODE_LOCATION, location);
		    			}
		    			
		    			testClassNodes.put(classId, newClassNode);
		    		}
		    		
		    		// Add the a test method tree node to the test class tree node
		    		TreeItem classNode = testClassNodes.get(classId);
		    		className = classNode.getText();
		    		
		    		// Create a tree item for the test method and save the test result
		    		TreeItem newTestMethodNode = createTestMethodTreeItem(classNode, testResult, className);
	    			// Set the color and icon of test method tree node based on test outcome
	    			setColorAndIconForNode(newTestMethodNode, testResult.getOutcome());
	    			// Update the color & icon of class tree node only if the test method
	    			// outcome is worse than what the class tree node indicates
	    			setColorAndIconForTheWorse(classNode, testResult.getOutcome());
		    	}
		    	
		    	// Expand the test classes that did not pass
		    	expandProblematicTestClasses(resultsTree);
			}
    	});
    }
    
    /**
     * Create a default TreeItem for a test class.
     * @param parent
     * @param font
     * @return TreeItem for test class
     */
    private TreeItem createTestClassTreeItem(Tree parent, Font font) {
    	TreeItem newClassNode = new TreeItem(parent, SWT.NONE);
		newClassNode.setFont(font);
		newClassNode.setExpanded(false);
		// Mark this test class as pass until we find a test method within it that says otherwise
		setColorAndIconForNode(newClassNode, ApexTestOutcome.Pass);
		return newClassNode;
    }
    
    /**
     * Set color and icon for a test method's TreeItem.
     * @param node
     * @param outcome
     */
    private void setColorAndIconForNode(TreeItem node, ApexTestOutcome outcome) {
    	if (Utils.isEmpty(node)|| Utils.isEmpty(outcome)) return;
    	
    	Display display = node.getDisplay();
    	if (outcome.equals(ApexTestOutcome.Pass)) {
    		node.setForeground(display.getSystemColor(PASS_COLOR));
    		node.setImage(PASS_ICON);
    	} else if (outcome.equals(ApexTestOutcome.Skip)) {
    		node.setForeground(display.getSystemColor(WARNING_COLOR));
    		node.setImage(WARNING_ICON);
    	} else {
    		node.setForeground(display.getSystemColor(FAILURE_COLOR));
    		node.setImage(FAILURE_ICON);
    	}
    }
    
    /**
     * Update the color & icon of a TreeItem only if the given outcome is worse than
     * what the TreeItem already indicates.
     * @param node
     * @param outcome
     */
    private void setColorAndIconForTheWorse(TreeItem node, ApexTestOutcome outcome) {
    	if (Utils.isEmpty(node) || Utils.isEmpty(outcome)) return;
    	
    	Image curImage = node.getImage();
    	boolean worseThanPass = curImage.equals(PASS_ICON) && !outcome.equals(ApexTestOutcome.Pass);
    	boolean worseThanWarning = curImage.equals(WARNING_ICON) && !outcome.equals(ApexTestOutcome.Pass) && !outcome.equals(ApexTestOutcome.Skip);
    	if (worseThanPass || worseThanWarning) {
    		setColorAndIconForNode(node, outcome);
    	}
    }
    
    /**
     * Create a default TreeItem for a test method.
     * @param classNode
     * @param testResult
     * @param className
     * @return TreeItem for test method
     */
    private TreeItem createTestMethodTreeItem(TreeItem classNode, ApexTestResult testResult, String className) {
    	TreeItem newTestMethodNode = new TreeItem(classNode, SWT.NONE);
    	newTestMethodNode.setText(testResult.getMethodName());
    	newTestMethodNode.setData(TREEDATA_TEST_RESULT, testResult);
    	
    	ApexCodeLocation location = getCodeLocationForTestMethod(newTestMethodNode, className, testResult.getMethodName(), testResult.getStackTrace());
    	newTestMethodNode.setData(TREEDATA_CODE_LOCATION, location);
    	
    	return newTestMethodNode;
    }
    
    /**
     * Get the code location of a test method. If there isn't one, we default to
     * the code location of the test class.
     * @param treeItem
     * @param className
     * @param methodName
     * @param stackTrace
     * @return ApexCodeLocation
     */
    private ApexCodeLocation getCodeLocationForTestMethod(TreeItem treeItem, String className, 
    		String methodName, String stackTrace) {    	
    	ApexCodeLocation tmLocation = getLocationFromStackLine(methodName, stackTrace);
    	ApexCodeLocation tcLocation = (ApexCodeLocation) treeItem.getParentItem().getData(TREEDATA_CODE_LOCATION);
    	// If there is no test method location, best effort is to use test class location
    	if (Utils.isEmpty(tmLocation)) {
    		tmLocation = tcLocation;
    	} else {
    		IFile file = tcLocation.getFile();
    		tmLocation.setFile(file);
    	}
    	
    	return tmLocation;
    }
    
    /**
     * Get line and column from a stack trace.
     * @param name
     * @param stackTrace
     * @return ApexCodeLocation
     */
    private ApexCodeLocation getLocationFromStackLine(String name, String stackTrace) {
        if (Utils.isEmpty(name) || Utils.isEmpty(stackTrace)) return null;

        String line = null;
        String column = null;
        try {
            String[] temp = stackTrace.split("line");
            line = temp[1].split(",")[0].trim();
            String c = temp[1].trim();
            column = c.split("column")[1].trim();
            if (Utils.isNotEmpty(column) && column.contains("\n")) {
                column = column.substring(0, column.indexOf("\n"));
            }
        } catch (Exception e) {}
        
        return new ApexCodeLocation(name, line, column);
    }
    
    /**
     * Find a resource and convert to a file.
     * @param className
     * @return
     */
    private IFile getFileFromId(Map<String, IResource> testResources, String classID) {
    	if (Utils.isNotEmpty(classID) && Utils.isNotEmpty(testResources)) {
    		IResource testResource = testResources.get(classID);
    		if (Utils.isNotEmpty(testResource)) {
    			return (IFile) testResource;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Expand the TreeItems that did not pass.
     * @param resultsTree
     */
    private void expandProblematicTestClasses(Tree resultsTree) {
    	if (Utils.isEmpty(resultsTree)) return;
    	
    	for (TreeItem classNode : resultsTree.getItems()) {
    		if (!classNode.getImage().equals(PASS_ICON)) {
    			classNode.setExpanded(true);
    		}
    	}
    }
    
    /**
     * Jump to and highlight a line based on the ApexCodeLocation.
     * @param location
     */
    public void highlightLine(ApexCodeLocation location) {
        if (Utils.isEmpty(location) || location.getFile() == null || !location.getFile().exists()) {
            Utils.openWarn("Highlight Failed", "Unable to highlight test file - file is unknown.");
            return;
        }

        HashMap<String, Integer> map = new HashMap<>();
        map.put(IMarker.LINE_NUMBER, location.getLine());
        try {
            IMarker marker = location.getFile().createMarker(IMarker.TEXT);
            marker.setAttributes(map);
            IDE.openEditor(getSite().getWorkbenchWindow().getActivePage(), marker);
        } catch (Exception e) {
            logger.error("Unable to highlight line.", e);
            Utils.openError(new InvocationTargetException(e), true, "Unable to highlight line.");
        }
    }
    
    /**
     * Update the test results tabs.
     * @param selectedTreeItem
     * @param selectedTab
     */
    public void updateView(TreeItem selectedTreeItem, String selectedTab) {
    	if (Utils.isEmpty(selectedTreeItem) || Utils.isEmpty(selectedTab) || Utils.isEmpty(runTestComposite)) {
    		return;
    	}
    	
    	// Only clear the right side because user will either select an item from the results tree
    	// or a tab. We do not want to clear the tree (on the left side).
    	runTestComposite.clearRightSide();
    	
    	ApexTestResult testResult = (ApexTestResult) selectedTreeItem.getData(TREEDATA_TEST_RESULT);
    	// If there is no test result, there is nothing to do on the right hand side so just
    	// show the test file
    	if (Utils.isEmpty(testResult)) {
    		// Get the code location and open the file
        	ApexCodeLocation location = (ApexCodeLocation) selectedTreeItem.getData(TREEDATA_CODE_LOCATION);
        	highlightLine(location);
    		return;
    	}
    	
    	// If there is an ApexTestResult to work with, then check which tab is in focus
    	// so we can update lazily.
    	switch(selectedTab) {
    	case RunTestViewComposite.STACK_TRACE:
    		showStackTrace(testResult.getMessage(), testResult.getStackTrace());
    		break;
    	case RunTestViewComposite.SYSTEM_LOG:
    		String systemLogId = testResult.getApexLogId();
    		String systemApexLog = getApexLog(selectedTreeItem, systemLogId);
    		showSystemLog(systemApexLog);
    		break;
    	case RunTestViewComposite.USER_LOG:
    		String userLogId = testResult.getApexLogId();
    		String userApexLog = getApexLog(selectedTreeItem, userLogId);
    		showUserLog(selectedTreeItem, userApexLog);
    		break;
    	}
    	
    	// Show the file after updating the right hand side
    	ApexCodeLocation location = (ApexCodeLocation) selectedTreeItem.getData(TREEDATA_CODE_LOCATION);
    	highlightLine(location);
    }
    
    /**
     * Get the body of an ApexLog. If that fails, get the toString of an ApexLog.
     * @param selectedTreeItem
     * @param logId
     * @return A string representation of an ApexLog
     */
    public String getApexLog(TreeItem selectedTreeItem, String logId) {
    	if (Utils.isEmpty(delegate) || Utils.isEmpty(forceProject) || Utils.isEmpty(selectedTreeItem) || Utils.isEmpty(logId)) return null;
    	
    	// Do we already have the log body?
    	String apexLogBody = (String) selectedTreeItem.getData(TREEDATA_APEX_LOG_BODY);
    	if (Utils.isNotEmpty(apexLogBody)) {
    		return apexLogBody;
    	}
    	
    	// Try to get the log body
    	apexLogBody = delegate.getApexLogBody(forceProject, logId);
    	if (Utils.isNotEmpty(apexLogBody)) {
    		// Save it for future uses
    		selectedTreeItem.setData(TREEDATA_APEX_LOG_BODY, apexLogBody);
    		return apexLogBody;
    	}
    	
    	// There is no ApexLog body, so try to retrieve a saved ApexLog
    	ApexLog apexLog = (ApexLog) selectedTreeItem.getData(TREEDATA_APEX_LOG);
    	if (Utils.isNotEmpty(apexLog)) {
    		return apexLog.toString();
    	}
    	
    	// Try to get the ApexLog object
    	apexLog = delegate.getApexLog(forceProject, logId);
    	selectedTreeItem.setData(TREEDATA_APEX_LOG, apexLog);
    	return (Utils.isNotEmpty(apexLog) ? apexLog.toString() : null);
    }
    
    /**
     * Update the Stack Trace tab with the given error message & stack trace.
     * @param message
     * @param stackTrace
     */
    public void showStackTrace(String message, String stackTrace) {
    	if (Utils.isNotEmpty(runTestComposite)) {
    		StringBuilder data = new StringBuilder();
    		
    		if (Utils.isNotEmpty(message)) {
    			String newLine = System.getProperty("line.separator");
    			data.append(message + newLine + newLine);
    		}
    		
    		if (Utils.isNotEmpty(stackTrace)) {
    			data.append(stackTrace);
    		}
    		
    		runTestComposite.setStackTraceArea(data.toString());
    	}
    }
    
    /**
     * Update the System Debug Log tab with the given log.
     * @param log
     */
    public void showSystemLog(String log) {
    	if (Utils.isNotEmpty(runTestComposite) && Utils.isNotEmpty(log)) {
    		runTestComposite.setSystemLogsTextArea(log);
    	}
    }
    
    /**
     * Update the User Debug Log tab with a filtered log.
     * @param selectedTreeItem
     * @param log
     */
    public void showUserLog(TreeItem selectedTreeItem, String log) {
    	if (Utils.isEmpty(selectedTreeItem) || Utils.isEmpty(runTestComposite)) {
    		return;
    	}
    	
    	// Do we already have a filtered log?
    	String userDebugLog = (String) selectedTreeItem.getData(TREEDATA_APEX_LOG_USER_DEBUG);
    	if (Utils.isNotEmpty(userDebugLog)) {
    		runTestComposite.setUserLogsTextArea(userDebugLog);
    		return;
    	}
    	
    	// Filter the given log with only DEBUG statements
    	if (Utils.isNotEmpty(log) && log.contains("DEBUG")) {
    		userDebugLog = "";
            String[] newDateWithSperators = log.split("\\|");
            for (int index = 0; index < newDateWithSperators.length; index++) {
                String newDateWithSperator = newDateWithSperators[index];
                if (newDateWithSperator.contains("USER_DEBUG")) {
                    String debugData = newDateWithSperators[index + 3];
                    debugData = debugData.substring(0, debugData.lastIndexOf('\n'));
                    userDebugLog += "\n" + debugData + "\n";
                }

            }
            // Save it for future uses
            selectedTreeItem.setData(TREEDATA_APEX_LOG_USER_DEBUG, userDebugLog);
            // Update the tab
            runTestComposite.setUserLogsTextArea(userDebugLog);
    	}
    }
    
    public void setProject(IProject project, RunTestsDelegate delegate) {
        projectChange(project);
        this.delegate = delegate;
    }
    
    public void projectChange(IProject project) {
    	this.project = project;
    	this.runTestComposite.setProject(project);
    	this.runTestComposite.enableComposite();
    }

    public IProject getProject() {
        return project;
    }

    public RunTestViewComposite getRunTestComposite() {
        return runTestComposite;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        getSite().getPage().removeSelectionListener(fPostSelectionListener);
    }
    
    @Override
    public void createPartControl(Composite parent) {
        runTestComposite = new RunTestViewComposite(parent, SWT.NONE, this);
        setPartName(VIEW_NAME);
        setTitleImage(getImage());

        UIUtils.setHelpContext(runTestComposite, this.getClass().getSimpleName());
    }

    @Override
    public void setFocus() {
        if (Utils.isNotEmpty(runTestComposite)) {
            runTestComposite.setFocus();
        }
    }
    
    private void setSelectionListener() {
        fPostSelectionListener = new ISelectionListener() {
            @Override
            public void selectionChanged(IWorkbenchPart part, ISelection selection) {
                project = getProjectService().getProject(selection);
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection) selection;
                    Object selElement = ss.getFirstElement();
                    if (selElement instanceof IResource) {
                        projectChange(((IResource) selElement).getProject());
                    }
                }
            }
        };
    }
}
