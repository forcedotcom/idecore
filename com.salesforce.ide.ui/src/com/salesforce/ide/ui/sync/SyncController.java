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
package com.salesforce.ide.ui.sync;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.OrgModel;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceTimeoutException;

/**
 * Handles synchronize functionality.
 * 
 * @author cwall
 */
public class SyncController extends Controller {

    private static final Logger logger = Logger.getLogger(SyncController.class);

    protected ProjectPackageList remoteProjectPackageList = null;
    protected List<IResource> syncResources = null;
    private long fetchRemoteTime = -1;

    //   C O N S T R U C T O R S
    public SyncController(IProject project, boolean filter, IResource... syncResources) {
        super();
        model = new OrgModel(project);
        if (syncResources != null) {
            List<IResource> list = new ArrayList<IResource>();
            for (IResource resource: syncResources) {
            	list.add(resource);
            }
            
            setSyncResources(list, filter);
        }    	
    }
    
    public SyncController(IProject project, IResource... syncResources) {
    	this(project, false, syncResources);
    }
    
    public SyncController(IProject project, List<IResource> syncResources) {
    	this(project, syncResources.toArray(new IResource[0]));
    }

    public SyncController(IProject project, List<IResource> syncResources, boolean filter) {
    	this(project, filter, syncResources.toArray(new IResource[0]));
    }

    //   M E T H O D S
    public void setSyncResources(List<IResource> syncResources, boolean filter) {
        if (filter) {
            try {
                this.syncResources = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getAllFilesOnly(syncResources);
            } catch (CoreException e) {
                String logMessage = Utils.generateCoreExceptionLog(e);
                logger.warn("Unable to get files from given resources: " + logMessage, e);
                this.syncResources = syncResources;
            }
        } else {
            this.syncResources = syncResources;
        }
    }

    public List<IResource> getSyncResources() {
        return syncResources;
    }

    public ProjectPackageList getRemoteProjectPackageList() {
        return remoteProjectPackageList;
    }

    public void setProjectPackageList(ProjectPackageList projectPackageList) {
        this.remoteProjectPackageList = projectPackageList;
    }

    public ComponentVariantComparator getResourceComparator() {
        return getComponentVariantComparatorInstance();
    }

    public ComponentVariantComparator getComponentVariantComparatorInstance() {
        return new ComponentVariantComparator(this);
    }

