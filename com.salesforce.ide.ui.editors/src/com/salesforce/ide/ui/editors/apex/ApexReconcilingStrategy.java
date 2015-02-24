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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

/**
 * This is the actual strategy used for doing the reconciling for the current text in the editor. For now, it will
 * reparse the whole thing again each time there is a change. We can try to optimize when we have an incremental parser.
 * 
 * @author nchen
 * 
 */
public class ApexReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
    static final Logger logger = Logger.getLogger(ApexReconcilingStrategy.class);

    ApexCodeEditor fTextEditor;

    public ApexReconcilingStrategy(ApexCodeEditor fTextEditor) {
        this.fTextEditor = fTextEditor;
    }

    @Override
    public void setProgressMonitor(IProgressMonitor fProgressMonitor) {
        /* nothing to do */
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
        SafeRunner.run(new ApexParserRunnable(this));
    }
}
