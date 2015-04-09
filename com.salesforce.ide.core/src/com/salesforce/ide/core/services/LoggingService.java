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
package com.salesforce.ide.core.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

import com.salesforce.ide.api.metadata.types.LogCategoryExt;
import com.salesforce.ide.api.metadata.types.LogCategoryLevelExt;
import com.salesforce.ide.core.internal.utils.LoggingInfo;
import com.salesforce.ide.core.internal.utils.LoggingInfo.SupportedFeatureEnum;
import com.salesforce.ide.core.internal.utils.Utils;
import com.sforce.soap.metadata.LogInfo;

/**
 * 
 * LoggingService serves the bridge setting logging related info to preference. Methods around LoggingInfo return/take
 * LoggingInfo as param. In contract to service that uses LogInfo.
 * 
 * REVIEWME: Seems like most of this is project-related, best suited for ProjectService
 * 
 * @author fchang
 */
public class LoggingService extends BaseService {

    public void setLoggingInfo(IProject project, LoggingInfo loggingInfo, SupportedFeatureEnum supportedFeatureEnum) {
        setLogCategory(project, loggingInfo.getCategoryExt(), loggingInfo.getLevelExt(), supportedFeatureEnum);
    }

    /**
     * 
     * Retrieve default selected log info preference for first category: used when logging composite first-time loaded.
     * The default category should be selected is 'Apex Code' and default level is 'Debug'
     * 
     * @param project
     * @param supportedFeatureEnum
     * @return
     */
    public LoggingInfo getDefaultSelectedLoggingInfo(IProject project, SupportedFeatureEnum supportedFeatureEnum) {
        LogCategoryExt defaultSelectedCategory = LogCategoryExt.Apex_code;
        LogCategoryLevelExt level = getLogCategoryLevelExt(project, defaultSelectedCategory, supportedFeatureEnum);
        return new LoggingInfo(defaultSelectedCategory, level);
    }

    /**
     * Retrieve all local log info preferences: used to send aggregated user's log selections to server.
     * 
     * @param project
     * @param supportedFeatureEnum
     * @return
     */
    public LogInfo[] getAllLogInfo(IProject project, SupportedFeatureEnum supportedFeatureEnum) {
        List<LogInfo> logInfoList = new ArrayList<>();
        for (int i = 0; i < LoggingInfo.getLogCategories().length; i++) {
            LoggingInfo loggingInfo =
                    getLoggingInfoByCategory(project, LoggingInfo.getLogCategories()[i], supportedFeatureEnum);
            if (loggingInfo.getLogInfo() != null) {
                logInfoList.add(loggingInfo.getLogInfo());
            }
        }
        return logInfoList.toArray(new LogInfo[logInfoList.size()]);
    }

    public LoggingInfo[] getAllLoggingInfo(IProject project, SupportedFeatureEnum supportedFeatureEnum) {
        List<LoggingInfo> loggingInfoList = new ArrayList<>();
        for (int i = 0; i < LoggingInfo.getLogCategories().length; i++) {
            LoggingInfo loggingInfo =
                    getLoggingInfoByCategory(project, LoggingInfo.getLogCategories()[i], supportedFeatureEnum);
            loggingInfoList.add(loggingInfo);
        }
        return loggingInfoList.toArray(new LoggingInfo[loggingInfoList.size()]);
    }

    /**
     * Apply all log info to preference: used by restore logging setting in Project property when user press cancel.
     * 
     * @param project
     * @param cachedLoggingSetting
     * @param supportedFeatureEnum
     */
    public void setAllLoggingInfo(IProject project, LoggingInfo[] cachedLoggingSetting,
            SupportedFeatureEnum supportedFeatureEnum) {
        for (LoggingInfo loggingInfo : cachedLoggingSetting) {
            setLogCategory(project, loggingInfo.getCategoryExt(), loggingInfo.getLevelExt(), supportedFeatureEnum);
        }
    }

    /**
     * @param supportedFeatureEnum
     *            - pre-pended as namespace to set proper preference for either run test or exec anonymous logging
     *            settings.
     */
    private void setLogCategory(IProject project, LogCategoryExt categoryExt, LogCategoryLevelExt levelyExt,
            SupportedFeatureEnum supportedFeatureEnum) {
        getProjectService().setString(project, supportedFeatureEnum + categoryExt.getInternalValue(),
            levelyExt.getExternalValue());
    }

    public LoggingInfo getLoggingInfoByCategory(IProject project, LogCategoryExt logCategoryExt,
            SupportedFeatureEnum supportedFeatureEnum) {
        LoggingInfo loggingInfo =
                new LoggingInfo(logCategoryExt, getLogCategoryLevelExt(project, logCategoryExt, supportedFeatureEnum));
        return loggingInfo;
    }

    /**
     * Return default log level for each category if preference hasn't set.
     * 
     * @param supportedFeatureEnum
     *            - pre-pended as namespace to retrieve proper preference for either run test or exec anonymous logging
     *            settings.
     */
    private LogCategoryLevelExt getLogCategoryLevelExt(IProject project, LogCategoryExt logCategory,
            SupportedFeatureEnum supportedFeatureEnum) {
        LogCategoryLevelExt level = null;
        String logCategoryLevel =
                getProjectService().getString(project, supportedFeatureEnum + logCategory.getInternalValue(), null);
        if (Utils.isEmpty(logCategoryLevel)) {
            // return default log level for each category if preference hasn't set.
            level = logCategory.getDefaultLogCategoryLevel();
        } else {
            level = LogCategoryLevelExt.fromExternalValue(logCategoryLevel);
        }
        return level;
    }

    /**
     * fchang: remove this transforming method once remove apex api completely!
     * 
     * @param supportedFeatureEnum
     * @return
     */
    public com.sforce.soap.apex.LogInfo[] getAllApexApiLogInfo(IProject project,
            SupportedFeatureEnum supportedFeatureEnum) {
        LogInfo[] metaLogInfo = getAllLogInfo(project, supportedFeatureEnum);

        List<com.sforce.soap.apex.LogInfo> apexLogInfoList = new ArrayList<>();
        for (int i = 0; i < metaLogInfo.length; i++) {
            com.sforce.soap.apex.LogInfo apexLogInfo = new com.sforce.soap.apex.LogInfo();
            apexLogInfo.setCategory(com.sforce.soap.apex.LogCategory.valueOf(metaLogInfo[i].getCategory().name()));
            apexLogInfo.setLevel(com.sforce.soap.apex.LogCategoryLevel.valueOf(metaLogInfo[i].getLevel().name()));
            apexLogInfoList.add(apexLogInfo);
        }
        return apexLogInfoList.toArray(new com.sforce.soap.apex.LogInfo[apexLogInfoList.size()]);
    }

}
