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

import com.salesforce.ide.apex.internal.core.CompilerService;

import junit.framework.TestCase;

/**
 * This tests the robustness of our outline view against malformed elements. The minimal guarantee that we provide is
 * that we don't throw a NullPointerException. This is because the jADT nodes that are returned from the parser could be
 * partially constructed, i.e., they have null values. Attempting to access these members (sometimes in a chaining
 * manner) will cause problems.
 * 
 * This is not exhaustive, unfortunately, but tries to handle the common cases that have arisen through manual testing.
 * 
 * @author nchen
 *         
 */
public class DisplayInvalidMembersTest_unit extends TestCase {
    private ApexOutlineContentProvider contentProvider;
    
    public DisplayInvalidMembersTest_unit() {
        contentProvider = new ApexOutlineContentProvider();
    }
    
    /**
     * Expands all of the elements like we would in the outline view. The point of this is to ensure that we don't hit
     * any NPE.
     */
    private void expandAll(Object o) {
        Object[] children = contentProvider.getChildren(o);
        for (Object child : children) {
            expandAll(child);
        }
    }
    
    public void testBadFieldMember() throws Exception {
        String classDecl = "class Bad { integer a; integer b integer c;}"; // Missing semicolon after integer b
        setupOutlineVisitor(classDecl);
    }

    private void setupOutlineVisitor(String classDecl) {
        OutlineViewVisitor visitor = new OutlineViewVisitor();
        CompilerService.INSTANCE.visitAstFromString(classDecl, visitor);
        expandAll(visitor.getTopLevel());
    }
    
    public void testBadMethodMember_incompleteTypeParameter() throws Exception {
        String classDecl = "class Bad { public void method(List<> a, Integer b) {}}";
        setupOutlineVisitor(classDecl);
    }
    
    public void testBadMethodMember_incompleteArray() throws Exception {
        String classDecl = "class Bad { public void method(MyObject[ a) {}}";
        setupOutlineVisitor(classDecl);
    }
    
    public void testBadPropertyMember_noGetterOrSetter() throws Exception {
        String classDecl = "class Bad { public String myProperty{}}";
        setupOutlineVisitor(classDecl);
    }
    
    public void testBadPropertyMember_noClosingParentheses() throws Exception {
        String classDecl = "class Bad { public String myProperty{ }";
        setupOutlineVisitor(classDecl);
    }
    
    public void testBadInnerClassMember() throws Exception {
        String classDecl = "class Bad { interface Inner { void method(List<> a, Integer b); } }";
        setupOutlineVisitor(classDecl);
    }
    
    public void testBadInnerInterfaceMember() throws Exception {
        String classDecl = "class Bad { interface Inner { void method(List<> a); } }";
        setupOutlineVisitor(classDecl);
    }
    
    public void testBadInnerEnumMember() throws Exception {
        String classDecl = "class Bad { enum MyEnum { bad.dot.bad } }";
        setupOutlineVisitor(classDecl);
    }
}
