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
package com.salesforce.ide.ui.internal.utils;

import java.util.ArrayList;
import java.util.List;

import com.salesforce.ide.api.metadata.types.ReportTypeCategory;

/**
 * This class is used to map internal report type category returned from server to external translatable ones.
 *
 * @author fchang
 */
public class ReportTypeCategoryExt {

    private static final String REPORT_TYPE_CATEGORY = "ReportTypeCategory";
    private static List<ReportTypeCategoryExt> list = new ArrayList<>();

    static {
        for (ReportTypeCategory reportTypeCategory : ReportTypeCategory.values()) {
            list.add(new ReportTypeCategoryExt(reportTypeCategory, UIMessages.getString(REPORT_TYPE_CATEGORY + "."
                    + reportTypeCategory.value())));
        }
    }

    public static List<String> getUiDisplayNames() {
        List<String> uiDisplayNames = new ArrayList<>();
        for (ReportTypeCategoryExt categoryExt : list) {
            uiDisplayNames.add(categoryExt.getUiDisplayName());
        }
        return uiDisplayNames;
    }

    public static ReportTypeCategory getReportTypeCategory(int index) {
        ReportTypeCategoryExt categoryExt = list.get(index);
        return categoryExt.getReportTypeCategory();
    }

    private final ReportTypeCategory reportTypeCategory;
    private final String uiDisplayName;

    private ReportTypeCategoryExt(ReportTypeCategory reportTypeCategory, String uiDisplayName) {
        this.reportTypeCategory = reportTypeCategory;
        this.uiDisplayName = uiDisplayName;
    }

    public String getUiDisplayName() {
        return uiDisplayName;
    }

    public ReportTypeCategory getReportTypeCategory() {
        return reportTypeCategory;
    }

}
