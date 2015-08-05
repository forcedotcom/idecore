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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.remote.tooling.RunTests;

import junit.framework.TestCase;

public class RunTestsTabTest_unit extends TestCase {
	
	private RunTestsTab mockedTab;
	
	@Before
    @Override
    public void setUp() throws Exception {
		mockedTab = mock(RunTestsTab.class);
	}
	
	@Test
	public void testGetTabName() {
		doCallRealMethod().when(mockedTab).getName();
		
		assertEquals(Messages.RunTestsTab_TabTitle, mockedTab.getName());
	}

	@Test
	public void testGetNullProjectTextWidget() {
		doCallRealMethod().when(mockedTab).getProjectName();
		
		assertEquals("", mockedTab.getProjectName());
	}
	
	@Test
	public void testGetNullClassTextWidget() {
		doCallRealMethod().when(mockedTab).getTestClassName();
		
		assertEquals("", mockedTab.getTestClassName());
	}
	
	@Test
	public void testGetNullMethodTextWidget() {
		doCallRealMethod().when(mockedTab).getTestMethodName();
		
		assertEquals("", mockedTab.getTestMethodName());
	}
	
	@Test
	public void testAsyncModeWithNullButtonWidget() {
		doCallRealMethod().when(mockedTab).isTestModeAsync();
		
		assertFalse(mockedTab.isTestModeAsync());
	}
	
	@Test
	public void testAsyncModeWithDisabledButton() {
		Button asyncButton = mock(Button.class);
		when(asyncButton.isEnabled()).thenReturn(false);
		mockedTab.testAsyncButton = asyncButton;
		doCallRealMethod().when(mockedTab).isTestModeAsync();
		
		assertFalse(mockedTab.isTestModeAsync());
	}
	
	@Test
	public void testAsyncModeWithUnselectedButton() {
		Button asyncButton = mock(Button.class);
		when(asyncButton.isEnabled()).thenReturn(true);
		when(asyncButton.getSelection()).thenReturn(false);
		mockedTab.testAsyncButton = asyncButton;
		doCallRealMethod().when(mockedTab).isTestModeAsync();
		
		assertFalse(mockedTab.isTestModeAsync());
	}
	
	@Test
	public void testAsyncModeWithSelectedButton() {
		Button asyncButton = mock(Button.class);
		when(asyncButton.isEnabled()).thenReturn(true);
		when(asyncButton.getSelection()).thenReturn(true);
		mockedTab.testAsyncButton = asyncButton;
		doCallRealMethod().when(mockedTab).isTestModeAsync();
		
		assertTrue(mockedTab.isTestModeAsync());
	}
	
	@Test
	public void testCreateControlWithNullParentComposite() {
		doCallRealMethod().when(mockedTab).createControl(null);
		
		mockedTab.createControl(null);
		
		verify(mockedTab, never()).createComposite(null, SWT.NONE);
		verify(mockedTab, never()).createSingleTestEditor(null);
		verify(mockedTab, never()).createTestModeEditor(null);
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
		genericShouldEnableBasedOnText(Messages.GenericTab_AllClasses, false);
	}
	
	@Test
	public void testShouldEnableBasedOnAllMethodsText() {
		genericShouldEnableBasedOnText(Messages.GenericTab_AllMethods, false);
	}
	
	@Test
	public void testShouldEnableBasedOnValidText() {
		genericShouldEnableBasedOnText("yep", true);
	}
	
	private void genericShouldEnableBasedOnText(String text, boolean enabled) {
		doCallRealMethod().when(mockedTab).shouldEnableBasedOnText(any(String.class));
		
		assertEquals(enabled, mockedTab.shouldEnableBasedOnText(text));
	}
	
	@Test
	public void testSetTextPropertiesWithNullText() {
		doCallRealMethod().when(mockedTab).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		
		assertNull(mockedTab.setTextProperties(null, null, null));
	}
	
	@Test
	public void testSetTextProperties() {
		Text text = mock(Text.class);
		Color color = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
		doNothing().when(text).setLayoutData(any(GridData.class));
		doNothing().when(text).setEditable(false);
		doNothing().when(text).setText("");
		doNothing().when(text).setForeground(color);
		doCallRealMethod().when(mockedTab).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		
		assertEquals(text, mockedTab.setTextProperties(text, "", color));
		verify(text, times(1)).setLayoutData(any(GridData.class));
		verify(text, times(1)).setEditable(false);
		verify(text, times(1)).setText("");
		verify(text, times(1)).setForeground(any(Color.class));
	}
	
