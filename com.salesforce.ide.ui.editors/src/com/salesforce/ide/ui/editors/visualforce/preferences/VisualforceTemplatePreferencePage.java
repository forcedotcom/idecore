/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.editors.visualforce.preferences;

import static com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin.*;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.html.ui.internal.editor.IHelpContextIds;
import org.eclipse.wst.html.ui.internal.preferences.ui.HTMLTemplatePreferencePage;
import org.osgi.service.prefs.BackingStoreException;

public class VisualforceTemplatePreferencePage extends HTMLTemplatePreferencePage {
    public static final String ID = "com.salesforce.ide.ui.editors.visualforce.TemplatesPreferencePage"; //$NON-NLS-1$

    public VisualforceTemplatePreferencePage() {
        setPreferenceStore(preferenceStore());
        setTemplateStore(templateStore());
        setContextTypeRegistry(templateContextRegistry());
    }

    @Override
    public boolean performOk() {
        boolean ok = super.performOk();
        try {
            InstanceScope.INSTANCE.getNode(PLUGIN_ID).flush();
        } catch (BackingStoreException e) {
            final String msg = "Unable to save template preferences";
            logger().log(new Status(IStatus.ERROR, PLUGIN_ID, msg, e));
        }
        return ok;
      }

    @Override
    protected Control createContents(Composite ancestor) {
        Control c = super.createContents(ancestor);
        // TODO: Inject the help system.
        // TODO: Set the help system's context for the template preference page
        PlatformUI.getWorkbench().getHelpSystem().setHelp(c, IHelpContextIds.HTML_PREFWEBX_TEMPLATES_HELPID);
        return c;
    }

    @Override
    protected boolean isShowFormatterSetting() {
        // Hide the formatter preference checkbox until the IDE supports code formatting
        return false;
    }

    // TODO: Inject the editor's preference store.
    private static IPreferenceStore preferenceStore() {
        return getDefault().getPreferenceStore();
    }

    // TODO: Inject the Visualforce template store.
    private static TemplateStore templateStore() {
        return getDefault().getVisualforceTemplateStore();
    }

    // TODO: Inject the Visualforce template context registry.
    private static ContextTypeRegistry templateContextRegistry() {
        return getDefault().getVisualforceTemplateContextRegistry();
    }

    // TODO: Inject the editor's logger.
    private static ILog logger() {
        return getDefault().getLog();
    }

}
