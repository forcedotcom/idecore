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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.CopyArguments;
import org.eclipse.ltk.core.refactoring.participants.CopyParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;

/**
 * Handles refactor Force.com package and component copy function.
 * 
 * Note: "If an exception occurs while creating the change the refactoring can not be carried out and the participant
 * will be disabled for the rest of the eclipse session."
 * 
 * @author cwall
 */
public class ResourceCopyParticipant extends CopyParticipant {

    private static final Logger logger = Logger.getLogger(ResourceCopyParticipant.class);

    private CopyChange copyChange = null;

    //   C O N S T R U C T O R
    public ResourceCopyParticipant() {
        super();
    }

    public ResourceCopyParticipant(RefactoringArguments arguments) {
        super();
        initialize(arguments);
    }

    //   M E T H O D S
    public CopyChange getCopyChange() {
        return copyChange;
    }

    @Override
    public String getName() {
        return Constants.PLUGIN_NAME + " Copy Resource Participant";
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor monitor, CheckConditionsContext context)
            throws OperationCanceledException {
        // REVIEWME: what to do w/ ResourceChangeChecker and/or ValidateEditChecker 

        CopyArguments arguments = getArguments();
        Object destination = arguments.getDestination();
        if (destination == null || destination instanceof IResource == false) {
            return copyChange.getRefactorController().createRefactoringStatus(RefactoringStatus.FATAL,
                "Destination is unknown or is not a resource");
        }

        IResource resource = (IResource) destination;
        RefactoringStatus refactoringStatus = null;
        try {
            refactoringStatus = copyChange.checkConditions(resource, monitor);

            ChangeRefactorModel refactorModel = copyChange.getRefactorController().getRefactorModel();

            // the destination might have been change
            if (refactorModel.isDestinationUpdated()) {
                CopyArguments newCopyArguments = new CopyArguments(resource, arguments.getExecutionLog());
                if (logger.isInfoEnabled()) {
                    logger.info("Destination changed from '" + resource.getProjectRelativePath().toPortableString()
                            + "' to '" + refactorModel.getDestinationPath() + "'");
                }
                initialize(newCopyArguments);
            }

        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            // catch exception because exceptions cause the participant to become deactivated for future refactorings
            logger.warn("Unable to validate destination '"
                    + copyChange.getRefactorController().getRefactorModel().getDestinationPath()
                    + "' of change request: " + logMessage);
        } catch (InterruptedException e) {
            throw new OperationCanceledException(e.getMessage());
        }

        return refactoringStatus;
    }

    /**
     * Evaluates copy candidate and aggregates sub-elements.
     * 
     * Always return true so that this participant is involved in all copy transactions.
     */
    @Override
    protected boolean initialize(Object element) {
        if (logger.isDebugEnabled()) {
            logger.debug("***" + getName() + " initiated ***");
        }

        // change elements will equate to a root resource copy and a project package list containing
        // all affected elements
        if (element instanceof IResource) {
            IResource resource = (IResource) element;

            try {
                copyChange = new CopyChange();
                copyChange.initialize(resource, new NullProgressMonitor());
            } catch (FactoryException e) {
                // catch exception because exceptions cause the participant to become deactivated for future refactorings
                logger.error(
                    "Unable to handle resource '" + resource.getProjectRelativePath().toPortableString() + "'", e);
            } catch (CoreException e) {
                String logMessage = Utils.generateCoreExceptionLog(e);
                // catch exception because exceptions cause the participant to become deactivated for future refactorings
                logger.warn("Unable to handle resource '" + resource.getProjectRelativePath().toPortableString()
                        + "': " + logMessage, e);
            } catch (InterruptedException e) {
                logger.warn("Operation canceled by user");
            } catch (RuntimeException e) {
                // catch exception because exceptions cause the participant to become deactivated for future refactorings
                logger.warn(e.getClass().getSimpleName() + " occurred while initializing copy change", e);
            }
        }

        // always return true so that this participant is involved in all copy transactions
        return true;
    }

    @Override
    public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
        copyChange = new CopyChange();
        return copyChange;
    }

    public void addElement(Object element, RefactoringArguments arguments) {
        if (element instanceof IResource == false || ((IResource) element).getType() != IResource.FILE) {
            return;
        }

        IResource resource = (IResource) element;
        try {
            copyChange.addChangeResource(resource, new NullProgressMonitor());
        } catch (FactoryException e) {
            // catch exception because exceptions cause the participant to become deactivated for future refactorings
            logger.error("Unable to handle resource '" + resource.getProjectRelativePath().toPortableString() + "'", e);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            // catch exception because exceptions cause the participant to become deactivated for future refactorings
            logger.error("Unable to handle resource '" + resource.getProjectRelativePath().toPortableString() + "': "
                    + logMessage, e);
        } catch (InterruptedException e) {
            logger.warn("Operation canceled by user");
        }
    }
}
