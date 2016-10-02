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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.salesforce.ide.api.metadata.types.MetadataExt;
import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.factories.PackageManifestFactory;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.QuietCloseable;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.ZipUtils;
import com.salesforce.ide.core.internal.utils.ZipUtils.ZipStats;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.services.ProjectService;
import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.ws.bind.XmlObject;

public class ProjectPackage {
    private static final Logger logger = Logger.getLogger(ProjectPackage.class);

    private String id = null;
    private String orgId = null;
    private String description = null;
    private boolean managed = false;
    private boolean installed = false;
    private String name = null;
    private String versionName = null;
    private Component packageManifest = null;
    private MetadataExt deleteManifest = null;
    private Class<MetadataExt> metadataExtClass = null;
    private ComponentList componentList = getComponentListInstance();
    private IFolder packageRootFolder = null;
    private Map<String, byte[]> filePathZipMapping = null;
    protected ProjectService projectService = null;
    
    public ProjectPackage() {}

    public ProjectPackage(String name) {
        this.name = name;
    }

    public void parseInput(SObject sobject) {
        Iterator<XmlObject> iter = sobject.getChildren();
        while (iter.hasNext()) {
            XmlObject field = iter.next();
            String fieldName = field.getName().getLocalPart();
            String fieldValue = (String) field.getValue();

            if ("Id".equals(fieldName)) {
                setId(fieldValue);
            } else if ("Name".equals(fieldName)) {
                setName(fieldValue);
            } else if ("Description".equals(fieldName)) {
                setDescription(fieldValue);
            } else if ("IsManaged".equals(fieldName)) {
                setManaged(fieldValue);
            } else if ("VersionName".equals(fieldName)) {
                setInstalled(true); // version name is required field when upload package
                setVersionName(fieldValue);
            }
        }
    }

