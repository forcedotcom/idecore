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
package com.salesforce.ide.api.metadata.types;

import java.util.Map;

import com.sforce.soap.metadata.LogCategory;

/**
 *
 * extend LogCategory to represent external/ui value
 *
 * @author fchang
 */
public class LogCategoryExt {

    private final LogCategory logCategory;
    private final String externalVal;
    private static Map<String, LogCategoryExt> map = new java.util.HashMap<String, LogCategoryExt>();

    protected LogCategoryExt(LogCategory logCategory, java.lang.String externalVal) {
        this.logCategory = logCategory;
        this.externalVal = externalVal;
        map.put(externalVal, this);
    }

    public static final LogCategoryExt Db = new LogCategoryExt(LogCategory.Db, "Database");
    public static final LogCategoryExt Workflow = new LogCategoryExt(LogCategory.Workflow, "Workflow");
    public static final LogCategoryExt Validation = new LogCategoryExt(LogCategory.Validation, "Validation");
    public static final LogCategoryExt Callout = new LogCategoryExt(LogCategory.Callout, "Callout");
    public static final LogCategoryExt Apex_code = new LogCategoryExt(LogCategory.Apex_code, "Apex Code");
    public static final LogCategoryExt Apex_profiling = new LogCategoryExt(LogCategory.Apex_profiling, "Apex Profiling");

    public String getInternalValue() {
        return this.logCategory.name();
    }

    public String getExternalValue() {
        return this.externalVal;
    }

    public LogCategory getLogCategory() {
        return this.logCategory;
    }

    public static LogCategoryExt fromExternalValue(String externalVal) {
        LogCategoryExt logCategoryExt = map.get(externalVal);
        if (logCategoryExt == null)
            throw new java.lang.IllegalStateException();
        return logCategoryExt;
    }


}
