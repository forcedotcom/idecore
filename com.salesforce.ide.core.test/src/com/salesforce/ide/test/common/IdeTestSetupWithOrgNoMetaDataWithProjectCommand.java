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

import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestOrgFactory;

/**
 * Command to setup the org and project but no extra data
 * @author ssasalatti
 *
 */
public class IdeTestSetupWithOrgNoMetaDataWithProjectCommand implements
		IdeTestCommand {
	
	IdeSetupTest testConfig;
	
	public IdeTestSetupWithOrgNoMetaDataWithProjectCommand(IdeSetupTest testConfig) {
		this.testConfig = testConfig;
	}

	public void executeSetup() throws IdeTestException {
	    IdeTestOrgFactory.getTestSetupOrgCommand(testConfig).executeSetup();
		new IdeTestSetupCreateProjectCommand(testConfig).executeSetup();

	}

	public void executeTearDown() throws IdeTestException {
		new IdeTestSetupCreateProjectCommand(testConfig).executeTearDown();
		IdeTestOrgFactory.getTestSetupOrgCommand(testConfig).executeTearDown();
	}

}
