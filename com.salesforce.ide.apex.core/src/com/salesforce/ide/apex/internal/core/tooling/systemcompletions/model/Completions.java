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
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections4.trie.PatriciaTrie;

/**
 * This is the POJO that maps the response from services/data/vXX.0/tooling/completions?type=apex Hierarchy is
 * Completions --->* Namespace --->* Type ---> PublicDeclaration --->* {constructors, methods, properties}
 * 
 * @author nchen
 * 
 */
@XmlRootElement
public class Completions {
    @XmlElement(name = "namespace", required = true)
    public List<Namespace> namespace;

    public PatriciaTrie<AbstractCompletionProposalDisplayable> namespaceTrie;

    void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {
        namespaceTrie = new PatriciaTrie<>();
    }

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        for (Namespace ns : namespace) {
            namespaceTrie.put(ns.name, ns);
        }
    }
}
