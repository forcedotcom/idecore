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
package com.salesforce.ide.ui.editors.intro;

import org.eclipse.osgi.util.NLS;

public class Messages {
	private static final String BUNDLE_NAME = "com.salesforce.ide.ui.editors.intro.Messages"; //$NON-NLS-1$

	public static String IntroEditor_name;
	public static String IntroEditor_tooltip;
	
	public static String IntroPreference_ShowGroup_message;
	public static String IntroPreference_ShowOnStartup_message;
	public static String IntroPreference_ShowOnPerspectiveOpen_message;
	public static String IntroPreference_ShowOnUpdate_message;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages()
	{}
}
