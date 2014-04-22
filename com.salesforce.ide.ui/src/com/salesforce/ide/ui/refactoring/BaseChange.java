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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.services.ProjectService;

public abstract class BaseChange extends Change {

    private static final Logger logger = Logger.getLogger(BaseChange.class);

    protected BaseRefactorController refactorController = null;
    protected boolean success = true;

    //   C O N S T R U C T O R
    public BaseChange() {
        super();
    }

    //   M E T H O D S
    public BaseRefactorController getRefactorController() {
        return refactorController;
    }

    public void setRefactorController(BaseRefactorController refactorController) {
        this.refactorController = refactorController;
    }

    protected ProjectService getProjectService() {
        return ContainerDelegate.getInstance().getServiceLocator().getProjectService();
    }

    protected ComponentFactory getComponentFactory() {
        return ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

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
}
