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
package com.salesforce.ide.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Utils;

public class ApexTrigger extends Component {
    private static final Logger logger = Logger.getLogger(ApexTrigger.class);

    private String objectName = null;
    private List<String> operationOptions = new ArrayList<>();
    private List<String> operations = new ArrayList<>();

    public ApexTrigger() {}

    public List<String> getOperationOptions() {
        return operationOptions;
    }

    public void setOperationOptions(List<String> operationOptions) {
        this.operationOptions = operationOptions;
    }

    public List<String> getOperations() {
        return operations;
    }

    public void setOperations(List<String> operations) {
        this.operations = operations;
    }

    public void addOperation(String operation) {
        operations.add(operation);
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getNewBodyFromTemplateString() {
        String templateString = getDefaultTemplateString();
        if (Utils.isNotEmpty(objectName)) {
            templateString = templateString.replace("@@OBJECT@@", objectName);
        }

        if (Utils.isNotEmpty(getOperationString())) {
            templateString = templateString.replace("@@OPERATIONS@@", getOperationString());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Generate the following body from template:\n" + templateString);
        }
        return templateString;
    }

    private String getOperationString() {
        StringBuffer strBuff = new StringBuffer();

        int tabs = strBuff.length() / 4;
        String tabString = "";
        for (int i = 0; i < tabs; i++) {
            tabString += "\t";
        }

        for (int i = 0; i < operations.size(); i++) {
            String operation = operations.get(i);

            // for operations per line
            if ((i + 1) % 4 == 0) {
                strBuff.append("\n");
                strBuff.append(tabString);
            }

            // add operation string, and ',' if not last
            strBuff.append(operation);
            if (i != (operations.size() - 1)) {
                strBuff.append(", ");
            }
        }
        return strBuff.toString();
    }
}
