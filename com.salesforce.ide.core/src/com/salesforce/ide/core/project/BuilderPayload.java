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

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.salesforce.ide.core.factories.FactoryLocator;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceLocator;

/**
 * Encapsulates components to be compiled/saved/deleted
 * 
 * @author cwall
 */
public class BuilderPayload {

    private static final Logger logger = Logger.getLogger(BuilderPayload.class);

    private IProject project = null;
    private ComponentList componentList = null;
    private ProjectPackageList projectPackageList = null;
    private boolean checkForConflicts = true;
    protected ServiceLocator serviceLocator = null;
    protected FactoryLocator factoryLocator = null;
    private ForceProject forceProject = null;
    private boolean conflictFound = false;
    private ComponentList conflictComponentList = null;

    // C O N S T R U C T O R S
    public BuilderPayload() {}

    public BuilderPayload(IProject project) {
        this.project = project;
    }

    public BuilderPayload(IProject project, ComponentList componentList) {
        this.project = project;
        setComponentList(componentList);
    }

    // M E T H O D S
    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public void setServiceLocator(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public FactoryLocator getFactoryLocator() {
        return factoryLocator;
    }

    public void setFactoryLocator(FactoryLocator factoryLocator) {
        this.factoryLocator = factoryLocator;
    }

    public ForceProject getForceProject() {
        return forceProject;
    }

    public void setForceProject(ForceProject forceProject) {
        this.forceProject = forceProject;
    }

    public boolean isConflictFound() {
        return conflictFound;
    }

    public void setConflictFound(boolean conflictFound) {
        this.conflictFound = conflictFound;
    }

    public Component getComponent(String fullName) {
        return projectPackageList.getComponentByFilePath(fullName);
    }

    public Component getComponentByNameType(String name, String componentType) {
        return projectPackageList.getComponentByNameType(name, componentType);
    }

    public Component getApexCodeComponent(String name, String message) {
        if (Utils.isEmpty(name)) {
            logger.warn("Unable to find Apex Code component for name '" + name + "'");
            return null;
        }

        // REVIEWME: attempt to get either class or trigger, but what if the names are the same?
        // another option is to use the ids.
        Component component1 = getComponentByNameType(name, Constants.APEX_CLASS);
        Component component2 = getComponentByNameType(name, Constants.APEX_TRIGGER);
        if (component1 != null && component2 != null && Utils.isNotEmpty(message)) {
            if (message.toLowerCase().contains("trigger")) {
                component1 = component2;
            }
        }

        return (component1 != null ? component1 : component2);
    }

    public void removeComponent(Component component) {
        projectPackageList.removeComponent(component);
    }

    public ComponentList getComponentList() {
        return componentList;
    }

    public void setComponentList(ComponentList componentList) {
        this.componentList = componentList;
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public boolean isEmpty() {
        return projectPackageList == null || projectPackageList.isEmpty();
    }

    public boolean hasSaveableComponents() {
        return projectPackageList.hasComponents(false);
    }

    public boolean isCheckForConflicts() {
        return checkForConflicts;
    }

    public void setCheckForConflicts(boolean checkForConflicts) {
        this.checkForConflicts = checkForConflicts;
    }

    public ComponentList getConflictComponentList() {
        return conflictComponentList;
    }

    public void setConflictComponentList(ComponentList conflictComponentList) {
        this.conflictComponentList = conflictComponentList;
    }

    public boolean addConflictComponentList(Component component) {
        if (conflictComponentList == null) {
            conflictComponentList = factoryLocator.getComponentFactory().getComponentListInstance();
        }
        return conflictComponentList.add(component);
    }

    public ProjectPackageList getLoadedProjectPackageList() {
        return projectPackageList;
    }

    public void setProjectPackageList(ProjectPackageList projectPackageList) {
        this.projectPackageList = projectPackageList;
    }

    public void loadPayload(ComponentList componentList, IProgressMonitor monitor) throws BuilderException {
        if (Utils.isEmpty(componentList)) {
            throw new IllegalArgumentException("Component list cannot be null");
        }

        setComponentList(componentList);
        initProjectPackageList();

        // check for conflicts
        if (checkForConflicts) {
            try {
                filterComponentsInConflict(projectPackageList, monitor);
            } catch (Exception e) {
                logger.error("Unable to perform conflict check", e);
                throw new BuilderException("Unable to perform conflict check", e);
            }
        }
    }

    private void initProjectPackageList() {
        // initialize container for to-be-saved components
        projectPackageList = factoryLocator.getProjectPackageFactory().getProjectPackageListInstance();
        projectPackageList.setProject(project);

        for (Component component : componentList) {
            // ignore install package components
            if (component.isInstalled()) {
                logger.info("Skipping referenced package component, " + component.getFullDisplayName());
                continue;
            }

            projectPackageList.addComponent(component);
        }
    }

    private void filterComponentsInConflict(ProjectPackageList projectPackageList, IProgressMonitor monitor)
            throws ForceConnectionException, InterruptedException, IOException, ForceRemoteException,
            ServiceException {
        Connection connection = null;
        if (project != null) {
            connection = factoryLocator.getConnectionFactory().getConnection(project);
        } else if (forceProject != null) {
            connection = factoryLocator.getConnectionFactory().getConnection(forceProject);
        }

        // go get selective components and create components from remote retrieve
        RetrieveResultExt retrieveResultHandler = serviceLocator.getPackageRetrieveService().retrieveSelective(
        		connection,
        		projectPackageList,
        		true,
                monitor);

        if (retrieveResultHandler == null) {
            String logDisplay = null == connection ? "" : connection.getLogDisplay();
            logger.warn("Unable to perform conflict check - retrieve handler null for " + logDisplay + " and project package " + projectPackageList);
            return;
        }

        // REVIEWME: do we conclude w/o component conflict check?
        if (retrieveResultHandler.hasMessages()) {
            StringBuffer strBuff = new StringBuffer("Retrieve result contains problem messages:");
            String[] messages = retrieveResultHandler.getMessageHandler().getDisplayMessages();
            int msgCnt = 0;
            for (String message : messages) {
                strBuff.append("\n (").append(++msgCnt).append(") ").append(message);
            }
            logger.warn(strBuff.toString());
        }

        if (retrieveResultHandler.getZipFileCount() != projectPackageList.getComponentCount(false)) {
            logger.warn("Remote retrieve result count [" + retrieveResultHandler.getZipFileCount() + "] does not equal request count [" + projectPackageList.getComponentCount(false) + "].");
        }

        ProjectPackageList remoteProjectPackageList = factoryLocator.getProjectPackageFactory().getProjectPackageListInstance(
        		project,
                retrieveResultHandler.getZipFile(),
                retrieveResultHandler.getFileMetadataHandler());

        // for each component in each package in build payload, check for conflict
        for (ProjectPackage projectPackage : projectPackageList) {
            ComponentList componentList = projectPackage.getComponentList();
            if (componentList != null && componentList.isNotEmpty()) {
                ComponentList tmpComponentList = new ComponentList();
                tmpComponentList.addAll(componentList);
                for (Iterator<Component> componentIterator = tmpComponentList.iterator(); componentIterator.hasNext();) {
                    Component component = componentIterator.next();
                    conflictCheck(
                    		component,
                    		componentList,
                    		remoteProjectPackageList,
                    		new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
                }
            }
        }
    }

    private void conflictCheck(
    		Component component,
    		ComponentList componentList,
            ProjectPackageList remoteProjectPackageList,
            IProgressMonitor monitor) {
        // skip package.xml
        if (component.isPackageManifest()) {
            return;
        }

        // find corresponding component in remote project package
        Component remoteComponent = remoteProjectPackageList.getComponentByFilePath(component.getMetadataFilePath());

        if (remoteComponent == null) {
            logger.warn(component.getFullDisplayName()
                + " not found in remote project package list.  Assuming no conflict.");
            return;
        }

        // check for conflict
        boolean hasConflict = false;
        try {
            hasConflict = component.hasRemoteChanged(remoteComponent, monitor);
        } catch (InterruptedException e) {
            // do nothing - thrown if user cancels
        }

        // if conflict found, handle by removing from deploy list and log and add appropriate markers
        if (hasConflict) {
            handleConflict(component, componentList);
        }
    }

    private void handleConflict(Component component, ComponentList componentList) {
        conflictFound = true;
        addConflictComponentList(component);

        // remove component from to-be-deploy list
        boolean remove = false;
        if (componentList.contains(component)) {
            remove = componentList.remove(component);
        }

        // if composite component, remove corresponding composite component from list too
        if (component.isMetadataComposite() && remove) {
            String componentCompositeFilePath = component.getCompositeMetadataFilePath();
            Component componentComposite = componentList.getComponentByFilePath(componentCompositeFilePath);
            if (componentComposite != null) {
                remove = componentList.remove(componentComposite);
            }
        }

        IFile file = component.getFileResource();
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Conflict found while preparing to save '");
        strBuff.append(file != null ? file.getName() : component.getMetadataFilePath());
        strBuff.append("' to server.  Remote instance has been updated ");
        strBuff.append("since last save or sync.  Use the Synchronize Perspective to resolve the conflict.");
        MarkerUtils markerUtils = MarkerUtils.getInstance();
        markerUtils.clearAll(file);
        markerUtils.applyDirty(file);
        markerUtils.applySaveErrorMarker(file, 1, 1, 0, strBuff.toString());
    }

    public IFile[] getFiles() {
        if (Utils.isEmpty(componentList)) {
            return null;
        }

        IFile[] files = new IFile[componentList.size()];
        for (int i = 0; i < files.length; i++) {
            files[i] = componentList.get(i).getFileResource();
        }

        return files;
    }
}
