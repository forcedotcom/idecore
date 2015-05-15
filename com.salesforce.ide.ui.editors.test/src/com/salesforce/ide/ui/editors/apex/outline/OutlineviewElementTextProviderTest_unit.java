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

import junit.framework.TestCase;
import apex.jorje.data.Loc;
import apex.jorje.data.ast.BlockMember;
import apex.jorje.data.ast.BlockMember.FieldMember;
import apex.jorje.data.ast.BlockMember.InnerClassMember;
import apex.jorje.data.ast.BlockMember.InnerEnumMember;
import apex.jorje.data.ast.BlockMember.InnerInterfaceMember;
import apex.jorje.data.ast.BlockMember.MethodMember;
import apex.jorje.data.ast.BlockMember.PropertyMember;
import apex.jorje.data.ast.BlockMember.StaticStmntBlockMember;
import apex.jorje.data.ast.BlockMember.StmntBlockMember;
import apex.jorje.data.ast.ClassDecl;
import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.data.ast.CompilationUnit.ClassDeclUnit;
import apex.jorje.data.ast.CompilationUnit.EnumDeclUnit;
import apex.jorje.data.ast.CompilationUnit.InterfaceDeclUnit;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.data.ast.EnumDecl;
import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.InterfaceDecl;
import apex.jorje.parser.impl.ApexParserImpl;

import com.salesforce.ide.apex.core.utils.ParserTestUtil;
import com.salesforce.ide.ui.editors.apex.outline.text.OutlineViewElementTextProvider;

/**
 * This tests the text label functionalities for the outline view. We generate synthetic constructs that are valid and
 * pass them to the text provider. Because our parser is LL, this is <i>easier</i> since we can call any starting
 * non-terminal.
 * 
 * This test is not a test of the robustness of the parser. Thus, some of the contents we test will seem simple (and
 * maybe semantically not valid). This is deliberate. We are testing that given the simplest string representation of a
 * unit, we can still generate a valid textual representation.
 * 
 * @author nchen
 * 
 */
public class OutlineviewElementTextProviderTest_unit extends TestCase {

    private TextProviderHandlerProxy handler;

    public OutlineviewElementTextProviderTest_unit() {
        this.handler = new TextProviderHandlerProxy();
    }

    public void testTriggerDeclUnit() throws Exception {
        String trigger = "trigger testTriggerDeclUnit on Account (before insert) {}";
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromString(trigger);
        handler.handle((TriggerDeclUnit) cu);
    }

    public void testClassDeclUnit() throws Exception {
        String classUnit = "class testClassDeclUnit extends ParentClass implements IInterface {}";
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromString(classUnit);
        handler.handle(((ClassDeclUnit)cu).body);
    }

    public void testInterfaceDeclUnit() throws Exception {
        String interfaceUnit = "interface testInterfaceDeclUnit extends IInterface {}";
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromString(interfaceUnit);
        handler.handle(((InterfaceDeclUnit)cu).body);
    }

    public void testEnumDecl() throws Exception {
        String enumUnit = "enum testEnumDecl {}";
        EnumDeclUnit enumDeclUnit = (EnumDeclUnit) ParserTestUtil.parseCompilationUnitFromString(enumUnit);
        handler.handle(enumDeclUnit.body);
    }

    public void testInnerClassMember() throws Exception {
        String innerClassMember = "class InnerClass {}";
        ApexParserImpl parser = ParserTestUtil.parseFromString(innerClassMember);
        BlockMember member = parser.classMember();
        handler.handle((InnerClassMember) member);
    }

    public void testInnerInterfaceMember() throws Exception {
        String innerInterfaceMember = "interface InnerInterface {}";
        ApexParserImpl parser = ParserTestUtil.parseFromString(innerInterfaceMember);
        BlockMember member = parser.classMember();
        handler.handle((InnerInterfaceMember) member);
    }

    public void testInnerEnumDecl() throws Exception {
        String innerEnumClassMember = "enum InnerEnum {}";
        ApexParserImpl parser = ParserTestUtil.parseFromString(innerEnumClassMember);
        BlockMember member = parser.classMember();
        handler.handle((InnerEnumMember) member);
    }

