/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.google.common.annotations.VisibleForTesting;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.DefaultNature;
import com.sforce.soap.tooling.ApexLogLevel;

/**
 * Apex Test launch configuration tab to select project & log levels
 * 
 * @author jwidjaja
 *
 */
public class ProjectConfigurationTab extends RunTestsTab {

	private TestConfigurationTab testTab;
	
	@VisibleForTesting
	public Text projectText;
	@VisibleForTesting
	public Button projectButton;
	@VisibleForTesting
	public Button logStatus;
	@VisibleForTesting
	public LinkedHashMap<String, Combo> logSettings;
	
	@VisibleForTesting
	public static final String[] logCategories = new String[] { 
		Messages.Tab_LogCategoryDatabase,
		Messages.Tab_LogCategoryWorkflow,
		Messages.Tab_LogCategoryValidation,
		Messages.Tab_LogCategoryCallout,
		Messages.Tab_LogCategoryApexCode,
		Messages.Tab_LogCategoryApexProfiling,
		Messages.Tab_LogCategoryVisualforce,
		Messages.Tab_LogCategorySystem };
	
	private static final LinkedHashMap<String, String> defaultLogLevels;
	static {
		defaultLogLevels = new LinkedHashMap<String, String>();
		for (String logCategory : logCategories) {
			if (logCategory.equals(Messages.Tab_LogCategoryApexCode) ||
					logCategory.equals(Messages.Tab_LogCategorySystem)) {
				defaultLogLevels.put(logCategory, ApexLogLevel.DEBUG.name());
			} else {
				defaultLogLevels.put(logCategory, ApexLogLevel.INFO.name());
			}
		}
	}
	
	/**
	 * Need to be able to get some stuff from TestConfigurationTab,
	 * so this is probably better than doing
	 * getLaunchConfigurationDialog().getTabs() and instanceof.
	 */
	@VisibleForTesting
	public void saveSiblingTab(RunTestsTab testTab) {
		this.testTab = (TestConfigurationTab) testTab;
	}
	
	@VisibleForTesting
	public TestConfigurationTab getSiblingTab() {
		return this.testTab;
	}
	
	@VisibleForTesting
	public String getProjectName() {
        return (Utils.isEmpty(projectText) ? "" : projectText.getText());
    }
	
	/**
     * Get the project from the name in project text field.
     */
	@VisibleForTesting
    public IProject getProjectFromName() {
    	String projectName = getProjectName();
    	if (StringUtils.isNotBlank(projectName)) {
    		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    	}
    	
        return null;
    }
	
	@VisibleForTesting
	@Override
	public void createControl(Composite parent) {
		if (Utils.isEmpty(parent)) return;
		
		Composite comp = createComposite(parent, SWT.NONE);
		setControl(comp);
		colorGray = comp.getDisplay().getSystemColor(SWT.COLOR_GRAY);
		colorBlack = comp.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		
		GridLayout grid = new GridLayout();
        comp.setLayout(grid);
        
        // This tab allows project & log selections
        createProjectSelector(comp);
        createLogEditor(comp);
	}
	
