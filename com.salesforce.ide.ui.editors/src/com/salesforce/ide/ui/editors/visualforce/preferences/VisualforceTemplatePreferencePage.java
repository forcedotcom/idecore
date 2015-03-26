package com.salesforce.ide.ui.editors.visualforce.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.html.ui.internal.editor.IHelpContextIds;
import org.eclipse.wst.html.ui.internal.preferences.ui.HTMLTemplatePreferencePage;

import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;

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
        // TODO: Replace this with a non-deprecated method.
        ForceIdeEditorsPlugin.getDefault().savePluginPreferences();
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
        return ForceIdeEditorsPlugin.getDefault().getPreferenceStore();
    }

    // TODO: Inject the Visualforce template store.
    private static TemplateStore templateStore() {
        return ForceIdeEditorsPlugin.getDefault().getVisualforceTemplateStore();
    }

    // TODO: Inject the Visualforce template context registry.
    private static ContextTypeRegistry templateContextRegistry() {
        return ForceIdeEditorsPlugin.getDefault().getVisualforceTemplateContextRegistry();
    }

}
