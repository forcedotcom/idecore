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

import junit.framework.TestCase;

public class ApexMethodTest_unit extends TestCase {
	public void testEquals() throws Exception {
		ApexMethod left = new ApexMethod("foo", "bar", "hello", false, "doc", new ApexArgument("a", "b", "c"));
		ApexMethod right = new ApexMethod("foo", "bar", "hello", false, "doc", new ApexArgument("a", "b", "c"));
		assertEquals(left, right);
		assertEquals(left.hashCode(), right.hashCode());
	}

	public void testMethodOverload() throws Exception {
		ApexMethod left = new ApexMethod("foo", "bar", "hello", false, "doc", new ApexArgument("a", "b", "c"));
		ApexMethod right = new ApexMethod("foo", "bar", "hello", false, "doc", new ApexArgument("a", "b", "c"), new ApexArgument("b", "c", "d"));
		assertFalse(left.equals(right));
		assertFalse(left.hashCode() == right.hashCode());
	}
	
	public void testMethodName() throws Exception {
		ApexMethod left = new ApexMethod("foo", "bar", "hello", false, "doc", new ApexArgument("a", "b", "c"));
		ApexMethod right = new ApexMethod("panda", "bar", "hello", false, "doc", new ApexArgument("a", "b", "c"));
		assertFalse(left.equals(right));
		assertFalse(left.hashCode() == right.hashCode());
	}
}
