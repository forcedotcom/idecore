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
package com.salesforce.ide.core.project;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.services.ServiceLocator;

/**
 * Listens for added projects, specifically imported Force.com projects, to apply builder skip flag.
 * 
 * @author cwall
 * 
 */
public class ProjectAddEvaluator implements IResourceChangeListener {

    private static final Logger logger = Logger.getLogger(ProjectAddEvaluator.class);

    private ServiceLocator serviceLocator = null;

    public ProjectAddEvaluator() {
        init();
    }

    private void init() {
        serviceLocator = ContainerDelegate.getInstance().getServiceLocator();

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);

        if (logger.isDebugEnabled()) {
            logger.debug("Added " + getClass().getSimpleName() + " as workspace listener");
        }
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        final Set<IProject> projects = new HashSet<>();
        IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta delta) {
                try {
                    //only interested in added force.com projects
                    if (delta.getResource().getType() == IResource.PROJECT && delta.getKind() == IResourceDelta.ADDED
                            && ((IProject) delta.getResource()).hasNature(DefaultNature.NATURE_ID)
                            && serviceLocator != null) {
                        IProject project = (IProject) delta.getResource();

                        serviceLocator.getProjectService().flagSkipBuilder(project);

                        if (logger.isDebugEnabled()) {
                            logger.debug("Added skip builder flag to added project '" + project.getName() + "'");
                        }

                        // add project for further eval
                        projects.add(project);

                    }
                } catch (CoreException e) {
                    String logMessage = Utils.generateCoreExceptionLog(e);
                    logger.warn("Unable to add skip builder flag to added project '" + delta.getResource() + "': "
                            + logMessage);
                }

                //only interested in add projects
                if (delta.getResource().getType() == IResource.PROJECT && delta.getKind() == IResourceDelta.ADDED
                        && serviceLocator != null) {
                    try {
                        serviceLocator.getProjectService().flagSkipBuilder((IProject) delta.getResource());

                        if (logger.isDebugEnabled()) {
                            logger.debug("Added skip builder flag to added project '" + delta.getResource() + "'");
                        }
                    } catch (CoreException e) {
                        String logMessage = Utils.generateCoreExceptionLog(e);
                        logger.warn("Unable to add skip builder flag to added project '" + delta.getResource() + "': "
                                + logMessage);
                    }
                }

                return true;
            }
        };

        try {
            event.getDelta().accept(visitor);

            if (Utils.isNotEmpty(projects)) {
                WorkspaceJob job = new WorkspaceJob("Applying updates to newly added Force.com projects") {
                    @Override
                    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                        for (IProject project : projects) {
                            if (!serviceLocator.getProjectService().hasCredentials(project)) {
                                OnlineNature.removeNature(project, new NullProgressMonitor());
                                logger.warn("No credentials found for project '" + project.getName()
                                        + "' - removing online nature");
                            }

                        }
                        return Status.OK_STATUS;
                    }
                };
                job.schedule();
            }

        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to evaluated added project(s) for builder skip: " + logMessage);
        }
    }

    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }
}
