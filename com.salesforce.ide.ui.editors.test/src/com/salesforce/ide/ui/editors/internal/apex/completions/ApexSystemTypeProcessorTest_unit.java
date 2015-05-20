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
import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;
import com.salesforce.ide.ui.editors.internal.apex.completions.ApexCompletionUtils.CompletionPrefix;

/**
 * @author nchen
 * 
 */
public class ApexSystemTypeProcessorTest_unit extends TestCase {
    private static int BOGUS_OFFSET = 100;

    private Completions completions;

    @Mock
    private ITextViewer mViewer;

    @Override
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        completions = CompletionsTestUtils.INSTANCE.createTestCompletions();
    }

    private ApexCompletionUtils constructSpy(String prefix) throws BadLocationException {
        ApexCompletionUtils sUtils = spy(ApexCompletionUtils.INSTANCE);
        doReturn(prefix).when(sUtils).getPrefix(mViewer, BOGUS_OFFSET);
        doReturn(new CompletionPrefix(prefix)).when(sUtils).determineFullyQualifiedNameFromPrefix(prefix);
        return sUtils;
    }

    @Test
    public void testShouldStillSuggestSystemNamespacedType() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("sysTEM.a");
        ApexCompletionProcessor processor = new ApexSystemTypeProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(6, proposals.length);
        assertEquals("Address", proposals[0].getDisplayString());
        assertEquals("Answers", proposals[1].getDisplayString());
        assertEquals("ApexPages", proposals[2].getDisplayString());
        assertEquals("AssertException", proposals[3].getDisplayString());
        assertEquals("AsyncException", proposals[4].getDisplayString());
        assertEquals("Aura", proposals[5].getDisplayString());
    }

    @Test
    public void testShouldSuggestNamespacedType() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("dataBASE.Get");
        ApexCompletionProcessor processor = new ApexSystemTypeProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(2, proposals.length);
        assertEquals("GetDeletedResult", proposals[0].getDisplayString());
        assertEquals("GetUpdatedResult", proposals[1].getDisplayString());
    }

    @Test
    public void testShouldNotSuggestBogusNamespacedType() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("bogus.system.");
        ApexCompletionProcessor processor = new ApexSystemTypeProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testShouldSuggestTopLevelSystemType() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("Addre");
        ApexCompletionProcessor processor = new ApexSystemTypeProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(1, proposals.length);
        assertEquals("Address", proposals[0].getDisplayString());
    }

    @Test
    public void testShouldNotSuggestMultiLevelSystemType() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("a.b.c");
        ApexCompletionProcessor processor = new ApexSystemTypeProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testShouldNotSuggestWhenNewIsOnTheSameLine() throws Exception {
        String prefix = "sYs"; // case-insensitive

        ApexCompletionUtils sUtils = spy(ApexCompletionUtils.INSTANCE);
        doReturn(prefix).when(sUtils).getPrefix(mViewer, BOGUS_OFFSET);
        doReturn(true).when(sUtils).hasInvokedNewOnSameLine(mViewer, BOGUS_OFFSET);
        doReturn(new CompletionPrefix(prefix)).when(sUtils).determineFullyQualifiedNameFromPrefix(prefix);

        ApexCompletionProcessor processor = new ApexSystemTypeProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(0, proposals.length);
    }
}
