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
 * concrete class for eclipse 3.4 specifics
 * @author ssasalatti
 */
public class Eclipse34SpecificChecks implements IIdeTestEclipseVerSpecificChecks {

	private static final Eclipse34SpecificChecks __instance = new Eclipse34SpecificChecks();

	private Eclipse34SpecificChecks() {
	}

	public static Eclipse34SpecificChecks getInstance() {
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
