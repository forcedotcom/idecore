/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.collect.Lists;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.ui.sync.ComponentSyncParticipant;

public final class SynchronizeHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(SynchronizeHandler.class);

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event).getWorkbench();
        ISelection selection = getSelection(event);
        if (selection instanceof ITextSelection) { // From text editor
            execute(workbench, HandlerUtil.getActiveEditorInput(event));
        } else if (selection instanceof IStructuredSelection) { // From package explorer
            execute(workbench, getStructuredSelection(event));
        }
        return null;
    }

    private void execute(IWorkbench workbench, IEditorInput editorInput) {
        if (editorInput instanceof IFileEditorInput) {
            IFileEditorInput input = (IFileEditorInput) editorInput;
            IResource file = input.getFile();
            IProject project = file.getProject();
            sync(workbench, Lists.newArrayList(file), project);
        }
    }

    public static void execute(final IWorkbench workbench, final IStructuredSelection selection) {
        if (null == workbench)
            throw new IllegalArgumentException("The workbench argument cannot be null");
        if (null == selection)
            throw new IllegalArgumentException("The selection argument cannot be null");

        final List<IResource> selectedResources = getFilteredResources(selection);
        if (Utils.isEmpty(selectedResources)) {
            logger.warn("Unable to perform sync - nothing selected");
            Utils.openWarn("No Resource Selected", "Please select a " + Constants.PRODUCT_NAME
                    + " resource to synchronize with remote server instance.");
            return;
        }

        final IProject project = selectedResources.get(0).getProject();
        for (final IResource resource : selectedResources) {
            if (!project.equals(resource.getProject())) {
                logger.warn("Unable to synchronize resources from multiple projects at the same time. Only synchronizing resources in "
                        + project.getName());
                break;
            }
        }

        sync(workbench, selectedResources, project);
    }

    protected static void sync(final IWorkbench workbench, final List<IResource> selectedResources,
            final IProject project) {
        final ComponentSyncParticipant syncParticipant = new ComponentSyncParticipant(project, selectedResources);
        try {
            prepareSyncParticipant(workbench, syncParticipant);

            ISynchronizeManager manager = TeamUI.getSynchronizeManager();
            manager.addSynchronizeParticipants(new ISynchronizeParticipant[] { syncParticipant });
            ISynchronizeView view = manager.showSynchronizeViewInActivePage();
            view.display(syncParticipant);
            syncParticipant.reset();

        } catch (InterruptedException e) {
            logger.warn("Operation canceled: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();

            if (cause instanceof TeamException && ((TeamException) cause).getStatus() != null
                    && ((TeamException) cause).getStatus().getException() != null) {
                cause = ((TeamException) cause).getStatus().getException();
            }

            if (cause instanceof InsufficientPermissionsException) {
                // log failure; option enabling occurs downstream
                logger.warn("Unable to synchronize resources with server: " + cause.getMessage());

                InsufficientPermissionsException ex = (InsufficientPermissionsException) e.getTargetException();
                // occurred during project create/update which determines message text
                ex.setShowUpdateCredentialsMessage(true);

                // show dialog
                DialogUtils.getInstance().presentInsufficientPermissionsDialog(ex);
            } else if (cause instanceof InvalidLoginException) {
                // log failure
                logger.warn("Unable to synchronize resources with server: " + cause.getMessage());

                InvalidLoginException ex = (InvalidLoginException) cause;
                // occurred during project create/update which determines message text
                ex.setShowUpdateCredentialsMessage(true);

                // choose further project create direction
                DialogUtils.getInstance().invalidLoginDialog(ex.getMessage(), null, true);
            } else {
                logger.error("Unable to synchronize resources with server", ForceExceptionUtils.getRootCause(e));
                Utils.openError(e, true,
                    "Unable to synchronize resources with server: " + ForceExceptionUtils.getRootCauseMessage(e));
            }
        }
    }

    private static void prepareSyncParticipant(final IWorkbench workbench,
            final ComponentSyncParticipant syncParticipant) throws InvocationTargetException, InterruptedException {
        workbench.getProgressService().run(true, true, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("Preparing and evaluating Synchronize results", 3);
                try {
                    syncParticipant.execute(monitor);
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        });
    }

}