    public void testStmntBlockMember() throws Exception {
        String stmnt = "{ field = 2; }";
        ApexParserImpl parser = ParserTestUtil.parseFromString(stmnt);
        BlockMember member = parser.classMember();
        handler.handle((StmntBlockMember) member);
    }

    public void testStaticStmntBlockMember() throws Exception {
        String staticStmnt = "static { field  = 2; }";
        ApexParserImpl parser = ParserTestUtil.parseFromString(staticStmnt);
        BlockMember member = parser.classMember();
        handler.handle((StaticStmntBlockMember) member);
    }

    public void testFieldMember() throws Exception {
        String fieldClassMember =
                "public static final Map<String, String> fieldClassMember = new Map<String, String>();";
        ApexParserImpl parser = ParserTestUtil.parseFromString(fieldClassMember);
        BlockMember member = parser.classMember();
        handler.handle((FieldMember) member);
    }

    public void testMethodMember() throws Exception {
        String methodClassMember =
                "public static Map<String, String> methodClassMember(List<String> l, Integer i) { return null; }";
        ApexParserImpl parser = ParserTestUtil.parseFromString(methodClassMember);
        BlockMember member = parser.classMember();
        handler.handle((MethodMember) member);
    }

    public void testPropertyMember() throws Exception {
        String propertyMember =
                "public Set<Integer> values { get; set; }";
        ApexParserImpl parser = ParserTestUtil.parseFromString(propertyMember);
        BlockMember member = parser.classMember();
        handler.handle((PropertyMember) member);
    }

    public void testIdentifier() throws Exception {
        Identifier identifier = Identifier._Identifier(Loc._SyntheticLoc(), "identifier");
        handler.handle(identifier);
    }

    /*
     * The following is a bit unusual, but I also want to test that our tests handles all the necessary objects to be displayed.
     * If it doesn't we should throw an error at compile time (not run-time). 
     * Thus, I created this class that implements the IOutlineViewElementHandler interface to handle the different cases. 
     * If it forgets one, it will throw an error at compile-time.
     */
    final class TextProviderHandlerProxy implements IOutlineViewElementHandler<Void> {

        OutlineViewElementTextProvider handler = new OutlineViewElementTextProvider();

        @Override
        public Void handle(TriggerDeclUnit element) {
            String result = handler.handle(element);
            assertEquals("testTriggerDeclUnit", result);
            return null;
        }

        @Override
        public Void handle(ClassDecl element) {
            String result = handler.handle(element);
            assertEquals("testClassDeclUnit", result);
            return null;
        }

        @Override
        public Void handle(InterfaceDecl element) {
            String result = handler.handle(element);
            assertEquals("testInterfaceDeclUnit", result);
            return null;
        }

        @Override
        public Void handle(EnumDecl element) {
            String result = handler.handle(element);
            assertEquals("testEnumDecl", result);
            return null;
        }

        @Override
        public Void handle(InnerClassMember element) {
            String result = handler.handle(element);
            assertEquals("InnerClass", result);
            return null;
        }

        @Override
        public Void handle(InnerInterfaceMember element) {
            String result = handler.handle(element);
            assertEquals("InnerInterface", result);
            return null;
        }

        @Override
        public Void handle(InnerEnumMember element) {
            String result = handler.handle(element);
            assertEquals("InnerEnum", result);
            return null;
        }

        @Override
        public Void handle(StmntBlockMember element) {
            String result = handler.handle(element);
            assertEquals("{...}", result);
            return null;
        }

        @Override
        public Void handle(StaticStmntBlockMember element) {
            String result = handler.handle(element);
            assertEquals("{...}", result);
            return null;
        }

        @Override
        public Void handle(FieldMember element) {
            String result = handler.handle(element);
            assertEquals("fieldClassMember : Map<String, String>", result);
            return null;
        }

        @Override
        public Void handle(MethodMember element) {
            String result = handler.handle(element);
            assertEquals("methodClassMember(List<String>, Integer) : Map<String, String>", result);
            return null;
        }

        @Override
        public Void handle(PropertyMember element) {
            String result = handler.handle(element);
            assertEquals("values : Set<Integer>", result);
            return null;
        }

        @Override
        public Void handle(Identifier element) {
            String result = handler.handle(element);
            assertEquals("identifier", result);
            return null;
        }

    }
}
