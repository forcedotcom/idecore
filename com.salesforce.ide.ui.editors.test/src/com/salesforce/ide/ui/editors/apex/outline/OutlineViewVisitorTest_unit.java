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
package com.salesforce.ide.ui.editors.apex.outline;

import java.io.IOException;

import com.salesforce.ide.apex.core.utils.ParserTestUtil;
import com.salesforce.ide.apex.internal.core.CompilerService;
import com.salesforce.ide.ui.editors.apex.outline.text.OutlineViewElementTextProvider.TypeInfoPrinter;

import junit.framework.TestCase;

/**
 * This tests that we will display the proper root elements in the outline view.
 * 
 * @author nchen
 *         
 */
public class OutlineViewVisitorTest_unit extends TestCase {
    private OutlineViewVisitor visitSource(String path) throws IOException {
        String fileContents = ParserTestUtil.readFromFile(path);
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(fileContents, visitor);
        return visitor;
    }
    
    public void testTriggerRecognized() throws Exception {
        OutlineViewVisitor visitor = visitSource("/filemetadata/outline/classes/TriggerOnly.trigger");
        assertEquals("TriggerOnly", TypeInfoPrinter.print(visitor.getTopLevel().getDefiningType()));
    }
    
    public void testEnumRecognized() throws Exception {
        OutlineViewVisitor visitor = visitSource("/filemetadata/outline/classes/EnumOnly.cls");
        assertEquals("EnumOnly", TypeInfoPrinter.print(visitor.getTopLevel().getDefiningType()));
    }
    
    public void testClassRecognized() throws Exception {
        OutlineViewVisitor visitor = visitSource("/filemetadata/outline/classes/ClassOnly.cls");
        assertEquals("ClassOnly", TypeInfoPrinter.print(visitor.getTopLevel().getDefiningType()));
    }
    
    public void testInterfaceRecognized() throws Exception {
        OutlineViewVisitor visitor = visitSource("/filemetadata/outline/classes/InterfaceOnly.cls");
        assertEquals("InterfaceOnly", TypeInfoPrinter.print(visitor.getTopLevel().getDefiningType()));
    }
    
    public void testAnonymousBlocksIgnored() throws Exception {
        OutlineViewVisitor visitor = visitSource("/filemetadata/outline/classes/AnonymousBlockOnly.cls");
        assertNull(visitor.getTopLevel());
    }
}
