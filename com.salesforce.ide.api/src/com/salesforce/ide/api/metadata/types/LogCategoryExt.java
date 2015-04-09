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
    private final LogCategoryLevelExt defaultLogCategoryLevel;
    private final String externalVal;
    private static Map<String, LogCategoryExt> map = new java.util.HashMap<>();

    protected LogCategoryExt(LogCategory logCategory, LogCategoryLevelExt defaultLogCategoryLevel, String externalVal) {
        this.logCategory = logCategory;
        this.externalVal = externalVal;
        this.defaultLogCategoryLevel = defaultLogCategoryLevel;
        map.put(externalVal, this);
    }

    public static final LogCategoryExt Db = new LogCategoryExt(LogCategory.Db, LogCategoryLevelExt.Info, "Database");
    public static final LogCategoryExt Workflow = new LogCategoryExt(LogCategory.Workflow, LogCategoryLevelExt.Info,
            "Workflow");
    public static final LogCategoryExt Validation = new LogCategoryExt(LogCategory.Validation,
            LogCategoryLevelExt.Info, "Validation");
    public static final LogCategoryExt Callout = new LogCategoryExt(LogCategory.Callout, LogCategoryLevelExt.Info,
            "Callout");
    public static final LogCategoryExt Apex_code = new LogCategoryExt(LogCategory.Apex_code, LogCategoryLevelExt.Debug,
            "Apex Code");
    public static final LogCategoryExt Apex_profiling = new LogCategoryExt(LogCategory.Apex_profiling,
            LogCategoryLevelExt.Info, "Apex Profiling");
    public static final LogCategoryExt Visualforce = new LogCategoryExt(LogCategory.Visualforce,
            LogCategoryLevelExt.Info, "Visualforce");
    public static final LogCategoryExt System = new LogCategoryExt(LogCategory.System, LogCategoryLevelExt.Debug,
            "System");

    public String getInternalValue() {
        return this.logCategory.name();
    }

    public String getExternalValue() {
        return this.externalVal;
    }

    public LogCategory getLogCategory() {
        return this.logCategory;
    }

    public LogCategoryLevelExt getDefaultLogCategoryLevel() {
        return defaultLogCategoryLevel;
    }

    public static LogCategoryExt fromExternalValue(String externalVal) {
        LogCategoryExt logCategoryExt = map.get(externalVal);
        if (logCategoryExt == null)
            throw new java.lang.IllegalStateException();
        return logCategoryExt;
    }

    // Auto-generated using Eclipse. Please don't modify by hand unless there is a good reason and please regenerate if you add/remove fields

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultLogCategoryLevel == null) ? 0 : defaultLogCategoryLevel.hashCode());
        result = prime * result + ((externalVal == null) ? 0 : externalVal.hashCode());
        result = prime * result + ((logCategory == null) ? 0 : logCategory.hashCode());
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
        LogCategoryExt other = (LogCategoryExt) obj;
        if (defaultLogCategoryLevel == null) {
            if (other.defaultLogCategoryLevel != null)
                return false;
        } else if (!defaultLogCategoryLevel.equals(other.defaultLogCategoryLevel))
            return false;
        if (externalVal == null) {
            if (other.externalVal != null)
                return false;
        } else if (!externalVal.equals(other.externalVal))
            return false;
        if (logCategory != other.logCategory)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LogCategoryExt [logCategory=" + logCategory + ", defaultLogCategoryLevel=" + defaultLogCategoryLevel
                + ", externalVal=" + externalVal + "]";
    }
}
