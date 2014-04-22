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
package com.salesforce.ide.core.internal.utils;

import com.sforce.soap.metadata.LogType;


/**
 * LIMITED USE!
 * This class should only be used to support OLD logging framework.
 * Deprecate this class as soon as metadata-api support new logging!
 *
 * @author fchang
 */
public class LoggingLevel {
    public static final String[] loggingLabels = { "None.", "Debug statements only.", "Debug and Database stats.",
            "Profiling (maximum verbosity)." };

    public static final LogType[] logTypes = { LogType.None, LogType.Debugonly, LogType.Db, LogType.Profiling, LogType.Callout };

    private int level = 0;

    public LoggingLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public LogType getLogType() {
        return logTypes[level];
    }

    public String getLevelText() {
        return loggingLabels[level];
    }

    public static String getLevelText(int level) {
        return loggingLabels[level];
    }
}
