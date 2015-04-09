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

import java.util.ArrayList;
import java.util.List;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.ICodeCoverageResultExt;
import com.salesforce.ide.core.remote.ICodeCoverageWarningExt;
import com.salesforce.ide.core.remote.IRunTestFailureExt;
import com.salesforce.ide.core.remote.IRunTestsResultExt;
import com.sforce.soap.metadata.CodeCoverageWarning;
import com.sforce.soap.metadata.DebuggingInfo_element;
import com.sforce.soap.metadata.RunTestFailure;
import com.sforce.soap.metadata.RunTestsResult;

public class RunTestsResultExt implements IRunTestsResultExt {

    public static final int CODE_COVERAGE_COMPLIANCE_PCT = 75;

    protected RunTestsResult metadataRunTestsResult;
    protected DebuggingInfo_element debugInfo;
    protected String namespacePrefix;

    public RunTestsResultExt(RunTestsResult metadataRunTestsResult) {
        this.metadataRunTestsResult = metadataRunTestsResult;
    }

    public RunTestsResultExt(IDeployResultExt results) {
        this.metadataRunTestsResult = results.getRunTestsResult();
        this.debugInfo = new DebuggingInfo_element();
        this.debugInfo.setDebugLog(results.getDebugLog());
    }

    @Override
    public DebuggingInfo_element getDebugInfo() {
        return debugInfo;
    }

    @Override
    public RunTestsResult getTestResult() {
        return metadataRunTestsResult;
    }

    @Override
    public void setDebugInfo(DebuggingInfo_element debugInfo) {
        this.debugInfo = debugInfo;
    }

    @Override
    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    @Override
    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }

    @Override
    public int getNumTestsRun() {
        return metadataRunTestsResult.getNumTestsRun();
    }

    @Override
    public double getTotalTime() {
        return metadataRunTestsResult.getTotalTime();
    }

    @Override
    public int getNumFailures() {
        if (metadataRunTestsResult == null) {
            return 0;
        }
        return metadataRunTestsResult.getNumFailures();
    }

    public boolean hasFailures() {
        return metadataRunTestsResult.getNumFailures() > 0;
    }

    @Override
    public ICodeCoverageResultExt[] getCodeCoverages() {
        ICodeCoverageResultExt[] codeCoverageResultExts = null;
        if (Utils.isNotEmpty(metadataRunTestsResult.getCodeCoverage())) {
            codeCoverageResultExts = new ICodeCoverageResultExt[metadataRunTestsResult.getCodeCoverage().length];
            for (int i = 0; i < metadataRunTestsResult.getCodeCoverage().length; i++) {
                codeCoverageResultExts[i] = new CodeCoverageResultExt(metadataRunTestsResult.getCodeCoverage()[i]);
            }
        }
        return codeCoverageResultExts;
    }

    @Override
    public ICodeCoverageResultExt getCodeCoverage(int i) {
        return new CodeCoverageResultExt(metadataRunTestsResult.getCodeCoverage()[i]);
    }

    @Override
    public ICodeCoverageWarningExt[] getCodeCoverageWarnings() {
        ICodeCoverageWarningExt[] codeCoverageWarningsExts = null;
        if (Utils.isNotEmpty(metadataRunTestsResult.getCodeCoverageWarnings())) {
            codeCoverageWarningsExts = new ICodeCoverageWarningExt[metadataRunTestsResult.getCodeCoverageWarnings().length];
            for (int i = 0; i < metadataRunTestsResult.getCodeCoverageWarnings().length; i++) {
                codeCoverageWarningsExts[i] = new CodeCoverageWarningExt(metadataRunTestsResult.getCodeCoverageWarnings()[i]);
            }
        }
        return codeCoverageWarningsExts;
    }

    public boolean hasCodeCoverageWarnings() {
        return Utils.isNotEmpty(metadataRunTestsResult.getCodeCoverageWarnings());
    }

    @Override
    public ICodeCoverageWarningExt getCodeCoverageWarnings(int i) {
        return new CodeCoverageWarningExt(metadataRunTestsResult.getCodeCoverageWarnings()[i]);
    }

    @Override
    public IRunTestFailureExt[] getFailures() {
        IRunTestFailureExt[] runTestFailureExts = null;
        if (Utils.isNotEmpty(metadataRunTestsResult.getFailures())) {
            runTestFailureExts = new IRunTestFailureExt[metadataRunTestsResult.getFailures().length];
            for (int i = 0; i < metadataRunTestsResult.getFailures().length; i++) {
                runTestFailureExts[i] = new RunTestFailureExt(metadataRunTestsResult.getFailures()[i]);
            }
        }
        return runTestFailureExts;
    }

    @Override
    public IRunTestFailureExt getFailures(int i) {
        return new RunTestFailureExt(metadataRunTestsResult.getFailures()[i]);
    }

    @Override
    public IRunTestSuccessExt[] getSuccesses() {
        IRunTestSuccessExt[] runTestSuccessExts = null;
        if (Utils.isNotEmpty(metadataRunTestsResult.getSuccesses())) {
            runTestSuccessExts = new IRunTestSuccessExt[metadataRunTestsResult.getFailures().length];
            for (int i = 0; i < metadataRunTestsResult.getSuccesses().length; i++) {
                runTestSuccessExts[i] = new RunTestSuccessExt(metadataRunTestsResult.getSuccesses()[i]);
            }
        }
        return runTestSuccessExts;
    }

    @Override
    public IRunTestSuccessExt getSuccesses(int i) {
        return new RunTestSuccessExt(metadataRunTestsResult.getSuccesses()[i]);
    }

    @Override
    public List<String> getFailureMessages() {
        List<String> messages = new ArrayList<>();
        if (metadataRunTestsResult.getNumFailures() > 0) {
            metadataRunTestsResult.getFailures();
            for (int i = 0; i < metadataRunTestsResult.getFailures().length; i++) {
                messages.add("Run test error: " + metadataRunTestsResult.getFailures()[i].getMessage());
            }
        }
        return messages;
    }

    @Override
    public int getCodeCompilanceCovagePercentage() {
        return CODE_COVERAGE_COMPLIANCE_PCT;
    }

    @Override
    public String toLog() {
        StringBuffer strBuff = new StringBuffer();
        if (metadataRunTestsResult.getNumFailures() > 0) {
            strBuff.append("\n\nRun Failures:");
            for (int i = 0; i < metadataRunTestsResult.getFailures().length; i++) {
                RunTestFailure failure = metadataRunTestsResult.getFailures()[i];
                if (Utils.isNotEmpty(failure.getNamespace())) {
                    strBuff.append("\n  " + failure.getNamespace() + ".");
                } else {
                    strBuff.append("\n  ");
                }
                strBuff.append(failure.getName() + ".");
                strBuff.append(failure.getMethodName() + " ");
                strBuff.append(failure.getMessage());
            }
        }

        if (Utils.isNotEmpty(metadataRunTestsResult.getCodeCoverageWarnings())) {
            for (int i = 0; i < metadataRunTestsResult.getCodeCoverageWarnings().length; i++) {
                CodeCoverageWarning warning = metadataRunTestsResult.getCodeCoverageWarnings()[i];
                if (Utils.isNotEmpty(warning.getNamespace())) {
                    strBuff.append("\n  " + warning.getNamespace() + ".");
                } else {
                    strBuff.append("\n  ");
                }
                if (Utils.isNotEmpty(warning.getName())) {
                    strBuff.append(warning.getName() + " ");
                }
                strBuff.append(warning.getMessage());
            }
        }
        return strBuff.toString();
    }
}
