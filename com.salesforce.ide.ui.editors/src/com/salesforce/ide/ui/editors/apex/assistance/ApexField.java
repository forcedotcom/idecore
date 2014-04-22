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

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.XmlConstants;

/**
 * Apex field is equivalent to public instance var to Apex class.
 * 
 * @author fchang
 * 
 */
public class ApexField {
    private static Logger logger = Logger.getLogger(ApexField.class);

    private String className = "";
    private String name;
    private String type;

    public ApexField(String className, Node itemNode) {
        this.className = className;
        loadItem(itemNode);
    }

    private void loadItem(Node fieldNode) {
        name = fieldNode.getAttributes().getNamedItem(XmlConstants.ATTR_NAME).getNodeValue();
        type = fieldNode.getAttributes().getNamedItem(XmlConstants.ATTR_TYPE).getNodeValue();

        if (logger.isDebugEnabled()) {
            logger.debug("Loading field/instance var'" + name + "' with type '" + type + "' for class '" + className
                    + "'");
        }

    }

    public String getClassName() {
        return className;
    }

    public String getDisplayClassName() {
        // workaround because apex code objects are stored in all lowercase (how we're supplied as of 08/20/08)
        return Utils.capFirstLetterAndLetterAfterToken(className, ".", true);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return Utils.replaceColonToSurroundingGenericBlock(type);
    }

    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("ApexField ( ").append("name = ").append(this.name).append(TAB).append("type = ").append(
            this.type).append(TAB).append("className = ").append(this.className);
        retValue.append(" )");
        return retValue.toString();
    }


}
