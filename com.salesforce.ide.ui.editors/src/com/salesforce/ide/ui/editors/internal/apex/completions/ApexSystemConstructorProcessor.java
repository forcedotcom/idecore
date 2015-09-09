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

import static com.google.common.collect.Iterables.concat;

import java.util.Collection;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.swt.graphics.Image;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.AbstractCompletionProposalDisplayable;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Namespace;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Type;
import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.editor.imagesupport.ApexElementImageDescriptor;

/**
 * Suggests constructors if the user is in a "new" expression. Ideally, you would look to see the static type on the LHS
 * and only suggest completions that are subtypes of that type. However, that would require type hierarchy analysis,
 * which we don't have yet.
 * 
 * @author nchen
 * 
 */
public class ApexSystemConstructorProcessor extends ApexCompletionProcessor implements IContentAssistProcessor {
    public ApexSystemConstructorProcessor() {}

    @VisibleForTesting
    public ApexSystemConstructorProcessor(ApexCompletionUtils utils, Completions completions) {
        this.utils = utils;
        this.completions = completions;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        Iterable<AbstractCompletionProposalDisplayable> suggestions = Lists.newArrayList();
        String prefix = null;
        ApexCompletionUtils.CompletionPrefix completionPrefix = null;

        try {
            if (getUtil().hasInvokedNewOnSameLine(viewer, offset)) {
                Completions completions = getCompletions();

                if (completions != null) {
                    prefix = getUtil().getPrefix(viewer, offset);
                    completionPrefix = getUtil().determineFullyQualifiedNameFromPrefix(prefix);

                    if (completionPrefix.shouldSuggestTopLevelConstructor()) {
                        String typeName = completionPrefix.segments.get(0);

                        Namespace namespace = completions.getSystemNamespace();
                        Collection<AbstractCompletionProposalDisplayable> possibleTypes =
                                namespace.typeTrie.prefixMap(typeName).values();
                        for (AbstractCompletionProposalDisplayable possibleType : possibleTypes) {
                            Type type = (Type) possibleType;
                            suggestions =
                                    concat(suggestions, concat(type.constructorTrie.prefixMap(typeName).values()));
                        }
                        return getUtil().createProposal(suggestions, typeName, offset, getImage());
                    } else if (completionPrefix.shouldSuggestNamespacedConstructor()) {
                        String namespaceName = completionPrefix.segments.get(0);
                        String typeName = completionPrefix.segments.get(1);

                        Namespace namespace = (Namespace) completions.namespaceTrie.get(namespaceName);
                        if (namespace != null) {
                            Collection<AbstractCompletionProposalDisplayable> possibleTypes =
                                    namespace.typeTrie.prefixMap(typeName).values();
                            for (AbstractCompletionProposalDisplayable possibleType : possibleTypes) {
                                Type type = (Type) possibleType;
                                suggestions =
                                        concat(suggestions, concat(type.constructorTrie.prefixMap(typeName).values()));
                            }
                            return getUtil().createProposal(suggestions, typeName, offset, getImage());
                        }

                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error trying to generate auto-completion for system constructor method", e);
        }

        return new ICompletionProposal[0];
    }

    @Override
    public Image getImage() {
        int accessorFlags_JVM = Flags.AccPublic;
        int accessorFlags_JDT = JavaElementImageDescriptor.CONSTRUCTOR;

        ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_GLOBAL_METHOD);
        ApexElementImageDescriptor decoratedDesc =
                new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
        return ForceImages.get(ForceImages.APEX_GLOBAL_METHOD, accessorFlags_JVM, decoratedDesc);
    }

}
