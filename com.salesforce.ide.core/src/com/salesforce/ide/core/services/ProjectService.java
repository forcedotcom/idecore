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
package com.salesforce.ide.core.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Preconditions;
import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.compatibility.auth.IAuthorizationService;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.components.lightning.AuraDefinitionBundleUtils.DeployErrorHandler;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.DeployMessageExtractor;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.QualifiedNames;
import com.salesforce.ide.core.internal.utils.QuietCloseable;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ApexCodeLocation;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.IComponent;
import com.salesforce.ide.core.model.PackageConfiguration;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.BaseNature;
import com.salesforce.ide.core.project.DefaultNature;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.MarkerUtils;
import com.salesforce.ide.core.project.OnlineNature;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.ICodeCoverageWarningExt;
import com.salesforce.ide.core.remote.IRunTestFailureExt;
import com.salesforce.ide.core.remote.SalesforceEndpoints;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.RetrieveMessageExt;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.remote.metadata.RunTestsResultExt;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.RetrieveMessage;

/**
 * 
 * @author cwall
 */
public class ProjectService extends BaseService {

    private static final Logger logger = Logger.getLogger(ProjectService.class);
    private static final String AUTH_URL = "http://www.salesforce.com";
    private static final String AUTH_TYPE = "Basic";
    private String ideBrandName = null;
    private String platformBrandName = null;
    private String ideReleaseName = null;
    private TreeSet<String> supportedEndpointVersions = null;
    private final IAuthorizationService authService;

    public ProjectService() {
        this(new AuthorizationServicePicker());
    }

    public ProjectService(AuthorizationServicePicker factory) {
        this.authService = factory.makeAuthorizationService();
    }

    public String getIdeBrandName() {
        return ideBrandName;
    }

    public void setIdeBrandName(String ideBrandName) {
        this.ideBrandName = ideBrandName;
    }

    public String getPlatformBrandName() {
        return platformBrandName;
    }

    public void setPlatformBrandName(String platformBrandName) {
        this.platformBrandName = platformBrandName;
    }

    public String getIdeReleaseName() {
        return ideReleaseName;
    }

    public void setIdeReleaseName(String ideReleaseName) {
        this.ideReleaseName = ideReleaseName;
    }

    public TreeSet<String> getSupportedEndpointVersions() {
        return supportedEndpointVersions;
    }

    public void setSupportedEndpointVersions(TreeSet<String> supportedEndpointVersions) {
        this.supportedEndpointVersions = supportedEndpointVersions;
    }

