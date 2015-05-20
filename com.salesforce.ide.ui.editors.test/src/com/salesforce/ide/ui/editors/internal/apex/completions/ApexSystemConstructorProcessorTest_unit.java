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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;
import com.salesforce.ide.ui.editors.internal.apex.completions.ApexCompletionUtils.CompletionPrefix;

/**
 * @author nchen
 * 
 */
public class ApexSystemConstructorProcessorTest_unit extends TestCase {
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
        doReturn(true).when(sUtils).hasInvokedNewOnSameLine(mViewer, BOGUS_OFFSET);
        doReturn(new CompletionPrefix(prefix)).when(sUtils).determineFullyQualifiedNameFromPrefix(prefix);
        return sUtils;
    }

    @Test
    public void testNoConstructorSuggestedWithoutNew() throws Exception {
        String prefix = "connectapi.";

        ApexCompletionUtils sUtils = spy(ApexCompletionUtils.INSTANCE);
        doReturn(prefix).when(sUtils).getPrefix(mViewer, BOGUS_OFFSET);
        doReturn(new CompletionPrefix(prefix)).when(sUtils).determineFullyQualifiedNameFromPrefix(prefix);
        ApexCompletionProcessor processor = new ApexSystemConstructorProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(0, proposals.length);
    }

    @Test
    public void testNamespacedSingleConstructor() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("connectAPI.addres");
        ApexCompletionProcessor processor = new ApexSystemConstructorProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(1, proposals.length);
        assertEquals("Address()", proposals[0].getDisplayString());
    }

    @Test
    public void testNamespacedOverloadedConstructors() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("connectAPI.BatchInpu");
        ApexCompletionProcessor processor = new ApexSystemConstructorProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(3, proposals.length);
        assertEquals("BatchInput(Object)", proposals[0].getDisplayString());
        assertEquals("BatchInput(Object,ConnectApi.BinaryInput)", proposals[1].getDisplayString());
        assertEquals("BatchInput(Object,List<ConnectApi.BinaryInput>)", proposals[2].getDisplayString());
    }

    @Test
    @Ignore
    public void _testTopLevelSingleConstructor() throws Exception {
        // The completions doesn't correctly return any single constructors in the system namespace
    }

    @Test
    public void testTopLevelOverloadedConstructors() throws Exception {
        ApexCompletionUtils sUtils = constructSpy("Ur");
        ApexCompletionProcessor processor = new ApexSystemConstructorProcessor(sUtils, completions);

        ICompletionProposal[] proposals = processor.computeCompletionProposals(mViewer, BOGUS_OFFSET);

        assertEquals(4, proposals.length);
        assertEquals("Url(String,String,Integer,String)", proposals[0].getDisplayString());
        assertEquals("Url(String,String,String)", proposals[1].getDisplayString());
        assertEquals("Url(String)", proposals[2].getDisplayString());
        assertEquals("Url(System.Url,String)", proposals[3].getDisplayString());
    }
}
