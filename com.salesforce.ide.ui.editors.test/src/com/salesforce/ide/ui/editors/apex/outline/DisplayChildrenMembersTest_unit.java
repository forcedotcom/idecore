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

import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.antlr.runtime.RecognitionException;

import com.salesforce.ide.apex.core.utils.ParserTestUtil;

import apex.jorje.data.ast.BlockMember.FieldMember;
import apex.jorje.data.ast.BlockMember.InnerClassMember;
import apex.jorje.data.ast.BlockMember.InnerEnumMember;
import apex.jorje.data.ast.BlockMember.InnerInterfaceMember;
import apex.jorje.data.ast.BlockMember.MethodMember;
import apex.jorje.data.ast.BlockMember.PropertyMember;
import apex.jorje.data.ast.BlockMember.StaticStmntBlockMember;
import apex.jorje.data.ast.BlockMember.StmntBlockMember;
import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.data.ast.Identifier;

/**
 * Tests that we are obtaining all the children that we want in the outline view. This is not a simple test of the
 * compiler, it's a test to verify that we are augmenting/collapsing certain nodes when we display.
 * 
 * This exercises the getChildren method in the ApexOutlineContentProvider. It does not need any UI.
 * 
 * @author nchen
 * 
 */
public class DisplayChildrenMembersTest_unit extends TestCase {
    private ApexOutlineContentProvider provider;

    Object[] classNoNestedChildren;
    Object[] interfaceNoNested;
    Object[] enumNoNested;
    Object[] classNestedChildren;
    Object[] triggerNestedChildren;

    // Classes
    //////////

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = new ApexOutlineContentProvider();
        classNoNestedChildren = setUpCompilationUnit("/filemetadata/outline/classes/ClassWithValidMembersNoNested.cls");
        interfaceNoNested = setUpCompilationUnit("/filemetadata/outline/classes/InterfaceWithValidMembers.cls");
        enumNoNested = setUpCompilationUnit("/filemetadata/outline/classes/EnumOnly.cls");
        classNestedChildren = setUpCompilationUnit("/filemetadata/outline/classes/ClassWithValidMembersNested.cls");
        triggerNestedChildren = setUpCompilationUnit("/filemetadata/outline/classes/TriggerWithValidMembers.trigger");
    }

    private Object[] setUpCompilationUnit(String testFile) throws IOException, URISyntaxException, RecognitionException {
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromFile(testFile);
        RootElementFilter filter = new RootElementFilter();
        cu._switch(filter);
        return provider.getChildren(filter.getRootElements()[0]);
    }

    public void testFieldMembers() {
        FieldMember staticField = (FieldMember) classNoNestedChildren[0];
        assertEquals("staticField", staticField.variableDecls.decls.get(0).name.value);

        FieldMember instanceField = (FieldMember) classNoNestedChildren[1];
        assertEquals("field", instanceField.variableDecls.decls.get(0).name.value);

        // Internally, we split the declaration into three lines
        // So each variable is on a different line in the outline view
        // This is the best way to display it
        FieldMember instanceFields = (FieldMember) classNoNestedChildren[2];
        assertEquals("field1", instanceFields.variableDecls.decls.get(0).name.value);
        instanceFields = (FieldMember) classNoNestedChildren[3];
        assertEquals("field2", instanceFields.variableDecls.decls.get(0).name.value);
        instanceFields = (FieldMember) classNoNestedChildren[4];
        assertEquals("field3", instanceFields.variableDecls.decls.get(0).name.value);
    }

    public void testPropertyMember() {
        PropertyMember propertyField = (PropertyMember) classNoNestedChildren[5];
        assertEquals("property", propertyField.propertyDecl.name.value);
    }

    // StaticInitializers do not have names, we just test that they are present.
    public void testStaticStmntBlockMember() {
        StaticStmntBlockMember staticInitializer = (StaticStmntBlockMember) classNoNestedChildren[6];
        assertNotNull(staticInitializer);
    }

    // Initializers do not have names, we just test that they are present.
    public void testStmntBlockMember() {
        StmntBlockMember initializer = (StmntBlockMember) classNoNestedChildren[7];
        assertNotNull(initializer);
    }

    public void testMethodMember() {
        MethodMember initializer = (MethodMember) classNoNestedChildren[8];
        assertEquals("ClassWithValidMembersNoNested", initializer.methodDecl.name.value);
    }

    // Interfaces
    /////////////

    public void testInterfaceMethod() {
        MethodMember initializer = (MethodMember) interfaceNoNested[0];
        assertEquals("method", initializer.methodDecl.name.value);
    }

    // Enums
    ////////

    public void testEnumMembers() {
        Identifier enumIdentifier = (Identifier) enumNoNested[0];
        assertEquals("YES", enumIdentifier.value);

        enumIdentifier = (Identifier) enumNoNested[1];
        assertEquals("NO", enumIdentifier.value);
    }

    // Triggers
    ///////////

    public void testTrickyGetElementsForTriggers() throws Exception {
        String triggerFile = "/filemetadata/outline/classes/TriggerWithValidMembers.trigger";
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromFile(triggerFile);

        provider.inputChanged(null, null, cu);

        TriggerDeclUnit firstPass = (TriggerDeclUnit) provider.getElements(cu)[0];
        assertEquals("MyTrigger", firstPass.name.value);

        Object[] secondPass = provider.getElements(cu);
        assertTrue(secondPass[0] instanceof FieldMember);
        assertTrue(secondPass[1] instanceof FieldMember);
        assertTrue(secondPass[2] instanceof InnerClassMember);
    }

    public void testTriggerMembers() {
        // Should only see the two fields and one inner class
        // Should not see the for loop as a stmnt
        assertEquals(3, triggerNestedChildren.length);
        
        FieldMember staticVar = (FieldMember) triggerNestedChildren[0];
        assertEquals("staticVar", staticVar.variableDecls.decls.get(0).name.value);

        FieldMember instanceVar = (FieldMember) triggerNestedChildren[1];
        assertEquals("instanceVar", instanceVar.variableDecls.decls.get(0).name.value);

        InnerClassMember myInnerClass = (InnerClassMember) triggerNestedChildren[2];
        assertEquals("MyInnerClass", myInnerClass.body.name.value);
        
        MethodMember methodMember = (MethodMember) myInnerClass.body.members.values.get(0);
        assertEquals("innerClassMethod", methodMember.methodDecl.name.value);
    }

    // Nested elements
    ///////////////////

    public void testNestedEnum() {
        InnerEnumMember innerEnum = (InnerEnumMember) classNestedChildren[0];
        Object[] children = provider.getChildren(innerEnum);

        Identifier enumIdentifier = (Identifier) children[0];
        assertEquals("YES", enumIdentifier.value);

        enumIdentifier = (Identifier) children[1];
        assertEquals("NO", enumIdentifier.value);
    }

    public void testNestedClass() {
        InnerClassMember innerClass = (InnerClassMember) classNestedChildren[1];
        Object[] children = provider.getChildren(innerClass);

        FieldMember field = (FieldMember) children[0];
        assertEquals("field", field.variableDecls.decls.get(0).name.value);
    }

    public void testNestedInterface() {
        InnerInterfaceMember innerInterface = (InnerInterfaceMember) classNestedChildren[2];
        Object[] children = provider.getChildren(innerInterface);

        MethodMember method = (MethodMember) children[0];
        assertEquals("innerInterfaceMethod", method.methodDecl.name.value);
    }
}
