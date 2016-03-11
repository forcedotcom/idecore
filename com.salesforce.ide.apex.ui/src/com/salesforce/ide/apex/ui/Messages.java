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
package com.salesforce.ide.apex.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.salesforce.ide.apex.ui.messages"; //$NON-NLS-1$

	public static String ApexTypeHierarchyView_View_Name;

	public static String OpenTypeView_View_Title;
	public static String OpenTypeView_View_Message;
	public static String OpenTypeView_View_Label;
	
	public static String FilteredApexResourcesSelectionDialog_Searching;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
