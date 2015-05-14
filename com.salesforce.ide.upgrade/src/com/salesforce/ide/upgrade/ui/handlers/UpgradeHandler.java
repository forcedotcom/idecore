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
package com.salesforce.ide.upgrade.ui.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.handlers.BaseHandler;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.upgrade.ui.wizards.UpgradeWizard;

public final class UpgradeHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(UpgradeHandler.class);

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IStructuredSelection selection = getStructuredSelection(event);
        if (!selection.isEmpty()) {
            final IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event).getWorkbench();
            final Shell shell = HandlerUtil.getActiveShellChecked(event);
            final IProject project = getProjectChecked(event);

            try {
                // instantiates the wizard container with the wizard and opens it
                UpgradeWizard upgradeWizard = new UpgradeWizard(project);
                upgradeWizard.init(workbench, selection);
                WizardDialog dialog = new WizardDialog(shell, upgradeWizard);
                dialog.create();
                UIUtils.placeDialogInCenter(shell, dialog.getShell());
                Utils.openDialog(project, dialog);
            } catch (Exception e) {
                logger.error("Unable to open upgrade wizard", e);
            }
        }

        return null;
    }

}
