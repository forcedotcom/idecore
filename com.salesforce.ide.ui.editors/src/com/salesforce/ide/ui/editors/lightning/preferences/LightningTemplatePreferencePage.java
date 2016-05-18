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
package com.salesforce.ide.ui.editors.lightning.preferences;

import static com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin.PLUGIN_ID;
import static com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin.getDefault;

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

public class LightningTemplatePreferencePage extends HTMLTemplatePreferencePage {
    public static final String ID = "com.salesforce.ide.ui.editors.lightning.TemplatesPreferencePage"; //$NON-NLS-1$
    
    public LightningTemplatePreferencePage() {
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
        PlatformUI.getWorkbench().getHelpSystem().setHelp(c, IHelpContextIds.HTML_PREFWEBX_TEMPLATES_HELPID);
        return c;
    }
    
    @Override
    protected boolean isShowFormatterSetting() {
        return false;
    }
    
    private static IPreferenceStore preferenceStore() {
        return getDefault().getPreferenceStore();
    }
    
    private static TemplateStore templateStore() {
        return getDefault().getLightningTemplateStore();
    }
    
    private static ContextTypeRegistry templateContextRegistry() {
        return getDefault().getLightningTemplateContextRegistry();
    }
    
    private static ILog logger() {
        return getDefault().getLog();
    }
    
}
