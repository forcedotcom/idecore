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
package com.salesforce.ide.core.remote;

import java.util.List;

import com.salesforce.ide.core.remote.metadata.IRunTestSuccessExt;
import com.sforce.soap.metadata.DebuggingInfo_element;

public interface IRunTestsResultExt {

	DebuggingInfo_element getDebugInfo();

	Object getTestResult();

	void setDebugInfo(DebuggingInfo_element debugInfo);

	String getNamespacePrefix();

	void setNamespacePrefix(String namespacePrefix);

	int getNumTestsRun();

	double getTotalTime();

	int getNumFailures();

	ICodeCoverageResultExt[] getCodeCoverages();

	ICodeCoverageResultExt getCodeCoverage(int i);

	ICodeCoverageWarningExt[] getCodeCoverageWarnings();

	ICodeCoverageWarningExt getCodeCoverageWarnings(int i);

	IRunTestFailureExt[] getFailures();

	IRunTestFailureExt getFailures(int i);

	IRunTestSuccessExt[] getSuccesses();

	IRunTestSuccessExt getSuccesses(int i);

	List<String> getFailureMessages();

	int getCodeCompilanceCovagePercentage();

	String toLog();

}
