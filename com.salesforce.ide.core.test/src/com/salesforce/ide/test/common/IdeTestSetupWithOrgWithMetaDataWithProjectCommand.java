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

/**
 * Command to setup org with data and with project
 * @author ssasalatti
 */
public class IdeTestSetupWithOrgWithMetaDataWithProjectCommand implements IdeTestCommand {
    
    private static final Logger logger = Logger.getLogger(IdeTestSetupWithOrgWithMetaDataWithProjectCommand.class);
    
	IdeSetupTest testConfig;

	public IdeTestSetupWithOrgWithMetaDataWithProjectCommand(IdeSetupTest testConfig) {
		this.testConfig = testConfig;
	}

	public void executeSetup() throws IdeTestException  {
	    logger.info("Setting up Org With Data With Project for test....");
		new IdeTestSetupWithOrgWithMetaDataNoProjectCommand(testConfig).executeSetup();
		new IdeTestSetupCreateProjectCommand(testConfig).executeSetup();
		logger.info("Setting up Org With Data With Project for test....DONE");
	}

	public void executeTearDown() throws IdeTestException {
	    logger.info("Tearing down Org With Data With Project after test....");
		new IdeTestSetupCreateProjectCommand(testConfig).executeTearDown();
		new IdeTestSetupWithOrgWithMetaDataNoProjectCommand(testConfig).executeTearDown();
		logger.info("Tearing down Org With Data With Project after test....DONE");
	}

}
