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

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.api.metadata.MetadataDebuggingInfoHandler;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.OperationStats;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.ZipUtils;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.MetadataStubExt;
import com.salesforce.ide.core.remote.metadata.DeployMessageExt;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.DescribeMetadataResultExt;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.DeployStatus;
import com.sforce.soap.metadata.LogInfo;
import com.sforce.soap.metadata.TestLevel;

public class PackageDeployService extends BasePackageService {
    private static final Logger logger = Logger.getLogger(PackageDeployService.class);

    private static OperationStats operationStats;

    private static final String OPERATION = "Deploy";

    public PackageDeployService() {
        operationStats = new OperationStats(OPERATION);
    }

    public static OperationStats getOperationStats() {
        return operationStats;
    }

    /**
     * Get deploy option for validate deployment (checkOnly) and true deployment (not checkOnly).
     * 
     * @param checkOnly
     * 
     * @return
     */
    public DeployOptions getDeployOptions(boolean checkOnly) {
        DeployOptions deployOptions = makeDefaultDeployOptions(checkOnly);
        deployOptions.setRollbackOnError(true);
        deployOptions.setAutoUpdatePackage(false);
        deployOptions.setPerformRetrieve(false);
        return deployOptions;
    }

    public DeployOptions getRunTestDeployOptions(String[] tests) {
        DeployOptions deployOptions = makeDefaultDeployOptions(true);
        deployOptions.setTestLevel(TestLevel.RunSpecifiedTests);
        deployOptions.setPerformRetrieve(false);
        deployOptions.setTestLevel(TestLevel.RunSpecifiedTests);

        if (Utils.isNotEmpty(tests)) {
            deployOptions.setRunTests(tests);
            if (logger.isDebugEnabled()) {
                logger.debug("Running the following tests:\n");
                for (String test : tests) {
                    logger.debug("  " + test);
                }
            }
        }

        return deployOptions;
    }

    public DeployOptions makeDefaultDeployOptions(boolean checkOnly) {
        DeployOptions deployOptions = new DeployOptions();
        // tell deploy to ignore missing files.  this will save on the one component
        deployOptions.setAllowMissingFiles(true);
        // if successful, return refreshed component
        deployOptions.setPerformRetrieve(true);
        // send back update package manifest too
        deployOptions.setAutoUpdatePackage(true);
        // oh and, if other components don't comply (fail to compile, failed tests, etc), save the ones that do comply
        // REVIEWME: apparently the api only handles test failures.  so, if one component fails
        //   to compile (for example, the whole lot will fail to save).
        deployOptions.setRollbackOnError(true);
        deployOptions.setCheckOnly(checkOnly);
        deployOptions.setSinglePackage(true);
        return deployOptions;
    }

    public DeployResultExt deploy(ProjectPackageList projectPackageList, IProgressMonitor monitor)
        throws ServiceException, ForceRemoteException, InterruptedException, ForceConnectionException {
        if (Utils.isEmpty(projectPackageList) || projectPackageList.getProject() == null) {
            throw new IllegalArgumentException("Project and/or project package list cannot be null");
        }
        Connection connection = getConnectionFactory().getConnection(projectPackageList.getProject());
        return deploy(connection, projectPackageList, monitor);
    }

    public DeployResultExt deploy(
        ProjectPackageList projectPackageList,
        IProgressMonitor monitor,
        DeployOptions options)
            throws ServiceException, ForceRemoteException, InterruptedException, ForceConnectionException {
        if (Utils.isEmpty(projectPackageList) || projectPackageList.getProject() == null) {
            throw new IllegalArgumentException("Project and/or project package list cannot be null");
        }
        Connection connection = getConnectionFactory().getConnection(projectPackageList.getProject());
        return deploy(connection, projectPackageList, options, monitor);
    }

    public DeployResultExt deploy(
        Connection connection,
        ProjectPackageList projectPackageList,
        IProgressMonitor monitor) throws ServiceException, ForceRemoteException, InterruptedException {
        if (connection == null || Utils.isEmpty(projectPackageList)) {
            throw new IllegalArgumentException("Connection and/or project package list cannot be null");
        }

        DeployOptions deployOptions = makeDefaultDeployOptions(false);
        return deploy(connection, projectPackageList, deployOptions, monitor);
    }

    DeployResultExt deploy(
        Connection connection,
        ProjectPackageList projectPackageList,
        DeployOptions deployOptions,
        IProgressMonitor monitor) throws ServiceException, ForceRemoteException, InterruptedException {
        return deploy(connection, projectPackageList, deployOptions, null, true, monitor);
    }

