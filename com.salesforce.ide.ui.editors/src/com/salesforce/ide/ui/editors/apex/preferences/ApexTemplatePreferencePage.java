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
package com.salesforce.ide.ui.editors.apex.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.ApexDocumentSetupParticipant;
import com.salesforce.ide.ui.editors.apex.ApexSourceViewerConfiguration;

public class ApexTemplatePreferencePage extends TemplatePreferencePage {
    public static final String ID = "com.salesforce.ide.ui.editors.apex.TemplatesPreferencePage"; //$NON-NLS-1$

    public ApexTemplatePreferencePage() {
        setPreferenceStore(preferenceStore());
        setTemplateStore(templateStore());
        setContextTypeRegistry(templateContextRegistry());
    }

    @Override
    protected SourceViewer createViewer(Composite parent) {
        final SourceViewer viewer = super.createViewer(parent);

        new ApexDocumentSetupParticipant().setup(viewer.getDocument());

        final ApexSourceViewerConfiguration configuration = new ApexSourceViewerConfiguration(preferenceStore(), null);
        configuration.init(null);
        viewer.unconfigure();
        viewer.configure(configuration);

        viewer.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        viewer.setEditable(false);

        return viewer;
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

    // TODO: Inject the Apex template store.
    private static TemplateStore templateStore() {
        return ForceIdeEditorsPlugin.getDefault().getApexTemplateStore();
    }

    // TODO: Inject the Apex template context registry.
    private static ContextTypeRegistry templateContextRegistry() {
        return ForceIdeEditorsPlugin.getDefault().getApexTemplateContextRegistry();
    }

}
