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
package com.salesforce.ide.core.internal.jobs;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.registries.DescribeObjectRegistry;

public class LoadSObjectsJob extends Job {

    private static final Logger logger = Logger.getLogger(LoadSObjectsJob.class);

    private static final String JOB_NAME = "Fetch and Cache SObjects";

    private DescribeObjectRegistry describeObjectRegistry = null;
    private Connection connection = null;
    private String projectName = null;

    public LoadSObjectsJob(DescribeObjectRegistry describeObjectRegistry, Connection connection, String projectName) {
        super(JOB_NAME);
        this.connection = connection;
        this.describeObjectRegistry = describeObjectRegistry;
        this.projectName = projectName;
        init();
    }

    private void init() {
        this.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                if (!event.getResult().isOK()) {
                    logger.error("Unable to pre-fetch sobjects");
                }
            }
        });
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        if (describeObjectRegistry == null || connection == null || Utils.isEmpty(projectName)) {
            logger.warn("Unable to pre-fetch sobjects - describe registry, connection, and/or project name is null");
            cancel();
            return Status.CANCEL_STATUS;
        }

        try {
            if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            if (logger.isDebugEnabled()) {
                ForceIdeCorePlugin.getStopWatch().start("LoadSObjectsJob.run");
            }

            describeObjectRegistry.getCachedDescribeSObjects(connection, projectName, true);

        } catch (Exception e) {
            logger.warn("Unable to pre-fetch sobjects", ForceExceptionUtils.getRootCause(e));
            cancel();
            return Status.CANCEL_STATUS;
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("Pre-fetched and cached describe sobjects");
                ForceIdeCorePlugin.getStopWatch().stop("LoadSObjectsJob.run");
            }
        }

        return Status.OK_STATUS;
    }
}
