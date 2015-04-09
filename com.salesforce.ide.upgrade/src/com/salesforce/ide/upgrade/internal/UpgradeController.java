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
package com.salesforce.ide.upgrade.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.bind.UnmarshalException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.IProgressService;

import com.salesforce.ide.api.metadata.types.MetadataExt;
import com.salesforce.ide.api.metadata.types.MetadataValidationEventCollector;
import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.controller.Controller;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceTimeoutException;
import com.salesforce.ide.upgrade.ForceIdeUpgradePlugin;
import com.salesforce.ide.upgrade.internal.ide.IInternalUpgrade;
import com.salesforce.ide.upgrade.project.UpgradeMarkerUtils;
import com.salesforce.ide.upgrade.project.UpgradeNature;

/**
 * Handles upgrade analysis and related project and IDE upgrades.
 * 
 * @author chris
 */
public class UpgradeController extends Controller {
    private static final Logger logger = Logger.getLogger(UpgradeController.class);

    private String installedIdeVersion = null;

    private ComponentList upgradeableComponentList = null;
    private List<String> excludeFileExtensions = null;
    private final List<IInternalUpgrade> ideInternalUpgrades = new ArrayList<>();

    public UpgradeController() {
        super();
        model = new UpgradeModel(ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPlatformBrandName(), ContainerDelegate.getInstance().getServiceLocator().getProjectService().getIdeReleaseName(),
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getIdeBrandName());

        installedIdeVersion = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getInstalledIdeVersion();

        upgradeableComponentList = new ComponentList();
                              
        String[] upgradableTypes = new String[] {
        		Constants.APEX_CLASS,
        		Constants.APEX_COMPONENT,
        		Constants.APEX_PAGE,
        		Constants.APEX_TRIGGER,
        		Constants.CUSTOM_OBJECT,
        		Constants.CUSTOM_OBJECT_TRANSLATION,
        		Constants.CUSTOM_SITE,
        		Constants.CUSTOM_TAB,        		
        		Constants.DASHBOARD,
        		Constants.DOCUMENT,
        		Constants.DATACATEGORYGROUP,
        		Constants.EMAIL_TEMPLATE,        		
        		Constants.LAYOUT,
        		Constants.PROFILE,
        		Constants.REPORT,
        		Constants.RECORD_TYPE,
        		Constants.WORKFLOW
        };
        
        for (String type : upgradableTypes) {
        	upgradeableComponentList.add(ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getComponentByComponentType(type));
        }
        
        excludeFileExtensions = new ArrayList<>();
        
        String[] excludedFileExtensions = new String[] {
        		Constants.APEX_CLASS,
        		Constants.APEX_COMPONENT,
        		Constants.APEX_PAGE,
        		Constants.APEX_TRIGGER,
        		Constants.EMAIL_TEMPLATE        		
        };
        
        for (String excludedExtension : excludedFileExtensions) {
            excludeFileExtensions.add(ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getComponentByComponentType(excludedExtension)
                    .getFileExtension());        	
        }
    }

    // M E T H O D S
    @Override
    public void init() throws ForceProjectException {
    // not implemented
    }

    @Override
    public void setProject(IProject project) {
        getUpgradeModel().setProject(project);
        super.setProject(project);
    }

    public UpgradeModel getUpgradeModel() {
        return (UpgradeModel)model;
    }

    public List<String> getExcludeFileExtensions() {
        return excludeFileExtensions;
    }

    public void setExcludeFileExtensions(List<String> excludeFileExtensions) {
        this.excludeFileExtensions = excludeFileExtensions;
    }

    public ComponentList getUpgradeableComponentList() {
        return upgradeableComponentList;
    }

    public void setUpgradeableComponentList(ComponentList upgradeableComponentList) {
        this.upgradeableComponentList = upgradeableComponentList;
    }

    public List<IInternalUpgrade> getIdeInternalUpgrades() {
        return ideInternalUpgrades;
    }

    public boolean addInternalUpgradeInstance(IInternalUpgrade internalUpgrade) {
        ideInternalUpgrades.add(internalUpgrade);
        sortIdeInternals();
        return true;
    }

