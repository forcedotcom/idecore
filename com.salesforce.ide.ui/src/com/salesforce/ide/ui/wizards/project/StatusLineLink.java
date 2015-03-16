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
package com.salesforce.ide.ui.wizards.project;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.ui.internal.utils.UIUtils;

/**
 * A message line displaying a status.
 */
public class StatusLineLink extends Composite {

    private Composite parent = null;
    private CLabel lblImage = null;
    private Link lnkMessage = null;

    public StatusLineLink(Composite parent, int style, final String helpContextId) {
        super(parent, style);
        this.parent = parent;

        GridLayout grdLayout = new GridLayout(2, false);
        grdLayout.horizontalSpacing = 0;
        grdLayout.verticalSpacing = 0;
        grdLayout.marginBottom = 0;
        grdLayout.marginWidth = 0;
        grdLayout.marginHeight = 0;
        grdLayout.marginLeft = 0;
        grdLayout.marginRight = 0;
        setLayout(grdLayout);

        lblImage = new CLabel(this, SWT.NONE);
        lblImage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 2));

        lnkMessage = new Link(this, SWT.WRAP);
        updateLinkLayout();

        lnkMessage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                UIUtils.displayHelp(helpContextId);
            }
        });
    }

    public void setMessage(String message) {
        lnkMessage.setText(message);
        lnkMessage.update();
    }

    private void updateLinkLayout() {
        GridData dataSelectContents = new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 2);
        Rectangle rect = UIUtils.getClientArea(parent.getShell());
        dataSelectContents.widthHint = rect.width;
        lnkMessage.setLayoutData(dataSelectContents);
    }

    private static Image findImage(IStatus status) {
        if (status.isOK()) {
            return null;
        } else if (status.matches(IStatus.ERROR)) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
        } else if (status.matches(IStatus.WARNING)) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
        } else if (status.matches(IStatus.INFO)) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        }
        return null;
    }

    /**
     * Sets the message and image to the given status. <code>null</code> is a valid argument and will set the empty text
     * and no image
     */
    public void setErrorStatus(IStatus status) {
        if (status != null) {
            String message = status.getMessage();
            if (message != null && message.length() > 0) {
                lnkMessage.setText(message);
                lblImage.setImage(findImage(status));
                updateLinkLayout();
                layout();
                return;
            }
        }
        lnkMessage.setText("");
        updateLinkLayout();
        lblImage.setImage(null);

        layout();
    }

}
