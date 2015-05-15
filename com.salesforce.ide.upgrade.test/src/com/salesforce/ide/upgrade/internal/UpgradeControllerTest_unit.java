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
package com.salesforce.ide.upgrade.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.upgrade.internal.UpgradeController;

public class UpgradeControllerTest_unit extends TestCase {		
	private UpgradeController upgradeController;
	
	public void testIsIncludedComponent_excludesUnfiledPublicFolder() throws Exception {		
		Component component = mock(Component.class);
		when(component.getName()).thenReturn("foo");
		assertTrue(upgradeController.isIncludedComponent(component));
		when(component.getName()).thenReturn(Constants.UNFILED_PUBLIC_FOLDER_NAME);
		assertFalse(upgradeController.isIncludedComponent(component));
	}
	
	public void testisExcludedFileExtensionSurvivesNPE() throws Exception {
		Component component = mock(Component.class);
		when(component.getFileName()).thenReturn("panda");
		upgradeController.setExcludeFileExtensions(Lists.newArrayList("foo"));
		assertFalse(upgradeController.isExcludedFileExtension(component));
	}
	
	public void testUpgradableComponentsContents() throws Exception {
		ComponentList upgradeableComponentList = upgradeController.getUpgradeableComponentList();
		Set<String> upgradableSet = new HashSet<String>(upgradeableComponentList.getComponentTypes());
		
		Set<String> expectedComponentNames = Sets.newHashSet(
				Constants.APEX_CLASS,
				Constants.APEX_COMPONENT,
				Constants.APEX_PAGE,
				Constants.APEX_TRIGGER,
				Constants.CUSTOM_OBJECT,
				Constants.CUSTOM_OBJECT_TRANSLATION,
				Constants.CUSTOM_SITE,
				Constants.CUSTOM_TAB,
				Constants.DASHBOARD,
				Constants.DOCUMENT,
				Constants.DATACATEGORYGROUP,
				Constants.EMAIL_TEMPLATE,				
				Constants.LAYOUT,
				Constants.PROFILE,
				Constants.REPORT,
				Constants.WORKFLOW
		);
		

		for (String component: upgradeableComponentList.getComponentTypes()) {
			System.out.println(component);
		}
		
		
		assertEquals(expectedComponentNames, upgradableSet);

		
        Set<String> actualExcludedFileExtensions = Sets.newHashSet(upgradeController.getExcludeFileExtensions());
        Set<String> expectedExcludedExtensions = Sets.newHashSet(
        		"cls",
        		"component",
        		"page",
        		"email",
        		"trigger"
        );
        
        assertEquals(expectedExcludedExtensions, actualExcludedFileExtensions);
	}

	public void testAddConflictSkipsOverNullComponents(){
	    Map<String, List<UpgradeConflict>> upgradeConflicts = new HashMap<String, List<UpgradeConflict>>();
        upgradeController.addConflict(upgradeConflicts , null, null);
        assertTrue(upgradeConflicts.isEmpty());
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		upgradeController = new UpgradeController();
	}
}
