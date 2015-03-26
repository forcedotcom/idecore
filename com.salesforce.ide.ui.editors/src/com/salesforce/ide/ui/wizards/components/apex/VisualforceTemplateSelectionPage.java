package com.salesforce.ide.ui.wizards.components.apex;

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
    protected final SourceViewer createViewer(Composite parent) {
        SourceViewerConfiguration sourceViewerConfiguration = new StructuredTextViewerConfiguration() {
            StructuredTextViewerConfiguration baseConfiguration = new StructuredTextViewerConfigurationHTML();

            public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
                return baseConfiguration.getConfiguredContentTypes(sourceViewer);
            }

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