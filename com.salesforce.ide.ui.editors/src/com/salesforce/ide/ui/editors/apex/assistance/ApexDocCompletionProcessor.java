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

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * Example Java doc completion processor.
 */
public class ApexDocCompletionProcessor implements IContentAssistProcessor {

    /**
     * FIXME: Duplicated!
     *
     * @see ApexDocScanner#fgKeywords
     */
    protected final static String[] fgProposals = {
            "@author", "@deprecated", "@exception", "@param", "@return", "@see", "@serial", "@serialData",
            "@serialField", "@since", "@throws", "@version" };

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
        ICompletionProposal[] result = new ICompletionProposal[fgProposals.length];
        for (int i = 0; i < fgProposals.length; i++) {
            result[i] = new CompletionProposal(fgProposals[i], documentOffset, 0, fgProposals[i].length());
        }
        return result;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public String getErrorMessage() {
        return null;
    }
}
