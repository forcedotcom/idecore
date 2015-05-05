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

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.StringUtils;

public class Namespace extends AbstractCompletionProposalDisplayable {
    @XmlElement(name = "name", required = true)
    public String name;

    @XmlElement(name = "type", required = true)
    public List<Type> type;

    public PatriciaTrie<AbstractCompletionProposalDisplayable> typeTrie;

    void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {
        typeTrie = new PatriciaTrie<>();
    }

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        name = StringUtils.capitalize(name);

        for (Type t : type) {
            typeTrie.put(t.name.toLowerCase(), t);
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