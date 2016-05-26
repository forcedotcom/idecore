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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.html.core.internal.provisional.contenttype.ContentTypeIdForHTML;
import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.StructuredTextViewerConfiguration;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.sse.ui.internal.provisional.style.LineStyleProvider;

/**
 * Templates page in new file wizard. Allows users to select a new file
 * template to be applied in new file.
 */
public abstract class VisualforceTemplateSelectionPage extends AbstractTemplateSelectionPage {

    public VisualforceTemplateSelectionPage(final String contextTypeId, final TemplateStore templateStore, final String pageName, final String description) {
        super(contextTypeId, templateStore, pageName, description);
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
        SourceViewerConfiguration sourceViewerConfiguration = new StructuredTextViewerConfiguration() {
            StructuredTextViewerConfiguration baseConfiguration = new StructuredTextViewerConfigurationHTML();

            @Override
            public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
                return baseConfiguration.getConfiguredContentTypes(sourceViewer);
            }

            @Override
            public LineStyleProvider[] getLineStyleProviders(ISourceViewer sourceViewer, String partitionType) {
                return baseConfiguration.getLineStyleProviders(sourceViewer, partitionType);
            }
        };
        SourceViewer viewer = new StructuredTextViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        viewer.getTextWidget().setFont(JFaceResources.getFont("org.eclipse.wst.sse.ui.textfont")); //$NON-NLS-1$
        IStructuredModel scratchModel = StructuredModelManager.getModelManager().createUnManagedStructuredModelFor(ContentTypeIdForHTML.ContentTypeID_HTML);
        IDocument document = scratchModel.getStructuredDocument();
        viewer.configure(sourceViewerConfiguration);
        viewer.setDocument(document);
        return viewer;
    }

}
