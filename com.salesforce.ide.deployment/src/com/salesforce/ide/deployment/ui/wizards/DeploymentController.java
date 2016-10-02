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
package com.salesforce.ide.deployment.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.api.metadata.types.PackageTypeMembers;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.factories.PackageManifestFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.LoggingInfo;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.PackageConfiguration;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.metadata.CustomObjectNameResolver;
import com.salesforce.ide.core.remote.metadata.DeployMessageExt;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.services.DeployException;
import com.salesforce.ide.core.services.PackageDeployService;
import com.salesforce.ide.core.services.RetrieveException;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceLocator;
import com.salesforce.ide.core.services.ServiceTimeoutException;
import com.salesforce.ide.deployment.ForceIdeDeploymentPlugin;
import com.salesforce.ide.deployment.PackageManifest;
import com.salesforce.ide.deployment.internal.DeploymentComponent;
import com.salesforce.ide.deployment.internal.DeploymentComponentSet;
import com.salesforce.ide.deployment.internal.DeploymentPayload;
import com.salesforce.ide.deployment.internal.DeploymentResult;
import com.salesforce.ide.deployment.internal.DeploymentSummary;
import com.salesforce.ide.deployment.internal.utils.DeploymentConstants;
import com.salesforce.ide.ui.internal.utils.ComponentArchiver;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.LogInfo;

public class DeploymentController extends Controller {
    private static final Logger logger = Logger.getLogger(DeploymentController.class);

    private DeploymentPayload deploymentPayload = null;
    private DeploymentResult deploymentResult = null;
    private ComponentList remoteComponentList = null;
    private ProjectPackageList localProjectPackageList = null;
    private ProjectPackageList remoteProjectPackageList = null;

    public DeploymentController(IProject project) {
        super();
        model = new DeploymentWizardModel(project);
    }

    public DeploymentController(IProject project, List<IResource> resources) {
        super();
        model = new DeploymentWizardModel(project, resources);
    }

    @Override
    public void init() throws ForceProjectException {

    }

    public DeploymentWizardModel getDeploymentWizardModel() {
        return (DeploymentWizardModel) model;
    }

    public void setDeploymentWizardModel(DeploymentWizardModel deploymentWizardModel) {
        this.model = deploymentWizardModel;
    }

    public ProjectPackageList getRemoteProjectPackageList() {
        return remoteProjectPackageList;
    }

    public DeploymentPayload getDeploymentPayload() {
        return deploymentPayload;
    }

    public void setDeploymentPayload(DeploymentPayload deploymentPayload) {
        this.deploymentPayload = deploymentPayload;
    }

    public DeploymentResult getDeploymentResult() {
        return deploymentResult;
    }

    public void setDeploymentResult(DeploymentResult deploymentResult) {
        this.deploymentResult = deploymentResult;
    }

    public boolean isDeploymentPayloadEmpty() {
        return deploymentPayload == null || deploymentPayload.isEmpty();
    }

    public boolean isSameAsProjectOrg(String username, String endpoint) {
        if (Utils.isEmpty(username) || Utils.isEmpty(endpoint)) {
            return false;
        }

        ForceProject forceProject = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProject(getProject());
        if (username.equals(forceProject.getUserName()) && endpoint.equals(forceProject.getEndpointServer())) {
            return true;
        }

        return false;
    }

    @Override
    public void dispose() {
        model = null;
    }

    public void testDeploy(IProgressMonitor monitor) throws ForceConnectionException, DeployException,
            FactoryException, Exception, RemoteException, InterruptedException {
        deploymentResult = deploy(true, monitor);
    }

    private DeploymentResult deploy(boolean checkOnly, IProgressMonitor monitor) throws ForceConnectionException,
            DeployException, FactoryException, Exception, InterruptedException {
        return deploy(getDeploymentWizardModel().getDestinationOrg(), deploymentPayload, checkOnly, monitor);
    }

    public void generateArchive(DeploymentComponentSet deploymentComponents, File zipPath, String zipName)
            throws IOException {
        // should already have composite components in deployment set
        ComponentList components = deploymentComponents.getComponents(false);
        generateArchive(components, zipPath, zipName);
    }

    public void generateArchive(ComponentList components, File zipPath, String zipName) throws IOException {
        ComponentArchiver.generateArchive(zipName, zipPath, components);
    }

    public void testConnection() throws ForceConnectionException, InsufficientPermissionsException {
        if (getDeploymentWizardModel().getDestinationOrg() != null) {
            ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(getDeploymentWizardModel().getDestinationOrg());
        }
    }

