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
 * Defines methods that implementors must provide for checks that are eclipse
 * specific . For. ex. upto eclipse 33( inclusive ) the standard preferences
 * menu was called Window->Preferences... but in eclipse 34 it was changed to
 * Windows->Preferences
 * 
 * @author ssasalatti
 */
public interface IIdeTestEclipseVerSpecificChecks {

	/**
	 * used for "Preferences"(3.4)/"Preferences..."(<3.4) menu option under
	 * Eclipse's Window menu
	 * 
	 * @return
	 */
	public String getWindowPreferencesMenuOptionName();

	/**
	 * used for "&Yes"(<3.4>/"OK"(3.4) button text on standard delete dialogs
	 * 
	 * @return
	 */
	public String getStandardDeleteDialogConfirmationButtonName();
}
