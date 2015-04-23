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

import javax.xml.bind.annotation.XmlElement;

/**
 * This is a mostly useless container lass. The only reason we have it was that on the server side, the JSON version was
 * first implemented and it used a map data structure, which is good for JSON serialization but not good for XML
 * serialization (at least with our server side serializers). Instead of breaking that API, we left it for JSON response
 * and wrapped it this way for XML response.
 * 
 * @author nchen
 */
public class PublicDeclaration {
    @XmlElement(name = "constructors", required = false)
    public List<Constructor> constructors;

    @XmlElement(name = "methods", required = false)
    public List<Method> methods;

    @XmlElement(name = "properties", required = false)
    public List<Property> properties;
}