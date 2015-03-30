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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.ui.internal.wizards.BaseWizard;
import com.salesforce.ide.upgrade.internal.UpgradeController;
import com.salesforce.ide.upgrade.internal.utils.UpgradeMessages;

public class UpgradeWizard extends BaseWizard {
    private static final Logger logger = Logger.getLogger(UpgradeWizard.class);

    private UpgradeIntroPage upgradeIntroPage = null;
    private UpgradeComponentConflictsPage upgradeComponentsPage = null;

    // C O N S T R U C T O R S
    public UpgradeWizard(IProject project) {
        super();

        this.project = project;
        controller = new UpgradeController();
        controller.setProject(project);

        if (logger.isDebugEnabled()) {
            logger.debug("");
            logger.debug("***   U P G R A D E   W I Z A R D   ***");
        }
    }

    public UpgradeController getUpgradeController() {
        return (UpgradeController)controller;
    }

    @Override
    protected String getWindowTitleString() {
        return UpgradeMessages.getString("UpgradeWizard.title");
    }

    @Override
    public void addPages() {
        upgradeIntroPage = new UpgradeIntroPage(this);
        addPage(upgradeIntroPage);
        upgradeComponentsPage = new UpgradeComponentConflictsPage(this);
        addPage(upgradeComponentsPage);
    }

    @Override
    public boolean performCancel() {
        int result =
                DialogUtils.getInstance().yesNoMessage(UpgradeMessages.getString("UpgradeWizard.Cancel.title"),
                    UpgradeMessages
            .getString("UpgradeWizard.Cancel.message"), MessageDialog.WARNING);
        return !(result > 0);
    }

    @Override
    public boolean performFinish() {

        if (controller != null) {
            try {
                // do upgrade
                controller.finish(new NullProgressMonitor());

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtils.getInstance().closeMessage(
                            UpgradeMessages.getString("UpgradeWizard.Complete.title"),
                            UpgradeMessages.getString("UpgradeWizard.Complete.message", new String[] {
                                    getUpgradeController().getUpgradeModel().getProjectName(),
                                    getUpgradeController().getUpgradeModel().getIdeReleaseName() }));
                    }
                });

            } catch (InterruptedException e) {
                logger.warn("Operation canceled: " + e.getMessage());

            } catch (InvocationTargetException e) {
                Throwable cause = e.getTargetException();
                if (cause instanceof InsufficientPermissionsException) {
                    DialogUtils.getInstance().presentInsufficientPermissionsDialog(
                        (InsufficientPermissionsException) cause);
                } else if (cause instanceof InvalidLoginException) {
                    // log failure
                    logger.warn("Unable to upgrade components: " + ForceExceptionUtils.getRootCauseMessage(cause));
                    // choose further project create direction
                    DialogUtils.getInstance().invalidLoginDialog(ForceExceptionUtils.getRootCauseMessage(cause));
                } else {
                    logger.error("Unable to upgrade components", ForceExceptionUtils.getRootCause(cause));
                    StringBuffer strBuff = new StringBuffer();
                    strBuff.append("Unable to upgrade components:\n\n").append(
                        ForceExceptionUtils.getStrippedRootCauseMessage(e)).append("\n\n ");
                    Utils.openError("Upgrade Error", strBuff.toString());
                }
            } catch (Exception e) {
                Utils.openError(e, true, "Unable to complete upgrade:\n\n" + e.getMessage());
                return false;
            } finally {
                this.controller.dispose();
            }
        }
        return true;
    }

}
