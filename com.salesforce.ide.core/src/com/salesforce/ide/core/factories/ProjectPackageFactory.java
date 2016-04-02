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
package com.salesforce.ide.core.factories;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.SoqlEnum;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.QueryResult;

/**
 * Encapsulates functionality related to managing project packages.
 *
 * @author cwall
 */
public class ProjectPackageFactory extends BaseFactory {
    private static final Logger logger = Logger.getLogger(ProjectPackageFactory.class);

    public ProjectPackageFactory() {}

    // lookup method injection by container
    @Override
    public ProjectPackageList getProjectPackageListInstance() {
        return new ProjectPackageList();
    }

    public ProjectPackageList getProjectPackageListInstance(String[] packageNames) {
        ProjectPackageList projectPackageList = getProjectPackageListInstance();
        if (Utils.isNotEmpty(packageNames)) {
            projectPackageList.addAll(packageNames);
        } else {
            projectPackageList.addAll(new String[] { Constants.DEFAULT_PACKAGED_NAME });
        }
        return projectPackageList;
    }

    // lookup method injection by container
    public ProjectPackage getProjectPackageInstance() {
        return new ProjectPackage();
    }

    public ProjectPackageList getProjectPackageListInstance(IProject project) throws FactoryException {
        ProjectPackageList projectPackageList = getProjectPackageListInstance();
        projectPackageList.setProject(project);
        ProjectPackage projectPackage = getProjectPackageFactory().getProjectPackage(project);
        projectPackageList.add(projectPackage);
        return projectPackageList;
    }

    public ProjectPackageList getProjectPackageListInstance(IProject project, byte[] zipFile,
            FileMetadataExt fileMetadataHandler) throws InterruptedException, IOException {
        if (fileMetadataHandler == null) {
            throw new IllegalArgumentException("Project and/or FileMetadataExt cannot be null");
        }

        ProjectPackageList projectPackageList = getProjectPackageListInstance();
        projectPackageList.setProject(project);
        projectPackageList.generateComponents(zipFile, fileMetadataHandler);
        return projectPackageList;
    }

    public ProjectPackage getProjectPackage(IProject project) throws FactoryException {
        return getProjectPackage(project, true);
    }

    public ProjectPackage getProjectPackage(IProject project, boolean addManifest) throws FactoryException {
        ProjectPackage projectPackage = getProjectPackageInstance();

        if (project == null) {
            logger.warn("Project not provided, creating '" + Constants.DEFAULT_PACKAGED_NAME
                    + "' project package instance without resource reference");
            projectPackage.setName(Constants.DEFAULT_PACKAGED_NAME);
            return projectPackage;
        }

        String packageName = serviceLocator.getProjectService().getPackageName(project);
        projectPackage.setName(packageName);

        IFolder sourceFolder = getProjectService().getSourceFolder(project);
        if (sourceFolder == null || !sourceFolder.exists()) {
            logger.warn("Source folder found in project '" + project.getName() + "'");
            return projectPackage;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found project package '" + projectPackage.getName() + "' in project '" + project.getName()
                    + "'");
        }

        projectPackage.setPackageRootResource(sourceFolder);
        if (addManifest) {
            Component packageManifest = getPackageManifestFactory().getPackageManifestComponent(project);
            if (packageManifest != null) {
                projectPackage.setPackageManifest(packageManifest);
                projectPackage.addComponent(packageManifest);
            } else {
                logger.error("Package manifest for package '" + packageName + "' is null");
            }
        }

