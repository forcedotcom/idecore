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
package com.salesforce.ide.ui.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;

import com.google.common.annotations.VisibleForTesting;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.MarkerUtils;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.IDeployResultExt;
import com.salesforce.ide.core.services.PackageDeployService;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceTimeoutException;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.sforce.soap.metadata.DeployOptions;

public class SaveToServerActionController extends ActionController {
    private static final Logger logger = Logger.getLogger(SaveToServerActionController.class);

    protected ProjectPackageList projectPackageList = null;
    protected DeployResultExt deployResultExt = null;

    public SaveToServerActionController() {
        super();
    }

    public IDeployResultExt getDeployResultExt() {
        return deployResultExt;
    }

    @Override
    public WizardDialog getWizardDialog() {
        return null;
    }

    @Override
    public boolean preRun() {
        if (Utils.isEmpty(selectedResources)) {
            logger.info("Operation cancelled.  Resources not provided.");
            return false;
        }

        // skip if not a force managed resource
        if (!getProjectService().isManagedResources(selectedResources)) {
            Utils.openError(
                "Not Managed Resource",
                "Unable to save resource '" + getSelectedResource().getName() + "'.  Resource is not a " + Constants.PRODUCT_NAME + " resource.");
            return false;
        }

        // if dirty, ask user what he/she wants to do
        if (!checkForDirtyResources()) {
            return false;
        }

        // proactively sync check against org to avoid overwriting updated content; true to cancel on sync error
        boolean syncResult = syncCheck(false);
        if (!syncResult) {
            return false;
        }

        boolean response = getUserConfirmation();

        if (!response) {
            if (logger.isInfoEnabled()) {
                logger.info("Save to server cancelled by user");
            }
            return false;
        }

        try {
            projectPackageList = getProjectPackageList();
            projectPackageList.setProject(project);
        } catch (FactoryException e) {
            logger.error("Unable to prepare project package list for resources", e);
            Utils.openError(e, true, "Unable to prepare project package list for resources:\n\n" + ForceExceptionUtils.getRootCauseMessage(e));
            return false;
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.error("Unable to prepare project package list for resources: " + logMessage, e);
            Utils.openError(e, true, "Unable to prepare project package list for resources:\n\n" + ForceExceptionUtils.getRootCauseMessage(e));
            return false;
        } catch (InterruptedException e) {
            logger.warn("Save to server operation canceled by user");
            return false;
        }

        return true;
    }

    public boolean saveResourcesToServer(IProgressMonitor monitor)
        throws ForceConnectionException, FactoryException, InterruptedException, CoreException, IOException,
        ServiceException, ForceRemoteException, InvocationTargetException, Exception {
        monitorCheck(monitor);
        return deploy(monitor);
    }

    @Override
    public void postRun() {}

    private boolean deploy(IProgressMonitor monitor)
        throws ForceConnectionException, FactoryException, InterruptedException, CoreException, IOException,
        ServiceException, ForceRemoteException, InvocationTargetException, Exception {
        monitorCheck(monitor);
        monitorSubTask(monitor, "Saving to server...");

        // compile and save new component
        final PackageDeployService packageDeployService = getServiceLocator().getPackageDeployService();
        try {
            deployResultExt = packageDeployService.deploy(projectPackageList, monitor, makeDeployOptions(packageDeployService));
        } catch (ServiceTimeoutException ex) {
            deployResultExt =
                    packageDeployService.handleDeployServiceTimeoutException(ex, "save to server",
                        monitor);
        }
        monitorWork(monitor);

        if (deployResultExt == null) {
            logger.error("Unable to finalize new component creation - deploy result is null");
            return false;
        }

        return getProjectService().handleDeployResult(projectPackageList, deployResultExt, true, monitor);
    }

    public DeployOptions makeDeployOptions(PackageDeployService packageDeployService) {
        final DeployOptions deployOptions = packageDeployService.makeDefaultDeployOptions(false);
        deployOptions.setIgnoreWarnings(true);
        return deployOptions;
    }

    @VisibleForTesting
    protected boolean checkForDirtyResources() {
        if (Utils.isEmpty(selectedResources)) {
            logger.info("Operation cancelled.  Resources not provided.");
            return false;
        }

        for (IResource selectedResource : selectedResources) {
            boolean dirty = MarkerUtils.getInstance().isDirty(selectedResource);
            if (dirty) {
                boolean result =
                        !Utils.openQuestion("Confirm Save Dirty Resource", "Save resource '"
                                + selectedResource.getName() + "' is dirty.\n\n" + "Continue to save to server?");
                if (result) {
                    return false;
                }
            }
        }

        return true;
    }
    
    @VisibleForTesting
    protected boolean getUserConfirmation() {
    	return Utils.openQuestion(getProject(), getShell(), "Confirm Save", UIMessages.getString("SaveToServerHandler.Overwrite.message"));
    }
    
    @VisibleForTesting
    protected ProjectPackageList getProjectPackageList() throws CoreException, InterruptedException, FactoryException {
    	return getProjectService().getProjectContents(selectedResources, new NullProgressMonitor());
    }
}
