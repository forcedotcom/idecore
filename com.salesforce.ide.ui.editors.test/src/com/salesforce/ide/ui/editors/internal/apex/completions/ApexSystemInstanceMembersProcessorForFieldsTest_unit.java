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

import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;
import com.salesforce.ide.ui.editors.internal.apex.completions.ApexCompletionUtils.CompletionPrefix;

import junit.framework.TestCase;

/**
 * @author nchen
 * 
 */
public class ApexSystemInstanceMembersProcessorForFieldsTest_unit extends TestCase {
    private static int BOGUS_OFFSET = 100;

    // @formatter:off
    private static final String FILE_CONTENTS = "public class MyClass {\n" + 
    		"    private String myString;\n" + 
    		"    protected Boolean myBoolean;\n" + 
    		"    global Set<String> mySet;\n" + 
    		"    static Map<String, String> myMap;\n" + 
    		"    List<String> myList;\n" + 
    		"    \n" + 
    		"    public static void myMethod() {\n" + 
    		"    }\n" + 
    		"    \n" + 
    		"    Integer myInt;\n" + 
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
    public void testShouldNotSuggestFieldNameWhenNewOnSameLine() throws Exception {
        ApexCompletionUtils sUtil = spy(ApexCompletionUtils.INSTANCE);
        doReturn("MYS").when(sUtil).getPrefix(mViewer, BOGUS_OFFSET);
        doReturn(true).when(sUtil).hasInvokedNewOnSameLine(mViewer, BOGUS_OFFSET);
        doReturn(new CompletionPrefix("MYS")).when(sUtil).determineFullyQualifiedNameFromPrefix("MYS");

        ApexCompletionProcessor processor =
                new ApexSystemInstanceMembersProcessorForFields(sUtil, completions, mEditor);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testShouldNotSuggestFieldNameForBogusField() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("somethingThatDoesntExist");
        ApexCompletionProcessor processor =
                new ApexSystemInstanceMembersProcessorForFields(sUtils, completions, mEditor);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testSuggestFieldNameFromTopOfFile() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("MYS");
        ApexCompletionProcessor processor =
                new ApexSystemInstanceMembersProcessorForFields(sUtils, completions, mEditor);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(2, proposals.length);
        assertEquals("myString", proposals[0].getDisplayString());
        assertEquals("mySet", proposals[1].getDisplayString());
    }

    @Test
    public void testSuggestFieldNameFromMiddleOfFile() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("MYinT");
        ApexCompletionProcessor processor =
                new ApexSystemInstanceMembersProcessorForFields(sUtils, completions, mEditor);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(1, proposals.length);
        assertEquals("myInt", proposals[0].getDisplayString());
    }

    /**
     * @see TypeInfoUtilTest_unit that provides more member tests
     */
    @Test
    public void testSuggestFieldMembers() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("MYSET.ADD");
        ApexCompletionProcessor processor =
                new ApexSystemInstanceMembersProcessorForFields(sUtils, completions, mEditor);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(3, proposals.length);
        assertEquals("add(ANY) - Boolean", proposals[0].getDisplayString());
        assertEquals("addAll(List) - Boolean", proposals[1].getDisplayString());
        assertEquals("addAll(Set) - Boolean", proposals[2].getDisplayString());
    }
}
