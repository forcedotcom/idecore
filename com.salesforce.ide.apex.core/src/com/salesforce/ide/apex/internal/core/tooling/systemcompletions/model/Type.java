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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.collections4.trie.PatriciaTrie;

public class Type {
    @XmlElement(name = "name", required = true)
    public String name;

    @XmlElement(name = "publicDeclarations", required = true)
    public PublicDeclaration publicDeclarations;

    // The tries for the different types of members that we want for auto-completion
    public PatriciaTrie<Constructor> constructorTrie;
    public PatriciaTrie<Method> methodTrie;
    public PatriciaTrie<Property> propertyTrie;

    void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {
        constructorTrie = new PatriciaTrie<>();
        methodTrie = new PatriciaTrie<>();
        propertyTrie = new PatriciaTrie<>();
    }

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if (publicDeclarations.constructors != null) {
            for (Constructor c : publicDeclarations.constructors) {
                constructorTrie.put(c.name, c);
            }
        }

        if (publicDeclarations.methods != null) {
            for (Method m : publicDeclarations.methods) {
                methodTrie.put(m.name, m);
            }
        }

        if (publicDeclarations.properties != null) {
            for (Property p : publicDeclarations.properties) {
                propertyTrie.put(p.name, p);
            }
        }
    }
}