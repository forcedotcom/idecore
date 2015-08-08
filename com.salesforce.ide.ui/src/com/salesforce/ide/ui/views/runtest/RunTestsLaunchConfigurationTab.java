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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.ide.apex.internal.core.ApexTestsUtils;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.QualifiedNames;
import com.salesforce.ide.core.internal.utils.ResourceProperties;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.remote.tooling.RunTests;
import com.salesforce.ide.core.remote.tooling.RunTests.Test;

/**
 * This is the main tab of Apex Test launch configuration which includes UI
 * and logic.
 * 
 * @author jwidjaja
 *
 */
public class RunTestsLaunchConfigurationTab extends AbstractLaunchConfigurationTab {
		
	protected Map<IProject, RunTests> allTests;
	protected RunTests currentSelectedTests;
	
	// Widgets for labels, input fields, buttons
	private Label projectLabel;
	protected Text projectText;
	protected Button projectButton;
    private Label classLabel;
    protected Text classText;
    protected Button classButton;
    private Label testMethodLabel;
    protected Text testMethodText;
    protected Button testMethodButton;
    protected Button testAsyncButton;
    protected Button testSyncButton;
    
    // Default colors 
    protected Color defaultGray;
    protected Color normalBlack;
    
    private final ApexTestsUtils sourceLookup = ApexTestsUtils.INSTANCE;

    public RunTestsLaunchConfigurationTab() {
    	super();
    	allTests = new HashMap<IProject, RunTests>();
    }
    
    @Override
    public String getName() {
        return Messages.RunTestsTab_TabTitle;
    }

    protected String getProjectName() {
        return (Utils.isEmpty(projectText) ? "" : projectText.getText());
    }
    
    protected String getTestClassName() {
    	return (Utils.isEmpty(classText) ? "": classText.getText());
    }
    
    protected String getTestMethodName() {
    	return (Utils.isEmpty(testMethodText) ? "" : testMethodText.getText());
    }
    
    protected boolean isTestModeAsync() {
    	return (!Utils.isEmpty(testAsyncButton) && testAsyncButton.isEnabled() && testAsyncButton.getSelection()) ? true : false;
    }
    
    protected Composite createComposite(Composite parent, int style) {
    	return new Composite(parent, style);
    }
    
    protected Group createGroup(Composite parent, int style) {
    	return new Group(parent, style);
    }

    @Override
    public void createControl(Composite parent) {
    	if (Utils.isEmpty(parent)) return;
    	
        Composite comp = createComposite(parent, SWT.NONE);
        setControl(comp);

        GridLayout grid = new GridLayout();
        comp.setLayout(grid);

        createSingleTestEditor(comp);
        createTestModeEditor(comp);
    }

    /**
     * Create 'run single test' group which contains project, test class, and test method.
     * Through this, user may do one of the following:
     *   [*] Run all Apex test methods in all Apex test classes in a Force.com project
     *   [*] Run all Apex test methods in one Apex test class in a Force.com project
     *   [*] Run one Apex test method in one Apex test class in a Force.com project
     *   
     * @param parent
     *   The Composite widget to hold all the labels, input fields, buttons, etc.
     */
    protected void createSingleTestEditor(Composite parent) {
    	Group runSingleTestGroup = createGroup(parent, SWT.NONE);
    	runSingleTestGroup.setText(Messages.RunTestsTab_TabGroupTitle);
    	runSingleTestGroup.setLayout(new GridLayout(3, false));
    	runSingleTestGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        createDefaultLayout(runSingleTestGroup);
    }
    
