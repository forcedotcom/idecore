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
package com.salesforce.ide.ui.editors.intro.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.salesforce.ide.ui.editors.intro.IntroPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = IntroPlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.SHOW_START_PAGE_ON_STARTUP, false);
		store.setDefault(PreferenceConstants.SHOW_START_PAGE_ON_PERSPECTIVE_OPEN, true);
		store.setDefault(PreferenceConstants.SHOW_START_PAGE_ON_UPDATE, true);
	}
}
