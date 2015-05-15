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

public class ApexArgumentTest_unit extends TestCase {
	public void testEquals() throws Exception {
		assertEquals(new ApexArgument("foo", "bar", "doc"), new ApexArgument("foo", "bar", "doc"));
		assertEquals(new ApexArgument("foo", "bar", "doc"), new ApexArgument("foo", "bar", "otherDoc"));
		assertFalse(new ApexArgument("theName", "bar", "hello").equals(new ApexArgument("otherName", "bar", "panda")));
	}
}
