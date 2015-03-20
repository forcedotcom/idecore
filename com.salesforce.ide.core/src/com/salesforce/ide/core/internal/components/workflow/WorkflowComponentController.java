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
package com.salesforce.ide.core.internal.components.workflow;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.SortedSet;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.components.ComponentController;
import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.remote.registries.DescribeObjectRegistry;

public class WorkflowComponentController extends ComponentController {

    private static final Logger logger = Logger.getLogger(WorkflowComponentController.class);

    public WorkflowComponentController() throws ForceProjectException {
        super(new WorkflowModel());
    }

    /**
     * get workflowable objects.
     * 
     * @throws ForceRemoteException
     */
    @Override
    public SortedSet<String> getObjectNames(boolean refresh) throws ForceConnectionException, ForceRemoteException {
        IProject project = getComponentWizardModel().getProject();

        if (project == null) {
            return null;
        }

        String namespace = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getNamespacePrefix(project);
        DescribeObjectRegistry describeObjectRegistry = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getDescribeObjectRegistry();

        return describeObjectRegistry.getCachedWorkflowableDescribeTypes(project, refresh, namespace);
    }

    @Override
    public void finish(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, IOException {
        if (getComponentWizardModel() == null || getComponentWizardModel().getProject() == null) {
            logger.error("Component model and/or project cannot be null");
            throw new IllegalArgumentException("Component model and/or project cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("***   C R E A T E   C O M P O N E N T   ***");
        }

        monitorWorkCheck(monitor, "Retrieving " + getComponentWizardModel().getDisplayName() + " container...");

        // create and load components from user input including metadata, if applicable
        ComponentList components = null;
        try {
            components = getComponentWizardModel().getLoadedComponents();
        } catch (FactoryException e) {
            logger.error("Unable to get loaded components", e);
            throw new InvocationTargetException(e);
        } catch (JAXBException e) {
            logger.error("Unable to get loaded components", e);
            throw new InvocationTargetException(e);
        }

        if (Utils.isEmpty(components)) {
            logger.error("Unable to create component.");
            Utils.openError(new Exception("Unable to create component.  Component is null."), "Unable to create "
                    + getComponentWizardModel().getDisplayName(), null);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Creating " + components.get(0).getFullDisplayName());
        }

        // prepare container to perform save and deploy ops
        ProjectPackageList projectPackageList = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageListInstance();
        projectPackageList.setProject(getComponentWizardModel().getProject());
        projectPackageList.addComponents(components, false);

        // save to f/s - enables offline work
        monitorSubTask(monitor, "Saving " + getComponentWizardModel().getDisplayName() + " to project...");
        projectPackageList.saveResources(new String[] { getComponentWizardModel().getComponent().getComponentType() },
            monitor);
        monitorWorkCheck(monitor);

        // retrieve from server
        RetrieveResultExt retrieveResultExt = null;
        try {
            retrieveResultExt =
                    ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrieveSelective(projectPackageList, true, monitor);
        } catch (Exception e) {
            logger.error("Unable to retrieve " + getComponentWizardModel().getDisplayName(), e);
            throw new InvocationTargetException(e);
        } finally {
            monitorWorkCheck(monitor);
        }

        // save to project
        monitorSubTask(monitor, "Saving " + getComponentWizardModel().getDisplayName() + " container to project...");
        try {
            ContainerDelegate.getInstance().getServiceLocator().getProjectService().handleRetrieveResult(retrieveResultExt, true, monitor);
            monitorWorkCheck(monitor, "Saving " + getComponentWizardModel().getDisplayName()
                    + " container to project...");
        } catch (Exception e) {
            logger.error("Unable to save " + getComponentWizardModel().getDisplayName(), e);
            throw new InvocationTargetException(e);
        } finally {
            monitorWork(monitor);
        }

        List<IResource> resources = retrieveResultExt.getProjectPackageList().getAllComponentResources(false);
        if (Utils.isNotEmpty(resources)) {
            getComponent().setFileResource((IFile) resources.get(0));
        }
    }

    @Override
    protected void preSaveProcess(ComponentModel componentWizardModel, IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        // TODO Auto-generated method stub
        
    }
}
