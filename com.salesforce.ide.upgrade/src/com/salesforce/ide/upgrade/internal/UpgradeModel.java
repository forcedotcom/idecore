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
package com.salesforce.ide.upgrade.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.OrgModel;

/**
 * Encapsulates upgrade data
 * 
 * @author chris
 */
public class UpgradeModel extends OrgModel {

    private static final Logger logger = Logger.getLogger(UpgradeModel.class);

    private Map<String, List<UpgradeConflict>> upgradeConflicts = new HashMap<>();
    private String ideBrandName = null;
    private String ideReleaseName = null;
    private String platformName = null;

    // C O N S T R U C T O R S
    public UpgradeModel(String platformName, String ideReleaseName, String ideBrandName) {
        super();
        this.platformName = platformName;
        this.ideBrandName = ideBrandName;
        this.ideReleaseName = ideReleaseName;
    }

    //   M E T H O D S
    public String getIdeReleaseName() {
        return ideReleaseName;
    }

    public void setIdeReleaseName(String ideReleaseName) {
        this.ideReleaseName = ideReleaseName;
    }

    public String getIdeBrandName() {
        return ideBrandName;
    }

    public void setIdeBrandName(String ideBrandName) {
        this.ideBrandName = ideBrandName;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public Map<String, List<UpgradeConflict>> getUpgradeConflicts() {
        return upgradeConflicts;
    }

    public void setUpgradeConflicts(Map<String, List<UpgradeConflict>> upgradeConflicts) {
        this.upgradeConflicts = upgradeConflicts;
        logUpgradeConflicts();
    }

    public boolean hasUpgradeComponents() {
        return Utils.isNotEmpty(upgradeConflicts);
    }

    public void logUpgradeConflicts() {
        if (!logger.isDebugEnabled()) {
            return;
        }

        if (Utils.isNotEmpty(upgradeConflicts)) {
            TreeSet<String> tmpComponentTypes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            tmpComponentTypes.addAll(upgradeConflicts.keySet());
            StringBuffer strBuff =
                    new StringBuffer("Found following upgrade conflicts for project '" + getProject().getName() + "'");
            int totalComponentCnt = 0;
            for (String tmpComponentType : tmpComponentTypes) {
                strBuff.append("\n").append(tmpComponentType);
                int componentCnt = 0;
                List<UpgradeConflict> tmpUpgradeConflict = upgradeConflicts.get(tmpComponentType);
                if (Utils.isNotEmpty(tmpUpgradeConflict)) {
                    for (UpgradeConflict upgradeConflict : tmpUpgradeConflict) {
                        strBuff.append("\n (").append(++componentCnt).append(") ").append(
                            upgradeConflict.getLocalComponent().getFileName());
                    }
                } else {
                    strBuff.append("\n n/a");
                }
                totalComponentCnt += componentCnt;
            }
            strBuff.append("\nTotal components: " + totalComponentCnt);
            logger.debug(strBuff.toString());
        } else {
            logger.debug("No upgrade conflicts found for project '" + getProject().getName() + "'");
        }
    }
}