    protected void createTestModeEditor(Composite parent) {
    	Group testModeGroup = createGroup(parent, SWT.NONE);
    	testModeGroup.setText(Messages.RunTestsTab_ModeGroupTitle);
    	testModeGroup.setLayout(new GridLayout(2, false));
    	testModeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	
    	// Run async or sync radio buttons (default is async)
    	Composite composite = createComposite(testModeGroup, SWT.NULL);
        composite.setLayout(new RowLayout());
        testAsyncButton = makeDefaultRadioButton(composite, Messages.RunTestsTab_RunAsync, true, true);
        testSyncButton = makeDefaultRadioButton(composite, Messages.RunTestsTab_RunSync, true, false);
        
        testAsyncButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	validatePage();
                updateLaunchConfigurationDialog();
            }
        });
        
        testSyncButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	validatePage();
                updateLaunchConfigurationDialog();
            }
        });
    }
    
    /**
     * Create labels, input fields, and buttons.
     * 
     * @param group
     *   The Group widget to hold all the labels, input fields, buttons, etc.
     */
    private void createDefaultLayout(Group group) {
    	disposeExistingWidgets();
    	
    	defaultGray = group.getDisplay().getSystemColor(SWT.COLOR_GRAY);
    	normalBlack = group.getDisplay().getSystemColor(SWT.COLOR_BLACK);
    	
        // Project group (label, text, button)
        projectLabel = makeDefaultLabel(group, Messages.GenericTab_ProjectGroupTitle);
        // Get user's selected project as a backup. If none is selected, user
        // has to select one
        String projectName = getCurrentProject();
        projectText = makeDefaultText(group, projectName, normalBlack);
        projectButton = makeDefaultButton(group, Messages.GenericTab_SearchButtonText, true);
        projectButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleProjectButtonSelected();
                validatePage();
                updateLaunchConfigurationDialog();
            }
        });
        
        // Test class group (label, text, button)
        classLabel = makeDefaultLabel(group, Messages.RunTestsTab_TestClassGroupTitle);
        classText = makeDefaultText(group, Messages.GenericTab_AllClasses, defaultGray);
        classButton = makeDefaultButton(group, Messages.GenericTab_SearchButtonText, 
        		shouldEnableBasedOnText(projectText.getText()));
        classButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            	handleClassButtonSelected();
                validatePage();
                updateLaunchConfigurationDialog();
            }
        });
        
        // Test method group (label, text, button)
        testMethodLabel = makeDefaultLabel(group, Messages.RunTestsTab_TestMethodGroupTitle);
        testMethodText = makeDefaultText(group, Messages.GenericTab_AllMethods, defaultGray);
        testMethodButton = makeDefaultButton(group, Messages.GenericTab_SearchButtonText, 
        		shouldEnableBasedOnText(classText.getText()));
        testMethodButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            	handleTestMethodButtonSelected();
                validatePage();
                updateLaunchConfigurationDialog();
            }
        });
    }
    
    /**
     * Cleanup widgets if necessary.
     */
    private void disposeExistingWidgets() {
    	if (projectLabel != null) projectLabel.dispose();
    	if (projectText != null) projectText.dispose();
    	if (projectButton != null) projectButton.dispose();
    	if (classLabel != null) classLabel.dispose();
    	if (classText != null) classText.dispose();
    	if (classButton != null) classButton.dispose();
    	if (testMethodLabel != null) testMethodLabel.dispose();
    	if (testMethodText != null) testMethodText.dispose();
    	if (testMethodButton != null) testMethodButton.dispose();
    	if (defaultGray != null) defaultGray.dispose();
    	if (normalBlack != null) normalBlack.dispose();
    	if (testAsyncButton != null) testAsyncButton.dispose();
    	if (testSyncButton != null) testSyncButton.dispose();
    }
    
    /**
     * Create a label with specified text.
     * @param parent
     * @param defaultText
     * @return Label widget
     */
    private Label makeDefaultLabel(Group parent, String defaultText) {
    	Label label = new Label(parent, SWT.SINGLE);
    	label.setText(defaultText);
        return label;
    }
    
    /**
     * Create a text field with specified text and foreground color
     * @param parent
     * @param defaultText
     * @param defaultColor
     * @return Text widget
     */
    private Text makeDefaultText(Group parent, String defaultText, Color defaultColor) {
    	Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
    	return setTextProperties(text, defaultText, defaultColor);
    }
    
    /**
     * Create a button with specified text and enabled value
     * @param parent
     * @param defaultText
     * @param enabled
     * @return Button widget
     */
    private Button makeDefaultButton(Group parent, String defaultText, boolean enabled) {
    	Button button = new Button(parent, SWT.PUSH);
    	button.setText(defaultText);
    	button.setLayoutData(new GridData());
    	button.setEnabled(enabled);
    	return button;
    }
    
    /**
     * Create a radio button with specified text and enabled value
     * @param parent
     * @param defaultText
     * @param enabled
     * @return
     */
    private Button makeDefaultRadioButton(Composite parent, String defaultText, boolean enabled, boolean selected) {
    	Button button = new Button(parent, SWT.RADIO);
    	button.setText(defaultText);
    	button.setEnabled(enabled);
    	button.setSelection(selected);
        return button;
    }
    
    /**
     * Set defaults for a Text widget. The text field is not editable.
     * @param text
     * @param defaultText
     * @param defaultColor
     * @return Text widget
     */
    protected Text setTextProperties(Text text, String defaultText, Color defaultColor) {
    	if (Utils.isEmpty(text)) return null;
    	
    	text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	// Do not let the user edit this value except through the browse button to minimize errors
    	text.setEditable(false);
    	text.setText(defaultText);
    	Color textColor = shouldEnableBasedOnText(defaultText) ? defaultColor : defaultGray;
    	text.setForeground(textColor);
    	return text;
    }
    
    /**
     * Enable or disable a button based the text.
     * @param text
     * @return True if text is not null/empty and does not equal
     *   to default strings (all classes) (all methods). False otherwise.
     */
    protected boolean shouldEnableBasedOnText(String text) {
    	return StringUtils.isNotBlank(text) && !text.equals(Messages.GenericTab_AllClasses)
    			&& !text.equals(Messages.GenericTab_AllMethods);
    }
    
    /**
     * Set the search class button to its defaults.
     */
    protected void resetSelectedTestClass() {
    	setTextProperties(classText, Messages.GenericTab_AllClasses, defaultGray);
    	classButton.setEnabled(shouldEnableBasedOnText(projectText.getText()));
    }
    
    /**
     * Set the search method button to its defaults.
     */
    protected void resetSelectedTestMethod() {
    	setTextProperties(testMethodText, Messages.GenericTab_AllMethods, defaultGray);
    	testMethodButton.setEnabled(shouldEnableBasedOnText(classText.getText()));
    }
    
    /**
     * Find projects in the workspace and update other widgets when
     * project is chosen.
     */
    protected void handleProjectButtonSelected() {
    	IProject selectedProject = chooseProject();

        if (Utils.isEmpty(selectedProject)) return;

        String projectName = selectedProject.getName();
        if (StringUtils.isBlank(projectName) || Utils.isEmpty(projectText)) return;
        
        // Reset test class and test method if user changed projects
        if (!projectName.equals(projectText.getText())) {
        	resetSelectedTestClass();
        	resetSelectedTestMethod();

            // Retrieve all test classes and test methods in the new selected project
        	if (!allTests.containsKey(selectedProject)) {
        		allTests.put(selectedProject, buildTestsForProject(selectedProject));
        	}
        }
        // Allow class selection after project is known
        classButton.setEnabled(true);
        // Display newest selected project name
        projectText = setTextProperties(projectText, projectName, normalBlack);
    }
    
    /**
     * Display a list of projects in the workspace and return the selected one.
     * @return IProject
     */
    protected IProject chooseProject() {
    	// Get all projects in workspace
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element == null)
                    return "";
                if (element instanceof IProject)
                    return ((IProject) element).getName();
                return element.toString();
            }
        });
        
        // Display the projects in dialog. If there is none, user cannot launch config.
        dialog.setTitle(Messages.GenericTab_ProjectDialogTitle);
        dialog.setMessage(Messages.RunTestsTab_ProjectDialogInstruction);
        dialog.setElements(ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProjects()
                .toArray());

        if (dialog.open() == Window.OK) {
            return (IProject) dialog.getFirstResult();
        }

        return null;
    }
    
    /**
     * When creating the tab controls, try to use a selected project to
     * populate the project text field. Otherwise, it'll be blank.
     * @return Name of selected project. Empty string otherwise.
     */
    private String getCurrentProject() {
    	ISelectionService selectionService = 
    			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
    	
    	ISelection selection = selectionService.getSelection();
    	IProject currentProject = ContainerDelegate.getInstance().getServiceLocator().
    			getProjectService().getProject(selection);
    	
    	return (currentProject != null ? currentProject.getName() : "");
    }
    
    /**
     * Retrieve test classes and methods for a specific project. This
     * should only be called when opening the config for the first time
     * or when user changes the project.
     * @param project
     * @return RunTests POJO
     */
    protected RunTests buildTestsForProject(IProject project) {
    	RunTests rt = new RunTests();
    	List<Test> testClasses = new ArrayList<Test>();
    	
    	Map<IResource, List<String>> allTests = sourceLookup.findTestClassesInProject(project);
    	for (IResource resource : allTests.keySet()) {
    		List<String> testMethods = allTests.get(resource);
    		if (testMethods == null || testMethods.isEmpty()) {
    			continue;
    		}
    		
    		// If there is more than one test method in the test class, add the 'all methods' option
    		if (testMethods.size() > 1) {
    			testMethods.add(0, Messages.GenericTab_AllMethods);
    		}
    		
    		String resourceId = ResourceProperties.getProperty(resource, QualifiedNames.QN_ID);
    		
    		Test testClass = rt.new Test();
    		testClass.setClassId(resourceId);
    		testClass.setClassName(resource.getName());
    		testClass.setTestMethods(testMethods);
    		
    		testClasses.add(testClass);
    	}
    	
    	// If there is more than one test class in the project, add the 'all classes' option
    	if (testClasses != null && testClasses.size() > 1) {
    		Test allClasses = rt.new Test();
    		allClasses.setClassId(Messages.GenericTab_AllClasses);
    		allClasses.setClassName(Messages.GenericTab_AllClasses);
    		List<String> allMethods = new ArrayList<String>();
    		allMethods.add(Messages.GenericTab_AllMethods);
    		allClasses.setTestMethods(allMethods);
    		
    		testClasses.add(0, allClasses);
    	}
    	
    	rt.setTests(testClasses);
    	return rt;
    }
    
    /**
     * Create the JSON of tests to run.
     * @return RunTests
     */
    protected RunTests buildTestsForConfig(IProject selectedProject) {
    	/*
    	 * Clone the original RunTests because the following logic
    	 * will filter out unwanted test classes/methods. We need to maintain the
    	 * original so we don't to re-build when user changes test class/method.
    	 */
    	RunTests rt = allTests.containsKey(selectedProject) ? allTests.get(selectedProject).clone() : null;
    	if (Utils.isEmpty(rt)) return null;
    	
    	boolean oneTestClass = (classText != null && classText.getText() != null && !classText.getText().equals(Messages.GenericTab_AllClasses));
    	boolean oneTestMethod = (testMethodText != null && testMethodText.getText() != null && !testMethodText.getText().equals(Messages.GenericTab_AllMethods));

		// Iterate through the test classes
		for (Iterator<Test> tcItr = rt.getTests().iterator(); tcItr.hasNext();) {
			Test curTest = tcItr.next();
			/*
			 * Remove this Test object if:
			 * - User wants all test classes and this test class says 'all'
			 * - User wants one test class and this is not the one user wants
			 */
			if ((!oneTestClass && curTest.getClassName().equals(Messages.GenericTab_AllClasses)) || 
					(oneTestClass && !curTest.getClassName().equals(classText.getText()))) {
				tcItr.remove();
				continue;
			}
			// Iterate through the test methods
			for (Iterator<String> tmItr = curTest.getTestMethods().iterator(); tmItr.hasNext();) {
				String curMethod = tmItr.next();
				/*
				 * Remove this test method if:
				 * - User wants all test methods and this test method says 'all'
				 * - User wants one test method and this is not the one user wants
				 */
				if ((!oneTestMethod && curMethod.equals(Messages.GenericTab_AllMethods)) || 
						(oneTestMethod && !curMethod.equals(testMethodText.getText()))) {
					tmItr.remove();
				}
			}
		}
		
    	return rt;
    }
    
    /**
     * Convert RunTests to JSON string
     * @param RunTests
     * @return JSON string
     */
    protected String convertTestsToJson(RunTests rt) {
    	String result = "";
    	ObjectMapper mapper = new ObjectMapper();
    	try {
			result = mapper.writeValueAsString(rt);
		} catch (Exception e) {}
    	
    	return result;
    }
    
    /**
     * Count the number of test methods.
     * @param rt
     * @return Total test methods
     */
    protected int countTotalTests(RunTests rt) {
    	int total = 0;
    	
    	if (rt != null) {
    		for (Test test : rt.getTests()) {
    			List<String> testMethods = test.getTestMethods();
    			if (testMethods != null && !testMethods.isEmpty()) {
    				total += testMethods.size();
    			}
    		}
    	}
    	
    	return total;
    }
    
    /**
     * Find test classes in the project and update other widgets
     * when test class is chosen.
     */
    protected void handleClassButtonSelected() {
    	String selectedTestClass = chooseTestClass();
    	
    	if (StringUtils.isBlank(selectedTestClass) || Utils.isEmpty(classText)) {
    		return;
    	}
    	
    	// Reset test method text if user changed test class
    	if (!selectedTestClass.equals(classText.getText())) {
    		resetSelectedTestMethod();
    	}
    	// Display newest selected class name
    	classText = setTextProperties(classText, selectedTestClass, normalBlack);
    	// Allow test method selection after test class is known, unless user selected 'all classes'
    	testMethodButton.setEnabled(shouldEnableBasedOnText(classText.getText()));
    }
    
    /**
     * Display a list of test classes in the project and return
     * the selected one.
     * @return Name of test class
     */
    protected String chooseTestClass() {
    	// Display the test classes in dialog
    	ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element == null)
                    return "";
                if (element instanceof Test)
                    return ((Test) element).getClassName();
                return element.toString();
            }
        });
    	
    	dialog.setTitle(Messages.GenericTab_ClassDialogTitle);
        dialog.setMessage(Messages.RunTestsTab_ClassDialogInstruction);
        IProject selectedProject = getProjectFromName();
        RunTests rt = allTests.get(selectedProject);
        // We already got the test classes earlier so just display them
        if (rt != null && rt.getTests() != null && !rt.getTests().isEmpty()) {
        	dialog.setElements(rt.getTests().toArray());
        }
        
        if (dialog.open() == Window.OK) {
            return ((Test) dialog.getFirstResult()).getClassName();
        }
    	
    	return null;
    }
    
    /**
     * Find test methods in the test class and update the
     * appropriate widgets when test method is chosen.
     */
    protected void handleTestMethodButtonSelected() {
    	String selectedTestMethod = chooseTestMethod();
    	
    	if (StringUtils.isEmpty(selectedTestMethod) || Utils.isEmpty(testMethodText)) {
    		return;
    	}
    	
    	// Display newest selected method name
    	testMethodText = setTextProperties(testMethodText, selectedTestMethod, normalBlack);
    }
    
    /**
     * Display a list of test methods in the test class and return
     * the selected one.
     * @return Name of test method
     */
    protected String chooseTestMethod() {
    	// We already got test methods earlier so just display the ones
    	// for previously specified test class
    	List<String> testMethodNames = new ArrayList<String>();
    	IProject selectedProject = getProjectFromName();
        RunTests rt = allTests.get(selectedProject);
    	for (Test test : rt.getTests()) {
    		if (test.getClassName().equals(classText.getText())) {
    			testMethodNames = test.getTestMethods();
    		}
    	}
    	
    	// Display the test methods in dialog
    	ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element == null)
                    return "";
                return element.toString();
            }
        });
    	
    	dialog.setTitle(Messages.GenericTab_MethodDialogTitle);
    	dialog.setMessage(Messages.RunTestsTab_MethodDialogInstruction);
    	if (testMethodNames != null && !testMethodNames.isEmpty()) {
    		dialog.setElements(testMethodNames.toArray());
    	}
    	
    	if (dialog.open() == Window.OK) {
    		return dialog.getFirstResult().toString();
    	}
    	
    	return null;
    }
    
    /**
     * If true, select asynchronous button and de-select synchronous button.
     * If false, de-select asynchronous button and select synchronous button.
     * @param isAsync
     */
    protected void selectAsync(boolean isAsync) {
    	if (Utils.isEmpty(testAsyncButton) || Utils.isEmpty(testSyncButton)) {
    		return;
    	}
    	
    	testAsyncButton.setEnabled(true);
    	testAsyncButton.setSelection(isAsync);
    	testSyncButton.setEnabled(true);
    	testSyncButton.setSelection(!isAsync);
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        validatePage();
        return getErrorMessage() == null;
    }
    
    /**
     * Reset messages and validate project selection.
     * @return True if all is okay. False otherwise.
     */
    protected boolean validatePage() {
        // Reset the messages first to a clean slate
        setErrorMessage(null);
        setMessage(null);

        return validateProjectSelection() && validateTestModeSelection();
    }
    
    /**
     * Validate project selection.
     * @return True if all is okay. False otherwise.
     */
    protected boolean validateProjectSelection() {
        if (getProjectName() == null || getProjectName().length() == 0) {
            setErrorMessage(Messages.GenericTab_EmptyProjectErrorMessage);
            return false;
        }

        IProject project = getProjectFromName();
        if (project == null) {
            setErrorMessage(Messages.GenericTab_NonExistingProjectErrorMessage);
            return false;
        }

        try {
            if (!project.hasNature(DefaultNature.NATURE_ID)) {
                setErrorMessage(Messages.GenericTab_InvalidForceProjectErrorMessage);
                return false;
            }
        } catch (CoreException e) {}

        return true;
    }
    
    /**
     * Validate test mode selection (async or sync)
     * @return True if all is okay. False otherwise.
     */
    protected boolean validateTestModeSelection() {
    	/*
    	 * Some rules regarding Apex test modes:
    	 * 
    	 * User cannot run tests asynchronously if there is an active
    	 * Apex debugging session for the same project. We should not enforce
    	 * that here because these launch configs are exportable and can be
    	 * shared between people with and without the ability to debug Apex.
    	 * The enforcement will take place after the config is launched.
    	 * We inform the user of the rule and they get to decide if they want
    	 * to continue with the asynchronous test run which will not be debuggable
    	 * or abort to change the test mode.
    	 * 
    	 * User cannot run more than one test class synchronously. We should
    	 * explicitly inform the user of this drawback instead of implicitly
    	 * switching to an asynchronous test run or selecting a specific
    	 * test class for them. This rule applies to everyone so we can enforce
    	 * while the config is being saved. If the rule is broken, the config
    	 * cannot be launched.
    	 */
    	
    	if (!isTestModeAsync() && (currentSelectedTests != null && currentSelectedTests.getTests() != null && currentSelectedTests.getTests().size() > 1)) {
    		setErrorMessage(Messages.RunTestsTab_TooManyTestClassesForSyncErrorMessage);
            return false;
    	}
    	
    	return true;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * Get the project from the name in project text field.
     * @return IProject
     */
    protected IProject getProjectFromName() {
    	String projectName = getProjectName();
    	if (StringUtils.isNotBlank(projectName)) {
    		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    	}
    	
        return null;
    }
    
    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    	// Save selected project, test class name, and test method name
        configuration.setAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, getProjectName());
        configuration.setAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_CLASS, getTestClassName());
        configuration.setAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_METHOD, getTestMethodName());
        
        // Build and save JSON string from test run config
        currentSelectedTests = buildTestsForConfig(getProjectFromName());
        String allTestsInJson = convertTestsToJson(currentSelectedTests);
        configuration.setAttribute(RunTestsConstants.ATTR_FORCECOM_TESTS_ARRAY, allTestsInJson);
        
        int totalTests = countTotalTests(currentSelectedTests);
        configuration.setAttribute(RunTestsConstants.ATTR_FORCECOM_TESTS_TOTAL, totalTests);
        
        // Async = true, sync = false
        configuration.setAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, isTestModeAsync());
    }
    
    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
    	try {
    		// Populate project, test class name, and test method name from saved config
            String projectName = configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, "");
            setTextProperties(projectText, projectName, normalBlack);
            projectButton.setEnabled(true);
            // Build the POJO for that project
            IProject selectedProject = getProjectFromName();
            if (Utils.isNotEmpty(selectedProject) && !allTests.containsKey(selectedProject)) {
            	RunTests selectedTests = buildTestsForProject(selectedProject);
            	allTests.put(selectedProject, selectedTests);
            	currentSelectedTests = selectedTests;
            }
            
            String testClassName = configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_CLASS, "");
            setTextProperties(classText, testClassName, normalBlack);
            classButton.setEnabled(shouldEnableBasedOnText(projectText.getText()));
            
            String testMethodName = configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_METHOD, "");
            setTextProperties(testMethodText, testMethodName, normalBlack);
            testMethodButton.setEnabled(shouldEnableBasedOnText(classText.getText()));
            
            boolean isAsync = configuration.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, true);
            selectAsync(isAsync);
        } catch (CoreException e) {}
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {}
}