    public void saveSettings(IProgressMonitor monitor) throws InterruptedException {
        saveArchiveSettings(monitor);
        saveOrgSettings(monitor);
    }

    public void saveArchiveSettings(IProgressMonitor monitor) throws InterruptedException {
        if (getDeploymentWizardModel() == null || getDeploymentWizardModel().getForceProject() == null) {
            throw new IllegalArgumentException("Project model and/or project cannot be null");
        }

        monitorCheck(monitor);

        if (logger.isDebugEnabled()) {
            logger.debug("Saving deployment archive '" + getDeploymentWizardModel().getProjectName() + "' settings");
        }

        ForceProject forceProject = getDeploymentWizardModel().getForceProject();

        if (forceProject == null) {
            logger.warn("Unable to save last entered/select deployment details - force project is null");
            return;
        }

        // save workspace settings
        if (getDeploymentWizardModel().getDestinationArchivePath() != null) {
            ForceIdeDeploymentPlugin.savePreference(DeploymentConstants.LAST_SOURCE_DEPLOYMENT_ARCHIVE_DIR_SELECTED,
                getDeploymentWizardModel().getDestinationArchivePath().toString());
        }

        if (getDeploymentWizardModel().getDestinationArchivePath() != null) {
            ForceIdeDeploymentPlugin.savePreference(DeploymentConstants.LAST_DEST_DEPLOYMENT_ARCHIVE_DIR_SELECTED,
                getDeploymentWizardModel().getDestinationArchivePath().toString());
        }
    }

    public void saveOrgSettings(IProgressMonitor monitor) throws InterruptedException {
        if (getDeploymentWizardModel() == null || getDeploymentWizardModel().getForceProject() == null) {
            throw new IllegalArgumentException("Deployment model and/or force project cannot be null");
        }

        monitorCheck(monitor);

        ForceProject forceProject = getDeploymentWizardModel().getForceProject();

        if (forceProject == null) {
            logger.warn("Unable to save last entered/select deployment details - force project is null");
            return;
        }

        ForceIdeDeploymentPlugin.savePreference(DeploymentConstants.LAST_DEPLOYMENT_ENV_SELECTED,
            getDeploymentWizardModel().getEnvironment());

        ForceIdeDeploymentPlugin.savePreference(DeploymentConstants.LAST_DEPLOYMENT_USERNAME_SELECTED, forceProject
                .getUserName());

        String otherLabel = Messages.getString("ProjectCreateWizard.OrganizationPage.OtherEnvironment.label");
        if (otherLabel.equals(getDeploymentWizardModel().getEnvironment())) {
            ForceIdeDeploymentPlugin
                .savePreference(DeploymentConstants.LAST_DEPLOYMENT_SERVER_SELECTED, forceProject.getEndpointServer());
            ForceIdeDeploymentPlugin.savePreference(
                DeploymentConstants.LAST_DEPLOYMENT_KEEP_ENDPOINT_SELECTED,
                forceProject.isKeepEndpoint());
            ForceIdeDeploymentPlugin
                .savePreference(DeploymentConstants.LAST_DEPLOYMENT_PROTOCOL_SELECTED, forceProject.isHttpsProtocol());
        }
    }

    public void generateDeploymentPayload(IProgressMonitor monitor) throws Exception {
        if (getDeploymentWizardModel() == null || getDeploymentWizardModel().getDestinationOrg() == null) {
            throw new IllegalArgumentException("Project and/or destination connection are not provided.");
        }

        monitorWorkCheck(monitor, "Getting connection...");
        ForceProject destinationProject = getDeploymentWizardModel().getDestinationOrg();
        try {
            monitorWorkCheck(monitor, "Generating deployment payload...");
            DeploymentPayload deploymentPayload =
                    generateDeploymentPayload(getDeploymentWizardModel().getDeployResources(), destinationProject,
                        new SubProgressMonitor(monitor, 4));
            setDeploymentPayload(deploymentPayload);
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }
    }

    public DeploymentPayload generateDeploymentPayload(IResource deployResource, ForceProject destinationProject,
            IProgressMonitor monitor) throws InterruptedException, ForceConnectionException, ForceRemoteException,
            CoreException, FactoryException {
        List<IResource> deployResources = new ArrayList<>(1);
        deployResources.add(deployResource);
        return generateDeploymentPayload(deployResources, destinationProject, monitor);
    }

