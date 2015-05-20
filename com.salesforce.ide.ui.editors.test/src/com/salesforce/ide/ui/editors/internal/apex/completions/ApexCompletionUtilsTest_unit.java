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
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.eclipse.jdt.internal.core.util.SimpleDocument;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.AbstractCompletionProposalDisplayable;
import com.salesforce.ide.ui.editors.internal.apex.completions.ApexCompletionUtils.CompletionPrefix;

/**
 * @author nchen
 */
public class ApexCompletionUtilsTest_unit extends TestCase {
    @SuppressWarnings("restriction")
    public static class MySimpleDocument extends SimpleDocument {

        public MySimpleDocument(String source) {
            super(source);
        }

        @Override
        public char getChar(int offset) {
            return get().charAt(offset);
        }
    }

    static class MockCompletion extends AbstractCompletionProposalDisplayable {
        String replacement;
        String display;

        public MockCompletion(String replacement, String display) {
            this.replacement = replacement;
            this.display = display;
        }

        @Override
        public String getReplacementString() {
            return replacement;
        }

        @Override
        public String getDisplayString() {
            return display;
        }

    }

    @Mock
    private ITextViewer mViewer;
    @Mock
    private IDocument mDoc;

    @Override
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    public ApexCompletionUtils getUtils() {
        return ApexCompletionUtils.INSTANCE;
    }

    @Test
    public void testGetPrefixReturnsNullWhenNullDoc() throws Exception {
        when(mViewer.getDocument()).thenReturn(null);

        String prefix = getUtils().getPrefix(mViewer, 0);

        assertNull(prefix);
    }

    @Test
    public void testGetPrefixReturnsNullWhenOffsetLargerThanDoc() throws Exception {
        when(mDoc.getLength()).thenReturn(90);
        when(mViewer.getDocument()).thenReturn(mDoc);

        String prefix = getUtils().getPrefix(mViewer, 100);

        assertNull(prefix);
    }

    @Test
    public void testGetPrefixReturnsValidJavaIdentifierPartSingleLineSingleWord() throws Exception {
        IDocument doc = new MySimpleDocument("one");
        when(mViewer.getDocument()).thenReturn(doc);

        String prefix = getUtils().getPrefix(mViewer, 3);

        assertEquals("one", prefix);
    }

    @Test
    public void testGetPrefixReturnsValidJavaIdentifierPartSingleLineMultipleWords() throws Exception {
        IDocument doc = new MySimpleDocument("one two three");
        when(mViewer.getDocument()).thenReturn(doc);

        String prefix = getUtils().getPrefix(mViewer, 5);

        assertEquals("t", prefix);
    }

    @Test
    public void testGetPrefixReturnsValidJavaIdentifierPartMultiline() throws Exception {
        IDocument doc = new MySimpleDocument("one\n two\n three\n");
        when(mViewer.getDocument()).thenReturn(doc);

        String prefix = getUtils().getPrefix(mViewer, 6);

        assertEquals("t", prefix);
    }

    @Test
    public void testGetPrefixReturnsValidJavaIdentifierPartWithQualifiedNameSeparator() throws Exception {
        IDocument doc = new MySimpleDocument("o.n.e");
        when(mViewer.getDocument()).thenReturn(doc);

        String prefix = getUtils().getPrefix(mViewer, 5);

        assertEquals("o.n.e", prefix);
    }

    public void testHasInvokedReturnsFalseWhenNullDoc() throws Exception {
        when(mViewer.getDocument()).thenReturn(null);

        boolean hasNew = getUtils().hasInvokedNewOnSameLine(mViewer, 0);

        assertFalse(hasNew);
    }

    @Test
    public void testHasInvokedReturnsFalseWhenOffsetLargerThanDoc() throws Exception {
        when(mDoc.getLength()).thenReturn(90);
        when(mViewer.getDocument()).thenReturn(mDoc);

        boolean hasNew = getUtils().hasInvokedNewOnSameLine(mViewer, 100);

        assertFalse(hasNew);
    }

