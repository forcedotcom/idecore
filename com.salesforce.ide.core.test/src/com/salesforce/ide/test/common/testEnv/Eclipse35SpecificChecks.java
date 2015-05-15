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

public class Eclipse35SpecificChecks implements IIdeTestEclipseVerSpecificChecks {

	private static final Eclipse35SpecificChecks __instance = new Eclipse35SpecificChecks();

	private Eclipse35SpecificChecks() {
	}

	public static Eclipse35SpecificChecks getInstance() {
		return __instance;
	}

	public String getWindowPreferencesMenuOptionName() {
		final String windowPreferencesMenuOptionName = "Preferences";
		return windowPreferencesMenuOptionName;
	}

	public String getStandardDeleteDialogConfirmationButtonName() {
		return "OK";
	}
}
