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
package com.salesforce.ide.ui.editors.internal.apex.completions;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import apex.jorje.semantic.symbol.type.GenericTypeInfo;
import apex.jorje.semantic.symbol.type.GenericTypeInfoFactory;
import apex.jorje.semantic.symbol.type.ScalarTypeInfo;
import apex.jorje.semantic.symbol.type.TypeInfos;

import com.google.common.collect.Lists;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.AbstractCompletionProposalDisplayable;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;
import com.salesforce.ide.ui.editors.internal.apex.completions.TypeInfoUtil.SystemsInstanceMembersCompletionSuggestor;

/**
 * @author nchen
 * 
 */
public class TypeInfoUtilTest_unit extends TestCase {

    private Completions completions;

    @Override
    @Before
    public void setUp() throws Exception {
        completions = CompletionsTestUtils.INSTANCE.createTestCompletions();
    }

    @Test
    public void testHandleList() throws Exception {
        GenericTypeInfo listTypeInfo = GenericTypeInfoFactory.createList(TypeInfos.OBJECT);
        SystemsInstanceMembersCompletionSuggestor suggestor =
                new SystemsInstanceMembersCompletionSuggestor("add", completions);

        List<AbstractCompletionProposalDisplayable> list = Lists.newArrayList(listTypeInfo.accept(suggestor));

        assertEquals(4, list.size());
        assertEquals("add(ANY) - Object", list.get(0).getDisplayString());
        assertEquals("add(Integer,ANY) - void", list.get(1).getDisplayString());
        assertEquals("addAll(List) - void", list.get(2).getDisplayString());
        assertEquals("addAll(Set) - void", list.get(3).getDisplayString());
    }

    @Test
    public void testHandleMap() throws Exception {
        GenericTypeInfo mapTypeInfo = GenericTypeInfoFactory.createMap(TypeInfos.OBJECT, TypeInfos.OBJECT);
        SystemsInstanceMembersCompletionSuggestor suggestor =
                new SystemsInstanceMembersCompletionSuggestor("put", completions);

        List<AbstractCompletionProposalDisplayable> list = Lists.newArrayList(mapTypeInfo.accept(suggestor));

        assertEquals(3, list.size());
        assertEquals("put(ANY,ANY) - String", list.get(0).getDisplayString()); // FIXME: (Server) Why does the completions suggest put(ANY, ANY) returns String type?
        assertEquals("putAll(List) - void", list.get(1).getDisplayString());
        assertEquals("putAll(Map) - void", list.get(2).getDisplayString());
    }

    @Test
    @Ignore
    public void testHandleSet() throws Exception {
        GenericTypeInfo setTypeInfo = GenericTypeInfoFactory.createSet(TypeInfos.OBJECT);
        SystemsInstanceMembersCompletionSuggestor suggestor =
                new SystemsInstanceMembersCompletionSuggestor("add", completions);

        List<AbstractCompletionProposalDisplayable> list = Lists.newArrayList(setTypeInfo.accept(suggestor));

        assertEquals(3, list.size());
        assertEquals("add(ANY) - Boolean", list.get(0).getDisplayString());
        assertEquals("addAll(List) - Boolean", list.get(1).getDisplayString());
        assertEquals("addAll(Set) - Boolean", list.get(2).getDisplayString());
    }

    @Test
    public void testDoesNotSuggestNonStaticMembers() throws Exception {
        SystemsInstanceMembersCompletionSuggestor suggestor =
                new SystemsInstanceMembersCompletionSuggestor("valueof", completions);

        List<AbstractCompletionProposalDisplayable> list =
                Lists.newArrayList(TypeInfos.STRING.accept(suggestor));

        assertEquals(0, list.size());
    }

    @Test
    public void testHandleTopLevelType() throws Exception {
        SystemsInstanceMembersCompletionSuggestor suggestor =
                new SystemsInstanceMembersCompletionSuggestor("isa", completions);

        List<AbstractCompletionProposalDisplayable> list = Lists.newArrayList(TypeInfos.STRING.accept(suggestor));

        assertEquals(7, list.size());
        assertEquals("isAllLowerCase() - Boolean", list.get(0).getDisplayString());
        assertEquals("isAllUpperCase() - Boolean", list.get(1).getDisplayString());
        assertEquals("isAlpha() - Boolean", list.get(2).getDisplayString());
        assertEquals("isAlphanumeric() - Boolean", list.get(3).getDisplayString());
        assertEquals("isAlphanumericSpace() - Boolean", list.get(4).getDisplayString());
        assertEquals("isAlphaSpace() - Boolean", list.get(5).getDisplayString());
        assertEquals("isAsciiPrintable() - Boolean", list.get(6).getDisplayString());
    }

    @Test
    public void testHandleNamespacedType() throws Exception {
        SystemsInstanceMembersCompletionSuggestor suggestor =
                new SystemsInstanceMembersCompletionSuggestor("isa", completions);

        ScalarTypeInfo mString = spy(TypeInfos.STRING);
        doReturn("System.String").when(mString).getApexName();
        List<AbstractCompletionProposalDisplayable> list = Lists.newArrayList(mString.accept(suggestor));

        assertEquals(7, list.size());
        assertEquals("isAllLowerCase() - Boolean", list.get(0).getDisplayString());
        assertEquals("isAllUpperCase() - Boolean", list.get(1).getDisplayString());
        assertEquals("isAlpha() - Boolean", list.get(2).getDisplayString());
        assertEquals("isAlphanumeric() - Boolean", list.get(3).getDisplayString());
        assertEquals("isAlphanumericSpace() - Boolean", list.get(4).getDisplayString());
        assertEquals("isAlphaSpace() - Boolean", list.get(5).getDisplayString());
        assertEquals("isAsciiPrintable() - Boolean", list.get(6).getDisplayString());
    }
}