    /**
     * Analyzes project content against release schema and corresponding remote content to determine component
     * upgradeability.
     * 
     * @param monitor
     * @throws ForceConnectionException
     * @throws ForceRemoteException
     * @throws FactoryException
     * @throws ServiceException
     * @throws InterruptedException
     * @throws CoreException
     * @throws IOException
     */
    public void initConflicts(IProgressMonitor monitor) throws ForceConnectionException, ForceRemoteException,
            FactoryException, ServiceException, InterruptedException, CoreException, IOException {
    	
        logStatus();
        initConflicts(monitor, model.getProject());
    }

	public void initConflicts(IProgressMonitor monitor, IProject project)
			throws InterruptedException, CoreException, FactoryException,
			ForceConnectionException, ForceRemoteException, ServiceException,
			IOException {
		// if not components are noted for upgrade in this release, then there's no metadata to upgrade
        if (Utils.isEmpty(upgradeableComponentList)) {
            if (logger.isInfoEnabled()) {
                logger.info("No component types upgrade-able for " + installedIdeVersion + "version");
            }
            return;
        }

        // get local and remote components
        monitorCheckSubTask(monitor, "Gathering project contents");
        List<String> componentTypes = upgradeableComponentList.getComponentTypes();        
		ProjectPackageList localProjectPackageList = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getComponentsForComponentTypes(
                project, componentTypes.toArray(new String[componentTypes.size()]));
        monitorWork(monitor);

        if (Utils.isEmpty(localProjectPackageList.getAllComponents(false))) {
            if (logger.isInfoEnabled()) {
                logger.info("Project is empty - nothing to upgrade");
            }
            return;
        }

        monitorCheckSubTask(monitor, "Retrieving remote content for upgrade analysis");
        ProjectPackageList remoteProjectPackageList = getRemoteProjectPackageList(localProjectPackageList,
                new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
        monitorWork(monitor);

        if (Utils.isEmpty(localProjectPackageList)) {
            if (logger.isInfoEnabled()) {
                logger.info("Remote content empty - nothing to upgrade");
            }
            return;
        }

        // store to-be-upgrade components
        Map<String, List<UpgradeConflict>> upgradeConflicts = new HashMap<>();

        monitorCheckSubTask(monitor, "Inspecting project contents for upgradeability");
        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN);
        // upgrade inspection
        // upgradeability is determine by a straight diff of local vs. remote
        ComponentList localComponentList = localProjectPackageList.getAllComponents();
        for (Component localComponent : localComponentList) {
            if (!isIncludedComponent(localComponent) || isExcludedFileExtension(localComponent)
                    || localComponent.isPackageManifest()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Excluding " + localComponent.getFullDisplayName() + " from upgrade consideration");
                }
                continue;
            }

            // upgrade analysis is a two phase process. if the first phase check, a xml validation against
            // current schema, passes, the second phase checks the local and remote checksum to determine difference.

            // if exist and has changed, store to be exposed in ui and later saved to project
            Component remoteComponent = remoteProjectPackageList.getComponentByFilePath(localComponent
                    .getMetadataFilePath());

            // phase 1 - validate xml against current schema
            MetadataValidationEventCollector metadataValidationEventCollector = new MetadataValidationEventCollector();
            MetadataExt metadataExt = null;
            try {
                metadataExt = localComponent.getMetadataExtFromBody(true, metadataValidationEventCollector);

                // if validation issues are found, we have a conflict
                if (metadataValidationEventCollector.hasValidationIssues()) {
                    if (metadataExt != null) {
                        metadataValidationEventCollector.logValidationMessages(metadataExt.getFullName());
                    }
                    // store conflict and continue to next bypassing phase two
                    addConflict(upgradeConflicts, localComponent, remoteComponent);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Added " + localComponent.getFullDisplayName()
                                + " as upgrade consideration - schema validation failed");
                    }

                    monitorWork(subMonitor);
                    continue;
                }
            } catch (UnmarshalException e) {
                // sometimes, for some reason, parsing issues will throw an exception despite
                // MetadataValidationEventCollector.failOnValidateError used in
                // MetadataValidationEventCollector.handleEvent to capture parsing issues and enable recovery
                if (metadataExt != null) {
                    metadataValidationEventCollector.logValidationMessages(metadataExt.getFullName());
                }
                // store conflict and continue to next bypassing phase two
                addConflict(upgradeConflicts, localComponent, remoteComponent);

                if (logger.isDebugEnabled()) {
                    logger.debug("Added " + localComponent.getFullDisplayName()
                            + " as upgrade consideration - schema validation failed");
                }

                monitorWork(subMonitor);
                continue;
            } catch (Exception e) {
                logger.warn("Unable to parse and validate " + localComponent.getFullDisplayName()
                        + " - will continue to second phase inspection, content diff: "
                        + ForceExceptionUtils.getRootCauseMessage(e));
            }

            // phase 2 - local and remote checksum to determine difference find component in remote list
            if (remoteComponent != null && localComponent.hasEitherChanged(remoteComponent, monitor)) {
                addConflict(upgradeConflicts, localComponent, remoteComponent);

                if (logger.isDebugEnabled()) {
                    logger.debug("Added " + localComponent.getFullDisplayName()
                            + " as upgrade consideration - local file differs from remote");
                }
            }

            monitorWork(subMonitor);
        }
        monitorWork(monitor);

        // store upgrade components
        getUpgradeModel().setUpgradeConflicts(upgradeConflicts);
	}

	private void logStatus() {
		// log include component types and excluded file extensions
        if (logger.isDebugEnabled()) {
            StringBuffer strBuff = new StringBuffer(installedIdeVersion
                    + " version upgrades include the following component types:");
            int cnt = 0;
            if (Utils.isNotEmpty(upgradeableComponentList)) {
                for (Component upgradeableComponent : upgradeableComponentList) {
                    strBuff.append("\n  ").append("(").append(++cnt).append(") ").append(
                            upgradeableComponent.getComponentType());
                }
            } else {
                strBuff.append(" n/a");
            }
            logger.debug(strBuff.toString());

            strBuff = new StringBuffer(installedIdeVersion
                    + " version upgrades excludes the following file extensions:");
            cnt = 0;
            if (Utils.isNotEmpty(excludeFileExtensions)) {
                for (String excludeFileExtension : excludeFileExtensions) {
                    strBuff.append("\n  ").append("(").append(++cnt).append(") ").append(excludeFileExtension);
                }
            } else {
                strBuff.append(" n/a");
            }
            logger.debug(strBuff.toString());

        }
	}

    public boolean isIncludedComponent(Component component) {
    	return ! component.getName().contains(Constants.UNFILED_PUBLIC_FOLDER_NAME);
    }

    public boolean isExcludedFileExtension(Component component) {
        IFile fileResource = component.getFileResource();
		if (Utils.isNotEmpty(excludeFileExtensions) && Utils.isNotEmpty(component.getFileName()) && fileResource != null) {
            String fileExtension = fileResource.getFileExtension();
			if (excludeFileExtensions.contains(fileExtension)) { return true; }
        }
        return false;
    }

    protected void addConflict(Map<String, List<UpgradeConflict>> upgradeConflicts, Component localComponent,
            Component remoteComponent) {
        if(null==localComponent || null==remoteComponent){
            return;
        }
        
        // set file resource to be saved over-top w/ remote instance
        remoteComponent.setFileResource(localComponent.getFileResource());

        // wrapper to contain local and remote
        UpgradeConflict upgradeConflict = new UpgradeConflict(localComponent, remoteComponent);

        // list containing components - remote and local - stored together by type
        List<UpgradeConflict> componetTypeUpgradeConflict = null;
        if (!upgradeConflicts.containsKey(localComponent.getComponentType())) {
            componetTypeUpgradeConflict = new ArrayList<>();
            upgradeConflicts.put(localComponent.getComponentType(), componetTypeUpgradeConflict);
        } else {
            componetTypeUpgradeConflict = upgradeConflicts.get(localComponent.getComponentType());
        }

        // add conflict to list
        componetTypeUpgradeConflict.add(upgradeConflict);
    }

    public boolean hasConflicts() {
        return getUpgradeModel().hasUpgradeComponents();
    }

    private ProjectPackageList getRemoteProjectPackageList(ProjectPackageList localProjectPackageList,
            IProgressMonitor monitor) throws ForceConnectionException, ForceRemoteException, FactoryException,
            ServiceException, InterruptedException, IOException {

        // declare return list
        ProjectPackageList remoteProjectPackageList = null;

        // update package manifest version to current ide version
        Package packageManifest = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageManifestFactory().getPackageManifest(model.getProject());
        packageManifest.setVersion(installedIdeVersion);

        // remove existing, stored connection, change endpoint version, and get new connection
        ForceProject forceProject = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getForceProject(model.getProject());
        ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().removeConnection(forceProject);
        ContainerDelegate.getInstance().getFactoryLocator().getMetadataFactory().removeMetadataStubExt(forceProject);
        ContainerDelegate.getInstance().getFactoryLocator().getToolingFactory().removeToolingStubExt(forceProject);
        forceProject.setEndpointApiVersion(installedIdeVersion);
        Connection connection = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(forceProject);

        // perform retrieve
        RetrieveResultExt retrieveResultHandler = null;
        try {
            retrieveResultHandler = ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrieveSelective(connection,
                    localProjectPackageList, true, packageManifest, monitor);
        } catch (ServiceTimeoutException ex) {
            //FIXME: Best way to handle?
        }

        if (retrieveResultHandler == null) {
            logger.warn("Unable to get remote content - retrieve result is null");
            return remoteProjectPackageList;
        }

        monitorWork(monitor);

        remoteProjectPackageList = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageFactory().getProjectPackageListInstance();
        remoteProjectPackageList.setProject(model.getProject());
        remoteProjectPackageList.generateComponents(retrieveResultHandler.getZipFile(), retrieveResultHandler
                .getFileMetadataHandler(), monitor);

        return remoteProjectPackageList;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void finish(IProgressMonitor monitor) throws Exception {
    	if(monitor==null){
    		monitor = new NullProgressMonitor();
    	}

        IProgressService service = PlatformUI.getWorkbench().getProgressService();

        service.run(false, true, new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
                    InterruptedException {
            	if(monitor==null){
            		monitor = new NullProgressMonitor();
            	}
                monitor.beginTask("", 3);
                try {

                    if (getUpgradeModel().hasUpgradeComponents()) {
                        monitorSubTask(monitor, "Upgrade project component(s)");

                        // temp turn off builder
                        ContainerDelegate.getInstance().getServiceLocator().getProjectService().flagSkipBuilder(model.getProject());

                        // upgrade components
                        Map<String, List<UpgradeConflict>> upgradedComponents = getUpgradeModel().getUpgradeConflicts();
                        Set<String> componentTypes = upgradedComponents.keySet();
                        for (String componentType : componentTypes) {
                            List<UpgradeConflict> upgradeConflicts = upgradedComponents.get(componentType);
                            for (UpgradeConflict upgradeConflict : upgradeConflicts) {
                                // save remove, upgraded component to project
                                upgradeConflict.getRemoteComponent().saveToFile(true,
                                        new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));

                                // clear upgrade markers and any existing save marker
                                UpgradeMarkerUtils.clearAllUpgradeMarkers(upgradeConflict.getRemoteComponent()
                                        .getFileResource());

                                // handle associated composite resource, if applicable
                                if (upgradeConflict.getRemoteComponent().isMetadataComposite()) {
                                    IFile compositeFile = model.getProject().getFile(
                                            upgradeConflict.getRemoteComponent().getCompositeResourceFilePath());
                                    if (compositeFile != null && compositeFile.exists()) {
                                        UpgradeMarkerUtils.clearAllUpgradeMarkers(compositeFile);
                                    }

                                }
                            }

                        }

                        // upgrade package.xml version
                        Package packageManifest = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageManifestFactory().getPackageManifest(model.getProject());
                        packageManifest.setVersion(installedIdeVersion);
                        IFile packageManifestFile = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getPackageManifestFactory().getPackageManifestFile(
                                model.getProject());
                        ContainerDelegate.getInstance().getServiceLocator().getProjectService().saveToFile(packageManifestFile, packageManifest.getXMLString(), monitor);

                        monitorWork(monitor);

                        monitorSubTask(monitor, "Refresh referenced packages(s)");
                        // upgrade install, managed content
                        upgradeManagedInstalledPackages(model.getProject(), new SubProgressMonitor(monitor,
                                IProgressMonitor.UNKNOWN));
                        monitorWork(monitor);
                    }

                    // upgrade any ide stuff
                    try {
                        upgradeIdeInternals();
                    } catch (Exception e) {
                        logger.warn("Unable to perform upgrade of ide internals: " + e.getMessage());
                        undoIdeInternals();
                        throw new InvocationTargetException(e);
                    }

                    // update ide version saved w/ project
                    ContainerDelegate.getInstance().getServiceLocator().getProjectService().updateIdeVersion(model.getProject());
                    if (logger.isDebugEnabled()) {
                        String installedIdeVersion = ForceIdeCorePlugin.getBundleVersion(true);
                        logger.debug("Upgrade project to version current ide version [" + installedIdeVersion + "]");
                    }

                    monitorSubTask(monitor, "Re-enabling project");
                    // remove upgrade nature and re-apply online nature
                    UpgradeNature.removeNature(model.getProject(), monitor);

                    // clear upgrade problem marker
                    UpgradeMarkerUtils.clearUpgradeRequiredMarker(model.getProject(), true);

                    monitorWork(monitor);
                } catch (InterruptedException e) {
                    throw e;
                } catch (InvocationTargetException e) {
                    throw e;
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                        monitor.done();
                }
            }
        });
    }

    // refresh installed managed packages
    protected boolean upgradeManagedInstalledPackages(IProject project, IProgressMonitor monitor)
            throws InterruptedException, ForceConnectionException, ForceRemoteException, CoreException, IOException, ServiceException, Exception {
        if (project == null) { throw new IllegalArgumentException("Project cannot be null"); }

        if (logger.isInfoEnabled()) {
            logger.info("Fetching and saving all installed, managed components for '" + project.getName() + "'");
        }

        // set reference pkg folder contents to readonly=false so save of retrieve content
        // doesn't prompt with 'unable to write'
        IFolder referencePkgFolder = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getReferencedPackagesFolder(project);
        Utils.adjustResourceReadOnly(referencePkgFolder, false, true);

        monitorCheck(monitor);
        RetrieveResultExt retrieveResultHandler = null;
        try {
            retrieveResultHandler = ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().retrieveManagedInstalledPackages(
                    project, monitor);
        } catch (ServiceTimeoutException ex) {
            retrieveResultHandler = ContainerDelegate.getInstance().getServiceLocator().getPackageRetrieveService().handleRetrieveServiceTimeoutException(
                    ex, "upgrade resource(s)", monitor);
            if (retrieveResultHandler != null) {
                Connection connection = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getConnection(project);
                ProjectPackageList projectPackageList = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageFactory().getManagedInstalledProjectPackages(
                        connection);
                if (projectPackageList != null) {
                    projectPackageList.setProject(project);
                }
                retrieveResultHandler.setProjectPackageList(projectPackageList);
            }
        }

        if (retrieveResultHandler == null) {
            logger.warn("Unable to refresh installed packages - retrieve result is null");
            return false;
        }

        if (retrieveResultHandler.getZipFileCount() == 0) { return true; }

        if (Utils.isNotEmpty(retrieveResultHandler.getProjectPackageList())) {
            retrieveResultHandler.getProjectPackageList().setProject(project);
        }

        monitorCheck(monitor);
        return ContainerDelegate.getInstance().getServiceLocator().getProjectService().handleRetrieveResult(retrieveResultHandler.getProjectPackageList(),
                retrieveResultHandler, true, null, monitor);
    }

    /**
     * Execute upgrade method on each IDE internal upgrade class. Perform undo is upgrade unsuccessful. 
     * REVIEWME: if one is not successful, fail all?
     * 
     * @return
     */
    protected boolean upgradeIdeInternals() throws Exception {
        initIdeInterals();

        if (Utils.isNotEmpty(ideInternalUpgrades)) {
            for (IInternalUpgrade internalUpgrade : ideInternalUpgrades) {
                if (!internalUpgrade.upgrade()) {
                    internalUpgrade.undo();
                }
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("No internal IDE upgrades found for this release");
            }
        }

        return true;
    }

    /**
     * Discover and register IDE internal upgrade classes
     * 
     * @throws Exception
     */
    protected void initIdeInterals() throws Exception {
        Class<?>[] tmpIdeUpgradeClasses = getInternalUpgradeClasses();
        if (Utils.isEmpty(tmpIdeUpgradeClasses)) { return; }

        for (Class<?> tmpClass : tmpIdeUpgradeClasses) {
            if (tmpClass.isInterface() || Utils.isEmpty(tmpClass.getInterfaces())) {
                continue;
            } else if ((Arrays.asList(tmpClass.getInterfaces())).contains(IInternalUpgrade.class)) {
                ideInternalUpgrades.add((IInternalUpgrade)tmpClass.newInstance());
            }
        }
        sortIdeInternals();
    }

    // sort IDE internal class execution by their order value
    private void sortIdeInternals() {
        Collections.sort(ideInternalUpgrades, new Comparator<IInternalUpgrade>() {
            @Override
            public int compare(IInternalUpgrade o1, IInternalUpgrade o2) {
                if (o1 == o2) {
                    return 0;
                } else if (o1.getOrder() > o2.getOrder()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    /**
     * Execute undo method for each IDE internal class
     * 
     * @return
     */
    protected boolean undoIdeInternals() {
        for (IInternalUpgrade internalUpgrade : ideInternalUpgrades) {
            internalUpgrade.undo();
        }
        return true;
    }

    /**
     * Get list of class in com.salesforce.ide.upgrade.internal.ide package. Classes perform IDE upgrades.
     * 
     * @return
     * @throws Exception
     */
    protected Class<?>[] getInternalUpgradeClasses() throws Exception {
        String pkgname = this.getClass().getPackage().getName() + ".ide";

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for ide ugprade classes in package '" + pkgname + "'");
        }

        String pkgPath = pkgname.replace(".", "/");
        URL resource = ForceIdeUpgradePlugin.getFullUrlResource(pkgPath);
        if (resource == null) {
            logger.warn("Unable to load ide upgrade change class - resource for package '" + pkgPath + "' null");
            return null;
        }

        String path = null;
        try {
            path = FileLocator.resolve(resource).getPath();
        } catch (Exception e) {
            logger.warn("Could not get path from FileLocator: " + e.getMessage());
            path = resource.getFile();
        }

        if (Utils.isEmpty(path)) {
            logger.warn("Unable to get root resource for package '" + path + "'");
            return null;
        }

        ArrayList<Class<?>> classes = new ArrayList<>();
        if (path.contains(".jar!")) {
            String jarPath = path.substring(path.indexOf(":") + 1, path.lastIndexOf("!"));
            logger.info("Inspecting jar:\n " + jarPath);
            try (final JarInputStream jarFile = new JarInputStream(new FileInputStream(jarPath))) {
                while (true) {
                    JarEntry jarEntry = jarFile.getNextJarEntry();
                    if (jarEntry == null) {
                        break;
                    }

                    if (jarEntry.getName().startsWith(pkgname.replaceAll("\\.", "/"))
                            && jarEntry.getName().endsWith(".class")) {
                        String className = jarEntry.getName().substring(jarEntry.getName().lastIndexOf("/") + 1,
                                jarEntry.getName().lastIndexOf("."));
                        className = pkgname + "." + className;
                        try {
                            // classes.add(Class.forName(className));
                            classes.add(Class.forName(className, true, this.getClass().getClassLoader()));
                            if (logger.isDebugEnabled()) {
                                logger.debug("Added ide internal change class: " + className);
                            }
                        } catch (ClassNotFoundException e) {
                            logger.warn("Unable to add ide internal change class '" + className + "': " + e.getMessage());
                        }
                    }
                }
            }
        } else {
            // get a file object for the package
            File directory = new File(path);
            if (directory.exists()) {
                File[] files = directory.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".class");
                    }
                });

                if (Utils.isNotEmpty(files)) {
                    Arrays.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });

                    for (File file : files) {
                        String className = pkgname + '.' + file.getName().substring(0, file.getName().length() - 6);
                        try {
                            classes.add(Class.forName(className, true, this.getClass().getClassLoader()));
                            if (logger.isDebugEnabled()) {
                                logger.debug("Added ide internal change class: " + className);
                            }
                        } catch (Exception e) {
                            logger.warn("Unable to add class '" + className + "': " + e.getMessage());
                        }
                    }
                } else {
                    logger.warn(pkgname + " does not appear to be a valid package - class directory '" + path
                            + "' not found");
                }
            } else {
                logger.warn(pkgname + " does not appear to be a valid package - class directory '" + path
                        + "' not found");
            }
        }

        Class<?>[] classesA = new Class<?>[classes.size()];
        classes.toArray(classesA);
        return classesA;
    }
}
