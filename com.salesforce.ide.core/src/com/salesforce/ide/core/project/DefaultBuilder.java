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

import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.project.BuilderController.DeltaComponentSynchronizer;

public class DefaultBuilder extends BaseBuilder {
    private static final Logger logger = Logger.getLogger(DefaultBuilder.class);

    public static final String BUILDER_ID = Constants.FORCE_PLUGIN_PREFIX + ".builder.default";

    //   C O N S T R U C T O R
    public DefaultBuilder() {}

    //  M E T H O D S
    @Override
    protected IProject[] build(int kind, Map<String,String> args, IProgressMonitor monitor) throws CoreException {
        if (logger.isDebugEnabled()) {
            logger.debug("Default build kick-off");
        }

        if (getProject() == null) {
            logger.warn("Unable to execute build - project is null");
            return null;
        }

        if (!hasOnlyDefaultNature(getProject())) {
            if (logger.isInfoEnabled()) {
                logger.info("Project '" + getProject().getName()
                    + "' has additional Force.com natures - skipping default build");
            }
            return null;
        }

        // TODO: After a project is created, auto-build notices a resource change and kicks-off a full build.
        // we want to postpone the first full build until the user explicitly asks for a full build
        if (checkSkipBuilder()) {
            if (logger.isInfoEnabled()) {
                logger.info("Discontinue default builder flag found.  Skipping build.");
            }
            return null;
        }

        IResourceDelta delta = getDelta(getProject());
        if (delta != null) {
            applyDirtyMarkers(delta, monitor);
        }
        return null;
    }

    // TODO: apply "Save locally only..." markers on each resource
    private void applyDirtyMarkers(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
        if (logger.isDebugEnabled()) {
            logger.debug("Incremental online build kicked-off");
        }

        DeltaComponentSynchronizer componentDeltas = builderController.getDeltaSynchronizer();
        delta.accept(componentDeltas);

        if (componentDeltas.isSaveEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("No delta resources found to build for project '" + getProject().getName() + "'");
            }
            return;
        }

        ComponentList componentList = componentDeltas.getSaveComponentList();
        for (Component component : componentList) {
            if (component.getFileResource() != null && component.getFileResource().exists()) {
                MarkerUtils.getInstance().applyDirty(component.getFileResource());
            }
        }

        logger.warn("TODO: apply 'Save locally only...' markers on each resource");
    }

    private static boolean hasOnlyDefaultNature(IProject project) {
        try {
            String[] natureIds = project.getDescription().getNatureIds();
            for (String natureId : natureIds) {
                if (natureId.equals(DefaultNature.NATURE_ID)) {
                    continue;
                } else if (natureId.startsWith(Constants.FORCE_PLUGIN_PREFIX)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found nature '" + natureId + "'");
                    }
                    return false;
                }
            }
        } catch (CoreException e) {
            return false;
        }

        return true;
    }
}
