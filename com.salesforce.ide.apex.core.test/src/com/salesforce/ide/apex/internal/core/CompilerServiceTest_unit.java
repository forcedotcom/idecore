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
package com.salesforce.ide.apex.internal.core;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * @author nchen
 * 
 */
public class CompilerServiceTest_unit extends TestCase {
    @Test
    public void testCanonicalizeNormal() {
        String input = "public class Normal {\r\n}";
        String expected = "public class Normal {\n}";

        String actual = CompilerService.canonicalizeString(input);
        
        assertEquals(expected, actual);
    }

    /**
     * For lack of a better name, it's the \r\r\n case where it can be either (\r)(\r\n) or (\r)(\r)(\n)
     */
    @Test
    public void testCanonicalizeAmbiguousCase1() {
        String input = "public class Normal {\r\r\n}";
        String expected = "public class Normal {\n\n}";

        String actual = CompilerService.canonicalizeString(input);
        
        assertEquals(expected, actual);
    }

    /**
     * For lack of a better name, it's the \n\r\n case where it can be either (\n)(\r\n) or (\n)(\r)(\n)
     */
    @Test
    public void testCanonicalizeAmbiguousCase2() {
        String input = "public class Normal {\n\r\n}";
        String expected = "public class Normal {\n\n}";

        String actual = CompilerService.canonicalizeString(input);
        
        assertEquals(expected, actual);
    }
}
