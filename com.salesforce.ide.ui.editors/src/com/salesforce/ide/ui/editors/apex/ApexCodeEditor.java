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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.internal.ui.text.JavaPairMatcher;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.ForceIdeUIPlugin;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.outline.ApexContentOutlinePage;
import com.salesforce.ide.ui.editors.apex.outline.OutlineViewDispatcher;
import com.salesforce.ide.ui.editors.apex.preferences.PreferenceConstants;
import com.salesforce.ide.ui.editors.apex.util.ParserLocationTranslator.HighlightRange;
import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;

/**
 * Apex specific code editor
 * 
 * @author cwall (Modified by nchen)
 */
@SuppressWarnings("restriction")
public class ApexCodeEditor extends TextEditor implements IShowInSource {
    private static final Logger logger = Logger.getLogger(ApexCodeEditor.class);

    public static final String EDITOR_NAME = "Apex Code Editor";
    private static final String ANNOTATION_TYPE_APEX_WARNING = "org.eclipse.ui.workbench.texteditor.warning";
    private static final String ANNOTATION_TYPE_APEX_ERROR = "org.eclipse.ui.workbench.texteditor.error";
    private static final String ACTION_DEFINE_FOLDING_REGION = "DefineFoldingRegion";
    private static final String ACTION_CONTENT_ASSIST_TIP = "ContentAssistTip";
    private static final String ACTION_CONTENT_ASSIST_PROPOSAL = "ContentAssistProposal";

    protected final static char[] BRACKETS = { '{', '}', '(', ')', '[', ']', '<', '>' };

    /** The editor's bracket matcher */
    protected JavaPairMatcher fBracketMatcher = new JavaPairMatcher(BRACKETS);

    /** The bracket inserter. */
    private BracketInserter fBracketInserter;

    protected ApexContentOutlinePage fOutlinePage; // The outline page
    private ProjectionSupport fProjectionSupport; // The projection support
    private IProject project = null;
    private final OutlineUpdateResourceListener outlineUpdateResourceListener = new OutlineUpdateResourceListener();
    private final AbstractSelectionChangedListener outlineSelectionChangedListener = new OutlineSelectionChangedListener();
    private EditorSelectionChangedListener editorSelectionChangedListener;
    private ApexSourceViewerConfiguration apexSourceViewerConfiguration = null;

    private final Object fReconcilerLock = new Object();
    
    public Object getReconcilerLock() {
        return fReconcilerLock;
    }

    public ApexCodeEditor(IProject project) {
        super();
        this.project = project;
        initializeEditor();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.addResourceChangeListener(outlineUpdateResourceListener);
    }