    public DeployResultExt deploy(Connection connection, byte[] zipFile, IProgressMonitor monitor)
        throws ServiceException, ForceRemoteException, InterruptedException {
        if (connection == null || Utils.isEmpty(zipFile)) {
            throw new IllegalArgumentException("Connection and/or file zip cannot be null");
        }

        DeployOptions deployOptions = makeDefaultDeployOptions(false);
        return deploy(connection, zipFile, deployOptions, null, monitor);
    }

    public DeployResultExt deploy(
        Connection connection,
        ProjectPackageList projectPackageList,
        DeployOptions deployOptions,
        LogInfo[] logInfos,
        boolean adjust,
        IProgressMonitor monitor) throws ServiceException, ForceRemoteException, InterruptedException {
        DeployResultExt deployResultExt =
                deployWork(connection, getZip(projectPackageList), deployOptions, logInfos, adjust, monitor);

        deployResultExt.getRetrieveResultHandler().setProjectPackageList(projectPackageList);
        return deployResultExt;
    }

    public DeployResultExt deployDelete(
        ProjectPackageList projectPackageList,
        boolean checkOnly,
        IProgressMonitor monitor)
            throws ServiceException, ForceRemoteException, InterruptedException, ForceConnectionException {
        if (Utils.isEmpty(projectPackageList) || projectPackageList.getProject() == null) {
            throw new IllegalArgumentException("Project and/or project package list cannot be null");
        }
        Connection connection = getConnectionFactory().getConnection(projectPackageList.getProject());
        return deployDelete(connection, projectPackageList, checkOnly, monitor);
    }

    public DeployResultExt deployDelete(
        Connection connection,
        ProjectPackageList projectPackageList,
        boolean checkOnly,
        IProgressMonitor monitor) throws ServiceException, ForceRemoteException, InterruptedException {
        DeployOptions deployOptions = makeDefaultDeployOptions(checkOnly);
        // set autoUpdatePackage and PerformRetrieve to true for retreiving updated package.xml
        // W-632267
        deployOptions.setAutoUpdatePackage(true);
        deployOptions.setPerformRetrieve(true);
        DeployResultExt deployResultExt = deploy(
            connection,
            getZip(projectPackageList, true),
            deployOptions,
            null,
            monitor);
        deployResultExt.getRetrieveResultHandler().setProjectPackageList(projectPackageList);
        return deployResultExt;
    }

    public DeployResultExt deploy(
        Connection connection,
        byte[] zipFile,
        DeployOptions deployOptions,
        LogInfo[] logInfos,
        IProgressMonitor monitor) throws ServiceException, ForceRemoteException, InterruptedException {
        return deployWork(connection, zipFile, deployOptions, logInfos, true, monitor);
    }

    public DeployResultExt getDeployResult(
        DeployResultExt deployResultExt,
        AsyncResult asyncResult,
        MetadataStubExt metadataStubExt,
        IProgressMonitor monitor) throws ServiceTimeoutException, ServiceException, ForceRemoteException,
            ForceRemoteException, InterruptedException {
        if (metadataStubExt == null) {
            throw new IllegalArgumentException("MetadataStubExt cannot be null");
        }

        DeployResult deployResult;
        try {
            IFileBasedResultAdapter result =
                    waitForResult(new DeployResultAdapter(asyncResult, metadataStubExt), metadataStubExt,
                        operationStats, monitor);
            deployResult = ((DeployResultAdapter) result).getDeployResult();
        } catch (ServiceTimeoutException ex) {
            ex.setMetadataResultExt(deployResultExt);
            throw ex;
        }

        monitorCheckSubTask(monitor, "Preparing results...");

        // REVIEWME: should we create an empty wrapper?
        if (deployResultExt == null) {
            deployResultExt = new DeployResultExt();
        }

        deployResultExt.setDeployResult(deployResult);
        deployResultExt.setDebugLog(MetadataDebuggingInfoHandler.getDebugLog());

        monitorWork(monitor);

        // log results
        logResult(deployResultExt);

        return deployResultExt;
    }

    public DeployResultExt handleDeployServiceTimeoutException(
        ServiceTimeoutException ex,
        String operation,
        IProgressMonitor monitor)
            throws InterruptedException, ServiceException, ForceRemoteException, InsufficientPermissionsException {
            
        // REVIEW: ui-stuff (dialog to continue) should be handled outside of services  -cwall 09/2//09
        boolean proceed = DialogUtils.getInstance().presentCycleLimitExceptionDialog(ex, monitor);
        if (proceed) {
            try {
                return getPackageDeployService().getDeployResult(
                    (DeployResultExt) ex.getMetadataResultExt(),
                    ex.getAsyncResult(),
                    ex.getMetadataStubExt(),
                    monitor);
            } catch (ServiceTimeoutException e) {
                return handleDeployServiceTimeoutException(e, operation, monitor);
            }
        }
        throw new InterruptedException(
            "User canceled " + operation + " due to cycle polling limits reached: " + ex.getMessage());
    }

