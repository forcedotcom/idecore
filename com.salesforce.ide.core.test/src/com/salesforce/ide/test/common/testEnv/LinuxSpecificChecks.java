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
 * concrete class for linux specific checks
 * 
 * @author ssasalatti
 */
public class LinuxSpecificChecks implements IIdeTestOSSpecificChecks {

	private static final LinuxSpecificChecks _instance = new LinuxSpecificChecks();

	public static LinuxSpecificChecks getInstance() {
		return _instance;
	}

	private LinuxSpecificChecks() {
	}

	public String getPreferencesMenuLocation() {
		// it's the same for linux and windows. Calling from win32 concrete
		// class to keep single point of change
		return Win32SpecificChecks.getInstance().getPreferencesMenuLocation();
	}

}
