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

package com.salesforce.ide.ui.views.runtest.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.google.common.collect.Maps;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.remote.tooling.RunTests.SuiteManager;
import com.salesforce.ide.core.remote.tooling.RunTests.TestsHolder;
import com.salesforce.ide.ui.views.runtest.Messages;
import com.salesforce.ide.ui.views.runtest.ProjectConfigurationTab;
import com.salesforce.ide.ui.views.runtest.RunTestsConstants;
import com.salesforce.ide.ui.views.runtest.TestConfigurationTab;
import com.sforce.soap.tooling.ApexLogLevel;
import com.salesforce.ide.test.common.IdeTestCase;
import com.salesforce.ide.test.common.IdeSetupTest;

@SuppressWarnings("unchecked")
@IdeSetupTest(needOrg = false, needProject = false)
public class RunTestsLaunchConfigurationTabTest_pdeui extends IdeTestCase {
		
	private ProjectConfigurationTab projectTab;
	private TestConfigurationTab testTab;
	
	@Before
    @Override
    public void setUp() throws Exception {
		projectTab = mock(ProjectConfigurationTab.class);
		testTab = mock(TestConfigurationTab.class);
		
		doCallRealMethod().when(projectTab).saveSiblingTab(any(TestConfigurationTab.class));
		projectTab.saveSiblingTab(testTab);
		
		doCallRealMethod().when(testTab).saveSiblingTab(any(ProjectConfigurationTab.class));
		testTab.saveSiblingTab(projectTab);
	}
	
	@Test
	public void testTestTabInitialize() {
		TestConfigurationTab realTestTab = new TestConfigurationTab();
		
		assertTrue(realTestTab.getTestHolder().isEmpty());
		assertTrue(realTestTab.getSuiteManagers().isEmpty());
	}
	
	@Test
	public void testGetProjectTabName() {
		doCallRealMethod().when(projectTab).getName();
		
		assertEquals(Messages.Tab_ProjectTabTitle, projectTab.getName());
	}
	
	@Test
	public void testGetTestTabName() {
		doCallRealMethod().when(testTab).getName();
		
		assertEquals(Messages.Tab_TestsTabTitle, testTab.getName());
	}

	@Test
	public void testGetNullProjectTextWidget() {
		doCallRealMethod().when(projectTab).getProjectName();
		
		assertEquals("", projectTab.getProjectName());
	}
	
	@Test
	public void testGetNullClassTextWidget() {
		doCallRealMethod().when(testTab).getTestClassName();
		
		assertEquals("", testTab.getTestClassName());
	}
	
	@Test
	public void testProjectTabCreateControl() {
		Composite parent = mock(Composite.class);
		doCallRealMethod().when(projectTab).createControl(parent);
		
		Composite comp = mock(Composite.class);
		Display display = mock(Display.class);
		Color gray = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		Color black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		when(display.getSystemColor(SWT.COLOR_GRAY)).thenReturn(gray);
		when(display.getSystemColor(SWT.COLOR_BLACK)).thenReturn(black);
		when(comp.getDisplay()).thenReturn(display);
		doNothing().when(comp).setLayout(any(GridLayout.class));
		when(projectTab.createComposite(parent, SWT.NONE)).thenReturn(comp);
		
		doNothing().when(projectTab).createProjectSelector(any(Composite.class));
		doNothing().when(projectTab).createLogEditor(any(Composite.class));
		
		projectTab.createControl(parent);
		
		verify(projectTab, times(1)).createProjectSelector(eq(comp));
		verify(projectTab, times(1)).createLogEditor(eq(comp));
	}
	
	@Test
	public void testCreateProjectSelector() {
		Composite parent = mock(Composite.class);
		doCallRealMethod().when(projectTab).createProjectSelector(parent);
		
		Group group = mock(Group.class);
		doNothing().when(group).setText(Messages.Tab_ProjectGroupTitle);
		doNothing().when(group).setLayout(any(GridLayout.class));
		doNothing().when(group).setLayoutData(any(GridData.class));
		when(projectTab.createGroup(parent, SWT.NONE)).thenReturn(group);
		
		Text projectText = mock(Text.class);
		when(projectTab.makeDefaultText(eq(group), eq(""), any(Color.class))).thenReturn(projectText);
		Button projectButton = mock(Button.class);
		doNothing().when(projectButton).addSelectionListener(any(SelectionAdapter.class));
		when(projectTab.makeDefaultButton(group, Messages.Tab_SearchButtonText, true)).thenReturn(projectButton);
		
		projectTab.createProjectSelector(parent);
		
		verify(projectTab, times(1)).makeDefaultText(group, "", projectTab.colorBlack);
		verify(projectTab, times(1)).makeDefaultButton(group, Messages.Tab_SearchButtonText, true);
		assertEquals(projectText, projectTab.projectText);
		assertEquals(projectButton, projectTab.projectButton);
	}
	
	@Test
	public void testTestTabCreateControl() {
		Composite parent = mock(Composite.class);
		doCallRealMethod().when(testTab).createControl(parent);
		
		Composite comp = mock(Composite.class);
		Display display = mock(Display.class);
		Color gray = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		Color black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		when(display.getSystemColor(SWT.COLOR_GRAY)).thenReturn(gray);
		when(display.getSystemColor(SWT.COLOR_BLACK)).thenReturn(black);
		when(comp.getDisplay()).thenReturn(display);
		doNothing().when(comp).setLayout(any(GridLayout.class));
		when(testTab.createComposite(parent, SWT.NONE)).thenReturn(comp);
		
		doNothing().when(testTab).createSingleTestSelector(any(Composite.class));
		doNothing().when(testTab).createSuiteSelector(any(Composite.class));
		
		testTab.createControl(parent);
		
		verify(testTab, times(1)).createSingleTestSelector(eq(comp));
		verify(testTab, times(1)).createSuiteSelector(eq(comp));
	}
	