        return projectPackage;
    }

    /**
     * Both development pkgs (pkgs u own) and unmanged installed pkg (pkg u installed that redeem as subscriber created
     * natively in the org) are editable to user, So they should be available to create project against.
     */
    public ProjectPackageList getDevelopmentAndUnmanagedInstalledProjectPackages(Connection connection)
            throws ForceConnectionException, ForceRemoteException {
        return getProjectPackageWork(connection, SoqlEnum.getDevelopmentPackages(), SoqlEnum
                .getUnManagedInstalledPackages());
    }

    public ProjectPackageList getManagedInstalledProjectPackages(Connection connection)
            throws ForceConnectionException, ForceRemoteException {
        return getProjectPackageWork(connection, SoqlEnum.getManagedInstalledPackages());
    }

    public ProjectPackageList getManagedInstalledProjectPackages(Connection connection, String[] packageNames)
            throws ForceConnectionException, ForceRemoteException {
        return getProjectPackageWork(connection, SoqlEnum.getManagedInstalledPackages(packageNames));
    }

    private ProjectPackageList getProjectPackageWork(Connection connection, String... soqls)
            throws ForceConnectionException, ForceRemoteException {
        if (connection == null || Utils.isEmpty(soqls)) {
            throw new IllegalArgumentException("Connection and/or sosql name cannot be null");
        }

        ProjectPackageList projectPackageList = getProjectPackageListInstance();
        for (String soql : soqls) {
            QueryResult result = connection.query(soql);
            if (Utils.isNotEmpty(result)) {
                SObject[] sobjects = result.getRecords();
                if (Utils.isNotEmpty(sobjects)) {
                    for (SObject sobject : sobjects) {
                        ProjectPackage projectPackage = getProjectPackageInstance();
                        projectPackage.setOrgId(connection.getOrgId());
                        projectPackage.parseInput(sobject);
                        projectPackageList.add(projectPackage);
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(projectPackageList.isEmpty() ? "No project packages assembled" : projectPackageList
                            .toString());
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Got [0] packages for " + connection.getLogDisplay());
                }
            }
        }
        return projectPackageList;
    }

    public ProjectPackageList loadProjectPackageList(IResource resource, IProgressMonitor monitor)
            throws FactoryException, CoreException, InterruptedException {
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }

        return loadProjectPackageList(resource, null, true, monitor);
    }

    public ProjectPackageList loadProjectPackageList(IResource resource, ProjectPackageList projectPackageList,
            boolean includeBody, IProgressMonitor monitor) throws FactoryException, CoreException, InterruptedException {
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Loading project package list for resource '"
                    + resource.getProjectRelativePath().toPortableString() + "'");
        }

        monitorCheck(monitor);

        // abort if resource is not a force managed resource
        boolean forceManaged = getProjectService().isManagedResource(resource);
        if (!forceManaged) {
            return null;
        }

        monitorCheck(monitor);
        if (resource.getType() == IResource.FILE) {
            IFile file = (IFile) resource;
            projectPackageList = prepareProjectPackageList(file, projectPackageList, monitor);
        } else if (resource.getType() == IResource.FOLDER) {
            IFolder folder = (IFolder) resource;
            projectPackageList = prepareProjectPackageList(folder, projectPackageList, includeBody, monitor);
        } else if (resource.getType() == IResource.PROJECT) {
            IProject project = (IProject) resource;
            projectPackageList = prepareProjectPackageList(project, projectPackageList, monitor);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Loaded project package list for resource '"
                    + resource.getProjectRelativePath().toPortableString() + "':\n" + projectPackageList);
        }

        return projectPackageList;
    }

    private ProjectPackageList prepareProjectPackageList(IFile file, ProjectPackageList projectPackageList,
            IProgressMonitor monitor) throws FactoryException, InterruptedException {
        if (projectPackageList == null) {
            projectPackageList = getProjectPackageListInstance();
        }

        projectPackageList.setProject(file.getProject());

        monitorCheck(monitor);
        Component component = getComponentFactory().getComponentFromFile(file);
        if (component == null) {
            logger.warn("File '" + file.getProjectRelativePath().toPortableString()
                    + "' not supported by copy refactoring");
            return projectPackageList;
        }

        boolean exists = projectPackageList.hasComponent(component);
        if (exists) {
            logger.warn("Project package list already contains component " + component.getFullDisplayName()
                    + " for file '" + file.getProjectRelativePath().toPortableString() + "'");
            return projectPackageList;
        }

        projectPackageList.addComponent(component, true);

        if (logger.isInfoEnabled()) {
            logger
                    .info("Added file '" + file.getProjectRelativePath().toPortableString()
                            + "' to project package list");
        }
        return projectPackageList;
    }

    private ProjectPackageList prepareProjectPackageList(IFolder folder, ProjectPackageList projectPackageList,
            boolean includeBody, IProgressMonitor monitor) throws FactoryException, CoreException, InterruptedException {
        if (projectPackageList == null) {
            projectPackageList = getProjectPackageListInstance();
        }

        monitorCheck(monitor);
        projectPackageList.setProject(folder.getProject());
        if (getProjectService().isSourceFolder(folder)) {
            List<IFolder> componentFolders = getProjectService().getComponentFolders(folder);
            if (Utils.isNotEmpty(componentFolders)) {
                for (IFolder componentFolder : componentFolders) {
                    loadComponentFolder(componentFolder, projectPackageList, includeBody, monitor);
                }
            }
        } else if (getProjectService().isComponentFolder(folder)) {
            loadComponentFolder(folder, projectPackageList, includeBody, monitor);
        } else if (getProjectService().isSubComponentFolder(folder)) {
            loadSubComponentFolder(folder, projectPackageList, monitor);
        } else {
            logger.warn("Unable to get project package list for folder '"
                    + folder.getProjectRelativePath().toPortableString());
            return null;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Added file '" + folder.getProjectRelativePath().toPortableString()
                    + "' to project package list");
        }

        return projectPackageList;
    }

    private void loadComponentFolder(IFolder componentFolder, ProjectPackageList projectPackageList,
            boolean includeBody, IProgressMonitor monitor) throws CoreException, FactoryException, InterruptedException {

        monitorCheck(monitor);

        ComponentList componentList =
                getProjectService().getComponentsForComponentFolder(componentFolder, true, includeBody);
        if (Utils.isEmpty(componentList)) {
            logger.warn("No components found for component folder '"
                    + componentFolder.getProjectRelativePath().toPortableString());
            return;
        }

        projectPackageList.addComponents(componentList, false);
    }

    private void loadSubComponentFolder(IFolder componentFolder, ProjectPackageList projectPackageList,
            IProgressMonitor monitor) throws CoreException, FactoryException, InterruptedException {

        monitorCheck(monitor);

        ComponentList componentList = getProjectService().getComponentsForSubComponentFolder(componentFolder, true);
        if (Utils.isEmpty(componentList)) {
            logger.warn("No components found for component folder '"
                    + componentFolder.getProjectRelativePath().toPortableString());
            return;
        }

        projectPackageList.addComponents(componentList, false);
    }

    private ProjectPackageList prepareProjectPackageList(IProject project, ProjectPackageList projectPackageList,
            IProgressMonitor monitor) throws CoreException, FactoryException, InterruptedException {
        if (projectPackageList == null) {
            projectPackageList = getProjectPackageListInstance();
        }

        monitorCheck(monitor);
        if (!getProjectService().hasSourceFolderContents(project)) {
            logger.warn("Selected project to be saved not found or is empty");
            return projectPackageList;
        }

        IFolder sourceFolder = getProjectService().getSourceFolder(project);
        return prepareProjectPackageList(sourceFolder, projectPackageList, true, monitor);
    }
}
