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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.salesforce.ide.core.remote.metadata.IDeployResultExt;
import com.sforce.soap.metadata.CodeCoverageWarning;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.RunTestFailure;

public class DeployMessageExtractor {

    IDeployResultExt deployResult;

    public DeployMessageExtractor(IDeployResultExt deployResultHandler) {
        if(deployResultHandler == null) {
        	throw new IllegalArgumentException("DeployResultExt should not be Null.");
        }
    	this.deployResult = deployResultHandler;
    }

    public Collection<DeployMessage> getDeploySuccesses() {
        Collection<DeployMessage> successes = new ArrayList<>();
        if (deployResult.getMessageCount() > 0) {
            for (DeployMessage message : deployResult.getMessageHandler().getMessages()) {
                if (message.isSuccess() && message.getProblem() == null) {
                    successes.add(message);
                }
            }
        }
        return successes;
    }

    public Collection<DeployMessage> getDeployFailures() {
        Collection<DeployMessage> failures = new ArrayList<>();
        if (deployResult.getMessageCount() > 0) {
            for (DeployMessage message : deployResult.getMessageHandler().getMessages()) {
                if (!message.isSuccess()) {
                    failures.add(message);
                }
            }
        }
        return failures;
    }

    public Collection<DeployMessage> getDeployWarnings() {
        Collection<DeployMessage> warnings = new ArrayList<>();
        if (deployResult.getMessageCount() > 0) {
            for (DeployMessage message : deployResult.getMessageHandler().getMessages()) {
                if (message.isSuccess() && message.getProblem() != null) {
                    warnings.add(message);
                }
            }
        }
        return warnings;
    }

    public Collection<RunTestFailure> getTestFailures() {
        return deployResult.getRunTestsResult() != null && deployResult.getRunTestsResult().getFailures() != null
                ? Arrays.asList(deployResult.getRunTestsResult().getFailures()) : new ArrayList<RunTestFailure>();
    }

    public List<CodeCoverageWarning> getTestWarnings() {
        List<CodeCoverageWarning> warnings = new ArrayList<>();
        if (deployResult.getRunTestsResult() != null
                && deployResult.getRunTestsResult().getCodeCoverageWarnings() != null
                && deployResult.getRunTestsResult().getCodeCoverageWarnings().length > 0) {
            for (CodeCoverageWarning warning : deployResult.getRunTestsResult().getCodeCoverageWarnings()) {
                warnings.add(warning);
            }
        }
        return warnings;
    }

}
