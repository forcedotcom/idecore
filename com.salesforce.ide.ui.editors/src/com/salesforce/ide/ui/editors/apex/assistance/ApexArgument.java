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

import org.w3c.dom.Node;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.XmlConstants;

public class ApexArgument {

    private String name = "param";
    private String type = "Unknown";
    private String documentation = "";

    public ApexArgument(Node paramNode) {
        loadParam(paramNode);
    }

    public ApexArgument(String name, String type, String documentation) {
    	this.name = name;
    	this.type = type;
    	this.documentation = documentation;
    }
    
    private void loadParam(Node paramNode) {    	
        if (paramNode.getAttributes().getNamedItem(XmlConstants.ATTR_TYPE) != null) {
            type = paramNode.getAttributes().getNamedItem(XmlConstants.ATTR_TYPE).getNodeValue();
        }

        if (paramNode.getAttributes().getNamedItem(XmlConstants.ATTR_NAME) != null) {
            name = paramNode.getAttributes().getNamedItem(XmlConstants.ATTR_NAME).getNodeValue();
        } else {
            name = type;
        }

        if (paramNode.getAttributes().getNamedItem(XmlConstants.ATTR_DOC) != null) {
            documentation = paramNode.getAttributes().getNamedItem(XmlConstants.ATTR_DOC).getNodeValue();
        }
    }


    public String getType() {
        return Utils.replaceColonToSurroundingGenericBlock(type);
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("ApexArgument ( ").append("name = ").append(this.name).append(TAB).append("type = ").append(
            this.type).append(TAB).append("doc = ").append(this.documentation).append(" )");
        return retValue.toString();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ApexArgument other = (ApexArgument) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
    
}
