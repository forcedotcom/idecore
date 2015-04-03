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
package com.salesforce.ide.ui.refactoring;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ProjectPackageList;

public class CopyChange extends BaseChange {
    private static final Logger logger = Logger.getLogger(CopyChange.class);

    // C O N S T R U C T O R
    public CopyChange() {
        super();
        this.refactorController = new ChangeRefactorController();
    }

    // M E T H O D S
    @Override
    public ChangeRefactorController getRefactorController() {
        return (ChangeRefactorController) refactorController;
    }

    @Override
    public Object getModifiedElement() {
        return null;
    }

    @Override
    public String getName() {
        return "Copy components";
    }

    @Override
    public void initializeValidationData(IProgressMonitor monitor) {
    }

    protected boolean initialize(IResource resource, IProgressMonitor monitor) throws FactoryException, CoreException,
            InterruptedException {
        // change elements will equate to a root resource copy and a project package list containing
        // all affected elements
        refactorController.getRefactorModel().setProject(resource.getProject());
        ProjectPackageList projectPackageList = refactorController.loadProjectPackageList(resource, monitor);
        if (projectPackageList != null) {
            refactorController.getRefactorModel().addChangeResource(resource);
            refactorController.getRefactorModel().setProjectPackageList(projectPackageList);
            if (logger.isDebugEnabled()) {
                logger.debug("Added project package list for resource '" + resource.getFullPath().toPortableString()
                        + "'");
            }
        }

        // always return true so that this participant is involved in all copy transactions
        return true;
    }

    public void addChangeResource(IResource resource, IProgressMonitor monitor) throws FactoryException, CoreException,
            InterruptedException {
        refactorController.getRefactorModel().addChangeResource(resource);
        refactorController.addToProjectPackageList(resource, monitor);
    }

    public RefactoringStatus checkConditions(IResource origDesintation, IProgressMonitor monitor)
            throws OperationCanceledException, CoreException, InterruptedException {
        // REVIEWME: what to do w/ ResourceChangeChecker and/or ValidateEditChecker

        ChangeRefactorModel refactorModel = getRefactorController().getRefactorModel();
        refactorModel.setDestinationResource(origDesintation);

        monitorCheck(monitor);

        if (refactorModel.isChangeResourcesEmpty()) {
            return refactorController.createFatalRefactoringStatus(Constants.PLUGIN_NAME
                    + " does not support copying resource to destination '" + refactorModel.getDestinationPath() + "'");
        }

        boolean forceManaged = getProjectService().isManagedResource(origDesintation);
        if (!forceManaged) {
            return refactorController.createFatalRefactoringStatus("Destination is not a " + Constants.PLUGIN_NAME
                    + " managed project");
        }

        monitorCheck(monitor);
        return getRefactorController().validateChangeDestination(monitor);
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
        if (refactorController.getRefactorModel().isChangeResourcesEmpty()) {
            return refactorController.createFatalRefactoringStatus(Constants.PLUGIN_NAME
                    + " does not support moving resource to destination '"
                    + getRefactorController().getRefactorModel().getDestinationPath() + "'");
        }
        return null;
    }

    @Override
    public Change perform(IProgressMonitor monitor) throws CoreException {

        // abort if online nature is not applied
        if (!getProjectService().isManagedOnlineProject(refactorController.getRefactorModel().getProject())) {
            logger.warn("Unable to perform copy on server in project '"
                    + refactorController.getRefactorModel().getProject().getName()
                    + "' - project is not online enabled");
            Utils.openInfo("Offline Mode", "Project is currently offline.  Resource will not be copied to server.");
            setSuccess(false);
            return new NullChange();
        }

        try {
            refactorController.finish(new SubProgressMonitor(monitor, 3));
            setSuccess(true);
        } catch (Exception e) {
            logger.error("Unable to copy resource", e);
            setSuccess(false);
            // commented out because if copy fails once, the extension is disabled for the entire session
            //ExceptionUtils.throwNewCoreException(e);
        }

        // no undo change at this time
        return new NullChange();
    }
}
