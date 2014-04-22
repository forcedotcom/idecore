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

import com.salesforce.ide.core.internal.utils.XmlConstants;

/**
 *
 * Apex static subtype.
 *
 * @author fchang
 */
public class ApexSubType {
	private static Logger logger = Logger.getLogger(ApexSubType.class);
	private String parentType = "";
	private String documentation = "";
	private String name;

	public ApexSubType(String parentType, Node subTypeNode) {
		this.parentType = parentType;
		init(subTypeNode);
	}

	public String getDocumentation() {
		return documentation;
	}

	public String getName() {
		return name;
	}

	public String getClassName() {
		return parentType;
	}

	private void init(Node methodNode) {
		name = methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_NAME).getNodeValue();
		if (methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_DOC) != null) {
			documentation = methodNode.getAttributes().getNamedItem(XmlConstants.ATTR_DOC).getNodeValue();
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Loading attributes for static subtype '" + name + "' with doc info of '" + documentation
					+ "'");
		}

	}
}
