/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.core.internal.utils;

public interface XmlConstants {
    String XMLTYPE_QUERY_RESULT = "QueryResult";
    // Common elements and attributes
    String ATTR_NAME = "name";
    String ATTR_TYPE = "type";
    String ATTR_STATIC = "isStatic";
    String ATTR_RETURN_TYPE = "returnType";
    String ATTR_DOCUMENTATION = "documentation";
    String ATTR_DOC = "doc";
    String ATTR_IS_PRIMITIVE= "isPrimitive";
    
    String ELEM_NAMESPACE = "namespace";
    String ELEM_TYPE = "type";
    String ELEM_METHOD = "method";
    Object ELEM_DOCUMENTATION = "documentation";
    Object ELEM_DOC = "doc";
    Object ELEM_ARGUMENT = "argument";
    Object ELEM_PARAM = "param";
    String ELEM_CONSTRUCTOR = "constructor";
    String ELEM_ITEMS = "items";
    String ELEM_ITEM = "item";
    String ELEM_FIELD = "field";
    
    // resolve pre-defined keyword to runtime values
    String RUNTIME_VALUE_TYPE = "@ValueType";
    String RUNTIME_KEY_TYPE = "@KeyType";


}
