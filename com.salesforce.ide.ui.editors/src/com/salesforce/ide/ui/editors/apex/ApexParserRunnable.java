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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.data.ast.CompilationUnit.ClassDeclUnit;
import apex.jorje.data.ast.CompilationUnit.EnumDeclUnit;
import apex.jorje.data.ast.CompilationUnit.InterfaceDeclUnit;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.parser.impl.ApexParserImpl;

import com.salesforce.ide.apex.core.ApexParserFactory;
import com.salesforce.ide.apex.internal.core.ApexModelManager;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.errors.ApexErrorMarkerHandler;
import com.salesforce.ide.ui.editors.apex.outline.ApexContentOutlinePage;
import com.salesforce.ide.ui.editors.apex.preferences.PreferenceConstants;

/**
 * The runnable that is called each time the reconciler needs to run.
 * 
 * @author nchen
 * 
 */
public class ApexParserRunnable implements ISafeRunnable {
    private ApexReconcilingStrategy apexReconcilingStrategy;
    private ApexParserImpl fParser;
    private ApexErrorMarkerHandler fMarkerHandler;
    private CompilationUnit fCompilationUnit;
    private IFile file;

    // For testing purposes
    protected ApexParserRunnable() {}

    public ApexParserRunnable(ApexReconcilingStrategy apexReconcilingStrategy) {
        this.apexReconcilingStrategy = apexReconcilingStrategy;
        file = ((IFileEditorInput) apexReconcilingStrategy.fTextEditor.getEditorInput()).getFile();
        IDocument doc = apexReconcilingStrategy.fTextEditor.getDocument();
        fMarkerHandler = new ApexErrorMarkerHandler(file, doc);
    }

    @Override
    public void run() throws Exception {
        clearExistingErrorMarkers();
        if (checkShouldUpdate()) {
            parseCurrentEditorContents();
            reportParseErrors();
            updateOutlineViewIfPossible();
        }
    }

    protected boolean checkShouldUpdate() {
        IPreferenceStore preferenceStore = ForceIdeEditorsPlugin.getDefault().getPreferenceStore();
        return preferenceStore.getBoolean(PreferenceConstants.EDITOR_PARSE_WITH_NEW_COMPILER);
    }

    protected void clearExistingErrorMarkers() {
        fMarkerHandler.clearExistingMarkers();
    }

    protected void parseCurrentEditorContents() throws Exception {
        // This uses the current contents of the file (before it is saved)
        fParser = ApexParserFactory.create(this.apexReconcilingStrategy.fTextEditor.getText());
        fCompilationUnit = fParser.compilationUnit();

        refreshApexModelIfPossible();
    }

    private void refreshApexModelIfPossible() {
        if (fCompilationUnit != null) {
            ApexModelManager.INSTANCE.cacheCompilationUnit(file, fCompilationUnit);
            ApexModelManager.INSTANCE.evictCompilation(file);
        }
    }

    protected void reportParseErrors() {
        fMarkerHandler.handleSyntaxErrors(fParser.getParseErrors());
    }

    protected void updateOutlineViewIfPossible() {
        if (canDisplayOutline()) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    ApexContentOutlinePage outline =
                            (ApexContentOutlinePage) apexReconcilingStrategy.fTextEditor
                                    .getAdapter(IContentOutlinePage.class);
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
        ApexReconcilingStrategy.logger.debug("Error occured during reconcile", exception);
    }
}