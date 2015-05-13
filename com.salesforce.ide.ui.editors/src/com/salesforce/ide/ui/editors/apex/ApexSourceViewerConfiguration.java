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
package com.salesforce.ide.ui.editors.apex;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.hover.JavaEditorTextHoverDescriptor;
import org.eclipse.jdt.internal.ui.text.java.hover.JavaEditorTextHoverProxy;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.assistance.ApexAutoIndentStrategy;
import com.salesforce.ide.ui.editors.apex.assistance.ApexCodeScanner;
import com.salesforce.ide.ui.editors.apex.assistance.ApexDocScanner;
import com.salesforce.ide.ui.editors.apex.assistance.ApexDoubleClickSelector;
import com.salesforce.ide.ui.editors.apex.util.ApexCodeColorProvider;
import com.salesforce.ide.ui.editors.internal.apex.completions.ApexCompletionCollector;

/**
 * Source viewer configuration for Apex editor.
 * 
 * @author nchen
 * 
 */
@SuppressWarnings({ "restriction" })
public class ApexSourceViewerConfiguration extends TextSourceViewerConfiguration {

    protected final static String APEX_PARTITIONING = "__apex_partitioning"; //$NON-NLS-1$

    private ITextEditor fTextEditor;
    private ApexCodeScanner apexCodeScanner = null;
    private ApexDocScanner apexDocScanner = null;
    private ApexCodeColorProvider apexCodeColorProvider = null;
    private ApexCompletionCollector apexCompletionCollector = null;

    /**
     * Single token scanner.
     */
    static class SingleTokenScanner extends BufferedRuleBasedScanner {
        public SingleTokenScanner(TextAttribute attribute) {
            setDefaultReturnToken(new Token(attribute));
        }
    }

    public ApexSourceViewerConfiguration() {}

    /**
     * Default constructor.
     */
    public ApexSourceViewerConfiguration(IPreferenceStore preferenceStore, ITextEditor editor) {
        super(preferenceStore);
        fTextEditor = editor;
        apexCodeScanner = (ApexCodeScanner) ContainerDelegate.getInstance().getBean(ApexCodeScanner.class);
        apexDocScanner = (ApexDocScanner) ContainerDelegate.getInstance().getBean(ApexDocScanner.class);
        apexCompletionCollector = new ApexCompletionCollector(fTextEditor);
        apexCodeColorProvider = ForceIdeEditorsPlugin.getApexCodeColorProvider();
    }

    public void init(IProject project) {
        apexCodeScanner.init(project);
        apexDocScanner.init();
    }

    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return new ApexAnnotationHover() {
            @Override
            protected boolean isIncluded(Annotation annotation) {
                return isShowInVerticalRuler(annotation);
            }
        };
    }

    @Override
    public IAnnotationHover getOverviewRulerAnnotationHover(ISourceViewer sourceViewer) {
        return getAnnotationHover(sourceViewer);
    }

    @Override
    public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
        JavaEditorTextHoverDescriptor[] hoverDescs = JavaPlugin.getDefault().getJavaEditorTextHoverDescriptors();
        int stateMasks[] = new int[hoverDescs.length];
        int stateMasksLength = 0;
        for (int i = 0; i < hoverDescs.length; i++) {
            if (hoverDescs[i].isEnabled()) {
                int j = 0;
                int stateMask = hoverDescs[i].getStateMask();
                while (j < stateMasksLength) {
                    if (stateMasks[j] == stateMask)
                        break;
                    j++;
                }
                if (j == stateMasksLength)
                    stateMasks[stateMasksLength++] = stateMask;
            }
        }
        if (stateMasksLength == hoverDescs.length)
            return stateMasks;

        int[] shortenedStateMasks = new int[stateMasksLength];
        System.arraycopy(stateMasks, 0, shortenedStateMasks, 0, stateMasksLength);
        return shortenedStateMasks;
    }

    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
        JavaEditorTextHoverDescriptor[] hoverDescs = JavaPlugin.getDefault().getJavaEditorTextHoverDescriptors();
        int i = 0;
        while (i < hoverDescs.length) {
            if (hoverDescs[i].isEnabled() && hoverDescs[i].getStateMask() == stateMask)
                return new JavaEditorTextHoverProxy(hoverDescs[i], fTextEditor);
            i++;
        }

        return null;
    }

    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        return getTextHover(sourceViewer, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
    }

    @Override
    public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
        return new IInformationControlCreator() {
            @Override
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent, new HTMLTextPresenter(true));
            }
        };
    }

    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
        IAutoEditStrategy strategy =
                (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType) ? new ApexAutoIndentStrategy()
                        : new DefaultIndentLineAutoEditStrategy());

        return new IAutoEditStrategy[] { strategy };
    }

    @Override
    public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
        return APEX_PARTITIONING;
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] { IDocument.DEFAULT_CONTENT_TYPE, ApexPartitionScanner.APEX_DOC,
                ApexPartitionScanner.APEX_MULTILINE_COMMENT };
    }

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        ContentAssistant assistant = new ContentAssistant();

        assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

        if (apexCompletionCollector != null) {
            assistant.setContentAssistProcessor(apexCompletionCollector, IDocument.DEFAULT_CONTENT_TYPE);
        }

        assistant.enableAutoActivation(true);
        assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_STACKED);
        assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
        assistant.setShowEmptyList(true); // display no proposal when code assist is triggered by ctrl+space, but display nothing when triggered by auto-activation

        if (apexCodeColorProvider != null) {
            assistant.setContextInformationPopupBackground(apexCodeColorProvider.getColor(new RGB(150, 150, 0)));
        }

        assistant.addCompletionListener(new ICompletionListener() {
            @Override
            public void assistSessionEnded(ContentAssistEvent event) {
                ((ApexCompletionCollector) event.assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE))
                        .clearState();
            }

            @Override
            public void assistSessionStarted(ContentAssistEvent event) {}

            @Override
            public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {}
        });

        assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
        return assistant;
    }

    public String getDefaultPrefix(ISourceViewer sourceViewer, String contentType) {
        return (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType) ? "//" : null); //$NON-NLS-1$
    }

    @Override
    public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
        return new ApexDoubleClickSelector();
    }

    @Override
    public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
        return new String[] { "\t", "    " }; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

        DefaultDamagerRepairer dr = null;
        if (apexCodeScanner != null) {
            dr = new DefaultDamagerRepairer(apexCodeScanner);
            reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
            reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
        }
        if (apexDocScanner != null) {
            dr = new DefaultDamagerRepairer(apexDocScanner);
            reconciler.setDamager(dr, ApexPartitionScanner.APEX_DOC);
            reconciler.setRepairer(dr, ApexPartitionScanner.APEX_DOC);
        }

        if (apexCodeColorProvider != null) {
            dr =
                    new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(
                            apexCodeColorProvider.getColor(ApexCodeColorProvider.MULTI_LINE_COMMENT))));
            reconciler.setDamager(dr, ApexPartitionScanner.APEX_MULTILINE_COMMENT);
            reconciler.setRepairer(dr, ApexPartitionScanner.APEX_MULTILINE_COMMENT);
        }
        return reconciler;
    }

    @Override
    public IReconciler getReconciler(ISourceViewer sourceViewer) {
        if (fTextEditor != null && fTextEditor.isEditable()) {
            ApexReconcilingStrategy strategy = new ApexReconcilingStrategy((ApexCodeEditor) fTextEditor);
            ApexReconciler reconciler = new ApexReconciler(fTextEditor, strategy, false);
            reconciler.setIsAllowedToModifyDocument(false);
            reconciler.setDelay(500);

            return reconciler;
        }
        return null;
    }
}
