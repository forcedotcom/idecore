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

import java.util.Collection;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.swt.graphics.Image;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.AbstractCompletionProposalDisplayable;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;
import com.salesforce.ide.ui.internal.ForceImages;

/**
 * Offers suggestions for system namespaces.
 * 
 * @author nchen
 * 
 */
public class ApexSystemNamespaceProcessor extends ApexCompletionProcessor implements IContentAssistProcessor {

    public ApexSystemNamespaceProcessor() {}

    @VisibleForTesting
    public ApexSystemNamespaceProcessor(ApexCompletionUtils utils, Completions completions) {
        this.utils = utils;
        this.completions = completions;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        Collection<AbstractCompletionProposalDisplayable> suggestions = Lists.newArrayList();
        String prefix = null;
        ApexCompletionUtils.CompletionPrefix completionPrefix = null;

        try {
            Completions completions = getCompletions();
            
            if (completions != null) {
                prefix = getUtil().getPrefix(viewer, offset);
                completionPrefix = getUtil().determineFullyQualifiedNameFromPrefix(prefix);

                if (completionPrefix.shouldSuggestNamespace()) {
                    String namespacePrefix = completionPrefix.segments.get(0);
                    suggestions = completions.namespaceTrie.prefixMap(namespacePrefix).values();
                    return getUtil().createProposal(suggestions, namespacePrefix, offset, getImage());
                }
            }
        } catch (Exception e) {
            logger.warn("Error trying to generate auto-completion for namespace type", e);
        }

        return new ICompletionProposal[0];
    }

    @Override
    public Image getImage() {
        return ForceImages.get(ForceImages.PACKAGE_ICON);
    }
}