    public ProjectService getProjectService() {
        return projectService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public ComponentFactory getComponentFactory() {
        return projectService.getComponentFactory();
    }

    public PackageManifestFactory getPackageManifestFactory() {
        return projectService.getPackageManifestFactory();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String getOrgId) {
        this.orgId = getOrgId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isManaged() {
        return managed;
    }

    public void setManaged(boolean managed) {
        this.managed = managed;
    }

    public void setManaged(String managed) {
        this.managed = Boolean.valueOf(managed);
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public IFolder getPackageRootResource() {
        return packageRootFolder;
    }

    public void setPackageRootResource(IFolder packageRootFolder) {
        this.packageRootFolder = packageRootFolder;
    }

    public Component getPackageManifest() {
        if (packageManifest == null && Utils.isNotEmpty(componentList)) {
            Component component = componentList.getComponentForComponentType(Constants.PACKAGE_MANIFEST);
            if (component != null && component.isPackageManifest()) {
                packageManifest = component;
            }
        }
        return packageManifest;
    }

    public void setPackageManifest(Component packageManifest) {
        this.packageManifest = packageManifest;
    }

    public boolean hasPackageManifest() {
        return (packageManifest != null && packageManifest.getFileResource() != null);
    }

    public boolean containsPackageManifest() {
        if (Utils.isEmpty(componentList)) {
            return false;
        }

        for (Component tmpComponent : componentList) {
            if (tmpComponent.isPackageManifest()) {
                return true;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Package manifest not found in component list");
        }

        return false;
    }

    public MetadataExt getDeleteManifest() {
        return deleteManifest;
    }

    public com.salesforce.ide.api.metadata.types.Package getDeletePackageManifest() {
        return (com.salesforce.ide.api.metadata.types.Package) deleteManifest;
    }

    public void setDeleteManifest(MetadataExt deleteManifest) {
        this.deleteManifest = deleteManifest;
    }

    public Class<MetadataExt> getMetadataExtClass() {
        return metadataExtClass;
    }

    public com.salesforce.ide.api.metadata.types.Package newMetadataExtInstance() throws InstantiationException,
            IllegalAccessException {
        if (metadataExtClass == null) {
            return null;
        }
        return (com.salesforce.ide.api.metadata.types.Package) metadataExtClass.newInstance();
    }

    public void setMetadataExtClass(Class<MetadataExt> metadataExtClass) {
        this.metadataExtClass = metadataExtClass;
    }

    public ComponentList getComponentList() {
        return componentList;
    }

    public void setComponentList(ComponentList componentList) {
        this.componentList = componentList;
    }

    // lookup method injection by container
    public ComponentList getComponentListInstance() {
        return new ComponentList();
    }

    public Component getComponentByFileName(String fileName) {
        if (Utils.isEmpty(componentList) || Utils.isEmpty(fileName)) {
            return null;
        }

        Component component = null;
        for (Component tmpComponent : componentList) {
            if (fileName.equals(tmpComponent.getFileName())) {
                component = tmpComponent;
                break;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Component '" + fileName + "' not found in component list");
        }

        return component;
    }

    public Map<String, byte[]> getFilePathZipMapping() {
        return filePathZipMapping;
    }

    public void setFilePathZipMapping(Map<String, byte[]> filePathZipMapping) {
        this.filePathZipMapping = filePathZipMapping;
    }

    public void addFilePathZipMapping(String filePath, byte[] fileContent) {
        if (filePathZipMapping == null) {
            filePathZipMapping = new HashMap<>();
        }

        filePathZipMapping.put(filePath, fileContent);

        if (logger.isDebugEnabled()) {
            logger.debug("Added '" + filePath + "' to '" + getName() + "' package's filepath-zip mapping");
        }
    }

    public byte[] getFileFromFilePathZipMapping(String filePath) {
        if (Utils.isEmpty(filePath)) {
            throw new IllegalArgumentException("Filepath cannot be null");
        }

        if (Utils.isNotEmpty(filePathZipMapping)) {
            return filePathZipMapping.get(filePath);
        }

        logger.warn("File not found for '" + filePath + "'");

        return null;
    }

    public int getFilePathZipMappingCount() {
        return filePathZipMapping != null ? filePathZipMapping.keySet().size() : 0;
    }

    public TreeSet<String> getSortedFilePaths() {
        TreeSet<String> filePaths = new TreeSet<>();
        if (Utils.isNotEmpty(filePathZipMapping)) {
            filePaths.addAll(filePathZipMapping.keySet());
        }
        return filePaths;
    }

    public List<String> getFilePaths() {
        return getFilePaths(false);
    }

    public List<String> getFilePaths(boolean stripSourcePrefix) {
        if (filePathZipMapping != null) {
            return new ArrayList<>(filePathZipMapping.keySet());
        } else if (Utils.isNotEmpty(componentList)) {
            return componentList.getFilePaths(stripSourcePrefix);
        } else {
            return null;
        }
    }

    public List<String> getComponentFilePaths(boolean stripSourcePrefix) {
        if (filePathZipMapping != null) {
            return new ArrayList<>(filePathZipMapping.keySet());
        } else if (Utils.isNotEmpty(componentList)) {
            return componentList.getFilePaths(stripSourcePrefix, false);
        } else {
            return null;
        }
    }

    public ComponentList getComponentsByComponentType(String componentType) {
        if (Utils.isEmpty(componentList) || Utils.isEmpty(componentType)) {
            return null;
        }

        ComponentList typedComponentList = getComponentListInstance();
        for (Component component : componentList) {
            if (component.isMetadataInstance()) {
                continue;
            }

            if (componentType.equals(component.getComponentType())) {
                typedComponentList.add(component);
            }
        }
        return typedComponentList;
    }

    public boolean isComponentListEmpty() {
        return Utils.isEmpty(componentList);
    }

    public int getComponentCount(boolean includeManifest) {
        if (isComponentListEmpty()) {
            return 0;
        }

        if (includeManifest) {
            return componentList.size();
        }

        int cnt = 0;
        for (Component component : componentList) {
            if (component.isPackageManifest()) {
                continue;
            }
            cnt++;
        }
        return cnt;
    }

    public boolean hasComponent(Component component) {
        if (Utils.isEmpty(componentList)) {
            return false;
        }

        for (Component tmpComponent : componentList) {
            if (component.equals(tmpComponent)) {
                return true;
            }
        }
        return false;
    }

    public byte[] getBytes(String fileName) {
        byte[] file = null;
        if (filePathZipMapping != null && !filePathZipMapping.isEmpty() && Utils.isNotEmpty(fileName)) {
            Set<String> fileNames = filePathZipMapping.keySet();
            for (String tmpFileName : fileNames) {
                if (tmpFileName.lastIndexOf(fileName) > -1) {
                    file = filePathZipMapping.get(tmpFileName);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found bytes for file '" + fileName + "'");
                    }
                    break;
                }
            }
        }
        return file;
    }

    public byte[] getZip(boolean all) throws IOException {
        return getZip(all, false);
    }

    public byte[] getZip(boolean all, boolean manifestsOnly) throws IOException {
        return all ? getZipRoot(manifestsOnly) : getZipOfComponentList(manifestsOnly);
    }

    private byte[] getZipOfComponentList(boolean manifestsOnly) throws IOException {
        byte[] zipAsBytes = null;

        // get componentlist; contents of list will be zipped
        ComponentList componentList = getComponentList();
        if (Utils.isEmpty(componentList)) {
            return zipAsBytes;
        }

        try (final QuietCloseable<ByteArrayOutputStream> c0 = QuietCloseable.make(new ByteArrayOutputStream())) {
            final ByteArrayOutputStream bos = c0.get();

            // new zip stats to gather info about zip
            ZipStats stats = new ZipStats();

            try (final QuietCloseable<ZipOutputStream> c = QuietCloseable.make(new ZipOutputStream(bos))) {
                final ZipOutputStream zos = c.get();

                // add each component in component list to zip
                for (Component component : componentList) {
                    if (manifestsOnly && !component.isPackageManifest()) {
                        continue;
                    }

                    IFile file = component.getFileResource();
                    IPath path = file.getFullPath();
                    stats.addStats(ZipUtils.zipFile(file.getProjectRelativePath().toPortableString(), path.toFile(),
                        zos, Integer.MAX_VALUE));
                }
            }

            zipAsBytes = bos.toByteArray();

            if (logger.isDebugEnabled()) {
                logger.debug(stats.toString());
            }
            return zipAsBytes;
        }
    }

    private static byte[] getZipRoot(boolean manifestsOnly) {
        byte[] zipAsBytes = null;
        return zipAsBytes;
    }

    public void addComponentsToZip(ZipStats stats, ZipOutputStream zos, boolean manifestsOnly) throws IOException {
        if (Utils.isEmpty(componentList)) {
            return;
        }

        for (Component component : componentList) {
            if (manifestsOnly && !component.isPackageManifest()) {
                continue;
            }

            ZipStats tmpStats = null;
            String filePath = Utils.stripSourceFolder(component.getMetadataFilePath());
            // retrieve component content from file first instead of component body, see bug
            // W-576656
            if (component.getFileResource() != null || component.getBundleFolder() != null) {
                File file;
                if(component.getFileResource() != null) {
                    file = component.getFileResource().getRawLocation().toFile();
                } else {
                    file = component.getBundleFolder().getRawLocation().toFile();
                }
                if (!file.exists()) {
                    logger.warn("File '" + file.getAbsolutePath() + "' does not exist");
                    continue;
                }
                // get zip and add to zip stats
                tmpStats = ZipUtils.zipFile(filePath, file, zos, Integer.MAX_VALUE);
            } else if (component.getBody() != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Zipping content from component's body");
                }

                // get zip and add to zip stats
                tmpStats = ZipUtils.zipFile(filePath, component.getBody(), zos, Integer.MAX_VALUE);
            } else if (Utils.isNotEmpty(component.getFile())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Zipping content from derived component file");
                }

                // get zip and add to zip stats
                tmpStats = ZipUtils.zipFile(filePath, component.getFile(), zos, Integer.MAX_VALUE);
            } else {
                logger.warn(
                    "Unable to zip '" 
                    + filePath 
                    + "': File and body or file bytes for component "
                    + component.getFullDisplayName() + " is null");
                continue;
            }

            if (stats != null) {
                stats.addStats(tmpStats);
                logger.debug("Updated zip stats:\n" + stats.toString());
            }
        }

        // add delete manifest
        addDeleteManifestToZip(stats, zos);
    }

    public void addDeleteManifestToZip(ZipStats stats, ZipOutputStream zos) throws IOException {
        if (deleteManifest == null) {
            return;
        }

        String deleteManifestStr = null;
        try {
            deleteManifestStr = getPackageManifestFactory().getPackageManifestString(getDeletePackageManifest());
        } catch (JAXBException e) {
            logger.error("Unable to generate manifest into string", e);
            return;
        }

        if (Utils.isEmpty(deleteManifestStr)) {
            logger.warn("Delete manifest string is null - not including in zip");
            return;
        }

        // get zip and add to zip stats
        ZipStats tmpStats = ZipUtils.zipFile(Constants.DESTRUCTIVE_MANIFEST_FILE_NAME, deleteManifestStr, zos, Integer.MAX_VALUE);

        logger.debug(
            "Added  '" 
            + Constants.DESTRUCTIVE_MANIFEST_FILE_NAME 
            + "' to zip with zip stats:\n "
            + tmpStats.toString());

        if (stats != null) {
            stats.addStats(tmpStats);
            logger.debug("Updated zip stats:\n" + stats.toString());
        }
    }

    public void addComponent(Component component) {
        addComponent(
            component,
            PackageConfiguration.builder().build());
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

        // REVIEWME: overwrite existing, by default
        removeComponentByFilePath(component.getMetadataFilePath());

        if (Constants.PACKAGE_MANIFEST_FILE_NAME.equals(component.getName())) {
            setPackageManifest(component);
            if (logger.isDebugEnabled()) {
                logger.debug("Set package manifest on '" + getName() + "' project package");
            }
        }

        componentList.add(component, configuration);
    }

    public void addComponents(ComponentList componentList) {
        if (componentList == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }

        if (Utils.isEmpty(this.componentList)) {
            this.componentList = componentList;
        } else {
            this.componentList.addAll(componentList);
        }
    }

    public void addDeleteComponent(Component component) throws ForceProjectException, FactoryException {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }

        if (component.isPackageManifest()) {
            return;
        }

        if (getDeleteManifest() == null) {
            getPackageManifestFactory().attachDeleteManifest(this);

            // and if it's STILL null, abort
            if (getDeleteManifest() == null) {
                throw new ForceProjectException(
                        "Unable to add component to delete manifest - destructive manifest is missing or cannot be "
                        + "generated for package '" 
                        + getName() + "'");
            }
        }

        getPackageManifestFactory().addComponentToManifest(getDeletePackageManifest(), component);
        componentList.remove(component);
    }

    public void loadComponents(boolean includeManifest) throws CoreException, FactoryException {
        if (packageRootFolder == null || !packageRootFolder.exists()) {
            throw new IllegalArgumentException("Folder cannot be null");
        }

        ComponentList tmpComponentList = projectService.getComponentsForFolder(packageRootFolder, true, includeManifest);
        if (Utils.isEmpty(componentList)) {
            componentList = tmpComponentList;
        } else if (Utils.isNotEmpty(tmpComponentList)) {
            componentList.addAll(tmpComponentList);
        }
    }

    public boolean removeComponentByFilePath(String filePath) {
        Component existingComponent = componentList.getComponentByFilePath(filePath);
        if (existingComponent != null) {
            return componentList.remove(existingComponent);
        }
        return false;
    }

    public boolean removeAllComponents() {
        if (Utils.isEmpty(componentList)) {
            return false;
        }

        componentList.clear();

        return true;
    }

    public boolean removePackageManifestComponent() {
        if (Utils.isEmpty(componentList)) {
            return false;
        }

        for (Component component : componentList) {
            if (component.isPackageManifest()) {
                return componentList.remove(component);
            }
        }

        return false;
    }

    public void removeDeleteManifest() {
        deleteManifest = null;
    }

    public boolean hasChanged(Object obj) throws InterruptedException {
        return hasChanged(obj, false, new NullProgressMonitor());
    }

    public boolean hasChanged(Object obj, boolean includeManifest, IProgressMonitor monitor)
            throws InterruptedException {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof ProjectPackage == false)
            return false;
        final ProjectPackage other = (ProjectPackage) obj;

        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;

        if (orgId == null) {
            if (other.orgId != null)
                return false;
        } else if (!orgId.equals(other.orgId))
            return false;

        if (managed != other.managed)
            return false;

        if (versionName == null) {
            if (other.versionName!= null)
                return false;
        } else if (!versionName.equals(other.versionName))
            return false;

        if (componentList == null) {
            if (other.componentList != null)
                return false;
        } else if (componentList.isNotEmpty()) {
            for (Component component : componentList) {
                monitorCheck(monitor);
                if (!includeManifest && component.isPackageManifest()) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Skipping " + component.getDisplayName() + " comparison check");
                    }
                    continue;
                }
                
                if (component.isBundle()) {
                    if(component.hasRemoteBundleChanged(other, monitor)) {
                        return false;
                    }
                } else {
                    Component otherComponent = other.getComponentList().getComponentByFilePath(component.getMetadataFilePath());
                    if (component.hasRemoteChanged(otherComponent, monitor)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
        result = prime * result + (managed ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((versionName == null) ? 0 : versionName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ProjectPackage other = (ProjectPackage) obj;
        if (orgId == null) {
            if (other.orgId != null)
                return false;
        } else if (!orgId.equals(other.orgId))
            return false;
        if (managed != other.managed)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (versionName == null) {
            if (other.versionName != null)
                return false;
        } else if (!versionName.equals(other.versionName))
            return false;
        if (packageManifest == null) {
            if (other.packageManifest != null)
                return false;
        } else if (!packageManifest.equals(other.packageManifest))
            return false;
        if (componentList == null) {
            if (other.componentList != null)
                return false;
        } else if (!componentList.equals(other.componentList))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final String SEP = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue
        	.append("ProjectPackage ( ")
        	.append(super.toString())
        	.append(SEP)
        	.append("name = ")
        	.append(this.name)
            .append(SEP)
            .append("installed = ")
            .append(this.isInstalled())
            .append(SEP)
            .append("id = ")
            .append(this.id)
            .append(SEP)
            .append("orgId = ")
            .append(this.orgId)
            .append(SEP)
            .append("description = ")
            .append(this.description)
            .append(SEP)
            .append("managed = ")
            .append(this.managed)
            .append(SEP)
            .append("version name = ")
            .append(this.versionName)
            .append(SEP);

        if (Utils.isNotEmpty(componentList)) {
            retValue
            	.append("\n")
            	.append("  ")
            	.append(componentList.toString());
        } else {
            retValue.append(" no components");
        }
        retValue.append(" )");

        return retValue.toString();
    }

    protected void monitorCheck(IProgressMonitor monitor) throws InterruptedException {
        if (monitor != null) {
            monitor.worked(1);
            if (monitor.isCanceled()) {
                throw new InterruptedException("Operation cancelled");
            }
        }
    }

    protected void monitorWork(IProgressMonitor monitor, String subtask) {
        monitor.subTask(subtask);
        monitor.worked(1);
    }
}
