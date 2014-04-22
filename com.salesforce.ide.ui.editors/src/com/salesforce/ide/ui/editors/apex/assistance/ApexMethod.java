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
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.XmlConstants;

public class ApexMethod {

    private static Logger logger = Logger.getLogger(ApexMethod.class);

    private final List<ApexArgument> arguments = new ArrayList<ApexArgument>();
    private String className = "";
    private String returnType;
    private String documentation = "";
    private String name;
    private boolean staticMethod;

    public ApexMethod(String className, Node methodNode) {
        this.className = className;
        loadMethod(methodNode);
    }

    public ApexMethod(String className, String name, String returnType, String isStatic, String documentation) {
    	this(className, name, returnType, "true".equalsIgnoreCase(isStatic) ? true : false, documentation);
    }


    public ApexMethod(String className, String name, String returnType, boolean isStatic, String documentation, ApexArgument... arguments) {
        this.className = className;
        this.name = name;
        this.returnType = returnType;
        this.staticMethod = isStatic;
        this.documentation = documentation;
        for (ApexArgument apexArgument : arguments) {
			this.arguments.add(apexArgument);
		}
    }
        
    public List<ApexArgument> getArguments() {
        return arguments;
    }

    public String getDocumentation() {
        return documentation;
    }

    public String getName() {
         return name;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((arguments == null) ? 0 : arguments.hashCode());
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((returnType == null) ? 0 : returnType.hashCode());
		result = prime * result + (staticMethod ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApexMethod other = (ApexMethod) obj;
		if (arguments == null) {
			if (other.arguments != null)
				return false;
		} else if (!arguments.equals(other.arguments))
			return false;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		if (staticMethod != other.staticMethod)
			return false;
		return true;
	}

	public String getReturnType() {
        return Utils.replaceColonToSurroundingGenericBlock(returnType);
    }

    public String getClassName() {
        return className;
    }

    public String getDisplayClassName() {
        // workaround because apex code objects are stored in all lowercase (how we're supplied as of 08/20/08)
        return Utils.capFirstLetterAndLetterAfterToken(className, ".", true);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isStaticMethod() {
        return staticMethod;
    }

    public void setStaticMethod(boolean staticMethod) {
        this.staticMethod = staticMethod;
    }

    private void loadMethod(Node methodNode) {
        name = methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_NAME).getNodeValue();

        if (logger.isDebugEnabled()) {
            logger.debug("Loading method '" + name + "'");
        }

        // capture return type value
        if (methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_RETURN_TYPE) != null) {
            returnType = methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_RETURN_TYPE).getNodeValue();
        }

        // capture static value
        if (methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_STATIC) != null) {
            staticMethod =
                    Boolean.valueOf(methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_STATIC).getNodeValue());
        }

        // capture doc value
        if (methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_DOC) != null) {
            documentation = methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_DOC).getNodeValue();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Loading attributes for method '" + name + "' with return type of '" + returnType + "'");
        }

        if (!methodNode.hasChildNodes()) {
            if (logger.isDebugEnabled()) {
                logger.debug("No params found for method '" + name + "'");
            }
            return;
        }

        NodeList params = methodNode.getChildNodes();
        for (int i = 0; i < params.getLength(); i++) {
            Node paramNode = params.item(i);
            if (paramNode.getNodeType() == Node.ELEMENT_NODE && XmlConstants.ELEM_PARAM.equals(paramNode.getNodeName())) {
                arguments.add(new ApexArgument(paramNode));
            }
        }
    }

    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("ApexMethod ( ").append("name = ").append(this.name).append(TAB).append("className = ").append(
            this.className).append(TAB).append("static = ").append(this.staticMethod).append(TAB).append(
            "returnType = ").append(this.returnType).append(TAB).append("doc = ").append(this.documentation)
                .append(TAB).append("params:");
        if (Utils.isNotEmpty(arguments)) {
            for (int i = 0; i < arguments.size(); i++) {
                retValue.append("\n      (" + (i + 1) + ") ").append(arguments.get(i).toString());
            }
        } else {
            retValue.append(" n/a ");
        }
        retValue.append(" )");
        return retValue.toString();
    }

}
