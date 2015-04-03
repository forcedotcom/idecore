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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.salesforce.ide.deployment.internal.utils.DeploymentMessages;
import com.salesforce.ide.ui.internal.wizards.BaseWizard;

public class DeploymentWizard extends BaseWizard {
    private static final Logger logger = Logger.getLogger(DeploymentWizard.class);

    private DeploymentDestinationSettingsPage destinationPage = null;
    private DeploymentArchivePage archivePage = null;
    private DeploymentPlanPage planPage = null;
    private DeploymentResultsPage resultsPage = null;

    //   C O N S T R U C T O R S
    public DeploymentWizard(IProject project, List<IResource> resources) {
        super();
        if (resources == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }
        this.project = project;
        controller = new DeploymentController(project, resources);

        if (logger.isDebugEnabled()) {
            logger.debug("");
            logger.debug("***   D E P L O Y M E N T   W I Z A R D   ***");
        }
    }

    public DeploymentController getDeploymentController() {
        return (DeploymentController) controller;
    }

    @Override
    protected String getWindowTitleString() {
        return DeploymentMessages.getString("DeploymentWizard.title");
    }

    @Override
    public void addPages() {
        int steps = 1;
        destinationPage = new DeploymentDestinationSettingsPage(this);
        destinationPage.setStep(steps++);
        addPage(destinationPage);
        archivePage = new DeploymentArchivePage(this);
        archivePage.setStep(steps++);
        addPage(archivePage);
        planPage = new DeploymentPlanPage(this);
        planPage.setStep(steps++);
        addPage(planPage);
        resultsPage = new DeploymentResultsPage(this);
        resultsPage.setStep(steps++);
        addPage(resultsPage);
    }

    @Override
    public boolean performFinish() {
        controller.dispose();
        return true;
    }
}
