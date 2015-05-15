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
package com.salesforce.ide.core.remote.apex;

import com.salesforce.ide.test.common.NoOrgSetupTest;
import com.sforce.soap.apex.CodeCoverageResult;
import com.sforce.soap.apex.CodeCoverageWarning;
import com.sforce.soap.apex.CodeLocation;
import com.sforce.soap.apex.RunTestFailure;
import com.sforce.soap.apex.RunTestSuccess;
import com.sforce.soap.apex.RunTestsResult;

@SuppressWarnings("deprecation")
public class ApexApiModelExtTest_unit extends NoOrgSetupTest {

    public void testApexApiModelExt_validateRunTestsResultExt() {
        logStart("testApexApiModelExt_validateRunTestsResultExt");
        try {
            RunTestsResult runTestsResult = new RunTestsResult();

            CodeCoverageResult codeCoverageResult = new CodeCoverageResult();
            codeCoverageResult.setId("1234");
            codeCoverageResult.setName("name");
            codeCoverageResult.setNamespace("namespace");
            codeCoverageResult.setLocationsNotCovered(new CodeLocation[] { new CodeLocation()} );
            runTestsResult.setCodeCoverage(new CodeCoverageResult[] { codeCoverageResult });

            CodeCoverageWarning codeCoverageWarning = new CodeCoverageWarning();
            codeCoverageWarning.setMessage("message");
            codeCoverageWarning.setName("name");
            runTestsResult.setCodeCoverageWarnings(new CodeCoverageWarning[] { codeCoverageWarning });

            RunTestSuccess runTestSuccess = new RunTestSuccess();
            runTestSuccess.setId("12345");
            runTestSuccess.setMethodName("method");
            runTestSuccess.setName("name");
            runTestsResult.setSuccesses(new RunTestSuccess[] { runTestSuccess });

            RunTestFailure runTestFailure = new RunTestFailure();
            runTestFailure.setMessage("message");
            runTestFailure.setMethodName("method");
            runTestsResult.setFailures(new RunTestFailure[] { runTestFailure });
            runTestsResult.setNumFailures(1);
        } catch (Exception e) {
            handleFailure("Unable to validate Apex API RunTestsResultExt wrapper", e);
        } finally {
            logEnd("testApexApiModelExt_validateRunTestsResultExt");
        }
    }

}
