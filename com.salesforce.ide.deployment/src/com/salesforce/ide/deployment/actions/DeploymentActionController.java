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
package com.salesforce.ide.deployment.actions;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.deployment.internal.utils.DeploymentMessages;
import com.salesforce.ide.deployment.ui.wizards.DeploymentWizard;
import com.salesforce.ide.ui.actions.ActionController;

public class DeploymentActionController extends ActionController {
    private static final Logger logger = Logger.getLogger(DeploymentActionController.class);

    public DeploymentActionController() {
        super();
    }

    public DeploymentActionController(IProject project, IWorkbenchWindow window) {
        super(project, window);
    }

    @Override
    public boolean preRun(IAction action) {
        boolean hasDeployableComponents = getServiceLocator().getProjectService().hasManagedComponents(selectedResources);
        if (!hasDeployableComponents) {
            logger.warn(DeploymentMessages.getString("Deployment.NoDeployable.message"));
            Utils.openWarn(DeploymentMessages.getString("Deployment.NoDeployable.title"), DeploymentMessages
                    .getString("Deployment.NoDeployable.message"));
            return false;
        }

        // proactively sync check against org to avoid overwriting updated content; false to not cancel on sync error
        return syncCheck(false);
    }

    @Override
    public WizardDialog getWizardDialog() throws ForceProjectException {
        DeploymentWizard deploymentWizard = new DeploymentWizard(project, selectedResources);
        deploymentWizard.init(getWorkbenchWindow().getWorkbench(), (IStructuredSelection) selection);
        return new WizardDialog(getShell(), deploymentWizard);
    }

    @Override
    public void postRun(IAction action) {}
}
