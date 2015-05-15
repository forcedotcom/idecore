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
package com.salesforce.ide.core.internal.utils;

import junit.framework.TestCase;


/**
 *
 * Testing SoqlEnum methods
 *
 * @author ranekere
 */
public class SoqlEnumTest_unit extends TestCase {

    public void testGetSchemaInitalizationQuery() {
        String expectedInitQuery = "SELECT Id, Name FROM User LIMIT 10" ;
        String actualInitQuery = SoqlEnum.getSchemaInitalizationQuery();
        assertEquals(expectedInitQuery, actualInitQuery);
    }

}