    // M E T H O D S
    @Override
    public ShowInContext getShowInContext() {
        ShowInContext context = new ShowInContext(getEditorInput(), getSelectionProvider().getSelection());
        return context;
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }
    
    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method extend the actions to
     * add those specific to the receiver
     */
    @Override
    protected void createActions() {
        super.createActions();

        IAction action = new TextOperationAction(
            EditorMessages.getResourceBundle(),
            "ApexEditor.ContentAssistProposal.",
            this, ISourceViewer.CONTENTASSIST_PROPOSALS);
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction(ACTION_CONTENT_ASSIST_PROPOSAL, action);

        action = new TextOperationAction(
            EditorMessages.getResourceBundle(),
            "ApexEditor.ContentAssistTip.",
            this,
            ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
        setAction(ACTION_CONTENT_ASSIST_TIP, action);

        action = new DefineFoldingRegionAction(
            EditorMessages.getResourceBundle(),
            "ApexEditor.DefineFoldingRegion.",
            this);
        setAction(ACTION_DEFINE_FOLDING_REGION, action);
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method performs any extra
     * disposal actions required by the java editor.
     */
    @Override
    public void dispose() {
        if (fOutlinePage != null) {
            fOutlinePage.dispose();
        }

        if (outlineUpdateResourceListener != null) {
            outlineUpdateResourceListener.clear();
            removeListenerObject(outlineUpdateResourceListener);
        }

        if (outlineSelectionChangedListener != null) {
            outlineSelectionChangedListener.uninstall(getSelectionProvider());
        }

        if (editorSelectionChangedListener != null) {
            editorSelectionChangedListener.uninstall(getSelectionProvider());
            editorSelectionChangedListener = null;
        }

        if (fBracketMatcher != null) {
            fBracketMatcher.dispose();
            fBracketMatcher = null;
        }

        if (getSourceViewer() instanceof ITextViewerExtension)
            ((ITextViewerExtension) getSourceViewer()).removeVerifyKeyListener(fBracketInserter);

        super.dispose();
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method performs any extra
     * revert behavior required by the java editor.
     */
    @Override
    public void doRevertToSaved() {
        outlineUpdateResourceListener.setFilePath(getFile().getFullPath());
        super.doRevertToSaved();
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method performs any extra save
     * behavior required by the java editor.
     * 
     * @param monitor
     *            the progress monitor
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        outlineUpdateResourceListener.setFilePath(getFile().getFullPath());
        super.doSave(monitor);
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method performs any extra save
     * as behavior required by the java editor.
     */
    @Override
    public void doSaveAs() {
        outlineUpdateResourceListener.setFilePath(getFile().getFullPath());
        super.doSaveAs();
    }

    @Override
    public void close(boolean save) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.removeResourceChangeListener(outlineUpdateResourceListener);
        super.close(save);
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method performs sets the input
     * of the outline page after AbstractTextEditor has set input.
     * 
     * @param input
     *            the editor input
     * @throws CoreException
     *             in case the input can not be set
     */
    @Override
    public void doSetInput(IEditorInput input) throws CoreException {
        if (!input.exists()) {
            logger.warn("Input does not exist for Apex Code Editor");
            return;
        }

        super.doSetInput(input);
        IFile file = getFile();
        if (file == null) {
            logger.warn("File for editor input is null");
            return;
        }

        outlineUpdateResourceListener.setFilePath(file.getFullPath());

        setProject(file.getProject());
        if (logger.isInfoEnabled()) {
            logger.info("Set resource attributes on '" + file.getProjectRelativePath().toPortableString() + "'");
        }
    }

    private IFile getFile() {
        return (getEditorInput() instanceof IFileEditorInput) ? ((IFileEditorInput) getEditorInput()).getFile() : null;
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void editorContextMenuAboutToShow(IMenuManager menu) {
        super.editorContextMenuAboutToShow(menu);
        addAction(menu, ACTION_CONTENT_ASSIST_PROPOSAL);
        addAction(menu, ACTION_CONTENT_ASSIST_TIP);
        addAction(menu, ACTION_DEFINE_FOLDING_REGION);
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method performs gets the java
     * content outline page if request is for a an outline page.
     * 
     * @param required
     *            the required type
     * @return an adapter for the required type or <code>null</code>
     */
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class required) {
        if (IContentOutlinePage.class.equals(required)) {
            if (fOutlinePage == null) {
                fOutlinePage = new ApexContentOutlinePage();
                if (getEditorInput() != null) {
                    outlineSelectionChangedListener.install(fOutlinePage);
                }
            }
            return fOutlinePage;
        }

        if (fProjectionSupport != null) {
            Object adapter = fProjectionSupport.getAdapter(getSourceViewer(), required);
            if (adapter != null) {
                return adapter;
            }
        }

        return super.getAdapter(required);
    }

    /*
     * (non-Javadoc) Method declared on AbstractTextEditor
     */
    @Override
    protected void initializeEditor() {
        setEditorContextMenuId("#ApexDebuggerEditorContext"); //$NON-NLS-1$
        setRulerContextMenuId("#ApexDebuggerRulerContext"); //$NON-NLS-1$
        setHelpContextId(ITextEditorHelpContextIds.TEXT_EDITOR);

        IPreferenceStore store = createCombinedPreferenceStore(null);
        setPreferenceStore(store);
        setHelpContextId(Constants.DOCUMENTATION_PLUGIN_PREFIX + "." + this.getClass().getSimpleName());
        apexSourceViewerConfiguration = new ApexSourceViewerConfiguration(getPreferenceStore(), this);
        apexSourceViewerConfiguration.init(project);
        setSourceViewerConfiguration(apexSourceViewerConfiguration);
    }
    
    /**
     * Creates and returns the preference store for this Java editor with the given input.
     * 
     * @param input
     *            The editor input for which to create the preference store
     * @return the preference store for this editor
     *
     */
    private static IPreferenceStore createCombinedPreferenceStore(IEditorInput input) {
        List<IPreferenceStore> stores = new ArrayList<>(3);

        stores.add(new ScopedPreferenceStore(InstanceScope.INSTANCE, ForceIdeEditorsPlugin.PLUGIN_ID));
        stores.add(new ScopedPreferenceStore(DefaultScope.INSTANCE, ForceIdeEditorsPlugin.PLUGIN_ID));
        stores.add(EditorsUI.getPreferenceStore());

        return new ChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        ((ProjectionViewer) getSourceViewer()).enableProjection();

        editorSelectionChangedListener = new EditorSelectionChangedListener();
        editorSelectionChangedListener.install(getSelectionProvider());

        IPreferenceStore preferenceStore = getPreferenceStore();
        boolean closeBrackets = preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_BRACKETS);
        boolean closeStrings = preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_STRINGS);
        boolean closeAngularBrackets = "1.5".compareTo(preferenceStore.getString(PreferenceConstants.COMPILER_SOURCE)) <= 0; // $NON-NLS-1$

        fBracketInserter.setCloseBracketsEnabled(closeBrackets);
        fBracketInserter.setCloseStringsEnabled(closeStrings);
        fBracketInserter.setCloseAngularBracketsEnabled(closeAngularBrackets);

        ISourceViewer sourceViewer = getSourceViewer();
        if (sourceViewer instanceof ITextViewerExtension)
            ((ITextViewerExtension) sourceViewer).prependVerifyKeyListener(fBracketInserter);
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite,
     *      org.eclipse.jface.text.source.IVerticalRuler, int)
     */
    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        fAnnotationAccess = createAnnotationAccess();
        fOverviewRuler = createOverviewRuler(getSharedColors());
        ProjectionViewer viewer =
                new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

        fProjectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
        fProjectionSupport.addSummarizableAnnotationType(ANNOTATION_TYPE_APEX_ERROR); //$NON-NLS-1$
        fProjectionSupport.addSummarizableAnnotationType(ANNOTATION_TYPE_APEX_WARNING); //$NON-NLS-1$
        fProjectionSupport.install();
        setTitleToolTip(EDITOR_NAME);

        // ensure source viewer decoration support has been created and configured
        getSourceViewerDecorationSupport(viewer);

        fBracketInserter = new BracketInserter(this, viewer);
        return viewer;
    }

    @Override
    protected ISharedTextColors getSharedColors() {
        ISharedTextColors sharedColors = ForceIdeUIPlugin.getSharedTextColors();
        return sharedColors;
    }

    /*
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#adjustHighlightRange(int,
     *      int)
     */
    @Override
    protected void adjustHighlightRange(int offset, int length) {
        ISourceViewer viewer = getSourceViewer();
        if (viewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
            extension.exposeModelRange(new Region(offset, length));
        }
    }

    private static void selectRange(int startPos, int stopPos, ITextViewer fText) {
        int offset = startPos + 1;
        int length = stopPos - offset;
        fText.setSelectedRange(offset, length);
    }

    private class DefineFoldingRegionAction extends TextEditorAction {

        public DefineFoldingRegionAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
            super(bundle, prefix, editor);
        }

        private IAnnotationModel getAnnotationModel(ITextEditor editor) {
            return (IAnnotationModel) editor.getAdapter(ProjectionAnnotationModel.class);
        }

        /*
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run() {
            ITextEditor editor = getTextEditor();
            ISelection selection = editor.getSelectionProvider().getSelection();
            if (selection instanceof ITextSelection) {
                ITextSelection textSelection = (ITextSelection) selection;
                if (!textSelection.isEmpty()) {
                    IAnnotationModel model = getAnnotationModel(editor);
                    if (model != null) {
                        int start = textSelection.getStartLine();
                        int end = textSelection.getEndLine();
                        try {
                            IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
                            int offset = document.getLineOffset(start);
                            int endOffset = document.getLineOffset(end + 1);
                            Position position = new Position(offset, endOffset - offset);
                            model.addAnnotation(new ProjectionAnnotation(), position);
                        } catch (BadLocationException x) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    class OutlineUpdateResourceListener implements IResourceChangeListener {
        protected IPath filePath = null;
        private int incrementer = 1;

        public OutlineUpdateResourceListener() {

        }

        public IPath getFilePath() {
            return filePath;
        }

        public void setFilePath(IPath filePath) {
            resetIncrementer();
            this.filePath = filePath;
        }

        public void clear() {
            resetIncrementer();
            this.filePath = null;
        }

        public void resetIncrementer() {
            incrementer = 1;
        }

        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            if (filePath == null) {
                return;
            }

            // we are only interested in POST_CHANGE events and post-build
            // changes since we're dependent upon
            // the server updates - changes to identifier tables
            if (event.getType() != IResourceChangeEvent.POST_CHANGE
                    || (event.getType() == IResourceChangeEvent.POST_CHANGE && incrementer < 2)) {
                incrementer++;
                return;
            }

            resetIncrementer();

            IResourceDelta rootDelta = event.getDelta();

            // get the delta, if any, for the documentation directory
            IResourceDelta docDelta = rootDelta.findMember(filePath);
            if (docDelta == null) {
                return;
            }

            IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
                @Override
                public boolean visit(IResourceDelta delta) {
                    // only interested in changed resources (not added or
                    // removed)
                    if (delta.getKind() != IResourceDelta.CHANGED) {
                        return true;
                    }

                    // only interested in content changes
                    if ((delta.getFlags() & IResourceDelta.CONTENT) == 0) {
                        return true;
                    }

                    IResource resource = delta.getResource();

                    // only interested in files with the "txt" extension
                    if (resource.getType() == IResource.FILE && filePath.equals(resource.getFullPath())) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Updating outline view for '" + filePath.toPortableString() + "'");
                        }
                    }
                    return true;
                }
            };

            try {
                docDelta.accept(visitor);
            } catch (CoreException e) {
                String logMessage = Utils.generateCoreExceptionLog(e);
                logger.warn("Unable to update Apex Code outline view: " + logMessage);
            }
        }
    }

    /**
     * Updates the selection in the editor's widget with the selection of the outline page.
     */
    class OutlineSelectionChangedListener extends AbstractSelectionChangedListener {
        OutlineViewDispatcher<HighlightRange> dispatcher = new OutlineViewDispatcher<>(new OutlineViewSelectionHandler(
                ApexCodeEditor.this));

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            ISelection selection = event.getSelection();
            if (selection instanceof ITreeSelection) {
                Object selectedObject = ((ITreeSelection) selection).getFirstElement();
                dispatcher.dispatch(selectedObject);
            }
        }
    }

    protected void setSelection(HighlightRange range, boolean moveCursor) {
        ISelection selection = getSelectionProvider().getSelection();
        if (selection instanceof ITextSelection) {
            ITextSelection textSelection = (ITextSelection) selection;
            if (moveCursor && (textSelection.getOffset() != 0 || textSelection.getLength() != 0))
                markInNavigationHistory();
        }

        StyledText textWidget = null;

        ISourceViewer sourceViewer = getSourceViewer();
        if (null == sourceViewer) return;

        textWidget = sourceViewer.getTextWidget();
        if (null == textWidget) return;

        try {
            int offset = range.startOffset;
            int length = range.length;
            
            if (offset < 0 || length < 0)
                return;

            setHighlightRange(offset, length, moveCursor);

            if (!moveCursor)
                return;

            if (offset > -1 && length > 0) {

                try {
                    textWidget.setRedraw(false);
                    sourceViewer.revealRange(offset, length);
                    sourceViewer.setSelectedRange(offset, length);
                } finally {
                    textWidget.setRedraw(true);
                }

                markInNavigationHistory();
            }

        } catch (IllegalArgumentException x) {}
    }

    private class EditorSelectionChangedListener extends AbstractSelectionChangedListener {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {}
    }

    protected boolean isActivePart() {
        IWorkbenchPart part = getActivePart();
        return part != null && part.equals(this);
    }

    private IWorkbenchPart getActivePart() {
        IWorkbenchWindow window = getSite().getWorkbenchWindow();
        IPartService service = window.getPartService();
        IWorkbenchPart part = service.getActivePart();
        return part;
    }

    public String getText() {
        return getDocument().get();
    }

    public IDocument getDocument() {
        return getDocumentProvider().getDocument(getEditorInput());
    }

    public int getTabWidth() {
        final int[] tabs = new int[1];
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                tabs[0] = getSourceViewer().getTextWidget().getTabs();
            }
        });
        return tabs[0];
    }

    /* (non-Javadoc)
     * adds matcher for chars in BRACKETS member variable
     */
    @Override
    protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
        // JavaPairMatcher only matches "<>" for certain compiler versions
        fBracketMatcher.setSourceVersion(getPreferenceStore().getString(PreferenceConstants.COMPILER_SOURCE));
        support.setCharacterPairMatcher(fBracketMatcher);
        support.setMatchingCharacterPainterPreferenceKeys(PreferenceConstants.EDITOR_MATCHING_BRACKETS,
            PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR);

        super.configureSourceViewerDecorationSupport(support);
    }
}
