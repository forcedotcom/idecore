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
package com.salesforce.ide.upgrade.project;

import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.project.BaseBuilder;
import com.salesforce.ide.core.project.BuilderController.DeltaComponentSynchronizer;

public class UpgradeBuilder extends BaseBuilder {
    private static final Logger logger = Logger.getLogger(UpgradeBuilder.class);

    public static final String BUILDER_ID = Constants.FORCE_PLUGIN_PREFIX + ".builder.upgrade";

    //   C O N S T R U C T O R
    public UpgradeBuilder() {}

    //  M E T H O D S
    @Override
    protected IProject[] build(int kind, Map<String,String> args, IProgressMonitor monitor) throws CoreException {
        if (logger.isDebugEnabled()) {
            logger.debug("Upgrade build kick-off");
        }

        if (getProject() == null) {
            logger.warn("Unable to execute build - project is null");
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
                UpgradeMarkerUtils.applyDirty(component.getFileResource());
            }
        }
    }
}
