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
import com.salesforce.ide.core.internal.utils.QualifiedNames;
import com.salesforce.ide.core.project.BuilderController.DeltaComponentSynchronizer;

public class OnlineBuilder extends BaseBuilder {
    static final Logger logger = Logger.getLogger(OnlineBuilder.class);

    public static final String BUILDER_ID = Constants.FORCE_PLUGIN_PREFIX + ".builder.online";

    //   C O N S T R U C T O R
    public OnlineBuilder() {
        super();
    }

    //   M E T H O D S
    @Override
    protected IProject[] build(int kind, Map<String,String> args, IProgressMonitor monitor) throws CoreException {
        if (logger.isDebugEnabled()) {
            logger.debug("");
            logger.debug("***   B U I L D E R   ( s t a r t )  ***");
        }

        // after a project is created, auto-build notices a resource change and kicks-off a full build.
        // we want to postpone the first full build until the user explicitly asks for a full build
        if (checkSkipBuilder()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Discontinue online builder flag found.  Skipping build.");
            }
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Getting project deltas");
        }

        IResourceDelta delta = getDelta(getProject());
        if (delta != null) {
            incrementalBuild(delta, monitor);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Online builder discontinued - not resource deltas found");
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("***   B U I L D E R   ( e n d )  ***");
        }

        return null;
    }

    protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
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

        try {
            builderController.build(componentDeltas.getSaveComponentList(), getProject(), monitor);
            getProject().setSessionProperty(QualifiedNames.QN_SKIP_BUILDER, false);
        } catch (Exception e) {
            logger.error("Unable to build project", e);
        }
    }
}
