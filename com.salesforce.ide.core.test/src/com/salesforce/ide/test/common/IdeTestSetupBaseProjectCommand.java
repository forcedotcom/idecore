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

import com.salesforce.ide.test.common.utils.IdeProjectFixture;
import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.OrgTypeEnum;

/**
 * The base class for project create and import project commands. as the teardown is the same.
 * 
 * @author ssasalatti
 */
public abstract class IdeTestSetupBaseProjectCommand implements IdeTestCommand {

    private static final Logger logger = Logger.getLogger(IdeTestSetupBaseProjectCommand.class);
    IdeSetupTest testConfig;

    public abstract void executeSetup() throws IdeTestException;

    public void executeTearDown() throws IdeTestException {
        logger.info("Tearing down project after test...");
        OrgTypeEnum orgType = testConfig.runForOrgType();
        //delete the project only it hasn't been explicitly asked to override.
        if (!testConfig.ingnoreProjectCleanAfterTestCheck()) {
            IdeProjectFixture.getInstance().deleteAllProjectsFromWorkspace(orgType);
            IdeProjectFixture.getInstance().refreshWorkspace();
        
            if (!IdeProjectFixture.getInstance().checkIfProjectCacheClean())
                throw IdeTestException
                        .getWrappedException("Project Cache wasn't cleaned up. Did you forget to delete a project you specifically created in the test?");
            logger.info("Tearing down project after test...DONE");
        }else{
            //project check override was requested.
            //flush the cache
            IdeProjectFixture.getInstance().flushProjCache();
        }
    }
}
