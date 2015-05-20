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

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.junit.Test;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

/**
 * @author nchen
 * 
 */
public class ApexCompletionCollectorTest_unit extends TestCase {
    @Test
    public void testIncludeAllActiveProcessors() {
        ApexCompletionCollector collector = new ApexCompletionCollector(null);
        List<IContentAssistProcessor> processors = collector.getProcessors();

        assertEquals(6, processors.size());
        assertTrue(Iterables.any(processors, Predicates.instanceOf(ApexSystemInstanceMembersProcessorForLocals.class)));
        assertTrue(Iterables.any(processors, Predicates.instanceOf(ApexSystemInstanceMembersProcessorForFields.class)));
        assertTrue(Iterables.any(processors, Predicates.instanceOf(ApexSystemConstructorProcessor.class)));
        assertTrue(Iterables.any(processors, Predicates.instanceOf(ApexSystemStaticMethodProcessor.class)));
        assertTrue(Iterables.any(processors, Predicates.instanceOf(ApexSystemTypeProcessor.class)));
        assertTrue(Iterables.any(processors, Predicates.instanceOf(ApexSystemNamespaceProcessor.class)));
    }

    @Test
    public void testCompletionActivationSequence() {
        ApexCompletionCollector collector = new ApexCompletionCollector(null);

        char[] activation = collector.getCompletionProposalAutoActivationCharacters();

        assertEquals(1, activation.length);
        assertEquals('.', activation[0]);
    }
}
