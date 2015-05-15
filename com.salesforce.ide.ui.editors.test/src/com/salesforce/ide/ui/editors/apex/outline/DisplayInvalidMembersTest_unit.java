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
import apex.jorje.data.ast.CompilationUnit;

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
    ApexOutlineContentProvider provider = new ApexOutlineContentProvider();
    

    public void testBadFieldMember() throws Exception {
        String classDecl = "class Bad { integer a; integer b integer c;}"; // Missing semicolon after integer b
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromString(classDecl);
        RootElementFilter filter = new RootElementFilter();
        cu._switch(filter);
        isFreeFromNulls(provider.getChildren(filter.getRootElements()[0]));
    }

    public void testBadMethodMember_incompleteTypeParameter() throws Exception {
        String classDecl = "class Bad { public void method(List<> a, Integer b) {}}";
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromString(classDecl);
        RootElementFilter filter = new RootElementFilter();
        cu._switch(filter);
        isFreeFromNulls(provider.getChildren(filter.getRootElements()[0]));
    }

    //    This causes a NPE but because we have more robust handlers now, it does not cause an issue in the IDE.
    //    Will check with spags about whether this is intended behavior  
    //    public void testBadMethodMember_incompleteArray() throws Exception {
    //        String classDecl = "class Bad { public void method(MyObject[ a) {}}";
    //        CompilationUnit cu = OutlineTestUtil.parseCompilationUnitFromString(classDecl);
    //        RootElementFilter filter = new RootElementFilter();
    //        cu._switch(filter);
    //        isFreeFromNulls(provider.getChildren(filter.getRootElements()[0]));
    //    }

    public void testBadPropertyMember_noGetterOrSetter() throws Exception {
        String classDecl = "class Bad { public String myProperty{}}";
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromString(classDecl);
        RootElementFilter filter = new RootElementFilter();
        cu._switch(filter);
        isFreeFromNulls(provider.getChildren(filter.getRootElements()[0]));
    }

    public void testBadPropertyMember_noClosingParentheses() throws Exception {
        String classDecl = "class Bad { public String myProperty{ }";
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromString(classDecl);
        RootElementFilter filter = new RootElementFilter();
        cu._switch(filter);
        isFreeFromNulls(provider.getChildren(filter.getRootElements()[0]));
    }

    public void testBadInnerClassMember() throws Exception {
        String classDecl = "class Bad { interface Inner { void method(List<> a, Integer b); } }";
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromString(classDecl);
        RootElementFilter filter = new RootElementFilter();
        cu._switch(filter);
        Object[] children = provider.getChildren(filter.getRootElements()[0]);
        isFreeFromNulls(provider.getChildren(children[0]));
    }

    public void testBadInnerInterfaceMember() throws Exception {
        String classDecl = "class Bad { interface Inner { void method(List<> a); } }";
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromString(classDecl);
        RootElementFilter filter = new RootElementFilter();
        cu._switch(filter);
        Object[] children = provider.getChildren(filter.getRootElements()[0]);
        isFreeFromNulls(provider.getChildren(children[0]));
    }

    public void testBadInnerEnumMember() throws Exception {
        String classDecl = "class Bad { enum MyEnum { bad.dot.bad } }";
        CompilationUnit cu = ParserTestUtil.parseCompilationUnitFromString(classDecl);
        RootElementFilter filter = new RootElementFilter();
        cu._switch(filter);
        isFreeFromNulls(provider.getChildren(filter.getRootElements()[0]));
    }

    private void isFreeFromNulls(Object[] toCheck) {
        for (Object each : toCheck) {
            if (each == null)
                fail("Saw a null element, this would be bad when we are trying to display in outline view");
        }
    }
}
