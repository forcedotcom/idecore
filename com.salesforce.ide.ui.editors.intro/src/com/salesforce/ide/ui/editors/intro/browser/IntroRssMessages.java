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
package com.salesforce.ide.ui.editors.intro.browser;

import org.eclipse.osgi.util.NLS;

public class IntroRssMessages {
	private static final String BUNDLE_NAME = "com.salesforce.ide.ui.editors.intro.browser.IntroRssMessages"; //$NON-NLS-1$

	public static String IntroRss_url;
	
	public static String IntroRss_morePosts_url;
	public static String IntroRss_morePosts_label;
	
	public static String IntroSampleProjects_url;
	
	public static String IntroSampleProjects_moreProjects_url;
	public static String IntroSampleProjects_moreProjects_label;

	static {
		NLS.initializeMessages(BUNDLE_NAME, IntroRssMessages.class);
	}
	
	private IntroRssMessages()
	{}
}
