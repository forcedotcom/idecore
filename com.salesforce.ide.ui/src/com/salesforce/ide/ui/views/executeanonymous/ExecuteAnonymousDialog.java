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
package com.salesforce.ide.ui.views.executeanonymous;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.salesforce.ide.ui.internal.ForceImages;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public class ExecuteAnonymousDialog extends TitleAreaDialog {

    protected ExecuteAnonymousViewComposite executeAnonymousViewComposite = null;
    protected ExecuteAnonymousController executeAnonymousController = null;

    public ExecuteAnonymousDialog(IProject project, Shell parentShell) {
        super(parentShell);
        executeAnonymousController = new ExecuteAnonymousController(project);
    }

    public ExecuteAnonymousController getExecuteAnonymousController() {
        return executeAnonymousController;
    }

    public void setExecuteAnonymousController(ExecuteAnonymousController executeAnonymousController) {
        this.executeAnonymousController = executeAnonymousController;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        setShellStyle(getShellStyle() | SWT.Resize);
        setBlockOnOpen(false);
        Composite composite = (Composite) super.createDialogArea(parent);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        executeAnonymousViewComposite =
                new ExecuteAnonymousViewComposite(composite, SWT.NONE, executeAnonymousController);
        executeAnonymousViewComposite.setLayoutData(gd);

        UIUtils.setHelpContext(executeAnonymousViewComposite, getClass().getSimpleName());

        return executeAnonymousViewComposite;
    }

    @Override
    protected void configureShell(final Shell shell) {
        super.configureShell(shell);

        shell.setImage(ForceImages.get(ForceImages.APEX_WIZARD_IMAGE));

        UIUtils.placeDialogInCenter(Display.getDefault().getActiveShell(), shell);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);

        setTitleImage(ForceImages.get(ForceImages.APEX_WIZARD_IMAGE));
        setTitle(UIMessages.getString("ExecuteAnonymousDialog.title"));
        setMessage("Execute an anonymous block of Apex to help quickly evaluate code on the fly.");

        return contents;
    }

    @Override
    public boolean close() {
        if (executeAnonymousViewComposite != null && !executeAnonymousViewComposite.isDisposed()) {
            executeAnonymousViewComposite.dispose();
        }
        return super.close();
    }
}
