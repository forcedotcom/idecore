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
package com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model;

import java.util.ArrayList;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class Type extends AbstractCompletionProposalDisplayable {
    @XmlElement(name = "name", required = true)
    public String name;

    @XmlElement(name = "publicDeclarations", required = true)
    public PublicDeclaration publicDeclarations;

    // The tries for the different types of members that we want for auto-completion
    public PatriciaTrie<ArrayList<AbstractCompletionProposalDisplayable>> constructorTrie;
    public PatriciaTrie<ArrayList<AbstractCompletionProposalDisplayable>> methodTrie;
    public PatriciaTrie<AbstractCompletionProposalDisplayable> propertyTrie;

    void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {
        constructorTrie = new PatriciaTrie<>();
        methodTrie = new PatriciaTrie<>();
        propertyTrie = new PatriciaTrie<>();
    }

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        name = StringUtils.capitalize(name);

        if (publicDeclarations.constructors != null) {
            for (Constructor c : publicDeclarations.constructors) {
                String lowerCase = c.name.toLowerCase();
                ArrayList<AbstractCompletionProposalDisplayable> list = constructorTrie.get(lowerCase);
                if (list == null) {
                    list = Lists.newArrayList();
                }
                list.add(c);
                constructorTrie.put(lowerCase, list);
            }
        }

        if (publicDeclarations.methods != null) {
            for (Method m : publicDeclarations.methods) {
                String lowerCase = m.name.toLowerCase();
                ArrayList<AbstractCompletionProposalDisplayable> list = methodTrie.get(lowerCase);
                if (list == null) {
                    list = Lists.newArrayList();
                }
                list.add(m);
                methodTrie.put(lowerCase, list);
            }
        }

        if (publicDeclarations.properties != null) {
            for (Property p : publicDeclarations.properties) {
                propertyTrie.put(p.name.toLowerCase(), p);
            }
        }
    }

    @Override
    public String getReplacementString() {
        return name;
    }

    @Override
    public String getDisplayString() {
        return name;
    }
}