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


/**
 * common interface for executing a test setup command
 * This is an implementation of the command pattern.
 * @author ssasalatti
 *
 */
public interface IdeTestCommand {
	public void executeSetup() throws IdeTestException ;
	public void executeTearDown() throws IdeTestException ;
	

}
