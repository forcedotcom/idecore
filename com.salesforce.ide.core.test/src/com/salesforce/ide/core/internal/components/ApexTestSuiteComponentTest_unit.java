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
package com.salesforce.ide.core.internal.components;

import org.junit.Test;

import com.salesforce.ide.core.internal.components.apex.test.ApexTestSuiteComponentController;
import com.salesforce.ide.core.internal.components.apex.test.ApexTestSuiteModel;
import com.salesforce.ide.core.internal.utils.Constants;

import junit.framework.TestCase;

public class ApexTestSuiteComponentTest_unit extends TestCase {

	@Test
	public void testATSCtrlExtendsComponentCtrl() throws Exception {
		assertEquals(ComponentController.class, ApexTestSuiteComponentController.class.getSuperclass());
	}
	
	@Test
	public void testATSCtrlUsesATSModel() throws Exception {
		ApexTestSuiteComponentController ctrl = new ApexTestSuiteComponentController();
		assertTrue(ctrl.getModel() instanceof ApexTestSuiteModel);
	}
	
	@Test
	public void testATSModelUsesATSType() throws Exception {
		ApexTestSuiteComponentController ctrl = new ApexTestSuiteComponentController();
		ComponentModel model = (ComponentModel) ctrl.getModel();
		assertEquals(Constants.APEX_TEST_SUITE, model.getComponentType());
	}
}
