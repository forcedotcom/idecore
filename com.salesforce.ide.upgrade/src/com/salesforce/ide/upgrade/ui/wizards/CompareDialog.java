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
package com.salesforce.ide.upgrade.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.internal.ICompareContextIds;
import org.eclipse.compare.internal.ResizableDialog;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

// TODO remove when we drop 3.2 support
public class CompareDialog extends ResizableDialog implements IPropertyChangeListener {

    private CompareEditorInput fCompareEditorInput;

    CompareDialog(Shell shell, CompareEditorInput input) {
        super(shell, null);

        Assert.isNotNull(input);
        fCompareEditorInput = input;
        fCompareEditorInput.addPropertyChangeListener(this);
        setHelpContextId(ICompareContextIds.COMPARE_DIALOG);
    }

    @Override
    public boolean close() {
        if (super.close()) {
            if (fCompareEditorInput != null)
                fCompareEditorInput.addPropertyChangeListener(this);
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    @Override
    protected Control createDialogArea(Composite parent2) {

        Composite parent = (Composite) super.createDialogArea(parent2);

        Control c = fCompareEditorInput.createContents(parent);
        c.setLayoutData(new GridData(GridData.FILL_BOTH));

        Shell shell = c.getShell();
        shell.setText(fCompareEditorInput.getTitle());
        shell.setImage(fCompareEditorInput.getTitleImage());
        applyDialogFont(parent);
        return parent;
    }

    /*
     * (non-Javadoc) Method declared on Window.
     */
    @Override
    public int open() {

        int rc = super.open();

        if (rc == OK && fCompareEditorInput.isSaveNeeded()) {

            WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
                @Override
                public void execute(IProgressMonitor pm) throws CoreException {
                    fCompareEditorInput.saveChanges(pm);
                }
            };

            Shell shell = getParentShell();
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
            try {
                operation.run(pmd.getProgressMonitor());

            } catch (InterruptedException x) {
                // NeedWork
            } catch (OperationCanceledException x) {
                // NeedWork
            } catch (InvocationTargetException x) {
                String title = Utilities.getString("CompareDialog.saveErrorTitle"); //$NON-NLS-1$
                String msg = Utilities.getString("CompareDialog.saveErrorMessage"); //$NON-NLS-1$
                MessageDialog.openError(shell, title, msg + x.getTargetException().getMessage());
            }
        }

        return rc;
    }
}
