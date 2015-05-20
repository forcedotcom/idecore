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
 */
public class ApexSystemNamespaceProcessorTest_unit extends TestCase {
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
    public void testShouldSuggestAllValidNamespace() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("");
        ApexCompletionProcessor processor = new ApexSystemNamespaceProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(Completions.kosherNamespace.size(), proposals.length);
    }

    @Test
    public void testShouldSuggestSingleValidNamespace() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("sYs");
        ApexCompletionProcessor processor = new ApexSystemNamespaceProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(1, proposals.length);
        assertEquals("System", proposals[0].getDisplayString());
    }

    @Test
    public void testShouldSuggestMultipleValidNamespaces() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("aP");
        ApexCompletionProcessor processor = new ApexSystemNamespaceProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(2, proposals.length);
        assertEquals("ApexPages", proposals[0].getDisplayString());
        assertEquals("Approval", proposals[1].getDisplayString());
    }

    @Test
    public void testShouldNotSuggestNamespaceForFullyQualifiedName() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("System.");
        ApexCompletionProcessor processor = new ApexSystemNamespaceProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(0, proposals.length);

    }
}