	@Test
	public void testResetSelectedTestClass() {
		Color defaultGray = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
		mockedTab.defaultGray = defaultGray;
		
		Text projectText = mock(Text.class);
		mockedTab.projectText = projectText;
		when(projectText.getText()).thenReturn("");
		
		Button classButton = mock(Button.class);
		mockedTab.classButton = classButton;
		doNothing().when(classButton).setEnabled(false);
		
		when(mockedTab.setTextProperties(any(Text.class), any(String.class), any(Color.class))).thenReturn(null);
		when(mockedTab.shouldEnableBasedOnText(any(String.class))).thenReturn(false);
		doCallRealMethod().when(mockedTab).resetSelectedTestClass();
		
		mockedTab.resetSelectedTestClass();
		
		verify(mockedTab, times(1)).setTextProperties(any(Text.class), eq(Messages.GenericTab_AllClasses), eq(defaultGray));
	}
	
	@Test
	public void testResetSelectedTestMethod() {
		Color defaultGray = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
		mockedTab.defaultGray = defaultGray;
		
		Text classText = mock(Text.class);
		mockedTab.classText = classText;
		when(classText.getText()).thenReturn("");
		
		Button testMethodButton = mock(Button.class);
		mockedTab.testMethodButton = testMethodButton;
		doNothing().when(testMethodButton).setEnabled(false);
		
		when(mockedTab.setTextProperties(any(Text.class), any(String.class), any(Color.class))).thenReturn(null);
		when(mockedTab.shouldEnableBasedOnText(any(String.class))).thenReturn(false);
		doCallRealMethod().when(mockedTab).resetSelectedTestMethod();
		
		mockedTab.resetSelectedTestMethod();
		
		verify(mockedTab, times(1)).setTextProperties(any(Text.class), eq(Messages.GenericTab_AllMethods), eq(defaultGray));
	}
	
