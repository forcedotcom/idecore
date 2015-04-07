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

import com.sforce.soap.metadata.LogCategoryLevel;

/**
 * 
 * extend LogCategoryLevel to represent None value externally.
 * 
 * @author fchang
 */
public final class LogCategoryLevelExt {
    private final String externalVal;
    private final LogCategoryLevel logCategoryLevel;
    private final static Map<String, LogCategoryLevelExt> map = new java.util.HashMap<>();

    protected LogCategoryLevelExt(LogCategoryLevel logCategoryLevel, String externalVal) {
        this.logCategoryLevel = logCategoryLevel;
        this.externalVal = externalVal;
        map.put(externalVal, this);
    }

    public static final LogCategoryLevelExt Finest = new LogCategoryLevelExt(LogCategoryLevel.Finest, "Finest");
    public static final LogCategoryLevelExt Finer = new LogCategoryLevelExt(LogCategoryLevel.Finer, "Finer");
    public static final LogCategoryLevelExt Fine = new LogCategoryLevelExt(LogCategoryLevel.Fine, "Fine");
    public static final LogCategoryLevelExt Debug = new LogCategoryLevelExt(LogCategoryLevel.Debug, "Debug");
    public static final LogCategoryLevelExt Info = new LogCategoryLevelExt(LogCategoryLevel.Info, "Info");
    public static final LogCategoryLevelExt Warn = new LogCategoryLevelExt(LogCategoryLevel.Warn, "Warn");
    public static final LogCategoryLevelExt Error = new LogCategoryLevelExt(LogCategoryLevel.Error, "Error");
    public static final LogCategoryLevelExt None = new LogCategoryLevelExt(null, "None");

    public String getInternalValue() {
        return this.logCategoryLevel == null ? null : this.logCategoryLevel.name();
    }

    public String getExternalValue() {
        return this.externalVal;
    }

    public LogCategoryLevel getLogCategoryLevel() {
        return this.logCategoryLevel;
    }

    public static LogCategoryLevelExt fromExternalValue(String externalVal) {
        LogCategoryLevelExt logCategoryExt = map.get(externalVal);
        if (logCategoryExt == null)
            throw new java.lang.IllegalStateException();
        return logCategoryExt;
    }

    // Auto-generated using Eclipse. Please don't modify by hand unless there is a good reason and please regenerate if you add/remove fields

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((externalVal == null) ? 0 : externalVal.hashCode());
        result = prime * result + ((logCategoryLevel == null) ? 0 : logCategoryLevel.hashCode());
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
        LogCategoryLevelExt other = (LogCategoryLevelExt) obj;
        if (externalVal == null) {
            if (other.externalVal != null)
                return false;
        } else if (!externalVal.equals(other.externalVal))
            return false;
        if (logCategoryLevel != other.logCategoryLevel)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LogCategoryLevelExt [externalVal=" + externalVal + ", logCategoryLevel=" + logCategoryLevel + "]";
    }
}
