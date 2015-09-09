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

import static com.google.common.collect.Iterables.filter;

import java.util.Collection;

import apex.jorje.semantic.symbol.type.GenericTypeInfo;
import apex.jorje.semantic.symbol.type.TypeInfo;
import apex.jorje.semantic.symbol.type.visitor.TypeInfoVisitor;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.AbstractCompletionProposalDisplayable;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Method;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Namespace;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Type;
import com.salesforce.ide.ui.editors.internal.apex.completions.ApexCompletionUtils.CompletionPrefix;

/**
 * Some utilities for working with TypeInfos and mapping them to the completion types. This should not be necessary once
 * we consolidate both to the same format.
 * 
 * @author nchen
 * 
 */
public class TypeInfoUtil {
    static final class NonStaticMethodPredicate implements Predicate<AbstractCompletionProposalDisplayable> {
        @Override
        public boolean apply(AbstractCompletionProposalDisplayable input) {
            assert input instanceof Method;
            return !((Method) input).isStatic;
        }
    }

    /**
     * Do not base any decisions on the design of this class. The TypeInfo hierarchy is going to be revamped
     * drastically.
     * 
     * @author nchen
     * 
     */
    static final class SystemsInstanceMembersCompletionSuggestor extends
            TypeInfoVisitor.Default<Collection<AbstractCompletionProposalDisplayable>> {
        private final String memberPrefix;
        private final Completions completions;

        public SystemsInstanceMembersCompletionSuggestor(String memberPrefix, Completions completions) {
            this.memberPrefix = memberPrefix;
            this.completions = completions;
        }

        @Override
        protected Collection<AbstractCompletionProposalDisplayable> _default(TypeInfo typeInfo) {
            return suggestCompletions(typeInfo.getApexName());
        }

        @Override
        public Collection<AbstractCompletionProposalDisplayable> visit(GenericTypeInfo typeInfo) {
            return suggestCompletions(typeInfo.getUnreifiedType().getApexName());
        }

        private Collection<AbstractCompletionProposalDisplayable> suggestCompletions(String apexName) {
            Collection<AbstractCompletionProposalDisplayable> suggestions = Lists.newArrayList();

            CompletionPrefix completionPrefix = new CompletionPrefix(apexName);
            if (completions != null && completionPrefix.shouldSuggestTopLevelType()) {
                String typeName = completionPrefix.segments.get(0);
                Namespace systemNS = completions.getSystemNamespace();

                Type type = (Type) systemNS.typeTrie.get(typeName);
                if (type != null) {
                    Iterable<AbstractCompletionProposalDisplayable> flattened =
                            Iterables.concat(type.methodTrie.prefixMap(memberPrefix).values());
                    suggestions.addAll(Lists.newArrayList(filter(flattened, new NonStaticMethodPredicate())));
                    suggestions.addAll(type.propertyTrie.prefixMap(memberPrefix).values());
                }

            } else if (completionPrefix.shouldSuggestNamespacedType()) {
                String namespaceName = completionPrefix.segments.get(0);
                String typeName = completionPrefix.segments.get(1);

                Namespace namespace = (Namespace) completions.namespaceTrie.get(namespaceName);
                if (namespace != null) {
                    Type type = (Type) namespace.typeTrie.get(typeName);
                    if (type != null) {
                        Iterable<AbstractCompletionProposalDisplayable> flattened =
                                Iterables.concat(type.methodTrie.prefixMap(memberPrefix).values());
                        suggestions.addAll(Lists.newArrayList(filter(flattened, new NonStaticMethodPredicate())));
                        suggestions.addAll(type.propertyTrie.prefixMap(memberPrefix).values());
                    }
                }

            }
            return suggestions;
        }

    }
}
