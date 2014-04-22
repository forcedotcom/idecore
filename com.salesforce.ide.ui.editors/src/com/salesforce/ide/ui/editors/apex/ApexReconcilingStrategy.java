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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.data.ast.CompilationUnit.AnonymousBlockUnit;
import apex.jorje.data.ast.CompilationUnit.ClassDeclUnit;
import apex.jorje.data.ast.CompilationUnit.EnumDeclUnit;
import apex.jorje.data.ast.CompilationUnit.InterfaceDeclUnit;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.parser.impl.ApexParserImpl;

import com.salesforce.ide.ui.editors.apex.errors.ApexErrorMarkerHandler;
import com.salesforce.ide.ui.editors.apex.outline.ApexContentOutlinePage;
import com.salesforce.ide.ui.editors.apex.parser.IdeApexParser;

/**
 * This is the actual strategy used for doing the reconciling for the current text in the editor. For now, it will
 * reparse the whole thing again each time there is a change. We can try to optimize when we have an incremental parser.
 * 
 * @author nchen
 * 
 */
public class ApexReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
    private static final Logger logger = Logger.getLogger(ApexReconcilingStrategy.class);

    private ApexCodeEditor fTextEditor;
    private IProgressMonitor fProgressMonitor;

    public ApexReconcilingStrategy(ApexCodeEditor fTextEditor) {
        this.fTextEditor = fTextEditor;
    }

    @Override
    public void setProgressMonitor(IProgressMonitor fProgressMonitor) {
        this.fProgressMonitor = fProgressMonitor;
    }

    @Override
    public void initialReconcile() {
        reconcile();
    }

    @Override
    public void setDocument(IDocument document) {}

    @Override
    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
        reconcile();
    }

    @Override
    public void reconcile(IRegion partition) {
        reconcile();
    }

    private void reconcile() {
        SafeRunner.run(new ApexParserRunnable());
    }

    class ApexParserRunnable implements ISafeRunnable {
        private ApexParserImpl fParser;
        private ApexErrorMarkerHandler fMarkerHandler;
        private CompilationUnit fCompilationUnit;

        public ApexParserRunnable() {
            IFile file = ((IFileEditorInput) fTextEditor.getEditorInput()).getFile();
            IDocument doc = fTextEditor.getDocument();
            fMarkerHandler = new ApexErrorMarkerHandler(file, doc);
        }

        @Override
        public void run() throws Exception {
            clearExistingErrorMarkers();
            parseCurrentEditorContents();
            reportParseErrors();
            updateOutlineViewIfPossible();
        }

        // TODO: Do we really have to clear it each time?
        private void clearExistingErrorMarkers() {
            fMarkerHandler.clearExistingMarkers();
        }

        private void parseCurrentEditorContents() throws Exception {
            fParser = IdeApexParser.initializeParser(fTextEditor.getText());
            fCompilationUnit = fParser.compilationUnit();
        }

        private void reportParseErrors() {
            fMarkerHandler.handleSyntaxErrors(fParser.getSyntaxErrors());
        }

        private void updateOutlineViewIfPossible() {
            if (canDisplayOutline()) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        ApexContentOutlinePage outline =
                                (ApexContentOutlinePage) fTextEditor.getAdapter(IContentOutlinePage.class);
                        outline.update(fCompilationUnit);
                    }
                });
            }
        }

        /*
         * Sometimes the parser trips on things and returns us something that is
         * not-displayable (jADT nodes are empty). In that case, it is better to
         * just retain the previous outline view (which would be stale, but
         * presentable). We use some simple heuristic here.
         */
        private boolean canDisplayOutline() {
            if (fCompilationUnit != null) {
                return fCompilationUnit.match(new CompilationUnit.MatchBlockWithDefault<Boolean>() {

                    @Override
                    public Boolean _case(ClassDeclUnit x) {
                        return x.body != null;
                    }

                    @Override
                    public Boolean _case(EnumDeclUnit x) {
                        return x.body != null;
                    }

                    @Override
                    public Boolean _case(InterfaceDeclUnit x) {
                        return x.body != null;
                    }

                    @Override
                    public Boolean _case(TriggerDeclUnit x) {
                        return x.name != null;
                    }

                    @Override
                    protected Boolean _default(CompilationUnit arg0) {
                        return false;
                    }
                });
            }

            return false;
        }

        @Override
        public void handleException(Throwable exception) {
            // This is for any other exceptions that we do not handle
            logger.debug("Error occured during reconcile", exception);
        }
    }
}
