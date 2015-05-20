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
package com.salesforce.ide.ui.editors.internal.apex.completions;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.preferences.PreferenceConstants;

/**
 * Collects all the possible completions for Apex and displays them.
 * 
 * @author nchen
 */
public class ApexCompletionCollector implements IContentAssistProcessor {
    private StringBuilder errorCollector = new StringBuilder();
    private final List<IContentAssistProcessor> processors;

    public ApexCompletionCollector(ITextEditor fTextEditor) {
        processors = Lists.newArrayList();
        processors.add(new ApexSystemInstanceMembersProcessorForLocals(fTextEditor));
        processors.add(new ApexSystemInstanceMembersProcessorForFields(fTextEditor));
        processors.add(new ApexSystemConstructorProcessor());
        processors.add(new ApexSystemStaticMethodProcessor());
        processors.add(new ApexSystemTypeProcessor());
        processors.add(new ApexSystemNamespaceProcessor());
    }

    protected boolean shouldEnableAutoCompletion() {
        IPreferenceStore preferenceStore = ForceIdeEditorsPlugin.getDefault().getPreferenceStore();
        return preferenceStore.getBoolean(PreferenceConstants.EDITOR_AUTOCOMPLETION);
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        List<ICompletionProposal> suggestions = Lists.newArrayList();

        if (shouldEnableAutoCompletion()) {

            for (IContentAssistProcessor processor : processors) {
                try {
                    suggestions.addAll(Lists.newArrayList(processor.computeCompletionProposals(viewer, offset)));
                } catch (Throwable t) {}
            }
        }

        return suggestions.toArray(new ICompletionProposal[0]);
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] { '.' };
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

    @VisibleForTesting
    public List<IContentAssistProcessor> getProcessors() {
        return processors;
    }

}
