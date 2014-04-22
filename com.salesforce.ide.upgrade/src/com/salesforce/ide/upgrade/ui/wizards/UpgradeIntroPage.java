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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.upgrade.internal.utils.UpgradeMessages;

/**
 * 
 * Intro page to upgrade process.
 * 
 * @author cwall
 */
public class UpgradeIntroPage extends BaseUpgradePage {

    private static final Logger logger = Logger.getLogger(UpgradeComponentConflictsPage.class);

    public static final String WIZARDPAGE_ID = "upgradeIntroWizardPage";

    private UpgradeIntroComposite upgradeIntroComposite = null;

    //   C O N S T R U C T O R S
    public UpgradeIntroPage(UpgradeWizard upgradeWizard) {
        super(WIZARDPAGE_ID, upgradeWizard);
    }

    // M E T H O D S

    // Assemble connection page wizard
    @Override
    public void createControl(Composite parent) {
        upgradeIntroComposite = new UpgradeIntroComposite(parent, SWT.NULL, upgradeController);
        setControl(upgradeIntroComposite);
        setPageComplete(false);
        initialize();

        UIUtils.setHelpContext(upgradeIntroComposite, this.getClass().getSimpleName());
    }

    private void initialize() {
        setPageComplete(true);
        getShell().setSize(new Point(575, 550));
    }

    protected void setWizardCosmetics(Composite parent) {
        parent.getShell().setSize(new Point(575, 550));
        UIUtils.placeDialogInCenter(Display.getDefault().getActiveShell(), parent.getShell());
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setTitleAndDescription(UpgradeMessages.getString("UpgradeWizard.IntroPage.title",
                new String[] { upgradeController.getUpgradeModel().getIdeReleaseName() }), UpgradeMessages.getString(
                    "UpgradeWizard.IntroPage.description", new String[] {
                            upgradeController.getUpgradeModel().getProjectName(),
                            upgradeController.getUpgradeModel().getIdeReleaseName() }));
            upgradeController.setCanComplete(false);
            upgradeIntroComposite.setFocus();
        }
        super.setVisible(visible);
    }

    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    @Override
    public IWizardPage getNextPage() {
        try {
            ForceProject forceProject =
                    ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProject(upgradeController.getModel().getProject());
            testConnection(upgradeController, forceProject);
            setPageComplete(true);
        } catch (InvocationTargetException e) {
            setPageComplete(false);

            Throwable cause = e.getTargetException();
            if (cause instanceof InsufficientPermissionsException) {
                // log failure; option enabling occurs downstream
                logger.warn("Insufficient permissions to upgrade project: " + cause.getMessage());

                InsufficientPermissionsException ex = (InsufficientPermissionsException) cause;
                // occurred during project create/update which determines message text
                ex.setShowUpdateCredentialsMessage(true);

                // show dialog
                DialogUtils.getInstance().presentInsufficientPermissionsDialog(ex);

                updateErrorStatus(Messages.getString("InsufficientPermissions.User.OrganizationSettings.message",
                    new String[] { ex.getConnection().getUsername() }));

                return this;
            } else if (cause instanceof InvalidLoginException) {
                // log failure
                logger.warn("Unable to perform upgrade: " + cause.getMessage());

                InvalidLoginException ex = (InvalidLoginException) cause;
                // occurred during project create/update which determines message text
                ex.setShowUpdateCredentialsMessage(true);

                // choose further project create direction
                DialogUtils.getInstance().invalidLoginDialog(ex.getMessage(), null, true);

                updateErrorStatus(UIMessages
                    .getString("ProjectCreateWizard.OrganizationPage.InvalidConnection.message"));

                return this;
            } else {
                Throwable th = ForceExceptionUtils.getRootCause(e);
                logger.error("Unable to test connection", th);
                updateErrorStatus(UpgradeMessages.getString("UpgradeWizard.Connection.error"));
                Utils.openError(th, true, UpgradeMessages.getString("UpgradeWizard.Connection.error"));
                return this;
            }

        }

        return upgradeWizard.getNextPage(this);
    }
}
