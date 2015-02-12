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

import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Used to update the underlying Apex model for the editor that the user is
 * interacting with. As the user pauses during typing, the reconciler kicks in,
 * runs in a separate thread, and gives us the opportunity to update the model.
 * The model is of our own construction and can be as simple or complicated as
 * we want. Uses for the model include, checking for parse errors, updating the
 * outline view, etc.
 * 
 * @author nchen
 * 
 */
public class ApexReconciler extends MonoReconciler {

    public ApexReconciler(ITextEditor fTextEditor, IReconcilingStrategy strategy, boolean isIncremental) {
		super(strategy, isIncremental);
	}

}