	@Test
	public void testCreateSingleTestSelector() {
		Composite parent = mock(Composite.class);
		doCallRealMethod().when(testTab).createSingleTestSelector(parent);
		
		Group group = mock(Group.class);
		doNothing().when(group).setText(Messages.Tab_TestsGroupTitle);
		doNothing().when(group).setLayout(any(GridLayout.class));
		doNothing().when(group).setLayoutData(any(GridData.class));
		when(testTab.createGroup(parent, SWT.NONE)).thenReturn(group);
		
		when(testTab.makeDefaultLabel(group, Messages.Tab_TestClassGroupTitle)).thenReturn(mock(Label.class));
		Text classText = mock(Text.class);
		when(testTab.makeDefaultText(group, Messages.Tab_AllClasses, testTab.colorGray)).thenReturn(classText);
		Button defaultSearchButton = mock(Button.class);
		doNothing().when(defaultSearchButton).addSelectionListener(any(SelectionAdapter.class));
		when(testTab.makeDefaultButton(eq(group), eq(Messages.Tab_SearchButtonText), any(Boolean.class))).thenReturn(defaultSearchButton);
		
		when(testTab.makeDefaultLabel(group, Messages.Tab_TestMethodGroupTitle)).thenReturn(mock(Label.class));
		Text testMethodText = mock(Text.class);
		when(testTab.makeDefaultText(group, Messages.Tab_AllMethods, testTab.colorGray)).thenReturn(testMethodText);
		
		testTab.createSingleTestSelector(parent);
		
		assertEquals(classText, testTab.classText);
		assertEquals(defaultSearchButton, testTab.classButton);
		assertEquals(testMethodText, testTab.testMethodText);
		assertEquals(defaultSearchButton, testTab.testMethodButton);
	}
	
	@Test
	public void testCreateSuiteSelector() {
		Composite parent = mock(Composite.class);
		doCallRealMethod().when(testTab).createSuiteSelector(parent);
		
		Group group = mock(Group.class);
		doNothing().when(group).setText(Messages.Tab_SuiteGroupTitle);
		doNothing().when(group).setLayout(any(GridLayout.class));
		doNothing().when(group).setLayoutData(any(GridData.class));
		when(testTab.createGroup(parent, SWT.NONE)).thenReturn(group);
		
		Button useSuites = mock(Button.class);
		doNothing().when(useSuites).addSelectionListener(any(SelectionAdapter.class));
		when(testTab.makeDefaultCheckbox(group, Messages.Tab_UseSuites, true, false)).thenReturn(useSuites);
		
		Table suiteTable = mock(Table.class);
		doNothing().when(suiteTable).addSelectionListener(any(SelectionAdapter.class));
		when(testTab.makeDefaultMultiCheckTable(group, new String[] { Messages.Tab_SuiteColumnName })).thenReturn(suiteTable);
		
		testTab.createSuiteSelector(parent);
		
		assertEquals(useSuites, testTab.suiteStatus);
		assertEquals(suiteTable, testTab.suiteTable);
	}
	
	@Test
	public void testGetNullMethodTextWidget() {
		doCallRealMethod().when(testTab).getTestMethodName();
		
		assertEquals("", testTab.getTestMethodName());
	}
	
	@Test
	public void testGetLogLevelsNullLogSettings() {
		doCallRealMethod().when(projectTab).getLogLevels();
		projectTab.logSettings = null;
		
		Map<String, String> logLevels = projectTab.getLogLevels();
		
		assertNotNull(logLevels);
		assertTrue(logLevels.isEmpty());
	}
	
	@Test
	public void testGetLogLevelsEmptyLogSettings() {
		doCallRealMethod().when(projectTab).getLogLevels();
		projectTab.logSettings = Maps.newLinkedHashMap();
		
		Map<String, String> logLevels = projectTab.getLogLevels();
		
		assertNotNull(logLevels);
		assertTrue(logLevels.isEmpty());
	}
	
	@Test
	public void testGetLogLevelsNullComboBox() {
		doCallRealMethod().when(projectTab).getLogLevels();
		projectTab.logSettings = Maps.newLinkedHashMap();
		projectTab.logSettings.put(ProjectConfigurationTab.logCategories[0], null);
		
		Map<String, String> logLevels = projectTab.getLogLevels();
		
		assertNotNull(logLevels);
		assertTrue(logLevels.isEmpty());
	}
	
	@Test
	public void testGetLogLevels() {
		doCallRealMethod().when(projectTab).getLogLevels();
		projectTab.logSettings = Maps.newLinkedHashMap();
		Combo combo = mock(Combo.class);
		when(combo.getText()).thenReturn(ApexLogLevel.DEBUG.name());
		projectTab.logSettings.put(ProjectConfigurationTab.logCategories[0], combo);
		
		Map<String, String> logLevels = projectTab.getLogLevels();
		
		assertNotNull(logLevels);
		assertEquals(1, logLevels.size());
		assertEquals(ApexLogLevel.DEBUG.name(), logLevels.get(ProjectConfigurationTab.logCategories[0]));
	}
	
	@Test
	public void testCreateControlWithNullParentComposite() {
		doCallRealMethod().when(projectTab).createControl(null);
		
		projectTab.createControl(null);
		
		verify(projectTab, never()).createComposite(null, SWT.NONE);
		verify(projectTab, never()).createProjectSelector(null);
		verify(projectTab, never()).createLogEditor(null);
	}
	
	@Test
	public void testShouldEnableBasedOnNullText() {
		genericShouldEnableBasedOnText(null, false);
	}
	
	@Test
	public void testShouldEnableBasedOnEmptyText() {
		genericShouldEnableBasedOnText("", false);
	}
	
	@Test
	public void testShouldEnableBasedOnAllClassesText() {
		genericShouldEnableBasedOnText(Messages.Tab_AllClasses, false);
	}
	
