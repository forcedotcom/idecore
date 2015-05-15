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
package com.salesforce.ide.ui.editors.apex.assistance;

import java.util.Collections;

import junit.framework.TestCase;

public class ApexObjectTest_unit extends TestCase {
	@SuppressWarnings("unchecked")
	public void testMethodsAreUnique() throws Exception {
		ApexObject apexObject = new ApexObject("foo", Collections.EMPTY_LIST);
		ApexMethod someMethod = new ApexMethod("a", "b", "c", false, "");
		ApexMethod someOtherMethod = new ApexMethod("panda", "b", "c", false, "");
		
		assertEquals(0, apexObject.getMethods().size());
		
		apexObject.addMethod(someMethod);
		apexObject.addMethod(someMethod);
		assertEquals(1, apexObject.getMethods().size());
		
		apexObject.addMethod(someOtherMethod);
		assertEquals(2, apexObject.getMethods().size());
	}
}
