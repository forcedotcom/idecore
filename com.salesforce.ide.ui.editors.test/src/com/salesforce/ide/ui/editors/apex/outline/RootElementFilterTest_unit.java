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
package com.salesforce.ide.ui.editors.apex.outline;

import com.salesforce.ide.apex.core.utils.ParserTestUtil;

import junit.framework.TestCase;
import apex.jorje.data.ast.ClassDecl;
import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.data.ast.EnumDecl;
import apex.jorje.data.ast.InterfaceDecl;

/**
 * This tests that we will display the proper root elements in the outline view. Almost anything that is a subtype of
 * CompilationUnit is a valid root element except AnonymousBlock since that is not allowed in file-based metadata.
 * 
 * @author nchen
 * 
 */
public class RootElementFilterTest_unit extends TestCase {

    private RootElementFilter filter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filter = new RootElementFilter();
    }

    public void testTriggerRecognized() throws Exception {
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromFile("/filemetadata/outline/classes/TriggerOnly.trigger");
        cu._switch(filter);
        Object[] rootElements = filter.getRootElements();
        assertEquals("TriggerOnly", ((TriggerDeclUnit) rootElements[0]).name.value);
    }

    public void testEnumRecognized() throws Exception {
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromFile("/filemetadata/outline/classes/EnumOnly.cls");
        cu._switch(filter);
        Object[] rootElements = filter.getRootElements();
        assertEquals("EnumOnly", ((EnumDecl) rootElements[0]).name.value);
    }

    public void testClassRecognized() throws Exception {
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromFile("/filemetadata/outline/classes/ClassOnly.cls");
        cu._switch(filter);
        Object[] rootElements = filter.getRootElements();
        assertEquals("ClassOnly", ((ClassDecl) rootElements[0]).name.value);
    }

    public void testInterfaceRecognized() throws Exception {
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromFile("/filemetadata/outline/classes/InterfaceOnly.cls");
        cu._switch(filter);
        Object[] rootElements = filter.getRootElements();
        assertEquals("InterfaceOnly", ((InterfaceDecl) rootElements[0]).name.value);
    }

    public void testAnonymousBlocksIgnored() throws Exception {
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromFile("/filemetadata/outline/classes/AnonymousBlockOnly.cls");
        assertNull(cu);
    }
}
