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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.deployment.ForceIdeDeploymentPlugin;
import com.salesforce.ide.ui.actions.BaseAction;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public class DeploymentAction extends BaseAction implements IWorkbenchWindowActionDelegate {
    private static final Logger logger = Logger.getLogger(DeploymentAction.class);

    public DeploymentAction() throws ForceProjectException {
        super();
        actionController = new DeploymentActionController();
    }

    // if project is a selected resource, remove and replace w/ src/ to narrow
    // deploy considerations removing referenced packages
    @Override
    protected List<IResource> filter(List<IResource> selectedResources) {
        Set<IResource> resourceSet = new HashSet<IResource>();
        resourceSet.addAll(selectedResources);
        if (Utils.isNotEmpty(selectedResources)) {
            for (IResource resource : selectedResources) {
                if (resource.getType() == IResource.PROJECT) {
                    resourceSet.remove(resource);
                    resourceSet.add(getProjectService().getSourceFolder(resource.getProject()));
                    break;
                }
            }
        }
        return super.filter(new ArrayList<IResource>(resourceSet));
    }

    @Override
    public void init() {
        if (logger.isDebugEnabled()) {
            logger.debug("");
            logger.debug("***  D E P L O Y   T O   S E R V E R   ***");
            logger.debug("Deploying [" + (Utils.isNotEmpty(selectedResources) ? selectedResources.size() : 0)
                    + "] resources");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Force.com IDE: '" + ForceIdeDeploymentPlugin.getPluginId() + "' plugin, version "
                    + ForceIdeDeploymentPlugin.getBundleVersion());
        }
    }

    @Override
    public void execute(IAction action) {
        try {
            // instantiates the wizard container with the wizard and opens it
            WizardDialog dialog = actionController.getWizardDialog();
            dialog.create();
            UIUtils.placeDialogInCenter(getWorkbenchWindow().getShell(), dialog.getShell());
            Utils.openDialog(getProject(), dialog);
        } catch (Exception e) {
            logger.error("Unable to open deployment wizard", e);
        }
    }

    public void dispose() {
        
    }

    public void init(IWorkbenchWindow window) {
        setWorkbenchWindow(window);
        actionController.setWorkbenchWindow(window);
    }
}
