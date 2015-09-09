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
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Namespace;
import com.salesforce.ide.ui.internal.ForceImages;

/**
 * Offers suggestions for system types. Takes into consideration that the types in System namespace are implicitly
 * visible at the top-level.
 * 
 * @author nchen
 * 
 */
public class ApexSystemTypeProcessor extends ApexCompletionProcessor implements IContentAssistProcessor {
    public ApexSystemTypeProcessor() {}

    @VisibleForTesting
    public ApexSystemTypeProcessor(ApexCompletionUtils utils, Completions completions) {
        this.utils = utils;
        this.completions = completions;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        Collection<AbstractCompletionProposalDisplayable> suggestions = Lists.newArrayList();
        String prefix = null;
        ApexCompletionUtils.CompletionPrefix completionPrefix = null;

        try {
            if (!getUtil().hasInvokedNewOnSameLine(viewer, offset)) {
                Completions completions = getCompletions();

                if (completions != null) {
                    prefix = getUtil().getPrefix(viewer, offset);
                    completionPrefix = getUtil().determineFullyQualifiedNameFromPrefix(prefix);

                    if (completionPrefix.shouldSuggestNamespacedType()) {
                        String namespaceName = completionPrefix.segments.get(0);
                        String typePrefix = completionPrefix.segments.get(1);

                        Namespace namespace = (Namespace) completions.namespaceTrie.get(namespaceName);
                        if (namespace != null) {
                            suggestions = namespace.typeTrie.prefixMap(typePrefix).values();
                        }
                        return getUtil().createProposal(suggestions, typePrefix, offset, getImage());
                    } else if (completionPrefix.shouldSuggestTopLevelType()) { // provide only system type since that doesn't need to be fully qualified
                        String typePrefix = completionPrefix.segments.get(0);
                        Namespace systemNS = completions.getSystemNamespace();
                        suggestions = Lists.newArrayList(systemNS.typeTrie.prefixMap(typePrefix).values());
                        return getUtil().createProposal(suggestions, typePrefix, offset, getImage());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error trying to generate auto-completion for system type", e);
        }

        return new ICompletionProposal[0];
    }

    @Override
    public Image getImage() {
        return ForceImages.get(ForceImages.APEX_GLOBAL_CLASS);
    }
}
