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
package com.salesforce.ide.ui.wizards.components;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.ApexDocumentSetupParticipant;
import com.salesforce.ide.ui.editors.apex.ApexSourceViewerConfiguration;

/**
 * Templates page in new file wizard. Allows users to select a new file
 * template to be applied in new file.
 */
public abstract class ApexCodeTemplateSelectionPage extends AbstractTemplateSelectionPage {
    private final ComponentModel componentModel;

    public ApexCodeTemplateSelectionPage(final ComponentModel componentModel, final String contextTypeId, final TemplateStore templateStore, final String pageName, final String description) {
        super(contextTypeId, templateStore, pageName, description);
        this.componentModel = componentModel;
    }

    /**
     * Creates, configures and returns a source viewer to present the template
     * pattern on the preference page. Clients may override to provide a
     * custom source viewer featuring e.g. syntax coloring.
     * 
     * @param parent
     *            the parent control
     * @return a configured source viewer
     */
    @Override
    protected final SourceViewer createViewer(Composite parent) {
        final ApexSourceViewerConfiguration configuration = new ApexSourceViewerConfiguration(preferenceStore(), null);
        configuration.init(this.componentModel.getProject());

        final IDocument document = new Document();
        new ApexDocumentSetupParticipant().setup(document);

        SourceViewer viewer= new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        viewer.configure(configuration);
        viewer.setDocument(document);

        viewer.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        viewer.setEditable(false);

        return viewer;
    }

    // TODO: Inject the editor's preference store.
    private static IPreferenceStore preferenceStore() {
        return ForceIdeEditorsPlugin.getDefault().getPreferenceStore();
    }

}
