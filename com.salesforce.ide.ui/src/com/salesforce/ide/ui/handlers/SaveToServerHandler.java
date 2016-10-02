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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import com.google.common.collect.Lists;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.ui.actions.SaveToServerActionController;
import com.salesforce.ide.ui.internal.startup.ForceStartup;

public final class SaveToServerHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(SaveToServerHandler.class);

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event).getWorkbench();
        ISelection selection = getSelection(event);
        if (selection instanceof ITextSelection) { // From text editor
            execute(workbench, buildController(selection, HandlerUtil.getActiveEditorInput(event)));
        } else if (selection instanceof IStructuredSelection) { // From package explorer
            execute(workbench, buildController((IStructuredSelection) selection));
        }
        return null;
    }

    public static final void execute(final IWorkbench workbench, final SaveToServerActionController actionController)
            throws IllegalArgumentException {
        if (null != actionController) {
            actionController.setWorkbenchWindow(workbench.getActiveWorkbenchWindow());
            if (actionController.preRun()) {
                fetchRemoteComponents(workbench, actionController);
                actionController.postRun();
            }
        }
    }

    private static void fetchRemoteComponents(
        final IWorkbench workbench,
        final SaveToServerActionController actionController
    ) {
        final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
                    InterruptedException {
                if (monitor == null) {
                    monitor = new NullProgressMonitor();
                }
                monitor.beginTask("", 4);

                ForceStartup.removePackageManifestChangeListener();
                try {
                    boolean success = actionController.saveResourcesToServer(monitor);
                    if (success) {
                        getProjectService().flagSkipBuilder(actionController.getProject());
                    }
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    ForceStartup.addPackageManifestChangeListener();
                    monitor.subTask("Done");
                }
            }
        };

        final IProgressService service = workbench.getProgressService();
        try {
            service.run(true, true, operation);
        } catch (InterruptedException e) {
            logger.warn("Operation canceled: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof InsufficientPermissionsException) {
                DialogUtils.getInstance()
                        .presentInsufficientPermissionsDialog((InsufficientPermissionsException) cause);
            } else if (cause instanceof InvalidLoginException) {
                // log failure
                logger.warn("Unable to save resource(s): " + ForceExceptionUtils.getRootCauseMessage(cause));
                // choose further project create direction
                DialogUtils.getInstance().invalidLoginDialog(ForceExceptionUtils.getRootCauseMessage(cause));
            } else {
                logger.error("Unable to save resource(s)", ForceExceptionUtils.getRootCause(e));
                final StringBuilder msg =
                        new StringBuilder().append("Unable to save resources:\n\n")
                                .append(ForceExceptionUtils.getStrippedRootCauseMessage(e)).append("\n\n ");
                Utils.openError(e, true, msg.toString());
            }
        }
    }

    private static SaveToServerActionController buildController(ISelection selection, IEditorInput editorInput) {
        if (editorInput instanceof IFileEditorInput) {
            IFileEditorInput input = (IFileEditorInput) editorInput;
            IResource file = input.getFile();
            IProject project = file.getProject();
            return buildController(selection, Lists.newArrayList(file), project);
        } else {
            return null;
        }
    }

    private static SaveToServerActionController buildController(final IStructuredSelection selection) {
        final IAdapterManager adapterManager = Platform.getAdapterManager();

        final List<IResource> selectedResources = new ArrayList<>();
        for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
            final IResource selectedResource = (IResource) adapterManager.getAdapter(iterator.next(), IResource.class);
            if (null != selectedResource) {
                selectedResources.add(selectedResource);
            }
        }

        final List<IResource> filteredResources = filter(selectedResources);
        if (filteredResources.isEmpty())
            return null;

        final IProject project = filteredResources.get(0).getProject();
        for (final IResource resource : filteredResources) {
            if (!project.equals(resource.getProject())) {
                logger.warn("Unable to save resources from multiple projects at the same time. Only saving resources from "
                        + project.getName());
                break;
            }
        }

        return buildController(selection, filteredResources, project);
    }

    private static SaveToServerActionController buildController(final ISelection selection,
            final List<IResource> filteredResources, final IProject project) {
        final SaveToServerActionController actionController = new SaveToServerActionController();
        actionController.setProject(project);
        actionController.setSelection(selection);
        actionController.setSelectedResources(filteredResources, false);
        return actionController;
    }

}
