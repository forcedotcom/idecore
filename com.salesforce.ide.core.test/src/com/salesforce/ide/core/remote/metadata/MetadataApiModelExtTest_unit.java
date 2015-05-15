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

import junit.framework.TestCase;

import com.sforce.soap.metadata.CodeCoverageResult;
import com.sforce.soap.metadata.CodeCoverageWarning;
import com.sforce.soap.metadata.CodeLocation;
import com.sforce.soap.metadata.RunTestFailure;
import com.sforce.soap.metadata.RunTestSuccess;
import com.sforce.soap.metadata.RunTestsResult;

public class MetadataApiModelExtTest_unit extends TestCase {

	public void testMetadataApiModelExt_validateRunTestsResultExt() {
		RunTestsResult runTestsResult = new RunTestsResult();

		CodeCoverageResult codeCoverageResult = new CodeCoverageResult();
		codeCoverageResult.setName("name");
		codeCoverageResult.setNamespace("namespace");
		codeCoverageResult
				.setLocationsNotCovered(new CodeLocation[] { new CodeLocation() });
		codeCoverageResult
				.setSoqlInfo(new CodeLocation[] { new CodeLocation() });
		runTestsResult
				.setCodeCoverage(new CodeCoverageResult[] { codeCoverageResult });

		CodeCoverageWarning codeCoverageWarning = new CodeCoverageWarning();
		codeCoverageWarning.setMessage("message");
		codeCoverageWarning.setName("name");
		runTestsResult
				.setCodeCoverageWarnings(new CodeCoverageWarning[] { codeCoverageWarning });

		RunTestSuccess runTestSuccess = new RunTestSuccess();
		runTestSuccess.setMethodName("method");
		runTestSuccess.setName("name");
		runTestsResult.setSuccesses(new RunTestSuccess[] { runTestSuccess });

		RunTestFailure runTestFailure = new RunTestFailure();
		runTestFailure.setMessage("message");
		runTestFailure.setMethodName("method");
		runTestsResult.setFailures(new RunTestFailure[] { runTestFailure });
		runTestsResult.setNumFailures(1);

		RunTestsResultExt runTestsResultExt = new RunTestsResultExt(
				runTestsResult);
		assertTrue("Number of run test failures should be 1", runTestsResultExt
				.getFailures().length == 1);
		assertTrue("Validate expected failure message failed",
				runTestsResultExt.getFailureMessages().get(0).contains(
						"message"));
		assertTrue("Number of run test successes should be 1",
				runTestsResultExt.getSuccesses().length == 1);
		assertTrue("Validate expected success method name failed",
				runTestsResultExt.getSuccesses()[0].getMethodName().equals(
						"method"));
		assertTrue("Number of code coverage results should be 1",
				runTestsResultExt.getCodeCoverages().length == 1);
		assertTrue("Number of code warnings should be 1", runTestsResultExt
				.getCodeCoverageWarnings().length == 1);
		assertNotNull("RunTestsResultExt toLog should not be null",
				runTestsResultExt.toLog());
	}

}
