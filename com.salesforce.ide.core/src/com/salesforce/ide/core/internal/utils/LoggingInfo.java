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

import java.util.HashMap;
import java.util.Map;

import com.salesforce.ide.api.metadata.types.LogCategoryExt;
import com.salesforce.ide.api.metadata.types.LogCategoryLevelExt;
import com.sforce.soap.metadata.LogInfo;

/**
 * 
 * This class should be stateless. The logging state should be stored in project preference thru LoggingService. This
 * class provides utilities to coordinate UI display and preference storage; as well as enforce type safety. Since this
 * class is used to support UI operation; therefore, it only supports ONE LogInfo instead of ARRAY of LogInfo.
 * 
 * @author fchang
 */
public class LoggingInfo {
    /** Support/control drop-down combo content */
    private static final LogCategoryExt[] categoryExts = { LogCategoryExt.Db, LogCategoryExt.Workflow,
            LogCategoryExt.Validation, LogCategoryExt.Callout, LogCategoryExt.Apex_code, LogCategoryExt.Apex_profiling,
            LogCategoryExt.Visualforce, LogCategoryExt.System };

    private static final Map<LogCategoryExt, LogCategoryLevelExt[]> loggingMap =
            new HashMap<>();

    static LogCategoryLevelExt[] generateFreshLevels() {
        return new LogCategoryLevelExt[] { LogCategoryLevelExt.None, LogCategoryLevelExt.Error,
                LogCategoryLevelExt.Warn, LogCategoryLevelExt.Info, LogCategoryLevelExt.Debug,
                LogCategoryLevelExt.Fine, LogCategoryLevelExt.Finer, LogCategoryLevelExt.Finest };
    }

    static {
        for (LogCategoryExt category : categoryExts) {
            loggingMap.put(category, generateFreshLevels());
        }
    }

    // Enum for features that are enabled for new logging framework. This is used as namespace for storing logging setting in project pref.
    public enum SupportedFeatureEnum {
        RunTest, ExecuteAnonymous
    }

    public static LogCategoryExt[] getLogCategories() {
        return categoryExts;
    }

    /**
     * State of supporting log category level range for scale swt.
     * 
     * @param categoryExt
     * @return
     */
    public static int getLoggingLevelRange(LogCategoryExt categoryExt) {
        return loggingMap.get(categoryExt).length;
    }

    /**
     * Used by UI to convert scale selection index and combo selection to corresponding LoggingInfo value.
     * 
     * @param scale
     * @param comboSelection
     * @return
     */
    public static LoggingInfo getLoggingInfo(int scale, String comboSelection) {
        LogCategoryExt categoryExt = LogCategoryExt.fromExternalValue(comboSelection);
        LogCategoryLevelExt levelExt = getLogCategoryLevelFrom(categoryExt, scale);
        return new LoggingInfo(categoryExt, levelExt);
    }

    /**
     * Used by UI to convert selection index to corresponding LogCategoryLevel value.
     * 
     * @param categoryExt
     * @param selectionIndex
     * @return
     */
    public static LogCategoryLevelExt getLogCategoryLevelFrom(LogCategoryExt categoryExt, int selectionIndex) {
        return loggingMap.get(categoryExt)[selectionIndex];
    }

    /**
     * Utility to convert from Log category level text to corresponding scale index.
     * 
     * @param categoryExt
     * @param logCategoryLevel
     * @return
     */
    private static int getLevelScaleSelectionFrom(LogCategoryExt categoryExt, LogCategoryLevelExt levelExt) {
        LogCategoryLevelExt[] levelExts = loggingMap.get(categoryExt);
        for (int i = 0; i < levelExts.length; i++) {
            if (levelExt.equals(levelExts[i]))
                return i;
        }
        return 0;
    }

    /**
     * Utility to convert from Log category level text to corresponding scale index.
     * 
     * @param logCategoryLevel
     * @return
     */
    private static int getCategoryComboSelectionFrom(LogCategoryExt logCategoryExt) {
        for (int i = 0; i < categoryExts.length; i++) {
            if (logCategoryExt.equals(categoryExts[i]))
                return i;
        }
        return 0;
    }

    private final LogCategoryExt categoryExt;
    private final LogCategoryLevelExt levelExt;

    public LoggingInfo(LogCategoryExt categoryExt, LogCategoryLevelExt levelExt) {
        this.categoryExt = categoryExt;
        this.levelExt = levelExt;
    }

    public LogCategoryExt getCategoryExt() {
        return categoryExt;
    }

    public LogCategoryLevelExt getLevelExt() {
        return levelExt;
    }

    /**
     * return null for None log level - exclude this category & level from debuggingHeader.
     */
    public LogInfo getLogInfo() {
        if (this.levelExt != LogCategoryLevelExt.None) {
            LogInfo logInfo = new LogInfo();
            logInfo.setCategory(categoryExt.getLogCategory());
            logInfo.setLevel(levelExt.getLogCategoryLevel());
            return logInfo;
        }
        return null;
    }

    /**
     * Used to re-display the logging setting back to UI from LogInfo
     * 
     * @return
     */
    public int getLevelScaleSelection() {
        return getLevelScaleSelectionFrom(this.categoryExt, this.levelExt);
    }

    /**
     * Used to re-display the logging setting back to UI from LogInfo.
     * 
     * @return
     */
    public String getLevelLabelText() {
        return getLevelExt().getExternalValue();
    }

    /**
     * Used to re-display the logging setting back to UI from LogInfo.
     * 
     * @return
     */
    public int getCategorySelection() {
        return getCategoryComboSelectionFrom(getCategoryExt());
    }

}
