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
package com.salesforce.ide.ui.internal;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.salesforce.ide.ui.wizards.project.ProjectCreateOperation;

public abstract class BaseWorkspaceModifyOperation extends WorkspaceModifyOperation {

    private static final Logger logger = Logger.getLogger(ProjectCreateOperation.class);

    protected void monitorCheckSubTask(IProgressMonitor monitor, String subtask) throws InterruptedException {
        monitorCheck(monitor);
        monitorSubTask(monitor, subtask);
    }

    protected void monitorWorkCheck(IProgressMonitor monitor, String subtask) throws InterruptedException {
        monitorCheck(monitor);
        monitorWork(monitor, subtask);
    }

    protected void monitorWorkCheck(IProgressMonitor monitor) throws InterruptedException {
        monitorCheck(monitor);
        monitorWork(monitor);
    }

    protected void monitorCheck(IProgressMonitor monitor) throws InterruptedException {
        if (monitor != null) {
            if (monitor.isCanceled()) {
                throw new InterruptedException("Operation cancelled");
            }
        }
    }

    protected void monitorWork(IProgressMonitor monitor, String subtask) {
        if (monitor == null) {
            return;
        }

        monitor.subTask(subtask);
        monitor.worked(1);
        if (logger.isDebugEnabled()) {
            logger.debug(subtask);
        }
    }

    protected void monitorSubTask(IProgressMonitor monitor, String subtask) {
        if (monitor == null) {
            return;
        }

        monitor.subTask(subtask);
        if (logger.isDebugEnabled()) {
            logger.debug(subtask);
        }
    }

    protected void monitorWork(IProgressMonitor monitor) {
        if (monitor == null) {
            return;
        }

        monitor.worked(1);
    }

    protected Shell getShell() {
        return Display.getDefault().getActiveShell();
    }
}
