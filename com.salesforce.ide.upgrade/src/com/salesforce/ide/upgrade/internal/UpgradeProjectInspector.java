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
package com.salesforce.ide.upgrade.internal;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.services.ServiceLocator;
import com.salesforce.ide.upgrade.ForceIdeUpgradePlugin;
import com.salesforce.ide.upgrade.internal.utils.UpgradeMessages;
import com.salesforce.ide.upgrade.project.UpgradeMarkerUtils;
import com.salesforce.ide.upgrade.project.UpgradeNature;

/**
 * Inspects open projects upon IDE initialization checking for out-of-date projects relative to installed IDE version.
 * 
 * @author chris
 */
public class UpgradeProjectInspector extends Job implements IResourceChangeListener {

    private static final Logger logger = Logger.getLogger(UpgradeProjectInspector.class);

    private static final String JOB_NAME = "Project Upgrade Inspector Job";

    private ServiceLocator serviceLocator = null;
    private String installedIdeVersion = null;
    private boolean enabled = true;

    public UpgradeProjectInspector() {
        super(JOB_NAME);
        init();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private void init() {
        serviceLocator = ContainerDelegate.getInstance().getServiceLocator();

        if (serviceLocator != null) {
            installedIdeVersion = serviceLocator.getProjectService().getInstalledIdeVersion();
        }

        // add inspector as listener for new/imported projects
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);

        if (logger.isDebugEnabled()) {
            logger.debug("Add " + getClass().getSimpleName() + " as workspace listener");
        }
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (!enabled) {
            logger.warn("Upgrade inspector disabled");
            return;
        }

        IResourceDelta rootDelta = event.getDelta();

        final Set<IProject> projects = new HashSet<>();
        IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta delta) {
                //only interested in changed resources (not added or removed)
                if (delta.getResource().getType() != IResource.PROJECT
                        && (delta.getKind() != IResourceDelta.ADDED || delta.getKind() != IResourceDelta.CHANGED)) {
                    return true;
                }

                // store project for later inspection
                IProject project = (IProject) delta.getResource();
                try {
                    if (project.isOpen() && project.hasNature(DefaultNature.NATURE_ID)
                            && !project.hasNature(UpgradeNature.NATURE_ID)) {
                        projects.add(project);

                        if (logger.isDebugEnabled()) {
                            logger.debug("Add " + getDeltaString(delta) + " project " + project.getName()
                                    + " to-be evaluated for upgrade");
                        }
                    }
                } catch (CoreException e) {
                    String logMessage = Utils.generateCoreExceptionLog(e);
                    logger.warn("Unable to evaluate project nature: " + logMessage);
                }

                return true;
            }

