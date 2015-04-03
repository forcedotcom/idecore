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
package com.salesforce.ide.core.internal.components.object;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.components.ComponentController;
import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.jobs.LoadSObjectsJob;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;

public class CustomObjectComponentController extends ComponentController {

    private static final Logger logger = Logger.getLogger(CustomObjectComponentController.class);

    public CustomObjectComponentController() throws ForceProjectException {
        super(new CustomObjectModel());
    }

    /**
    * refresh custom object cache
    */
    @Override
    protected void postSaveProcess(ComponentModel componentWizardModel, IProgressMonitor monitor) {
        if (componentWizardModel.getProject() == null || !componentWizardModel.getProject().exists()) {
            logger.warn("Unable to perform post finish jobs - project is null or does not exist");
            return;
        }

        try {
            Connection connection = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(componentWizardModel.getProject());
            LoadSObjectsJob loadSObjectsJob =
                    new LoadSObjectsJob(ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getDescribeObjectRegistry(), connection,
                            componentWizardModel.getProject().getName());
            loadSObjectsJob.setSystem(true);
            loadSObjectsJob.schedule();
        } catch (Exception e) {
            // this is fine because we may load later
            logger.warn("Unable to refresh custom object cache: " + e.getMessage());
        }

        super.postSaveProcess(componentWizardModel, monitor);
    }

    @Override
    protected void preSaveProcess(ComponentModel componentWizardModel, IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        // TODO Auto-generated method stub
        
    }
}
