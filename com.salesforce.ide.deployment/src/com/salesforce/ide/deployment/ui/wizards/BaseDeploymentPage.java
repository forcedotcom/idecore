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

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.SalesforceEndpoints;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.EmptyDeployResultExt;
import com.salesforce.ide.deployment.internal.DeploymentResult;
import com.salesforce.ide.deployment.internal.utils.DeploymentMessages;
import com.salesforce.ide.ui.internal.utils.DeployResultsViewAssembler;
import com.salesforce.ide.ui.internal.wizards.BaseOrgWizardPage;

public abstract class BaseDeploymentPage extends BaseOrgWizardPage {
    private static final Logger logger = Logger.getLogger(BaseDeploymentPage.class);

    protected DeploymentWizard deploymentWizard = null;
    protected int step = 0;

    public BaseDeploymentPage(String wizardName, DeploymentWizard deploymentWizard) {
        super(wizardName);
        this.deploymentWizard = deploymentWizard;
    }

    //   G E T T E R   /   S E T T E R S
    public DeploymentController getController() {
        return deploymentWizard.getDeploymentController();
    }

    public SalesforceEndpoints getSalesforceEndpoints() {
        return ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getSalesforceEndpoints();
    }

    protected DeploymentWizardModel getDeploymentWizardModel() {
        return getController().getDeploymentWizardModel();
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getStep() {
        return this.step;
    }

    public String getStepString() {
        return "(Step " + step + " of " + deploymentWizard.getPageCount() + ")";
    }

    // for lack of a better way to size the wizard
    protected void setWizardCosmetics(Composite parent) {
        parent.getShell().setSize(new Point(525, 615));
    }

    @Override
    public IWizardPage getNextPage() {
        return deploymentWizard.getNextPage(this);
    }

    protected void prepareResultsViewComposite(DeploymentResultsComposite resultsComposite,
            DeploymentController deploymentWizardController) {

        DeploymentResult deploymentResult = deploymentWizardController.getDeploymentResult();
        if (deploymentResult == null) {
            logger.warn(DeploymentMessages.getString("DeploymentWizard.BaseDeploymentPage.NoResults.message"));
            Utils.openQuestion(DeploymentMessages.getString("DeploymentWizard.BaseDeploymentPage.NoResults.title"),
                DeploymentMessages.getString("DeploymentWizard.BaseDeploymentPage.NoResults.message"));
            return;
        }

        // set page description
        resultsComposite.setLblResult(deploymentResult.isSuccess());
        DeployResultExt result = deploymentResult.getDeployResultHandler();
        if (!deploymentResult.isSuccess()) {
            if (result != null && result.getMessageCount() == 0) {
                resultsComposite.setLblReason(result.getMessageHandler().getDisplayMessages()[0]);
            } else if (result != null && result.getMessageCount() > 1) {
                resultsComposite.setLblReason(DeploymentMessages
                        .getString("DeploymentWizard.Results.MultipleProblems.message"));
            }
        } else {
            resultsComposite.setLblReason(DeploymentMessages.getString("DeploymentWizard.Results.Success.message"));
        }

        // create results tree
        DeployResultsViewAssembler assembler =
                new DeployResultsViewAssembler(result == null ? new EmptyDeployResultExt() : result, resultsComposite
                        .getTreeResults(), deploymentWizardController.getProject(), ContainerDelegate.getInstance().getServiceLocator().getProjectService());
        assembler.assembleDeployResultsTree();

        LogViewShell logView =
                new LogViewShell(getShell(), deploymentResult.getDeployLog(), deploymentResult.getRemoteDeployLog(),
                        deploymentWizardController.getDeploymentWizardModel().getProjectName());
        resultsComposite.setLogShellView(logView);

    }
}
