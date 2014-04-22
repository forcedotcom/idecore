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
package com.salesforce.ide.core.remote.metadata;

import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.RunTestsResult;

public class EmptyDeployResultExt implements IDeployResultExt {

	public String getDebugLog() {
		return null;
	}

	public DeployResult getDeployResult() {
		return null;
	}

	public int getMessageCount() {
		return 0;
	}

	public DeployMessageExt getMessageHandler() {
		return null;
	}

	public RetrieveResultExt getRetrieveResultHandler() {
		return null;
	}

	public RunTestsResult getRunTestsResult() {
		return null;
	}

	public RunTestsResultExt getRunTestsResultHandler() {
		return null;
	}

	public boolean hasMessages() {
		return false;
	}

	public boolean hasRetriveResult() {
		return false;
	}

	public boolean isSuccess() {
		return false;
	}

}
