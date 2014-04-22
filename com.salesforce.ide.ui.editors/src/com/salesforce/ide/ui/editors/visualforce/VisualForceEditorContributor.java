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
package com.salesforce.ide.ui.editors.visualforce;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Manages the installation/deinstallation of global actions for multi-page editors. Responsible for the redirection of
 * global actions to the active editor. Multi-page contributor replaces the contributors for the individual editors in
 * the multi-page editor.
 */
public class VisualForceEditorContributor extends MultiPageEditorActionBarContributor {
    private IEditorPart activeEditorPart;
    private TextEditorActionContributor textContributer;

    /**
     * Creates a multi-page contributor.
     */
    public VisualForceEditorContributor() {
        super();
        textContributer = new TextEditorActionContributor();
    }

    /*
     * @see IEditorActionBarContributor#init(IActionBars)
     */
    @Override
    public void init(IActionBars bars) {
        super.init(bars);
        textContributer.init(bars);
    }

    /**
     * Returns the action registed with the given text editor.
     * 
     * @return IAction or null if editor is null.
     */
    protected IAction getAction(ITextEditor editor, String actionID) {
        return (editor == null ? null : editor.getAction(actionID));
    }

    /*
     * (non-JavaDoc) Method declared in AbstractMultiPageEditorActionBarContributor.
     */

    @Override
    public void setActivePage(IEditorPart part) {
        if (activeEditorPart == part) {
            return;
        }

        activeEditorPart = part;
        textContributer.setActiveEditor(activeEditorPart);
        getActionBars().updateActionBars();
    }

    /*
     * @see IEditorActionBarContributor#dispose()
     */
    @Override
    public void dispose() {
        setActiveEditor(null);
        super.dispose();
    }

    protected VisualForceMultiPageEditor getTextEditor(IEditorPart editor) {
        VisualForceMultiPageEditor textEditor = null;
        if (editor instanceof VisualForceMultiPageEditor) {
            textEditor = (VisualForceMultiPageEditor) editor;
        }
        if ((textEditor == null) && (editor != null)) {
            textEditor = (VisualForceMultiPageEditor) editor.getAdapter(VisualForceMultiPageEditor.class);
        }
        return textEditor;
    }
}
