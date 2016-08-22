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

import com.salesforce.ide.apex.internal.core.CompilerService;

import junit.framework.TestCase;

/**
 * This tests the text label functionalities for the outline view. 
 * 
 * @author nchen
 *         
 */
public class OutlineviewElementTextProviderTest_unit extends TestCase {
    private ApexOutlineContentProvider contentProvider;
    private ApexLabelProvider labelProvider;
    
    public OutlineviewElementTextProviderTest_unit() {
        contentProvider = new ApexOutlineContentProvider();
        labelProvider = new ApexLabelProvider();
    }
    
    public void testUserTrigger() throws Exception {
        String userTrigger = "trigger testUserTrigger on Account (before insert) {}";
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(userTrigger, visitor);
        String text = labelProvider.getText(visitor.getTopLevel());
        assertEquals("testUserTrigger", text);
    }
    
    public void testUserClass() throws Exception {
        String userClass = "class testUserClass extends ParentClass implements IInterface {}";
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(userClass, visitor);
        String text = labelProvider.getText(visitor.getTopLevel());
        assertEquals("testUserClass", text);
    }
    
    public void testUserInterface() throws Exception {
        String userInterface = "interface testUserInterface extends IInterface {}";
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(userInterface, visitor);
        String text = labelProvider.getText(visitor.getTopLevel());
        assertEquals("testUserInterface", text);
    }
    
    public void testUserEnum() throws Exception {
        String userEnum = "enum testUserEnum {}";
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(userEnum, visitor);
        String text = labelProvider.getText(visitor.getTopLevel());
        assertEquals("testUserEnum", text);
    }
    
    public void testFieldMember() throws Exception {
        String userClass =
            "public class testUserClass { public static final Map<String, String> classMember = new Map<String, String>(); }";
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(userClass, visitor);
        Object fieldMember = contentProvider.getChildren(visitor.getTopLevel())[0];
        String text = labelProvider.getText(fieldMember);
        assertEquals("classMember : Map<String,String>", text);
    }

    public void testConstructorMemberWithoutParam() throws Exception {
        String userClass =
            "public class testUserClass { public testUserClass() { } }";
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(userClass, visitor);
        Object methodMember = contentProvider.getChildren(visitor.getTopLevel())[0];
        String text = labelProvider.getText(methodMember);
        assertEquals("testUserClass()", text);
    }

    public void testConstructorMemberWithParams() throws Exception {
        String userClass =
            "public class testUserClass { public testUserClass(List<String> l, String s) { } }";
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(userClass, visitor);
        Object methodMember = contentProvider.getChildren(visitor.getTopLevel())[0];
        String text = labelProvider.getText(methodMember);
        assertEquals("testUserClass(List<String>, String)", text);
    }
    
    public void testMethodMemberWithoutParam() throws Exception {
        String userClass =
            "public class testUserClass { public static void methodClassMember() { return null; } }";
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(userClass, visitor);
        Object methodMember = contentProvider.getChildren(visitor.getTopLevel())[0];
        String text = labelProvider.getText(methodMember);
        assertEquals("methodClassMember() : void", text);
    }

    public void testMethodMemberWithParams() throws Exception {
        String userClass =
            "public class testUserClass { public static Map<String, String> methodClassMember(List<String> l, Integer i) { return null; } }";
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(userClass, visitor);
        Object methodMember = contentProvider.getChildren(visitor.getTopLevel())[0];
        String text = labelProvider.getText(methodMember);
        assertEquals("methodClassMember(List<String>, Integer) : Map<String,String>", text);
    }
    
    public void testPropertyMember() throws Exception {
        String userClass = "public class testUserClass { public Set<Integer> values { get; set; } }";
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(userClass, visitor);
        Object propertyMember = contentProvider.getChildren(visitor.getTopLevel())[0];
        String text = labelProvider.getText(propertyMember);
        assertEquals("values : Set<Integer>", text);
    }
}
