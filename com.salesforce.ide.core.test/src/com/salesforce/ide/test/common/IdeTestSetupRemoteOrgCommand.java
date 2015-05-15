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
package com.salesforce.ide.test.common;

import org.apache.log4j.Logger;

import com.salesforce.ide.test.common.utils.IdeRemoteTestOrgFixture;
import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestOrgFactory;
import com.salesforce.ide.test.common.utils.IdeTestUtil;

/**
 * Command to setup Org on the Remote App server. Doesn't really create an org but just persists org related info within
 * the test framework for a particular test.
 * 
 * @author ssasalatti
 */
public class IdeTestSetupRemoteOrgCommand implements IdeTestCommand {
    private static final Logger logger = Logger.getLogger(IdeTestSetupLocalOrgCommand.class);
    IdeSetupTest testConfig;
    IdeRemoteTestOrgFixture remoteOrgFixture = IdeTestOrgFactory.getRemoteOrgFixture();

    public IdeTestSetupRemoteOrgCommand(IdeSetupTest testConfig) {
        this.testConfig = testConfig;
    }

    public void executeSetup() throws IdeTestException {

        //get the org.
        logger.info("Setting up Remote org for test...");

        remoteOrgFixture.getOrg(testConfig.runForOrgType());

        logger.info("Setting up Remote org for test...DONE");
    }

    public void executeTearDown() throws IdeTestException {
        logger.info("Tearing down Remote org after test...I don't do much! Check for Org Clean and remove from Cache.");
        
        // Check if the org is clean or not
        if (!testConfig.ignoreOrgCleanSanityCheck()) {
            logger.info("Checking if remote org is clean.");
            String failedOrgCheck =
                    remoteOrgFixture.checkIfOrgClean(remoteOrgFixture.getOrg(testConfig.runForOrgType()));
            if (IdeTestUtil.isNotEmpty(failedOrgCheck))
                throw IdeTestException.getWrappedException(failedOrgCheck);
        }
        //get rid of org from the cache.
        //this is being done so to achieve the following
        //1) if test 1 wants to use org A, and specifies it as a particular orgType(Developer by defailt). It goes into the cache.
        //2) if test 2 wants to use org B, and specifies it as a particular orgType which was the same as Org A or doesn't specify anything,
        //    in which case it is flagged as Developer, then the Cache would return an old value which came from test 1, 
        //    and if the previous test( i.e. test 1) did not remove entry from the cache during teardown, the old org A will be returned where you actually wanted org B 
        //hence we remove orgs from cache.
        logger.info("Purging Remote Org Cache after Test");            
        remoteOrgFixture.getOrgCacheInstance().purgeCache();
        logger.info("Tearing down org after test...DONE");
    }

}
