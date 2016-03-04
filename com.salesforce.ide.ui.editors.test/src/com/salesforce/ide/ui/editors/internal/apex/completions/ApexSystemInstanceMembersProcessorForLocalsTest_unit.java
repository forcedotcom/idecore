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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.salesforce.ide.apex.core.utils.ParserTestUtil;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;
import com.salesforce.ide.ui.editors.internal.apex.completions.ApexCompletionUtils.CompletionPrefix;

import apex.jorje.semantic.ast.compilation.Compilation;
import junit.framework.TestCase;

/**
 * @author nchen
 * 
 */
public class ApexSystemInstanceMembersProcessorForLocalsTest_unit extends TestCase {
    private static int BOGUS_OFFSET = 100;

    // @formatter:off
    private static final String FILE_CONTENTS = "public class MyClass {\n" + 
    		"    public static void method1() {\n" + 
    		"        String insideMethod1;\n" + 
    		"    }\n" + 
    		"    \n" + 
    		"    public static void method2() {\n" + 
    		"      Set<String> insideMethod2;\n" + 
    		"    }\n" + 
    		"    \n" + 
    		"}";
    // @formatter:on

    private Completions completions;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ITextEditor mEditor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IFile mFile;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ITextViewer mViewer;

    @Override
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        completions = CompletionsTestUtils.INSTANCE.createTestCompletions();

        when(mEditor.getEditorInput().getAdapter(any())).thenReturn(mFile);
        when(mFile.getContents()).thenReturn(new ByteArrayInputStream(FILE_CONTENTS.getBytes()));
        when(mFile.getPersistentProperty(any())).thenReturn(null);
        when(mViewer.getDocument().get()).thenReturn(FILE_CONTENTS);
    }

    private ApexCompletionUtils constructSpy(String prefix) throws BadLocationException {
        ApexCompletionUtils sUtils = spy(ApexCompletionUtils.INSTANCE);
        doReturn(prefix).when(sUtils).getPrefix(mViewer, BOGUS_OFFSET);
        doReturn(new CompletionPrefix(prefix)).when(sUtils).determineFullyQualifiedNameFromPrefix(prefix);
        return sUtils;
    }

    @Test
    public void testShouldNotSuggestLocalNameWhenNewOnSameLine() throws Exception {
        String prefix = "insideMethod1";
        ApexCompletionUtils sUtil = spy(ApexCompletionUtils.INSTANCE);
        doReturn(prefix).when(sUtil).getPrefix(mViewer, BOGUS_OFFSET);
        doReturn(true).when(sUtil).hasInvokedNewOnSameLine(mViewer, BOGUS_OFFSET);
        doReturn(new CompletionPrefix(prefix)).when(sUtil).determineFullyQualifiedNameFromPrefix(prefix);

        ApexCompletionProcessor processor =
                new ApexSystemInstanceMembersProcessorForLocals(sUtil, completions, mEditor);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testShouldNotSuggestLocalNameForBogusLocal() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("somethingThatDoesntExist");
        when(mViewer.getDocument().getLineOfOffset(anyInt())).thenReturn(3); // inside method1();
        ApexCompletionProcessor processor =
                new ApexSystemInstanceMembersProcessorForLocals(sUtils, completions, mEditor);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testShouldSuggestLocalNameInsideMethod() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("INSIDEmethod");
        when(mViewer.getDocument().getLineOfOffset(anyInt())).thenReturn(3); // inside method1();
        ApexCompletionProcessor processor =
                new ApexSystemInstanceMembersProcessorForLocals(sUtils, completions, mEditor);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(1, proposals.length);
        assertEquals("insideMethod1", proposals[0].getDisplayString());
    }

    @Test
    public void testShouldSuggestLocalNameInsideMethodDoesntLeakOut() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("INSIDEmethod");
        when(mViewer.getDocument().getLineOfOffset(anyInt())).thenReturn(7); // inside method2();
        ApexCompletionProcessor processor =
                new ApexSystemInstanceMembersProcessorForLocals(sUtils, completions, mEditor);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(1, proposals.length);
        assertEquals("insideMethod2", proposals[0].getDisplayString());
    }

    /**
     * @see TypeInfoUtilTest_unit that provides more member tests
     */
    @Test
    public void testSuggestFieldMembers() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("insideMethod2.ADD");
        when(mViewer.getDocument().getLineOfOffset(anyInt())).thenReturn(7); // inside method2();
        ApexCompletionProcessor processor =
                new ApexSystemInstanceMembersProcessorForLocals(sUtils, completions, mEditor);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(3, proposals.length);
        assertEquals("add(ANY) - Boolean", proposals[0].getDisplayString());
        assertEquals("addAll(List) - Boolean", proposals[1].getDisplayString());
        assertEquals("addAll(Set) - Boolean", proposals[2].getDisplayString());
    }
}