    /*
     * Inspects project contents and given remote destination org to determine a deployment plan containing deployment
     * candidates and their respective deployment action - new, overwrite, delete, etc.
     */
    public DeploymentPayload generateDeploymentPayload(
        List<IResource> deployResources,
        ForceProject destinationProject,
        IProgressMonitor monitor) throws InterruptedException, ForceConnectionException, ForceRemoteException,
            CoreException, FactoryException {
        if (Utils.isEmpty(deployResources) || destinationProject == null || model.getProject() == null) {
            throw new IllegalArgumentException("Resources, project, and/or destination cannot be null");
        }
        
        // initialize payload container
        DeploymentPayload deploymentPayload = new DeploymentPayload(model.getProject(), deployResources);
        deploymentPayload.setDestinationOrgUsername(destinationProject.getUserName());
        
        monitorCheckSubTask(monitor, "Getting connection to destination organization...");
        Connection connection = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory()
            .getConnection(destinationProject);
        monitorWork(monitor);
        
        monitorCheckSubTask(monitor, "Getting permissible object types for destination organization...");
        String[] enabledComponentTypes = ContainerDelegate.getInstance().getServiceLocator().getMetadataService()
            .getEnabledComponentTypes(connection, true, true);
        if (Utils.isEmpty(enabledComponentTypes)) {
            logger.warn("No object types are enabled for " + connection.getLogDisplay());
            return deploymentPayload;
        }
        monitorWork(monitor);
        
        List<String> remoteEnabledComponentTypes = new ArrayList<>(enabledComponentTypes.length);
        remoteEnabledComponentTypes.addAll(Arrays.asList(enabledComponentTypes));
        
        monitorCheckSubTask(monitor, "Gathering project contents for resource(s)");
        // get local and remote components
        localProjectPackageList = ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .getProjectContents(deployResources, true, monitor);
        localProjectPackageList.setProject(model.getProject());
        monitorWork(monitor);
        
        // there's nothing to deploy if local is empty
        if (localProjectPackageList.isEmpty()) {
            logger.warn("Local package list is empty");
            return deploymentPayload;
        }
        
        //  retrieve remote components
        try {
            loadRemoteProjectPackageList(connection, deployResources, monitor);
        } catch (Exception e) {
            logger.warn("Unable to retrieve remote components for resources", ForceExceptionUtils.getRootCause(e));
        }
        
        // if remote is empty, are components are considered new
        if (Utils.isEmpty(remoteProjectPackageList)) {
            ComponentList componentList = localProjectPackageList.getAllComponents();
            for (Component component : componentList) {
                DeploymentComponent deploymentComponent =
                    createNewDeploymentComponent(component, remoteEnabledComponentTypes);
                deploymentComponent.setRemoteFound(false);
                
                boolean added = deploymentPayload.add(deploymentComponent, true);
                if (added) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Added  " + deploymentComponent.getFullDisplayName() + " to deployment set");
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(deploymentComponent.getDisplayName() + " already exists in deployment set");
                    }
                }
            }
            
        } else {
            ComponentList remoteComponents = remoteProjectPackageList.getAllComponents();
            if (Utils.isNotEmpty(remoteComponents)) {
                deploymentPayload.setRemoteComponentList(remoteComponents);
            }
            
            monitorCheckSubTask(monitor, "Evaluating project and remote components...");
            // test either project package
            for (ProjectPackage localProjectPackage : localProjectPackageList) {
                monitorCheck(monitor);
                DeploymentComponentSet deploymentComponentSet = getDeploymentComponentSetForPackage(
                    localProjectPackage,
                    remoteProjectPackageList,
                    remoteEnabledComponentTypes,
                    monitor);
                deploymentPayload.addAll(deploymentComponentSet);
            }
            monitorWork(monitor);
        }
        
        return deploymentPayload;
    }
    
    protected void loadRemoteProjectPackageList(
        Connection connection,
        List<IResource> deployResources,
        IProgressMonitor monitor) throws ForceConnectionException, ForceRemoteException, InterruptedException,
            FactoryException, CoreException, IOException, ServiceException {
        if (Utils.isEmpty(deployResources)) {
            logger.warn("Unable to get remote content for resources - resources is null or empty");
        }
        
        monitorCheck(monitor);
        monitorSubTask(monitor, "Retrieving remote components...");
        
        remoteProjectPackageList =
            ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageListInstance();
        remoteProjectPackageList.setProject(model.getProject());
        
        // handle if source root was selected
        try {
            handleSourceRetrieve(connection, deployResources, monitor);
            
            // handle source component folders
            // handling sub-src folder selections: some are individual file retrieves and some selections should
            // include new content.  this difference requires that we either query all content and pair-down or make
            // multiple specific, retrieve calls.  the latter was chosen so that we don't bog down the server with large
            // requests and the ide from having to parse large result sets.
            handleSourceComponentFolderRetrieve(connection, deployResources, monitor);
            
            // handle source component files
            handleSourceComponentFileRetrieve(connection, deployResources, monitor);
        } catch (RetrieveException ex) {
            // okay if pkg is not found - in handlePackageRetrieve we inspect if components exists as not packaged
            if (!ForceExceptionUtils.isPackageNotFoundException(ex)) {
                throw ex;
            }
        }
        
        // for package content, see if unpackaged instance exists
        handlePackageRetrieve(connection, deployResources, monitor);
        
        remoteProjectPackageList = flattenBundleComponents();
    }
    
    /*
     * Bundle elements that have been retrieved need to be flattened before we can do an effective comparison
     */
    private ProjectPackageList flattenBundleComponents() {
        ProjectPackageList flattenedPackageList = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageListInstance();
        flattenedPackageList.setProject(model.getProject());
        flattenedPackageList.addComponents(remoteProjectPackageList.getAllComponents(), PackageConfiguration.builder().setReplaceComponent(true).build());
        return flattenedPackageList;
    }

    protected void handleSourceRetrieve(
        Connection connection,
        List<IResource> deployResources,
        IProgressMonitor monitor) throws InterruptedException, ForceConnectionException, ForceRemoteException,
            FactoryException, IOException, ServiceException {
        monitorCheck(monitor);
        if (Utils.isNotEmpty(ContainerDelegate.getInstance().getServiceLocator().getProjectService().getFolder(deployResources, Constants.SOURCE_FOLDER_NAME))) {
            String packageName = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageName(model.getProject());

            // perform retrieve
            RetrieveResultExt retrieveResultHandler = ContainerDelegate.getInstance().getServiceLocator()
                .getPackageRetrieveService().retrievePackage(connection, model.getProject(), packageName, monitor);
                
            if (retrieveResultHandler == null) {
                logger.warn("Unable to retrieve remote components for src folder - retrieve result is null");
                return;
            }

            monitorWork(monitor);

            remoteProjectPackageList.generateComponents(
                retrieveResultHandler.getZipFile(),
                retrieveResultHandler.getFileMetadataHandler());
        }
    }

    protected void handleSourceComponentFolderRetrieve(
        Connection connection,
        List<IResource> deployResources,
        IProgressMonitor monitor) throws InterruptedException, ForceConnectionException, ForceRemoteException,
            FactoryException, CoreException, IOException, ServiceException {
            
        // if only source root was selected was selected, let's end here
        if (deployResources.size() == 1
                && Utils.isNotEmpty(ContainerDelegate.getInstance().getServiceLocator().getProjectService().getFolder(deployResources, Constants.SOURCE_FOLDER_NAME))) {
            return;
        }

        monitorCheck(monitor);
        List<IResource> folders = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getResourcesByType(deployResources, IResource.FOLDER);
        List<String> componentTypes = new ArrayList<>();
        if (Utils.isNotEmpty(folders)) {
            for (IResource folder : folders) {
                if (ContainerDelegate.getInstance().getServiceLocator().getProjectService().isComponentFolder(folder)) {
                    Component component = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getComponentByFolderName(folder.getName());
                    componentTypes.add(component.getComponentType());
                } else if (ContainerDelegate.getInstance().getServiceLocator().getProjectService().isSubComponentFolder(folder)) {
                    Component component = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getComponentFromSubFolder((IFolder) folder, false);
                    componentTypes.add(component.getSecondaryComponentType());
                }
            }

            if (Utils.isNotEmpty(componentTypes)) {
                // refresh component dirs
                ProjectPackageList projectPackageList =
                        ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageFactory().getProjectPackageListInstance(model.getProject());

                // only save these types
                String[] componentTypeArray = componentTypes.toArray(new String[componentTypes.size()]);

                // perform retrieve
                RetrieveResultExt retrieveResultHandler =
                        ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrieveSelective(connection, projectPackageList,
                            componentTypeArray, monitor);

                if (retrieveResultHandler == null) {
                    logger
                        .warn("Unable to retrieve remote components for component folder(s) - retrieve result is null");
                    return;
                }

                monitorWork(monitor);

                remoteProjectPackageList.generateComponents(
                    retrieveResultHandler.getZipFile(),
                    retrieveResultHandler.getFileMetadataHandler());
            }
        }
    }

    // we retrieve using the projects package.xml which designates which package the api will inspect and
    // retrieve from.  if a component of the same name exists in the remote org, but is not in the project's
    // package, we need to mark that the component will be overwritten.  note that a component can be in multiple
    // packages and that packages are basically just tags on components, not necessary self-contained units.
    // for example, assuming it is really <nonamespace>.myApexClass & <nonamespace>.myApexClass is already
    // on the server, it will update <nonamespace>.myApexClass AND add <nonamespace>.myApexClass to the
    // newly created 'mypackage' (it's quite possible <nonamespace>.myApexClass was already a member of
    // another package; that relationship would be unchanged).
    protected void handlePackageRetrieve(
        Connection connection,
        List<IResource> deployResources,
        IProgressMonitor monitor)
            throws InterruptedException, ForceRemoteException, FactoryException, IOException, ServiceException {
            
        // 1.) get list of component not found in remote org - assume components don't exist in package, but might be unpackaged
        // 2.) selectively query for said components
        // 3.) added return components to remoteProjectPackageList
        
        String packageName =
            ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageName(getProject());
        // we're only interested if the project's is package content
        if (Utils.isEmpty(packageName) || Constants.DEFAULT_PACKAGED_NAME.equals(packageName)) {
            return;
        }
        
        monitorCheck(monitor);
        ComponentList componentList = localProjectPackageList.getComponentsNotFound(remoteProjectPackageList);
        
        // all components present in remote org
        if (!Utils.isEmpty(componentList)) {
            // check unpackage content for not-found components
            List<String> componentNames = componentList.getFilePaths(true, false);
            
            RetrieveResultExt retrieveResultHandler =
                ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrieveSelective(
                    connection,
                    componentNames.toArray(new String[componentNames.size()]),
                    Constants.DEFAULT_PACKAGED_NAME,
                    getProject(),
                    monitor);
                    
            if (retrieveResultHandler == null) {
                logger.warn("Unable to retrieve remote components for component file(s) - retrieve result is null");
                return;
            }
            
            monitorWork(monitor);
            
            remoteProjectPackageList.generateComponents(
                retrieveResultHandler.getZipFile(),
                retrieveResultHandler.getFileMetadataHandler());
        }
    }
    
    protected void handleSourceComponentFileRetrieve(
        Connection connection,
        List<IResource> deployResources,
        IProgressMonitor monitor) throws InterruptedException, ForceRemoteException, IOException, ServiceException {
        
        // if only source root was selected was selected, let's end here
        if (deployResources.size() == 1 && Utils.isNotEmpty(
            ContainerDelegate.getInstance().getServiceLocator().getProjectService()
                .getFolder(deployResources, Constants.SOURCE_FOLDER_NAME))) {
            return;
        }
        
        monitorCheck(monitor);
        List<IResource> files = ContainerDelegate.getInstance().getServiceLocator().getProjectService()
            .getResourcesByType(deployResources, IResource.FILE);
        if (Utils.isNotEmpty(files)) {
            monitorCheck(monitor);
            RetrieveResultExt retrieveResultHandler = ContainerDelegate.getInstance().getServiceLocator()
                .getPackageRetrieveService().retrieveSelective(connection, localProjectPackageList, true, monitor);
                
            if (retrieveResultHandler == null) {
                logger.warn("Unable to retrieve remote components for component file(s) - retrieve result is null");
                return;
            }
            
            monitorWork(monitor);
            
            remoteProjectPackageList.generateComponents(
                retrieveResultHandler.getZipFile(),
                retrieveResultHandler.getFileMetadataHandler());
        }
    }
    
    // inspect local vs. remote project package content
    private DeploymentComponentSet getDeploymentComponentSetForPackage(ProjectPackage localProjectPackage,
            ProjectPackageList remoteProjectPackageList, List<String> remoteEnabledComponentTypes,
            IProgressMonitor monitor) throws InterruptedException {
        DeploymentComponentSet deploymentComponentSet = new DeploymentComponentSet();

        // local package does not exist remotely, assume all local components are new
        ProjectPackage remoteProjectPackage =
                remoteProjectPackageList.getProjectPackage(localProjectPackage.getName(), false);
        ComponentList localComponentList = localProjectPackage.getComponentList();

        // no corresponding package found in destination org
        if (remoteProjectPackage == null) {
            // create deployment candidate for each local component
            for (Component localComponent : localComponentList) {

                DeploymentComponent deploymentComponent =
                        createNewDeploymentComponent(localComponent, remoteEnabledComponentTypes);
                deploymentComponent.setRemoteFound(false);

                // if packaged content, check against retrieved non-package stuff to determine if the component
                // exists in the destination, but is not packaged.
                if (!localComponent.isPackageManifest() && Utils.isNotEmpty(localProjectPackage.getName())
                        && !Constants.DEFAULT_PACKAGED_NAME.equals(localProjectPackage.getName())) {
                    Component remoteComponent =
                            remoteProjectPackageList.getComponentByFilePath(localComponent.getMetadataFilePath());

                    if (remoteComponent != null) {
                        initDeploymentComponent(deploymentComponent, localComponent, remoteComponent, monitor);
                        deploymentComponent.setRemoteFound(true);
                    }
                }

               deploymentComponentSet.add(deploymentComponent, false);
            }

        } else {
            // found package in destination org
            // deep equal check on same-named project package
            remoteComponentList = localProjectPackage.getComponentListInstance();
            remoteComponentList.addAll(remoteProjectPackage.getComponentList());

            for (Component localComponent : localComponentList) {
                DeploymentComponent deploymentComponent =
                        getDeploymentComponentForComponent(localComponent, remoteEnabledComponentTypes, monitor);

                deploymentComponentSet.add(deploymentComponent, true);
                monitorCheck(monitor);
            }

            // remaining remote components are considered action-able deletes, but are deploy=false (default)
            if (Utils.isNotEmpty(remoteComponentList)) {
                for (Component remoteComponent : remoteComponentList) {
                    DeploymentComponent deploymentComponent = createDeleteDeploymentComponent(remoteComponent);
                    deploymentComponentSet.add(deploymentComponent, false);
                }
            }
        }

        return deploymentComponentSet;
    }

    // evaluate local component vs. remote component
    private DeploymentComponent getDeploymentComponentForComponent(
        Component localComponent,
        List<String> remoteEnabledComponentTypes,
        IProgressMonitor monitor) throws InterruptedException {
        // create instance containing local deploy candidate
        DeploymentComponent deploymentComponent =
            createNewDeploymentComponent(localComponent, remoteEnabledComponentTypes);
            
        Component remoteComponent = remoteComponentList.getComponentByFilePath(localComponent.getMetadataFilePath());
        initDeploymentComponent(deploymentComponent, localComponent, remoteComponent, monitor);
        
        return deploymentComponent;
    }
    
    private void initDeploymentComponent(DeploymentComponent deploymentComponent, Component localComponent,
            Component remoteComponent, IProgressMonitor monitor) throws InterruptedException {
        if (deploymentComponent == null) {
            return;
        }

        // if not found in remote list, assume new
        if (remoteComponent == null) {
            deploymentComponent.setRemoteFound(false);
        } else if (DeploymentSummary.isDeployable(deploymentComponent.getDestinationSummary())) {
            // if found, compare attributes to identify change
            boolean changed = localComponent.hasEitherChanged(remoteComponent, monitor);
            if (changed) {
                deploymentComponent.setDestinationSummary(DeploymentSummary.UPDATED);
            } else {
                deploymentComponent.setDestinationSummary(DeploymentSummary.NO_CHANGE_OVERWRITE);
                deploymentComponent.setDeploy(false);
            }

            // removed from further consideration
            // remaining artifacts exist remote only and will be handled later
            if (Utils.isNotEmpty(remoteComponentList)) {
                remoteComponentList.remove(remoteComponent);
            }
        }
    }

    private static DeploymentComponent createNewDeploymentComponent(
        Component component,
        List<String> remoteEnabledComponentTypes) {
        DeploymentComponent deploymentComponent = new DeploymentComponent(component);
        // make sure type is permissible in destination and remote add is supported
        if (Utils.isEmpty(remoteEnabledComponentTypes)
            || !remoteEnabledComponentTypes.contains(component.getComponentType())) {
            deploymentComponent.setDestinationSummary(DeploymentSummary.NOT_PERMISSIBLE);
            deploymentComponent.setDeploy(false);
        } else if (!component.isBundle() && (component.getFileResource() == null || !component.getFileResource().exists())) {
            // We usually do not deploy any component without a file resource except for the case of a bundle since we hoist its internals up
            deploymentComponent.setDestinationSummary(DeploymentSummary.RESOURCE_NOT_FOUND);
            deploymentComponent.setDeploy(false);
        } else if (component.isRemoteAdd()) {
            deploymentComponent.setDestinationSummary(DeploymentSummary.NEW);
        } else if (CustomObjectNameResolver.getCheckerForStandardObject().check(component.getName(), component.getComponentType())) {
            deploymentComponent.setDestinationSummary(DeploymentSummary.UPDATED);
        } else {
            deploymentComponent.setDestinationSummary(DeploymentSummary.NEW_NOT_SUPPORTED);
            deploymentComponent.setDeploy(false);
        }
        return deploymentComponent;
    }
    
    private static DeploymentComponent createDeleteDeploymentComponent(Component component) {
        DeploymentComponent deploymentComponent = new DeploymentComponent(component);
        if (component.isRemoteDeleteable()) {
            deploymentComponent.setDestinationSummary(DeploymentSummary.DELETED);
        } else {
            deploymentComponent.setDestinationSummary(DeploymentSummary.DELETE_NOT_SUPPORTED);
        }
        deploymentComponent.setDeploy(false);
        return deploymentComponent;
    }

    @Override
    public void finish(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        // record datetime
        Calendar cal = new GregorianCalendar();
        long datetime = cal.getTimeInMillis();

        // create source archive
        monitorCheck(monitor);
        if (Utils.isNotEmpty(getDeploymentWizardModel().getDestinationOrg().getUserName())) {
            monitorSubTask(monitor, "Generating archive of deployed content...");
            File sourceArchivePath = getDeploymentWizardModel().getSourceArchivePath();
            if (sourceArchivePath != null && sourceArchivePath.exists()) {
                try {
                    String archiveName = getDeploymentWizardModel().getProject().getName() + "-deploy-" + datetime;
                    generateArchive(deploymentPayload.getDeploySelectedComponents(true), sourceArchivePath, archiveName);
                } catch (Exception e) {
                    logger.error("Unable to create destination archive.", e);
                }
            }
            monitorWork(monitor);
        }

        // create destination archive
        monitorCheck(monitor);
        File deploymentArchivePath = getDeploymentWizardModel().getDestinationArchivePath();
        if (deploymentArchivePath != null && deploymentArchivePath.exists()) {
            monitorSubTask(monitor, "Generating archive of remote content...");
            try {
                String archiveName =
                        getDeploymentWizardModel().getDestinationOrg().getUserName() + "-deploy-" + datetime;
                generateArchive(deploymentPayload.getRemoteComponentList(), deploymentArchivePath, archiveName);
            } catch (Exception e) {
                logger.error("Unable to create destination archive.", e);
                // REVIEWME: should we continue w/ deployment if we cannot archive the
                // to-be-overwritten components?
            }
            monitorWork(monitor);
        }

        // go for it!
        try {
            deploymentResult = deploy(false, new SubProgressMonitor(monitor, 3));
            monitorWork(monitor);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        } finally {
            // save endpoint, if new
            ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getSalesforceEndpoints()
                .addUserEndpoint(getDeploymentWizardModel().getDestinationOrg().getEndpointServer());
        }
    }

    /*
     * Deploy given components to given destination. Set checkOnly to true if deployment is to be committed.
     */
    public DeploymentResult deploy(
        ForceProject destinationProject,
        DeploymentPayload deploymentPayload,
        boolean checkOnly,
        IProgressMonitor monitor) throws ForceConnectionException, ForceRemoteException, InterruptedException,
            ForceProjectException, FactoryException, ServiceException {
        Connection connection = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory()
            .getConnection(destinationProject);
        return deployWork(connection, deploymentPayload, checkOnly, monitor);
    }
    
    private DeploymentResult deployWork(Connection connection, DeploymentPayload deploymentPayload, boolean checkOnly,
            IProgressMonitor monitor) throws InterruptedException, ForceProjectException, FactoryException,
            ServiceException, ForceRemoteException {

        final ServiceLocator serviceLocator = ContainerDelegate.getInstance().getServiceLocator();
        ProjectPackageList projectPackageList = serviceLocator.getProjectService().getProjectPackageListInstance();
        projectPackageList.setProject(deploymentPayload.getProject());
        PackageManifestFactory factory = projectPackageList.getPackageManifestFactory();
        // See W-837427 - we handle custom objects differently upon deploy, and so
        // call this specialized manifest factory method.
        Package manifest = factory.createSpecialDefaultPackageManifest();
        manifest.setVersion(serviceLocator.getProjectService().getLastSupportedEndpointVersion());

        monitorCheckSubTask(monitor, "Loading deployment candidates...");
        DeploymentComponentSet deploymentComponents = deploymentPayload.getDeploySelectedComponents();

        IProject project = deploymentPayload.getProject();
        Package packageManifest = serviceLocator.getProjectService().getPackageManifestFactory().getPackageManifest(project);
        
        // Looping through each component that we want to deploy.
        //
        // Custom object components are handled differently.
        for (DeploymentComponent deploymentComponent : deploymentComponents) {
            if (deploymentComponent.getDestinationSummary().equals(DeploymentSummary.DELETED)) {
                projectPackageList.addDeleteComponent(deploymentComponent.getComponent());
            } else {
                projectPackageList.addComponent(deploymentComponent.getComponent());
               
                // If this is a custom object component, check to see if it's actually in the manifest -
                // maybe a field inside the object was retrieved from the source org into the project,
                // but the object itself wasn't.
                //
                // If so, we don't want to add the object to the deployment manifest.  Any fields
                // will be included by default because the manifest has a "*" for that component type.
                // (In fact it has a "*" for every component type except custom object, because of
                // this particular issue. See changelist 1452239.)
                //
                //Package packageManifest = serviceLocator.getProjectService().getPackageManifestFactory().getPackageManifest(project);
				String type = deploymentComponent.getComponent().getComponentType();
                boolean isObject = type.equals(Constants.CUSTOM_OBJECT);
                boolean isSharingRule = Constants.ABSTRACT_SHARING_RULE_TYPES.contains(type) || Constants.SHARING_RULE_TYPES.contains(type);
				if(isObject) {
					boolean isPresent = new PackageManifest(packageManifest).contains("CustomObject", deploymentComponent.getComponent().getName());
					if (isPresent) {
						factory.addComponentToManifest(manifest, deploymentComponent.getComponent());
					}
				}
				//If it's a sharing rule, we will add it from the project manifest
				else if (!isSharingRule) {// && !type.equals("Settings")) {
					factory.addComponentToManifest(manifest, deploymentComponent.getComponent());
				}

            }
        }
        
        //Add sharing rules from the package manifest file since the component and file structure is 
        //different than what is required by the package manifest file.
        //W-1169372
        for (PackageTypeMembers member : packageManifest.getTypes()) {
        	if (Constants.SHARING_RULE_TYPES.contains(member.getName())) {
        		manifest.getTypes().add(member);
        	}
        }
        monitorWork(monitor);

        try {
            Component comp = factory.getComponentFactory().getComponentById(Constants.PACKAGE_MANIFEST);
            comp.setMetadataFilePath(Constants.PACKAGE_MANIFEST_FILE_NAME);
            comp.setBody(manifest.getXMLString());
            comp.setName(manifest.getFullName());

            projectPackageList.get(0).removePackageManifestComponent();
            projectPackageList.get(0).setPackageManifest(comp);
            projectPackageList.get(0).addComponent(comp);
        } catch (Exception e) {
            logger.warn("Unable to change deploy manifest", e);
        }

        // prepare result and record datetime
        DeploymentResult result = new DeploymentResult();
        result.setDeployTime(new GregorianCalendar());
        result.setDeploymentPayload(deploymentPayload);
        result.setDestinationOrg(connection.getConnectionInfo());
        result.setSourceProjectName(deploymentPayload.getProject().getName());
        result.setSourceEndpoint(serviceLocator.getProjectService().getSoapEndPoint(deploymentPayload.getProject()));
        result.setSourceUsername(serviceLocator.getProjectService().getUsername(deploymentPayload.getProject()));

        DeployOptions deployOptions = makeDefaultDeployOptions(checkOnly, serviceLocator.getPackageDeployService());

        LogInfo[] runTestLogSettings =
                serviceLocator.getLoggingService().getAllLogInfo(getProject(), LoggingInfo.SupportedFeatureEnum.RunTest);

        DeployResultExt deployResultHandler = null;
        try {
            deployResultHandler =
                    serviceLocator.getPackageDeployService().deploy(connection, projectPackageList, deployOptions, runTestLogSettings,
                        false, new SubProgressMonitor(monitor, 6));
        } catch (ServiceTimeoutException ex) {
            deployResultHandler = serviceLocator.getPackageDeployService().handleDeployServiceTimeoutException(ex, "deploy", monitor);
        }

        result.setDeployResultHandler(deployResultHandler);

        if (deployResultHandler != null && !deployResultHandler.isSuccess()) {
            StringBuffer strBuff = new StringBuffer("Deployment FAILED.  ");
            DeployMessageExt messageHandler = deployResultHandler.getMessageHandler();
            if (messageHandler != null) {
                messageHandler.logMessage(strBuff, false);
            }
            logger.warn(strBuff.toString());
        }

        return result;
    }

	protected DeployOptions makeDefaultDeployOptions(boolean checkOnly, PackageDeployService packageDeployService) {
        DeployOptions deployOptions = packageDeployService.getDeployOptions(checkOnly);
        deployOptions.setIgnoreWarnings(true);
        return deployOptions;
    }
}
