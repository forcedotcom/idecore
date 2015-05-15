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
package com.salesforce.ide.core.model;

import junit.framework.TestCase;

public class ApexCodeLocationTest_unit extends TestCase {

    public void testConstruct_WhenStringInputs() throws Exception {
        final String expectedName = "someName";
        ApexCodeLocation loc = new ApexCodeLocation(expectedName, "someLine", "someColumn");
        assertEquals(expectedName, loc.getName());
        assertEquals(1, loc.getLine().intValue());
        assertEquals(1, loc.getColumn().intValue());
    }
}
