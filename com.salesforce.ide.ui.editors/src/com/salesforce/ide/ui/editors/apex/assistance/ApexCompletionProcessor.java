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
package com.salesforce.ide.ui.editors.apex.assistance;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.google.common.collect.Lists;
import com.salesforce.ide.apex.core.tooling.systemcompletions.ApexSystemCompletionsRepository;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.AbstractCompletionProposalDisplayable;

/**
 * @author nchen
 */
public class ApexCompletionProcessor implements IContentAssistProcessor {
    private StringBuilder errorCollector = new StringBuilder();

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        Collection<AbstractCompletionProposalDisplayable> suggestions = Lists.newArrayList();
        String prefix = null;
        try {
            prefix = getPrefix(viewer, offset);
            if (prefix == null || prefix.length() == 0) { // provide everything
                suggestions = ApexSystemCompletionsRepository.INSTANCE.getCompletions().namespaceTrie.values();
            } else { // use the prefix
                suggestions = ApexSystemCompletionsRepository.INSTANCE.getCompletions().namespaceTrie.prefixMap(prefix).values();
            }
        } catch (BadLocationException e) {}
        return createProposal(suggestions, prefix, offset);
    }

    private ICompletionProposal[] createProposal(Collection<AbstractCompletionProposalDisplayable> suggestions,
            String prefix, int offset) {
        List<ICompletionProposal> proposals = Lists.newArrayList();

        for (AbstractCompletionProposalDisplayable suggestion : suggestions) {
            String completionProposal = suggestion.completionProposal();
            int prefixLength = prefix != null ? prefix.length() : 0;
            proposals.add(new CompletionProposal(completionProposal, offset - prefixLength, 0, completionProposal.length()));
        }
        return proposals.toArray(new ICompletionProposal[0]);
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] { '.' };
    }

    private String getPrefix(ITextViewer viewer, int offset) throws BadLocationException {
        IDocument doc = viewer.getDocument();
        if (doc == null || offset > doc.getLength())
            return null;

        int length = 0;
        while (--offset >= 0 && Character.isJavaIdentifierPart(doc.getChar(offset)))
            length++;

        return doc.get(offset + 1, length);
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return errorCollector.toString();
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    public void clearState() {
        errorCollector = new StringBuilder();
    }

}
