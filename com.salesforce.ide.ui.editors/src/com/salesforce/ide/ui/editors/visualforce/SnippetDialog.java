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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

/**
 *
 *
 * @author cwall
 */
public class SnippetDialog extends Dialog {

	private StructuredTextEditor editor;
	private IProject project;
	private SnippetDialogComposite SnippetDialogComposite = null;
	private SnippetDialogController snippetDialogController = null;

	//   C O N S T R U C T O R S
	public SnippetDialog(Shell parentShell, StructuredTextEditor editor, SnippetDialogController snippetDialogController) {
		super(parentShell); // no info text
		this.editor = editor;
		this.snippetDialogController = snippetDialogController;
	}
	
	public SnippetDialog(Shell parentShell, StructuredTextEditor editor) {
		super(parentShell); // no info text
		this.editor = editor;
	}
	
	//  M E T H O D S
	public SnippetDialogComposite getSnippetDialogComposite() {
		return SnippetDialogComposite;
	}

	public void setSnippetDialogComposite(SnippetDialogComposite snippetDialogComposite) {
		SnippetDialogComposite = snippetDialogComposite;
	}
	
	public SnippetDialogController getSnippetDialogController() {
		return snippetDialogController;
	}

	public void setSnippetDialogController(SnippetDialogController snippetDialogController) {
		this.snippetDialogController = snippetDialogController;
	}
	
	public IProject getProject() {
		return project;
	}
	
	public void setProject(IProject project) {
		this.project = project;
		snippetDialogController.setProject(project);
	}

	public StructuredTextEditor getEditor() {
		return editor;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		SnippetDialogComposite = new SnippetDialogComposite(composite, SWT.NONE, this);
		SnippetDialogComposite.setEditor(editor);
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public void closeDialog() {
		close();
	}

}
