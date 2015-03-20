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
package com.salesforce.ide.core.internal.components.apex.trigger;

import java.lang.reflect.InvocationTargetException;
import java.util.SortedSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.components.ComponentController;
import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.registries.DescribeObjectRegistry;

public class ApexTriggerComponentController extends ComponentController {

    public ApexTriggerComponentController() throws ForceProjectException {
        super(new ApexTriggerModel());
    }

    /**
     * get trigger objects.
     * @throws ForceRemoteException 
     */
    @Override
    public SortedSet<String> getObjectNames(boolean refresh) throws ForceConnectionException, ForceRemoteException {
        IProject project = getComponentWizardModel().getProject();

        if (project == null) {
            return null;
        }

        DescribeObjectRegistry describeObjectRegistry = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getDescribeObjectRegistry();

        return describeObjectRegistry.getCachedTriggerableDescribeTypes(project, refresh);
    }

    @Override
    protected void preSaveProcess(ComponentModel componentWizardModel, IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        // TODO Auto-generated method stub
        
    }
}
