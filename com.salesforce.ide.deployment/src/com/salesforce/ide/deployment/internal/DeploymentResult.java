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
package com.salesforce.ide.deployment.internal;

import java.text.DateFormat;
import java.util.Calendar;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.IRunTestsResultExt;
import com.salesforce.ide.core.remote.metadata.DeployMessageExt;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.RunTestsResultExt;
import com.sforce.soap.metadata.DeployMessage;

public class DeploymentResult {

    private DeployResultExt deployResultHandler = null;
    private String deployLog = null;
    private Calendar deployTime = null;
    private DeploymentPayload deploymentPayload = null;
    private ForceProject destinationOrg = null;
    private String sourceProjectName = null;
    private String sourceUsername = null;
    private String sourceEndpoint = null;

    public DeploymentResult() {}

    public Calendar getDeployTime() {
        return deployTime;
    }

    public void setDeployTime(Calendar deployTime) {
        this.deployTime = deployTime;
    }

    public DeployResultExt getDeployResultHandler() {
        return deployResultHandler;
    }

    public void setDeployResultHandler(DeployResultExt deployResultHandler) {
        this.deployResultHandler = deployResultHandler;
    }

    public String getDeployLog() {
        if (Utils.isEmpty(deployLog)) {
            deployLog = generateDeploymentLog();
        }
        return deployLog;
    }

    public void setDeployLog(String deployLog) {
        this.deployLog = deployLog;
    }

    public String getRemoteDeployLog() {
        return deployResultHandler != null ? deployResultHandler.getDebugLog() : null;
    }

    public DeploymentPayload getDeploymentPayload() {
        return deploymentPayload;
    }

    public void setDeploymentPayload(DeploymentPayload deploymentPayload) {
        this.deploymentPayload = deploymentPayload;
    }

    public ForceProject getDestinationOrg() {
        return destinationOrg;
    }

    public void setDestinationOrg(ForceProject destinationOrg) {
        this.destinationOrg = destinationOrg;
    }

    public String getSourceProjectName() {
        return sourceProjectName;
    }

    public void setSourceProjectName(String sourceProjectName) {
        this.sourceProjectName = sourceProjectName;
    }

    public String getSourceUsername() {
        return sourceUsername;
    }

    public void setSourceUsername(String sourceUsername) {
        this.sourceUsername = sourceUsername;
    }

    public String getSourceEndpoint() {
        return sourceEndpoint;
    }

    public void setSourceEndpoint(String sourceEndpoint) {
        this.sourceEndpoint = sourceEndpoint;
    }

    public boolean isSuccess() {
        boolean success = false;
        if (deployResultHandler != null) {
            success = deployResultHandler.isSuccess();
        }
        return success;
    }

    public String generateDeploymentLog() {
        StringBuffer strBuff = new StringBuffer("*** Deployment Log ***");
        strBuff.append("\nResult: " + (isSuccess() ? "SUCCESS" : "FAILED")).append("\nDate: ");
        if (deployTime != null) {
            strBuff.append(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG)
                    .format(deployTime.getTime()));
        } else {
            strBuff.append("n/a");
        }

        strBuff.append("\n\n# Deployed From:").append("\n   Project name: " + sourceProjectName).append(
            "\n   Username: " + sourceUsername).append("\n   Endpoint: " + sourceEndpoint);

        strBuff.append("\n\n# Deployed To:");
        if (destinationOrg != null) {
            strBuff.append("\n   Username: " + destinationOrg.getUserName()).append(
                "\n   Endpoint: " + destinationOrg.getEndpointServer());
        } else {
            strBuff.append("\n   n/a");
        }

        strBuff.append("\n\n# Deploy Results:");
        if (deployResultHandler != null && deployResultHandler.getMessageCount() > 0) {
            DeployMessageExt deployMessageExt = deployResultHandler.getMessageHandler();
            deployMessageExt.sort(DeployMessageExt.SORT_RESULT);
            DeployMessage[] deployMessages = deployMessageExt.getMessages();
            for (int i = 0; i < deployMessages.length; i++) {
                DeployMessage deployMessage = deployMessages[i];
                strBuff.append("\n   File Name:    ").append(deployMessage.getFileName()).append("\n   Full Name:  ").append(
                    deployMessage.getFullName()).append("\n   Action:  ").append(
                    getActionString(deployMessage)).append("\n   Result:  ").append(
                    deployMessage.isSuccess() ? "SUCCESS" : "FAILED").append("\n   Problem: ").append(
                    Utils.isNotEmpty(deployMessage.getProblem()) ? deployMessage.getProblem() : "n/a").append("\n");
            }
        } else {
            strBuff.append("\n  n/a\n");
        }

        strBuff.append("\n# Test Results:");
        String runTestString = generateTestResultString();
        strBuff.append(Utils.isNotEmpty(runTestString) ? runTestString : "\n   n/a");

        return strBuff.toString();
    }

    public String getDeployFailMessages() {
        StringBuffer strBuff = new StringBuffer();

        strBuff.append("\n\n# Deploy Results:");
        if (deployResultHandler != null && deployResultHandler.getMessageCount() > 0) {
            DeployMessageExt deployMessageExt = deployResultHandler.getMessageHandler();
            deployMessageExt.sort(DeployMessageExt.SORT_RESULT);
            DeployMessage[] deployMessages = deployMessageExt.getMessages();
            for (int i = 0; i < deployMessages.length; i++) {
                DeployMessage deployMessage = deployMessages[i];
                if (deployMessage.isSuccess()) {
                    continue;
                }

                strBuff.append("\n   Name:    ").append(deployMessage.getFileName()).append(
                    Utils.isNotEmpty(deployMessage.getProblem()) ? deployMessage.getProblem() : "n/a").append("\n");
            }
        } else {
            strBuff.append("\n  n/a\n");
        }
        return strBuff.toString();
    }

    private static String getActionString(DeployMessage deployMessage) {
        if (deployMessage.isChanged()) {
            return "UPDATED";
        } else if (deployMessage.isCreated()) {
            return "CREATED";
        } else if (deployMessage.isDeleted()) {
            return "DELETED";
        } else {
            return "NO ACTION";
        }
    }

    private String generateTestResultString() {
        if (deployResultHandler == null) {
            return null;
        }

        IRunTestsResultExt runResults = new RunTestsResultExt(deployResultHandler.getDeployResult().getDetails().getRunTestResult());
        String resultsLog = null;
        resultsLog = runResults.toLog();
        return resultsLog;
    }
}
