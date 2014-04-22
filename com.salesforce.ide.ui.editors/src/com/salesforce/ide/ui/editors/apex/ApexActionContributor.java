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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import org.eclipse.ui.texteditor.TextEditorAction;

import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;
import com.salesforce.ide.ui.editors.visualforce.VisualForceEditorContributor;

/**
 * Contributes interesting Java actions to the desktop's Edit menu and the toolbar.
 */
public class ApexActionContributor extends VisualForceEditorContributor {
    protected RetargetTextEditorAction fContentAssistProposal;
    protected RetargetTextEditorAction fContentAssistTip;
    protected TextEditorAction fTogglePresentation;

    /**
     * Default constructor.
     */
    public ApexActionContributor() {
        super();
        fContentAssistProposal =
                new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "ApexEditor.ContentAssistProposal.");
        fContentAssistProposal.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        fContentAssistTip =
                new RetargetTextEditorAction(EditorMessages.getResourceBundle(), "ApexEditor.ContentAssistTip.");
        fContentAssistTip.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
        fTogglePresentation = new PresentationAction();
    }

    /*
     * @see IEditorActionBarContributor#init(IActionBars)
     */
    @Override
    public void init(IActionBars bars) {
        super.init(bars);

        IMenuManager menuManager = bars.getMenuManager();
        IMenuManager editMenu = menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
        if (editMenu != null) {
            editMenu.add(new Separator());
            editMenu.add(fContentAssistProposal);
            editMenu.add(fContentAssistTip);
        }

        IToolBarManager toolBarManager = bars.getToolBarManager();
        if (toolBarManager != null) {
            toolBarManager.add(new Separator());
            toolBarManager.add(fTogglePresentation);
        }
    }

    @Override
    public void setActivePage(IEditorPart activeEditor) {
        super.setActivePage(activeEditor);

        ITextEditor editor = null;
        if (activeEditor instanceof ITextEditor) {
            editor = (ITextEditor) activeEditor;
        }

        fContentAssistProposal.setAction(getAction(editor, "ContentAssistProposal")); //$NON-NLS-1$
        fContentAssistTip.setAction(getAction(editor, "ContentAssistTip")); //$NON-NLS-1$

        fTogglePresentation.setEditor(editor);
        fTogglePresentation.update();
        getActionBars().updateActionBars();
    }
}
