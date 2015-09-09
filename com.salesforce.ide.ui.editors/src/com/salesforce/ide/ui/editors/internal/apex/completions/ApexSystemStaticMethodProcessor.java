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
import static com.google.common.collect.Iterables.filter;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.swt.graphics.Image;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.AbstractCompletionProposalDisplayable;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Method;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Namespace;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Type;
import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.editor.imagesupport.ApexElementImageDescriptor;

/**
 * Offers suggestions for static system methods.
 * 
 * @author nchen
 * 
 */
public class ApexSystemStaticMethodProcessor extends ApexCompletionProcessor implements IContentAssistProcessor {
    private final class StaticMethodPredicate implements Predicate<AbstractCompletionProposalDisplayable> {
        @Override
        public boolean apply(AbstractCompletionProposalDisplayable input) {
            assert input instanceof Method;
            return ((Method) input).isStatic;
        }
    }

    public ApexSystemStaticMethodProcessor() {}

    @VisibleForTesting
    public ApexSystemStaticMethodProcessor(ApexCompletionUtils sUtils, Completions completions) {
        this.utils = sUtils;
        this.completions = completions;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        Iterable<AbstractCompletionProposalDisplayable> suggestions = Lists.newArrayList();
        String prefix = null;
        ApexCompletionUtils.CompletionPrefix completionPrefix = null;

        try {
            if (!getUtil().hasInvokedNewOnSameLine(viewer, offset)) {
                Completions completions = getCompletions();

                if (completions != null) {
                    prefix = getUtil().getPrefix(viewer, offset);
                    completionPrefix = getUtil().determineFullyQualifiedNameFromPrefix(prefix);

                    if (completionPrefix.shouldSuggestTopLevelMember()) {
                        String typeName = completionPrefix.segments.get(0);
                        String methodPrefix = completionPrefix.segments.get(1);

                        Namespace namespace = completions.getSystemNamespace();
                        Type type = (Type) namespace.typeTrie.get(typeName);
                        if (type != null) {
                            suggestions =
                                    filter(concat(type.methodTrie.prefixMap(methodPrefix).values()),
                                        new StaticMethodPredicate());
                            return getUtil().createProposal(suggestions, methodPrefix, offset, getImage());
                        }
                    } else if (completionPrefix.shouldSuggestNamespacedMember()) {
                        String namespaceName = completionPrefix.segments.get(0);
                        String typeName = completionPrefix.segments.get(1);
                        String methodPrefix = completionPrefix.segments.get(2);

                        Namespace namespace = (Namespace) completions.namespaceTrie.get(namespaceName);
                        if (namespace != null) {
                            Type type = (Type) namespace.typeTrie.get(typeName);
                            if (type != null) {
                                suggestions =
                                        filter(concat(type.methodTrie.prefixMap(methodPrefix).values()),
                                            new StaticMethodPredicate());
                                return getUtil().createProposal(suggestions, methodPrefix, offset, getImage());
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error trying to generate auto-completion for system static method", e);
        }

        return new ICompletionProposal[0];
    }

    @Override
    public Image getImage() {
        int accessorFlags_JVM = Flags.AccStatic;
        int accessorFlags_JDT = JavaElementImageDescriptor.STATIC;

        ImageDescriptor desc = ForceImages.getDesc(ForceImages.APEX_GLOBAL_METHOD);
        ApexElementImageDescriptor decoratedDesc =
                new ApexElementImageDescriptor(desc, accessorFlags_JDT, APEX_ICON_SIZE);
        return ForceImages.get(ForceImages.APEX_GLOBAL_METHOD, accessorFlags_JVM, decoratedDesc);
    }
}