    // workhorse for deploying
    private DeployResultExt deployWork(
        Connection connection,
        byte[] zipFile,
        DeployOptions deployOptions,
        LogInfo[] logInfos,
        boolean adjust,
        IProgressMonitor monitor) throws ServiceException, ForceRemoteException, InterruptedException {
        if (connection == null || Utils.isEmpty(zipFile)) {
            throw new IllegalArgumentException("Connection and/or zip file name cannot be null");
        }

        monitorCheck(monitor);

        DeployResultExt deployResultHandler = new DeployResultExt();

        // log deployment details
        logDeploy(connection, zipFile, deployOptions);

        try {
            // prepare deployment stub
            MetadataStubExt metadataStubExt = getMetadataFactory().getMetadataStubExt(connection);
            metadataStubExt.setMetadataDebugHeader(logInfos);

            if (adjust) {
                // adjust deploy options based on org settings
                adjustDeployOptions(metadataStubExt, deployOptions, monitor);
            }

            monitorCheckSubTask(monitor, "Deploying components...");

            // call deploy and wait for response
            if (logger.isDebugEnabled()) {
                logger.debug("Calling deploy() at " + (new Date()).toString());
            }

            AsyncResult asyncResult = metadataStubExt.deploy(zipFile, deployOptions);
            monitorWork(monitor);

            // get async result
            deployResultHandler = getDeployResult(deployResultHandler, asyncResult, metadataStubExt, monitor);

        } catch (InterruptedException e) {
            throw e;
        } catch (ServiceTimeoutException e) {
            throw e;
        } catch (InsufficientPermissionsException e) {
            throw e;
        } catch (ServiceException e) {
            logger.warn("Unable to retrieve components: " + ForceExceptionUtils.getRootCauseMessage(e));
            throw new DeployException(e, connection, zipFile, deployOptions, logInfos);
        } catch (Exception e) {
            logger.error("Unable to retrieve components: " + ForceExceptionUtils.getRootCauseMessage(e));
            throw new DeployException(e, connection, zipFile, deployOptions, logInfos);
        }

        return deployResultHandler;
    }

    private void adjustDeployOptions(MetadataStubExt metadataStubExt, DeployOptions deployOptions,
            IProgressMonitor monitor) throws ForceRemoteException, InterruptedException {
        DescribeMetadataResultExt describeMetadataResultExt =
                getMetadataService().getDescribeMetadata(metadataStubExt, monitor);
        // assume that org is prod
        if (describeMetadataResultExt.isPartialSaveAllowed()) {
            deployOptions.setRollbackOnError(false);
            if (logger.isDebugEnabled()) {
                logger.debug("Partial save is allowed - rollback on error set to false");
            }
        } else {
            deployOptions.setRollbackOnError(true);
            if (logger.isDebugEnabled()) {
                logger.debug("Partial save is not allowed - rollback on error set to true");
            }
        }
    }

    protected byte[] getZip(Object obj) throws DeployException {
        return getZip(obj, false);
    }

    private static byte[] getZip(Object obj, boolean manifestsOnly) throws DeployException {
        byte[] zip = null;
        try {
            if (obj instanceof ProjectPackageList) {
                zip = ((ProjectPackageList) obj).getZip(manifestsOnly);
            } else if (obj instanceof ProjectPackage) {
                zip = ((ProjectPackage) obj).getZip(manifestsOnly);
            } else {
                logger.warn("Do not know how to get zip from unknown object type");
            }
        } catch (IOException e) {
            logger.error("Unable to get zip", e);
            throw new DeployException("Unable to get zip", e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Got zip file of size [" + (null != zip ? zip.length : null)
                    + "] for project package");
        }

        return zip;
    }

    private void logDeploy(Connection connection, byte[] zipFile, DeployOptions deployOptions) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deploying to " + connection.getLogDisplay());

            String filePathLog = getFilePathLog("Deploying the following components", zipFile);
            logger.debug(filePathLog);

            if (deployOptions != null) {
                StringBuffer strBuff = new StringBuffer("Deploy options:");
                strBuff.append("\n  Check only = " + deployOptions.isCheckOnly())
                        .append("\n  Single package = " + deployOptions.isSinglePackage())
                        .append("\n  Update package manifest = " + deployOptions.isAutoUpdatePackage())
                        .append("\n  Save w/ missing files = " + deployOptions.isAllowMissingFiles())
                        .append("\n  Rollback on error = " + deployOptions.isRollbackOnError())
                        .append("\n  Perform retrieve = " + deployOptions.isPerformRetrieve());
                logger.debug(strBuff.toString());
                if (Utils.isNotEmpty(deployOptions.getRunTests())) {
                    String[] tests = deployOptions.getRunTests();
                    strBuff = new StringBuffer("Deploy options set to run the following tests");
                    int testCnt = 0;
                    for (String test : tests) {
                        strBuff.append("\n (").append(++testCnt).append(") ").append(test);
                    }
                    logger.debug("\n" + strBuff.toString());
                }
            } else {
                logger.debug("Deploy options not set");
            }
        } else if (logger.isInfoEnabled()) {
            logger.info("Deploying to " + connection.getLogDisplay());
            String filePathLog = getFilePathLog("Deploying the following components", zipFile);
            logger.info(filePathLog);
        }
    }

    private void logResult(DeployResultExt deployResultHandler) {
        if (logger.isDebugEnabled()) {
            // write zip to f/s
            String filePathLog = null;
            if (deployResultHandler.getRetrieveResultHandler() != null) {
                byte[] zipFile = deployResultHandler.getRetrieveResultHandler().getZipFile();
                filePathLog = getFilePathLog("Deployed returned the following components:", zipFile);
                if (Utils.isNotEmpty(zipFile)) {
                    ZipUtils.writeRetrieveZipToTempDir(zipFile);
                }
            }
            logger.debug(filePathLog);

            // log deploy messages
            DeployMessageExt messageHandler = deployResultHandler.getMessageHandler();
            if (messageHandler != null) {
                messageHandler.logMessage();
            }

            // log api log
            if (deployResultHandler.getDeployResult() != null) {
                logger.debug("Metadata debug log:\n"
                        + (Utils.isNotEmpty(deployResultHandler.getDebugLog()) ? deployResultHandler.getDebugLog()
                                : "n/a"));
            }
        } else if (logger.isInfoEnabled()) {
            DeployMessageExt messageHandler = deployResultHandler.getMessageHandler();
            if (messageHandler != null) {
                messageHandler.logMessage();
            }
        }

    }
}

