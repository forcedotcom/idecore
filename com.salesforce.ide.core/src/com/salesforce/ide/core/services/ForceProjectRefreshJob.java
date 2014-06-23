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
package com.salesforce.ide.core.services;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.model.ProjectPackageList;

/**
 * Run some of the component generation in its own job to prevent blocking the UI.
 * 
 * @author nchen
 * 
 */
public abstract class ForceProjectRefreshJob extends WorkspaceJob {

    protected ProjectPackageList packageList;

    public ForceProjectRefreshJob(ProjectPackageList packageList) {
        super(Messages.getString("Components.Saving.Background"));
        this.packageList = packageList;
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
        try {
            doSaveResources(monitor);
        } catch (Exception e) {
            return new Status(IStatus.ERROR, Constants.FORCE_PLUGIN_PREFIX, 0, e.getMessage(), e);
        }
        return new Status(IStatus.OK, Constants.FORCE_PLUGIN_PREFIX, "");
    }

    protected abstract void doSaveResources(IProgressMonitor monitor) throws InterruptedException;

    public static class ForceProjectRefreshProject extends ForceProjectRefreshJob {

        private IProject project;

        public ForceProjectRefreshProject(ProjectPackageList packageList, IProject project) {
            super(packageList);
            this.project = project;
        }

        @Override
        protected void doSaveResources(IProgressMonitor monitor) throws InterruptedException {
            packageList.saveResources(project, monitor);
        }

    }

    public static class ForceProjectRefreshResources extends ForceProjectRefreshJob {

        private String[] componentNames;

        public ForceProjectRefreshResources(ProjectPackageList packageList, String[] componentNames) {
            super(packageList);
            this.componentNames = componentNames;
        }

        @Override
        protected void doSaveResources(IProgressMonitor monitor) throws InterruptedException {
            packageList.saveResources(componentNames, monitor);
        }

    }
}