            protected String getDeltaString(IResourceDelta delta) {
                switch (delta.getKind()) {
                case IResourceDelta.ADDED:
                    return "added";
                case IResourceDelta.CHANGED:
                    return "changed";
                default:
                    return "unknown";
                }
            }
        };

        try {
            rootDelta.accept(visitor);

            if (Utils.isNotEmpty(projects)) {
                WorkspaceJob job = new WorkspaceJob("Setting projects for upgrade") {
                    @Override
                    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                        for (IProject project : projects) {
                            try {
                                inspectProject(project, monitor);
                            } catch (CoreException e) {
                                String logMessage = Utils.generateCoreExceptionLog(e);
                                logger.warn("Unable to evaluate project '" + project.getName()
                                        + "' for upgrade-ability: " + logMessage);
                            }
                        }
                        return Status.OK_STATUS;
                    }
                };
                job.schedule();
            }
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to evaluate projects for upgrade-ability: " + logMessage);
        }
    }

    /**
     * Loops through each open, Force.com project comparing stored IDE version w/ installed IDE version. If the versions
     * mismatch, online nature is removed and an upgrade nature is applied disabling most server-based actions/features.
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        return inspectProjects(monitor);
    }

    public IStatus inspectProjects(final IProgressMonitor monitor) {
        if (!enabled) {
            logger.warn("Upgrade inspector disabled");
            return Status.CANCEL_STATUS;
        }
        serviceLocator = ContainerDelegate.getInstance().getServiceLocator();

        // get list of all workspace projects
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        if (Utils.isEmpty(projects)) {
            return Status.OK_STATUS;
        }

        // inspect each project
        for (IProject project : projects) {
            // skip projects that are not opened and don't have ide and to-be-upgrade natures
            // TODO: handle closed out-of-date projects that are opened post ide startup
            try {
                inspectProject(project, monitor);
            } catch (CoreException e) {
                String logMessage = Utils.generateCoreExceptionLog(e);
                logger.warn("Unable to check for '" + DefaultNature.NATURE_ID + "' nature on project '"
                        + project.getName() + "': " + logMessage);
                continue;
            }
        }

        return Status.OK_STATUS;
    }

    /**
     * Inspect project for upgradeability
     * 
     * @param project
     * @param monitor
     * @throws CoreException
     */
    public void inspectProject(final IProject project, final IProgressMonitor monitor) throws CoreException {
        if (!project.exists() || !project.isOpen() || !project.hasNature(DefaultNature.NATURE_ID)
                || project.hasNature(UpgradeNature.NATURE_ID) && serviceLocator != null) {
            if (logger.isInfoEnabled()) {
                logger
                        .info("Skipping evaluation of project '"
                                + project.getName()
                                + "' for upgradeability - project does not exist and/or not open and/or doesn't have proper nature");
            }
            return;
        }

        // .setting files is essential
        if (project.getFile(Constants.PROJECT_SETTINGS_FILE) == null
                || !project.getFile(Constants.PROJECT_SETTINGS_FILE).exists()) {
            if (logger.isInfoEnabled()) {
                logger.info("Skipping evaluation of project '" + project.getName()
                        + "' for upgradeability - project .settings does not exist");
            }
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Evaluating project '" + project.getName() + "' for upgradeability");
        }

        // get project from params
        String ideVersion = serviceLocator.getProjectService().getIdeVersion(project);

        // compare stored and install versions
        if (Utils.isEqual(ideVersion, installedIdeVersion)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Project deemed up-to-date [" + ideVersion + "] - no upgrade needed");
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Project '" + project.getName()
                        + "' found to be out-of-date with installed IDE - project created with version '" + ideVersion
                        + "', installed version is '" + installedIdeVersion + "'");
            }

            // let the workbench generate events to update all resources affected by a decorator
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void run() {
                    // remove online nature first to disable builder
                    try {
                        UpgradeNature.addNature(project, monitor);
                    } catch (CoreException e) {
                        String logMessage = Utils.generateCoreExceptionLog(e);
                        logger.warn("Unable to adjust natures on project '" + project.getName() + "': " + logMessage);
                    }

                    // apply upgrade required marker
                    UpgradeMarkerUtils.applyUpgradeRequiredMarker(project, UpgradeMessages.getString(
                        "Upgrade.Marker.message",
                        new String[] { serviceLocator.getProjectService().getIdeReleaseName() }));

                    if (logger.isDebugEnabled()) {
                        logger.debug("Updating " + Constants.FORCE_PLUGIN_PREFIX + ".decorator.project.* decorators");
                    }
                    ForceIdeCorePlugin.getDefault().getWorkbench().getDecoratorManager().update(
                        Constants.FORCE_PLUGIN_PREFIX + ".decorator.project");
                    ForceIdeCorePlugin.getDefault().getWorkbench().getDecoratorManager().update(
                        Constants.FORCE_PLUGIN_PREFIX + ".decorator.project.online");

                    UpgradeNotifier upgradeNotifier = ForceIdeUpgradePlugin.getDefault().getUpgradeNotifier();
                    if (upgradeNotifier != null) {
                        upgradeNotifier.removeNotifiedProjectName(project.getName());
                    }
                }
            });
        }
    }

    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }
}
