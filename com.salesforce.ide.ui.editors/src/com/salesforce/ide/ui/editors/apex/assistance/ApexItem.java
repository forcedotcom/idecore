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
 * Apex item is equivalent to Enum items.
 * @author fchang
 *
 */
public class ApexItem {
    private static Logger logger = Logger.getLogger(ApexItem.class);
    
    private String className = "";
    private String name;
    
    public ApexItem(String className, Node itemNode) {
        this.className = className;
        loadItem(itemNode);
    }

    private void loadItem(Node itemNode) {
        name = itemNode.getAttributes().getNamedItem(XmlConstants.ATTR_NAME).getNodeValue();

        if (logger.isDebugEnabled()) {
            logger.debug("Loading item/enum '" + name + "' for class '" + className + "'");
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

    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("ApexItem ( ").append("name = ").append(this.name).append(TAB).append("className = ").append(
            this.className);
        retValue.append(" )");
        return retValue.toString();
    }


}
