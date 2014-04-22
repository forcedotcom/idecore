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

public interface IDeployResultExt {

	public abstract DeployResult getDeployResult();

	public abstract RunTestsResult getRunTestsResult();

	public abstract RunTestsResultExt getRunTestsResultHandler();

	public abstract DeployMessageExt getMessageHandler();

	public abstract boolean hasRetriveResult();

	public abstract RetrieveResultExt getRetrieveResultHandler();

	public abstract int getMessageCount();

	public abstract boolean hasMessages();

	public abstract boolean isSuccess();

	public abstract String getDebugLog();

}
