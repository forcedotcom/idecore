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
package com.salesforce.ide.apex.core.tooling.systemcompletions;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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

    public static class Namespace {
        @XmlElement(name = "name", required = true)
        public String name;

        @XmlElement(name = "type", required = true)
        public List<Type> type;
    }

    public static class Type {
        @XmlElement(name = "name", required = true)
        public String name;

        @XmlElement(name = "publicDeclarations", required = true)
        public PublicDeclaration publicDeclarations;
    }

    /*
     * This is a mostly useless container lass. The only reason we have it was that on the server side, the JSON version was first implemented and it used a map data structure, which is good for JSON serialization but not good for XML serialization.
     * Instead of breaking that API, we left it for JSON response and wrapped it this way for XML response.
     */
    public static class PublicDeclaration {
        @XmlElement(name = "constructors", required = false)
        public List<Constructor> constructors;

        @XmlElement(name = "methods", required = false)
        public List<Method> methods;

        @XmlElement(name = "properties", required = false)
        public List<Property> properties;
    }

    public static class Constructor {
        @XmlElement(name = "name", required = true)
        public String name;

        @XmlElement(name = "parameters", required = false)
        public List<Parameter> parameters;
    }

    public static class Method {
        @XmlElement(name = "name", required = true)
        public String name;

        @XmlElement(name = "parameters", required = false)
        public List<Parameter> parameters;

        @XmlElement(name = "argTypes", required = false)
        public List<String> argTypes;

        @XmlElement(name = "returnType", required = true)
        public String returnType;

        @XmlElement(name = "isStatic", required = true)
        public boolean isStatic;
    }

    public static class Parameter {
        @XmlElement(name = "name", required = true)
        public String name;

        @XmlElement(name = "type", required = true)
        public String type;
    }

    public static class Property {
        @XmlElement(name = "name", required = true)
        public String name;
    }
}
