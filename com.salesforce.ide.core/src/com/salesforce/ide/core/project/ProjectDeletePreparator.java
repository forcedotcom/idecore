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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.services.ServiceLocator;

/**
 * Listens for and act upon Force.com project deletes preparing project for cleanup.
 * 
 * @author cwall
 * 
 */
public class ProjectDeletePreparator implements IResourceChangeListener {

    private static final Logger logger = Logger.getLogger(ProjectDeletePreparator.class);

    private ServiceLocator serviceLocator = null;

    public ProjectDeletePreparator() {
        init();
    }

    private void init() {

        serviceLocator = ContainerDelegate.getInstance().getServiceLocator();

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);

        if (logger.isDebugEnabled()) {
            logger.debug("Added " + getClass().getSimpleName() + " as workspace listener");
        }
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        // pre delete operations
        if (event.getType() == IResourceChangeEvent.PRE_DELETE && event.getResource() != null
                && event.getResource().getType() == IResource.PROJECT) {
            final IProject project = (IProject) event.getResource();
            try {
                if (project.isOpen() && project.hasNature(DefaultNature.NATURE_ID)) {
                    preProjectDelete(project, new NullProgressMonitor());
                }
            } catch (CoreException e) {
                String logMessage = Utils.generateCoreExceptionLog(e);
                logger.warn("Unable to perform pre-delete operations: " + logMessage);
            }
        } else if (event.getType() == IResourceChangeEvent.POST_CHANGE && event.getDelta() != null) {
            // handle post-delete operations
            final Set<IProject> deletedProjects = new HashSet<>();
            IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
                @Override
                public boolean visit(IResourceDelta delta) {
                    if (delta.getKind() == IResourceDelta.REMOVED && delta.getResource() != null
                            && delta.getResource().getType() == IResource.PROJECT) {
                        deletedProjects.add((IProject) delta.getResource());
                    }
                    return true;
                }
            };

            try {
                IResourceDelta rootDelta = event.getDelta();
                rootDelta.accept(visitor);

                if (Utils.isNotEmpty(deletedProjects)) {
                    for (IProject deletedProject : deletedProjects) {
                        postProjectDelete(deletedProject);
                    }
                }
            } catch (CoreException e) {
                String logMessage = Utils.generateCoreExceptionLog(e);
                logger.warn("Unable to perform post-delete operations on delete projects: " + logMessage);
            }
        }
    }

    // note: many operations may not be performed due to tree locking
    protected void preProjectDelete(final IProject project, IProgressMonitor monitor) {
        // set reference package content to writable so that no delete warnings appear
        IFolder referencedPackageFolder = serviceLocator.getProjectService().getReferencedPackagesFolder(project);
        if (referencedPackageFolder != null && referencedPackageFolder.exists()) {
            Utils.adjustResourceReadOnly(referencedPackageFolder, false, true);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Prepared project '" + project.getName() + "' for deletion");
        }
    }

    protected void postProjectDelete(final IProject project) throws CoreException {
        if (project != null && project.exists()) {
            // if project still exists, remove all force.com natures
            IProjectDescription description = project.getDescription();
            String[] natureIds = description.getNatureIds();
            if (Utils.isNotEmpty(natureIds)) {
                List<String> newNatures = new ArrayList<>();
                for (String natureId : natureIds) {
                    if (!natureId.startsWith(Constants.FORCE_PLUGIN_PREFIX)) {
                        newNatures.add(natureId);
                    }
                }

                if (Utils.isNotEmpty(newNatures)) {
                    description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
                    project.setDescription(description, null);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Performed post-delete operations on project '" + project.getName() + "'");
            }
        }
    }

    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }
}
