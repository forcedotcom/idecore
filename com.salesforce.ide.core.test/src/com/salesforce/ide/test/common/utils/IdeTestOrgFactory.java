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
package com.salesforce.ide.test.common.utils;

import com.salesforce.ide.test.common.IdeSetupTest;
import com.salesforce.ide.test.common.IdeTestCommand;
import com.salesforce.ide.test.common.IdeTestSetupLocalOrgCommand;
import com.salesforce.ide.test.common.IdeTestSetupRemoteOrgCommand;


/**
 * An Test Org Factory that can return fixtures and commands based on the instance against which test is running . ( local or remote )
 * 
 * @author ssasalatti
 */
public class IdeTestOrgFactory {

	/**
	 * @return a local org fixture.
	 */
	public static IdeLocalTestOrgFixture getLocalOrgFixture() {
		return new IdeLocalTestOrgFixture();
	}

	/**
	 * @return a remote org fixture.
	 */
	public static IdeRemoteTestOrgFixture getRemoteOrgFixture() {
		return new IdeRemoteTestOrgFixture();
	}

	/**
	 * @return a local or a remote org fixture depending on the system property set in an annotation.
	 * @throws IdeTestException 
	 */
	public static IdeOrgFixture getOrgFixture() throws IdeTestException {
		IdeOrgFixture retFixture = null;
		String server = System
				.getProperty(IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX);
		
		if(IdeTestUtil.isEmpty(server))
		    IdeTestException.wrapAndThrowException("System property for custom org prefix couldn't be retrieved.");
		
		if (server.toLowerCase().equalsIgnoreCase(
				IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX_VALUE_LOCAL))
			retFixture = new IdeLocalTestOrgFixture();
		else
			retFixture = new IdeRemoteTestOrgFixture();

		return retFixture;
	}
	
	/**
	 * returns a test setup command( local or remote) based on the system property set in the annotation.
	 * @param testConfig
	 * @return
	 * @throws IdeTestException 
	 */
	public static IdeTestCommand getTestSetupOrgCommand(IdeSetupTest testConfig) throws IdeTestException{
		IdeTestCommand retCommand = null;
		String server = System
				.getProperty(IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX);
		if(IdeTestUtil.isEmpty(server))
            IdeTestException.wrapAndThrowException("System property for custom org prefix couldn't be retrieved.");
        
		if (server.toLowerCase().equalsIgnoreCase(
				IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX_VALUE_LOCAL))
			retCommand = new IdeTestSetupLocalOrgCommand(testConfig);
		else
			retCommand = new IdeTestSetupRemoteOrgCommand(testConfig);

		return retCommand;
	}
	
}
