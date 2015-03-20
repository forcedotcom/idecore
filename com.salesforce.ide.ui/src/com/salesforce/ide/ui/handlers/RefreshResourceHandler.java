package com.salesforce.ide.ui.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.core.services.RetrieveException;
import com.salesforce.ide.ui.actions.RefreshResourceActionController;
import com.salesforce.ide.ui.internal.startup.ForceStartup;

public final class RefreshResourceHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(RefreshResourceHandler.class);

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event).getWorkbench();
        final IStructuredSelection selection = getSelection(event);
        execute(workbench, selection);
        return null;
    }

    public static final void execute(final IWorkbench workbench, final IStructuredSelection selection) throws IllegalArgumentException {
        if (null == workbench) throw new IllegalArgumentException("The workbench argument cannot be null");
        if (null == selection) throw new IllegalArgumentException("The selection argument cannot be null");

        if (!selection.isEmpty()) {
            final RefreshResourceActionController actionController = buildController(selection);
            if (null != actionController) {
                fetchRemoteComponents(workbench, actionController);
            }
        }
    }

    private static void fetchRemoteComponents(final IWorkbench workbench, final RefreshResourceActionController actionController) {
        final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
                if (monitor == null) {
                    monitor = new NullProgressMonitor();
                }
                monitor.beginTask("", 12);

                ForceStartup.removePackageManifestChangeListener();
                try {
                    boolean success = actionController.refreshResources(monitor);
                    if (!success) {
                        throw new RetrieveException(
                                "Error(s) occurred while refreshing resource(s).  See Problems View for details.");
                    }
                    monitor.worked(1);
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    ForceStartup.addPackageManifestChangeListener();
                    monitor.done();
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
                logger.warn("Unable to refresh resource(s): " + ForceExceptionUtils.getRootCauseMessage(cause));
                // choose further project create direction
                DialogUtils.getInstance().invalidLoginDialog(ForceExceptionUtils.getRootCauseMessage(cause));
            } else {
                logger.error("Unable to refresh resource(s)", ForceExceptionUtils.getRootCause(cause));
                final StringBuilder msg = new StringBuilder()
                    .append("Unable to refresh resources:\n\n")
                    .append(ForceExceptionUtils.getStrippedRootCauseMessage(e))
                    .append("\n\n ")
                    ;
                Utils.openError("Refresh Error", msg.toString());
            }
        }
    }

    private static RefreshResourceActionController buildController(final IStructuredSelection selection) {
        final List<IResource> filteredResources = getFilteredResources(selection);
        if (filteredResources.isEmpty()) return null;

        final IProject project = filteredResources.get(0).getProject();
        for (final IResource resource : filteredResources) {
            if (!project.equals(resource.getProject())) {
                logger.warn("Unable to refresh resources from multiple projects at the same time. Only refreshing resources from " + project.getName());
                break;
            }
        }

        final RefreshResourceActionController actionController = new RefreshResourceActionController();
        actionController.setProject(project);
        actionController.setSelection(selection);
        actionController.setSelectedResources(filteredResources, false);
        return actionController;
    }

}
