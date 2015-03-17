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

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.metadata.IDeployResultExt;
import com.salesforce.ide.ui.internal.utils.UIConstants;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.views.runtest.RunTestView;

/**
 *
 * 
 * @author cwall
 */
public class RunTestsAction extends BaseAction {
    private static final Logger logger = Logger.getLogger(RunTestsAction.class);

    protected RunTestView runTestView = null;

    public RunTestsAction() {
        super();
    }

    public RunTestView getRunTestView() {
        return runTestView;
    }

    @Override
    public void init() {
        // not implemeneted
    }

    @Override
    public void execute(IAction action) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        try {
            runTestView = (RunTestView) window.getActivePage().showView(UIConstants.RUN_TEST_VIEW_ID);
        } catch (Exception e1) {
            Utils.openError(new InvocationTargetException(e1), true, UIMessages
                .getString("RunTestsAction.CannotOpenRunTestsView.error"));
        }

        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        try {
            service.run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    if (monitor != null) {
                        monitor.beginTask("Run Test for '" + getSelectedResource().getName() + "'", 2);
                    }
                    try {
                        monitorSubTask(monitor, "Running testing on server...");
                        IDeployResultExt results =
                                serviceLocator.getRunTestsService().runTests(getSelectedResource(), monitor);
                        monitorWork(monitor);
                        monitorSubTask(monitor, "Evaluating test results...");
                        handleResults(results);
                        monitorWork(monitor);
                    } catch (InterruptedException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        if (monitor != null) {
                            monitor.done();
                        }
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
                strBuff.append("Unable to run tests on '" + getSelectedResource().getName() + "':\n\n").append(
                    ForceExceptionUtils.getStrippedRootCauseMessage(e)).append("\n\n ");
                Utils.openError("Run Test Error", strBuff.toString());
            }
        }
    }

    protected void handleResults(IDeployResultExt result) {
        if (result == null) {
            logger.error("Unable to handle run tests results - results are null");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(result.getDebugLog());
        }

        runTestView.setProject(project);
        runTestView.processRunTestResults(result, getSelectedResource());
    }
}
