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
package com.salesforce.ide.ui.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

/**
 * A dialog for showing messages to the user. Message is displayed in a FormText, with processing enabled
 */
public class HyperLinkMessageDialog extends MessageDialog {

    public HyperLinkMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
            int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
                defaultIndex);
    }
    
    @Override
    protected Control createMessageArea(Composite composite) {
        // create composite
        // create image
        Image image = getImage();
        if (image != null) {
            imageLabel = new Label(composite, SWT.NULL);
            image.setBackground(imageLabel.getBackground());
            imageLabel.setImage(image);
            //            addAccessibleListeners(imageLabel, image);
            GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(imageLabel);
        }
        
        composite.setLayoutData(new GridData());
        
        FormText formText = new FormText(composite, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        formText.setLayoutData(gd);

        StringBuilder buf = new StringBuilder();
        buf.append("<form>"); //$NON-NLS-1$
        buf.append(message);
        buf.append("</form>"); //$NON-NLS-1$

        formText.setText(buf.toString(), true, false);
        formText.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                HyperLinkMessageDialog.this.linkActivated(e);
            }
        });

        return composite;
    }

    protected void linkActivated(HyperlinkEvent e) {

    }

    /**
     * Convenience method to open a simple confirm (OK/Cancel) dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     * @return <code>true</code> if the user presses the OK button, <code>false</code> otherwise
     */
    public static boolean openConfirm(Shell parent, String title, String message) {
        HyperLinkMessageDialog dialog =
                new HyperLinkMessageDialog(parent, title, null, // accept
                        // the
                        // default
                        // window
                        // icon
                        message, MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL,
                                IDialogConstants.CANCEL_LABEL }, 0); // OK is the
        // default
        return dialog.open() == 0;
    }

    /**
     * Convenience method to open a standard error dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     */
    public static void openError(Shell parent, String title, String message) {
        HyperLinkMessageDialog dialog = new HyperLinkMessageDialog(parent, title, null, // accept
                // the
                // default
                // window
                // icon
                message, MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0); // ok
        // is
        // the
        // default
        dialog.open();
        return;
    }

    /**
     * Convenience method to open a standard information dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     */
    public static void openInformation(Shell parent, String title, String message) {
        HyperLinkMessageDialog dialog = new HyperLinkMessageDialog(parent, title, null, // accept
                // the
                // default
                // window
                // icon
                message, MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0);
        // ok is the default
        dialog.open();
        return;
    }

    /**
     * Convenience method to open a simple Yes/No question dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     * @return <code>true</code> if the user presses the OK button, <code>false</code> otherwise
     */
    public static boolean openQuestion(Shell parent, String title, String message) {
        HyperLinkMessageDialog dialog =
                new HyperLinkMessageDialog(parent, title, null, // accept
                        // the
                        // default
                        // window
                        // icon
                        message, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL,
                                IDialogConstants.NO_LABEL }, 0); // yes is the default
        return dialog.open() == 0;
    }

    /**
     * Convenience method to open a standard warning dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     */
    public static void openWarning(Shell parent, String title, String message, IResource resource) {
        HyperLinkMessageDialog dialog = new HyperLinkMessageDialog(parent, title, null, // accept
                // the
                // default
                // window
                // icon
                message, MessageDialog.WARNING, new String[] { IDialogConstants.OK_LABEL }, 0); // ok
        // is
        // the
        // default

        dialog.open();
        return;
    }
}
