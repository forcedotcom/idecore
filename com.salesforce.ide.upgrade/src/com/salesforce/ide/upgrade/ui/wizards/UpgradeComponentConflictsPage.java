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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.QuietCloseable;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.upgrade.internal.utils.UpgradeMessages;

/**
 * 
 * Displays upgrade conflict components.
 * 
 * @author cwall
 */
public class UpgradeComponentConflictsPage extends BaseUpgradePage {

    private static final Logger logger = Logger.getLogger(UpgradeComponentConflictsPage.class);

    public static final String WIZARDPAGE_ID = "upgradeComponentsWizardPage";

    private UpgradeComponentConflictsComposite upgradeComponentsComposite = null;

    //   C O N S T R U C T O R S
    public UpgradeComponentConflictsPage(UpgradeWizard upgradeWizard) {
        super(WIZARDPAGE_ID, upgradeWizard);
    }

    // M E T H O D S

    // Assemble connection page wizard
    @Override
    public void createControl(Composite parent) {
        upgradeComponentsComposite = UpgradeComponentConflictsComposite.newInstance(parent, SWT.NULL, upgradeController);
        setControl(upgradeComponentsComposite);
        setPageComplete(false);
        initialize();

        UIUtils.setHelpContext(upgradeComponentsComposite, this.getClass().getSimpleName());
    }

    private void initialize() {
        setPageComplete(true);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            initializeConflicts();
            upgradeComponentsComposite.loadUpgradeableComponentsTree(upgradeController.getUpgradeModel()
                .getUpgradeConflicts());
            upgradeController.setCanComplete(true);

            setTitleAndDescription(UpgradeMessages.getString("UpgradeWizard.ConflictsPage.title",
                new String[] { upgradeController.getUpgradeModel().getIdeReleaseName() }), UpgradeMessages
                .getString("UpgradeWizard.ConflictsPage.description"));

            upgradeComponentsComposite.setFocus();
        }
        super.setVisible(visible);
    }

    private void initializeConflicts() {
        try {
            IProgressService service = PlatformUI.getWorkbench().getProgressService();
            service.run(false, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Project Upgrade Analysis", 5);
                    monitor.worked(1);
                    try {
                        upgradeController.initConflicts(monitor);
                    } catch (InterruptedException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (InterruptedException e) {
            logger.warn("Operation canceled: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof InsufficientPermissionsException) {
                DialogUtils.getInstance()
                        .presentInsufficientPermissionsDialog((InsufficientPermissionsException) cause);
            } else if (cause instanceof InvalidLoginException) {
                DialogUtils.getInstance().invalidLoginDialog(ForceExceptionUtils.getRootCauseMessage(cause));
            } else {
                try (final QuietCloseable<ByteArrayOutputStream> c0 = QuietCloseable.make(new ByteArrayOutputStream())) {
                    final ByteArrayOutputStream out = c0.get();

                    try (final QuietCloseable<PrintStream> c = QuietCloseable.make(new PrintStream(out))) {
                        final PrintStream ps = c.get();
                        cause.printStackTrace(ps);
                    }

                    StringBuffer strBuff = new StringBuffer();
                    strBuff.append("Unable to perform upgrade analysis:\n\n").append(new String(out.toByteArray())).append("\n\n ");
                    Utils.openError("Upgrade Error", strBuff.toString());
                }
            }
        }
    }
}