    @Test
    public void testHasInvokedNewIsPartOfAWord() throws Exception {
        IDocument spiedDoc = spy(new MySimpleDocument("newInstance"));
        when(mViewer.getDocument()).thenReturn(spiedDoc);
        doReturn(new IRegion() {
            @Override
            public int getOffset() {
                return 0;
            }

            @Override
            public int getLength() {
                return 11;
            }
        }).when(spiedDoc).getLineInformationOfOffset(11);

        boolean hasNew = getUtils().hasInvokedNewOnSameLine(mViewer, 11);

        assertFalse(hasNew);
    }

    @Test
    public void testHasInvokedNew() throws Exception {
        IDocument spiedDoc = spy(new MySimpleDocument("new cursor"));
        when(mViewer.getDocument()).thenReturn(spiedDoc);
        doReturn(new IRegion() {
            @Override
            public int getOffset() {
                return 0;
            }

            @Override
            public int getLength() {
                return 10;
            }
        }).when(spiedDoc).getLineInformationOfOffset(10);

        boolean hasNew = getUtils().hasInvokedNewOnSameLine(mViewer, 10);

        assertTrue(hasNew);
    }

    // Note: I'm not going to micro-test the various shouldSuggest* methods in CompletionPrefix
    // We can test that for each individual proposal later

    @Test
    public void testCompletionPrefixCountsSegmentsWithSeparator() throws Exception {
        ApexCompletionUtils.CompletionPrefix cp = new CompletionPrefix("one.two");

        assertEquals(2, cp.segments.size());
        assertEquals("one", cp.segments.get(0));
        assertEquals("two", cp.segments.get(1));
    }

    @Test
    public void testCompletionPrefixCountsSegmentsAfterSeparator() throws Exception {
        ApexCompletionUtils.CompletionPrefix cp = new CompletionPrefix("one.two.");

        assertEquals(3, cp.segments.size());
        assertEquals("one", cp.segments.get(0));
        assertEquals("two", cp.segments.get(1));
        assertEquals("", cp.segments.get(2));
    }

    @Test
    public void testCreateProposalReturnsProposalsValidCollection() throws Exception {
        MockCompletion mock1 = new MockCompletion("mock1", "mock1");
        MockCompletion mock2 = new MockCompletion("mock2", "mock2");
        Collection<AbstractCompletionProposalDisplayable> suggestions =
                new ImmutableList.Builder<AbstractCompletionProposalDisplayable>().add(mock1).add(mock2).build();

        ICompletionProposal[] proposals = getUtils().createProposal(suggestions, "prefix", "prefix".length() + 1, null);

        assertEquals(2, proposals.length);
        assertEquals("mock1", proposals[0].getDisplayString());
        assertEquals("mock2", proposals[1].getDisplayString());
    }

    @Test
    public void testCreateProposalReturnsEmptyProposalsOnEmptyCollection() throws Exception {
        Collection<AbstractCompletionProposalDisplayable> suggestions = Collections.emptyList();

        ICompletionProposal[] proposals = getUtils().createProposal(suggestions, "prefix", "prefix".length() + 1, null);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testCreateProposalAcceptsEmptyDisplayString() throws Exception {
        MockCompletion mock1 = new MockCompletion("mock1", "");
        MockCompletion mock2 = new MockCompletion("mock2", "");
        Collection<AbstractCompletionProposalDisplayable> suggestions =
                new ImmutableList.Builder<AbstractCompletionProposalDisplayable>().add(mock1).add(mock2).build();

        ICompletionProposal[] proposals = getUtils().createProposal(suggestions, "prefix", "prefix".length() + 1, null);

        assertEquals(2, proposals.length);
        assertEquals("", proposals[0].getDisplayString());
        assertEquals("", proposals[1].getDisplayString());
    }

    @Test
    public void testCreateProposalRejectsEmptyReplacementString() throws Exception {
        MockCompletion mock1 = new MockCompletion("", "mock1");
        MockCompletion mock2 = new MockCompletion("", "mock2");
        Collection<AbstractCompletionProposalDisplayable> suggestions =
                new ImmutableList.Builder<AbstractCompletionProposalDisplayable>().add(mock1).add(mock2).build();

        ICompletionProposal[] proposals = getUtils().createProposal(suggestions, "prefix", "prefix".length() + 1, null);

        assertEquals(0, proposals.length);
    }

}
