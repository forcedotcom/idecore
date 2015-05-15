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
package com.salesforce.ide.test.common.testEnv;

/**
 * concrete class for win32 specific checks
 * 
 * @author ssasalatti
 * 
 */
public class Win32SpecificChecks implements IIdeTestOSSpecificChecks {

	private static final Win32SpecificChecks _instance = new Win32SpecificChecks();

	public static Win32SpecificChecks getInstance() {
		return _instance;
	}

	private Win32SpecificChecks() {
	}

	public String getPreferencesMenuLocation() {
		return "Window";
	}

}
