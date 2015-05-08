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

import org.apache.commons.lang3.StringUtils;

public class Constructor extends AbstractCompletionProposalDisplayable {
    @XmlElement(name = "name", required = true)
    public String name;

    @XmlElement(name = "parameters", required = false)
    public List<Parameter> parameters;

    @Override
    public String getReplacementString() {
        if (StringUtils.isEmpty(name))
            return null;

        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("()");
        return sb.toString();
    }

    @Override
    public String getDisplayString() {
        if (StringUtils.isEmpty(name))
            return null;

        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append('(');
        if (parameters != null) {
            sb.append(StringUtils.join(parameters, ","));
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public int cursorPosition() {
        if (parameters == null || parameters.size() == 0) {
            return getReplacementString().length();
        } else {
            // Position inside the ()
            return getReplacementString().length() - 1;
        }
    }

}