	@VisibleForTesting
	public void createProjectSelector(Composite parent) {
		Group group = createGroup(parent, SWT.NONE);
		group.setText(Messages.Tab_ProjectGroupTitle);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Get user's selected project as a backup. If none is selected, user
        // has to select one.
		projectText = makeDefaultText(group, "", colorBlack);
		projectButton = makeDefaultButton(group, Messages.Tab_SearchButtonText, true);
		projectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleProjectButtonSelected();
                validatePage();
                updateLaunchConfigurationDialog();
            }
        });
	}
	
	/**
     * Create log settings editor with checkbox,
     * log categories, and log levels.
     */
	@VisibleForTesting
	public void createLogEditor(Composite parent) {
		Group logGroup = createGroup(parent, SWT.NONE);
    	logGroup.setText(Messages.Tab_LogGroupTitle);
    	logGroup.setLayout(new GridLayout(4, true));
    	logGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	
    	logStatus = makeDefaultCheckbox(logGroup, Messages.Tab_LogEnableLogging, true, true);
    	GridData gridData = new GridData();
    	gridData.horizontalAlignment = GridData.FILL;
    	gridData.horizontalSpan = 4;
    	logStatus.setLayoutData(gridData);
    	logStatus.addSelectionListener(new SelectionAdapter() {
    		@Override
            public void widgetSelected(SelectionEvent e) {
    			Button btn = (Button) e.getSource();
    			shouldEnableLevels(btn.getSelection());
    			validatePage();
                updateLaunchConfigurationDialog();
            }
    	});
    	
    	String[] logLevels = new String[] { ApexLogLevel.NONE.name(),
    			ApexLogLevel.ERROR.name(), ApexLogLevel.WARN.name(),
    			ApexLogLevel.INFO.name(), ApexLogLevel.DEBUG.name(),
    			ApexLogLevel.FINE.name(), ApexLogLevel.FINER.name(),
    			ApexLogLevel.FINEST.name()};
    	
    	logSettings = new LinkedHashMap<String, Combo>();
    	
    	for (String logCategory : logCategories) {
    		makeDefaultLabel(logGroup, logCategory);
    		Combo logCategoryCombo = new Combo(logGroup, SWT.READ_ONLY);
    		logCategoryCombo.setVisible(true);
    		logCategoryCombo.setEnabled(true);
    		logCategoryCombo.setItems(logLevels);
    		logCategoryCombo.addSelectionListener(new SelectionAdapter() {
    			@Override
                public void widgetSelected(SelectionEvent e) {
        			validatePage();
                    updateLaunchConfigurationDialog();
                }
    		});
    		
    		logSettings.put(logCategory, logCategoryCombo);
    	}
    	
    	setLogLevels(defaultLogLevels);
	}
	
	/**
     * Enable or disable the log categories
     */
	@VisibleForTesting
	public void shouldEnableLevels(boolean shouldEnable) {
    	if (Utils.isEmpty(logStatus) || Utils.isEmpty(logSettings)) {
    		return;
    	}
    	
    	logStatus.setSelection(shouldEnable);
    	
    	for (Combo logCategoryCombo : logSettings.values()) {
    		logCategoryCombo.setEnabled(shouldEnable);
    	}
    }
	
	/**
     * Set the log level combo boxes
     */
	@VisibleForTesting
	public void setLogLevels(Map<String, String> logLevels) {
    	if (Utils.isEmpty(logLevels)) {
    		return;
    	}
    	
    	for (String logCategory : logLevels.keySet()) {
    		String desiredLogLevelName = logLevels.get(logCategory);
    		Combo logCombo = logSettings.get(logCategory);
    		if (Utils.isNotEmpty(logCombo)) {
    			logCombo.setText(desiredLogLevelName);
    		}
    	}
    }
	
    /**
     * Find projects in the workspace and update other widgets when
     * project is chosen.
     */
	@VisibleForTesting
	public void handleProjectButtonSelected() {
		IProject selectedProject = chooseProject();
		
		if (Utils.isEmpty(selectedProject) || Utils.isEmpty(projectText)) return;
		
		String projectName = selectedProject.getName();
        if (StringUtils.isBlank(projectName) || Utils.isEmpty(projectText)) return;
        
        // Reset test class, test method, and suites if user changed projects
        if (!projectName.equals(projectText.getText())) {
        	testTab.resetTestSelection();
        	testTab.fetchSuites(selectedProject);
        }
        
        // Display newest selected project name
        projectText = setTextProperties(projectText, projectName, colorBlack);
	}
	
	/**
     * Display a list of projects in the workspace and return the selected one.
     */
	@VisibleForTesting
	public IProject chooseProject() {
    	// Get all projects in workspace
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider() {
            @Override
            public String getText(Object element) {
            	if (Utils.isNotEmpty(element) && element instanceof IProject) {
            		return ((IProject) element).getName();
            	}
            	return "";
            }
        });
        
        // Display the projects in dialog. If there is none, user cannot launch config.
        dialog.setTitle(Messages.Tab_ProjectDialogTitle);
        dialog.setMessage(Messages.Tab_ProjectDialogInstruction);
        dialog.setHelpAvailable(false);
        dialog.setElements(ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProjects()
                .toArray());

        if (dialog.open() == Window.OK) {
            return (IProject) dialog.getFirstResult();
        }

        return null;
    }
	
	/**
     * Reset messages and validate project selection.
     * @return True if all is okay. False otherwise.
     */
	@VisibleForTesting
	public boolean validatePage() {
		// Reset the messages first to a clean slate
        setErrorMessage(null);
        setMessage(null);
        
        return validateProjectSelection() && testTab.validateSuiteSelection();
	}
	
	/**
     * Validate project selection.
     * @return True if all is okay. False otherwise.
     */
	@VisibleForTesting
	public boolean validateProjectSelection() {
		if (getProjectName() == null || getProjectName().length() == 0) {
            setErrorMessage(Messages.Tab_EmptyProjectErrorMessage);
            return false;
        }

        IProject project = getProjectFromName();
        if (Utils.isEmpty(project)) {
            setErrorMessage(Messages.Tab_NonExistingProjectErrorMessage);
            return false;
        }

        try {
            if (!project.hasNature(DefaultNature.NATURE_ID)) {
                setErrorMessage(Messages.Tab_InvalidForceProjectErrorMessage);
                return false;
            }
        } catch (CoreException e) {}

        return true;
	}
	
	@VisibleForTesting
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		validatePage();
		return getErrorMessage() == null;
	}
	
	@VisibleForTesting
	public boolean isLoggingEnabled() {
    	return (Utils.isNotEmpty(logStatus) && logStatus.getSelection());
    }
	
	@VisibleForTesting
	public Map<String, String> getLogLevels() {
    	Map<String, String> logLevels = new LinkedHashMap<String, String>();
    	
    	if (Utils.isEmpty(logSettings)) {
    		return logLevels;
    	}
    	
    	for (String logCategory : logSettings.keySet()) {
    		Combo selectedCombo = logSettings.get(logCategory);
    		if (Utils.isEmpty(selectedCombo)) {
    			continue;
    		}
    		
    		ApexLogLevel logLevel = ApexLogLevel.valueOf(selectedCombo.getText());
    		if (Utils.isNotEmpty(logLevel)) {
    			String logLevelName = logLevel.name();
        		logLevels.put(logCategory, logLevelName);
    		}
    	}
    	
    	return logLevels;
    }
	
	@VisibleForTesting
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			// Populate project from saved config
			String projectName = configuration.getAttribute(RunTestsConstants.ATTR_PROJECT_NAME, "");
			setTextProperties(projectText, projectName, colorBlack);
	        
	        // Set log levels
            boolean shouldEnableLogging = configuration.getAttribute(RunTestsConstants.ATTR_ENABLE_LOGGING, false);
            shouldEnableLevels(shouldEnableLogging);
			Map<String, String> logLevels = configuration.getAttribute(RunTestsConstants.ATTR_LOG_LEVELS, Collections.emptyMap());
            setLogLevels(logLevels);
		} catch (CoreException e) {}
	}
	
	@VisibleForTesting
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RunTestsConstants.ATTR_PROJECT_NAME, getProjectName());
		configuration.setAttribute(RunTestsConstants.ATTR_ENABLE_LOGGING, isLoggingEnabled());
        configuration.setAttribute(RunTestsConstants.ATTR_LOG_LEVELS, getLogLevels());
	}
	
	@VisibleForTesting
	@Override
	public String getName() {
		return Messages.Tab_ProjectTabTitle;
	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {}
}
