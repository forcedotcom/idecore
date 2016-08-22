/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.wizards.components.apex.test;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.salesforce.ide.ui.wizards.components.apex.test.messages"; //$NON-NLS-1$
	
	public static String ApexTestSuiteSelectionPage_Title;
	public static String ApexTestSuiteSelectionPage_Description;
	public static String ApexTestSuiteSelectionPage_TableColumnTitle;
	public static String ApexTestSuiteSelectionPage_SelectAll;
	public static String ApexTestSuiteSelectionPage_DeselectAll;
	public static String ApexTestSuiteSelectionPage_NoTests;
	
	static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {}
}
