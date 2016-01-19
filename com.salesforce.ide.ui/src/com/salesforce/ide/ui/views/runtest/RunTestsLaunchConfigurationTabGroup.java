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

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * A tab group for Apex Test launch configuration
 * 
 * @author jwidjaja
 *
 */
public class RunTestsLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ProjectConfigurationTab projectTab = new ProjectConfigurationTab();
		TestConfigurationTab testTab = new TestConfigurationTab();
		
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] { 
				projectTab,
				testTab,
				new CommonTab() };
		
        setTabs(tabs);
        
        projectTab.saveSiblingTab(testTab);
        testTab.saveSiblingTab(projectTab);
	}
}
