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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PerspectiveAdapter;

import com.salesforce.ide.ui.editors.intro.IntroPlugin;
import com.salesforce.ide.ui.editors.intro.actions.OpenIntroAction;
import com.salesforce.ide.ui.editors.intro.preferences.PreferenceConstants;
import com.salesforce.ide.ui.internal.utils.UIConstants;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public class IntroStartup implements IStartup {
    private static PerspectiveAdapter perspectivListener;

    public void earlyStartup() {
        final IPreferenceStore store = IntroPlugin.getDefault().getPreferenceStore();
        if (store.getBoolean(PreferenceConstants.SHOW_START_PAGE_ON_STARTUP)) {
            openEditorAtStartup();
        }

        perspectivListener = new PerspectiveAdapter() {
            @Override
            public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

                if (perspective.getId().equals(UIConstants.FORCE_PERSPECTIVE_ID)) {
                    if (store.getBoolean(PreferenceConstants.SHOW_START_PAGE_ON_PERSPECTIVE_OPEN)) {
                        OpenIntroAction action = new OpenIntroAction();
                        action.run(null);
                    }
                }
            }

        };

        UIUtils.addPerspectiveListener(perspectivListener);
    }

    private static void openEditorAtStartup() {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                OpenIntroAction action = new OpenIntroAction();
                action.run(null);
            }
        });
    }

    // will only actually do anything if user uninstalls our plugin and doesn't restart workbench...
    public static void removePerspectiveListener() {
        UIUtils.removePerspectiveListener(perspectivListener);
    }
}