	@Test
	public void testShouldEnableBasedOnAllMethodsText() {
		genericShouldEnableBasedOnText(Messages.Tab_AllMethods, false);
	}
	
	@Test
	public void testShouldEnableBasedOnValidText() {
		genericShouldEnableBasedOnText("yep", true);
	}
	
	private void genericShouldEnableBasedOnText(String text, boolean enabled) {
		doCallRealMethod().when(testTab).shouldEnableBasedOnText(any(String.class));
		
		assertEquals(enabled, testTab.shouldEnableBasedOnText(text));
	}
	
	@Test
	public void testSetTextPropertiesWithNullText() {
		doCallRealMethod().when(testTab).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		
		assertNull(testTab.setTextProperties(null, null, null));
	}
	
	@Test
	public void testSetTextProperties() {
		Text text = mock(Text.class);
		Color color = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
		doNothing().when(text).setLayoutData(any(GridData.class));
		doNothing().when(text).setEditable(false);
		doNothing().when(text).setText("");
		doNothing().when(text).setForeground(color);
		doCallRealMethod().when(testTab).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		
		assertEquals(text, testTab.setTextProperties(text, "", color));
		verify(text, times(1)).setLayoutData(any(GridData.class));
		verify(text, times(1)).setEditable(false);
		verify(text, times(1)).setText("");
		verify(text, times(1)).setForeground(any(Color.class));
	}
	
	@Test
	public void testShouldEnableLevelsNullLogStatus() {
		doCallRealMethod().when(projectTab).shouldEnableLevels(any(Boolean.class));
		projectTab.logStatus = null;
		
		LinkedHashMap<String, Combo> logSettings = Maps.newLinkedHashMap();
		Combo combo = mock(Combo.class, RETURNS_DEFAULTS);
		logSettings.put(ProjectConfigurationTab.logCategories[7], combo);
		projectTab.logSettings = logSettings;
		
		projectTab.shouldEnableLevels(true);
		
		verify(combo, never()).setEnabled(true);
	}
	
	@Test
	public void testShouldEnableLevelsNullLogSettings() {
		doCallRealMethod().when(projectTab).shouldEnableLevels(any(Boolean.class));
		Button logStatus = mock(Button.class, RETURNS_DEFAULTS);
		projectTab.logStatus = logStatus;
		
		projectTab.logSettings = null;
		
		projectTab.shouldEnableLevels(true);
		
		verify(logStatus, never()).setSelection(true);
	}
	
	@Test
	public void testShouldEnableLevels() {
		doCallRealMethod().when(projectTab).shouldEnableLevels(any(Boolean.class));
		Button logStatus = mock(Button.class, RETURNS_DEFAULTS);
		projectTab.logStatus = logStatus;
		
		LinkedHashMap<String, Combo> logSettings = Maps.newLinkedHashMap();
		Combo combo = mock(Combo.class, RETURNS_DEFAULTS);
		logSettings.put(ProjectConfigurationTab.logCategories[7], combo);
		projectTab.logSettings = logSettings;
		
		projectTab.shouldEnableLevels(true);
		
		verify(logStatus, times(1)).setSelection(true);
		verify(combo, times(1)).setEnabled(true);
	}
	
	@Test
	public void testSetLogLevelsNoneProvided() {
		doCallRealMethod().when(projectTab).setLogLevels(any(Map.class));
		
		LinkedHashMap<String, Combo> logSettings = Maps.newLinkedHashMap();
		Combo combo = mock(Combo.class, RETURNS_DEFAULTS);
		logSettings.put(ProjectConfigurationTab.logCategories[7], combo);
		projectTab.logSettings = logSettings;
		
		projectTab.setLogLevels(null);
		
		verify(combo, never()).setText(any(String.class));
	}
	
	@Test
	public void testSetLogLevelsUnsupportedCategory() {
		doCallRealMethod().when(projectTab).setLogLevels(any(Map.class));
		
		LinkedHashMap<String, Combo> logSettings = Maps.newLinkedHashMap();
		Combo combo = mock(Combo.class, RETURNS_DEFAULTS);
		logSettings.put(ProjectConfigurationTab.logCategories[7], combo);
		projectTab.logSettings = logSettings;
		
		Map<String, String> logLevels = Maps.newLinkedHashMap();
		logLevels.put("nope", ApexLogLevel.DEBUG.name());
		
		projectTab.setLogLevels(logLevels);
		
		verify(combo, never()).setText(ApexLogLevel.DEBUG.name());
	}
	
	@Test
	public void testSetLogLevelsSupportedCategory() {
		doCallRealMethod().when(projectTab).setLogLevels(any(Map.class));
		
		LinkedHashMap<String, Combo> logSettings = Maps.newLinkedHashMap();
		Combo combo = mock(Combo.class, RETURNS_DEFAULTS);
		logSettings.put(ProjectConfigurationTab.logCategories[7], combo);
		projectTab.logSettings = logSettings;
		
		Map<String, String> logLevels = Maps.newLinkedHashMap();
		logLevels.put(ProjectConfigurationTab.logCategories[7], ApexLogLevel.DEBUG.name());
		
		projectTab.setLogLevels(logLevels);
		
		verify(combo, times(1)).setText(ApexLogLevel.DEBUG.name());
	}
	
