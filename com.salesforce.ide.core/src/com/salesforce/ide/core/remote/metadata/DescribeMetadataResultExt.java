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

import java.util.Arrays;
import java.util.Comparator;

import com.salesforce.ide.core.internal.utils.Utils;
import com.sforce.soap.metadata.DescribeMetadataResult;

public class DescribeMetadataResultExt {

    protected DescribeMetadataResult describeMetadataResult = null;
    protected DescribeMetadataObjectExt[] describeMetadataObjectExts = null;
    private static Comparator<DescribeMetadataObjectExt> describeComparator =
            new Comparator<DescribeMetadataObjectExt>() {
                @Override
                public int compare(DescribeMetadataObjectExt o1, DescribeMetadataObjectExt o2) {
                    if (o1 == o2) {
                        return 0;
                    } else if (Utils.isEmpty(o1.getDescribeMetadataObject().getXmlName())
                            && Utils.isEmpty(o2.getDescribeMetadataObject().getXmlName())) {
                        return 0;
                    } else if (Utils.isNotEmpty(o1.getDescribeMetadataObject().getXmlName())
                            && Utils.isEmpty(o2.getDescribeMetadataObject().getXmlName())) {
                        return -1;
                    } else if (Utils.isEmpty(o1.getDescribeMetadataObject().getXmlName())
                            && Utils.isNotEmpty(o2.getDescribeMetadataObject().getXmlName())) {
                        return 1;
                    } else {
                        return o1.getDescribeMetadataObject().getXmlName().compareTo(
                            o2.getDescribeMetadataObject().getXmlName());
                    }
                }
            };

    public DescribeMetadataResultExt(DescribeMetadataResult describeMetadataResult) {
        super();
        this.describeMetadataResult = describeMetadataResult;
        sort();
    }

    public DescribeMetadataResult getDescribeMetadataResult() {
        return describeMetadataResult;
    }

    public void setDescribeMetadataResult(DescribeMetadataResult describeMetadataResult) {
        this.describeMetadataResult = describeMetadataResult;
        sort();
    }

    public DescribeMetadataObjectExt[] getMetadataObjects() {
        if (describeMetadataResult == null || Utils.isEmpty(describeMetadataResult.getMetadataObjects())) {
            return null;
        }

        if (describeMetadataObjectExts == null) {
            describeMetadataObjectExts =
                    new DescribeMetadataObjectExt[describeMetadataResult.getMetadataObjects().length];
            for (int i = 0; i < describeMetadataResult.getMetadataObjects().length; i++) {
                describeMetadataObjectExts[i] =
                        new DescribeMetadataObjectExt(describeMetadataResult.getMetadataObjects()[i]);
            }
        }

        sort();

        return describeMetadataObjectExts;
    }

    public DescribeMetadataObjectExt getMetadataObjects(int i) {
        if (describeMetadataResult == null || Utils.isEmpty(describeMetadataResult.getMetadataObjects())) {
            return null;
        }

        return new DescribeMetadataObjectExt(describeMetadataResult.getMetadataObjects()[i]);
    }

    public int getMetadataObjectCount() {
        return Utils.isNotEmpty(getMetadataObjects()) ? getMetadataObjects().length : 0;
    }

    public java.lang.String getOrganizationNamespace() {
        return describeMetadataResult.getOrganizationNamespace();
    }

    public boolean isPartialSaveAllowed() {
        return describeMetadataResult.isPartialSaveAllowed();
    }

    public boolean isTestRequired() {
        return describeMetadataResult.isTestRequired();
    }

    public void sort() {
        if (Utils.isNotEmpty(describeMetadataObjectExts)) {
            Arrays.sort(describeMetadataObjectExts, describeComparator);
        }
    }

    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("DescribeMetadataResultExt ( ").append("namespace = ").append(getOrganizationNamespace())
                .append(TAB).append("partial save allowed = ").append(isPartialSaveAllowed()).append(TAB).append(
                    "test required = ").append(isTestRequired()).append(TAB);

        if (Utils.isNotEmpty(getMetadataObjects())) {
            retValue.append("describeMetadataObjectExts = ");
            int cnt = 0;
            for (DescribeMetadataObjectExt describeMetadataObjectExt : getMetadataObjects()) {
                retValue.append("\n  (").append(++cnt).append(") ").append(describeMetadataObjectExt.toString());
            }
        } else {
            retValue.append("describeMetadataObjectExts = n/a");
        }
        retValue.append(" )");
        return retValue.toString();
    }

}
