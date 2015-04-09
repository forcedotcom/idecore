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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.utils.OperationStats;
import com.salesforce.ide.core.internal.utils.QuietCloseable;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.MetadataStubExt;
import com.sforce.soap.metadata.AsyncResult;

public abstract class BasePackageService extends BaseService {

    private static final Logger logger = Logger.getLogger(BasePackageService.class);

    // the following are default, but are injected via services.xml
    private long timeSoFarInitMillis = 0;
    private long initialPollingInterval = 500; // .5 sec
    private long maxPollingInterval = 8000; // 8 sec
    private long maxPollingTime = 600000; // 10 min
    private int pollingMultiple = 2; // exponential value to increase polling interval
    private int applyMultipleRound = 3; // which cycle round to start applying exponential multiple

    //   C O N  S T R U C T O R
    public BasePackageService() {
        super();
    }

    //  M E T H O D S
    public long getInitialPollingInterval() {
        return initialPollingInterval;
    }

    public void setInitialPollingInterval(long initialPollingInterval) {
        this.initialPollingInterval = initialPollingInterval;
    }

    public long getMaxPollingInterval() {
        return maxPollingInterval;
    }

    public void setMaxPollingInterval(long maxPollingInterval) {
        this.maxPollingInterval = maxPollingInterval;
    }

    public long getMaxPollingTime() {
        String pollLimit = Utils.getPollLimit();
        if (Utils.isNotEmpty(pollLimit)) {
            maxPollingTime = Long.parseLong(pollLimit);
        }
        return maxPollingTime;
    }

    public void setMaxPollingTime(long maxPollingTime) {
        this.maxPollingTime = maxPollingTime;
    }

    public int getPollingMultiple() {
        return pollingMultiple;
    }

    public void setPollingMultiple(int pollingMultiple) {
        this.pollingMultiple = pollingMultiple;
    }

    public int getApplyMultipleRound() {
        return applyMultipleRound;
    }

    public void setApplyMultipleRound(int applyMultipleRound) {
        this.applyMultipleRound = applyMultipleRound;
    }

    public long getTimeSoFarInitMillis() {
        return timeSoFarInitMillis;
    }

    public void setTimeSoFarInitMillis(long timeSoFarInitMillis) {
        this.timeSoFarInitMillis = timeSoFarInitMillis;
    }

    protected void addToCumulative(OperationStats operationStats, long timeSoFar) {
        if (operationStats != null) {
            operationStats.addPollingTime(timeSoFar);
            operationStats.incrementOperationCount();

            if (logger.isDebugEnabled()) {
                operationStats.logStats();
            }
        }
    }

    protected String getFilePathLog(String message, byte[] zipFile) {
        String filePathsStr = "No filePaths found";
        try {
            List<String> filePaths = getFilePaths(zipFile);
            if (Utils.isNotEmpty(filePaths)) {
                StringBuffer strBuff = new StringBuffer();
                strBuff.append(message).append(" [").append(filePaths.size()).append("]: ");
                int fileCnt = 0;
                Collections.sort(filePaths);
                for (String filePath : filePaths) {
                    strBuff.append("\n (").append(++fileCnt).append(") ").append(filePath);
                }
                filePathsStr = "\n" + strBuff.toString();
            }
        } catch (IOException e) {
            logger.debug("Unable to retrieve file paths from zip", e);
        }
        return filePathsStr;
    }

    protected List<String> getFilePaths(byte[] zipFile) throws IOException {
        List<String> filePaths = new ArrayList<>();
        if (Utils.isNotEmpty(zipFile)) {
            try (final QuietCloseable<ZipInputStream> c = QuietCloseable.make(new ZipInputStream(new ByteArrayInputStream(zipFile)))) {
                final ZipInputStream zis = c.get();

                for (;;) {
                    ZipEntry ze = zis.getNextEntry();
                    if (ze == null) {
                        break;
                    }

                    String name = ze.getName();
                    if (ze.isDirectory()) {
                        continue;
                    }

                    filePaths.add(name);
                }
            }

            if (Utils.isNotEmpty(filePaths)) {
                Collections.sort(filePaths);
            }

        } else {
            logger.warn("Unable to get filepaths from zip - byte array is empty or null (likely because deploy or"
                    + " retrieve failed)");
        }

        return filePaths;
    }

