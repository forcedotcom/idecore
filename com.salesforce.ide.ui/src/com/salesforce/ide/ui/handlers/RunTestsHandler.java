package com.salesforce.ide.ui.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.metadata.IDeployResultExt;
import com.salesforce.ide.ui.internal.utils.UIConstants;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.views.runtest.RunTestView;

public final class RunTestsHandler extends BaseHandler {
    private static final Logger logger = Logger.getLogger(RunTestsHandler.class);

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        final IStructuredSelection selection = getSelection(event);

        final List<IResource> filteredResources = getFilteredResources(selection);
        if (filteredResources.isEmpty()) return null;

        final IResource firstResource = filteredResources.get(0);

        final IProject project = firstResource.getProject();
        for (final IResource resource : filteredResources) {
            if (!project.equals(resource.getProject())) {
                logger.warn("Unable to execute tests on multiple projects at the same time. Only executing tests related to " + project.getName());
                break;
            }
        }

        final IWorkbench workbench = window.getWorkbench();
        final IWorkbenchPage page = window.getActivePage();
        if (null == page) {
            Utils.openError(Utils.DIALOG_TITLE_ERROR, UIMessages.getString("RunTestsHandler.CannotOpenRunTestsView.error"));
            return null;
        }

        try {
            final RunTestView runTestView = (RunTestView) page.showView(UIConstants.RUN_TEST_VIEW_ID);

            final IProgressService service = workbench.getProgressService();
            try {
                service.run(false, false, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        if (monitor == null) {
                            monitor = new NullProgressMonitor();
                        }
                        monitor.beginTask("Run Test for '" + firstResource.getName() + "'", 2);

                        try {
                            monitor.subTask("Running testing on server...");
                            IDeployResultExt results = getRunTestsService().runTests(firstResource, monitor);
                            monitor.worked(1);

                            monitor.subTask("Evaluating test results...");
                            if (results == null) {
                                logger.error("Unable to handle run tests results - results are null");
                                return;
                            }

                            if (logger.isDebugEnabled()) {
                                logger.debug(results.getDebugLog());
                            }

                            runTestView.setProject(project);
                            runTestView.processRunTestResults(results, firstResource);
                            monitor.worked(1);
                        } catch (InterruptedException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new InvocationTargetException(e);
                        } finally {
                            monitor.done();
                        }
                    }
                });
            } catch (InterruptedException e) {
                logger.warn("Operation canceled: " + e.getMessage());
            } catch (InvocationTargetException e) {
                Throwable cause = e.getTargetException();
                if (cause instanceof InsufficientPermissionsException) {
                    DialogUtils.getInstance()
                            .presentInsufficientPermissionsDialog((InsufficientPermissionsException) cause);
                } else {
                    logger.error("Unable to run tests", ForceExceptionUtils.getRootCause(cause));
                    StringBuffer strBuff = new StringBuffer();
                    strBuff.append("Unable to run tests on '" + firstResource.getName() + "':\n\n").append(
                        ForceExceptionUtils.getStrippedRootCauseMessage(e)).append("\n\n ");
                    Utils.openError("Run Test Error", strBuff.toString());
                }
            }
        } catch (final PartInitException e) {
            Utils.openError(new InvocationTargetException(e), true, UIMessages.getString("RunTestsHandler.CannotOpenRunTestsView.error"));
        }

        return null;
    }

}
