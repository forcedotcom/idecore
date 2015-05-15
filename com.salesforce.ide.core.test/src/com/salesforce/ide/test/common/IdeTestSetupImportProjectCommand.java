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

import java.io.File;

import org.apache.log4j.Logger;

import com.salesforce.ide.test.common.utils.ConfigProps;
import com.salesforce.ide.test.common.utils.IdeOrgCache.OrgInfo;
import com.salesforce.ide.test.common.utils.IdeProjectFixture;
import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestOrgFactory;
import com.salesforce.ide.test.common.utils.IdeTestUtil;

/**
 * Command to import a project. This command takes care of setting up the org related info too as all that is already
 * present in the org.
 *
 * @author ssasalatti
 */
public class IdeTestSetupImportProjectCommand extends IdeTestSetupBaseProjectCommand{

    private static final Logger logger = Logger.getLogger(IdeTestSetupImportProjectCommand.class);
//    IdeSetupTest testConfig;

    public IdeTestSetupImportProjectCommand(IdeSetupTest testConfig) {
        this.testConfig = testConfig;
    }

    @Override
    public void executeSetup() throws IdeTestException {
        logger.info("Setting up project import before test...");
        if (!IdeProjectFixture.getInstance().checkIfProjectCacheClean()) {
            logger.error("Project Cache was unlean!!! purging all projects so that tests can continue.");
            IdeProjectFixture.getInstance().purgeAllProjectsFromWorkspace();
            IdeProjectFixture.getInstance().flushProjCache();
            //          throw IdeTestException.getWrappedException("Project Cache supposed to be clean for a new test.Aborting test.");
        }
        //get the import path
        String importRelPath = testConfig.importProjectFromPath(); //cannot be null at this point.
        importRelPath = IdeTestUtil.convertToOSSpecificPath(importRelPath);

        //create location of the preferences file
        final String preferencesPath =
                (new StringBuffer(importRelPath).append(File.separator).append(".settings").append(File.separator)
                        .append("com.salesforce.ide.core.prefs")).toString();

        //get the values from there.
        final String endpointServer = ConfigProps.getInstance(preferencesPath).getProperty("endpointServer");
        final String userName = ConfigProps.getInstance(preferencesPath).getProperty("username");
        final String password = ConfigProps.getInstance(preferencesPath).getProperty("password");

        //set up the org info in the cache.
        OrgInfo orgInfo =
                IdeTestOrgFactory.getRemoteOrgFixture().getOrg(testConfig.runForOrgType(), endpointServer, userName,
                    password);

        //set the builder to on/off based on setting.
        IdeProjectFixture.getInstance().switchAutoBuild(testConfig.autoBuildOn());

        //import the project
        IdeProjectFixture.getInstance().importProjectFromFS(orgInfo, importRelPath);

        //refresh the workspace
        IdeProjectFixture.getInstance().refreshWorkspace();

        logger.info("Setting up project before test...DONE");
    }



}
