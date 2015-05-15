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

import com.salesforce.ide.test.common.utils.IdeLocalTestOrgFixture;
import com.salesforce.ide.test.common.utils.IdeTestConstants;
import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestOrgFactory;
import com.salesforce.ide.test.common.utils.IdeTestUtil;
import com.salesforce.ide.test.common.utils.IdeOrgCache.OrgInfo;

/**
 * Command to setup org on local app server.
 * 
 * @author ssasalatti
 */
public class IdeTestSetupLocalOrgCommand implements IdeTestCommand {

    private static final Logger logger = Logger.getLogger(IdeTestSetupLocalOrgCommand.class);
    IdeSetupTest testConfig;
    IdeLocalTestOrgFixture localOrgFixture = IdeTestOrgFactory.getLocalOrgFixture();

    public IdeTestSetupLocalOrgCommand(IdeSetupTest testConfig) {
        this.testConfig = testConfig;
    }

    @Override
    public void executeSetup() throws IdeTestException {
        System.setProperty(IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX, testConfig.useOrgOnInstancePrefix());

        //create the org.
        logger.info("Setting up org for test...");

        OrgInfo orgInfo = localOrgFixture.getOrg(testConfig.runForOrgType(), testConfig.forceOrgCreation());

        logger.info("Checking if org is clean...");
        // Check if the org is clean or not
        String failedOrgCheck = localOrgFixture.checkIfOrgClean(orgInfo);
        if (IdeTestUtil.isNotEmpty(failedOrgCheck) && !testConfig.ignoreOrgCleanSanityCheck()) {
            //IdeOrgFixture.getInstance().forceRevokeOrgFromCache(testConfig.runForOrgType());
            logger.info("Org unclean. Forcing Org creation ...");
            orgInfo = localOrgFixture.getOrg(testConfig.runForOrgType(), true);
        }

        //enable org perm if any.
        if (IdeTestUtil.isNotEmpty(testConfig.enableOrgPerms())) {
            logger.info("Enabling perm on org...");
            localOrgFixture.enableOrgPerm(orgInfo, testConfig.enableOrgPerms());
        }
        
        if (testConfig.useDebugApexStreamingUser()) {
            logger.info("Get or create apex debug streaming user...");
            localOrgFixture.useApexDebugStreamingUser(orgInfo);
        } else {
	        if (IdeTestUtil.isNotEmpty(testConfig.enableUserPerms())) {
	            logger.info("Enabling user perm on org...");
	            localOrgFixture.enableUserPerm(orgInfo, testConfig.enableUserPerms());
	        }
        }
        

        logger.info("Setting up org for test...DONE");
    }

    @Override
    public void executeTearDown() throws IdeTestException {
        logger.info("Tearing down org after test...I don't do much!");

        //revoke org perm if any
        if (IdeTestUtil.isNotEmpty(testConfig.enableOrgPerms())) {
            logger.info("Revoking perm on org...");
            localOrgFixture.revokeOrgPerm(localOrgFixture.getOrg(testConfig.runForOrgType()), testConfig
                    .enableOrgPerms());
        }

        // Check if the org is clean or not
        if (testConfig.forceRevokeOrgFromLocalOrgCacheAfterTest()) {
            logger.info("Ignoring the check for org being clean. This will recreate the org for future tests.");
            //hose that org as it might flap a whole bunch of tests later.
            localOrgFixture.forceRevokeOrgFromCache(testConfig.runForOrgType());
        }

        //shouldn't we delete orgs
        //TODO: maybe mark them as split.
        logger.info("Tearing down org after test...DONE");
    }

}
