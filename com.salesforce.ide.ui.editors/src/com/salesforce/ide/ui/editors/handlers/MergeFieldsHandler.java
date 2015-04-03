/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.editors.handlers;

import static org.eclipse.core.expressions.IEvaluationContext.UNDEFINED_VARIABLE;
import static org.eclipse.ui.ISources.ACTIVE_WORKBENCH_WINDOW_NAME;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import com.salesforce.ide.ui.editors.visualforce.SnippetDialog;
import com.salesforce.ide.ui.editors.visualforce.SnippetDialogController;
import com.salesforce.ide.ui.editors.visualforce.VisualForceMultiPageEditor;
import com.salesforce.ide.ui.handlers.BaseHandler;

public final class MergeFieldsHandler extends BaseHandler {

    @Override
    public void setEnabled(final Object context) {
        boolean enabled = false;
        if (context instanceof IEvaluationContext) {
            final IEvaluationContext evaluationContext = (IEvaluationContext) context;
            final Object obj = evaluationContext.getVariable(ACTIVE_WORKBENCH_WINDOW_NAME);
            if (null != obj && UNDEFINED_VARIABLE != obj && obj instanceof IWorkbenchWindow) {
                final IWorkbenchPage activePage = ((IWorkbenchWindow) obj).getActivePage();
                if (null != activePage) {
                    final IEditorPart activeEditor = activePage.getActiveEditor();
                    if (activeEditor instanceof VisualForceMultiPageEditor) {
                        enabled = null != ((VisualForceMultiPageEditor) activeEditor).getTextEditor();
                    }
                }
            }
        }
        setBaseEnabled(enabled);
    }

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);

        final IWorkbenchPage activePage = workbenchWindow.getActivePage();
        if (null == activePage) {
            throw new ExecutionException("No active page found while executing " + event.getCommand().getId()); //$NON-NLS-1$
        }

        final IEditorPart activeEditor = activePage.getActiveEditor();
        if (null == activeEditor) {
            throw new ExecutionException("No active editor found while executing " + event.getCommand().getId()); //$NON-NLS-1$
        }

        if (!VisualForceMultiPageEditor.class.isInstance(activeEditor)) {
            throw new ExecutionException("Incorrect type for active editor found while executing " //$NON-NLS-1$
                + event.getCommand().getId()
                + ", expected " + VisualForceMultiPageEditor.class.getName() //$NON-NLS-1$
                + " found " + activeEditor.getClass().getName()); //$NON-NLS-1$
        }

        final VisualForceMultiPageEditor ourEditor = (VisualForceMultiPageEditor) activeEditor;
        final StructuredTextEditor textEditor = ourEditor.getTextEditor();
        if (null == textEditor) {
            throw new ExecutionException("No text editor found while executing " + event.getCommand().getId()); //$NON-NLS-1$
        }

        final SnippetDialog snippetDialog = new SnippetDialog(textEditor.getSite().getShell(), textEditor);
        snippetDialog.setSnippetDialogController(new SnippetDialogController());
        snippetDialog.setProject(ourEditor.getEditorInputFile().getProject());
        snippetDialog.open();

        return null;
    }

}
