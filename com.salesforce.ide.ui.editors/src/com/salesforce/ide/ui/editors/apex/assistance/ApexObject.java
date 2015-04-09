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
package com.salesforce.ide.ui.editors.apex.assistance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.XmlConstants;

public class ApexObject {

    private static Logger logger = Logger.getLogger(ApexObject.class);

    private Collection<ApexMethod> methods = new HashSet<>();
    private List<ApexItem> items = new ArrayList<>();
    private List<ApexField> fields = new ArrayList<>();    
    private String namespace = null;
    private String name = null;
    private boolean isPrimitive;
   
    public ApexObject(Node node) {
        loadObject(node);
    }

    public ApexObject(String namespace, Node node) {
        this.namespace = namespace;
        loadObject(node);
    }

    public ApexObject(String name, String namespace, Node node) {
        this.name = name;
        this.namespace = namespace;
        loadObject(node);
    }

    public ApexObject(String name, List<ApexMethod> methods) {
        this.name = name;
        this.methods.addAll(methods);
    }

    //   M E T H O D S
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public Collection<ApexMethod> getMethods() {
        return methods;
    }
    
    public List<ApexItem> getItems() {
        return items;
    }
    
    public List<ApexField> getFields() {
        return fields;
    }
    
    public boolean isPrimitive() {
        return isPrimitive;
    }
    
    public void loadObject(Node typeNode) {
        name = typeNode.getAttributes().getNamedItem(XmlConstants.ATTR_NAME).getNodeValue();
        Node isPrimitiveAttr = typeNode.getAttributes().getNamedItem(XmlConstants.ATTR_IS_PRIMITIVE);
        isPrimitive = Utils.isNotEmpty(isPrimitiveAttr) && "true".equalsIgnoreCase(isPrimitiveAttr.getNodeValue())? true : false;
        
        if (logger.isDebugEnabled()) {
            logger.debug("Loading built-in type '" + name + "', isPrimitive?" + isPrimitive);
        }

        NodeList subTypeNodes = typeNode.getChildNodes();
        for (int i = 0; i < subTypeNodes.getLength(); i++) {
            Node subTypeNode = subTypeNodes.item(i);
            if (subTypeNode.getNodeType() == Node.ELEMENT_NODE
                    && XmlConstants.ELEM_METHOD.equalsIgnoreCase(subTypeNode.getNodeName())) {
                addMethod(subTypeNode);
            } else if (subTypeNode.getNodeType() == Node.ELEMENT_NODE
                    && XmlConstants.ELEM_ITEMS.equalsIgnoreCase(subTypeNode.getNodeName())) {
                addItems(subTypeNode);
            } else if (subTypeNode.getNodeType() == Node.ELEMENT_NODE
                    && XmlConstants.ELEM_FIELD.equalsIgnoreCase(subTypeNode.getNodeName())) {
                addFields(subTypeNode);
            } else if (subTypeNode.getNodeType() == Node.ELEMENT_NODE
                    && XmlConstants.ELEM_CONSTRUCTOR.equalsIgnoreCase(subTypeNode.getNodeName())) {
                //                addConstructor(methodNode);
            }
        }
    }

    public void addMethod(Node methodNode) {
        try {
            ApexMethod method = new ApexMethod(name, methodNode);
			addMethod(method);
        } catch (Exception e) {
            String methodName = "unknown";
            try {
                methodName = methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_NAME).getNodeValue();
            } catch (Exception e1) {}
            logger.warn("Unable to load method '" + methodName + "' for built-in type '" + name + "'", e);
        }
    }

	public void addMethod(ApexMethod method) {
		methods.add(method);
	}

    public void addItems(Node itemsNode) {
        try {
            NodeList itemNodes = itemsNode.getChildNodes();
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE
                        && XmlConstants.ELEM_ITEM.equalsIgnoreCase(itemNode.getNodeName())) {
                    items.add(new ApexItem(name, itemNode));
                }
            }
        } catch (Exception e) {
            String methodName = "unknown";
            try {
                methodName = itemsNode.getAttributes().getNamedItem(XmlConstants.ATTR_NAME).getNodeValue();
            } catch (Exception e1) {}
            logger.warn("Unable to load method '" + methodName + "' for built-in type '" + name + "'", e);
        }
    }

    private void addFields(Node fieldNode) {
        try {
            fields.add(new ApexField(name, fieldNode));
        } catch (Exception e) {
            String fieldName = "unknown";
            try {
                fieldName = fieldNode.getAttributes().getNamedItem(XmlConstants.ATTR_NAME).getNodeValue();
            } catch (Exception e1) {}
            logger.warn("Unable to load method '" + fieldName + "' for built-in type '" + name + "'", e);
        }
    }

    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("ApexObject ( ").append("name = ").append(this.name).append(TAB).append("namespace = ").append(
            this.namespace).append(TAB).append("methods:");
        if (Utils.isNotEmpty(methods)) {
        	for (ApexMethod method : this.methods) {
                retValue.append("\n").append(method.toString());
            }
        } else {
            retValue.append(" n/a ");
        }
        retValue.append(TAB).append("items:");
        if (Utils.isNotEmpty(items)) {
            for (int i = 0; i < items.size(); i++) {
                retValue.append("\n  (" + (i + 1) + ") ").append(items.get(i).toString());
            }
        } else {
            retValue.append(" n/a ");
        }
        retValue.append(TAB).append("fields:");
        if (Utils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                retValue.append("\n  (" + (i + 1) + ") ").append(fields.get(i).toString());
            }
        } else {
            retValue.append(" n/a ");
        }
        retValue.append(" )");
        return retValue.toString();
    }

}
