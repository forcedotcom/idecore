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
package com.salesforce.ide.core.remote.metadata;

import com.salesforce.ide.core.internal.utils.Utils;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.RunTestsResult;

public class DeployResultExt implements IMetadataResultExt, IDeployResultExt {

	private DeployResult deployResult = null;
	private DeployMessageExt messageHandler = null;
	private RetrieveResultExt retrieveHandler = new RetrieveResultExt();
	private String debugLog = null;

	public DeployResultExt() {
	}

	public DeployResultExt(DeployResult deployResult) {
		this.deployResult = deployResult;
	}

	public void setDeployResult(DeployResult deployResult) {
		this.deployResult = deployResult;
	}

	public void setMessageHandler(DeployMessageExt messageHandler) {
		this.messageHandler = messageHandler;
	}

    @Override
    public DeployResult getDeployResult() {
		return deployResult;
	}

    @Override
    public RunTestsResult getRunTestsResult() {
    	if (deployResult != null) {
    		return deployResult.getDetails().getRunTestResult();
    	}
		return null;
	}

    @Override
    public RunTestsResultExt getRunTestsResultHandler() {
    	if (deployResult != null) {
    		return new RunTestsResultExt(deployResult.getDetails().getRunTestResult());
    	}
		return null;
	}

	@Override
    public DeployMessageExt getMessageHandler() {
		if (deployResult != null && messageHandler == null) {
			messageHandler = new DeployMessageExt(getMessages(deployResult));
		}
		return messageHandler;
	}

	@Override
    public boolean hasRetriveResult() {
		boolean result = false;
		if (deployResult != null && deployResult.getDetails().getRetrieveResult() != null) {
			result = true;
		}
		return result;
	}

	@Override
    public RetrieveResultExt getRetrieveResultHandler() {
		if (deployResult != null && deployResult.getDetails().getRetrieveResult() != null) {
			retrieveHandler = new RetrieveResultExt(deployResult.getDetails().getRetrieveResult());
		}
		return retrieveHandler;
	}

	@Override
    public int getMessageCount() {
		int count = 0;
		DeployMessage[] messages = getMessages(deployResult);
        if (deployResult != null && Utils.isNotEmpty(messages)) {
			count = messages.length;
		}
		return count;
	}

	@Override
    public boolean hasMessages() {
		return getMessageCount() > 0;
	}

	@Override
    public boolean isSuccess() {
		boolean success = false;
		if (deployResult != null) {
			return deployResult.isSuccess();
		}
		return success;
	}

	@Override
    public String getDebugLog() {
		return debugLog;
	}

	public void setDebugLog(String debugLog) {
		this.debugLog = debugLog;
	}
	
	private static DeployMessage[] getMessages(DeployResult deployResult){
        DeployMessage[] componentSuccesses = deployResult.getDetails().getComponentSuccesses();
        DeployMessage[] componentFailures = deployResult.getDetails().getComponentFailures();
        DeployMessage[] combined = new DeployMessage[componentSuccesses.length + componentFailures.length];
        System.arraycopy(componentSuccesses, 0, combined, 0, componentSuccesses.length);
        System.arraycopy(componentFailures, 0, combined, componentSuccesses.length, componentFailures.length);
        return combined;
	}
}
