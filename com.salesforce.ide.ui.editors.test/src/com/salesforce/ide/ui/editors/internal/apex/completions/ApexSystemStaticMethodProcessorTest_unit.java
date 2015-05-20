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
public class ApexSystemStaticMethodProcessorTest_unit extends TestCase {
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
    public void testShouldNotSuggestInstanceMethods() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("Set.a");
        ApexCompletionProcessor processor = new ApexSystemStaticMethodProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testShouldSuggestFullyQualifiedTypeStaticMethod() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("System.sysTEM.abo");
        ApexCompletionProcessor processor = new ApexSystemStaticMethodProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(1, proposals.length);
        assertEquals("abortJob(String) - void", proposals[0].getDisplayString());
    }

    @Test
    public void testShouldSuggestImplicitSystemNamespaceStaticMethods() throws Exception {
        String prefix = "sysTEM.abo"; // case-insensitive

        ApexCompletionUtils sUtils = constructSpy(prefix);
        ApexCompletionProcessor processor = new ApexSystemStaticMethodProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(1, proposals.length);
        assertEquals("abortJob(String) - void", proposals[0].getDisplayString());
    }

    @Test
    public void testShouldSuggestOverloadedStaticMethod() throws Exception {
        String prefix = "sysTEM.de"; // case-insensitive

        ApexCompletionUtils sUtils = constructSpy(prefix);
        ApexCompletionProcessor processor = new ApexSystemStaticMethodProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(2, proposals.length);
        assertEquals("debug(ANY) - void", proposals[0].getDisplayString());
        assertEquals("debug(APEX_OBJECT,ANY) - void", proposals[1].getDisplayString());
    }

    @Test
    public void testShouldNotSuggestStaticMethodInNewContext() throws Exception {
        String prefix = "sysTEM.de";

        ApexCompletionUtils sUtils = spy(ApexCompletionUtils.INSTANCE);
        doReturn(prefix).when(sUtils).getPrefix(mViewer, BOGUS_OFFSET);
        doReturn(true).when(sUtils).hasInvokedNewOnSameLine(mViewer, BOGUS_OFFSET);
        doReturn(new CompletionPrefix(prefix)).when(sUtils).determineFullyQualifiedNameFromPrefix(prefix); // case-insensitive

        ApexCompletionProcessor processor = new ApexSystemStaticMethodProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(0, proposals.length);
    }
}