    protected IFileBasedResultAdapter waitForResult(IFileBasedResultAdapter result, MetadataStubExt metadataStubExt,
            OperationStats operationStats, IProgressMonitor monitor) throws ForceRemoteException,
            ServiceTimeoutException, ServiceException, InterruptedException {

        if (metadataStubExt == null) {
            throw new IllegalArgumentException("Metadata stub cannot be null");
        }

        AsyncResult asyncResult = result.getAsyncResult();
        long timeSoFarMillis = getTimeSoFarInitMillis();
        long pollingInternal = getInitialPollingInterval();
        int applyMultipleAtCycle = getApplyMultipleRound();
        int maxPollingTime = metadataStubExt.getReadTimeout();

        if (logger.isDebugEnabled()) {
            logger.debug("Start polling for response " + Calendar.getInstance().getTime().toString());
            logger.debug("Initial polling interval will be " + pollingInternal + " milliseconds for "
                    + applyMultipleAtCycle + " rounds");
            logger.debug("Metadata API timeout set to " + Utils.timeoutToSecs(metadataStubExt.getReadTimeout()));

        }

        for (int i = 0;; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("");
                logger.debug("####  Polling cycle round " + (i + 1) + "  ####");
            }

            monitorSubTask(monitor, result.retrieveRealTimeStatusUpdatesIfAny());

            // poll until it's done:
            result.checkStatus();

            if (logger.isDebugEnabled()) {
                result.logStatus(logger);
            }

            if (result.isDone()) {
                if (logger.isDebugEnabled()) {
                    addToCumulative(operationStats, timeSoFarMillis);
                    result.logResult(logger, operationStats);
                }

                // failed
                if (result.isFailure()) {
                    String failureString = result.logFailure(logger);
                    if (logger.isDebugEnabled()) {

                        logger.debug("Polling failed after " + (timeSoFarMillis / 1000) + " secs for id '"
                                + asyncResult.getId() + "'");
                    }
                    throw new ServiceException(failureString, result.getAsyncResult());
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Polling complete for operation id '" + asyncResult.getId() + "' after "
                            + (timeSoFarMillis / 1000) + " secs");
                }

                // success
                break;
            }

            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Next poll will be in ~" + (double) pollingInternal / 1000 + " secs");
                }
                Thread.sleep(pollingInternal);
            } catch (InterruptedException e) {} finally {
                timeSoFarMillis += pollingInternal;
            }

            // record polling time to this point and evaluate base on limit
            if (timeSoFarMillis > getMaxPollingTime()) {
                logger.warn("Polling aborted after " + (timeSoFarMillis / 1000) + " secs for id '"
                        + asyncResult.getId() + "'");

                if (logger.isDebugEnabled()) {
                    addToCumulative(operationStats, timeSoFarMillis);
                }

                throw new ServiceTimeoutException("Server processing time has exceeded limit ("
                        + Utils.timeoutToSecs(getMaxPollingTime()) + ")", metadataStubExt, result.getAsyncResult(),
                        operationStats);
            }

            // determine if polling multiple is to be applied
            if (pollingInternal < maxPollingTime && i == (applyMultipleAtCycle - 1)) {
                pollingInternal = pollingInternal * getPollingMultiple();
                applyMultipleAtCycle += getApplyMultipleRound();
                if (logger.isDebugEnabled()) {
                    logger.debug("Adjusted polling multiple, next application will be after round "
                            + applyMultipleAtCycle);
                }
            }

            try {
                monitorCheck(monitor);
            } catch (InterruptedException e) {
                logger.warn("Polling canceled by user after " + (timeSoFarMillis / 1000) + " secs for id '"
                        + asyncResult.getId() + "'");
                throw e;
            }
        }

        return result;
    }

    public static Logger getLogger() {
        return logger;
    }
}
