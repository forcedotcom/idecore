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

/**
 * A dummy command. 
 * @author ssasalatti
 */
public class IdeTestNoOrgNoDataNoProjectCommand implements IdeTestCommand {
	private static final Logger logger = Logger.getLogger(IdeTestNoOrgNoDataNoProjectCommand.class);
	public void executeSetup() {
		//do nothing
		logger.info("Setting up Test with NO Org && NO Data && NO Project");
	}

	public void executeTearDown() {
		//do nothing
		logger.info("Tearing down Test with NO Org && NO Data && NO Project");
	}

}
