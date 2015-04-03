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
package com.salesforce.ide.core.internal.components;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.project.MarkerUtils;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceTimeoutException;

/**
 * Same as the ComponentController except that it waits for all related classes to be created before it sends to the
 * server
 * 
 * @author kevin.ren
 * 
 */

public class MultiClassComponentController extends ComponentController {

    private Boolean shouldSaveToServer = false;
    private static final Logger logger = Logger.getLogger(MultiClassComponentController.class);
    //used for storing all the classes that needs to be sent to the server
    private ProjectPackageList allPackages;
    
    public MultiClassComponentController(ComponentModel componentWizardModel, ProjectPackageList allPackages)
            throws ForceProjectException {
        super(componentWizardModel);
        this.allPackages = allPackages;
    }

    public Boolean getShouldSaveToServer() {
        return shouldSaveToServer;
    }

    public void setShouldSaveToServer(Boolean b) {
        this.shouldSaveToServer = b;
    }

    @Override
    public void finish(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, IOException,
            ForceConnectionException, ForceRemoteException, FactoryException, CoreException, ServiceException,
            JAXBException, Exception {
        if (getComponentWizardModel() == null || getComponentWizardModel().getProject() == null) {
            logger.error("Component model and/or project cannot be null");
            throw new IllegalArgumentException("Component model and/or project cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("***   C R E A T E   C O M P O N E N T   ***");
        }

        monitorWorkCheck(monitor, "Creating new " + getComponentWizardModel().getDisplayName() + "...");

        // create and load components from user input including metadata, if applicable
        ComponentList components = getComponentWizardModel().getLoadedComponents();

        if (Utils.isEmpty(components)) {
            logger.error("Unable to create component.");
            Utils.openError(new Exception("Unable to create component.  Component is null."),
                "Unable to create component", null);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Creating " + components.get(0).getFullDisplayName());
        }

        // prepare container to perform save and deploy ops
        ProjectPackageList projectPackageList =
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageListInstance();
        projectPackageList.setProject(getComponentWizardModel().getProject());
        projectPackageList.addComponents(components, false);
        allPackages.setProject(getComponentWizardModel().getProject());
        allPackages.addComponents(components, false);

        // perform pre-save actions
        defaultPreSaveProcess(getComponentWizardModel(), monitor);
        preSaveProcess(getComponentWizardModel(), monitor);

        // save to f/s - enables offline work
        monitorSubTask(monitor, "Saving " + getComponentWizardModel().getDisplayName() + " to project...");
        projectPackageList.saveResources(new String[] { getComponentWizardModel().getComponent().getComponentType() },
            monitor);
        monitorWorkCheck(monitor);

        // commit to server
        monitorSubTask(monitor, "Saving " + getComponentWizardModel().getDisplayName() + " to server...");
        deploy(projectPackageList, monitor);

        // performing post-save actions
        postSaveProcess(getComponentWizardModel(), monitor);
    }

    @Override
    protected void deploy(ProjectPackageList projectPackageList, IProgressMonitor monitor)
            throws ForceConnectionException, FactoryException, InterruptedException, CoreException, IOException,
            ServiceException, ForceRemoteException, InvocationTargetException, Exception {

        if (!shouldSaveToServer || !isProjectOnlineEnabled()) { //want to go in here if shouldSaveToServer == false and project online is not enabled
            logger.warn("Remote save aborted - project is not online enabled");
            // TODO: apply dirty marker to file?
            if (!isProjectOnlineEnabled()) //if project is not online, don't even want to save it in allPackages
            {
                allPackages.clear();
            }
            ComponentList components = projectPackageList.getAllComponents();
            for (Component component : components) {
                if (!component.isPackageManifest()) {
                    MarkerUtils.getInstance().applyDirty(component.getFileResource(),
                        Messages.getString("Markers.OfflineOnlySavedLocally.message"));
                }
            }
            return;
        }

        // compile and save new component
        try {
            deployResultHandler =
                    ContainerDelegate.getInstance().getServiceLocator().getPackageDeployService()
                            .deploy(allPackages, monitor);
        } catch (ServiceTimeoutException ex) {
            deployResultHandler =
                    ContainerDelegate
                            .getInstance()
                            .getServiceLocator()
                            .getPackageDeployService()
                            .handleDeployServiceTimeoutException(ex, "create new " + getComponent().getDisplayName(),
                                monitor);
        }

        if (deployResultHandler == null) {
            logger.error("Unable to finalize new component creation - deploy result is null");
            allPackages.clear(); //there was an error, so we should clear the packages that caused the error
            allPackages =
                    ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                            .getProjectPackageListInstance();
            return;
        }

        ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                .handleDeployResult(allPackages, deployResultHandler, true, monitor);
        allPackages.clear(); //all packages got sent, so clear the package list
        allPackages =
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageListInstance(); //reinitialize the packagelist
    }

    @Override
    protected void preSaveProcess(ComponentModel componentWizardModel, IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        // TODO Auto-generated method stub
        
    }

}
