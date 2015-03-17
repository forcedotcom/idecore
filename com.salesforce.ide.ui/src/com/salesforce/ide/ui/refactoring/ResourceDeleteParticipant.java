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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.dialogs.WebOnlyDeleteMessageDialog;
import com.salesforce.ide.ui.internal.Messages;

/**
 * Handles refactor Force.com package and component delete function.
 * 
 * Note: "If an exception occurs while creating the change the refactoring can not be carried out and the participant
 * will be disabled for the rest of the eclipse session."
 * 
 * @author cwall
 */
public class ResourceDeleteParticipant extends DeleteParticipant implements ISharableParticipant {
    private static final Logger logger = Logger.getLogger(ResourceDeleteParticipant.class);

    private DeleteChange deleteChange = null;
    private boolean cancelFlag = false;

    //   C O N S T R U C T O R
    public ResourceDeleteParticipant() {
        super();
    }

    //   M E T H O D S

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor monitor, CheckConditionsContext context)
            throws OperationCanceledException {
        // REVIEWME: what to do w/ ResourceChangeChecker and/or ValidateEditChecker

        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                Set<IResource> deletedResources =
                        deleteChange.getRefactorController().getRefactorModel().getChangeResources();
                List<IResource> resources = new ArrayList<>();
                StringBuffer buffer = new StringBuffer();
                Component workflow =
                        deleteChange.getComponentFactory().getComponentByComponentType(Constants.WORKFLOW);
                Component portal = deleteChange.getComponentFactory().getComponentByComponentType(Constants.PORTAL);
                Component site =
                        deleteChange.getComponentFactory().getComponentByComponentType(Constants.CUSTOM_SITE);

                List<IResource> workflows = new ArrayList<>();
                List<IResource> sites = new ArrayList<>();
                List<IResource> portals = new ArrayList<>();

                for (Object o : deletedResources) {
                    if (o instanceof IFile) {
                        IFile file = (IFile) o;

                        if (file.getFileExtension().equals(portal.getFileExtension())) {
                            resources.add(file);
                            portals.add(file);
                        }

                        else if (file.getFileExtension().equals(site.getFileExtension())) {
                            resources.add(file);
                            sites.add(file);
                        }

                        else if (file.getFileExtension().equals(workflow.getFileExtension())) {
                            resources.add(file);
                            workflows.add(file);
                        }
                    }
                }

                Collections.sort(portals, new Comparator<IResource>() {
                    @Override
                    public int compare(IResource o1, IResource o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });

                Collections.sort(sites, new Comparator<IResource>() {
                    @Override
                    public int compare(IResource o1, IResource o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });

                Collections.sort(workflows, new Comparator<IResource>() {
                    @Override
                    public int compare(IResource o1, IResource o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });

                for (IResource file : portals) {
                    addBullet(buffer, NLS.bind(Messages.ResourceDeleteParticipant_fileLocallyDeleted_message, file
                            .getName()), NLS.bind(
                        Messages.ResourceDeleteParticipant_remotePortalConfirmation_message, resources
                                .indexOf(file), file.getFullPath().removeFileExtension().lastSegment()));
                }

                for (IResource file : sites) {
                    addBullet(buffer, NLS.bind(Messages.ResourceDeleteParticipant_fileLocallyDeleted_message, file
                            .getName()), NLS.bind(
                        Messages.ResourceDeleteParticipant_remoteSiteConfirmation_message, resources.indexOf(file),
                        file.getFullPath().removeFileExtension().lastSegment()));
                }

                if (workflows.size() == 1) {
                    IResource file = workflows.get(0);

                    addBullet(buffer, NLS.bind(Messages.ResourceDeleteParticipant_fileLocallyDeleted_message, file
                            .getName()), NLS.bind(
                        Messages.ResourceDeleteParticipant_remoteWorkflowConfirmation_message, resources
                                .indexOf(file)));
                }

                else if (workflows.size() > 0) {
                    for (IResource file : workflows) {
                        addBullet(buffer, NLS.bind(Messages.ResourceDeleteParticipant_fileLocallyDeleted_message,
                            file.getName()));
                    }

                    addBullet(buffer, NLS.bind(
                        Messages.ResourceDeleteParticipant_remoteWorkflowConfirmation_message, resources
                                .indexOf(workflows.get(0))));
                }

                boolean showWarning = true;
                if (resources.size() != deletedResources.size()
                        && !MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
                            Messages.ResourceDeleteParticipant_remoteConfirmation_title,
                            Messages.ResourceDeleteParticipant_remoteConfirmation_message)) {
                    cancelFlag = true;
                    showWarning = false;
                }

                if (resources.size() > 0 && showWarning) {
                    WebOnlyDeleteMessageDialog.openWarning(Display.getDefault().getActiveShell(), Messages.WARNING,
                        buffer.toString(), resources);

                    if (resources.size() == deletedResources.size()) {
                        cancelFlag = true;
                    }
                }
            }
        });

        if (cancelFlag) {
            deleteChange.setRemoteDeleteCanceled(true);
            return null;
        }

        if (deleteChange.getRefactorController().getRefactorModel().isChangeResourcesEmpty()) {
            logger.warn("Change elements for delete are null or empty");
            return null;
        }

        RefactoringStatus refactoringStatus = null;
        try {
            refactoringStatus = deleteChange.getRefactorController().validateDelete(monitor);
        } catch (InterruptedException e) {
            logger.warn("Operation canceled by user");
            throw new OperationCanceledException(e.getMessage());
        }

        return refactoringStatus;
    }

    private static void addBullet(StringBuffer buffer, String... msgs) {
        buffer.append("<li>"); //$NON-NLS-1$
        for (String msg : msgs) {
            buffer.append(msg);
        }
        buffer.append("</li>"); //$NON-NLS-1$
    }

    /**
     * Evaluates copy candidate and aggregates sub-elements.
     * 
     * Always return true so that this participant is involved in all copy transactions.
     */
    @Override
    protected boolean initialize(Object element) {
        if (logger.isDebugEnabled()) {
            logger.debug("*** " + getName() + " initiated ***");
        }

        // change elements will equate to a root resource move and a project package list containing
        // all affected elements
        if (element instanceof IResource) {
            IResource resource = (IResource) element;

            try {
                deleteChange = new DeleteChange();
                deleteChange.initialize(resource, new NullProgressMonitor());
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
                logger.warn(e.getClass().getSimpleName() + " occurred while initializing delete change", e);
            }
        }

        // always return true so that this participant is involved in all copy transactions
        return true;
    }

    @Override
    public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
        if (deleteChange == null) {
            deleteChange = new DeleteChange();
        }
        return deleteChange;
    }

    @Override
    public String getName() {
        return Constants.PLUGIN_NAME + " Delete Resource Participant";
    }

    @Override
    public void addElement(Object element, RefactoringArguments arguments) {
        if (element instanceof IResource == false || ((IResource) element).getType() != IResource.FILE) {
            return;
        }

        IResource resource = (IResource) element;
        try {
            deleteChange.addChangeResource(resource, new NullProgressMonitor());
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
