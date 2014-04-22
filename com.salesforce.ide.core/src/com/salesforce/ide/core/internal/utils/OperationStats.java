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

import java.text.NumberFormat;

import org.apache.log4j.Logger;

public class OperationStats {

    private static final Logger logger = Logger.getLogger(OperationStats.class);

    public static final String AGGREGATED_OPERATIONS = "Aggregated";

    private String operationName = null;
    private long cumulativePollingTime = 0;
    private long cumulativeOperationCount = 0;

    public OperationStats() {}

    public OperationStats(String operation) {
        this.operationName = operation;
    }

    public long getCumulativePollingTime() {
        return cumulativePollingTime;
    }

    public long getCumulativeOperationCount() {
        return cumulativeOperationCount;
    }

    public void addPollingTime(long pollingTime) {
        cumulativePollingTime += pollingTime;
    }

    public void incrementOperationCount() {
        cumulativeOperationCount++;
    }

    public void addOperationCount(long operationCount) {
        cumulativeOperationCount += operationCount;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public void logStats() {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);
        try {
            StringBuffer strBuff = new StringBuffer("\n*** Cumulative polling stats for this session's '");
            strBuff.append(operationName).append("' operation(s):").append("\n Total polling time: ").append(
                cumulativePollingTime > 0 ? (nf.format((double) cumulativePollingTime / 1000)) : cumulativePollingTime)
                    .append(" secs").append("\n Num of operations:  ").append(cumulativeOperationCount).append(
                        "\n Avg polling time:   ");
            if (cumulativePollingTime > 0 && cumulativeOperationCount > 0) {
                double avg = ((double) cumulativePollingTime / 1000) / cumulativeOperationCount;
                strBuff.append(nf.format(avg));
            } else {
                strBuff.append(0);
            }
            strBuff.append(" secs/operation\n");
            logger.info(strBuff.toString());
        } catch (Exception e) {
            logger.warn("Unable to print accumulative stats", e);
        }
    }

    public void aggregateStats(OperationStats[] operationStats) {
        if (Utils.isNotEmpty(operationStats)) {
            StringBuffer strBuff = new StringBuffer(getOperationName());
            strBuff.append(" [");
            for (OperationStats operationStat : operationStats) {
                if (operationStat == null) {
                    continue;
                }

                strBuff.append(" ").append(operationStat.getOperationName());
                addPollingTime(operationStat.getCumulativePollingTime());
                addOperationCount(operationStat.getCumulativeOperationCount());
            }
            strBuff.append(" ]");
            setOperationName(strBuff.toString());
        }
    }
}
