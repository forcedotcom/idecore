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
package com.salesforce.ide.ui.editors.apex.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public PreferenceInitializer() {}

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = ForceIdeEditorsPlugin.getDefault().getPreferenceStore();
        store.setDefault(PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS, true);
        store.setDefault(PreferenceConstants.EDITOR_MATCHING_BRACKETS, true);
        store.setDefault(PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR, "192,192,192"); //$NON-NLS-1$
        store.setDefault(PreferenceConstants.COMPILER_SOURCE, "1.5"); //$NON-NLS-1$
        store.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACKETS, true);
        store.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACES, true);
        store.setDefault(PreferenceConstants.EDITOR_CLOSE_STRINGS, true);
        store.setDefault(PreferenceConstants.EDITOR_PARSE_WITH_NEW_COMPILER, true);
        store.setDefault(PreferenceConstants.EDITOR_AUTOCOMPLETION, true);
    }
}
