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
package com.salesforce.ide.core.remote.metadata;

import com.salesforce.ide.core.internal.utils.Utils;
import com.sforce.soap.metadata.DescribeMetadataObject;

public class DescribeMetadataObjectExt {

    protected DescribeMetadataObject describeMetadataObject = null;

    public DescribeMetadataObjectExt(DescribeMetadataObject describeMetadataObject) {
        super();
        this.describeMetadataObject = describeMetadataObject;
    }

    public DescribeMetadataObject getDescribeMetadataObject() {
        return describeMetadataObject;
    }

    public void setDescribeMetadataObject(DescribeMetadataObject describeMetadataObject) {
        this.describeMetadataObject = describeMetadataObject;
    }

    public String getDirectoryName() {
        return describeMetadataObject.getDirectoryName();
    }

    public boolean isInFolder() {
        return describeMetadataObject.isInFolder();
    }

    public boolean isMetaFile() {
        return describeMetadataObject.isMetaFile();
    }

    public String[] getChildren() {
        return describeMetadataObject.getChildXmlNames();
    }

    public String getSuffix() {
        return describeMetadataObject.getSuffix();
    }

    public String getName() {
        return describeMetadataObject.getXmlName();
    }

    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("DescribeMetadataObjectExt ( ").append("componentName = ").append(getName()).append(TAB)
                .append("suffix = ").append(getSuffix()).append(TAB).append("directory = ").append(getDirectoryName())
                .append(TAB).append("in folder = ").append(isInFolder()).append(TAB).append("isMetaFile = ").append(
                    isMetaFile()).append(TAB).append("children = [");
        String[] children = getChildren();
        if (Utils.isNotEmpty(children)) {
            for (String child : children) {
                retValue.append(child).append(" ");
            }
        } else {
            retValue.append("n/a");
        }
        retValue.append("]");
        return retValue.toString();
    }
}
