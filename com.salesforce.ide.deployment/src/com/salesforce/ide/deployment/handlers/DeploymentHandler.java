package com.salesforce.ide.deployment.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.deployment.actions.DeploymentActionController;
import com.salesforce.ide.ui.handlers.BaseHandler;
import com.salesforce.ide.ui.internal.utils.UIUtils;

public final class DeploymentHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(DeploymentHandler.class);

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IStructuredSelection selection = getSelection(event);
        if (!selection.isEmpty()) {
            final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
            final DeploymentActionController actionController = buildController(window, selection);
            if (null != actionController) {
                final Shell shell = HandlerUtil.getActiveShellChecked(event);

                try {
                    // instantiates the wizard container with the wizard and opens it
                    WizardDialog dialog = actionController.getWizardDialog();
                    dialog.create();
                    UIUtils.placeDialogInCenter(shell, dialog.getShell());
                    Utils.openDialog(actionController.getProject(), dialog);
                } catch (Exception e) {
                    logger.error("Unable to open deployment wizard", e);
                }
            }
        }

        return null;
    }

    private static DeploymentActionController buildController(final IWorkbenchWindow window, final IStructuredSelection selection) {
        final List<IResource> selectedResources = discardReferencedPackages(getSelectedResources(selection));
        final List<IResource> filteredResources = filter(selectedResources);
        if (filteredResources.isEmpty()) return null;

        final IProject project = filteredResources.get(0).getProject();
        for (final IResource resource : filteredResources) {
            if (!project.equals(resource.getProject())) {
                logger.warn("Unable to refresh resources from multiple projects at the same time. Only refreshing resources from " + project.getName());
                break;
            }
        }

        final DeploymentActionController actionController = new DeploymentActionController();
        actionController.setProject(project);
        actionController.setSelection(selection);
        actionController.setWorkbenchWindow(window);
        actionController.setSelectedResources(filteredResources, false);
        return actionController;
    }

    // if project is a selected resource, remove and replace w/ src/ to narrow
    // deploy considerations removing referenced packages
    private static final List<IResource> discardReferencedPackages(List<IResource> selectedResources) {
        Set<IResource> resourceSet = new HashSet<>();
        resourceSet.addAll(selectedResources);
        for (IResource resource : selectedResources) {
            if (IResource.PROJECT == resource.getType()) {
                resourceSet.remove(resource);
                resourceSet.add(resource.getProject().getFolder(Constants.SOURCE_FOLDER_NAME));
                break;
            }
        }
        return new ArrayList<>(resourceSet);
    }

}