    public void loadRemoteComponents(IProgressMonitor monitor) throws InterruptedException, ForceConnectionException,
            FactoryException, CoreException, IOException, ForceRemoteException, ServiceException {
        if (getProject() == null) {
            throw new IllegalArgumentException("Resources and/or project cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger
                    .debug("Getting remote components for selected resources in project '" + getProject().getName()
                            + "'");
        }

        // initial store for remote components
        remoteProjectPackageList = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageListInstance();
        remoteProjectPackageList.setProject(getProject());
        fetchRemoteTime = Calendar.getInstance().getTimeInMillis();

        if (Utils.isEmpty(syncResources)) {
            if (logger.isInfoEnabled()) {
                logger.info("No remote components to fetch - sync resources provided");
            }
            return;
        }

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        monitorCheck(monitor);
        monitorSubTask(monitor, "Retrieving remote components...");

        // handle if source root was selected
        handleSourceRetrieve(monitor);

        // source root covers everything
        if (syncResources.size() == 1 && Utils.isNotEmpty(getFolder(syncResources, Constants.SOURCE_FOLDER_NAME))) {
            return;
        }

        // handle component folders
        handleComponentFolderRefresh(monitor);

        // handle component files
        handleSourceComponentFileRefresh(monitor);
    }

    protected void handleSourceRetrieve(IProgressMonitor monitor) throws InterruptedException,
            ForceConnectionException, ForceRemoteException, FactoryException, IOException,
            ServiceException {
        monitorCheck(monitor);
        if (Utils.isNotEmpty(getFolder(syncResources, Constants.SOURCE_FOLDER_NAME))) {
            String packageName = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageName(getProject());

            // perform retrieve
            RetrieveResultExt retrieveResultHandler =
                    ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrievePackage(getProject(), packageName, monitor);

            if (retrieveResultHandler == null) {
                logger.warn("Unable to sync source root - retrieve result is null");
                return;
            }

            monitorWork(monitor);
            remoteProjectPackageList.generateComponents(retrieveResultHandler.getZipFile(), retrieveResultHandler
                    .getFileMetadataHandler(), new SubProgressMonitor(monitor, 3));
        }
    }

    protected void handleComponentFolderRefresh(IProgressMonitor monitor) throws InterruptedException,
            ForceConnectionException, ForceRemoteException, FactoryException, CoreException, IOException,
            ServiceException {
        monitorCheck(monitor);
        List<IResource> folders = getResourcesByType(syncResources, IResource.FOLDER);
        List<String> componentTypes = new ArrayList<String>();
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
                ProjectPackageList localProjectPackageList =
                        ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageFactory().getProjectPackageListInstance(getProject());

                // only save these types
                String[] saveComponentTypes = componentTypes.toArray(new String[componentTypes.size()]);

                // perform retrieve
                RetrieveResultExt retrieveResultHandler =
                        ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrieveSelective(localProjectPackageList,
                            saveComponentTypes, monitor);

                remoteProjectPackageList.generateComponents(retrieveResultHandler.getZipFile(), retrieveResultHandler
                        .getFileMetadataHandler(), new SubProgressMonitor(monitor, 3));
            }
        }
    }

    protected void handleSourceComponentFileRefresh(IProgressMonitor monitor) throws InterruptedException,
            ForceConnectionException, ForceRemoteException, FactoryException, CoreException, IOException,
            ServiceException {
        monitorCheck(monitor);
        List<IResource> files = getResourcesByType(syncResources, IResource.FILE);
        if (Utils.isNotEmpty(files)) {
            List<IResource> sourceResources = new ArrayList<IResource>(files.size());
            for (IResource file : files) {
                if (ContainerDelegate.getInstance().getServiceLocator().getProjectService().isSourceResource(file)) {
                    sourceResources.add(file);
                }
            }

            ProjectPackageList localProjectPackageList =
                    ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectContents(sourceResources, monitor);
            localProjectPackageList.setProject(getProject());
            monitorCheck(monitor);
            RetrieveResultExt retrieveResultHandler =
                    ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService()
                            .retrieveSelective(localProjectPackageList, true, monitor);

            remoteProjectPackageList.generateComponents(retrieveResultHandler.getZipFile(), retrieveResultHandler
                    .getFileMetadataHandler(), new SubProgressMonitor(monitor, 3));
        }
    }

    /**
     * Assemble variant data to be compared for local and remote differences.
     * 
     * @param resource
     * @return
     * @throws FactoryException
     * @throws TeamException
     */
    public SyncInfo getSyncInfo(IResource resource) throws TeamException {
        if (!ContainerDelegate.getInstance().getServiceLocator().getProjectService().isManagedFile(resource)) {
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Get sync info for '" + resource.getProjectRelativePath().toPortableString() + "'");
        }

        ComponentVariant baseVariant = null;
        IFile file = (IFile) resource;

        if (file == null) {
            logger.warn("File is null. Skipping as sync candidate");
            return null;
        }

        Component baseComponent = null;
        try {
            baseComponent = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getComponentFromFile(file, true);
        } catch (FactoryException e) {
            logger.error("Unable to get component from file '" + file.getProjectRelativePath().toPortableString()
                    + "'. Skipping as sync candidate");
            return null;
        }

        // exclude default manifest
        if (baseComponent == null
                || (baseComponent.isPackageManifest() && Constants.DEFAULT_PACKAGED_NAME.equals(baseComponent
                        .getPackageName()))) {
            if (logger.isInfoEnabled()) {
                logger.info("Base component is the default package manifest - skipping ");
            }
            return null;
        }

        // prepare remote variant.
        Component remoteComponent = null;
        if (file.exists()) {
            // we only have a base variant if the file exist locally and we assemble a corresponding base component
            baseVariant = new ComponentVariant(baseComponent);

            // get remote component from pre-fetched remote project package list
            remoteComponent = getComponentFromRemoteList(baseComponent);

            // if we cannot find component in remote list, then we'll assume component was
            // created locally and not yet saved to remote server
            if (remoteComponent != null) {
                remoteComponent.setFileResource(baseComponent.getFileResource());
                remoteComponent.setPackageName(baseComponent.getPackageName());
                remoteComponent.setInstalled(baseComponent.isInstalled());
            }
        } else if (!file.exists() && Utils.isNotEmpty(file.getName())) {
            // typically this means file exists remotely, so there will only be a remote variant
            remoteComponent = getRemoteComponentByFilePath(file);
        } else {
            logger.warn("File '" + file.getName()
                    + "' does not exist and remote component not found. Skipping as sync candidate");
            return null;
        }

        ComponentVariant remoteVariant = null;
        if (remoteComponent != null) {
            remoteVariant = new ComponentVariant(remoteComponent);
            remoteVariant.setRemote(true);
        } else {
            if (logger.isInfoEnabled()) {
                logger
                        .info("Remote component not found.  Assuming component was created locally and not saved remotely");
            }
            // REVIEWME: temp workaround to trick compare algorithm that change is a new outgoing addition
            baseVariant = null;
        }

        SyncInfo syncInfo =
                new ComponentSyncInfo(resource, baseVariant, remoteVariant, getComponentVariantComparatorInstance());
        syncInfo.init();

        // if in sync, remove resource for further consideration
        if (syncInfo.getKind() == SyncInfo.IN_SYNC) {
            if (logger.isDebugEnabled()) {
                logger.debug("Resource '" + resource.getProjectRelativePath().toPortableString()
                        + "' is in sync - removing as a cached sync resource");
            }
            syncResources.remove(resource);
        }

        return syncInfo;
    }

    /**
     * Aggregates resources contained in a given resource.
     * 
     * @param resource
     * @return
     * @throws CoreException
     */
    public IResource[] members(IResource resource) throws CoreException {
        if (logger.isDebugEnabled()) {
            logger.debug("Assembling list of members of '" + resource.getName() + "' resource");
        }
        // files do not have members
        if (resource.getType() == IResource.FILE) {
            return getResources((IFile) resource);
        } else if (resource.getType() == IResource.PROJECT) {
            // projects can have members and we are only interested in managed folders
            return getResources((IProject) resource);
        } else if (resource.getType() != IResource.FOLDER) {
            // if you are neither a project nor a file, you must be a folder
            return new IResource[0];
        } else if (resource.getType() == IResource.FOLDER) {
            return getResources((IFolder) resource);
        } else {
            return new IResource[0];
        }
    }

    // sync api calls this to determine the root from which project resources will be gathered for sync inspection
    public IResource[] roots() {
        if (Utils.isEmpty(syncResources)) {
            if (logger.isDebugEnabled()) {
                logger.debug("No sync resource found");
            }
            return new IResource[0];
        }

        if (logger.isDebugEnabled()) {
            StringBuffer strBuff = new StringBuffer("Gathered [" + syncResources.size() + "] sync roots");
            int resourceCnt = 0;
            synchronized (syncResources) {
                for (IResource resource : syncResources) {
                    strBuff.append("\n (").append(++resourceCnt).append(") ").append(
                        resource.getProjectRelativePath().toPortableString());
                }
            }

            logger.debug(strBuff.toString());
        }

        return syncResources.toArray(new IResource[syncResources.size()]);
    }

    public boolean isSupervised(IResource resource) {
        return ContainerDelegate.getInstance().getServiceLocator().getProjectService().isManagedFile(resource);
    }

    private IResource[] getResources(IProject project) throws CoreException {
        Set<IResource> syncCandidates = new HashSet<IResource>();
        IResource[] childResources = getProject().members();
        if (Utils.isNotEmpty(childResources)) {
            for (IResource childResource : childResources) {
                if (ContainerDelegate.getInstance().getServiceLocator().getProjectService().isSourceFolder(childResource)) {
                    boolean exits = syncCandidates.add(childResource);
                    if (exits) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Added resource '" + childResource.getName() + "' as a local sync candidate");
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Resource '" + childResource.getName() + "' already exists as sync candidate");
                        }
                    }
                }
            }
        }

        logResources(syncCandidates);

        return syncCandidates.toArray(new IResource[syncCandidates.size()]);
    }

    private IResource[] getResources(IFolder folder) throws CoreException {
        // we're only interested in files that pertain to given org - not referenced packages
        if (!ContainerDelegate.getInstance().getServiceLocator().getProjectService().isManagedFolder(folder)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping folder '" + folder.getName() + "' as managed resource");
            }
            return new IResource[0];
        }

        Set<IResource> syncCandidates = new HashSet<IResource>();
        List<IResource> members = null;
        members = new ArrayList<IResource>(Arrays.asList(folder.members()));

        for (IResource resource : members) {
            if (resource.getType() == IResource.FOLDER) {
                Set<IResource> subFolderMembers = getFolderResources(syncCandidates, (IFolder) resource);
                syncCandidates.addAll(subFolderMembers);
            } else if (resource.getType() == IResource.FILE) {
                addFileResource(syncCandidates, (IFile) resource);
            } else {
                logger.warn("Resource '" + resource.getName() + "' is not considered for sync'ing");
            }
        }

        addRemoteSyncCandidates(syncCandidates, remoteProjectPackageList);

        logResources(syncCandidates);

        return syncCandidates.toArray(new IResource[syncCandidates.size()]);
    }

    private IResource[] getResources(IFile file) {
        // we're only interested in files that pertain to given org - not referenced packages
        if (!ContainerDelegate.getInstance().getServiceLocator().getProjectService().isManagedResource(file)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping folder '" + file.getName() + "' as managed resource");
            }
            return new IResource[0];
        }

        Set<IResource> syncCandidates = new HashSet<IResource>();
        addFileResource(syncCandidates, file);

        addRemoteSyncCandidates(syncCandidates, remoteProjectPackageList);

        logResources(syncCandidates);

        return syncCandidates.toArray(new IResource[syncCandidates.size()]);
    }

    private Set<IResource> getFolderResources(Set<IResource> syncCandidates, IFolder folder) throws CoreException {
        // for managed folders, consider managed files locally
        List<IResource> members = null;
        members = new ArrayList<IResource>(Arrays.asList(folder.members()));

        for (IResource resource : members) {
            if (resource.getType() == IResource.FOLDER) {
                Set<IResource> subFolderMembers = getFolderResources(syncCandidates, (IFolder) resource);
                syncCandidates.addAll(subFolderMembers);
            } else if (resource.getType() == IResource.FILE) {
                addFileResource(syncCandidates, (IFile) resource);
            } else {
                logger.warn("Resource '" + resource.getName() + "' is not considered for sync'ing");
            }
        }

        return syncCandidates;
    }

    // add remote components to sync candidate list.
    // this captures components that were created remotely and do not exist w/in the existing project
    private static void addRemoteSyncCandidates(Set<IResource> syncCandidates, ProjectPackageList projectPackageList) {
        if (syncCandidates == null || Utils.isEmpty(projectPackageList)) {
            logger.warn("No remote resources to add - sync candiates null or project package list null/empty");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Adding remote components to sync candidate list");
        }
        for (ProjectPackage projectPackage : projectPackageList) {
            ComponentList componentList = projectPackage.getComponentList();
            if (Utils.isNotEmpty(componentList)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found [" + componentList.size() + "] components in package '"
                            + projectPackage.getName() + "' to evaluate");
                }
                for (Component component : componentList) {
                    IResource resource = component.getFileResource(projectPackageList.getProject());
                    if (resource == null || ContainerDelegate.getInstance().getServiceLocator().getProjectService().isDefaultPackageManifestFile(resource)
                            || !ContainerDelegate.getInstance().getServiceLocator().getProjectService().isManagedFile(resource)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Excluding '" + (resource != null ? resource.getName() : component.getMetadataFilePath())
                                    + "' resource - resource is null and/or resource is not a "
                                    + Constants.PLUGIN_NAME
                                    + " managed resource");
                        }
                        continue;
                    }

                    boolean addSuccess = syncCandidates.add(resource);
                    if (addSuccess) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Added resource '" + resource.getProjectRelativePath().toPortableString()
                                    + "' as a remote sync candidate");
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Resource '" + resource.getProjectRelativePath().toPortableString()
                                    + "' already exists as sync candidate");
                        }
                    }
                }
            }
        }
    }

    private static void addFileResource(Set<IResource> syncCandidates, IFile file) {
        if (null == file) return;

        // we're only interested in files that pertain to given org - not installed packages & package manifest
        if (syncCandidates == null || ContainerDelegate.getInstance().getServiceLocator().getProjectService().isDefaultPackageManifestFile(file)
                || !ContainerDelegate.getInstance().getServiceLocator().getProjectService().isManagedFile(file)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping resource '" + file.getName() + "' as managed resource");
            }
            return;
        }

        // add resource as sync candidate
        addResource(syncCandidates, file);
    }

    private static void addResource(Set<IResource> syncCandidates, IResource resource) {
        // add resource to candidates
        boolean exits = syncCandidates.add(resource);
        if (exits) {
            if (logger.isDebugEnabled()) {
                logger.debug("Added resource '" + resource.getName() + "' as a local sync candidate");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Resource '" + resource.getName() + "' already exists as sync candidate");
            }
        }
    }

    private Component getComponentFromRemoteList(Component localComponent) {
        if (Utils.isEmpty(remoteProjectPackageList) || localComponent == null) {
            return null;
        }

        Component remoteComponent =
                remoteProjectPackageList.getComponentByFilePath(localComponent.getMetadataFilePath());

        // REVIEWME: re-think impl; maybe refetch just out-of-date component 
        // since we cache remote components, remote components become out-of-date.
        if ((remoteComponent != null && !remoteComponent.isFetchedAfter(localComponent.getFetchTime()))
                || localComponent.isFetchedAfter(fetchRemoteTime)) {
            if (logger.isInfoEnabled()) {
                logger.info("Remote component " + (remoteComponent != null ? remoteComponent.getFullDisplayName() : "")
                        + " is out-of-date - refreshing all remote components");
            }
            try {
                loadRemoteComponents(null);
            } catch (Exception e) {
                logger.error("Unable to refetch remote compoents", e);
            }
        }
        return remoteComponent;
    }

    private Component getRemoteComponentByFilePath(IFile file) {
        Component component = null;
        if (Utils.isNotEmpty(remoteProjectPackageList) && file != null) {
            String filePath = file.getProjectRelativePath().toPortableString();
            component = remoteProjectPackageList.getComponentByFilePath(filePath);
        }
        return component;
    }

    //  S Y N C   O P E R A T I O N S
    public boolean applyToProject(ComponentSubscriber subscriber, SyncInfo[] syncInfos, IProgressMonitor monitor)
            throws InterruptedException, CoreException, InvocationTargetException, IOException, ForceProjectException,
            Exception {
        if (Utils.isEmpty(syncInfos)) {
            throw new IllegalArgumentException("Sync array and/or subscriber cannot be null");
        }

        boolean result = true;
        List<SyncInfo> projectDeletes = new ArrayList<SyncInfo>();
        List<SyncInfo> projectSaves = new ArrayList<SyncInfo>();
        for (SyncInfo syncInfo : syncInfos) {
            int change = SyncInfo.getChange(syncInfo.getKind());
            // outgoing
            if (SyncInfo.getDirection(syncInfo.getKind()) == SyncInfo.OUTGOING) {
                if (change == SyncInfo.DELETION || change == SyncInfo.CHANGE) {
                    projectSaves.add(syncInfo);
                } else if (change == SyncInfo.ADDITION) {
                    projectDeletes.add(syncInfo);
                }
            } else {
                // incoming
                if (change == SyncInfo.ADDITION || change == SyncInfo.CHANGE) {
                    projectSaves.add(syncInfo);
                } else if (change == SyncInfo.DELETION) {
                    projectDeletes.add(syncInfo);
                }
            }
        }

        if (Utils.isNotEmpty(projectDeletes)) {
            result = deleteFromProject(projectDeletes, monitor);
        }

        if (result && Utils.isNotEmpty(projectSaves)) {
            result = saveToProject(projectSaves, monitor);
        }

        if (result && subscriber != null) {
            // refresh sync view
            subscriber.refresh(new SubProgressMonitor(monitor, 5));
        }

        return result;
    }

    /**
     * Apply selected remote synchronize conflict to local project.
     * 
     * @param syncInfos
     * @return
     * @throws InvocationTargetException
     * @throws InterruptedException
     * @throws IOException
     * @throws CoreException
     * @throws ForceProjectException
     */
    protected boolean saveToProject(final List<SyncInfo> syncInfos, IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException, CoreException, IOException, ForceProjectException,
            Exception {
        if (Utils.isEmpty(syncInfos)) {
            throw new IllegalArgumentException("Sync array and/or subscriber cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Apply server change to local");
        }

        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    try {
                        for (SyncInfo syncInfo : syncInfos) {
                            monitorCheck(monitor);
                            ComponentVariant componentVariant = (ComponentVariant) syncInfo.getRemote();
                            if (componentVariant != null) {
                                Component remoteComponent = componentVariant.getComponent();

                                // skip auto build for this operation
                                ContainerDelegate.getInstance().getServiceLocator().getProjectService().flagSkipBuilder(getProject());

                                monitorCheck(monitor);
                                syncResources.remove(syncInfo.getLocal());
                                IFile file = remoteComponent.saveToFile(true, monitor);

                                if (logger.isInfoEnabled()) {
                                    logger.info("Saved '" + file.getProjectRelativePath().toPortableString()
                                            + "' to project '" + file.getProject().getName()
                                            + "' and remove as sync resource");
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new CoreException(new Status(IStatus.ERROR, Constants.FORCE_PLUGIN_PREFIX, 0, e
                                .getMessage(), e));
                    }
                }
            }, null, IResource.NONE, monitor);
        } catch (CoreException e) {
            throw (Exception) e.getCause();
        }

        return true;
    }

    public boolean deleteFromProject(final List<SyncInfo> syncInfos, IProgressMonitor monitor)
            throws InterruptedException, CoreException, InvocationTargetException, Exception {
        if (Utils.isEmpty(syncInfos)) {
            throw new IllegalArgumentException("SyncInfo array cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Delete from project");
        }

        class Result {
            boolean result = true;
        }

        final Result result = new Result();

        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    try {
                        for (SyncInfo syncInfo : syncInfos) {
                            monitorCheck(monitor);
                            IResource resource = syncInfo.getLocal();
                            if (resource == null || !resource.exists() || resource.getType() != IResource.FILE) {
                                logger
                                        .error("Unable to delete resource from project - resource is null or does not exist or is not a file");
                                result.result = false;
                                break;
                            }

                            // skip auto build for this operation
                            ContainerDelegate.getInstance().getServiceLocator().getProjectService().flagSkipBuilder(getProject());

                            handleCompositeResourceDelete((IFile) resource, monitor);
                            deleteResource(resource, monitor);
                        }
                    } catch (Exception e) {
                        throw new CoreException(new Status(IStatus.ERROR, Constants.FORCE_PLUGIN_PREFIX, 0, e
                                .getMessage(), e));
                    }
                }
            }, null, IResource.NONE, monitor);
        } catch (CoreException e) {
            throw (Exception) e.getCause();
        }

        return result.result;
    }

    protected boolean handleCompositeResourceDelete(IFile file, IProgressMonitor monitor) throws InterruptedException,
            CoreException {
        IFile compositeFile = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getCompositeFileResource(file);
        if (compositeFile != null) {
            deleteResource(compositeFile, monitor);
        }
        return true;
    }

    protected void deleteResource(IResource resource, IProgressMonitor monitor) throws InterruptedException,
            CoreException {
        String resourceFilePath = resource.getProjectRelativePath().toPortableString();
        String projectName = resource.getProject().getName();

        monitorCheck(monitor);
        syncResources.remove(resource);
        resource.delete(true, monitor);

        if (logger.isInfoEnabled()) {
            logger.info("Delete '" + resourceFilePath + "' from project '" + projectName
                    + "' and remove as sync resource");
        }
    }

    /**
     * Apply selected local synchronize conflict to server.
     * 
     * @param subscriber
     * @param infos
     * @return
     * @throws SyncException
     * @throws InterruptedException
     * @throws InterruptedException
     * @throws IOException
     * @throws CoreException
     * @throws ServiceException
     * @throws FactoryException
     * @throws ForceRemoteException
     * @throws ForceConnectionException
     * @throws InvocationTargetException
     */
    public boolean applyToServer(ComponentSubscriber subscriber, SyncInfo[] syncInfos, IProgressMonitor monitor)
            throws SyncException, InterruptedException, ForceConnectionException, ForceRemoteException,
            FactoryException, ServiceException, CoreException, IOException, InvocationTargetException, Exception {
        if (Utils.isEmpty(syncInfos)) {
            throw new IllegalArgumentException("Sync array and/or subscriber cannot be null");
        }

        boolean result = true;
        List<SyncInfo> serverDeletes = new ArrayList<SyncInfo>();
        List<SyncInfo> serverSaves = new ArrayList<SyncInfo>();
        for (SyncInfo syncInfo : syncInfos) {
            int change = SyncInfo.getChange(syncInfo.getKind());
            if (SyncInfo.getDirection(syncInfo.getKind()) == SyncInfo.INCOMING) {
                if (change == SyncInfo.DELETION || change == SyncInfo.CHANGE) {
                    serverSaves.add(syncInfo);
                } else if (change == SyncInfo.ADDITION) {
                    serverDeletes.add(syncInfo);
                }
            } else {
                if (change == SyncInfo.ADDITION || change == SyncInfo.CHANGE) {
                    serverSaves.add(syncInfo);
                } else if (change == SyncInfo.DELETION) {
                    serverDeletes.add(syncInfo);
                }
            }
        }

        if (Utils.isNotEmpty(serverDeletes)) {
            result = deleteFromServer(serverDeletes, monitor);
        }

        if (result && Utils.isNotEmpty(serverSaves)) {
            result = saveToServer(serverSaves, monitor);
        }

        if (result && subscriber != null) {
            // refresh sync candidates
            subscriber.refresh(getResources(syncInfos), new SubProgressMonitor(monitor, 5));
        }

        return result;
    }

    protected boolean saveToServer(List<SyncInfo> syncInfos, IProgressMonitor monitor) throws InterruptedException,
            FactoryException, ForceConnectionException, CoreException, IOException, ServiceException,
            ForceRemoteException, InvocationTargetException, Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Apply local change to server");
        }

        monitorCheck(monitor);
        ProjectPackageList projectPackageList = generateProjectPackageList(syncInfos, monitor);

        monitorCheck(monitor);
        DeployResultExt deployResultHandler = ContainerDelegate.getInstance().getServiceLocator().getPackageDeployService().deploy(projectPackageList, monitor);
        boolean result = ContainerDelegate.getInstance().getServiceLocator().getProjectService().handleDeployResult(projectPackageList, deployResultHandler, true, monitor);

        if (result) {
            if (logger.isInfoEnabled()) {
                logger.info("Saved the following components from server:\n"
                        + projectPackageList.getAllComponents().toStringLite());
            }

            for (SyncInfo syncInfo : syncInfos) {
                IResource resource = syncInfo.getLocal();
                if (logger.isInfoEnabled()) {
                    logger.info("Remove '" + resource.getProjectRelativePath().toPortableString()
                            + "' as sync resource");
                }
                syncResources.remove(resource);
            }
        } else {
            logger.warn("Failed to apply saves to server");
        }

        return true;
    }

    public boolean deleteFromServer(List<SyncInfo> syncInfos, IProgressMonitor monitor) throws FactoryException,
            InterruptedException, ForceConnectionException, ServiceException, CoreException, IOException,
            ForceRemoteException, InvocationTargetException, Exception {
        if (Utils.isEmpty(syncInfos)) {
            throw new IllegalArgumentException("SyncInfo array cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Apply local delete to server");
        }

        ProjectPackageList projectPackageList = generateProjectPackageList(syncInfos, monitor);
        // ensure that each project package has a delete manifest
        for (ProjectPackage projectPackage : projectPackageList) {
            ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageManifestFactory().attachDeleteManifest(projectPackage);
        }

        monitorCheck(monitor);
        DeployResultExt deployResultHandler = null;
        try {
            deployResultHandler = ContainerDelegate.getInstance().getServiceLocator().getPackageDeployService().deployDelete(projectPackageList, false, monitor);
        } catch (ServiceTimeoutException ex) {
            deployResultHandler =
                    ContainerDelegate.getInstance().getServiceLocator().getPackageDeployService().handleDeployServiceTimeoutException(ex,
                        "delete from server", monitor);
        }

        monitorCheck(monitor);
        boolean result = ContainerDelegate.getInstance().getServiceLocator().getProjectService().handleDeployResult(projectPackageList, deployResultHandler, true, monitor);

        if (result) {
            if (logger.isInfoEnabled()) {
                logger.info("Delete the following components from server:\n"
                        + projectPackageList.getAllComponents().toStringLite());
            }

            for (SyncInfo syncInfo : syncInfos) {
                IResource resource = syncInfo.getLocal();
                if (logger.isInfoEnabled()) {
                    logger.info("Remove '" + resource.getProjectRelativePath().toPortableString()
                            + "' as sync resource");
                }
                syncResources.remove(resource);
            }
        } else {
            logger.warn("Failed to apply deletes to server");
        }

        return true;
    }

    protected IResource[] getResources(SyncInfo[] syncInfos) {
        if (Utils.isEmpty(syncInfos)) {
            return null;
        }

        List<IResource> resources = new ArrayList<IResource>();
        for (SyncInfo syncInfo : syncInfos) {
            resources.add(syncInfo.getLocal());
        }

        return resources.toArray(new IResource[resources.size()]);

    }

    protected ProjectPackageList generateProjectPackageList(List<SyncInfo> syncInfos, IProgressMonitor monitor)
            throws FactoryException, InterruptedException {

        ProjectPackageList projectPackageList = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageListInstance();
        if (Utils.isEmpty(syncInfos)) {
            return projectPackageList;
        }

        for (SyncInfo syncInfo : syncInfos) {
            monitorCheck(monitor);
            IResource res = syncInfo.getLocal();
            if (res.getType() == IResource.FILE) {
                IFile file = (IFile) res;
                projectPackageList.setProject(file.getProject());

                monitorCheck(monitor);
                Component component = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getComponentFromFile(file, true);
                projectPackageList.addComponent(component, true);
            }
        }
        return projectPackageList;
    }

    //   C O M P A R A T O R   O P E R A T I O N S
    public boolean compare(IResource file, IResourceVariant baseVariant) {
        if (file == null || baseVariant == null) {
            throw new IllegalArgumentException("Resource and/or variant cannot be null");
        }

        boolean changed = false;

        if (file.getType() != IResource.FILE || !(baseVariant instanceof ComponentVariant)) {
            logger.warn("Unable to compare local file and variant");
            return changed;
        }

        IFile componentFile = (IFile) file;
        ComponentVariant baseComponentVariant = (ComponentVariant) baseVariant;
        Component baseComponent = baseComponentVariant.getComponent();

        if (baseComponent == null) {
            logger.warn("Unable to compare file and variant - base component is null");
            return changed;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Comparing local file '" + componentFile.getName() + "' with base component "
                    + baseComponent.getFullDisplayName());
        }

        Component localComponent = null;
        try {
            localComponent = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getComponentFromFile(componentFile, true);
        } catch (FactoryException e) {
            logger.error("Unable to load component from file '" + componentFile.getName() + "'", e);
            // REVIEWME: is assuming no change the best way to handle this scenario? 
            return changed;
        }

        if (localComponent != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Is base variant a remote component? " + baseComponentVariant.isRemote());
            }
            changed = localComponent.hasLocalChanged();

            if (logger.isInfoEnabled()) {
                logger.info("Local " + localComponent.getFullDisplayName() + " body is " + (changed ? "OUT" : "IN")
                        + " of sync with original body.");
            }
        } else {
            logger.warn("Local component is null.");
        }

        // if changed, then we return that file and variant are not equal
        return !changed;
    }

    public boolean compare(IResourceVariant baseVariant, IResourceVariant remoteVariant) {
        if (remoteVariant == null || baseVariant == null) {
            throw new IllegalArgumentException("Base and/or remote variant cannot be null");
        }

        boolean changed = false;
        if (!(baseVariant instanceof ComponentVariant)
                || !(remoteVariant instanceof ComponentVariant)) {
            logger.warn("Unable to compare base and remote variant");
            return changed;
        }

        ComponentVariant baseComponentVariant = (ComponentVariant) baseVariant;
        Component baseComponent = baseComponentVariant.getComponent();

        ComponentVariant remoteComponentVariant = (ComponentVariant) remoteVariant;
        Component remoteComponent = remoteComponentVariant.getComponent();

        if (remoteComponent == null || baseComponent == null || isDefaultPackageManifest(remoteComponent)
                || isDefaultPackageManifest(baseComponent)) {
            logger.warn("Unable to compare base and remote variant - base and/or remote component is null "
                    + "or is default manifest");
            return changed;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Comparing base and remote " + baseComponent.getFullDisplayName());
        }

        try {
            changed = baseComponent.hasRemoteChanged(remoteComponent, false, new NullProgressMonitor());
        } catch (InterruptedException e) {
            // do nothing - thrown if user cancels
        }

        if (logger.isInfoEnabled()) {
            logger.info("Remote " + remoteComponent.getFullDisplayName() + " body is " + (changed ? "OUT" : "IN")
                    + " of sync with local original body.");
        }

        // if changed, then we return that base and remote variant are not equal
        return !changed;
    }

    private static boolean isDefaultPackageManifest(Component component) {
        return (component.isPackageManifest() && Constants.DEFAULT_PACKAGED_NAME.equals(component.getName()));
    }

    private static void logResources(Set<IResource> forceFolders) {
        if (logger.isDebugEnabled() && Utils.isNotEmpty(forceFolders)) {
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("Assembled the following resources ").append("[").append(forceFolders.size()).append(
                "] to be inspected: ");
            int resCnt = 0;
            for (IResource resource : forceFolders) {
                strBuff.append("\n (").append(++resCnt).append(") ").append(
                    resource.getProjectRelativePath().toPortableString()).append(", ").append(" type=").append(
                    resource.getType());
            }
            logger.debug("\n" + strBuff.toString());
        }
    }

    @Override
    public void finish(IProgressMonitor monitor) throws Exception {

    }

    @Override
    public void init() {

    }

    public void clean() {
        if (Utils.isEmpty(syncResources)) {
            return;
        }
        syncResources.clear();
    }

    @Override
    public void dispose() {

    }

    private static List<IResource> getResourcesByType(List<IResource> resources, int type) {
        if (Utils.isEmpty(resources)) {
            return null;
        }

        List<IResource> specificResources = new ArrayList<IResource>(resources.size());
        for (IResource resource : resources) {
            if (resource.getType() == type) {
                specificResources.add(resource);
            }
        }
        return specificResources;
    }

    private static IResource getFolder(List<IResource> resources, String name) {
        if (Utils.isEmpty(resources)) {
            return null;
        }

        for (IResource resource : resources) {
            if (resource.getType() == IResource.FOLDER && resource.getName().endsWith(name)) {
                return resource;
            }
        }

        return null;
    }
}
