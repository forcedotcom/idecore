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
package com.salesforce.ide.core.internal.controller;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.model.IModel;
import com.salesforce.ide.core.project.ForceProjectException;

public abstract class Controller {

    private static final Logger logger = Logger.getLogger(Controller.class);

    protected IModel model;
    private boolean complete;
    
    public IModel getModel() {
        return model;
    }

    public void setModel(IModel model) {
        this.model = model;
    }

    public IProject getProject() {
        if (model == null) {
            return null;
        }
        return model.getProject();
    }

    public void setProject(IProject project) {
        if (model != null) {
            model.setProject(project);
        }
    }

    public boolean canComplete() {
        return complete;
    }

    public void setCanComplete(boolean complete) {
        this.complete = complete;
    }

    //   L I F E C Y C L E   M E T H O D S
    /**
     * Implement to perform functions post class creation.
     * 
     * @throws ForceProjectException
     */
    public abstract void init() throws ForceProjectException;

    /**
     * Encapsulates concluding functionality when the wizard is finished.
     * 
     * @throws Exception
     */
    public abstract void finish(IProgressMonitor monitor) throws Exception;

    public abstract void dispose();

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
