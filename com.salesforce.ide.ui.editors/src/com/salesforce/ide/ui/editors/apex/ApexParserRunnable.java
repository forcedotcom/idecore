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
package com.salesforce.ide.ui.editors.apex;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.salesforce.ide.apex.internal.core.CompilerService;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.errors.ApexErrorMarkerHandler;
import com.salesforce.ide.ui.editors.apex.outline.ApexContentOutlinePage;
import com.salesforce.ide.ui.editors.apex.outline.OutlineViewVisitor;
import com.salesforce.ide.ui.editors.apex.preferences.PreferenceConstants;

import apex.jorje.semantic.compiler.ApexCompiler;

/**
 * The runnable that is called each time the reconciler needs to run.
 * 
 * @author nchen
 *         
 */
public class ApexParserRunnable implements ISafeRunnable {
    private ApexReconcilingStrategy apexReconcilingStrategy;
    private IFile file;
    private OutlineViewVisitor outlineViewVisitor;
    private ApexErrorMarkerHandler fMarkerHandler;
    private ApexCompiler compiler;
    
    protected ApexParserRunnable() {
        outlineViewVisitor = new OutlineViewVisitor();
    }
    
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
        String text = this.apexReconcilingStrategy.fTextEditor.getText();
        outlineViewVisitor = new OutlineViewVisitor();
        compiler = CompilerService.INSTANCE.visitAstFromString(text, outlineViewVisitor);
    }
    
    protected void reportParseErrors() {
        fMarkerHandler.handleSyntaxErrors(compiler.getErrors());
    }
    
    protected void updateOutlineViewIfPossible() {
        if (outlineViewVisitor.hasValidTopLevel()) {
            Display.getDefault().asyncExec(() -> {
                ApexContentOutlinePage outline =
                    (ApexContentOutlinePage) apexReconcilingStrategy.fTextEditor.getAdapter(IContentOutlinePage.class);
                outline.update(outlineViewVisitor);
            });
        }
    }
    
    @Override
    public void handleException(Throwable exception) {
        // This is for any other exceptions that we do not handle
        ApexReconcilingStrategy.logger.debug("Error occured during reconcile", exception);
    }
}