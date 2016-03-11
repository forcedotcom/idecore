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
import java.net.URISyntaxException;

import org.antlr.runtime.RecognitionException;

import com.salesforce.ide.apex.core.utils.ParserTestUtil;
import com.salesforce.ide.apex.internal.core.CompilerService;
import com.salesforce.ide.ui.editors.apex.outline.text.OutlineViewElementTextProvider.FieldPrinter;
import com.salesforce.ide.ui.editors.apex.outline.text.OutlineViewElementTextProvider.MethodInfoPrinter;
import com.salesforce.ide.ui.editors.apex.outline.text.OutlineViewElementTextProvider.PropertyPrinter;
import com.salesforce.ide.ui.editors.apex.outline.text.OutlineViewElementTextProvider.TypeInfoPrinter;

import apex.jorje.semantic.ast.compilation.UserClass;
import apex.jorje.semantic.ast.compilation.UserEnum;
import apex.jorje.semantic.ast.compilation.UserInterface;
import apex.jorje.semantic.ast.member.Field;
import apex.jorje.semantic.ast.member.Method;
import apex.jorje.semantic.ast.member.Property;
import junit.framework.TestCase;

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
        String fileContents = ParserTestUtil.readFromFile(testFile);
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(fileContents, visitor);
        return provider.getChildren(visitor.getTopLevel());
    }

    public void testFieldMembers() {
        Field staticField = (Field) classNoNestedChildren[0];
        assertEquals("staticField : Integer", FieldPrinter.print(staticField));

        Field instanceField = (Field) classNoNestedChildren[1];
        assertEquals("field : Integer", FieldPrinter.print(instanceField));

        // Internally, we split the declaration into three lines
        // So each variable is on a different line in the outline view
        // This is the best way to display it
        Field instanceFields = (Field) classNoNestedChildren[2];
        assertEquals("field1 : Integer", FieldPrinter.print(instanceFields));
        instanceFields = (Field) classNoNestedChildren[3];
        assertEquals("field2 : Integer", FieldPrinter.print(instanceFields));
        instanceFields = (Field) classNoNestedChildren[4];
        assertEquals("field3 : Integer", FieldPrinter.print(instanceFields));
    }

    public void testPropertyMember() {
        Property propertyField = (Property) classNoNestedChildren[5];
        assertEquals("property : Integer", PropertyPrinter.print(propertyField));
    }

    public void testConstructor() {
        Method initializer = (Method) classNoNestedChildren[6];
        assertEquals("ClassWithValidMembersNoNested()", MethodInfoPrinter.print(initializer.getMethodInfo()));
    }

    // Interfaces
    /////////////

    public void testInterfaceMethod() {
        Method method = (Method) interfaceNoNested[0];
        assertEquals("method() : void", MethodInfoPrinter.print(method.getMethodInfo()));
    }

    // Enums
    ////////

    public void testEnumMembers() {
        Field enumIdentifier = (Field) enumNoNested[0];
        assertEquals("YES", FieldPrinter.print(enumIdentifier));

        enumIdentifier = (Field) enumNoNested[1];
        assertEquals("NO", FieldPrinter.print(enumIdentifier));
    }

    // Triggers
    ///////////

    public void testTriggerMembers() {
        // Should only see the two fields and one inner class
        // Should not see the for loop as a stmnt
        assertEquals(3, triggerNestedChildren.length);
        
        Field staticVar = (Field) triggerNestedChildren[0];
        assertEquals("staticVar : Integer", FieldPrinter.print(staticVar));

        Field instanceVar = (Field) triggerNestedChildren[1];
        assertEquals("instanceVar : Integer", FieldPrinter.print(instanceVar));

        UserClass myInnerClass = (UserClass) triggerNestedChildren[2];
        assertEquals("MyInnerClass", TypeInfoPrinter.print(myInnerClass.getDefiningType()));
        
        Method methodMember = (Method) provider.getChildren(myInnerClass)[0];
        assertEquals("innerClassMethod() : void", MethodInfoPrinter.print(methodMember.getMethodInfo()));
    }

    // Nested elements
    ///////////////////

    public void testNestedEnum() {
        UserEnum innerEnum = (UserEnum) classNestedChildren[0];
        Object[] children = provider.getChildren(innerEnum);

        Field enumIdentifier = (Field) children[0];
        assertEquals("YES", FieldPrinter.print(enumIdentifier));

        enumIdentifier = (Field) children[1];
        assertEquals("NO", FieldPrinter.print(enumIdentifier));
    }

    public void testNestedClass() {
        UserClass innerClass = (UserClass) classNestedChildren[1];
        Object[] children = provider.getChildren(innerClass);

        Field field = (Field) children[0];
        assertEquals("field : Integer", FieldPrinter.print(field));
    }

    public void testNestedInterface() {
        UserInterface innerInterface = (UserInterface) classNestedChildren[2];
        Object[] children = provider.getChildren(innerInterface);

        Method method = (Method) children[0];
        assertEquals("innerInterfaceMethod() : void", MethodInfoPrinter.print(method.getMethodInfo()));
    }
}
