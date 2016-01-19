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

import static org.mockito.Mockito.mock;

import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.junit.Test;

import com.salesforce.ide.test.common.IdeSetupTest;
import com.salesforce.ide.test.common.IdeTestCase;
import com.salesforce.ide.ui.views.runtest.ProjectConfigurationTab;
import com.salesforce.ide.ui.views.runtest.RunTestsLaunchConfigurationTabGroup;
import com.salesforce.ide.ui.views.runtest.TestConfigurationTab;

@IdeSetupTest(needOrg = false, needProject = false)
public class RunTestsLaunchConfigurationTabGroupTest_pdeui extends IdeTestCase {

	@Test
	public void testCreateTabs() {
		RunTestsLaunchConfigurationTabGroup tabGroup = new RunTestsLaunchConfigurationTabGroup();
		ILaunchConfigurationDialog mockDialog = mock(ILaunchConfigurationDialog.class);
		String mockMode = "";
		
		tabGroup.createTabs(mockDialog, mockMode);
		
		ILaunchConfigurationTab[] tabs = tabGroup.getTabs();
		assertNotNull(tabs);
		assertEquals(3, tabs.length);
		assertTrue(tabs[0] instanceof ProjectConfigurationTab);
		assertTrue(tabs[1] instanceof TestConfigurationTab);
		assertTrue(tabs[2] instanceof CommonTab);
		
		ProjectConfigurationTab projectTab = (ProjectConfigurationTab) tabs[0];
		assertNotNull(projectTab.getSiblingTab());
		
		TestConfigurationTab testTab = (TestConfigurationTab) tabs[1];
		assertNotNull(testTab.getSiblingTab());
	}
}
