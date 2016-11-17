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
package com.salesforce.ide.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.factories.PackageManifestFactory;
import com.salesforce.ide.core.factories.ProjectPackageFactory;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.MessageDialogRunnable;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.QuietCloseable;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.ZipUtils;
import com.salesforce.ide.core.internal.utils.ZipUtils.ZipStats;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.core.services.hooks.SyncServiceListenerBroadcaster;
import com.sforce.soap.metadata.FileProperties;

/**
 * Encapsulates a project's package management including a package contents.
 * 
 * @author cwall
 */
public class ProjectPackageList extends ArrayList<ProjectPackage> {

    private static final Logger logger = Logger.getLogger(ProjectPackageList.class);

    private static final long serialVersionUID = 1L;

    private transient ProjectService projectService = null;
    private transient IProject project = null;

    public ProjectPackageList() {
        super();
    }

    public ProjectService getProjectService() {
        return projectService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public ProjectPackageFactory getProjectPackageFactory() {
        return projectService.getProjectPackageFactory();
    }

    public ComponentFactory getComponentFactory() {
        return projectService.getComponentFactory();
    }

    public PackageManifestFactory getPackageManifestFactory() {
        return projectService.getPackageManifestFactory();
    }

    public ProjectPackageList getProjectPackageListInstance() {
        return getProjectPackageFactory().getProjectPackageListInstance();
    }

    public ProjectPackage getProjectPackageInstance() {
        return getProjectPackageFactory().getProjectPackageInstance();
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public ProjectPackage getProjectPackageForComponent(Component component) {
        return getProjectPackage(component.getPackageName(), true);
    }

    public ProjectPackage addProjectPackageByName(String packageName) {
        return getProjectPackage(packageName, true);
    }

    @Override
    public boolean add(ProjectPackage projectPackage) {
        boolean success = super.add(projectPackage);
        return success;
    }

    public void addAll(String[] packageNames) {
        if (Utils.isNotEmpty(packageNames)) {
            for (String packageName : packageNames) {
                add(new ProjectPackage(packageName));
            }
        }
    }

    public ProjectPackage getProjectPackage(String packageName) {
        return getProjectPackage(packageName, true);
    }

    public ProjectPackage getProjectPackage(String packageName, boolean create) {
        if (Utils.isEmpty(packageName)) {
            throw new IllegalArgumentException("Package name cannot be null");
        }

        ProjectPackage foundProjectPackage = null;

        // dissecting is required if package is under referenced package
        String tmpPackageName = packageName;
        // strip down to just package name
        if (tmpPackageName.indexOf('/') > -1) {
            tmpPackageName = packageName.substring(0, packageName.indexOf('/'));
        }

        if (!isEmpty()) {
            // loop thru searching for existing instance of package
            for (ProjectPackage projectPackage : this) {
                if (tmpPackageName.equals(projectPackage.getName())) {
                    foundProjectPackage = projectPackage;
                    break;
                }
            }
        }

        // if existing project package not found, get from project or create a new instance
        if (foundProjectPackage == null && create) {
            try {
                foundProjectPackage = getProjectPackageFactory().getProjectPackage(getProject(), true);
                foundProjectPackage.setName(tmpPackageName);
                add(foundProjectPackage);
            } catch (FactoryException e) {
                logger.error(
                    "Unable to get project package for project '" 
                    + getProject().getName() 
                    + "' and package '"
                    + packageName 
                    + "'", e);
            }

        }
        return foundProjectPackage;
    }

    public ProjectPackage getDefaultPackageProjectPackage() {
        return getProjectPackage(Constants.DEFAULT_PACKAGED_NAME);
    }

    public String[] getNamedPackageNames() {
        List<String> packageNames = new ArrayList<>();
        for (ProjectPackage projectPackage : this) {
            String packageName = projectPackage.getName();
            if (Constants.DEFAULT_PACKAGED_NAME.equals(packageName)) {
                continue;
            }
            packageNames.add(packageName);
        }
        return packageNames.toArray(new String[packageNames.size()]);
    }

    public String[] getPackageNames(boolean includeDefault) {
        List<String> packageNames = new ArrayList<>();
        for (ProjectPackage projectPackage : this) {
            String packageName = projectPackage.getName();
            if (Constants.DEFAULT_PACKAGED_NAME.equals(packageName) && !includeDefault) {
                continue;
            }
            packageNames.add(packageName);
        }
        return packageNames.toArray(new String[packageNames.size()]);
    }

    public String[] getPackageNames() {
        return getPackageNames(true);
    }

    public ProjectPackageList getReferencedPackages() {
        if (isEmpty()) {
            return null;
        }

        ProjectPackageList projectPackageList = getProjectPackageListInstance();
        for (ProjectPackage projectPackage : this) {
            if (projectPackage.isInstalled()) {
                projectPackageList.add(projectPackage);
            }
        }
        return projectPackageList;
    }

    public boolean hasPackage(String packageName) {
        boolean contains = false;
        if (Utils.isNotEmpty(packageName)) {
            String[] packageNames = getPackageNames();
            if (Utils.isNotEmpty(packageNames)) {
                for (String tmpPackageName : packageNames) {
                    if (tmpPackageName.equals(packageName)) {
                        contains = true;
                        break;
                    }
                }
            }
        }
        return contains;
    }

    public boolean hasComponent(Component component) {
        if (isEmpty()) {
            return false;
        }

        for (ProjectPackage projectPackage : this) {
            if (projectPackage.hasComponent(component)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasComponents(boolean includeManifest) {
        if (isEmpty()) {
            return false;
        }

        for (ProjectPackage projectPackage : this) {
            if (Utils.isNotEmpty(projectPackage.getComponentList())) {
                for (Component component : projectPackage.getComponentList()) {
                    if (component.isPackageManifest() && !includeManifest) {
                        continue;
                    }
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasComponentsByType(String componentType) {
        if (isEmpty() || Utils.isEmpty(componentType)) {
            return false;
        }

        for (ProjectPackage projectPackage : this) {
            ComponentList componentList = projectPackage.getComponentsByComponentType(componentType);
            if (Utils.isNotEmpty(componentList)) {
                return true;
            }
        }

        return false;
    }

    public ComponentList getComponentsByType(String componentType) {
        if (isEmpty() || Utils.isEmpty(componentType)) {
            return null;
        }

        ComponentList componentList = getComponentFactory().getComponentListInstance();
        for (ProjectPackage projectPackage : this) {
            ComponentList tmpComponentList = projectPackage.getComponentsByComponentType(componentType);
            if (Utils.isNotEmpty(tmpComponentList)) {
                componentList.addAll(tmpComponentList);
            }
        }

        return componentList;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public byte[] getZip(boolean manifestsOnly) throws IOException {
        try (final QuietCloseable<ByteArrayOutputStream> c0 = QuietCloseable.make(new ByteArrayOutputStream())) {
            final ByteArrayOutputStream bos = c0.get();

            try (final QuietCloseable<ZipOutputStream> c = QuietCloseable.make(new ZipOutputStream(bos))) {
                final ZipOutputStream zos = c.get();
    
                // new zip stats to gather info about zip
                ZipStats stats = new ZipStats();
                for (ProjectPackage projectPackage : this) {
                    projectPackage.addComponentsToZip(stats, zos, manifestsOnly);
                }
        
                final byte[] zipAsBytes = bos.toByteArray();
        
                if (logger.isDebugEnabled()) {
                    logger.debug("Zip stats for entire project package list:\n" + stats.toString());
                    ZipUtils.writeDeployZipToTempDir(zipAsBytes);
                }
        
                return zipAsBytes;
            }
        }
    }

    public void parseZip(byte[] zipFile, IProgressMonitor monitor) throws IOException {
        if (zipFile == null) {
            throw new IllegalArgumentException("File zip cannot be null");
        }

        if (logger.isDebugEnabled()) {
            ZipUtils.writeRetrieveZipToTempDir(zipFile);
        }

        monitor.subTask("Parsing retrieved zip response...");

        List<String> folderNames = projectService.getComponentFactory().getFolderNamesForFolderComponents();

        try (final QuietCloseable<ZipInputStream> c = QuietCloseable.make(new ZipInputStream(new ByteArrayInputStream(zipFile)))) {
            final ZipInputStream zis = c.get();

            for (;;) {
                ZipEntry ze = zis.getNextEntry();
                if (ze == null) {
                    break;
                }

                byte[] fileContent = StreamUtils.getBytes(zis);
                String name = ze.getName();
                if (ze.isDirectory()) {
                    continue;
                }

                ProjectPackage projectPackage = null;
                // path starts with package name
                if (startsWithPackageName(folderNames, name)) {
                    projectPackage = getProjectPackage(name.split("/")[0]);
                } else if (size() > 0) {
                    projectPackage = get(0);
                } else {
                    projectPackage = getProjectPackage(Constants.DEFAULT_PACKAGED_NAME);
                }

                if (projectPackage == null) {
                    continue;
                }

                projectPackage.addFilePathZipMapping(name, fileContent);
            }
        }

        monitorWork(monitor);
    }

    // FIXME: this does not handle instances where the package name is the same name as the folder-based
    // component's default folder (<package>/<default-folder>/<customer-folder>/<component-full-name>).
    // for example "documents/documents/documents/doc.txt
    private static boolean startsWithPackageName(List<String> folderNames, String name) {
        if (Utils.isNotEmpty(name) && name.contains("/") && name.split("/").length > 2) {
            if (Utils.isNotEmpty(folderNames)) {
                for (String folderName : folderNames) {
                    if (name.startsWith(folderName + "/")) {
                        // attempt to handle scenario mentioned above
                        String[] parts = name.split("/");
                        if (parts.length == 3) {
                            return false;
                        } else if (parts.length > 4 && parts[1].equals(folderName)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Determine package name '" + parts[0] + "' from filepath '" + name + "'");
                            }
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        return false;
    }

    public void generateComponents(byte[] zipFile, FileMetadataExt fileMetadataHandler) throws InterruptedException,
            IOException {
        generateComponents(zipFile, fileMetadataHandler, new NullProgressMonitor());
    }

    public void generateComponents(byte[] zipFile, FileMetadataExt fileMetadataHandler, IProgressMonitor monitor)
            throws InterruptedException, IOException {
        generateComponentsForComponentTypes(zipFile, fileMetadataHandler, null, monitor);
    }

    public void generateComponentsForComponentTypes(byte[] zipFile, FileMetadataExt fileMetadataHandler,
            String[] componentTypes, IProgressMonitor monitor) throws InterruptedException, IOException {
        if (fileMetadataHandler == null) {
            throw new IllegalArgumentException("FileMetadataHandler, zip, and/or object types cannot be null");
        }

        if (zipFile != null) {
            parseZip(zipFile, new SubProgressMonitor(monitor, 2));
        } else {
            ProjectPackage projectPackage = getProjectPackage(Constants.DEFAULT_PACKAGED_NAME);
            FileProperties[] filePropertiesArry = fileMetadataHandler.getFileProperties();
            for (FileProperties fileProperties : filePropertiesArry) {
                String filePath = fileProperties.getFileName();
                if (Utils.isNotEmpty(filePath)) {
                    projectPackage.addFilePathZipMapping(filePath, null);
                }
            }

            add(projectPackage);
        }

        generateComponents(fileMetadataHandler, componentTypes, monitor);
    }

    private void generateComponents(
        FileMetadataExt fileMetadataHandler,
        String[] componentTypes,
        IProgressMonitor monitor) throws InterruptedException {

        if (fileMetadataHandler == null) {
            throw new IllegalArgumentException("ProjectPackageList and/or fileMetadataHandler cannot be null");
        }

        List<String> desiredComponentTypes = null;
        if (Utils.isNotEmpty(componentTypes)) {
            desiredComponentTypes = Arrays.asList(componentTypes);
        }

        for (ProjectPackage projectPackage : this) {
            monitorCheck(monitor);

            Map<String, byte[]> filePathZipMapping = null;

            // get filepath to zip file mapping
            filePathZipMapping = projectPackage.getFilePathZipMapping();
            if (Utils.isEmpty(filePathZipMapping)) {
                continue;
            }

            // sort by name
            TreeSet<String> filePaths = projectPackage.getSortedFilePaths();

            // for each file in zip, create component object
            if (Utils.isEmpty(filePaths)) {
                continue;
            }

            for (String filePath : filePaths) {
                monitorCheck(monitor);

                byte[] fileBytes = filePathZipMapping.get(filePath);
                // using associated factory, create component and set file properties
                Component component = null;
                try {
                    component = getComponentFactory().createComponent(
                        projectPackage,
                        filePath,
                        fileBytes,
                        fileMetadataHandler);
                } catch (Exception e) {
                    logger.error("Unable to create component for file path '" + filePath + "'", e);
                    continue;
                }

                // filter out undesirable objects, if applicable
                if (!isDesiredComponentType(desiredComponentTypes, component)) {
                    continue;
                }

                // add component to project package
                if (component != null) {
                    projectPackage.addComponent(component);
                } else {
                    logger.warn(
                        "Unable to add '" 
                        + filePath 
                        + "' has an component to '" 
                        + projectPackage.getName()
                        + "' project package, component is null");
                }
            }
        }

        monitorWorkCheck(monitor);
    }

    // if component is not package.xml, check if the type exists in designate list.  if folder, check sub type
    boolean isDesiredComponentType(List<String> designatedSaveComponentTypes, Component component) {
        if (component.isPackageManifest() || Utils.isEmpty(designatedSaveComponentTypes)) {
            return true;
        }
        
        return designatedSaveComponentTypes.contains(component.getComponentType())
            || (Utils.isNotEmpty(component.getSecondaryComponentType()) && designatedSaveComponentTypes.contains(component.getSecondaryComponentType()));
    }

    public int getFilePathZipMappingCount() {
        int count = 0;
        for (ProjectPackage projectPackage : this) {
            count += projectPackage.getFilePathZipMappingCount();
        }
        return count;
    }

    public List<String> getFilePaths(boolean stripSourcePrefix) {
        List<String> filePaths = new ArrayList<>();
        for (ProjectPackage projectPackage : this) {
            List<String> tmpFilePaths = projectPackage.getFilePaths(stripSourcePrefix);
            if (Utils.isNotEmpty(tmpFilePaths)) {
                filePaths.addAll(tmpFilePaths);
            }
        }
        return filePaths;
    }

    public List<String> getComponentFilePaths(boolean stripSourcePrefix) {
        List<String> filePaths = new ArrayList<>();
        for (ProjectPackage projectPackage : this) {
            List<String> tmpFilePaths = projectPackage.getComponentFilePaths(stripSourcePrefix);
            if (Utils.isNotEmpty(tmpFilePaths)) {
                filePaths.addAll(tmpFilePaths);
            }
        }
        return filePaths;
    }

    public byte[] getFileBytesForFilePath(String filePath) {
        byte[] fileBytes = null;
        for (ProjectPackage projectPackage : this) {
            fileBytes = projectPackage.getFileFromFilePathZipMapping(filePath);
            if (Utils.isNotEmpty(fileBytes)) {
                break;
            }
        }
        return fileBytes;
    }

    public String[] getFilePathArray(boolean stripSourcePrefix) {
        List<String> filePaths = getFilePaths(stripSourcePrefix);
        return Utils.isNotEmpty(filePaths) ? filePaths.toArray(new String[filePaths.size()]) : null;
    }

    public String[] getComponentFilePathArray(boolean stripSourcePrefix) {
        List<String> filePaths = getComponentFilePaths(stripSourcePrefix);
        return Utils.isNotEmpty(filePaths) ? filePaths.toArray(new String[filePaths.size()]) : null;
    }

    public void addAllComponents(ComponentList components) {
        if (Utils.isEmpty(components)) {
            return;
        }

        for (Component component : components) {
            addComponent(component, true);
        }
    }

    public void addComponents(ComponentList components, boolean includeComposite) {
        addComponents(components, 
            PackageConfiguration.builder()
            .setIncludeComposite(includeComposite)
            .build());
    }

    public void addComponents(ComponentList components, PackageConfiguration configuration) {
        if (Utils.isEmpty(components)) {
            return;
        }

        for (Component component : components) {
            addComponent(component, configuration);
        }
    }

    public void addComponent(Component component) {
        addComponent(component, true);
    }

    public void addComponent(Component component, boolean includeComposite) {
        addComponent(
            component,
            PackageConfiguration.builder()
            .setIncludeComposite(includeComposite)
            .build());
    }

    public void addComponent(Component component, PackageConfiguration configuration) {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }

        ProjectPackage projectPackage = getProjectPackageForComponent(component);

        if (projectPackage == null) {
            return;
        }

        projectPackage.addComponent(component, configuration);
    }

    public void addDeleteComponent(Component component) throws ForceProjectException, FactoryException {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }

        ProjectPackage projectPackage = getProjectPackageForComponent(component);

        if (projectPackage == null) {
            return;
        }

        projectPackage.addDeleteComponent(component);
    }

    // metadata filepath and filepath are the same for non-installed packages;
    // for installed packages filepath is prefixed with the package name
    public Component getComponentByFilePath(String filePath) {
        if (isEmpty() || Utils.isEmpty(filePath)) {
            return null;
        }

        for (ProjectPackage projectPackage : this) {
            ComponentList componentList = projectPackage.getComponentList();
            for (Component component : componentList) {
                if (Utils.isNotEmpty(component.getMetadataFilePath())
                        && isEqualStripSourcePrefix(filePath, component.getMetadataFilePath())) {
                    return component;
                }
            }
        }

        logger.warn("Did not find component for '" + filePath + "'");
        return null;
    }

    public Component getComponentByNameType(String name, String componentType) {
        if (isEmpty() || Utils.isEmpty(name) || Utils.isEmpty(componentType)) {
            return null;
        }

        for (ProjectPackage projectPackage : this) {
            ComponentList componentList = projectPackage.getComponentsByComponentType(componentType);
            for (Component component : componentList) {
                if (Utils.isNotEmpty(component.getName()) && component.getName().equals(name)) {
                    return component;
                }
            }
        }

        logger.warn("Did not find " + componentType + " component for '" + name + "'");
        return null;
    }

    public Component getApexCodeComponent(String name, String message) {
        if (Utils.isEmpty(name)) {
            return null;
        }

        // REVIEWME: attempt to get either class or trigger, but what if the names are the same?
        //           another option is to use the ids.
        Component component1 = getComponentByNameType(name, Constants.APEX_CLASS);
        Component component2 = getComponentByNameType(name, Constants.APEX_TRIGGER);
        if (component1 != null && component2 != null && Utils.isNotEmpty(message)) {
            if (message.toLowerCase().contains("trigger")) {
                component1 = component2;
            }
        }

        return (component1 != null ? component1 : component2);
    }

    public Component getComponentByFileName(String fileName) {
        if (isEmpty() || Utils.isEmpty(fileName)) {
            return null;
        }

        for (ProjectPackage projectPackage : this) {
            ComponentList componentList = projectPackage.getComponentList();
            for (Component component : componentList) {
                if (Utils.isNotEmpty(component.getFileName()) && fileName.equals(component.getFileName())) {
                    return component;
                }
            }
        }

        logger.warn("Did not find component for '" + fileName + "'");
        return null;
    }

    // metadata filepath and filepath are the same for non-installed packages;
    // for installed packages filepath is prefixed with the package name
    public Component getComponentByMetadataFilePath(String fileName) {
        if (isEmpty() || Utils.isEmpty(fileName)) {
            return null;
        }

        for (ProjectPackage projectPackage : this) {
            ComponentList componentList = projectPackage.getComponentList();
            for (Component component : componentList) {
                if (Utils.isNotEmpty(component.getMetadataFilePath())
                        && fileName.equals(component.getMetadataFilePath())) {
                    return component;
                }
            }
        }

        logger.warn("Did not find component for '" + fileName + "'");
        return null;
    }

    private static boolean isEqualStripSourcePrefix(String filepathA, String filepathB) {
        if (Utils.isEmpty(filepathA) || Utils.isEmpty(filepathB)) {
            return false;
        }

        filepathA = Utils.stripSourceFolder(filepathA);
        filepathB = Utils.stripSourceFolder(filepathB);

        return filepathA.equals(filepathB);

    }

    public Component getComponentById(String id) {
        Component component = null;
        if (!isEmpty() && Utils.isNotEmpty(id)) {
            for (ProjectPackage projectPackage : this) {
                ComponentList componentList = projectPackage.getComponentList();
                for (Component tmpComponent : componentList) {
                    if (id.equals(tmpComponent.id)) {
                        component = tmpComponent;
                        break;
                    }
                }
            }
        }

        return component;
    }

    public List<Component> getComponentsById(String id) {
        List<Component> components = new ArrayList<>();
        if (!isEmpty() && Utils.isNotEmpty(id)) {
            for (ProjectPackage projectPackage : this) {
                ComponentList componentList = projectPackage.getComponentList();
                for (Component tmpComponent : componentList) {
                    if (id.equals(tmpComponent.id)) {
                        components.add(tmpComponent);
                    }
                }
            }
        }

        return components;
    }

    public boolean removeComponent(Component component) {
        if (isEmpty()) {
            return false;
        }

        boolean result = false;
        for (ProjectPackage projectPackage : this) {
            ComponentList componentList = projectPackage.getComponentList();
            if (!componentList.contains(component)) {
                continue;
            }

            result = componentList.remove(component);
            if (result) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Removed " + component.getFullDisplayName() + " from component list");
                }
                break;
            }
        }
        return result;
    }

    public Component removeComponentByFilePath(String filePath, boolean includeDeleteManifest, boolean includeComposite) {
        if (isEmpty() || Utils.isEmpty(filePath)) {
            return null;
        }

        Component component = null;
        for (ProjectPackage projectPackage : this) {
            ComponentList componentList = projectPackage.getComponentList();
            if (!componentList.hasComponentByFilePath(filePath)) {
                continue;
            }

            component = componentList.getComponentByFilePath(filePath);
            boolean result = componentList.remove(component);
            if (result && component != null) {
                // delete from manifest
                if (includeDeleteManifest && projectPackage.getDeletePackageManifest() != null) {
                    result = getPackageManifestFactory()
                        .removeFromDeleteManifest(projectPackage.getDeletePackageManifest(), component);
                    if (result) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Removed " + filePath + " from delete manifest");
                        }
                    } else {
                        logger.warn("Failed to remove " + filePath + " from delete manifest");
                    }
                }

                // delete composite, if applicable
                if (includeComposite && component.isMetadataComposite()
                        && Utils.isNotEmpty(component.getCompositeMetadataFilePath())) {
                    Component tmpComponent =
                            componentList.getComponentByFilePath(component.getCompositeMetadataFilePath());
                    if (tmpComponent != null) {
                        componentList.remove(tmpComponent);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Removed composite " + filePath + " from component list");
                        }
                    } else {
                        logger.warn("Failed to remove composite " + filePath + " from component list");
                    }
                }
            } else {
                logger.warn("Failed to remove " + filePath + " from component list");
            }
        }
        return component;
    }

    public boolean removeComponentsByType(String componentType) {
        if (isEmpty() || Utils.isEmpty(componentType)) {
            return false;
        }

        boolean result = true;
        for (ProjectPackage projectPackage : this) {
            ComponentList componentList = projectPackage.getComponentsByComponentType(componentType);
            if (Utils.isEmpty(componentList)) {
                continue;
            }

            List<String> filepaths = componentList.getFilePaths();
            for (String filepath : filepaths) {
                boolean tmpResult = projectPackage.removeComponentByFilePath(filepath);

                if (!tmpResult) {
                    result = false;
                }
            }
        }
        return result;
    }

    public boolean removeAllComponents() {
        if (isEmpty()) {
            return false;
        }

        boolean result = true;
        for (ProjectPackage projectPackage : this) {
            projectPackage.removeAllComponents();
        }
        return result;
    }

    public int getComponentCount(boolean includeManifest) {
        int componentCnt = 0;
        if (!isEmpty()) {
            for (ProjectPackage projectPackage : this) {
                componentCnt += projectPackage.getComponentCount(includeManifest);
            }
        }
        return componentCnt;
    }

    public ComponentList getAllComponents() {
        return getAllComponents(true);
    }

    public ComponentList getAllComponents(boolean includeManifest) {
        ComponentList componentList = getProjectService().getComponentFactory().getComponentListInstance();
        for (ProjectPackage projectPackage : this) {
            if (Utils.isNotEmpty(projectPackage.getComponentList())) {
                ComponentList tmpComponentList = projectPackage.getComponentList();
                for (Component component : tmpComponentList) {
                    if (component.isPackageManifest() && !includeManifest) {
                        continue;
                    }
                    componentList.add(component);
                }
            }
        }
        return componentList;
    }

    public List<IResource> getAllComponentResources(boolean includeManifest) {
        List<IResource> resources = new ArrayList<>();

        if (isEmpty()) {
            return resources;
        }

        ComponentList componentList = getAllComponents();
        for (Component component : componentList) {
            if (component.isPackageManifest() && !includeManifest) {
                continue;
            }

            if (component.getFileResource() == null) {
                logger.warn("File resource not found for component " + component.getFullDisplayName());
                continue;
            }
            resources.add(component.getFileResource());
        }
        return resources;
    }

    public List<IResource> getComponentResourcesForComponentTypes(String[] componentTypes) {
        List<IResource> resources = new ArrayList<>();

        if (isEmpty() || Utils.isEmpty(componentTypes)) {
            return resources;
        }

        for (ProjectPackage projectPackage : this) {
            for (String componentType : componentTypes) {
                ComponentList componentList = projectPackage.getComponentsByComponentType(componentType);
                if (Utils.isNotEmpty(componentList)) {
                    resources.addAll(componentList.getResources());
                }
            }
        }

        return resources;
    }

    public ComponentList getComponentsNotFound(ProjectPackageList remoteProjectPackageList) {
        if (isEmpty()) {
            return null;
        } else if (remoteProjectPackageList == null || remoteProjectPackageList.isEmpty()) {
            return getAllComponents();
        }

        ComponentList componentList = getProjectService().getComponentFactory().getComponentListInstance();

        for (ProjectPackage projectPackage : this) {
            if (Utils.isNotEmpty(projectPackage.getComponentList())) {
                ComponentList tmpComponentList = projectPackage.getComponentList();
                for (Component component : tmpComponentList) {
                    if (component.isPackageManifest()) {
                        continue;
                    }

                    if (!remoteProjectPackageList.hasComponent(component)) {
                        componentList.add(component);
                    }
                }
            }
        }
        return componentList;
    }

    public void saveResources(IProgressMonitor monitor) throws InterruptedException {
        saveResources(project, null, monitor);
    }

    public void saveResources(IProject project, IProgressMonitor monitor) throws InterruptedException {
        saveResources(project, null, monitor);
    }

    public void saveResources(String[] componentTypes, IProgressMonitor monitor) throws InterruptedException {
        saveResources(project, componentTypes, monitor);
    }

    private void saveResources(IProject project, String[] componentTypes, IProgressMonitor monitor)
            throws InterruptedException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        if (isEmpty()) {
            logger.warn("Project package list is empty.  No resources to save.");
        }

        List<String> designatedSaveComponentTypes = null;
        if (Utils.isNotEmpty(componentTypes)) {
            designatedSaveComponentTypes = Arrays.asList(componentTypes);
        }

        boolean skipAllReadOnlyExceptions = false;
        for (ProjectPackage projectPackage : this) {
            monitorCheck(monitor);
            ComponentList componentList = projectPackage.getComponentList();
            if (Utils.isEmpty(componentList)) {
                continue;
            }

            int savedCount = 0;
            int totalCount = componentList.size();
            monitor.beginTask(Messages.getString("Components.Generating"), totalCount);
            for (Component component : componentList) {
                monitorCheck(monitor);

                // If provided, only save selected object types
                if (Utils.isNotEmpty(componentTypes)
                    && !isDesignatedSaveComponentType(designatedSaveComponentTypes, component)) {
                    continue;
                }

                // Do not save the packageManifest response since that is only for this particular deploy
                if(component.getComponentType().equals(Constants.PACKAGE_MANIFEST)) {
                    if (Utils.isEmpty(componentTypes)
                        || !isDesignatedSaveComponentType(designatedSaveComponentTypes, component)) {
                        continue;
                    }
                }
                
                try {
                    monitor.setTaskName(
                        Messages.getString("Components.Generating.Updating",
                        new Object[] { savedCount++, totalCount }));
                    monitor.worked(1);
                    component.saveToFile(project, projectPackage, new SubProgressMonitor(monitor, 1));
                } catch (OperationCanceledException e) {
                    break;
                } catch (CoreException e) {
                    if (ForceExceptionUtils.isReadOnlyException(e)) {
                        if (!skipAllReadOnlyExceptions) {
                            skipAllReadOnlyExceptions = handleReadOnlyException(e, component);
                        }
                        component.handleReadOnlyFile();
                    }
                } catch (Exception e) {
                    handleSaveException(e, component);
                }
            }

            SyncServiceListenerBroadcaster.broadcast(componentList);
        }
        
    }

    private static boolean handleReadOnlyException(CoreException coreException, Component component) {
        boolean skipAllReadOnlyExceptions = false;
        if (ForceExceptionUtils.isReadOnlyException(coreException) && !skipAllReadOnlyExceptions) {
            String message = ForceExceptionUtils.getStrippedExceptionMessage(coreException.getMessage());
            StringBuffer strBuff = new StringBuffer(Messages.getString("Components.SaveResourceError.message"));
            strBuff
                .append(":\n\n")
                .append(message)
                .append("\n\n")
                .append(Messages.getString("Components.SaveResourceError.SkipAllReadOnly.message"));

            MessageDialogRunnable messageDialogRunnable = new MessageDialogRunnable(
                "Cannot Write to File",
                null,
                strBuff.toString(),
                MessageDialog.WARNING,
                new String[] { IDialogConstants.NO_LABEL, IDialogConstants.YES_TO_ALL_LABEL },
                0);
            Display.getDefault().syncExec(messageDialogRunnable);

            if (messageDialogRunnable.getAction() == 1) {
                skipAllReadOnlyExceptions = true;
                logger.warn("Skipping all further read-only exceptions");
            }
        }
        return skipAllReadOnlyExceptions;
    }

    private static void handleSaveException(Exception exception, Component component) throws InterruptedException {
        String message = ForceExceptionUtils.getStrippedExceptionMessage(exception.getMessage());
        logger.warn("Unable to save " + component.getFullDisplayName() + " to file - " + message);
        StringBuffer strBuff = new StringBuffer(Messages.getString("Components.SaveResourceError.message"));
        strBuff.append(":\n\n").append(message).append("\n\n")
                .append(Messages.getString("Components.SaveResourceError.ContinueWithSaving.message"));

        MessageDialogRunnable messageDialogRunnable =
                new MessageDialogRunnable("Cannot Write to File", null, strBuff.toString(), MessageDialog.WARNING,
                        new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
        Display.getDefault().syncExec(messageDialogRunnable);

        if (messageDialogRunnable.getAction() == 1) {
            throw new InterruptedException("Save components to project canceled");
        }
    }

    private static boolean isDesignatedSaveComponentType(
        List<String> designatedSaveComponentTypes,
        Component component) {
        return designatedSaveComponentTypes.contains(component.getComponentType())
            || (Utils.isNotEmpty(component.getSecondaryComponentType())
                && designatedSaveComponentTypes.contains(component.getSecondaryComponentType()));
    }
    
    public String[] getComponentTypes(boolean includeManifest) {
        if (isEmpty()) {
            return null;
        }

        Set<String> componentTypes = new HashSet<>();
        for (ProjectPackage projectPackage : this) {
            ComponentList componentList = projectPackage.getComponentList();
            if (Utils.isEmpty(componentList)) {
                continue;
            }

            for (Component component : componentList) {
                if (component.isPackageManifest() && !includeManifest) {
                    continue;
                }
                componentTypes.add(component.getComponentType());
            }
        }

        return Utils.isNotEmpty(componentTypes) ? componentTypes.toArray(new String[componentTypes.size()]) : null;
    }

    public void removeDeleteManifests() {
        if (isEmpty()) {
            return;
        }

        for (ProjectPackage projectPackage : this) {
            projectPackage.removeDeleteManifest();
        }
    }

    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("ProjectPackageList ( ").append("count = ").append(size()).append(TAB);
        if (!isEmpty()) {
            int projectPackageCnt = 0;
            for (ProjectPackage projectPackage : this) {
                retValue.append("\n (");
                retValue.append(++projectPackageCnt);
                retValue.append(") ");
                retValue.append(projectPackage.toString());
            }
        }
        retValue.append(" )");

        return retValue.toString();
    }

    public String toStringLite() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("ProjectPackageList ( ").append("count = ").append(size()).append(TAB);
        String[] packageNames = getPackageNames();
        if (Utils.isNotEmpty(packageNames)) {
            retValue.append(" , packages {");
            for (String packageName : packageNames) {
                retValue.append(packageName);
            }
            retValue.append("}");
        }
        retValue.append(" )");

        return retValue.toString();
    }

    protected void monitorCheck(IProgressMonitor monitor) throws InterruptedException {
        if (monitor != null) {
            if (monitor.isCanceled()) {
                throw new InterruptedException("Operation cancelled");
            }
        }
    }

    protected void monitorWork(IProgressMonitor monitor, String subtask) {
        if (monitor == null) {
            return;
        }

        monitor.subTask(subtask);
        monitor.worked(1);
        if (logger.isDebugEnabled()) {
            logger.debug(subtask);
        }
    }

    protected void monitorWorkCheck(IProgressMonitor monitor) throws InterruptedException {
        monitorCheck(monitor);
        monitorWork(monitor);
    }

    protected void monitorWork(IProgressMonitor monitor) {
        if (monitor == null) {
            return;
        }

        monitor.worked(1);
    }
}
