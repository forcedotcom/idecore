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

import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestOrgFactory;

public class IdeTestSetupWithOrgWithMetadataAsInstalledPkgNoProjectCommand implements IdeTestCommand {

    private static final Logger logger = Logger.getLogger(IdeTestSetupWithOrgWithMetadataAsInstalledPkgNoProjectCommand.class);

    private IdeSetupTest testConfig;

    public IdeTestSetupWithOrgWithMetadataAsInstalledPkgNoProjectCommand(IdeSetupTest testConfig) {
        this.testConfig = testConfig;
    }

    public void executeSetup() throws IdeTestException {
    IdeTestOrgFactory.getLocalOrgFixture().createOrgWithInstalledPkgInIt(testConfig.runForOrgType(),testConfig.addMetaDataFromPath(),testConfig.addMetadataDataAsPackage());

    }

    public void executeTearDown() throws IdeTestException {
        logger.info("Executing tear down:"+IdeTestSetupWithOrgWithMetadataAsInstalledPkgNoProjectCommand.class.getName()+"...BEGIN");
        logger.info("Removing org type '" + testConfig.runForOrgType() + "' from cache.");
        IdeTestOrgFactory.getLocalOrgFixture().forceRevokeOrgFromCache(testConfig.runForOrgType());
        logger.info("Executing tear down:"+IdeTestSetupWithOrgWithMetadataAsInstalledPkgNoProjectCommand.class.getName()+"...DONE");

    }

}
