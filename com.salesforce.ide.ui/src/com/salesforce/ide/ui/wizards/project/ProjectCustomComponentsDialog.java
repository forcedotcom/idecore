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

import java.util.Arrays;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.PackageManifestModel;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.ui.packagemanifest.IStatusChangedListener;

public class ProjectCustomComponentsDialog extends SelectionDialog {

    protected Point shellSize = new Point(580, 700);
    protected ProjectCustomComponentsComposite customComponentsComposite = null;
    protected PackageManifestModel packageManifestModel = null;
    protected Connection connection = null;
    protected Shell parentShell = null;
    protected IStatus lastStatus = null;
    protected Image fImage = null;

    //   C O N S T R U C T O R
    public ProjectCustomComponentsDialog(Shell shell, PackageManifestModel packageManifestModel, Connection connection) {
        super(shell);
        this.parentShell = shell;
        this.packageManifestModel = packageManifestModel;
        this.connection = connection;
        setShellStyle(getShellStyle() | SWT.DIALOG_TRIM | SWT.RESIZE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        customComponentsComposite =
                new ProjectCustomComponentsComposite(parent, SWT.NULL, packageManifestModel, connection);
        customComponentsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // set dialog help context
        UIUtils.setHelpContext(customComponentsComposite, this.getClass().getSimpleName());

        customComponentsComposite.getProjectPackageManifestTree().addStatusChangedListener(
            new IStatusChangedListener() {
                @Override
                public void statusChanged(Status status) {
                    updateStatus(status);
                }
            });

        return customComponentsComposite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);

        Button btnOk = getOkButton();
        btnOk.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                packageManifestModel.setManifestDocument(customComponentsComposite.getProjectPackageManifestTree()
                        .getDocument());
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }

        });
    }

    private static IStatus getMostSevereStatus(IStatus[] allStatus) {
        if (Utils.isEmpty(allStatus)) {
            return null;
        }

        if (allStatus.length == 1) {
            return allStatus[0];
        }

        IStatus worstStatus = allStatus[0];
        for (int i = 1; i < allStatus.length; i++) {
            if (allStatus[i].getSeverity() > worstStatus.getSeverity()) {
                worstStatus = allStatus[i];
            }
        }
        return worstStatus;
    }

    @Override
    protected void configureShell(final Shell shell) {
        super.configureShell(shell);
        if (fImage != null) {
            shell.setImage(fImage);
        }

        shell.setText(UIMessages.getString("ProjectCreateWizard.ProjectContent.CustomComponents.title"));
        setShellStyle(getShellStyle() | SWT.RESIZE);
        shell.setSize(shellSize);

        UIUtils.placeDialogInCenter(parentShell, shell);
    }

    public void setImage(Image image) {
        fImage = image;
    }

    public Object getFirstResult() {
        Object[] result = getResult();
        if (result == null || result.length == 0) {
            return null;
        }
        return result[0];
    }

    protected void setResult(int position, Object element) {
        Object[] result = getResult();
        result[position] = element;
        setResult(Arrays.asList(result));
    }

    @Override
    protected void okPressed() {
        if (customComponentsComposite != null) {
            customComponentsComposite.loadPackageManifestTreeSelections();
        }
        super.okPressed();
    }

    @Override
    public void create() {
        super.create();
        if (lastStatus != null) {
            updateStatus(lastStatus);
        }
    }

    protected void updateStatus(IStatus status) {
        lastStatus = status;

        customComponentsComposite.updateStatus(status);

        updateButtonsEnableState(status);
    }

    protected void updateButtonsEnableState(IStatus status) {
        Button okButton = getOkButton();
        if (okButton != null && !okButton.isDisposed()) {
            if (status.isMultiStatus()) {
                status = getMostSevereStatus(((MultiStatus) status).getChildren());
            }
            okButton.setEnabled(status == null || !status.matches(IStatus.ERROR));
        }
    }

    @Override
    public boolean close() {
        if (customComponentsComposite != null && !customComponentsComposite.isDisposed()) {
            customComponentsComposite.dispose();
        }
        return super.close();
    }

}
