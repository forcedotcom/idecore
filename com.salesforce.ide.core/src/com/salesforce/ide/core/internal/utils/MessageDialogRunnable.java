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
package com.salesforce.ide.core.internal.utils;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class MessageDialogRunnable implements Runnable {
    private String dialogTitle = null;
    private Image dialogTitleImage = null;
    private String dialogMessage = null;
    private int dialogImageType = -1;
    private String[] dialogButtonLabels = null;
    private int defaultIndex = -1;
    private int action = -1;

    public MessageDialogRunnable(String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType,
            String[] dialogButtonLabels, int defaultIndex) {
        this.dialogTitle = dialogTitle;
        this.dialogTitleImage = dialogTitleImage;
        this.dialogMessage = dialogMessage;
        this.dialogImageType = dialogImageType;
        this.dialogButtonLabels = dialogButtonLabels;
        this.defaultIndex = defaultIndex;
    }

    @Override
    public void run() {
        MessageDialog messageDialog = new MessageDialog(Display.getDefault().getActiveShell(), dialogTitle,
                dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
        action = messageDialog.open();
    }

    public int getAction() {
        return action;
    }
}