	@Test
	public void testHandleProjectButtonNullProject() {
		doCallRealMethod().when(projectTab).handleProjectButtonSelected();
		
		when(projectTab.chooseProject()).thenReturn(null);
		
		projectTab.handleProjectButtonSelected();
		
		verify(projectTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		assertNull(projectTab.projectText);
	}
	
	@Test
	public void testHandleProjectButtonNullProjectTextWidget() {
		doCallRealMethod().when(projectTab).handleProjectButtonSelected();
		
		IProject project = mock(IProject.class);
		when(project.getName()).thenReturn("MyProject");
		when(projectTab.chooseProject()).thenReturn(project);
				
		projectTab.handleProjectButtonSelected();
		
		verify(projectTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		assertNull(projectTab.projectText);
	}
	
	@Test
	public void testHandleProjectButtonDifferentProject() {
		doCallRealMethod().when(projectTab).handleProjectButtonSelected();
		
		String projectName = "MyProject";
		IProject project = mock(IProject.class);
		when(project.getName()).thenReturn(projectName);
		when(projectTab.chooseProject()).thenReturn(project);
				
		Text projectText = mock(Text.class);
		when(projectText.getText()).thenReturn(projectName + "New");
		when(projectTab.setTextProperties(eq(projectText), eq(projectName), any(Color.class))).thenReturn(projectText);
		projectTab.projectText = projectText;
		
		doNothing().when(testTab).resetTestSelection();
		when(testTab.fetchSuites(any(IProject.class))).thenReturn(Collections.EMPTY_LIST);
		
		projectTab.handleProjectButtonSelected();
		
		verify(testTab, times(1)).resetTestSelection();
		verify(testTab, times(1)).fetchSuites(project);
		verify(projectTab, times(1)).setTextProperties(eq(projectText), eq(projectName), any(Color.class));
	}
	
	@Test
	public void testHandleProjectButtonSameProject() {
		doCallRealMethod().when(projectTab).handleProjectButtonSelected();
				
		String projectName = "MyProject";
		IProject project = mock(IProject.class);
		when(project.getName()).thenReturn(projectName);
		when(projectTab.chooseProject()).thenReturn(project);
				
		Text projectText = mock(Text.class);
		when(projectText.getText()).thenReturn(projectName);
		when(projectTab.setTextProperties(eq(projectText), eq(projectName), any(Color.class))).thenReturn(projectText);
		projectTab.projectText = projectText;
				
		projectTab.handleProjectButtonSelected();
		
		verify(testTab, never()).resetTestSelection();
		verify(testTab, never()).fetchSuites(any(IProject.class));
		verify(projectTab, times(1)).setTextProperties(eq(projectText), eq(projectName), any(Color.class));
	}
	
	@Test
	public void testHandleClassButtonNullClass() {
		doCallRealMethod().when(testTab).handleClassButtonSelected();
		
		when(testTab.chooseTestClass()).thenReturn(null);
		
		Button testMethodButton = mock(Button.class);
		testTab.testMethodButton = testMethodButton;
		
		testTab.handleClassButtonSelected();
		
		verify(testTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		verify(testMethodButton, never()).setEnabled(any(Boolean.class));
	}
	
	@Test
	public void testHandleClassButtonNullClassTextWidget() {
		doCallRealMethod().when(testTab).handleClassButtonSelected();
		
		when(testTab.chooseTestClass()).thenReturn("MyClass");
		
		Button testMethodButton = mock(Button.class);
		testTab.testMethodButton = testMethodButton;
		
		testTab.handleClassButtonSelected();
		
		verify(testTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		verify(testMethodButton, never()).setEnabled(any(Boolean.class));
	}
	
	@Test
	public void testHandleClassButtonDifferentClass() {
		doCallRealMethod().when(testTab).handleClassButtonSelected();
		
		String newClass = "NewClass";
		when(testTab.chooseTestClass()).thenReturn(newClass);
		
		String oldClass = "oldClass";
		Text classText = mock(Text.class);
		when(classText.getText()).thenReturn(oldClass);
		testTab.classText = classText;
		doCallRealMethod().when(testTab).getTestClassName();
		
		when(testTab.setTextProperties(eq(testTab.testMethodText), eq(Messages.Tab_AllMethods), any(Color.class))).thenReturn(testTab.testMethodText);
		when(testTab.setTextProperties(eq(testTab.classText), eq(newClass), any(Color.class))).thenReturn(testTab.classText);
				
		Button testMethodButton = mock(Button.class, RETURNS_DEFAULTS);
		doNothing().when(testMethodButton).setEnabled(any(Boolean.class));
		testTab.testMethodButton = testMethodButton;
		
		testTab.handleClassButtonSelected();
		
		verify(testTab, times(1)).setTextProperties(eq(testTab.testMethodText), eq(Messages.Tab_AllMethods), any(Color.class));
		verify(testTab, times(1)).setTextProperties(eq(testTab.classText), eq(newClass), any(Color.class));
		verify(testMethodButton, times(1)).setEnabled(any(Boolean.class));
	}
	
	@Test
	public void testHandleClassButtonSameClass() {
		doCallRealMethod().when(testTab).handleClassButtonSelected();
		
		String newClass = "NewClass";
		when(testTab.chooseTestClass()).thenReturn(newClass);
		
		Text classText = mock(Text.class);
		when(classText.getText()).thenReturn(newClass);
		testTab.classText = classText;
		doCallRealMethod().when(testTab).getTestClassName();
				
		when(testTab.setTextProperties(eq(testTab.testMethodText), eq(Messages.Tab_AllMethods), any(Color.class))).thenReturn(testTab.testMethodText);
		when(testTab.setTextProperties(eq(testTab.classText), eq(newClass), any(Color.class))).thenReturn(testTab.classText);
				
		Button testMethodButton = mock(Button.class, RETURNS_DEFAULTS);
		doNothing().when(testMethodButton).setEnabled(any(Boolean.class));
		testTab.testMethodButton = testMethodButton;
		
		testTab.handleClassButtonSelected();
		
		verify(testTab, never()).setTextProperties(eq(testTab.testMethodText), eq(Messages.Tab_AllMethods), any(Color.class));
		verify(testTab, times(1)).setTextProperties(eq(testTab.classText), eq(newClass), any(Color.class));
		verify(testMethodButton, times(1)).setEnabled(any(Boolean.class));
	}
	
	@Test
	public void testHandleTestMethodButtonNullMethod() {
		doCallRealMethod().when(testTab).handleTestMethodButtonSelected();
		when(testTab.chooseTestMethod()).thenReturn(null);
		
		testTab.handleTestMethodButtonSelected();
		
		verify(testTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
	}

	@Test
	public void testHandleTestMethodButtonNullMethodTextWidget() {
		doCallRealMethod().when(testTab).handleTestMethodButtonSelected();
		
		String methodName = "MyMethod";
		when(testTab.chooseTestMethod()).thenReturn(methodName);
		
		testTab.handleTestMethodButtonSelected();
		
		verify(testTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
	}
	
	@Test
	public void testHandleTestMethodButtonSelected() {
		doCallRealMethod().when(testTab).handleTestMethodButtonSelected();
		
		String methodName = "MyMethod";
		when(testTab.chooseTestMethod()).thenReturn(methodName);
		
		Text testMethodText = mock(Text.class);
		testTab.testMethodText = testMethodText;
				
		when(testTab.setTextProperties(eq(testMethodText), eq(methodName), any(Color.class))).thenReturn(testMethodText);
		
		testTab.handleTestMethodButtonSelected();
		
		verify(testTab, times(1)).setTextProperties(eq(testMethodText), eq(methodName), any(Color.class));
	}
	
	@Test
	public void testProjectTabIsValidWithNoErrors() {
		doCallRealMethod().when(projectTab).isValid(any(ILaunchConfiguration.class));
		when(projectTab.validatePage()).thenReturn(true);
		when(projectTab.getErrorMessage()).thenReturn(null);
		
		assertTrue(projectTab.isValid(mock(ILaunchConfiguration.class)));
	}
	
	@Test
	public void testTestTabIsValidWithNoErrors() {
		doCallRealMethod().when(testTab).isValid(any(ILaunchConfiguration.class));
		when(testTab.validatePage()).thenReturn(true);
		when(testTab.getErrorMessage()).thenReturn(null);
		
		assertTrue(testTab.isValid(mock(ILaunchConfiguration.class)));
	}
	
	@Test
	public void testProjectTabIsValidWithEmptyErrorMessage() {
		doCallRealMethod().when(projectTab).isValid(any(ILaunchConfiguration.class));
		when(projectTab.validatePage()).thenReturn(false);
		when(projectTab.getErrorMessage()).thenReturn("");
		
		assertFalse(projectTab.isValid(mock(ILaunchConfiguration.class)));
	}
	
	@Test
	public void testTestTabIsValidWithEmptyErrorMessage() {
		doCallRealMethod().when(testTab).isValid(any(ILaunchConfiguration.class));
		when(testTab.validatePage()).thenReturn(false);
		when(testTab.getErrorMessage()).thenReturn("");
		
		assertFalse(testTab.isValid(mock(ILaunchConfiguration.class)));
	}
	
	@Test
	public void testProjectTabIsValidWithRealErrorMessage() {
		doCallRealMethod().when(projectTab).isValid(any(ILaunchConfiguration.class));
		when(projectTab.validatePage()).thenReturn(false);
		when(projectTab.getErrorMessage()).thenReturn("OhNo");
		
		assertFalse(projectTab.isValid(mock(ILaunchConfiguration.class)));
	}
	
	@Test
	public void testTestTabIsValidWithRealErrorMessage() {
		doCallRealMethod().when(testTab).isValid(any(ILaunchConfiguration.class));
		when(testTab.validatePage()).thenReturn(false);
		when(testTab.getErrorMessage()).thenReturn("OhNo");
		
		assertFalse(testTab.isValid(mock(ILaunchConfiguration.class)));
	}
	
	@Test
	public void testProjectTabValidatePageBadProject() {
		doCallRealMethod().when(projectTab).validatePage();
		
		when(projectTab.validateProjectSelection()).thenReturn(false);
		when(testTab.validateSuiteSelection()).thenReturn(true);
		
		assertFalse(projectTab.validatePage());
	}
	
	@Test
	public void testProjectTabValidatePageBadSuite() {
		doCallRealMethod().when(projectTab).validatePage();
		
		when(projectTab.validateProjectSelection()).thenReturn(true);
		when(testTab.validateSuiteSelection()).thenReturn(false);
		
		assertFalse(projectTab.validatePage());
	}
	
	@Test
	public void testProjectTabValidatePage() {
		doCallRealMethod().when(projectTab).validatePage();
		
		when(projectTab.validateProjectSelection()).thenReturn(true);
		when(testTab.validateSuiteSelection()).thenReturn(true);
		
		assertTrue(projectTab.validatePage());
	}
	
	@Test
	public void testTestTabValidatePageBadProject() {
		doCallRealMethod().when(testTab).validatePage();
		
		when(projectTab.validateProjectSelection()).thenReturn(false);
		when(testTab.validateSuiteSelection()).thenReturn(true);
		
		assertFalse(testTab.validatePage());
	}
	
	@Test
	public void testTestTabValidatePageBadSuite() {
		doCallRealMethod().when(testTab).validatePage();
		
		when(projectTab.validateProjectSelection()).thenReturn(true);
		when(testTab.validateSuiteSelection()).thenReturn(false);
		
		assertFalse(testTab.validatePage());
	}
	
	@Test
	public void testTestTabValidatePage() {
		doCallRealMethod().when(testTab).validatePage();
		
		when(projectTab.validateProjectSelection()).thenReturn(true);
		when(testTab.validateSuiteSelection()).thenReturn(true);
		
		assertTrue(testTab.validatePage());
	}
	
	@Test
	public void testValidateProjectSelectionNullProjectName() {
		doCallRealMethod().when(projectTab).validateProjectSelection();
		when(projectTab.getProjectName()).thenReturn(null);
		
		assertFalse(projectTab.validateProjectSelection());
	}
	
	@Test
	public void testValidateProjectSelectionEmptyProjectName() {
		doCallRealMethod().when(projectTab).validateProjectSelection();
		when(projectTab.getProjectName()).thenReturn("");
		
		assertFalse(projectTab.validateProjectSelection());
	}
	
	@Test
	public void testValidateProjectSelectionNullProject() {
		doCallRealMethod().when(projectTab).validateProjectSelection();
		when(projectTab.getProjectName()).thenReturn("MyProject");
		when(projectTab.getProjectFromName()).thenReturn(null);
		
		assertFalse(projectTab.validateProjectSelection());
	}
	
	@Test
	public void testValidateProjectSelectionProjectWithoutCorrectNatureId() throws Exception {
		doCallRealMethod().when(projectTab).validateProjectSelection();
		when(projectTab.getProjectName()).thenReturn("MyProject");
		
		IProject project = mock(IProject.class);
		when(project.hasNature(DefaultNature.NATURE_ID)).thenReturn(false);
		when(projectTab.getProjectFromName()).thenReturn(project);
		
		assertFalse(projectTab.validateProjectSelection());
	}
	
	@Test
	public void testValidateProjectSelection() throws Exception {
		doCallRealMethod().when(projectTab).validateProjectSelection();
		when(projectTab.getProjectName()).thenReturn("MyProject");
		
		IProject project = mock(IProject.class);
		when(project.hasNature(DefaultNature.NATURE_ID)).thenReturn(true);
		when(projectTab.getProjectFromName()).thenReturn(project);
		
		assertTrue(projectTab.validateProjectSelection());
	}
	
	@Test
	public void testValidateSuiteSelectionDisabledSuites() {
		doCallRealMethod().when(testTab).validateSuiteSelection();
		
		Button suiteStatus = mock(Button.class);
		when(suiteStatus.isEnabled()).thenReturn(true);
		when(suiteStatus.getSelection()).thenReturn(false);
		testTab.suiteStatus = suiteStatus;
		
		assertTrue(testTab.validateSuiteSelection());
	}
	
	@Test
	public void testValidateSuiteSelectionNoSuitesSelected() {
		doCallRealMethod().when(testTab).validateSuiteSelection();
		
		Button suiteStatus = mock(Button.class);
		when(suiteStatus.isEnabled()).thenReturn(true);
		when(suiteStatus.getSelection()).thenReturn(true);
		testTab.suiteStatus = suiteStatus;
		
		Table suiteTable = mock(Table.class);
		TableItem ti = mock(TableItem.class);
		when(ti.getChecked()).thenReturn(false);
		when(suiteTable.getItems()).thenReturn(new TableItem[] { ti });
		testTab.suiteTable = suiteTable;
		
		assertFalse(testTab.validateSuiteSelection());
	}
	
	@Test
	public void testValidateSuiteSelection() {
		doCallRealMethod().when(testTab).validateSuiteSelection();
		
		Button suiteStatus = mock(Button.class);
		when(suiteStatus.isEnabled()).thenReturn(true);
		when(suiteStatus.getSelection()).thenReturn(true);
		testTab.suiteStatus = suiteStatus;
		
		Table suiteTable = mock(Table.class);
		TableItem ti = mock(TableItem.class);
		when(ti.getChecked()).thenReturn(true);
		when(suiteTable.getItems()).thenReturn(new TableItem[] { ti });
		testTab.suiteTable = suiteTable;
		
		assertTrue(testTab.validateSuiteSelection());
	}
	
	@Test
	public void testGetProjectFromNullName() {
		doCallRealMethod().when(projectTab).getProjectFromName();
		when(projectTab.getProjectName()).thenReturn(null);
		
		assertNull(projectTab.getProjectFromName());
	}
	
	@Test
	public void testProjectTabPerformApply() {
		doCallRealMethod().when(projectTab).performApply(any(ILaunchConfigurationWorkingCopy.class));
		ILaunchConfigurationWorkingCopy config = mock(ILaunchConfigurationWorkingCopy.class);
		doNothing().when(config).setAttribute(any(String.class), any(String.class));
		doNothing().when(config).setAttribute(any(String.class), any(Boolean.class));
		doNothing().when(config).setAttribute(any(String.class), any(Map.class));
		
		String projectName = "MyProject";
		when(projectTab.getProjectName()).thenReturn(projectName);
		boolean isLoggingEnabled = true;
		when(projectTab.isLoggingEnabled()).thenReturn(isLoggingEnabled);
		Map<String, String> logLevels = Maps.newLinkedHashMap();
		when(projectTab.getLogLevels()).thenReturn(logLevels);
		
		projectTab.performApply(config);
		
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_PROJECT_NAME, projectName);
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_ENABLE_LOGGING, isLoggingEnabled);
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_LOG_LEVELS, logLevels);
	}
	
	@Test
	public void testProjectTabInitializeFrom() throws Exception {
		doCallRealMethod().when(projectTab).initializeFrom(any(ILaunchConfiguration.class));
		ILaunchConfiguration config = mock(ILaunchConfiguration.class);
		
		String projectName = "MyProject";
		boolean isLoggingEnabled = true;
		Map<String, String> logLevels = Maps.newLinkedHashMap();
		
		when(config.getAttribute(RunTestsConstants.ATTR_PROJECT_NAME, "")).thenReturn(projectName);
		when(projectTab.setTextProperties(eq(projectTab.projectText), eq(projectName), any(Color.class))).thenReturn(projectTab.projectText);
		
		when(config.getAttribute(RunTestsConstants.ATTR_ENABLE_LOGGING, false)).thenReturn(isLoggingEnabled);
		doNothing().when(projectTab).shouldEnableLevels(isLoggingEnabled);
		
		when(config.getAttribute(eq(RunTestsConstants.ATTR_LOG_LEVELS), any(Map.class))).thenReturn(logLevels);
		doNothing().when(projectTab).setLogLevels(logLevels);
				
		projectTab.initializeFrom(config);
		
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_PROJECT_NAME, "");
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_ENABLE_LOGGING, false);
		verify(config, times(1)).getAttribute(eq(RunTestsConstants.ATTR_LOG_LEVELS), any(Map.class));
		verify(projectTab, times(1)).setTextProperties(eq(projectTab.projectText), eq(projectName), any(Color.class));
		verify(projectTab, times(1)).shouldEnableLevels(isLoggingEnabled);
		verify(projectTab, times(1)).setLogLevels(logLevels);
	}
	
	@Test
	public void testTestTabPerformApplyWithResetTestSelection() {
		doCallRealMethod().when(testTab).performApply(any(ILaunchConfigurationWorkingCopy.class));
		doCallRealMethod().when(testTab).resetTestSelection();
		
		ILaunchConfigurationWorkingCopy config = mock(ILaunchConfigurationWorkingCopy.class);
		doNothing().when(config).setAttribute(any(String.class), any(String.class));
		doNothing().when(config).setAttribute(any(String.class), any(Boolean.class));
		
		boolean shouldUseSuites = true;
		when(testTab.shouldUseSuites()).thenReturn(shouldUseSuites);
		
		String currentSelectedSuiteIds = "foo,bar";
		IProject project = mock(IProject.class);
		when(projectTab.getProjectFromName()).thenReturn(project);
		when(testTab.getCommaSeparatedSuiteIds(project)).thenReturn(currentSelectedSuiteIds);
		
		doCallRealMethod().when(testTab).buildSuitesForConfig(currentSelectedSuiteIds);
		doCallRealMethod().when(testTab).buildTestsForConfig(project);
		
		boolean isAsync = true;
		when(testTab.isAsyncTestRun(any(TestsHolder.class), eq(shouldUseSuites))).thenReturn(isAsync);
		
		testTab.allTests = Collections.emptyMap();
		
		testTab.resetTestSelection();
		testTab.performApply(config);
		
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_TEST_CLASS, Messages.Tab_AllClasses);
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_TEST_METHOD, Messages.Tab_AllMethods);
		assertFalse(testTab.resetTestSelection);
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_USE_SUITES, shouldUseSuites);
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_SUITE_IDS, currentSelectedSuiteIds);
		verify(config, times(1)).setAttribute(eq(RunTestsConstants.ATTR_SUITES), any(String.class));
		verify(config, times(1)).setAttribute(eq(RunTestsConstants.ATTR_TESTS_ARRAY), any(String.class));
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_TEST_MODE, isAsync);
		
	}
	
	@Test
	public void testTestTabPerformApplyWithoutResetTestSelection() {
		doCallRealMethod().when(testTab).performApply(any(ILaunchConfigurationWorkingCopy.class));
		
		ILaunchConfigurationWorkingCopy config = mock(ILaunchConfigurationWorkingCopy.class);
		doNothing().when(config).setAttribute(any(String.class), any(String.class));
		doNothing().when(config).setAttribute(any(String.class), any(Boolean.class));
		
		String className = "className";
		when(testTab.getTestClassName()).thenReturn(className);
		String methodName = "methodName";
		when(testTab.getTestMethodName()).thenReturn(methodName);
		
		boolean shouldUseSuites = true;
		when(testTab.shouldUseSuites()).thenReturn(shouldUseSuites);
		
		String currentSelectedSuiteIds = "foo,bar";
		IProject project = mock(IProject.class);
		when(projectTab.getProjectFromName()).thenReturn(project);
		when(testTab.getCommaSeparatedSuiteIds(project)).thenReturn(currentSelectedSuiteIds);
		
		doCallRealMethod().when(testTab).buildSuitesForConfig(currentSelectedSuiteIds);
		doCallRealMethod().when(testTab).buildTestsForConfig(project);
		
		boolean isAsync = true;
		when(testTab.isAsyncTestRun(any(TestsHolder.class), eq(shouldUseSuites))).thenReturn(isAsync);
		
		testTab.allTests = Collections.emptyMap();
		
		testTab.resetTestSelection();
		testTab.performApply(config);
		
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_TEST_CLASS, className);
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_TEST_METHOD, methodName);
		assertFalse(testTab.resetTestSelection);
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_USE_SUITES, shouldUseSuites);
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_SUITE_IDS, currentSelectedSuiteIds);
		verify(config, times(1)).setAttribute(eq(RunTestsConstants.ATTR_SUITES), any(String.class));
		verify(config, times(1)).setAttribute(eq(RunTestsConstants.ATTR_TESTS_ARRAY), any(String.class));
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_TEST_MODE, isAsync);
	}
	
	@Test
	public void testTestTabInitializeFrom() throws Exception {
		doCallRealMethod().when(testTab).initializeFrom(any(ILaunchConfiguration.class));
		ILaunchConfiguration config = mock(ILaunchConfiguration.class);
		
		IProject project = mock(IProject.class);
		when(projectTab.getProjectFromName()).thenReturn(project);
		testTab.allTests = Maps.newHashMap();
		when(testTab.buildTestsForProject(project)).thenReturn(mock(TestsHolder.class));
		
		when(testTab.setTextProperties(any(Text.class), any(String.class), any(Color.class))).thenReturn(mock(Text.class));
		
		String testClassName = "className";
		when(config.getAttribute(RunTestsConstants.ATTR_TEST_CLASS, Messages.Tab_AllClasses)).thenReturn(testClassName);
		String projectName = "projectName";
		when(projectTab.getProjectName()).thenReturn(projectName);
		when(testTab.shouldEnableBasedOnText(any(String.class))).thenReturn(true);
		Button classButton = mock(Button.class);
		doNothing().when(classButton).setEnabled(true);;
		testTab.classButton = classButton;
		
		String testMethodName = "methodName";
		when(config.getAttribute(RunTestsConstants.ATTR_TEST_METHOD, Messages.Tab_AllMethods)).thenReturn(testMethodName);
		when(testTab.getTestClassName()).thenReturn(testClassName);
		Button testMethodButton = mock(Button.class);
		doNothing().when(testMethodButton).setEnabled(true);;
		testTab.testMethodButton = testMethodButton;
		
		boolean shouldUseSuites = true;
		when(config.getAttribute(RunTestsConstants.ATTR_USE_SUITES, false)).thenReturn(shouldUseSuites);
		doNothing().when(testTab).enableSuiteTable(shouldUseSuites);
		when(config.getAttribute(RunTestsConstants.ATTR_SUITE_IDS, "")).thenReturn("foo,bar");
		doNothing().when(testTab).reconcileSuites(any(Set.class));
		
		testTab.initializeFrom(config);
		
		verify(testTab, times(1)).buildTestsForProject(project);
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_TEST_CLASS, Messages.Tab_AllClasses);
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_TEST_METHOD, Messages.Tab_AllMethods);
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_USE_SUITES, false);
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_SUITE_IDS, "");
	}
	
	@Test
	public void testEnableSuiteTableTrue() {
		genericEnableSuiteTest(true);
	}
	
	@Test
	public void testEnableSuiteTableFalse() {
		genericEnableSuiteTest(false);
	}
	
	private void genericEnableSuiteTest(boolean enable) {
		doCallRealMethod().when(testTab).enableSuiteTable(enable);
		
		Button suiteStatus = mock(Button.class);
		doNothing().when(suiteStatus).setSelection(any(Boolean.class));
		testTab.suiteStatus = suiteStatus;
		
		Table suiteTable = mock(Table.class);
		doNothing().when(suiteTable).setEnabled(any(Boolean.class));
		TableItem ti = mock(TableItem.class);
		doNothing().when(ti).setGrayed(any(Boolean.class));
		when(suiteTable.getItems()).thenReturn(new TableItem[] { ti });
		testTab.suiteTable = suiteTable;
		
		Text classText = mock(Text.class);
		doNothing().when(classText).setEnabled(any(Boolean.class));
		testTab.classText = classText;
		
		Text testMethodText = mock(Text.class);
		doNothing().when(testMethodText).setEnabled(any(Boolean.class));
		testTab.testMethodText = testMethodText;
		
		Button classButton = mock(Button.class);
		doNothing().when(classButton).setEnabled(any(Boolean.class));
		testTab.classButton = classButton;
		
		Button testMethodButton = mock(Button.class);
		doNothing().when(testMethodButton).setEnabled(any(Boolean.class));
		testTab.testMethodButton = testMethodButton;
		
		when(testTab.shouldEnableBasedOnText(any(String.class))).thenReturn(!enable);
		
		testTab.enableSuiteTable(enable);
		
		verify(suiteStatus, times(1)).setSelection(enable);
		verify(suiteTable, times(1)).setEnabled(enable);
		verify(ti, times(1)).setGrayed(enable);
		verify(classText, times(1)).setEnabled(!enable);
		verify(testMethodText, times(1)).setEnabled(!enable);
		verify(classButton, times(1)).setEnabled(!enable);
		verify(testMethodButton, times(1)).setEnabled(!enable);
	}
	
	@Test
	public void testFetchSuitesNullProject() {
		doCallRealMethod().when(testTab).fetchSuites(any(IProject.class));
		testTab.suiteManagers = Maps.newHashMap();
		
		assertTrue(testTab.fetchSuites(null).isEmpty());
		assertTrue(testTab.suiteManagers.isEmpty());
	}
	
	@Test
	public void testFetchSuites() {
		doCallRealMethod().when(testTab).fetchSuites(any(IProject.class));
		testTab.suiteManagers = Maps.newHashMap();
		
		IProject project = mock(IProject.class);
		SuiteManager mgr = mock(SuiteManager.class);
		when(mgr.fetchSuites()).thenReturn(Collections.EMPTY_LIST);
		when(testTab.createSuiteMgr(project)).thenReturn(mgr);
		doNothing().when(testTab).generateSuiteTable(any(java.util.List.class));
		
		assertTrue(testTab.fetchSuites(project).isEmpty());
		assertEquals(1, testTab.suiteManagers.size());
		verify(testTab, times(1)).generateSuiteTable(any(java.util.List.class));
	}
	
	@Test
	public void testCreateSuiteMgrNullProject() {
		doCallRealMethod().when(testTab).createSuiteMgr(any(IProject.class));
		testTab.suiteManagers = Maps.newHashMap();
		
		assertNull(testTab.createSuiteMgr(null));
		assertTrue(testTab.suiteManagers.isEmpty());
	}
	
	@Test
	public void testReconcileSuitesNullProject() {
		doCallRealMethod().when(testTab).reconcileSuites(any(Set.class));
		when(projectTab.getProjectFromName()).thenReturn(null);
		
		testTab.reconcileSuites(Collections.EMPTY_SET);
		
		verify(testTab, never()).fetchSuites(any(IProject.class));
		verify(testTab, never()).generateSuiteTable(any(java.util.List.class));
	}
	
	@Test
	public void testGenerateSuiteTableNull() {
		doCallRealMethod().when(testTab).generateSuiteTable(any(java.util.List.class));
		
		Table suiteTable = mock(Table.class);
		testTab.suiteTable = suiteTable;
		
		testTab.generateSuiteTable(null);
		
		verify(suiteTable, never()).removeAll();
		verify(suiteTable, never()).clearAll();
	}
	
	@Test
	public void testGenerateSuiteTableEmpty() {
		doCallRealMethod().when(testTab).generateSuiteTable(any(java.util.List.class));
		
		Table suiteTable = mock(Table.class);
		testTab.suiteTable = suiteTable;
		
		testTab.generateSuiteTable(Collections.EMPTY_LIST);
		
		verify(suiteTable, never()).removeAll();
		verify(suiteTable, never()).clearAll();
	}
}
