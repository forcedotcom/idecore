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

import static org.mockito.Mockito.mock;
import junit.framework.TestCase;

import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.junit.Test;

public class RunTestsLaunchConfigurationTabGroupTest_unit extends TestCase {

	@Test
	public void testCreateTabs() {
		RunTestsLaunchConfigurationTabGroup tabGroup = new RunTestsLaunchConfigurationTabGroup();
		ILaunchConfigurationDialog mockDialog = mock(ILaunchConfigurationDialog.class);
		String mockMode = "";
		
		tabGroup.createTabs(mockDialog, mockMode);
		
		ILaunchConfigurationTab[] tabs = tabGroup.getTabs();
		assertNotNull(tabs);
		assertEquals(2, tabs.length);
		assertTrue(tabs[0] instanceof RunTestsLaunchConfigurationTab);
		assertTrue(tabs[1] instanceof CommonTab);
	}
}