/**
 * Adapts the DeployResult to our common interface.
 * 
 * @author nchen
 * 
 */
class DeployResultAdapter implements IFileBasedResultAdapter {

    private final AsyncResult asyncResult;
    private DeployResult deployResult;
    private final MetadataStubExt metadataStubExt;

    public DeployResultAdapter(AsyncResult asyncResult, MetadataStubExt metadataStubExt) {
        this.asyncResult = asyncResult;
        this.metadataStubExt = metadataStubExt;
    }

    @Override
    public AsyncResult getAsyncResult() {
        return asyncResult;
    }

    @Override
    public IFileBasedResultAdapter checkStatus() throws ForceRemoteException {
        deployResult = metadataStubExt.checkDeployStatus(asyncResult.getId());
        return this;
    }

    @Override
    public boolean isDone() {
        return deployResult.isDone();
    }

    @Override
    public boolean isFailure() {
        return deployResult.getStatus().equals(DeployStatus.Failed) && deployResult.getErrorStatusCode() != null;
    }

    @Override
    public String logFailure(Logger logger) {
        StringBuffer strBuff =
                new StringBuffer().append(deployResult.getErrorStatusCode()).append(" (")
                        .append(deployResult.getErrorMessage()).append(")");
        logger.warn("Deploy or retrieve operation to '" + metadataStubExt.getServerName() + "' failed: "
                + strBuff.toString());
        return strBuff.toString();
    }

    @Override
    public String logResult(Logger logger, OperationStats operationStats) {
        StringBuffer errorMessageBuffer = new StringBuffer();

        errorMessageBuffer.append("\n'Operation' [Total count :Error count :Success count]\n").append("'")
                .append(operationStats.getOperationName()).append("' Components [")
                .append(deployResult.getNumberComponentsTotal()).append(":")
                .append(deployResult.getNumberComponentErrors()).append(":")
                .append(deployResult.getNumberComponentsDeployed()).append("] Tests : [")
                .append(deployResult.getNumberTestsTotal()).append(":").append(deployResult.getNumberTestErrors())
                .append(":").append(deployResult.getNumberTestsCompleted()).append("]").append("\nState Detail : ")
                .append(deployResult.getStateDetail());

        logger.debug(errorMessageBuffer.toString());
        return errorMessageBuffer.toString();
    }

    @Override
    public String logStatus(Logger logger) {
        String status =
                "Deploy state is '" + deployResult.getStatus().toString() + "' for operation id '"
                        + asyncResult.getId() + "'";
        logger.debug(status);
        return status;
    }

    @Override
    public String retrieveRealTimeStatusUpdatesIfAny() {
        if (deployResult != null && deployResult.getStatus() != null) {
            return Messages.getString(
                "Deploy.ReportingStatus",
                new Object[] { deployResult.getStatus(), deployResult.getNumberComponentsDeployed(),
                        deployResult.getNumberComponentsTotal(), new Date() });
        }
        return Messages.getString("PackageService.Polling", new Object[] { metadataStubExt.getServerName() });
    }

    public DeployResult getDeployResult() {
        return deployResult;
    }
}