	@Test
	public void testHandleProjectButtonNullProject() {
		doCallRealMethod().when(mockedTab).handleProjectButtonSelected();
		
		Button classButton = mock(Button.class);
		mockedTab.classButton = classButton;
		
		when(mockedTab.chooseProject()).thenReturn(null);
		
		Map<IProject, RunTests> allTests = Collections.<IProject, RunTests> emptyMap();
		mockedTab.allTests = allTests;
		
		mockedTab.handleProjectButtonSelected();
		
		verify(mockedTab, never()).resetSelectedTestClass();
		verify(mockedTab, never()).resetSelectedTestMethod();
		verify(mockedTab, never()).buildTestsForProject(any(IProject.class));
		assertTrue(allTests.isEmpty());
		verify(classButton, never()).setEnabled(true);
		verify(mockedTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		assertNull(mockedTab.projectText);
	}
	
	@Test
	public void testHandleProjectButtonNullProjectName() {
		doCallRealMethod().when(mockedTab).handleProjectButtonSelected();
		
		Button classButton = mock(Button.class);
		mockedTab.classButton = classButton;
		
		IProject project = mock(IProject.class);
		when(project.getName()).thenReturn(null);
		when(mockedTab.chooseProject()).thenReturn(project);
		
		Map<IProject, RunTests> allTests = Collections.<IProject, RunTests> emptyMap();
		mockedTab.allTests = allTests;
		
		mockedTab.handleProjectButtonSelected();
		
		verify(mockedTab, never()).resetSelectedTestClass();
		verify(mockedTab, never()).resetSelectedTestMethod();
		verify(mockedTab, never()).buildTestsForProject(project);
		assertTrue(allTests.isEmpty());
		verify(classButton, never()).setEnabled(true);
		verify(mockedTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		assertNull(mockedTab.projectText);
	}
	
	@Test
	public void testHandleProjectButtonNullProjectTextWidget() {
		doCallRealMethod().when(mockedTab).handleProjectButtonSelected();
		
		Button classButton = mock(Button.class);
		mockedTab.classButton = classButton;
		
		IProject project = mock(IProject.class);
		when(project.getName()).thenReturn("MyProject");
		when(mockedTab.chooseProject()).thenReturn(project);
		
		Map<IProject, RunTests> allTests = Collections.<IProject, RunTests> emptyMap();
		mockedTab.allTests = allTests;
		
		mockedTab.handleProjectButtonSelected();
		
		verify(mockedTab, never()).resetSelectedTestClass();
		verify(mockedTab, never()).resetSelectedTestMethod();
		verify(mockedTab, never()).buildTestsForProject(project);
		assertTrue(allTests.isEmpty());
		verify(classButton, never()).setEnabled(true);
		verify(mockedTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		assertNull(mockedTab.projectText);
	}
	
	@Test
	public void testHandleProjectButtonDifferentProjectSelectedForTheFirstTime() {
		doCallRealMethod().when(mockedTab).handleProjectButtonSelected();
		
		Button classButton = mock(Button.class);
		doNothing().when(classButton).setEnabled(true);
		mockedTab.classButton = classButton;
		
		String newProject = "NewProject";
		IProject project = mock(IProject.class);
		when(project.getName()).thenReturn(newProject);
		when(mockedTab.chooseProject()).thenReturn(project);
		
		Color normalBlack = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		mockedTab.normalBlack = normalBlack;
		
		String oldProject = "OldProject";
		Text projectText = mock(Text.class);
		when(projectText.getText()).thenReturn(oldProject);
		when(mockedTab.setTextProperties(projectText, newProject, normalBlack)).thenReturn(projectText);
		mockedTab.projectText = projectText;
		
		RunTests rt = mock(RunTests.class);
		
		Map<IProject, RunTests> allTests = new HashMap<IProject, RunTests>();
		mockedTab.allTests = allTests;
		
		doNothing().when(mockedTab).resetSelectedTestClass();
		doNothing().when(mockedTab).resetSelectedTestMethod();
		when(mockedTab.buildTestsForProject(project)).thenReturn(rt);
		
		mockedTab.handleProjectButtonSelected();
		
		verify(mockedTab, times(1)).resetSelectedTestClass();
		verify(mockedTab, times(1)).resetSelectedTestMethod();
		verify(mockedTab, times(1)).buildTestsForProject(project);
		assertEquals(1, allTests.size());
		verify(classButton, times(1)).setEnabled(true);
		verify(mockedTab, times(1)).setTextProperties(projectText, newProject, normalBlack);
	}
	
	@Test
	public void testHandleProjectButtonDifferentProjectSelectedBefore() {
		doCallRealMethod().when(mockedTab).handleProjectButtonSelected();
		
		Button classButton = mock(Button.class);
		doNothing().when(classButton).setEnabled(true);
		mockedTab.classButton = classButton;
		
		String newProject = "NewProject";
		IProject project = mock(IProject.class);
		when(project.getName()).thenReturn(newProject);
		when(mockedTab.chooseProject()).thenReturn(project);
		
		Color normalBlack = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		mockedTab.normalBlack = normalBlack;
		
		String oldProject = "OldProject";
		Text projectText = mock(Text.class);
		when(projectText.getText()).thenReturn(oldProject);
		when(mockedTab.setTextProperties(projectText, newProject, normalBlack)).thenReturn(projectText);
		mockedTab.projectText = projectText;
		
		RunTests rt = mock(RunTests.class);
		
		Map<IProject, RunTests> allTests = new HashMap<IProject, RunTests>();
		allTests.put(project, rt);
		mockedTab.allTests = allTests;
		
		doNothing().when(mockedTab).resetSelectedTestClass();
		doNothing().when(mockedTab).resetSelectedTestMethod();
		
		mockedTab.handleProjectButtonSelected();
		
		verify(mockedTab, times(1)).resetSelectedTestClass();
		verify(mockedTab, times(1)).resetSelectedTestMethod();
		verify(mockedTab, never()).buildTestsForProject(project);
		assertEquals(1, allTests.size());
		verify(classButton, times(1)).setEnabled(true);
		verify(mockedTab, times(1)).setTextProperties(projectText, newProject, normalBlack);
	}
	
	@Test
	public void testHandleProjectButtonSameProject() {
		doCallRealMethod().when(mockedTab).handleProjectButtonSelected();
		
		Button classButton = mock(Button.class);
		doNothing().when(classButton).setEnabled(true);
		mockedTab.classButton = classButton;
		
		String projectName = "MyProject";
		IProject project = mock(IProject.class);
		when(project.getName()).thenReturn(projectName);
		when(mockedTab.chooseProject()).thenReturn(project);
		
		Color normalBlack = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		mockedTab.normalBlack = normalBlack;
		
		Text projectText = mock(Text.class);
		when(projectText.getText()).thenReturn(projectName);
		when(mockedTab.setTextProperties(projectText, projectName, normalBlack)).thenReturn(projectText);
		mockedTab.projectText = projectText;
		
		Map<IProject, RunTests> allTests = Collections.<IProject, RunTests> emptyMap();
		mockedTab.allTests = allTests;
		
		mockedTab.handleProjectButtonSelected();
		
		verify(mockedTab, never()).resetSelectedTestClass();
		verify(mockedTab, never()).resetSelectedTestMethod();
		verify(mockedTab, never()).buildTestsForProject(project);
		assertTrue(allTests.isEmpty());
		verify(classButton, times(1)).setEnabled(true);
		verify(mockedTab, times(1)).setTextProperties(projectText, projectName, normalBlack);
	}
	
	@Test
	public void testHandleClassButtonNullClass() {
		doCallRealMethod().when(mockedTab).handleClassButtonSelected();
		
		when(mockedTab.chooseTestClass()).thenReturn(null);
		
		Button testMethodButton = mock(Button.class);
		mockedTab.testMethodButton = testMethodButton;
		
		mockedTab.handleClassButtonSelected();
		
		verify(mockedTab, never()).resetSelectedTestClass();
		verify(mockedTab, never()).resetSelectedTestMethod();
		verify(mockedTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		verify(testMethodButton, never()).setEnabled(any(Boolean.class));
	}
	
	@Test
	public void testHandleClassButtonNullClassTextWidget() {
		doCallRealMethod().when(mockedTab).handleClassButtonSelected();
		
		when(mockedTab.chooseTestClass()).thenReturn("MyClass");
		
		Button testMethodButton = mock(Button.class);
		mockedTab.testMethodButton = testMethodButton;
		
		mockedTab.handleClassButtonSelected();
		
		verify(mockedTab, never()).resetSelectedTestClass();
		verify(mockedTab, never()).resetSelectedTestMethod();
		verify(mockedTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
		verify(testMethodButton, never()).setEnabled(any(Boolean.class));
	}
	
	@Test
	public void testHandleClassButtonDifferentClass() {
		doCallRealMethod().when(mockedTab).handleClassButtonSelected();
		
		String newClass = "NewClass";
		when(mockedTab.chooseTestClass()).thenReturn(newClass);
		
		String oldClass = "oldClass";
		Text classText = mock(Text.class);
		when(classText.getText()).thenReturn(oldClass);
		mockedTab.classText = classText;
		
		doNothing().when(mockedTab).resetSelectedTestMethod();
		
		Button testMethodButton = mock(Button.class);
		doNothing().when(testMethodButton).setEnabled(true);
		mockedTab.testMethodButton = testMethodButton;
		
		Color normalBlack = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		mockedTab.normalBlack = normalBlack;
		
		when(mockedTab.setTextProperties(classText, newClass, normalBlack)).thenReturn(classText);
		when(mockedTab.shouldEnableBasedOnText(any(String.class))).thenReturn(true);
		
		mockedTab.handleClassButtonSelected();
		
		verify(mockedTab, never()).resetSelectedTestClass();
		verify(mockedTab, times(1)).resetSelectedTestMethod();
		verify(mockedTab, times(1)).setTextProperties(classText, newClass, normalBlack);
		verify(testMethodButton, times(1)).setEnabled(any(Boolean.class));
	}
	
	@Test
	public void testHandleClassButtonSameClass() {
		doCallRealMethod().when(mockedTab).handleClassButtonSelected();
		
		String newClass = "NewClass";
		when(mockedTab.chooseTestClass()).thenReturn(newClass);
		
		String oldClass = "NewClass";
		Text classText = mock(Text.class);
		when(classText.getText()).thenReturn(oldClass);
		mockedTab.classText = classText;
		
		doNothing().when(mockedTab).resetSelectedTestMethod();
		
		Button testMethodButton = mock(Button.class);
		doNothing().when(testMethodButton).setEnabled(true);
		mockedTab.testMethodButton = testMethodButton;
		
		Color normalBlack = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		mockedTab.normalBlack = normalBlack;
		
		when(mockedTab.setTextProperties(classText, newClass, normalBlack)).thenReturn(classText);
		when(mockedTab.shouldEnableBasedOnText(any(String.class))).thenReturn(true);
		
		mockedTab.handleClassButtonSelected();
		
		verify(mockedTab, never()).resetSelectedTestClass();
		verify(mockedTab, never()).resetSelectedTestMethod();
		verify(mockedTab, times(1)).setTextProperties(classText, newClass, normalBlack);
		verify(testMethodButton, times(1)).setEnabled(any(Boolean.class));
	}
	
	@Test
	public void testHandleTestMethodButtonNullMethod() {
		doCallRealMethod().when(mockedTab).handleTestMethodButtonSelected();
		when(mockedTab.chooseTestMethod()).thenReturn(null);
		
		mockedTab.handleTestMethodButtonSelected();
		
		verify(mockedTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
	}

	@Test
	public void testHandleTestMethodButtonNullMethodTextWidget() {
		doCallRealMethod().when(mockedTab).handleTestMethodButtonSelected();
		
		String methodName = "MyMethod";
		when(mockedTab.chooseTestMethod()).thenReturn(methodName);
		
		mockedTab.handleTestMethodButtonSelected();
		
		verify(mockedTab, never()).setTextProperties(any(Text.class), any(String.class), any(Color.class));
	}
	
	@Test
	public void testHandleTestMethodButtonSelected() {
		doCallRealMethod().when(mockedTab).handleTestMethodButtonSelected();
		
		String methodName = "MyMethod";
		when(mockedTab.chooseTestMethod()).thenReturn(methodName);
		
		Text testMethodText = mock(Text.class);
		mockedTab.testMethodText = testMethodText;
		
		Color normalBlack = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		mockedTab.normalBlack = normalBlack;
		
		when(mockedTab.setTextProperties(testMethodText, methodName, normalBlack)).thenReturn(testMethodText);
		
		mockedTab.handleTestMethodButtonSelected();
		
		verify(mockedTab, times(1)).setTextProperties(testMethodText, methodName, normalBlack);
	}
	
	@Test
	public void testSelectAsyncNullAsyncButtonWidget() {
		doCallRealMethod().when(mockedTab).selectAsync(any(Boolean.class));
		
		Button testSyncButton = mock(Button.class);
		mockedTab.testSyncButton = testSyncButton;
		
		mockedTab.selectAsync(true);
		
		verify(testSyncButton, never()).setEnabled(any(Boolean.class));
		verify(testSyncButton, never()).setSelection(any(Boolean.class));
	}
	
	@Test
	public void testSelectAsyncNullSyncButtonWidget() {
		doCallRealMethod().when(mockedTab).selectAsync(any(Boolean.class));
		
		Button testAsyncButton = mock(Button.class);
		mockedTab.testAsyncButton = testAsyncButton;
		
		mockedTab.selectAsync(true);
		
		verify(testAsyncButton, never()).setEnabled(any(Boolean.class));
		verify(testAsyncButton, never()).setSelection(any(Boolean.class));
	}
	
	@Test
	public void testSelectAsyncTrue() {
		doCallRealMethod().when(mockedTab).selectAsync(any(Boolean.class));
		
		Button testSyncButton = mock(Button.class);
		mockedTab.testSyncButton = testSyncButton;
		Button testAsyncButton = mock(Button.class);
		mockedTab.testAsyncButton = testAsyncButton;
		
		boolean isAsync = true;
		mockedTab.selectAsync(isAsync);
		
		verify(testAsyncButton, times(1)).setEnabled(true);
		verify(testAsyncButton, times(1)).setSelection(isAsync);
		verify(testSyncButton, times(1)).setEnabled(true);
		verify(testSyncButton, times(1)).setSelection(!isAsync);
	}
	
	@Test
	public void testSelectAsyncFalse() {
		doCallRealMethod().when(mockedTab).selectAsync(any(Boolean.class));
		
		Button testSyncButton = mock(Button.class);
		mockedTab.testSyncButton = testSyncButton;
		Button testAsyncButton = mock(Button.class);
		mockedTab.testAsyncButton = testAsyncButton;
		
		boolean isAsync = false;
		mockedTab.selectAsync(isAsync);
		
		verify(testAsyncButton, times(1)).setEnabled(true);
		verify(testAsyncButton, times(1)).setSelection(isAsync);
		verify(testSyncButton, times(1)).setEnabled(true);
		verify(testSyncButton, times(1)).setSelection(!isAsync);
	}
	
	@Test
	public void testIsValidWithNoErrors() {
		doCallRealMethod().when(mockedTab).isValid(any(ILaunchConfiguration.class));
		when(mockedTab.validatePage()).thenReturn(false);
		when(mockedTab.getErrorMessage()).thenReturn(null);
		
		assertTrue(mockedTab.isValid(mock(ILaunchConfiguration.class)));
	}
	
	@Test
	public void testIsValidWithEmptyErrorMessage() {
		doCallRealMethod().when(mockedTab).isValid(any(ILaunchConfiguration.class));
		when(mockedTab.validatePage()).thenReturn(false);
		when(mockedTab.getErrorMessage()).thenReturn("");
		
		assertFalse(mockedTab.isValid(mock(ILaunchConfiguration.class)));
	}
	
	@Test
	public void testIsValidWithRealErrorMessage() {
		doCallRealMethod().when(mockedTab).isValid(any(ILaunchConfiguration.class));
		when(mockedTab.validatePage()).thenReturn(false);
		when(mockedTab.getErrorMessage()).thenReturn("OhNo");
		
		assertFalse(mockedTab.isValid(mock(ILaunchConfiguration.class)));
	}
	
	@Test
	public void testValidatePageProjectValidTestModeValid() {
		genericValidatePageTest(true, true, true);
	}
	
	@Test
	public void testValidatePageProjectValidTestModeInvalid() {
		genericValidatePageTest(true, false, false);
	}
	
	@Test
	public void testValidatePageProjectInvalidTestModeValid() {
		genericValidatePageTest(false, true, false);
	}
	
	@Test
	public void testValidatePageProjectInvalidTestModeInvalid() {
		genericValidatePageTest(false, false, false);
	}
	
	private void genericValidatePageTest(boolean isProjectValid, boolean isTestModeValid, boolean validateResult) {
		doCallRealMethod().when(mockedTab).validatePage();
		doNothing().when(mockedTab).setErrorMessage(any(String.class));
		when(mockedTab.validateProjectSelection()).thenReturn(isProjectValid);
		when(mockedTab.validateTestModeSelection()).thenReturn(isTestModeValid);
		
		assertEquals(validateResult, mockedTab.validatePage());
		
		verify(mockedTab, times(1)).setErrorMessage(null);
		verify(mockedTab, times(1)).validateProjectSelection();
		verify(mockedTab, isProjectValid ? times(1) : never()).validateTestModeSelection();
	}
	
	@Test
	public void testValidateProjectSelectionNullProjectName() {
		doCallRealMethod().when(mockedTab).validateProjectSelection();
		doNothing().when(mockedTab).setErrorMessage(any(String.class));
		when(mockedTab.getProjectName()).thenReturn(null);
		
		assertFalse(mockedTab.validateProjectSelection());
		
		verify(mockedTab, times(1)).setErrorMessage(Messages.GenericTab_EmptyProjectErrorMessage);
	}
	
	@Test
	public void testValidateProjectSelectionEmptyProjectName() {
		doCallRealMethod().when(mockedTab).validateProjectSelection();
		doNothing().when(mockedTab).setErrorMessage(any(String.class));
		when(mockedTab.getProjectName()).thenReturn("");
		
		assertFalse(mockedTab.validateProjectSelection());
		
		verify(mockedTab, times(1)).setErrorMessage(Messages.GenericTab_EmptyProjectErrorMessage);
	}
	
	@Test
	public void testValidateProjectSelectionNullProject() {
		doCallRealMethod().when(mockedTab).validateProjectSelection();
		doNothing().when(mockedTab).setErrorMessage(any(String.class));
		when(mockedTab.getProjectName()).thenReturn("MyProject");
		when(mockedTab.getProjectFromName()).thenReturn(null);
		
		assertFalse(mockedTab.validateProjectSelection());
		
		verify(mockedTab, times(1)).setErrorMessage(Messages.GenericTab_NonExistingProjectErrorMessage);
	}
	
	@Test
	public void testValidateProjectSelectionProjectWithoutCorrectNatureId() throws Exception {
		doCallRealMethod().when(mockedTab).validateProjectSelection();
		doNothing().when(mockedTab).setErrorMessage(any(String.class));
		when(mockedTab.getProjectName()).thenReturn("MyProject");
		
		IProject project = mock(IProject.class);
		when(project.hasNature(DefaultNature.NATURE_ID)).thenReturn(false);
		when(mockedTab.getProjectFromName()).thenReturn(project);
		
		assertFalse(mockedTab.validateProjectSelection());
		
		verify(mockedTab, times(1)).setErrorMessage(Messages.GenericTab_InvalidForceProjectErrorMessage);
	}
	
	@Test
	public void testValidateProjectSelection() throws Exception {
		doCallRealMethod().when(mockedTab).validateProjectSelection();
		doNothing().when(mockedTab).setErrorMessage(any(String.class));
		when(mockedTab.getProjectName()).thenReturn("MyProject");
		
		IProject project = mock(IProject.class);
		when(project.hasNature(DefaultNature.NATURE_ID)).thenReturn(true);
		when(mockedTab.getProjectFromName()).thenReturn(project);
		
		assertTrue(mockedTab.validateProjectSelection());
		verify(mockedTab, never()).setErrorMessage(any(String.class));
	}
	
	@Test
	public void testGetProjectFromNullName() {
		doCallRealMethod().when(mockedTab).getProjectFromName();
		when(mockedTab.getProjectName()).thenReturn(null);
		
		assertNull(mockedTab.getProjectFromName());
	}
	
	@Test
	public void testPerformApply() {
		doCallRealMethod().when(mockedTab).performApply(any(ILaunchConfigurationWorkingCopy.class));
		ILaunchConfigurationWorkingCopy config = mock(ILaunchConfigurationWorkingCopy.class);
		doNothing().when(config).setAttribute(any(String.class), any(String.class));
		
		when(mockedTab.getProjectName()).thenReturn("MyProject");
		when(mockedTab.getTestClassName()).thenReturn("MyClass");
		when(mockedTab.getTestMethodName()).thenReturn("MyMethod");
		IProject project = mock(IProject.class);
		when(mockedTab.getProjectFromName()).thenReturn(project);
		when(mockedTab.buildTestsForConfig(project)).thenReturn(mock(RunTests.class));
		when(mockedTab.convertTestsToJson(any(RunTests.class))).thenReturn("testsGalore");
		when(mockedTab.countTotalTests(any(RunTests.class))).thenReturn(5);
		when(mockedTab.isTestModeAsync()).thenReturn(true);
		
		mockedTab.performApply(config);
		
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, "MyProject");
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_CLASS, "MyClass");
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_METHOD, "MyMethod");
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_FORCECOM_TESTS_ARRAY, "testsGalore");
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_FORCECOM_TESTS_TOTAL, 5);
		verify(config, times(1)).setAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, true);
	}
	