    public boolean isSupportedEndpointVersion(String endpointVersion) {
        if (Utils.isNotEmpty(supportedEndpointVersions) && Utils.isNotEmpty(endpointVersion)) {
            endpointVersion = extractEndpointVersion(endpointVersion);
            return supportedEndpointVersions.contains(endpointVersion);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Endpoint version [" + endpointVersion + "] is not supported");
            }
            return false;
        }
    }

    private static String extractEndpointVersion(String endpoint) {
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        if (endpoint.contains("/")) {
            endpoint = endpoint.substring(endpoint.lastIndexOf("/") + 1);
        }

        return endpoint;
    }

    public String getLastSupportedEndpointVersion() {
        return Utils.isNotEmpty(supportedEndpointVersions) ? supportedEndpointVersions.last() : "";
    }

    // C R E A T E   P R O J E C T
    public IProject createProject(String projectName, String[] natures, IProgressMonitor monitor) throws CoreException {
        if (projectName == null) {
            throw new IllegalArgumentException("Project name cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Create new project '" + projectName + "'");
        }

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject newProject = root.getProject(projectName);

        // if project doesn't exist, create
        IProjectDescription projectDescription = workspace.newProjectDescription(projectName);
        if (!newProject.exists()) {
            projectDescription = workspace.newProjectDescription(projectName);
            newProject.create(projectDescription, monitor);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Existing project '" + projectName
                        + "' found in workspace - will attempt to apply Force.com");
            }
            projectDescription = newProject.getDescription();
        }

        // open project
        newProject.open(monitor);

        // create project w/ natures does not configure builds like the following does
        if (Utils.isNotEmpty(natures) && newProject.isOpen()) {
            BaseNature.addNature(newProject, natures, monitor);
        } else {
            logger.warn("Applying no nature to new project '" + projectName + "'");
        }

        flagProjectAsNew(newProject);
        return newProject;
    }

    public void flagProjectAsNew(IProject project) throws CoreException {
        // set project property identifying this project as brand new
        // property is used downstream to skip building of new projects
        flagSkipBuilder(project);
    }

    public void flagSkipBuilder(IProject project) throws CoreException {
        if (project == null) {
            logger.error("Unable to set skip builder flag - project is null");
            return;
        }

        project.setSessionProperty(QualifiedNames.QN_SKIP_BUILDER, true);
    }

    public ProjectPackageList getProjectContents(IResource resource, IProgressMonitor monitor) throws CoreException,
            InterruptedException, FactoryException {
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }

        List<IResource> resources = new ArrayList<>(1);
        resources.add(resource);
        return getProjectContents(resources, monitor);
    }

    public ProjectPackageList getProjectContents(List<IResource> resources, IProgressMonitor monitor)
            throws CoreException, InterruptedException, FactoryException {
        return getProjectContents(resources, false, monitor);
    }

    public ProjectPackageList getProjectContents(
        List<IResource> resources,
        boolean includeAssociated,
        IProgressMonitor monitor) 
            throws CoreException, InterruptedException, FactoryException {
        if (Utils.isEmpty(resources)) {
            throw new IllegalArgumentException("Resources cannot be null");
        }

        ProjectPackageList projectPackageList = getProjectPackageListInstance();
        projectPackageList.setProject(resources.get(0).getProject());
        for (IResource resource : resources) {
            if (resource.getType() == IResource.PROJECT) {
                projectPackageList = getProjectContents(resource.getProject(), monitor);
            } else if (isSourceFolder(resource)) {
                projectPackageList = getProjectContents(resource.getProject(), monitor);
            } else if (isComponentFolder(resource)) {
                ComponentList componentList = getComponentsForComponentFolder((IFolder) resource, true, true);
                projectPackageList.addComponents(componentList, false);
            } else if (isSubComponentFolder(resource)) {
                ComponentList componentList = getComponentsForSubComponentFolder((IFolder) resource, true);
                projectPackageList.addComponents(componentList, false);
            } else if (isManagedFile(resource)) { //if we're in a force.com project. "isManaged" is misleading.
                Component component = getComponentFromFile(resource);
                projectPackageList.addComponent(component, true);

                // add dependent or associated components such as folder metadata component if component is sub-folder component
                if (includeAssociated) {
                    ComponentList componentList = getComponentFactory().getAssociatedComponents(component);
                    if (Utils.isNotEmpty(componentList)) {
                        projectPackageList.addComponents(componentList, false);
                    }
                }
            }
        }
        //filter out any built in folder stuff.
        // if component subfolder is built-in subfolder, then this subfolder will be available in dest org (also this subfolder is not retrievable/deployable)
        // so no need to add subfolder component to deploy list. W-623512
        ComponentList allComponentsList = getComponentFactory().getComponentListInstance();
        allComponentsList.addAll(projectPackageList.getAllComponents());
        for (Component component : allComponentsList) {
            if (Utils.isNotEmpty(component.getBuiltInSubFolders())) {
                for (String builtInSubFolder : component.getBuiltInSubFolders()) {
                    if (builtInSubFolder.equals(component.getName())) {
                        projectPackageList.removeComponent(component);
                    }
                }
            }
        }

        return projectPackageList;
    }

    private Component getComponentFromFile(IResource resource) throws FactoryException {
        Component component = getComponentFactory().getComponentFromFile((IFile) resource);
        
        // W-3437959 - You cannot retrieve a StandardObject per-se, the package.xml needs to say CustomObject
        if (component.getComponentType().equals(Constants.STANDARD_OBJECT)) {
            component.setComponentType(Constants.CUSTOM_OBJECT);
        }
        return component;
    }

    public ProjectPackageList getProjectContents(IProject project, IProgressMonitor monitor) throws CoreException,
            InterruptedException, FactoryException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        monitorCheck(monitor);

        // initialize container to holder contents
        ProjectPackageList projectPackageList = getProjectPackageListInstance();
        projectPackageList.setProject(project);

        // ensure project is not empty
        if (isProjectEmpty(project)) {
            return projectPackageList;
        }

        // get src folder; discontinue if not found or empty
        IResource sourceResource = project.findMember(Constants.SOURCE_FOLDER_NAME);
        if (sourceResource == null || !sourceResource.exists() || sourceResource.getType() != IResource.FOLDER
                || Utils.isEmpty(((IFolder) sourceResource).members())) {
            if (logger.isInfoEnabled()) {
                logger.info(Constants.SOURCE_FOLDER_NAME
                        + " not found, does not exist, or does not contain package folders in project '"
                        + project.getName() + "'");
            }
            return projectPackageList;
        }

        List<IFolder> componentFolders = getComponentFolders((IFolder) sourceResource);
        for (IFolder componentFolder : componentFolders) {
            monitorCheck(monitor);

            if (Utils.isEmpty(componentFolder.members())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping folder " + project.getName() + "' - no components found");
                }
                continue;
            }

            // find components in componet folder
            ComponentList componentList = getComponentsForComponentFolder(componentFolder, true, true);
            if (componentList.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Component folder '" + componentFolder.getName()
                            + "' is does not contain components for enabled object types");
                }
                continue;
            }

            // add components to project package list instance including reference to package manifest
            projectPackageList.addComponents(componentList, false);
            if (logger.isDebugEnabled()) {
                logger.debug("Added '" + componentFolder.getName() + "' folder containing [" + componentList.size()
                        + "] components to project package list");
            }
        }

        return projectPackageList;
    }

    // R E S O U R C E   F I N D E R S

    /**
     * Get Force.com projects
     * 
     * @param includeOnline
     *            include all projects with online nature vs. only base nature
     * @return
     */
    public List<IProject> getForceProjects() {
        List<IProject> forceProjects = new ArrayList<>();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        if (Utils.isNotEmpty(projects)) {
            for (IProject project : projects) {
                if (project.isOpen() && isForceProject(project)) {
                    forceProjects.add(project);
                }
            }
        }
        return forceProjects;
    }
    
    public boolean isForceProject(IProject project) {
        try {
            return project.hasNature(DefaultNature.NATURE_ID);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to determine project nature: " + logMessage);
        }
        return false;
    }

    /**
     * This method supports getting component file within sub-component folder (only 1 level down - not recursive)
     * 
     * @param project
     * @param componentName
     * @param componentTypes
     * @return
     * @throws CoreException
     */
    public IFile getComponentFileByNameType(IProject project, String componentName, String[] componentTypes)
            throws CoreException {
        if (Utils.isEmpty(componentName) || project == null || Utils.isEmpty(componentTypes)) {
            throw new IllegalArgumentException("Component name, types, and/or project cannot be null");
        }

        IFile file = null;
        for (String componentType : componentTypes) {
            file = getComponentFileByNameType(project, componentName, componentType);
            if (file != null) {
                return file;
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("File for '" + componentName + "' not found");
        }

        return file;
    }

    public IFile getComponentFileByNameType(IProject project, String componentName, String componentType)
            throws CoreException {
        if (Utils.isEmpty(componentName) || project == null || Utils.isEmpty(componentType)) {
            throw new IllegalArgumentException("Package name, type, and/or project cannot be null");
        }

        IFolder componentFolder = getComponentFolderByComponentType(project, componentType);
        if (componentFolder == null || !componentFolder.exists()) {
            if (logger.isInfoEnabled()) {
                logger.info("Did not find folder for type '" + componentType);
            }
            return null;
        }

        Component componentTypeInfo = getComponentFactory().getComponentByComponentType(componentType);
        IResource[] resources = componentFolder.members();
        for (IResource resource : resources) {
            String fullName = componentName;
            if (!componentName.contains(componentTypeInfo.getFileExtension())) {
                fullName = componentName + "." + componentTypeInfo.getFileExtension();
            }
            // because trigger and class may have the same name, so need to match the file extension as well
            if (resource.getType() == IResource.FILE && ((IFile) resource).getName().equalsIgnoreCase(fullName)) {
                if (logger.isInfoEnabled()) {
                    StringBuilder stringBuilder = new StringBuilder("File '");
                    stringBuilder.append(resource.getProjectRelativePath().toPortableString())
                            .append(" found for component type '").append(componentType).append("', component '")
                            .append(componentName).append("'");
                    logger.info(stringBuilder.toString());
                }
                return (IFile) resource;
            } else if (resource.getType() == IResource.FOLDER) {
                IResource[] subFolderResources = ((IFolder) resource).members();
                for (IResource subFolderResource : subFolderResources) {
                    // because trigger and class may have the same name, so need to match the file extension as well
                    if (subFolderResource.getType() == IResource.FILE
                            && ((IFile) subFolderResource).getName().equalsIgnoreCase(fullName)) {
                        if (logger.isInfoEnabled()) {
                            StringBuilder stringBuilder = new StringBuilder("File '");
                            stringBuilder
                                .append(subFolderResource.getProjectRelativePath().toPortableString())
                                .append(" found for component type '").append(componentType)
                                .append("', component '").append(componentName).append("'");
                            logger.info(stringBuilder.toString());
                        }
                        return (IFile) subFolderResource;
                    }
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("File not found for component '" + componentName + "'");
        }

        return null;
    }

    public IFile getCompositeFileResource(IFile file) {
        // check is file is a component and is a metadata composite, if so add composite
        Component component = null;
        try {
            component = getComponentFactory().getComponentFromFile(file);
        } catch (FactoryException e) {
            logger.warn("Unable to detemine if file '" + file.getName() + "' is a component");
            return null;
        }

        if (component.isPackageManifest() || !component.isMetadataComposite()) {
            return null;
        }

        // load component composite
        String compositeComponentFilePath = component.getCompositeMetadataFilePath();
        if (Utils.isEmpty(compositeComponentFilePath)) {
            logger.warn("Component metadata path is null for " + component.getFullDisplayName());
            return null;
        }
        return getComponentFileForFilePath(file.getProject(), compositeComponentFilePath);
    }

    public List<IFolder> getComponentFolders(IFolder sourceFolder) throws CoreException {
        if (sourceFolder == null) {
            throw new IllegalArgumentException("Source folder cannot be null");
        }

        if (!sourceFolder.exists() || !isSourceFolder(sourceFolder)) {
            if (logger.isInfoEnabled()) {
                logger.info("Did not find '" + Constants.SOURCE_FOLDER_NAME + " folder");
            }
            return null;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Found '" + sourceFolder.getName() + "' folder");
        }

        IResource[] sourceFolderResources = sourceFolder.members();
        if (Utils.isEmpty(sourceFolderResources)) {
            if (logger.isInfoEnabled()) {
                logger.info("Did not any package folders in '" + Constants.SOURCE_FOLDER_NAME + " folder");
            }
            return null;
        }

        List<IFolder> componentFolders = new ArrayList<>();
        for (IResource sourceFolderResource : sourceFolderResources) {
            if (sourceFolderResource.getType() != IResource.FOLDER) {
                continue;
            }

            if (isComponentFolder(sourceFolderResource)) {
                componentFolders.add((IFolder) sourceFolderResource);
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Found [" + componentFolders.size() + "] component folders");
        }

        return componentFolders;
    }

    public IFolder getComponentFolderByComponentType(IProject project, String componentType) throws CoreException {
        Component component = getComponentFactory().getComponentByComponentType(componentType);
        if (component == null) {
            logger.warn("Unable to find component for type '" + componentType + "'");
            return null;
        }
        return getComponentFolderByName(project, component.getDefaultFolder(), false, new NullProgressMonitor());
    }

    public IFolder getComponentFolderByName(IProject project, String componentFolderName, boolean create,
            IProgressMonitor monitor) throws CoreException {
        if (Utils.isEmpty(componentFolderName) || project == null) {
            throw new IllegalArgumentException("Folder name and/or project cannot be null");
        }

        IFolder sourceFolder = getSourceFolder(project);
        if (sourceFolder == null || !sourceFolder.exists() || Utils.isEmpty(sourceFolder.members())) {
            if (logger.isInfoEnabled()) {
                logger.info("Did not find '" + Constants.SOURCE_FOLDER_NAME + " folder or folder is empty");
            }
            return null;
        }

        IFolder componentFolder = sourceFolder.getFolder(componentFolderName);
        if (componentFolder == null || !componentFolder.exists()) {
            if (logger.isInfoEnabled()) {
                logger.info("Did not find component folder '" + componentFolderName + "'");
            }
            return null;
        }

        return componentFolder;
    }

    public ProjectPackageList getComponentsForComponentTypes(IProject project, String[] componentTypes)
            throws CoreException, FactoryException {
        ProjectPackageList projectPackageList = getProjectPackageFactory().getProjectPackageListInstance(project);
        if (Utils.isNotEmpty(componentTypes)) {
            for (String componentType : componentTypes) {
                IFolder componentFolder = getComponentFolderByComponentType(project, componentType);
                if (componentFolder != null && componentFolder.exists()) {
                    ComponentList componentList = getComponentFactory().getComponentListInstance();
                    componentList.addAll(getComponentsForFolder(componentFolder, true));
                    projectPackageList.addComponents(componentList, true);
                }
            }
        }
        return projectPackageList;
    }

    public ComponentList getComponentsForComponentType(IProject project, String componentType) throws CoreException,
            FactoryException {
        IFolder componentFolder = getComponentFolderByComponentType(project, componentType);
        ComponentList componentList = getComponentFactory().getComponentListInstance();
        componentList.addAll(getComponentsForFolder(componentFolder, true));
        return componentList;
    }

    public ComponentList getComponentsForFolder(IFolder folder, boolean traverse) throws CoreException,
            FactoryException {
        return getComponentsForFolder(folder, traverse, true);
    }

    public ComponentList getComponentsForFolder(IFolder folder, boolean traverse, boolean includeManifest)
            throws CoreException, FactoryException {
        return getComponentsForFolder(folder, null, traverse, includeManifest);
    }

    public ComponentList getComponentsForFolder(IFolder folder, String[] enabledComponentTypes, boolean traverse)
            throws CoreException, FactoryException {
        return getComponentsForFolder(folder, enabledComponentTypes, traverse, true);
    }

    public ComponentList getComponentsForFolder(IFolder folder, String[] enabledComponentTypes, boolean traverse,
            boolean includeManifest) throws CoreException, FactoryException {
        if (folder == null || !folder.exists()) {
            throw new IllegalArgumentException("Folder cannot be null");
        }

        // initialize component
        ComponentList componentList = getComponentFactory().getComponentListInstance();

        // convert for contains access
        List<String> enabledComponentTypesList = null;
        if (enabledComponentTypes != null) {
            enabledComponentTypesList = Arrays.asList(enabledComponentTypes);
        }

        if (isSourceFolder(folder)) {
            // loop thru registered component list inspecting respective folder and folder contents
            ComponentList registeredComponents =
                    getComponentFactory().getEnabledRegisteredComponents(enabledComponentTypes);
            for (IComponent registeredComponent : registeredComponents) {
                if (null != enabledComponentTypesList
                        && !enabledComponentTypesList.contains(registeredComponent.getComponentType())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipping component type '" + registeredComponent.getComponentType()
                                + "' - type not a selected object type");
                    }
                    continue;
                }
                String componentFolderPath =
                        Constants.SOURCE_FOLDER_NAME + "/" + registeredComponent.getDefaultFolder();
                IFolder componentFolder = folder.getProject().getFolder(componentFolderPath);

                if (componentFolder == null || !componentFolder.exists()) {
                    continue;
                }

                ComponentList tmpComponentList = getComponentsForComponentFolder(componentFolder, traverse, true);
                if (Utils.isNotEmpty(tmpComponentList)) {
                    componentList.addAll(tmpComponentList);
                }
            }
        } else if (isComponentFolder(folder)) {
            ComponentList tmpComponentList = getComponentsForComponentFolder(folder, traverse, true);
            if (Utils.isNotEmpty(tmpComponentList)) {
                componentList.addAll(tmpComponentList);
            }
        } else if (isSubComponentFolder(folder)) {
            ComponentList tmpComponentList = getComponentsForComponentFolder(folder, traverse, true);
            if (Utils.isNotEmpty(tmpComponentList)) {
                componentList.addAll(tmpComponentList);
            }
        }

        return componentList;
    }

    public ComponentList getComponentsForComponentFolder(IFolder folder, boolean traverse, boolean includeBody)
            throws CoreException, FactoryException {
        if (null == folder) return null;

        if (!folder.exists() || Utils.isEmpty(folder.members())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Component folder '" + folder.getName() + "' does not exist or does not "
                        + "contain components");
            }
            return null;
        }

        // initialize component
        ComponentList componentList = getComponentFactory().getComponentListInstance();

        IResource[] componentFolderResources = folder.members();
        for (IResource componentFolderResource : componentFolderResources) {
            if (traverse && IResource.FOLDER == componentFolderResource.getType()) {
                IFolder componentFolderWithSub = (IFolder) componentFolderResource;
                ComponentList tmpComponentList = getComponentsForSubComponentFolder(componentFolderWithSub, traverse);
                componentList.addAll(tmpComponentList);

            } else if (IResource.FILE == componentFolderResource.getType()) {
                IFile componentFile = (IFile) componentFolderResource;
                try {
                    Component tmpComponent = getComponentFactory().getComponentFromFile(componentFile);

                    componentList.add(tmpComponent, 
                        PackageConfiguration.builder()
                        .setIncludeComposite(false)
                        .setRemoveComposite(true)
                        .build());

                } catch (FactoryException e) {
                    logger.error("Unable to create component from filepath " + componentFile.getProjectRelativePath().toPortableString());
                }
            }
        }

        return componentList;
    }

    /**
     * This method is used to retrieve components under sub-componentfolder including sub-componentfolder itself in the
     * returned list. Assumption: the subComponentFolder's metadata file is located under componentFolder and naming as
     * <subComponentFolderName>-meta.xml
     * 
     * @param folder
     *            - subComponentFolder
     * @param traverse
     * @return
     * @throws CoreException
     * @throws FactoryException
     */
    public ComponentList getComponentsForSubComponentFolder(IFolder folder, boolean traverse) throws CoreException,
            FactoryException {
        ComponentList componentList = getComponentsForComponentFolder(folder, traverse, true);
        if (componentList == null) {
            componentList = getComponentFactory().getComponentListInstance();
        }

        Component component = getComponentFactory().getComponentFromSubFolder(folder, false);
        componentList.add(component);
        return componentList;
    }

    public IFile getComponentFileForFilePath(IProject project, String filePath) {
        if (Utils.isEmpty(filePath) || project == null) {
            throw new IllegalArgumentException("Filepath and/or project cannot be null");
        }

        String tmpFilePath = filePath;
        if (!filePath.startsWith(Constants.SOURCE_FOLDER_NAME)) {
            IFolder packageSourceFolder = getSourceFolder(project);
            if (packageSourceFolder == null || !packageSourceFolder.exists()) {
                logger.warn("Unable to find package source folder.  Discontinuing search of file '" + filePath + "'");
                return null;
            }
            tmpFilePath = packageSourceFolder.getProjectRelativePath().toPortableString() + "/" + filePath;
        }

        IResource resource = project.findMember(tmpFilePath);
        if (resource == null || !resource.exists() || resource.getType() != IResource.FILE) {
            logger.warn("Resource not found or not a file for filepath '" + tmpFilePath + "'");
            return null;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Found resource for filepath '" + tmpFilePath + "'");
        }

        return (IFile) resource;
    }

    public List<IResource> filterChildren(List<IResource> selectedResources) {
        if (Utils.isEmpty(selectedResources) || selectedResources.size() == 1) {
            return selectedResources;
        }

        for (Iterator<IResource> iterator = selectedResources.iterator(); iterator.hasNext();) {
            IResource resource = iterator.next();
            if (hasParent(selectedResources, resource)) {
                iterator.remove();
            }
        }

        return selectedResources;
    }

    private boolean hasParent(List<IResource> selectedResources, IResource resource) {
        if (resource == null || resource.getParent() == null || resource.getType() == IResource.ROOT
                || Utils.isEmpty(selectedResources)) {
            return false;
        }

        if (resource.getParent() != null && selectedResources.contains(resource.getParent())) {
            return true;
        }

        return hasParent(selectedResources, resource.getParent());
    }

    /**
     * filters out only IFiles from a list of IResources
     * 
     * @param allResources
     * @return
     * @throws CoreException
     */
    public List<IResource> getAllFilesOnly(List<IResource> allResources) throws CoreException {
        if (Utils.isEmpty(allResources)
                || (allResources.size() == 1 && allResources.get(0).getType() == IResource.FILE)) {
            return allResources;
        }

        List<IResource> ret_filesList = new ArrayList<>();
        for (IResource resource : allResources) {
            if (resource.getType() == IResource.PROJECT) {
                addAllFilesOnly((IProject) resource, ret_filesList);
            } else if (resource.getType() == IResource.FOLDER) {
                addAllFilesOnly((IFolder) resource, ret_filesList);
            } else if (resource.getType() == IResource.FILE && isBuildableResource(resource)) {
                ret_filesList.add(resource);
            }
        }

        return ret_filesList;
    }

    /**
     * filters out files from a list of resource given a IProject
     * 
     * @param project
     *            the project in which these files belong
     * @param resourcesList
     *            a list of all resources that will be populated with IFiles
     * @throws CoreException
     */
    private void addAllFilesOnly(IProject project, List<IResource> resourcesList) throws CoreException {
        if (Utils.isEmpty(project.members())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Project does not have members");
            }
            return;
        }

        for (IResource member : project.members()) {
            if (member.getType() == IResource.FOLDER) {
                addAllFilesOnly((IFolder) member, resourcesList);
            } else if (member.getType() == IResource.FILE && isBuildableResource(member)) {
                resourcesList.add(member);
            }
        }
    }

    /**
     * filters out files from a list of resources given a Ifolder
     * 
     * @param folder
     *            the folder to work on.
     * @param resourcesList
     *            a list of resources which will be populated.
     * @throws CoreException
     */
    private void addAllFilesOnly(IFolder folder, List<IResource> resourcesList) throws CoreException {
        if (Utils.isEmpty(folder.members())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Folder does not have members");
            }
            return;
        }

        for (IResource member : folder.members()) {
            if (member.getType() == IResource.FOLDER) {
                addAllFilesOnly((IFolder) member, resourcesList);
            } else if (member.getType() == IResource.FILE && isBuildableResource(member)) {
                resourcesList.add(member);
            }
        }
    }

    public List<IResource> getResourcesByType(List<IResource> resources, int type) {
        if (Utils.isEmpty(resources)) {
            return null;
        }

        List<IResource> specificResources = new ArrayList<>(resources.size());
        for (IResource resource : resources) {
            if (resource.getType() == type) {
                specificResources.add(resource);
            }
        }
        return specificResources;
    }

    public IFolder getFolder(List<IResource> resources, String name) {
        if (Utils.isEmpty(resources)) {
            return null;
        }

        for (IResource resource : resources) {
            if (resource.getType() == IResource.FOLDER && resource.getName().endsWith(name)) {
                return (IFolder) resource;
            }
        }

        return null;
    }

    public boolean isProjectEmpty(IProject project) throws CoreException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        IResource[] resources = project.members();
        if (Utils.isEmpty(resources)) {
            if (logger.isInfoEnabled()) {
                logger.info("No resources found in project '" + project.getName() + "'");
            }
            return true;
        }
        return false;
    }

    public IFolder getSourceFolder(IProject project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        IResource packageSourceFolder = project.findMember(Constants.SOURCE_FOLDER_NAME);

        if (packageSourceFolder == null || !packageSourceFolder.exists()
                || packageSourceFolder.getType() != IResource.FOLDER) {
            if (logger.isInfoEnabled()) {
                logger.info("Did not find '" + Constants.SOURCE_FOLDER_NAME + " folder");
            }
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found '" + packageSourceFolder.getName() + "' folder");
        }

        return (IFolder) packageSourceFolder;
    }

    public IFolder getReferencedPackagesFolder(IProject project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        IResource referencedPackageFolder = project.findMember(Constants.REFERENCED_PACKAGE_FOLDER_NAME);

        if (referencedPackageFolder == null || !referencedPackageFolder.exists()
                || referencedPackageFolder.getType() != IResource.FOLDER) {
            if (logger.isInfoEnabled()) {
                logger.info("Did not find '" + Constants.REFERENCED_PACKAGE_FOLDER_NAME + " folder");
            }
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found '" + referencedPackageFolder.getName() + "' folder");
        }

        return (IFolder) referencedPackageFolder;
    }

    public boolean hasSourceFolderContents(IProject project) throws CoreException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        IResource packageSourceFolder = project.findMember(Constants.SOURCE_FOLDER_NAME);

        if (packageSourceFolder == null || !packageSourceFolder.exists()
                || packageSourceFolder.getType() != IResource.FOLDER) {
            if (logger.isInfoEnabled()) {
                logger.info("Did not find '" + Constants.SOURCE_FOLDER_NAME + " folder");
            }
            return false;
        }

        if (Utils.isEmpty(((IFolder) packageSourceFolder).members())) {
            return false;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Project '" + project.getName() + "' has contents in package folders");
        }

        return true;
    }

    // N A T U R E S
    public void applyDefaultNature(IProject project, IProgressMonitor monitor) throws CoreException {
        DefaultNature.addNature(project, monitor);
        monitorWork(monitor);
    }

    public void applyOnlineNature(IProject project, IProgressMonitor monitor) throws CoreException {
        OnlineNature.addNature(project, monitor);
        monitorWork(monitor);
    }

    public void applyNatures(IProject project, IProgressMonitor monitor) throws CoreException {
        applyDefaultNature(project, monitor);
        applyOnlineNature(project, monitor);
    }

    public void removeNatures(IProject project, IProgressMonitor monitor) {
        OnlineNature.removeNature(project, monitor);
        monitorWork(monitor);

        DefaultNature.removeNature(project, monitor);
        monitorWork(monitor);
    }

    // I D E   M A N A G E D   R E S O U R C E   C H E C K S
    public boolean isManagedResources(List<IResource> resources) {
        if (Utils.isEmpty(resources)) {
            return false;
        }

        for (IResource resource : resources) {
            if (!isManagedResource(resource)) {
                return false;
            }
        }

        return true;
    }

    public boolean isManagedResource(IResource resource) {
        if (resource == null) {
            return false;
        }

        if (resource.getType() == IResource.FILE) {
            return isManagedFile(resource);
        } else if (resource.getType() == IResource.FOLDER) {
            return isManagedFolder(resource);
        } else if (resource.getType() == IResource.PROJECT) {
            return isManagedProject(resource);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Resource type '" + resource.getType() + "' not managed by force");
            }
            return false;
        }

    }

    public boolean isManagedFile(IResource resource) {
        if (resource == null || resource.getType() != IResource.FILE) {
            return false;
        }

        return isManagedFile((IFile) resource);
    }

    public boolean isManagedFile(IFile file) {
        if (file == null || file.getType() != IResource.FILE) {
            return false;
        }

        if (!isInManagedProject(file)) {
            return false;
        }

        if (Utils.isEmpty(file.getProjectRelativePath().toPortableString())) {
            if (logger.isInfoEnabled()) {
                logger.info("Filepath not found for file '" + file + "'");
            }
            return false;
        }

        Component component = null;
        component = getComponentFactory().getComponentByFilePath(file.getProjectRelativePath().toPortableString());

        if (component != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Component found for filepath '" + file.getProjectRelativePath().toPortableString() + "'");
            }
            return true;
        }

        logger.warn("Component not found for filepath '" + file.getProjectRelativePath().toPortableString() + "'");

        return false;
    }

    public boolean isManagedFolder(IResource resource) {
        if (resource == null || resource.getType() != IResource.FOLDER) {
            return false;
        }

        return isManagedFolder(((IFolder) resource));
    }

    public boolean isManagedFolder(IFolder folder) {
        if (folder == null || folder.getType() != IResource.FOLDER) {
            return false;
        }

        if (!isInManagedProject(folder)) {
            return false;
        }

        if (isSourceFolder(folder)) {
            return true;
        }

        if (isComponentFolder(folder)) {
            return true;
        }

        if (isSubComponentFolder(folder)) {
            return true;
        }

        if (isPackageFolder(folder)) {
            return true;
        }

        if (isReferencedPackagesFolder(folder)) {
            return true;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Folder '" + folder.getProjectRelativePath().toPortableString() + "' is not "
                    + Constants.PLUGIN_NAME + " managed");
        }

        return false;
    }

    public boolean isManagedProject(IResource resource) {
        if (resource == null || resource.getType() != IResource.PROJECT) {
            return false;
        }

        IProject project = resource.getProject();

        try {
            boolean hasNature = project.hasNature(DefaultNature.NATURE_ID);
            if (logger.isDebugEnabled()) {
                logger.debug("Project '" + project.getName() + "' is " + (hasNature ? Constants.EMPTY_STRING : "not")
                        + " " + Constants.PLUGIN_NAME + " managed");
            }
            return hasNature;
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to determine if project is " + Constants.PLUGIN_NAME + " managed with exception: "
                    + logMessage);
            return false;
        }
    }

    public boolean isManagedOnlineProject(IResource resource) {
        if (resource == null || resource.getType() != IResource.PROJECT) {
            return false;
        }

        IProject project = resource.getProject();

        try {
            boolean hasNature = project.hasNature(OnlineNature.NATURE_ID);
            if (logger.isDebugEnabled()) {
                logger.debug("Project '" + project.getName() + "' is " + (hasNature ? Constants.EMPTY_STRING : "not")
                        + " " + Constants.PLUGIN_NAME + " managed");
            }
            return hasNature;
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to determine if project is " + Constants.PLUGIN_NAME + " managed: " + logMessage);
            return false;
        }
    }

    /**
     * @return true is the resource is in a managed project (that has an force nature )
     */
    public boolean isInManagedProject(IResource resource) {
        if (resource == null
                || (resource.getType() != IResource.PROJECT && resource.getType() != IResource.FOLDER && resource
                        .getType() != IResource.FILE)) {
            return false;
        }

        IProject project = resource.getProject();
        if (!project.isOpen()) {
            return false;
        }

        try {
            return project.hasNature(DefaultNature.NATURE_ID);
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to determine if resource '" + resource.getName() + "' is force managed: " + logMessage);
            return false;
        }
    }

    public boolean isSourceFolder(IResource resource) {
        if (resource == null || resource.getType() != IResource.FOLDER) {
            return false;
        }

        return isSourceFolder((IFolder) resource);
    }

    public boolean isSourceFolder(IFolder folder) {
        if (folder == null || folder.getType() != IResource.FOLDER) {
            return false;
        }

        if (!isInManagedProject(folder)) {
            return false;
        }

        String folderName = folder.getName();

        if (folderName.equals(Constants.SOURCE_FOLDER_NAME) && isAtProjectRoot(folder)) {
            return true;
        }

        return false;
    }

    public boolean isSourceResource(IResource resource) {
        if (resource == null) {
            return false;
        }

        if (!isInManagedProject(resource)) {
            return false;
        }

        if (resource.getType() == IResource.FOLDER && isSourceFolder(resource)) {
            return true;
        }

        if (resource.getProjectRelativePath().toPortableString().startsWith(Constants.SOURCE_FOLDER_NAME)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Resource '" + resource.getProjectRelativePath().toPortableString()
                        + "' is a package resource");
            }
            return true;
        }

        return false;
    }

    public boolean hasPackageManifest(IResource resource) {
        if (resource == null || !resource.exists()) {
            return false;
        }

        IFile packageManifest =
                resource.getProject()
                        .getFile(Constants.SOURCE_FOLDER_NAME + "/" + Constants.PACKAGE_MANIFEST_FILE_NAME);
        return packageManifest != null && packageManifest.exists();
    }

    public boolean isPackageFolder(IResource resource) {
        if (resource == null || resource.getType() != IResource.FOLDER) {
            return false;
        }

        return isPackageFolder((IFolder) resource);
    }

    public boolean isPackageFolder(IFolder folder) {
        if (folder == null || folder.getType() != IResource.FOLDER) {
            return false;
        }

        if (!isInManagedProject(folder)) {
            return false;
        }

        IResource[] members = null;
        try {
            members = folder.members();
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to get members of folder '" + folder.getProjectRelativePath().toPortableString()
                    + "': " + logMessage);
        }

        if (null == members || 0 == members.length) {
            if (logger.isInfoEnabled()) {
                logger.info("Package manifest not found in folder '"
                        + folder.getProjectRelativePath().toPortableString() + "'");
            }
            return false;
        }

        for (IResource tmpResource : members) {
            if (tmpResource.getType() == IResource.FILE
                    && Constants.PACKAGE_MANIFEST_FILE_NAME.equals(tmpResource.getName())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found '" + Constants.PACKAGE_MANIFEST_FILE_NAME + "' in folder '"
                            + folder.getProjectRelativePath().toPortableString() + "'");
                }
                return true;
            }
        }

        return false;
    }

    public boolean isReferencedPackagesFolder(IResource resource) {
        if (resource == null || resource.getType() != IResource.FOLDER) {
            return false;
        }

        return isReferencedPackagesFolder((IFolder) resource);
    }

    public boolean isReferencedPackagesFolder(IFolder folder) {
        if (folder == null || folder.getType() != IResource.FOLDER) {
            return false;
        }

        if (!isInManagedProject(folder)) {
            return false;
        }

        IResource parentResource = folder.getParent();
        if (parentResource != null && Constants.REFERENCED_PACKAGE_FOLDER_NAME.equals(folder.getName())
                && parentResource.getType() == IResource.PROJECT) {
            return true;
        }

        if (parentResource == null || !Constants.REFERENCED_PACKAGE_FOLDER_NAME.equals(parentResource.getName())) {
            if (logger.isDebugEnabled()) {
                logger.debug("'" + Constants.REFERENCED_PACKAGE_FOLDER_NAME + "' is not parent of '"
                        + folder.getProjectRelativePath().toPortableString() + "'");
            }
            return false;
        }

        return true;
    }

    /** Individual package folder under "Reference Packages" folder */
    public boolean isReferencedPackageFolder(IResource resource) {
        if (resource == null || resource.getType() != IResource.FOLDER) {
            return false;
        }

        return isReferencedPackageFolder((IFolder) resource);
    }

    /** Individual package folder under "Reference Packages" folder */
    public boolean isReferencedPackageFolder(IFolder folder) {
        if (folder == null || folder.getType() != IResource.FOLDER) {
            return false;
        }

        if (!isInManagedProject(folder)) {
            return false;
        }

        IResource parentResource = folder.getParent();
        if (parentResource == null || !isReferencedPackagesFolder(parentResource)) {
            return false;
        }

        return true;
    }

    public boolean isReferencedPackageResource(IResource resource) {
        if (resource == null) {
            return false;
        }

        if (!isInManagedProject(resource)) {
            return false;
        }

        if (resource.getType() == IResource.FOLDER && isReferencedPackagesFolder(resource)) {
            return true;
        }

        if (resource.getProjectRelativePath().toPortableString().contains(Constants.REFERENCED_PACKAGE_FOLDER_NAME)) {
            return true;
        }

        return false;
    }

    public boolean isSubComponentFolder(IResource resource) {
        if (resource == null || resource.getType() != IResource.FOLDER) {
            return false;
        }
        return isSubComponentFolder((IFolder) resource);
    }

    public boolean isSubComponentFolder(IFolder folder) {
        return folder.getParent().getType() == IResource.FOLDER && isComponentFolder((IFolder) folder.getParent());
    }

    public boolean isComponentFolder(IResource resource) {
        if (resource == null || resource.getType() != IResource.FOLDER) {
            return false;
        }

        return isComponentFolder((IFolder) resource);
    }

    public boolean isComponentFolder(IFolder folder) {
        if (folder == null || folder.getType() != IResource.FOLDER) {
            return false;
        }

        if (!isInManagedProject(folder)) {
            return false;
        }

        IResource parentFolder = folder.getParent();
        if (parentFolder == null || parentFolder.getType() != IResource.FOLDER || !isSourceFolder(parentFolder)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Parent resource of folder name '" + folder.getProjectRelativePath().toPortableString()
                        + "' is not a src folder - folder deemed not a component folder");
            }
            return false;
        }

        String folderName = folder.getName();
        Component component = null;
        component = getComponentFactory().getComponentByFolderName(folderName);

        if (component != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Component found for folder name '" + folderName + "'");
            }
            return true;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Component not found for folder name '" + folderName + "'");
        }

        return false;
    }

    public boolean isPackageManifestFile(IResource resource) {
        if (resource == null || resource.getType() != IResource.FILE) {
            return false;
        }

        if (!isInManagedProject(resource)) {
            return false;
        }

        if (resource.getName().equals(Constants.PACKAGE_MANIFEST_FILE_NAME)) {
            if (logger.isDebugEnabled()) {
                logger.debug("File '" + resource.getProjectRelativePath().toPortableString()
                        + "' is a package manifest file");
            }
            return true;
        }

        return false;
    }

    public boolean isDefaultPackageManifestFile(IResource resource) {
        if (resource == null || resource.getType() != IResource.FILE) {
            return false;
        }

        if (!isInManagedProject(resource)) {
            return false;
        }

        if (resource.getProjectRelativePath().toPortableString()
                .endsWith(Constants.DEFAULT_PACKAGED_NAME + "/" + Constants.PACKAGE_MANIFEST_FILE_NAME)) {
            if (logger.isDebugEnabled()) {
                logger.debug("File '" + resource.getProjectRelativePath().toPortableString()
                        + "' is the defauult package manifest file");
            }
            return true;
        }

        return false;
    }

    /**
     * @return true is the resource is at the root of a project
     */
    private static boolean isAtProjectRoot(IResource resource) {
        return resource.getParent() instanceof IProject;
    }

    public boolean hasManagedComponents(List<IResource> resources) {
        if (Utils.isEmpty(resources)) {
            return false;
        }

        for (IResource resource : resources) {
            if (hasManagedComponents(resource)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasManagedComponents(IResource resource) {
        if (resource == null) {
            return false;
        }

        if (resource.getType() == IResource.FILE) {
            return isManagedFile(resource);
        }

        IFolder rootFolder = null;
        if (resource.getType() == IResource.PROJECT) {
            rootFolder = getSourceFolder(resource.getProject());
            if (rootFolder == null || !rootFolder.exists()) {
                return false;
            }
        } else {
            rootFolder = (IFolder) resource;
        }

        ComponentList componentList = null;
        try {
            componentList = getComponentsForFolder(rootFolder, true, true);
        } catch (Exception e) {
            logger.error("Unable to get components for source folder");
            return false;
        }

        if (Utils.isEmpty(componentList)) {
            return false;
        }

        return true;
    }

    public boolean isBuildableResource(IResource resource) {
        // why isn't the formatter wrapping this???
        return (resource.getType() == IResource.FILE
                && !resource.getProjectRelativePath().toPortableString().contains(Constants.PROJECT_SETTINGS_DIR)
                && !resource.getProjectRelativePath().toPortableString().contains(Constants.PROJECT_FILE)
                /* not sure how else to do this, because isHidden() doesn't work for "." files and directories */
                && !resource.getProjectRelativePath().toPortableString().contains(Constants.SVN_DIR)
                && !resource.getProjectRelativePath().toPortableString().endsWith(Constants.SCHEMA_FILENAME)
                && isManagedResource(resource) 
                && !isPackageManifestFile(resource));
    }

    // F I L E   P R O P E R T I E S

    protected int getInt(IProject project, String propertyName, int defaultvalue) {
        IEclipsePreferences node = getPreferences(project);
        return node != null ? node.getInt(propertyName, defaultvalue) : defaultvalue;
    }

    protected boolean getBoolean(IProject project, String propertyName, boolean defaultvalue) {
        IEclipsePreferences node = getPreferences(project);
        return node != null ? node.getBoolean(propertyName, defaultvalue) : defaultvalue;
    }

    protected void setInt(IProject project, String propertyName, int value) {
        IEclipsePreferences node = getPreferences(project);
        if (node != null) {
            node.putInt(propertyName, value);
            try {
                node.flush();
            } catch (BackingStoreException e) {
                // TODO: no error, just a log
                Utils.openError(new InvocationTargetException(e), true, "Unable to set int.");
            }
        }
    }

    protected void setBoolean(IProject project, String propertyName, boolean value) {
        IEclipsePreferences node = getPreferences(project);
        if (node != null) {
            node.putBoolean(propertyName, value);
            try {
                node.flush();
            } catch (BackingStoreException e) {
                // TODO: no error, just a log
                Utils.openError(new InvocationTargetException(e), true, "Unable to set boolean.");
            }
        }
    }

    protected void setString(IProject project, String propertyName, String value) {
        IEclipsePreferences node = getPreferences(project);
        if (node != null) {
            node.put(propertyName, (value != null ? value : Constants.EMPTY_STRING));
            try {
                node.flush();
            } catch (BackingStoreException e) {
                // TODO: no error, just a log
                Utils.openError(new InvocationTargetException(e), true, "Unable to set string.");
            }
        }
    }

    protected String getString(IProject project, String key, String defaultvalue) {
        IEclipsePreferences node = getPreferences(project);
        return node != null ? node.get(key, (defaultvalue != null ? defaultvalue : Constants.EMPTY_STRING))
                : defaultvalue;
    }

    protected IEclipsePreferences getPreferences(IProject project) {
        ProjectScope projectScope = new ProjectScope(project);
        IEclipsePreferences node = projectScope.getNode(ForceIdeCorePlugin.getPluginId());
        return node;
    }

    public ForceProject getForceProject(IProject project) {
        ForceProject forceProject = new ForceProject();
        IEclipsePreferences preferences = getPreferences(project);
        if (preferences == null) {
            return forceProject;
        }
        forceProject.setProject(project);

        forceProject.setUserName(preferences.get(Constants.PROP_USERNAME, null));
        forceProject.setNamespacePrefix(preferences.get(Constants.PROP_NAMESPACE_PREFIX, null));
        forceProject.setSessionId(preferences.get(Constants.PROP_SESSION_ID, null));

        // previous versions (<154) stored the full endpoint
        String endpoint = preferences.get(Constants.PROP_ENDPOINT, null);
        String endpointServer = preferences.get(Constants.PROP_ENDPOINT_SERVER, null);

        if (Utils.isNotEmpty(endpoint) && Utils.isEmpty(endpointServer)) {
            endpointServer = Utils.getServerNameFromUrl(endpoint);
        }

        forceProject.setPackageName(preferences.get(Constants.PROP_PACKAGE_NAME, null));
        forceProject.setEndpointServer(endpointServer);
        forceProject.setEndpointEnvironment(preferences.get(Constants.PROP_ENDPOINT_ENVIRONMENT, null));
        forceProject.setEndpointApiVersion(preferences.get(Constants.PROP_ENDPOINT_API_VERSION,
            getLastSupportedEndpointVersion()));
        forceProject.setMetadataFormatVersion(preferences.get(Constants.PROP_METADATA_FORMAT_VERSION,
            getLastSupportedEndpointVersion()));
        forceProject.setIdeVersion(preferences.get(Constants.PROP_IDE_VERSION, Constants.EMPTY_STRING));
        forceProject.setProjectIdentifier(preferences.get(Constants.PROP_PROJECT_IDENTIFIER, Constants.EMPTY_STRING));

        forceProject.setKeepEndpoint(preferences.getBoolean(Constants.PROP_KEEP_ENDPOINT, false));
        forceProject.setPreferToolingDeployment(preferences.getBoolean(Constants.PROP_PREFER_TOOLING_DEPLOYMENT, true));
        forceProject.setHttpsProtocol(preferences.getBoolean(Constants.PROP_HTTPS_PROTOCOL, true));
        forceProject.setReadTimeoutSecs(preferences.getInt(Constants.PROP_READ_TIMEOUT,
            Constants.READ_TIMEOUT_IN_SECONDS_DEFAULT));

        if (Utils.isEmpty(forceProject.getEndpointServer())) {
            logger.warn("Unable to get authorization info - endpoint is null or empty");
            return forceProject;
        }

        URL url = null;
        try {
            url = new URL(AUTH_URL);
        } catch (MalformedURLException e) {
            logger.error("Invalid URL ", e);
        }

        Map<String, String> credentialMap = getAuthorizationService().getCredentialMap(url, project.getName(), AUTH_TYPE);
        if (credentialMap != null) {
            String password = credentialMap.get(Constants.PROP_PASSWORD);
            String token = credentialMap.get(Constants.PROP_TOKEN);
            // identification of old storage
            if (password.equals("") && token.equals("")) {
                Map<String, String> oldCrendtialMap = migrateOldAuthInfoAndGetNewCredentials(url, project, AUTH_TYPE);
                if (oldCrendtialMap != null) {
                    password = oldCrendtialMap.get(Constants.PROP_PASSWORD);
                    token = oldCrendtialMap.get(Constants.PROP_TOKEN);
                }
            }
            forceProject.setPassword(password);
            forceProject.setToken(token);
        }

        defaultApiVersionCheck(forceProject);
        defaultServerEndpointCheckandFix(forceProject);
        
        return forceProject;
    }

    @SuppressWarnings("deprecation")
    private Map<String, String> migrateOldAuthInfoAndGetNewCredentials(URL url, IProject project, String authType) {
        //get the existing password and security token
        Map<String, String> authorizationInfo = Platform.getAuthorizationInfo(url, project.getName(), authType);
        //This adds the authorization information to new migrated project using default mechanism
        if (authorizationInfo != null) {
            getAuthorizationService().addAuthorizationInfo(url.toString(), project, authType, authorizationInfo);
            try {
                Platform.flushAuthorizationInfo(url, project.getName(), authType);
            } catch (CoreException e) {
                logger.error("Unable to delete old preferences", e);
            }
            return authorizationInfo;
        }
        return null;
    }

    
    /*
     * Accessor for SalesforceEndpoints
     */
    protected SalesforceEndpoints getSalesforceEndpoints() {
        return ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getSalesforceEndpoints();
    }
    
  
    /*
     * Set to Default Server authentication server endpoint endpoint if current setting is [Label] Default server endpoint.
     * This is to support deprecation of www.salesforce.com as authentication endpoint and only login.Salesforce.com being supported as of 200+
     */
    private void defaultServerEndpointCheckandFix(ForceProject forceProject){
    	
        // Upgrade endpoint if the default (Production/Developer endpoint label is already selected 
        String lastEnvironmentSelected = ForceIdeCorePlugin.getPreferenceString(Constants.LAST_ENV_SELECTED);
        String sProdServer = getSalesforceEndpoints().getEndpointServerForLabel(lastEnvironmentSelected);
        if (Utils.isNotEmpty(sProdServer)&& lastEnvironmentSelected.equals(getSalesforceEndpoints().getDefaultEndpointLabel())){
        	forceProject.setEndpointServer(getSalesforceEndpoints().getEndpointServerForLabel(getSalesforceEndpoints().getDefaultEndpointLabel()));
        	saveForceProject(forceProject);
        }
        
        if (logger.isInfoEnabled()) {
            logger.info("Remove all cached connections and objects related to unsupported API version");
        }
    }
  
    
    
    
    private void defaultApiVersionCheck(ForceProject forceProject) {
        String endpointApiVesion = forceProject.getEndpointApiVersion();
        String lastSupportEndpointVersion = getLastSupportedEndpointVersion();
        if (Utils.isNotEmpty(endpointApiVesion) && endpointApiVesion.equals(lastSupportEndpointVersion)) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder("API version '");
        stringBuilder.append(endpointApiVesion)
                .append("' is not supported by this Force.com IDE release. Project will be updated to ")
                .append("latest supported API version: ").append(lastSupportEndpointVersion);
        logger.warn(stringBuilder.toString());

        getMetadataFactory().removeMetadataStubExt(forceProject);
        getConnectionFactory().removeConnection(forceProject);
        getConnectionFactory().getDescribeObjectRegistry().remove(forceProject.getProject().getName());
        forceProject.setEndpointApiVersion(lastSupportEndpointVersion);
        saveForceProject(forceProject);

        if (logger.isInfoEnabled()) {
            logger.info("Remove all cached connections and objects related to unsupported API version");
        }
    }
    
 
    public void saveForceProject(ForceProject forceProject) {
        saveForceProject(forceProject.getProject(), forceProject);
    }

    public void saveForceProject(IProject project, ForceProject forceProject) {
        setString(project, Constants.PROP_USERNAME, forceProject.getUserName());
        setString(project, Constants.PROP_NAMESPACE_PREFIX, forceProject.getNamespacePrefix());
        
        setString(project, Constants.PROP_SESSION_ID, forceProject.getSessionId());
        
        setString(project, Constants.PROP_PACKAGE_NAME, forceProject.getPackageName());
        setString(project, Constants.PROP_ENDPOINT_SERVER, forceProject.getEndpointServer());
        setString(project, Constants.PROP_ENDPOINT_ENVIRONMENT, forceProject.getEndpointEnvironment());
        setString(project, Constants.PROP_ENDPOINT_API_VERSION, forceProject.getEndpointApiVersion());
        setString(project, Constants.PROP_METADATA_FORMAT_VERSION, forceProject.getMetadataFormatVersion());
        setString(project, Constants.PROP_IDE_VERSION, forceProject.getIdeVersion());
        setString(project, Constants.PROP_PROJECT_IDENTIFIER, forceProject.getProjectIdentifier());
        setBoolean(project, Constants.PROP_KEEP_ENDPOINT, forceProject.isKeepEndpoint());
        setBoolean(project, Constants.PROP_HTTPS_PROTOCOL, forceProject.isHttpsProtocol());
        setBoolean(project, Constants.PROP_PREFER_TOOLING_DEPLOYMENT, forceProject.getPreferToolingDeployment());
        setInt(project, Constants.PROP_READ_TIMEOUT, forceProject.getReadTimeoutSecs());

        Map<String, String> credentialMap = new HashMap<>();
        credentialMap.put(Constants.PROP_PASSWORD, forceProject.getPassword());
        credentialMap.put(Constants.PROP_TOKEN, forceProject.getToken());

        getAuthorizationService().addAuthorizationInfo(AUTH_URL, project, AUTH_TYPE, credentialMap);
    }

    public int getReadTimeout(IProject project) {
        return getInt(project, Constants.PROP_READ_TIMEOUT, Constants.READ_TIMEOUT_IN_SECONDS_DEFAULT);
    }

    public int getReadTimeoutInMilliSeconds(IProject project) {
        return (getInt(project, Constants.PROP_READ_TIMEOUT, Constants.READ_TIMEOUT_IN_SECONDS_DEFAULT) * Constants.SECONDS_TO_MILISECONDS);
    }
    
    public String getSessionId(IProject project){
    	return getString(project, Constants.PROP_SESSION_ID, Constants.EMPTY_STRING);
    }

    public String getUsername(IProject project) {
        return getString(project, Constants.PROP_USERNAME, Constants.EMPTY_STRING);
    }

    public String getPassword(IProject project) {
        return getAuthorizationService().getPassword(project, AUTH_URL, AUTH_TYPE);
    }

    public boolean hasCredentials(IProject project) {
        return Utils.isNotEmpty(getUsername(project)) && Utils.isNotEmpty(getPassword(project));
    }

    public String getPackageName(IProject project) {
        String packageName = getString(project, Constants.PROP_PACKAGE_NAME, Constants.EMPTY_STRING);
        if (Utils.isEmpty(packageName)) {
            // attempt to infer via package.xml
            try {
                packageName = getPackageManifestFactory().getPackageNameFromPackageManifest(project);
            } catch (FactoryException e) {
                logger.warn("Unable to get package name from project's package manifest");
            }
        }

        return Utils.isNotEmpty(packageName) ? packageName : Constants.DEFAULT_PACKAGED_NAME;
    }

    public String getNamespacePrefix(IProject project) {
        return getString(project, Constants.PROP_NAMESPACE_PREFIX, Constants.EMPTY_STRING);
    }

    public String getSoapEndPoint(IProject project) {
        return getString(project, Constants.PROP_ENDPOINT_SERVER, Constants.EMPTY_STRING);
    }

    public String getIdeVersion(IProject project) {
        return getString(project, Constants.PROP_IDE_VERSION, Constants.EMPTY_STRING);
    }

    public void setIdeVersion(IProject project, String ideVersion) {
        setString(project, Constants.PROP_IDE_VERSION, ideVersion);
    }

    public String getInstalledIdeVersion() {
        String version = ForceIdeCorePlugin.getBundleVersion(true);
        if (version.split("\\.").length > 2) {
            version = version.substring(0, version.lastIndexOf("."));
        }

        return version;
    }

    public void updateIdeVersion(IProject project) {
        updateIdeVersion(project, getInstalledIdeVersion());
    }

    public void updateIdeVersion(IProject project, String version) {
        if (Utils.isEmpty(version) || version.indexOf(".") < 1) {
            version = ForceIdeCorePlugin.getBundleVersion(true);
        }

        if (version.split("\\.").length > 2) {
            version = version.substring(0, version.lastIndexOf("."));
        }

        setString(project, Constants.PROP_IDE_VERSION, version);
        setString(project, Constants.PROP_METADATA_FORMAT_VERSION, version);
        setString(project, Constants.PROP_ENDPOINT_API_VERSION, version);

        if (logger.isDebugEnabled()) {
            logger.debug("Updated project's ide version to " + version);
        }
    }

    public boolean isProjectUpgradeable(IProject project) {
        return Utils.isEqual(getIdeVersion(project), getInstalledIdeVersion());
    }

    // R E S O U R C E  H E L P E R S

    public IProject getProject(ISelection selection) {
        if (selection == null || selection.isEmpty() || selection instanceof IStructuredSelection == false) {
            return null;
        }

        IStructuredSelection ss = (IStructuredSelection) selection;
        Object obj = ss.getFirstElement();
        if (obj == null || obj instanceof IResource == false) {
            return null;
        }

        IResource resource = (IResource) obj;
        return resource.getProject();
    }

    public IFile saveToFile(IFile file, String content, IProgressMonitor monitor) throws CoreException {

        if (file == null || file.getType() != IResource.FILE || Utils.isEmpty(content)) {
            logger.warn("Unable to save file - file and/or content is null or empty");
            return null;
        }

        // save or update contents
        try (final QuietCloseable<ByteArrayInputStream> c = QuietCloseable.make(new ByteArrayInputStream(content.getBytes()))) {
            final ByteArrayInputStream stream = c.get();

            if (file.exists()) {
                file.setContents(stream, true, true, new SubProgressMonitor(monitor, 1));
            } else {
                file.create(stream, true, new SubProgressMonitor(monitor, 1));
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Saved file [" + content.length() + "] to file '"
                    + file.getProjectRelativePath().toPortableString() + "'");
        }
        return file;
    }

    // H A N D L E   R E T U R N E D   C O N T E N T   &   M E S S A G E S
    public boolean handleDeployResult(ProjectPackageList projectPackageList, DeployResultExt deployResultHandler,
            boolean save, IProgressMonitor monitor) throws CoreException, InterruptedException, IOException,
            Exception {
        if (deployResultHandler == null || Utils.isEmpty(projectPackageList)) {
            throw new IllegalArgumentException("Project package list and/or deploy result cannot be null");
        }

        monitorCheck(monitor);
        monitorSubTask(monitor, "Handling save result...");

        List<IResource> resources = projectPackageList.getAllComponentResources(false);
        if (deployResultHandler.isSuccess()) {
            if (logger.isInfoEnabled()) {
                logger.info("Save succeeded!");
            }
            MarkerUtils.getInstance().clearDirty(resources.toArray(new IResource[resources.size()]));
        } else {
            logger.warn("Save failed!");
            MarkerUtils.getInstance().applyDirty(resources.toArray(new IResource[resources.size()]));
        }

        // clear all existing save markers on deployed resources
        MarkerUtils.getInstance().clearSaveMarkers(projectPackageList.getAllComponentResources(false).toArray(new IResource[0]));

        DeployMessageExtractor messageExtractor = new DeployMessageExtractor(deployResultHandler);

        handleDeployErrorMessages(projectPackageList, messageExtractor.getDeployFailures(), monitor);

        // retrieve result will clear markers for successfully saved files, so it must be
        // done before adding the deploy warning messages
        handleRetrieveResult(projectPackageList, deployResultHandler.getRetrieveResultHandler(), save, monitor);

        handleDeployWarningMessages(projectPackageList, messageExtractor.getDeployWarnings(), monitor);

        handleRunTestResult(projectPackageList, deployResultHandler.getRunTestsResultHandler(), monitor);

        monitorWork(monitor);

        // represents rolled up status of retrieve, test, etc events
        return deployResultHandler.isSuccess();
    }

    public void handleDeployWarningMessages(ProjectPackageList projectPackageList,
            Collection<DeployMessage> deployWarnings, IProgressMonitor monitor) throws InterruptedException {
        if (deployWarnings.size() == 0) {
            if (logger.isInfoEnabled()) {
                logger.info("No deploy warnings found");
            }
        } else {
            monitorSubTask(monitor, "Evaluating warning messages...");

            for (DeployMessage warningMessage : deployWarnings) {
                monitorCheck(monitor);
                Component component = projectPackageList.getComponentByMetadataFilePath(warningMessage.getFileName());
                applySaveWarningMarker(component.getFileResource(), warningMessage);
                applyWarningsToAssociatedComponents(projectPackageList, component);
            }
        }
    }

    public void handleDeployErrorMessages(
        ProjectPackageList projectPackageList,
        Collection<DeployMessage> errorMessages,
        IProgressMonitor monitor) throws InterruptedException {
        if (Utils.isEmpty(projectPackageList)) {
            throw new IllegalArgumentException("Project package list and/or message handler cannot be null");
        }
        
        DeployErrorHandler.clearAuraMarkers(getSourceFolder(projectPackageList.getProject()));
        
        if (errorMessages.size() == 0) {
            logger.info("No deploy errors found");
        } else {
            monitorSubTask(monitor, "Evaluating error messages...");

            DeployErrorHandler auraHandler = new DeployErrorHandler();
            for (DeployMessage deployMessage : errorMessages) {
                monitorCheck(monitor);
                
                if (DeployErrorHandler.shouldHandle(deployMessage)) {
                    auraHandler.applySaveErrorMarker(deployMessage, getSourceFolder(projectPackageList.getProject()));
                } else {
                    Component component =
                        projectPackageList.getComponentByMetadataFilePath(deployMessage.getFileName());
                    if (component == null) {
                        handleMessageForNullComponent(projectPackageList, deployMessage);
                    } else {
                        applySaveErrorMarker(component.getFileResource(), deployMessage);
                        applyWarningsToAssociatedComponents(projectPackageList, component);
                    }
                }
            }
            monitorWork(monitor);
        }
    }
    
    private void handleMessageForNullComponent(ProjectPackageList projectPackageList, DeployMessage deployMessage) {
        logger.warn(
            "Unable to handle deploy message - could not find component '" 
            + deployMessage.getFileName()
            + "' in list.  Will attempt to find w/in project");
                
        IFile file = getComponentFileForFilePath(projectPackageList.getProject(), deployMessage.getFileName());
        if (file != null) {
            if (deployMessage.isSuccess()) {
                logger.info(deployMessage.getFileName() + " successfully saved");
                MarkerUtils.getInstance().clearSaveMarkers(file);
            } else {
                applySaveErrorMarker(file, deployMessage);
            }
        } else {
            // didn't find associated resource
            logger.warn(
                "Unable to get file resource for '" 
                + deployMessage.getFileName() 
                + "' for message "
                + deployMessage.getProblem());
        }
    }

    private void applySaveErrorMarker(IResource resource, DeployMessage deployMessage) {
        if (deployMessage == null) {
            logger.error("Unable to mark resource with deploy message - deploy message is null");
            return;
        }

        MarkerUtils.getInstance().applyDirty(resource);
        boolean componentHasSubType = false;
        try {
            componentHasSubType = (resource instanceof IFile)
                ? getComponentFactory().getComponentFromFile((IFile) resource).hasSubComponentTypes()
                : false;
        } catch (FactoryException e) {
            logger.debug("Unable to determine whether component " + resource.getName() + " has a subtype", e);
        }

        // Bug #221065: display fullname info only for components that have subtypes:
        String problemStr = (componentHasSubType)
            ? deployMessage.getFullName() + " : " + deployMessage.getProblem()
            : deployMessage.getProblem();
            
        MarkerUtils.getInstance().applySaveErrorMarker(
            resource,
            deployMessage.getLineNumber(),
            deployMessage.getColumnNumber(),
            deployMessage.getColumnNumber(),
            problemStr);
    }

    private void applySaveWarningMarker(IResource resource, DeployMessage deployMessage) {
        Preconditions.checkNotNull(deployMessage);

        boolean componentHasSubType = false;
        try {
            componentHasSubType = (resource instanceof IFile)
                ? getComponentFactory().getComponentFromFile((IFile) resource).hasSubComponentTypes()
                : false;
        } catch (FactoryException e) {
            logger.debug("Unable to determine whether component " + resource.getName() + " has a subtype", e);
        }

        // Bug #221065: display fullname info only for components that have subtypes:
        String problemStr = (componentHasSubType)
            ? deployMessage.getFullName() + " : " + deployMessage.getProblem()
            : deployMessage.getProblem();
            
        MarkerUtils.getInstance().applySaveWarningMarker(resource, deployMessage.getLineNumber(),
            deployMessage.getColumnNumber(), deployMessage.getColumnNumber(), problemStr);
    }

    public boolean handleRetrieveResult(RetrieveResultExt retrieveResultHandler, boolean save, IProgressMonitor monitor)
        throws InterruptedException, CoreException, IOException, Exception {
        if (retrieveResultHandler == null) {
            throw new IllegalArgumentException("Retrieve result cannot be null");
        }
        return handleRetrieveResult(retrieveResultHandler.getProjectPackageList(), retrieveResultHandler, save, monitor);
    }

    public boolean handleRetrieveResult(
        ProjectPackageList projectPackageList,
        RetrieveResultExt retrieveResultHandler,
        boolean save,
        IProgressMonitor monitor) throws InterruptedException, CoreException, IOException {
        return handleRetrieveResult(projectPackageList, retrieveResultHandler, save, null, monitor);
    }

    public boolean handleRetrieveResult(
        final ProjectPackageList projectPackageList,
        RetrieveResultExt retrieveResultHandler,
        boolean save,
        final String[] toSaveComponentTypes,
        IProgressMonitor monitor) throws InterruptedException, CoreException, IOException {
        if (projectPackageList == null) {
            throw new IllegalArgumentException("Project package list cannot be null");
        }

        //Can't handler the results if there isn't one.
        if (retrieveResultHandler == null) {
            return false;
        }

        // save results to project
        if (save) {
            if (retrieveResultHandler.getZipFileCount() == 0) {
                logger.warn("Nothing to save to project - retrieve result is empty");
                return true;
            }

            monitorCheckSubTask(monitor, Messages.getString("Components.Generating"));

            // clean then load clean project package list to be saved to project
            projectPackageList.removeAllComponents();
            projectPackageList.generateComponentsForComponentTypes(
                retrieveResultHandler.getZipFile(),
                retrieveResultHandler.getFileMetadataHandler(),
                toSaveComponentTypes,
                monitor);
            retrieveResultHandler.setProjectPackageList(projectPackageList);
            monitorWork(monitor);

            // flag builder to not build and save
            flagSkipBuilder(projectPackageList.getProject());
            monitorCheckSubTask(monitor, Messages.getString("Components.Saving"));

            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    try {
                        projectPackageList.saveResources(toSaveComponentTypes, monitor);
                    } catch (Exception e) {
                        throw new CoreException(
                            new Status(IStatus.ERROR, Constants.FORCE_PLUGIN_PREFIX, 0, e.getMessage(), e));
                    }
                }
            }, null, IResource.NONE, monitor);

            monitorWork(monitor);
        } else {
            logger.warn("Save intentionally skipped; may be handled later downstream by client");
        }

        if (retrieveResultHandler.getMessageHandler() != null) {
            monitorSubTask(monitor, "Applying retrieve result messages to components...");
            handleRetrieveMessages(
                projectPackageList,
                retrieveResultHandler.getMessageHandler(),
                toSaveComponentTypes,
                monitor);
            monitorWork(monitor);
        }

        return true;
    }

    /**
     * @param projectPackageList
     * @param messageHandler
     * @param toSaveComponentTypes
     *            : This param will be value only specific component folder is selected, ex. refresh from server on
     *            layout folder
     * @param monitor
     * @throws InterruptedException
     */
    public void handleRetrieveMessages(ProjectPackageList projectPackageList, RetrieveMessageExt messageHandler,
            String[] toSaveComponentTypes, IProgressMonitor monitor) throws InterruptedException {
        if (messageHandler == null || projectPackageList == null) {
            throw new IllegalArgumentException("Project package list and/or message handler cannot be null");
        }

        monitorCheck(monitor);

        // clear problem marker for specific object type package.xml stanza when object types is specified
        if (Utils.isNotEmpty(toSaveComponentTypes)) {
            IFile packageManifestFile =
                    getPackageManifestFactory().getPackageManifestFile(projectPackageList.getProject());
            MarkerUtils.getInstance().clearRetrieveMarkers(packageManifestFile, toSaveComponentTypes);
        }

        if (Utils.isEmpty(messageHandler.getMessages())) {
            if (logger.isInfoEnabled()) {
                logger.info("No retrieve messages returned");
            }
            return;
        }

        monitorSubTask(monitor, "Evaluating retrieve messages...");

        // loop thru handling each messages and associated resource
        RetrieveMessage[] retrieveMessages = messageHandler.getMessages();
        for (RetrieveMessage retrieveMessage : retrieveMessages) {
            monitorCheck(monitor);

            Component component = projectPackageList.getComponentByMetadataFilePath(retrieveMessage.getFileName());

            if (component == null) {
                logger.warn("Unable to handle retrieve message - could not find component '"
                        + retrieveMessage.getFileName() + "' for message '" + retrieveMessage.getProblem() + "'");
                continue;
            } else if (component.isPackageManifest() && Utils.isNotEmpty(toSaveComponentTypes)) {
                // look for package.xml in f/s because refresh from server at component folder level doesn't included in
                // projectpackagelist
                IFile file =
                        getComponentFileForFilePath(projectPackageList.getProject(), retrieveMessage.getFileName());
                if (file != null) {
                    MarkerUtils.getInstance().applyRetrieveWarningMarker(file, toSaveComponentTypes,
                        retrieveMessage.getProblem());
                } else {
                    logger.warn("Unable to get file resource for '" + retrieveMessage.getFileName() + "' for message "
                            + retrieveMessage.getProblem());
                }
                continue;
            }

            MarkerUtils.getInstance().applyRetrieveWarningMarker(component.getFileResource(),
                retrieveMessage.getProblem());
            applyWarningsToAssociatedComponents(projectPackageList, component);
        }

        monitorWork(monitor);
    }

    // REVIEWME: a deploy message should be sent for aborted source/metadata saves
    private static void applyWarningsToAssociatedComponents(ProjectPackageList projectPackageList, Component component) {
        if (!component.isMetadataComposite()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Applying error message to associated metadata component");
        }

        String compositeComponentFilePath = component.getCompositeMetadataFilePath();
        if (Utils.isEmpty(compositeComponentFilePath)) {
            logger.warn("Unable to handle composite deploy message for '" + component.getMetadataFilePath()
                    + "' - composite filepath is null or empty");
            return;
        }

        Component componentComposite = projectPackageList.getComponentByFilePath(compositeComponentFilePath);

        if (componentComposite == null) {
            logger.warn("Unable to set error marker - composite component is null");
            return;
        }

        MarkerUtils.getInstance().clearDirty(componentComposite.getFileResource());
        MarkerUtils.getInstance().applySaveWarningMarker(componentComposite.getFileResource(),
            Messages.getString("Markers.CompositeNotSave.message"));
    }

    public void handleRunTestResult(ProjectPackageList projectPackageList, RunTestsResultExt runTestResultHandler,
            IProgressMonitor monitor) throws InterruptedException {

        // handle run tests
        handleRunTestMessages(projectPackageList, runTestResultHandler, monitor);

        // handle code coverage warnings
        handleCodeCoverageWarnings(projectPackageList, runTestResultHandler, monitor);
    }

    public void handleRunTestMessages(ProjectPackageList projectPackageList, RunTestsResultExt runTestResultHandler,
            IProgressMonitor monitor) throws InterruptedException {
        if (Utils.isEmpty(projectPackageList)) {
            throw new IllegalArgumentException("Project package list cannot be null");
        }

        monitorCheck(monitor);
        monitorSubTask(monitor, "Evaluating run test results...");

        // clear all existing run test failure markers on deployed resources
        MarkerUtils.getInstance().clearRunTestFailureMarkers(
            projectPackageList.getComponentResourcesForComponentTypes(new String[] { Constants.APEX_CLASS,
                    Constants.APEX_TRIGGER }));

        if (runTestResultHandler == null || runTestResultHandler.getNumFailures() == 0) {
            if (logger.isInfoEnabled()) {
                logger.info("No test failure results found");
            }
            return;
        }

        IRunTestFailureExt[] runTestFailures = runTestResultHandler.getFailures();
        if (logger.isInfoEnabled()) {
            logger.info("Found [" + runTestResultHandler.getNumFailures() + "] run test failures");
        }

        // loop thru handling each messages and associated resource
        for (IRunTestFailureExt runTestFailure : runTestFailures) {
            monitorCheck(monitor);

            // get resource (file, usually) to associate a message/project/failure/warning and displayed in problems
            // view
            Component component =
                    projectPackageList.getComponentByNameType(runTestFailure.getName(), Constants.APEX_CLASS);
            if (component == null) {
                logger.warn("Unable to handle run test message - could not find component '" + runTestFailure.getName()
                        + "' in list.  Will attempt to find w/in project");
                try {
                    IFile file =
                            getComponentFileByNameType(projectPackageList.getProject(), runTestFailure.getName(),
                                new String[] { Constants.APEX_CLASS });

                    if (file != null) {
                        setRunTestFailureMarker(file, runTestFailure);
                    } else if (projectPackageList.getProject() != null) {
                        StringBuffer strBuff = new StringBuffer();
                        strBuff.append("Unable to get file resource for '").append(runTestFailure.getName())
                                .append("' for code coverage warning '").append(runTestFailure.getMessage())
                                .append("'. Assigning failure to project.");
                        logger.warn(strBuff.toString());
                        MarkerUtils.getInstance().applyRunTestFailureMarker(projectPackageList.getProject(),
                            runTestFailure.getMessage());
                    } else {
                        logger.warn("Unable to get file resource for '" + runTestFailure.getName()
                                + "' for run test failure " + runTestFailure.getMessage());
                    }
                } catch (CoreException e) {
                    String logMessage = Utils.generateCoreExceptionLog(e);
                    logger.warn("Unable to get file resource for '" + runTestFailure.getName() + "' for failure "
                            + runTestFailure.getMessage() + ": " + logMessage, e);
                }
                continue;
            }

            MarkerUtils.getInstance().applyDirty(component.getFileResource());
            setRunTestFailureMarker(component.getFileResource(), runTestFailure);
            applyWarningsToAssociatedComponents(projectPackageList, component);

            try {
                component.getFileResource().findMarkers(MarkerUtils.MARKER_RUN_TEST_FAILURE, true,
                    IResource.DEPTH_INFINITE);
            } catch (CoreException e) {
                String logMessage = Utils.generateCoreExceptionLog(e);
                logger.warn("Unable apply run test marker on component resource: " + logMessage, e);
            }
        }

        monitorWork(monitor);

    }

    private void setRunTestFailureMarker(IResource resource, IRunTestFailureExt runTestFailure) {
        if (runTestFailure == null) {
            logger.warn("Unable to set error marker - run test failure is null");
            return;
        }

        ApexCodeLocation apexCodeLocation =
                getLocationFromStackLine(runTestFailure.getName(), runTestFailure.getStackTrace());
        int line = apexCodeLocation != null ? apexCodeLocation.getLine() : 0;
        int charStart = apexCodeLocation != null ? apexCodeLocation.getColumn() : 0;
        int charEnd = charStart + 1;
        MarkerUtils.getInstance().clearRunTestFailureMarkers(resource);
        MarkerUtils.getInstance().applyRunTestFailureMarker(resource, line, charStart, charEnd,
            runTestFailure.getMessage());
    }

    protected ApexCodeLocation getLocationFromStackLine(String name, String stackTrace) {
        if (Utils.isEmpty(name) || Utils.isEmpty(stackTrace)) {
            logger.warn("Unable to get location from stacktrace - name and/or stacktrace is null");
            return null;
        }
        final Pattern pattern =
                Pattern.compile(".*line ([0-9]+?), column ([0-9]+?).*", Pattern.DOTALL | Pattern.MULTILINE
                        | Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(stackTrace);
        matcher.find();
        String line = matcher.group(1);
        String column = matcher.group(2);
        return new ApexCodeLocation(name, line, column);
    }

    public void handleCodeCoverageWarnings(ProjectPackageList projectPackageList,
            RunTestsResultExt runTestResultHandler, IProgressMonitor monitor) throws InterruptedException {
        if (Utils.isEmpty(projectPackageList)) {
            throw new IllegalArgumentException("Project package list cannot be null");
        }
        IProject project = projectPackageList.getProject();

        //clear all markers on the project
        clearAllWarningMarkers(project);

        if (runTestResultHandler == null || Utils.isEmpty(runTestResultHandler.getCodeCoverageWarnings())) {
            if (logger.isInfoEnabled()) {
                logger.info("No code coverage warnings returned");
            }
            return;
        }

        monitorCheck(monitor);
        monitorSubTask(monitor, "Evaluating code coverage warnings...");

        if (logger.isInfoEnabled()) {
            logger.info("Found [" + runTestResultHandler.getCodeCoverageWarnings().length + "] code coverage warnings");
        }

        // loop thru handling each messages and associated resource
        for (ICodeCoverageWarningExt codeCoverageWarning : runTestResultHandler.getCodeCoverageWarnings()) {
            monitorCheck(monitor);

            // if name is not provided, attach warning at the project level
            if (Utils.isEmpty(codeCoverageWarning.getName())) {
                StringBuffer strBuff = new StringBuffer();
                strBuff.append("Unable to get file resource for '").append(codeCoverageWarning.getName())
                        .append("' for code coverage warning '").append(codeCoverageWarning.getMessage())
                        .append("'. Applying warning to project.");
                logger.warn(strBuff.toString());
                applyCodeCoverageWarningMarker(project, codeCoverageWarning.getMessage());
                continue;
            }

            // get resource (file, usually) to associate a message/project/failure/warning and displayed in problems
            // view
            Component component =
                    projectPackageList.getApexCodeComponent(codeCoverageWarning.getName(),
                        codeCoverageWarning.getMessage());

            if (component == null) {
                logger.warn("Unable to handle code coverage warning - could not find component '"
                        + codeCoverageWarning.getName() + "' in list.  Will attempt to find w/in project");
                try {
                    IFile file =
                            getComponentFileByNameType(project, codeCoverageWarning.getName(), new String[] {
                                    Constants.APEX_CLASS, Constants.APEX_TRIGGER });
                    if (file != null) {
                        applyCodeCoverageWarningMarker(file, codeCoverageWarning.getMessage());
                    } else if (project != null) {
                        StringBuffer strBuff = new StringBuffer("Unable to get file resource for '");
                        strBuff.append(codeCoverageWarning.getName()).append("' for code coverage warning '")
                                .append(codeCoverageWarning.getMessage()).append("'. Assigning warning to project.");
                        logger.warn(strBuff.toString());
                        applyCodeCoverageWarningMarker(project, codeCoverageWarning.getMessage());
                    } else {
                        logger.warn("Unable to get file resource for '" + codeCoverageWarning.getName()
                                + "' for code coverage warning " + codeCoverageWarning.getMessage());
                    }
                } catch (Exception e) {
                    logger.error("Unable to get file resource for '" + codeCoverageWarning.getName()
                            + "' for code coverage warning " + codeCoverageWarning.getMessage(), e);
                }
                continue;
            }
            applyCodeCoverageWarningMarker(component.getFileResource(), codeCoverageWarning.getMessage());
        }

        monitorWork(monitor);
    }

    public void clearAllWarningMarkers(IProject project) {
        MarkerUtils.getInstance().clearCodeCoverageWarningMarkers(project);
    }
    
    public void clearAllWarningMarkersFor(IResource resource) {
    	MarkerUtils.getInstance().clearCodeCoverageWarningMarkersFor(resource);
    }

    private void applyCodeCoverageWarningMarker(IResource resource, String message) {
    	clearAllWarningMarkersFor(resource);
        MarkerUtils.getInstance().applyCodeCoverageWarningMarker(resource, message);
    }

    public boolean isResourceInSync(IResource resource, IProgressMonitor monitor) throws CoreException,
            ForceConnectionException, FactoryException, IOException, ServiceException, ForceRemoteException,
            InterruptedException {
        if (resource == null) {
            throw new IllegalArgumentException("IResource cannot be null");
        }

        if (resource.getType() == IResource.PROJECT) {
            return isProjectInSync((IProject) resource, monitor);
        } else if (getProjectService().isReferencedPackageResource(resource)) {
            logger.warn("Resource '" + resource.getName() + "' is not support by in sync check");
            return true;
        } else if (resource.getType() == IResource.FOLDER) {
            return isFolderInSync((IFolder) resource, monitor);
        } else if (resource.getType() == IResource.FILE) {
            return isFileInSync((IFile) resource, monitor);
        } else {
            logger.warn("Resource '" + resource.getName() + "' is not support by in sync check");
            return true;
        }
    }

    /**
     * Check if project contents have been update remotely.
     * 
     * @param project
     * @param monitor
     * @return
     * @throws CoreException
     * @throws ForceConnectionException
     * @throws FactoryException
     * @throws InterruptedException
     * @throws IOException
     * @throws ServiceException
     */
    public boolean isProjectInSync(IProject project, IProgressMonitor monitor) throws CoreException,
            ForceConnectionException, FactoryException, IOException, ServiceException, ForceRemoteException,
            InterruptedException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        // get local and remote components
        ProjectPackageList localProjectPackageList = getProjectService().getProjectContents(project, monitor);

        if (localProjectPackageList == null) {
            logger.warn("Unable to check in sync for project '" + project.getName()
                    + "' - local local project package list is null");
            return true;
        }

        monitorCheck(monitor);
        monitor.subTask("Retrieving remote compontents...");
        RetrieveResultExt retrieveResultExt =
                getPackageRetrieveService().retrieveSelective(localProjectPackageList, true, monitor);

        monitorWork(monitor);

        if (retrieveResultExt == null) {
            logger.warn("Unable to check in sync for project '" + project.getName() + "' - retrieve result is null");
            return false;
        }

        return evaluateLocalAndRemote(localProjectPackageList, retrieveResultExt, monitor);
    }

    public boolean isFolderInSync(IFolder folder, IProgressMonitor monitor) throws CoreException,
            ForceConnectionException, FactoryException, IOException, ServiceException, ForceRemoteException,
            InterruptedException {
        if (folder == null || folder.getProject() == null) {
            throw new IllegalArgumentException("Folder and/or folder's project cannot be null");
        }

        // get local components
        monitorCheck(monitor);
        monitor.subTask("Retrieving local project contents...");
        ProjectPackageList localProjectPackageList = null;
        if (getProjectService().isPackageFolder(folder) || getProjectService().isSourceFolder(folder)) {
            localProjectPackageList = getProjectPackageFactory().loadProjectPackageList(folder, monitor);
        } else if (getProjectService().isComponentFolder(folder) || getProjectService().isSubComponentFolder(folder)) {
            localProjectPackageList = getProjectPackageFactory().loadProjectPackageList(folder, monitor);
        } else if (getProjectService().isReferencedPackageResource(folder)) {
            logger.warn("Folder '" + folder.getName() + "' is not support by in sync check");
            return true;
        }

        monitor.worked(1);

        if (localProjectPackageList == null) {
            logger.warn("Unable to check in sync for folder '" + folder.getName()
                    + "' - local local project package list is null");
            return true;
        }

        localProjectPackageList.setProject(folder.getProject());

        monitorCheck(monitor);
        monitor.subTask("Retrieving remote contents...");
        RetrieveResultExt retrieveResultExt =
                getPackageRetrieveService().retrieveSelective(localProjectPackageList, true, monitor);

        if (retrieveResultExt == null) {
            logger.warn("Unable to check in sync for folder '" + folder.getName() + "' - retrieve result is null");
            return false;
        }

        monitor.worked(1);

        return evaluateLocalAndRemote(localProjectPackageList, retrieveResultExt, monitor);
    }

    public boolean isFileInSync(IFile file, IProgressMonitor monitor) throws ForceConnectionException,
            IOException, ServiceException, ForceRemoteException, InterruptedException {
        if (file == null || file.getProject() == null) {
            throw new IllegalArgumentException("File and/or file's project cannot be null");
        }

        // get local components
        monitorWorkCheck(monitor, "Retrieving local project contents...");
        monitorCheck(monitor);
        ProjectPackageList localProjectPackageList = null;

        Component component = null;
        try {
            component = getComponentFactory().getComponentFromFile(file);
        } catch (FactoryException e) {
            logger.warn("Unable to check in sync for file '" + file.getName() + "' - unable to create component from file");
            return true;
        }

        if (component.isPackageManifest()) {
            logger.warn("Component is a package manifest - skipping as sync file resource");
            return true;
        }

        localProjectPackageList = getProjectPackageListInstance();
        localProjectPackageList.setProject(file.getProject());
        localProjectPackageList.addComponent(component, true);

        monitorWorkCheck(monitor, "Retrieving remote contents...");
        RetrieveResultExt retrieveResultExt = getPackageRetrieveService().retrieveSelective(localProjectPackageList, true, monitor);

        if (retrieveResultExt == null) {
            logger.warn("Unable to check in sync for file '" + file.getName() + "' - retrieve result is null");
            return false;
        }

        return evaluateLocalAndRemote(localProjectPackageList, retrieveResultExt, monitor);
    }

    private boolean evaluateLocalAndRemote(
    		ProjectPackageList localProjectPackageList,
            RetrieveResultExt retrieveResultExt,
            IProgressMonitor monitor) throws InterruptedException, IOException {

        // get remote package list to evaluate
        ProjectPackageList remoteProjectPackageList = factoryLocator.getProjectPackageFactory().getProjectPackageListInstance(
        		localProjectPackageList.getProject(),
                retrieveResultExt.getZipFile(),
                retrieveResultExt.getFileMetadataHandler());

        monitorWork(monitor);
        monitorCheck(monitor);
        // if either local or remote are empty, assume project is out-of-sync
        if ((localProjectPackageList.isEmpty() && remoteProjectPackageList.isNotEmpty())
             || (localProjectPackageList.isNotEmpty() && remoteProjectPackageList.isEmpty())) {
            return false;
        }

        monitorSubTask(monitor, Messages.getString("Components.Evaluating"));
        // test either project package
        for (ProjectPackage localProjectPackage : localProjectPackageList) {
            monitorCheck(monitor);
            // local package does not exist remotely, assume project is out-of-sync
            if (!remoteProjectPackageList.hasPackage(localProjectPackage.getName())) {
                logger.warn("Project package '" + localProjectPackage.getName() + "' does not exist remotely - assuming project is out of sync");
                return false;
            }

            // deep equal check on same-named project package
            ProjectPackage remoteProjectPackage = remoteProjectPackageList.getProjectPackage(localProjectPackage.getName());
            if (!localProjectPackage.hasChanged(remoteProjectPackage)) {
                logger.warn("Project package '" + localProjectPackage.getName() + "' does not jive with remote package - assuming project is out of sync");
                return false;
            }
            monitorCheck(monitor);
        }

        monitorWork(monitor);

        // sweet, project contents are up-to-date
        return true;
    }

    private IAuthorizationService getAuthorizationService() {
        return authService;
    }
}
