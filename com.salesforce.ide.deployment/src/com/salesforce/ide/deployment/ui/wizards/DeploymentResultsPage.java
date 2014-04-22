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
package com.salesforce.ide.deployment.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.deployment.internal.utils.DeploymentMessages;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public class DeploymentResultsPage extends BaseDeploymentPage {

    static final Logger logger = Logger.getLogger(DeploymentResultsPage.class);

    private static final String WIZARDPAGE_ID = "deploymentResultPage";

    private DeploymentResultsComposite resultsComposite = null;

    //   C O N S T R U C T O R
    public DeploymentResultsPage(DeploymentWizard deploymentWizard) {
        super(WIZARDPAGE_ID, deploymentWizard);
    }

    //   M E T H O D S
    @Override
    public void createControl(Composite parent) {
        resultsComposite = new DeploymentResultsComposite(parent, SWT.NULL);
        setControl(resultsComposite);
        resultsComposite.pack();
        setPageComplete(true);

        UIUtils.setHelpContext(resultsComposite, this.getClass().getSimpleName());
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            try {
                // NOTE: this feature is disabled until priority is restored.
                // W-572143

                // deploy payload
                deploy();

                // prepare view of results
                prepareResultsViewComposite(resultsComposite, deploymentWizard.getDeploymentController());

                // all done for now!
                deploymentWizard.getDeploymentController().setCanComplete(true);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getTargetException();
                if (cause instanceof InsufficientPermissionsException) {
                    DialogUtils.getInstance().presentInsufficientPermissionsDialog(
                        (InsufficientPermissionsException) cause);
                } else {
                    logger.error(DeploymentMessages.getString("DeploymentWizard.ResultsPage.error"), e);
                    Utils.openError(e, true, DeploymentMessages.getString("DeploymentWizard.ResultsPage.error"));
                }
            }
            setTitleAndDescription(DeploymentMessages.getString("DeploymentWizard.ResultsPage.title") + " "
                    + getStepString(), DeploymentMessages.getString("DeploymentWizard.ResultsPage.description"));
        }
        super.setVisible(visible);
    }

    private void deploy() throws InvocationTargetException {
        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        try {
            service.run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Deploying components...", 1);
                    try {
                        deploymentWizard.getDeploymentController().finish(new SubProgressMonitor(monitor, 3));
                        monitor.worked(1);
                    } finally {
                        monitor.subTask("Done");
                    }
                }
            });
        } catch (InterruptedException e) {
            logger.warn("Operation cancelled: " + e.getMessage());
        }
    }
}