	@Test
	public void testInitializeFromNullProject() throws Exception {
		doCallRealMethod().when(mockedTab).initializeFrom(any(ILaunchConfiguration.class));
		ILaunchConfiguration config = mock(ILaunchConfiguration.class);
		
		String projectName = "MyProject";
		String className = "MyClass";
		String methodName = "MyMethod";
		boolean isAsync = false;
		
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, "")).thenReturn(projectName);
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_CLASS, "")).thenReturn(className);
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_METHOD, "")).thenReturn(methodName);
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, true)).thenReturn(isAsync);
		
		Button projectButton = mock(Button.class);
		doNothing().when(projectButton).setEnabled(any(Boolean.class));
		mockedTab.projectButton = projectButton;
		Button classButton = mock(Button.class);
		doNothing().when(classButton).setEnabled(any(Boolean.class));
		mockedTab.classButton = projectButton;
		Button testMethodButton = mock(Button.class);
		doNothing().when(testMethodButton).setEnabled(any(Boolean.class));
		mockedTab.testMethodButton = projectButton;
		
		Text projectText = mock(Text.class);
		when(projectText.getText()).thenReturn("");
		mockedTab.projectText = projectText;
		Text classText = mock(Text.class);
		when(classText.getText()).thenReturn("");
		mockedTab.classText = classText;
		
		Color normalBlack = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		mockedTab.normalBlack = normalBlack;
		
		when(mockedTab.getProjectFromName()).thenReturn(null);
		when(mockedTab.shouldEnableBasedOnText(any(String.class))).thenReturn(false);
		when(mockedTab.setTextProperties(any(Text.class), any(String.class), any(Color.class))).thenReturn(null);
		doNothing().when(mockedTab).selectAsync(isAsync);
		
		mockedTab.initializeFrom(config);
		
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, "");
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_CLASS, "");
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_METHOD, "");
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, true);
		verify(mockedTab, times(1)).setTextProperties(any(Text.class), eq(projectName), eq(normalBlack));
		verify(mockedTab, times(1)).setTextProperties(any(Text.class), eq(className), eq(normalBlack));
		verify(mockedTab, times(1)).setTextProperties(any(Text.class), eq(methodName), eq(normalBlack));
		verify(mockedTab, times(1)).selectAsync(isAsync);
	}
	
	@Test
	public void testInitializeFromExistingProject() throws Exception {
		doCallRealMethod().when(mockedTab).initializeFrom(any(ILaunchConfiguration.class));
		ILaunchConfiguration config = mock(ILaunchConfiguration.class);
		
		String projectName = "MyProject";
		String className = "MyClass";
		String methodName = "MyMethod";
		boolean isAsync = false;
		
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, "")).thenReturn(projectName);
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_CLASS, "")).thenReturn(className);
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_METHOD, "")).thenReturn(methodName);
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, true)).thenReturn(isAsync);
		
		Button projectButton = mock(Button.class);
		doNothing().when(projectButton).setEnabled(any(Boolean.class));
		mockedTab.projectButton = projectButton;
		Button classButton = mock(Button.class);
		doNothing().when(classButton).setEnabled(any(Boolean.class));
		mockedTab.classButton = projectButton;
		Button testMethodButton = mock(Button.class);
		doNothing().when(testMethodButton).setEnabled(any(Boolean.class));
		mockedTab.testMethodButton = projectButton;
		
		Text projectText = mock(Text.class);
		when(projectText.getText()).thenReturn("");
		mockedTab.projectText = projectText;
		Text classText = mock(Text.class);
		when(classText.getText()).thenReturn("");
		mockedTab.classText = classText;
		
		Color normalBlack = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		mockedTab.normalBlack = normalBlack;
		
		IProject project = mock(IProject.class);
		when(mockedTab.getProjectFromName()).thenReturn(project);
		Map<IProject, RunTests> allTests = new HashMap<IProject, RunTests>();
		allTests.put(project, mock(RunTests.class));
		mockedTab.allTests = allTests;
		
		when(mockedTab.shouldEnableBasedOnText(any(String.class))).thenReturn(false);
		when(mockedTab.setTextProperties(any(Text.class), any(String.class), any(Color.class))).thenReturn(null);
		doNothing().when(mockedTab).selectAsync(isAsync);
		
		mockedTab.initializeFrom(config);
		
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, "");
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_CLASS, "");
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_METHOD, "");
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, true);
		verify(mockedTab, times(1)).setTextProperties(any(Text.class), eq(projectName), eq(normalBlack));
		verify(mockedTab, times(1)).setTextProperties(any(Text.class), eq(className), eq(normalBlack));
		verify(mockedTab, times(1)).setTextProperties(any(Text.class), eq(methodName), eq(normalBlack));
		verify(mockedTab, times(1)).selectAsync(isAsync);
		verify(mockedTab, never()).buildTestsForProject(any(IProject.class));
		assertEquals(1, allTests.size());
	}
	
	@Test
	public void testInitializeFromNewProject() throws Exception {
		doCallRealMethod().when(mockedTab).initializeFrom(any(ILaunchConfiguration.class));
		ILaunchConfiguration config = mock(ILaunchConfiguration.class);
		
		String projectName = "MyProject";
		String className = "MyClass";
		String methodName = "MyMethod";
		boolean isAsync = false;
		
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, "")).thenReturn(projectName);
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_CLASS, "")).thenReturn(className);
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_METHOD, "")).thenReturn(methodName);
		when(config.getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, true)).thenReturn(isAsync);
		
		Button projectButton = mock(Button.class);
		doNothing().when(projectButton).setEnabled(any(Boolean.class));
		mockedTab.projectButton = projectButton;
		Button classButton = mock(Button.class);
		doNothing().when(classButton).setEnabled(any(Boolean.class));
		mockedTab.classButton = projectButton;
		Button testMethodButton = mock(Button.class);
		doNothing().when(testMethodButton).setEnabled(any(Boolean.class));
		mockedTab.testMethodButton = projectButton;
		
		Text projectText = mock(Text.class);
		when(projectText.getText()).thenReturn("");
		mockedTab.projectText = projectText;
		Text classText = mock(Text.class);
		when(classText.getText()).thenReturn("");
		mockedTab.classText = classText;
		
		Color normalBlack = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		mockedTab.normalBlack = normalBlack;
		
		IProject project = mock(IProject.class);
		when(mockedTab.getProjectFromName()).thenReturn(project);
		Map<IProject, RunTests> allTests = new HashMap<IProject, RunTests>();
		mockedTab.allTests = allTests;
		RunTests rt = mock(RunTests.class);
		when(mockedTab.buildTestsForProject(project)).thenReturn(rt);
		
		when(mockedTab.shouldEnableBasedOnText(any(String.class))).thenReturn(false);
		when(mockedTab.setTextProperties(any(Text.class), any(String.class), any(Color.class))).thenReturn(null);
		doNothing().when(mockedTab).selectAsync(isAsync);
		
		mockedTab.initializeFrom(config);
		
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_PROJECT_NAME, "");
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_CLASS, "");
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_METHOD, "");
		verify(config, times(1)).getAttribute(RunTestsConstants.ATTR_FORCECOM_TEST_MODE, true);
		verify(mockedTab, times(1)).setTextProperties(any(Text.class), eq(projectName), eq(normalBlack));
		verify(mockedTab, times(1)).setTextProperties(any(Text.class), eq(className), eq(normalBlack));
		verify(mockedTab, times(1)).setTextProperties(any(Text.class), eq(methodName), eq(normalBlack));
		verify(mockedTab, times(1)).selectAsync(isAsync);
		verify(mockedTab, times(1)).buildTestsForProject(project);
		assertEquals(1, allTests.size());
		assertEquals(rt, mockedTab.currentSelectedTests);
	}
}
