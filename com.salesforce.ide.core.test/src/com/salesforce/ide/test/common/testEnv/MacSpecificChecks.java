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
 * concrete class for Mac specific checks
 * 
 * @author ssasalatti
 * 
 */
public class MacSpecificChecks implements IIdeTestOSSpecificChecks {

	private static final MacSpecificChecks _instance = new MacSpecificChecks();

	public static MacSpecificChecks getInstance() {
		return _instance;
	}

	private MacSpecificChecks() {
	}

	public String getPreferencesMenuLocation() {
		return "Eclipse";
	}

}
