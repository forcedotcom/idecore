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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.ui.internal.Messages;

/**
 *
 * Note: Catch exception because exceptions cause the participant to become deactivated for future refactorings
 * 
 * @author cwall
 */
public class DeleteChange extends BaseChange {
    private static final Logger logger = Logger.getLogger(DeleteChange.class);
    boolean remoteDeleteCanceled = false;

    // C O N S T R U C T O R
    public DeleteChange() {
        super();
        this.refactorController = new DeleteRefactorController();
    }

    // M E T H O D S
    @Override
    public DeleteRefactorController getRefactorController() {
        return (DeleteRefactorController) refactorController;
    }

    @Override
    public Object getModifiedElement() {
        return null;
    }

    @Override
    public String getName() {
        return "Delete...";
    }

    @Override
    public void initializeValidationData(IProgressMonitor monitor) {
    }

    public void setRemoteDeleteCanceled(boolean remoteDeleteCanceled) {
        this.remoteDeleteCanceled = remoteDeleteCanceled;
    }

    protected boolean initialize(IResource resource, IProgressMonitor monitor) throws FactoryException, CoreException,
            InterruptedException {
        // change elements will equate to a root resource copy and a project
        // package list containing
        // all affected elements
        refactorController.getRefactorModel().setProject(resource.getProject());
        ProjectPackageList projectPackageList = refactorController.loadProjectPackageList(resource, monitor);
        if (projectPackageList != null) {
            if (!ContainerDelegate.getInstance().getServiceLocator().getProjectService().isReferencedPackageResource(resource)) {
                refactorController.getRefactorModel().setProjectPackageList(projectPackageList);
                refactorController.getRefactorModel().refreshChangeResources(false);
                if (logger.isDebugEnabled()) {
                    logger.debug("Added project package list for resource '"
                            + resource.getFullPath().toPortableString() + "'");
                }
            }
        }

        // always return true so that this participant is involved in all copy
        // transactions
        return true;
    }

    public void addChangeResource(IResource resource, IProgressMonitor monitor) throws FactoryException, CoreException,
            InterruptedException {
        refactorController.addToProjectPackageList(resource, monitor);
        refactorController.getRefactorModel().refreshChangeResources(false);
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor monitor) throws CoreException, OperationCanceledException {

        RefactoringStatus refactoringStatus = new RefactoringStatus();

        if (remoteDeleteCanceled) {
            return refactoringStatus;
        }

        if (refactorController.getRefactorModel().isChangeResourcesEmpty()) {
            refactoringStatus.addEntry(refactorController.createFatalRefactoringStatusEntry(Constants.PLUGIN_NAME
                    + " does not support deleting resource"));
            return refactoringStatus;
        }

        // we delete on server BEFORE we delete locally
        List<RefactoringStatusEntry> entries = null;
        try {
            entries = getRefactorController().performRemoteDelete(monitor);
        } catch (Exception e) {
            if (!(e instanceof OperationCanceledException)) {
                logger.error("Unable to perform server delete", e);
            }

            entries = new ArrayList<>();
            entries.add(refactorController.createFatalRefactoringStatusEntry("Unable to perform server delete: "
                    + e.getMessage() + ".  " + Messages.ResourceDeleteParticipant_exception_message));
        }

        if (Utils.isNotEmpty(entries)) {
            for (RefactoringStatusEntry refactoringStatusEntry : entries) {
                refactoringStatus.addEntry(refactoringStatusEntry);
            }
        }

        return refactoringStatus;
    }

    @Override
    public Change perform(IProgressMonitor monitor) {
        if (!refactorController.getRefactorModel().isChangeResourcesEmpty()) {
            boolean overallSuccess = true;
            for (IResource resource : refactorController.getRefactorModel().getChangeResources()) {
                if (!resource.exists()) {
                    continue;
                }

                if (resource.getType() == IResource.PROJECT) {
                    refactorController.deleteSavedProjectContent(resource);
                }

                try {
                    resource.delete(true, monitor);
                } catch (CoreException e) {
                    String logMessage = Utils.generateCoreExceptionLog(e);
                    // catch and display - throwing all the way up will disable
                    // the plugin's delete refactoring mechanism
                    logger.error("Unable to delete composite file '"
                            + resource.getProjectRelativePath().toPortableString() + "': " + logMessage);
                    Utils.openError("Delete Error", "Unable to delete composite file '"
                            + resource.getProjectRelativePath().toPortableString() + "': " + e.getMessage());
                    overallSuccess = false;
                    continue;
                }

                if (logger.isInfoEnabled()) {
                    logger.info("Deleted composite resource '" + resource.getFullPath().toPortableString() + "'");
                }
            }

            setSuccess(overallSuccess);
        }

        return new NullChange();
    }
}
