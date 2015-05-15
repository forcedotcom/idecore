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
 * Concrete class for Eclipse 3.3 specifics
 * @author ssasalatti
 */
public class Eclipse33SpecificChecks implements IIdeTestEclipseVerSpecificChecks {

	private static final Eclipse33SpecificChecks __instance = new Eclipse33SpecificChecks();

	private Eclipse33SpecificChecks() {
	}

	public static Eclipse33SpecificChecks getInstance() {
		return __instance;
	}

	public String getWindowPreferencesMenuOptionName() {
		final String windowPreferencesMenuOptionName = "Preferences...";
		return windowPreferencesMenuOptionName;
	}

	public String getStandardDeleteDialogConfirmationButtonName() {
		return "&Yes";
	}
}
