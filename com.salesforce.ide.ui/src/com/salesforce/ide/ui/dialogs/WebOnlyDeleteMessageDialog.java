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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import com.salesforce.ide.ui.handlers.ShowInBrowserHandler;

/**
 * A dialog for showing messages to the user. Message is displayed in a FormText, with processing enabled
 */
public class WebOnlyDeleteMessageDialog extends HyperLinkMessageDialog {
    private static final Logger logger = Logger.getLogger(WebOnlyDeleteMessageDialog.class);

    List<IResource> resources;

    public WebOnlyDeleteMessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage,
            String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
                defaultIndex);
    }

    @Override
    protected void linkActivated(HyperlinkEvent e) {
        int index = 0;
        try {
            index = Integer.parseInt(e.data.toString());
        } catch (NumberFormatException e1) {
            logger.error("An error occured while opening the selected resource in the web", e1); //$NON-NLS-1$
            return;
        }

        ShowInBrowserHandler.execute(PlatformUI.getWorkbench(), new StructuredSelection(resources.get(index)));
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
    public static void openWarning(Shell parent, String title, String message, List<IResource> resources) {
        WebOnlyDeleteMessageDialog dialog = new WebOnlyDeleteMessageDialog(parent, title, null, // accept
                // the
                // default
                // window
                // icon
                message, MessageDialog.WARNING, new String[] { IDialogConstants.OK_LABEL }, 0); // ok
        // is
        // the
        // default

        dialog.resources = resources;
        dialog.open();
        return;
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
        List<IResource> list = new ArrayList<>();
        list.add(resource);
        openWarning(parent, title, message, list);
    }
}
