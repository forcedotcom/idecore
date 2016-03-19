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
package com.salesforce.ide.apex.ui;

import java.util.Map;
import org.junit.Test;

import com.salesforce.ide.apex.core.utils.ParserTestUtil;
import com.salesforce.ide.apex.internal.core.CompilerService;
import com.salesforce.ide.apex.visitors.OpenTypeVisitor;
import junit.framework.TestCase;

public class OpenTypeVisitorTest_unit extends TestCase {
	OpenTypeVisitor visitor = new OpenTypeVisitor();
	
	@Test
    public void testKitchenSink() throws Exception {
		String fileContents = ParserTestUtil.readFromFile("/filemetadata/apex_debugger/classes/KitchenSink.cls");
		CompilerService.INSTANCE.visitAstFromString(fileContents, visitor);
        Map<String, Integer> lineNumberMapping = visitor.getNumberLineMapping();
        
        // Assertions for the line numbers and what they map to
        assertEquals(10, lineNumberMapping.size());
        assertLineNumberMapping("KitchenSink", lineNumberMapping, 4);
        assertLineNumberMapping("MyInterface", lineNumberMapping, 34);
        assertLineNumberMapping("MySecondInterface", lineNumberMapping, 42);
        assertLineNumberMapping("InnerClass", lineNumberMapping, 49);
        assertLineNumberMapping("AbstractChildClass", lineNumberMapping, 85);
        assertLineNumberMapping("ConcreteChildClass", lineNumberMapping, 101);
        assertLineNumberMapping("AnotherChildClass", lineNumberMapping, 108);
        assertLineNumberMapping("MyException", lineNumberMapping, 116);
        assertLineNumberMapping("MySecondException", lineNumberMapping, 130);
        assertLineNumberMapping("NumericEnum", lineNumberMapping, 151);
    }
	
	@Test
    public void testNestedTrigger() throws Exception {
		String fileContents = ParserTestUtil.readFromFile("/filemetadata/apex_debugger/triggers/NestedTrigger.trigger");
		CompilerService.INSTANCE.visitAstFromString(fileContents, visitor);
        Map<String, Integer> lineNumberMapping = visitor.getNumberLineMapping();
        
        // Assertions for the line numbers and what they map to
        assertEquals(3, lineNumberMapping.size());
        assertLineNumberMapping("NestedTrigger", lineNumberMapping, 1);
        assertLineNumberMapping("InnerNestedClass", lineNumberMapping, 5);
        assertLineNumberMapping("MyTrgException", lineNumberMapping, 13);
    }
	
    private void assertLineNumberMapping(String className, Map<String, Integer> lineNumberMapping, Integer validLine) {
    	assertEquals(String.format("Could not find line %d", validLine), validLine, lineNumberMapping.get(className));
    }
}
