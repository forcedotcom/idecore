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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.api.metadata.types.PackageTypeMembers;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceException;
import com.salesforce.ide.core.services.RetrieveException;
import com.sforce.soap.metadata.RetrieveRequest;

/**
 * Encapsulates functionality related managing metadata package manifests.
 *
 * @author cwall
 */
public class PackageManifestFactory extends BaseFactory {

    private static final Logger logger = Logger.getLogger(PackageManifestFactory.class);

    private List<String> defaultDisabledRetrieveComponentTypes = null;

    public List<String> getDefaultDisabledRetrieveComponentTypes() {
        return defaultDisabledRetrieveComponentTypes;
    }

    public void setDefaultDisabledRetrieveComponentTypes(List<String> defaultDisabledRetrieveComponentTypes) {
        this.defaultDisabledRetrieveComponentTypes = defaultDisabledRetrieveComponentTypes;
    }

    public Component getPackageManifestComponentInstance() {
        return getComponentFactory().getComponentByComponentType(Constants.PACKAGE_MANIFEST);
    }

    /**
     * Generate a default manifest with registered component types.
     *
     * @return
     */
    public Package getDefaultPackageManifest() {
        return createDefaultPackageManifest();
    }

    /**
     * Parse project's default manifest file creating API package manifest.
     *
     * @param project
     * @return
     * @throws FactoryException
     */
    public Package getDefaultPackageManifest(IProject project) throws FactoryException {
        return getPackageManifest(project, Constants.DEFAULT_PACKAGED_NAME);
    }

    public Package getPackageManifest(IProject project) throws FactoryException {
        if (project == null) {
            throw new IllegalArgumentException("Project and/or package name cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Getting manifest in project '" + project.getName() + "'");
        }

        // find manifest file for package
        IFile manfiestFile = getPackageManifestFile(project);

        // generate package object by parsing package file
        Package packageManifest = null;

        if (manfiestFile != null && manfiestFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found existing existing package manifest.");
            }
            try {
                packageManifest = parsePackageManifest(manfiestFile.getRawLocation().toFile());
            } catch (Exception e) {
                logger.error("Unable to create package manifest", e);
                throw new FactoryException(e);
            }

        }
        return packageManifest;
    }

    /**
     * Create API package manifest for a given package name in a project. If manifest file exists, parse into API
     * package manifest, otherwise generate a default manifest with registered component types.
     *
     * @param project
     * @param packageName
     * @return
     * @throws FactoryException
     * @throws FactoryException
     */
    public Package getPackageManifest(IProject project, String packageName) throws FactoryException {
        if (Utils.isEmpty(packageName) || project == null) {
            throw new IllegalArgumentException("Project and/or package name cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Getting manifest for package '" + packageName + "' in project '" + project.getName() + "'");
        }

        // find package manifest in project
        Package packageManifest = getPackageManifest(project);

        if (logger.isDebugEnabled()) {
            logger.debug("Could not find existing package manifest.  Creating default package manifest.");
        }

        // not found in project, create one on fly
        if (packageManifest == null) {
            try {
                Connection connection = getConnectionFactory().getConnection(project);
                packageManifest = getDefaultPackageManifest(connection);
            } catch (Exception e) {
                logger.warn("Unable to create manifest from permissible object types.  Creating default manifest.");
                packageManifest = createDefaultPackageManifest();
            }
        }

        return packageManifest;
    }

    public String getPackageNameFromPackageManifest(IProject project) throws FactoryException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        Package packageManifest = getPackageManifest(project);
        return packageManifest != null ? packageManifest.getFullName() : null;
    }

    public void setDefaultPackageManifest(Connection connection, RetrieveRequest retrieveRequest) {
        Package packageManifest = getDefaultPackageManifest(connection);
        retrieveRequest.setUnpackaged(convert(packageManifest));
    }

    public Package getDefaultPackageManifest(Connection connection) {
        return createPackageManifest(connection, Constants.DEFAULT_PACKAGED_NAME);
    }

    public Package getDefaultPackageManifest(ProjectPackageList projectPackageList) throws RetrieveException {
        if (projectPackageList == null) {
            throw new IllegalArgumentException("Project package cannot be null");
        }

        Package defaultPackageManifest = null;
        ProjectPackage defaultPackageProjectPackage = projectPackageList.getDefaultPackageProjectPackage();
        try {
            if (defaultPackageProjectPackage.getPackageManifest() != null
                    && defaultPackageProjectPackage.hasPackageManifest()) {
                IFile packageManifestFile = defaultPackageProjectPackage.getPackageManifest().getFileResource();
                if (packageManifestFile.exists()) {
                    defaultPackageManifest = getPackageManifestFromFile(packageManifestFile);
                } else {
                    defaultPackageManifest = getDefaultPackageManifest();
                }
            } else if (projectPackageList.getProject() != null) {
                defaultPackageManifest = getDefaultPackageManifest(projectPackageList.getProject());
            } else if (projectPackageList.getProject() == null) {
                defaultPackageManifest = getDefaultPackageManifest();
            }
        } catch (Exception e) {
            logger.error("Unable to create default package manifest", e);
            throw new RetrieveException("Unable to create default package manifest", e);
        }
        return defaultPackageManifest;
    }

    /**
     * @param project
     * @param packageName
     * @return
     * @throws FactoryException
     */
    public Component getPackageManifestComponent(IProject project) throws FactoryException {
        IFile manfiestFile = getPackageManifestFile(project);
        return getComponentFactory().getComponentFromFile(manfiestFile);
    }

    public IFile getPackageManifestFile(IProject project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Getting manifest file in project '" + project.getName() + "'");
        }

        // find manifest file for package
        IFile manfiestFile = project.getFile("src/" + Constants.PACKAGE_MANIFEST_FILE_NAME);
        if (manfiestFile == null) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug(Constants.PACKAGE_MANIFEST_FILE_NAME + " not found in project '" + project.getName()
                                + "'");
            }
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Filepath for manifest is '" + manfiestFile.getRawLocation().toOSString() + "'");
        }

        return manfiestFile;
    }

    public Document getPackageManifestDOMDocument(IProject project) throws FactoryException {
        IFile packageManifestFile = getPackageManifestFile(project);
        Document document = null;
        try {
            InputSource content = new InputSource(packageManifestFile.getContents());
            DOMParser parser = new DOMParser();
            parser.parse(content);
            document = parser.getDocument();
        } catch (CoreException ce) {
            String logMessage = Utils.generateCoreExceptionLog(ce);
            logger.error("Core exception occurred when marshalling package manifest into DOM document: " + logMessage,
                ce);
            throw new FactoryException("Unable to create DOM document from path "
                    + packageManifestFile.getProjectRelativePath().toPortableString(), ce);
        } catch (SAXException se) {
            logger.error("SAX exception occurred when marshalling package manifest into DOM document", se);
            throw new FactoryException("Unable to create DOM document from path "
                    + packageManifestFile.getProjectRelativePath().toPortableString(), se);
        } catch (IOException ioe) {
            logger.error("IO exception occurred when marshalling package manifest into DOM document", ioe);
            throw new FactoryException("Unable to create DOM document from path "
                    + packageManifestFile.getProjectRelativePath().toPortableString(), ioe);
        }
        return document;
    }

    public Package createPackageManifest(String packageName) {
        Package packageManifest = createGenericDefaultPackageManifest();

        if (Utils.isNotEmpty(packageName) && !Constants.DEFAULT_PACKAGED_NAME.equals(packageName)) {
            packageManifest.setFullName(packageName);
        }

        return packageManifest;
    }

    /**
     * @param componentTypes
     * @return
     */
    public Package createDefaultPackageManifestForComponentTypes(String[] componentTypes) {
        return createPackageManifestForComponentTypes(Constants.DEFAULT_PACKAGED_NAME, componentTypes);
    }

    /**
     * @param packageName
     * @param componentTypes
     * @return
     */
    public Package createPackageManifestForComponentTypes(String packageName, String[] componentTypes) {
        if (Utils.isEmpty(packageName) || Utils.isEmpty(componentTypes)) {
            throw new IllegalArgumentException("Package name and/or object types cannot be null");
        }

        Package packageManifest = createGenericDefaultPackageManifest();

        for (String componentType : componentTypes) {
            if (!getComponentFactory().isWildCardSupportedComponentType(componentType)
                    || !isEnabledRetrieveComponentType(componentType)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping disabled object type '" + componentType + "'");
                }
                continue;
            }
            PackageTypeMembers packageTypeMember = createPackageTypeMembers(componentType, new String[] { "*" });
            packageManifest.getTypes().add(packageTypeMember);
        }

        if (Utils.isNotEmpty(packageName) && !Constants.DEFAULT_PACKAGED_NAME.equals(packageName)) {
            packageManifest.setFullName(packageName);
        }

        return packageManifest;
    }

    /**
     * This method is only for the deployment code, which has to be careful about custom objects and their contents.
     *
     * See W-837427 As should be obvious, this method is a quick fix that should be refactored.
     *
     * @return the package manifest to be deployed
     */
    public Package createSpecialDefaultPackageManifest() {
        Package packageManifest = new Package();

        // Only "wildcard-supported" component types appear in the deployment
        // manifest, according to the code.
        //
        // However, custom object components have subcomponents which can be
        // deployed without
        // their parent custom objects. These subcomponents are found in .object
        // files.
        // The deployment code can only write a .object file to the deployment
        // package; it can't
        // write only the subcomponent. To prevent the whole custom object from
        // being deployed
        // we must remove the "*" from the deployment manifest. If the object
        // really is being
        // deployed, its name will appear in the manifest. See changelist
        // 1452239.
        //
        List<String> componentTypes = getComponentFactory().getWildcardSupportedComponentTypes();
        for (String componentType : componentTypes) {
            if (!isEnabledRetrieveComponentType(componentType)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping disabled object type '" + componentType + "' from deployment");
                }
                continue;
            }
            PackageTypeMembers packageTypeMember = null;
            if (componentType.equals("CustomObject")) {
                packageTypeMember = createPackageTypeMembers(componentType, new String[0]);
            } else if (!componentType.equals("Settings")){
                packageTypeMember = createPackageTypeMembers(componentType, new String[] { "*" });
                
            }
            
            //Don't add any of the sharing rules to the default manifest. We will add them from the project manifest.
            if (packageTypeMember != null && !Constants.SHARING_RULE_TYPES.contains(componentType)) {
            	packageManifest.getTypes().add(packageTypeMember);
            }
        }
 
        return packageManifest;
    }

    public Package createDefaultPackageManifest() {
        Package packageManifest = new Package();

        List<String> componentTypes = getComponentFactory().getWildcardSupportedComponentTypes();
        for (String componentType : componentTypes) {
            PackageTypeMembers PackageTypeMember = createPackageTypeMembers(componentType, new String[] { "*" });
            packageManifest.getTypes().add(PackageTypeMember);
        }

        return packageManifest;
    }

    public Package createGenericDefaultPackageManifest() {
        Package genericManifest = new Package();
        genericManifest.setVersion(getProjectService().getLastSupportedEndpointVersion());
        return genericManifest;
    }

    public Package createPackageManifest(IProject project, boolean wildcard) throws ForceException {
        Connection connection = getConnectionFactory().getConnection(project);
        return createPackageManifest(connection, null);
    }

    public Package createPackageManifest(Connection connection, String packageName) {
        Package packageManifest = null;
        try {
            // if connection is not null, retrieve enabled types from org,
            // otherwise, use registered types
            String[] componentTypes = null;
            if (connection != null) {
                componentTypes = serviceLocator.getMetadataService().getEnabledComponentTypes(connection);
            }

            if (Utils.isEmpty(componentTypes)) {
                List<String> componentTypesList = getComponentFactory().getEnabledRegisteredComponentTypes();
                componentTypes = componentTypesList.toArray(new String[componentTypesList.size()]);
            }

            packageManifest = createPackageManifestForComponentTypes(packageName, componentTypes);

        } catch (InterruptedException e) {
            logger.warn("Operation canceled by user");
        } catch (Exception e) {
            String logDisplay = null == connection ? "" : connection.getLogDisplay();
            logger.error("Generic package manifest created - unable to get enabled object types from "
                    + logDisplay, e);
        }

        return packageManifest;
    }

    public Package createPackageManifest(Document document) throws JAXBException {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        Package packageManifest = createGenericDefaultPackageManifest();
        packageManifest = (Package) packageManifest.getComponentFromNode(document);
        if (Utils.isEmpty(packageManifest.getVersion())) {
            packageManifest.setVersion(getProjectService().getLastSupportedEndpointVersion());
        }

        return packageManifest;
    }

    public void sort(Package packageManifest) {
        if (packageManifest == null || Utils.isEmpty(packageManifest.getTypes())) {
            return;
        }

        Collections.sort(packageManifest.getTypes(), new Comparator<PackageTypeMembers>() {
            @Override
            public int compare(PackageTypeMembers o1, PackageTypeMembers o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });

        for (PackageTypeMembers type : packageManifest.getTypes()) {
            Collections.sort(type.getMembers());
        }
    }

    public void sort(List<PackageTypeMembers> types) {
        if (Utils.isEmpty(types)) {
            return;
        }

        Collections.sort(types, new Comparator<PackageTypeMembers>() {
            @Override
            public int compare(PackageTypeMembers o1, PackageTypeMembers o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });
    }

    /**
     * @param packageManifest
     * @return
     * @throws JAXBException
     */
    public String getPackageManifestString(Package packageManifest) throws JAXBException {
        if (packageManifest == null) {
            return null;
        }

        return packageManifest.getXMLString();
    }

    /**
     * @param component
     * @return
     */
    public Package createPackageManifest(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }

        return Utils.isNotEmpty(component.getPackageName()) && Utils.isNotEmpty(component.getFileName())
                ? createPackageManifest(component.getPackageName(), component) : null;
    }

    /**
     * @param packageName
     * @param component
     * @return
     */
    public Package createPackageManifest(String packageName, Component component) {
        if (component == null || Utils.isEmpty(packageName)) {
            throw new IllegalArgumentException("Component and/or package name cannot be null");
        }

        Package packageManifest = new Package();
        PackageTypeMembers packageTypeMembers =
                createPackageTypeMembers(component.getComponentType(), new String[] { component.getFileName() });
        packageManifest.getTypes().add(packageTypeMembers);

        if (Utils.isNotEmpty(packageName) && !Constants.DEFAULT_PACKAGED_NAME.equals(packageName)) {
            packageManifest.setFullName(packageName);
        }
        return packageManifest;
    }

    private static PackageTypeMembers createPackageTypeMembers(String componentType, String[] fileNames) {
        PackageTypeMembers packageTypeMembers = new PackageTypeMembers();
        packageTypeMembers.setName(componentType);
        packageTypeMembers.getMembers().addAll(Arrays.asList(fileNames));

        if (logger.isDebugEnabled()) {
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("Created manifest entry for " + componentType + " with members: ");
            for (String fileName : fileNames) {
                strBuff.append(fileName);
                strBuff.append(" ");
            }
            logger.debug(strBuff.toString());
        }

        return packageTypeMembers;
    }

    private boolean isEnabledRetrieveComponentType(String componentType) {
        return defaultDisabledRetrieveComponentTypes != null ? !defaultDisabledRetrieveComponentTypes
                .contains(componentType) : true;
    }

    /**
     * @param file
     * @return
     * @throws JAXBException
     * @throws FileNotFoundException
     */
    public Package getPackageManifestFromFile(IFile file) throws JAXBException, FileNotFoundException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        return parsePackageManifest(file.getRawLocation().toFile());
    }

    /**
     * @param projectPackageList
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void attachDeleteManifests(ProjectPackageList projectPackageList) throws FactoryException {
        if (projectPackageList == null) {
            throw new IllegalArgumentException("Project package list cannot be null");
        }

        if (Utils.isEmpty(projectPackageList)) {
            logger.warn("Unable to attach delete manifest - project package list is null or empty");
            return;
        }

        for (ProjectPackage projectPackage : projectPackageList) {
            attachDeleteManifest(projectPackage, true);
        }
    }

    public void attachDeleteManifest(ProjectPackage projectPackage) throws FactoryException {
        attachDeleteManifest(projectPackage, false);
    }

    /**
     * @param projectPackage
     * @throws FactoryException
     */
    public void attachDeleteManifest(ProjectPackage projectPackage, boolean addAll) throws FactoryException {
        if (projectPackage == null) {
            throw new IllegalArgumentException("Project package cannot be null");
        }

        Package deleteManifest = null;
        try {
            deleteManifest = projectPackage.newMetadataExtInstance();
        } catch (InstantiationException e) {
            throw new FactoryException(e);
        } catch (IllegalAccessException e) {
            throw new FactoryException(e);
        }

        deleteManifest.setVersion(getProjectService().getLastSupportedEndpointVersion());
        if (Utils.isNotEmpty(projectPackage.getName())
                && !Constants.DEFAULT_PACKAGED_NAME.equals(projectPackage.getName())) {
            deleteManifest.setFullName(projectPackage.getName());
        }

        if (addAll) {
            addComponentListToDeleteManifest(deleteManifest, projectPackage.getComponentList());
        }

        projectPackage.setDeleteManifest(deleteManifest);

        if (logger.isDebugEnabled()) {
            logger.debug("Added delete manifest to project package '" + projectPackage.getName() + "'");
        }

        logManifest(deleteManifest);
    }

    public boolean removeFromDeleteManifest(Package deletePackageManifest, Component component) {
        if (deletePackageManifest == null || component == null) {
            throw new IllegalArgumentException("Delete manfest and/or component cannot be null");
        }

        if (deletePackageManifest.getTypes().isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("Unable to remove component from delete manifest - manifest is empty");
            }
            return false;
        }

        boolean success = false;
        List<PackageTypeMembers> types = deletePackageManifest.getTypes();
        if (Utils.isEmpty(types)) {
            if (logger.isInfoEnabled()) {
                logger.info("Unable to remove component from delete manifest - types is empty");
            }
            return false;
        }

        for (Iterator<PackageTypeMembers> iterator = types.iterator(); iterator.hasNext();) {
            PackageTypeMembers packageTypeMembers = iterator.next();
            if (packageTypeMembers.getName().equals(component.getComponentType())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found package type for '" + component.getComponentType() + "'");
                }

                // remove component
                if (Utils.isNotEmpty(packageTypeMembers.getMembers())) {
                    success = packageTypeMembers.getMembers().remove(component.getName());
                    if (success) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Remove '" + component.getFullDisplayName() + "' from delete manifest");
                        }
                    }
                }

                // if is empty, remove it too
                if (Utils.isEmpty(packageTypeMembers.getMembers())) {
                    types.remove(packageTypeMembers);
                }

                break;
            }
        }

        for (PackageTypeMembers packageTypeMembers : types) {
            if (packageTypeMembers.getName().equals(component.getComponentType())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found package type for '" + component.getComponentType() + "'");
                }
                if (Utils.isNotEmpty(packageTypeMembers.getMembers())) {
                    success = packageTypeMembers.getMembers().remove(component.getName());
                    if (success) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Remove '" + component.getFullDisplayName() + "' from delete manifest");
                        }
                    }
                }
                break;
            }
        }
        return success;
    }

    /**
     * Adds the filesnames for components based on the map to the package manifest
     *
     * @param manifest
     * @param packagemanifestMap
     */

    public void addFileNamesToManifest(Package manifest, Map<String, List<String>> packageManifestMap) {

        if (manifest == null)
            return;
        for (String compType : packageManifestMap.keySet()) {
            String[] fileNames =
                    packageManifestMap.get(compType).toArray(new String[packageManifestMap.get(compType).size()]);
            PackageTypeMembers members = createPackageTypeMembers(compType, fileNames);
            manifest.getTypes().add(members);
        }
    }

    /**
     * @param manifest
     * @param component
     */
    public void addComponentToManifest(Package manifest, Component component) {

        if (component.isPackageManifest()) {
            if (logger.isInfoEnabled()) {
                logger.info("Component is a package manifest instance - will not add to manifest types");
            }
            return;
        }

        // We're calling a special method here, created to handle how standard objects
        // have to appear in the manifest - i.e. they must appear in the custom object
        // type member.  Now that we're explicitly adding each custom object name
        // to the deployment manifest, and no longer have a wildcard in its
        // type member, standard objects need to be listed here explicitly too.
        // See W-820653 and W-837384.
        PackageTypeMembers member = getDeploymentPackageTypeForComponent(manifest, component, true);
        if (member == null) {
            logger.warn("Unable to add components of type '" + component.getComponentType()
                    + "' to manifest - PackageTypeMembers is null");
            return;
        }

        if (!checkComponentExists(member, component.getName())) {
            String componentName =
                    component.isWithinFolder() ? component.getPackageTypeMemberNameWithFolder() : component.getName();
            member.getMembers().add(componentName);
            if (logger.isInfoEnabled()) {
                logger.info("Add component '" + component.getName() + "' as member of type '" + member.getName() + "'");
            }
        }

        if (!memberExists(manifest.getTypes(), member)) {
            manifest.getTypes().add(member);
        }

        if (logger.isDebugEnabled()) {
            logger
                    .debug("Added " + member.getMembers() + " of type '" + component.getComponentType()
                            + "' to manifest");
        }
    }

    private static PackageTypeMembers getDeploymentPackageTypeForComponent(Package manifest, Component component, boolean b) {
        if (component.getComponentType().equals(Constants.STANDARD_OBJECT)) {
            List<PackageTypeMembers> types = manifest.getTypes();
            for (PackageTypeMembers member : types) {
                if (member.getName().equals(Constants.CUSTOM_OBJECT)) {
                    return member;
                }
            }
            throw new RuntimeException("No custom object type found in deployment manifest!  Should never happen!");
        }
        return getPackageTypeForComponent(manifest, component, b);
    }

    public Package addComponentListToManifest(ProjectPackage projectPackage, ComponentList componentList, boolean save)
            throws FactoryException, IOException, InterruptedException,
            ForceProjectException {

        Component packageManifestComponent = projectPackage.getPackageManifest();
        if (packageManifestComponent == null || packageManifestComponent.getFileResource() == null
                || !packageManifestComponent.getFileResource().exists()) {
            logger.warn("Package manifest component and/or file is null or does not exist");
            return null;
        }

        Package packageManifest = null;
        try {
            packageManifest = getPackageManifestFromFile(packageManifestComponent.getFileResource());
        } catch (JAXBException e) {
            throw new FactoryException(e);
        }

        if (packageManifest == null) {
            logger.warn("Unable to convert packge manifest");
            return null;
        }

        for (Component component : componentList) {
            addComponentToManifest(packageManifest, component);
        }

        if (save) {
            try {
                packageManifestComponent.setFile(packageManifest.getBytes());
                packageManifestComponent.saveToFile(false, new NullProgressMonitor());
            } catch (CoreException e) {
                String logMessage = Utils.generateCoreExceptionLog(e);
                logger.warn("Package manifest save failed: " + logMessage);
                throw new FactoryException(e);
            }
        }

        return packageManifest;
    }

    public void addComponentListToDeleteManifest(Package deleteManifest, ComponentList componentList) {
        for (Component component : componentList) {
            if (!component.isRemoteDeleteable())
                continue;
            addComponentToManifest(deleteManifest, component);
        }
    }

    // U T I L I T Y
    private static boolean checkComponentExists(PackageTypeMembers member, String componentName) {
        List<String> existingMembers = member.getMembers();
        if (Utils.isEmpty(existingMembers)) {
            return false;
        }
        for (String existingMember : existingMembers) {
            if (componentName.equals(existingMember)) {
                return true;
            }
        }
        return false;
    }

    private static com.salesforce.ide.api.metadata.types.PackageTypeMembers getPackageType(Package manifest,
            String componentType, boolean add) {
        List<com.salesforce.ide.api.metadata.types.PackageTypeMembers> types = manifest.getTypes();
        if (Utils.isNotEmpty(types)) {
            for (PackageTypeMembers type : types) {
                if (componentType.equals(type.getName())) {
                    return type;
                }
            }
        }

        PackageTypeMembers desiredType = new PackageTypeMembers();
        desiredType.setName(componentType);

        if (add) {
            manifest.getTypes().add(desiredType);
        }

        return desiredType;
    }

    /**
     * Given component, return corresponding PackageType from package.xml. Handled Folder component type.
     *
     * @param manifest
     * @param component
     * @param add
     * @return
     */
    private static com.salesforce.ide.api.metadata.types.PackageTypeMembers getPackageTypeForComponent(Package manifest,
            Component component, boolean add) {
        List<com.salesforce.ide.api.metadata.types.PackageTypeMembers> types = manifest.getTypes();
        if (Utils.isNotEmpty(types)) {
            for (PackageTypeMembers type : types) {
                if ((component.getComponentType().equals(Constants.FOLDER) && component.getSecondaryComponentType()
                        .equals(type.getName()))
                        || component.getComponentType().equals(type.getName())) {
                    return type;
                }
            }
        }

        PackageTypeMembers desiredType = new PackageTypeMembers();
        if (component.getComponentType().equals(Constants.FOLDER)) {
            desiredType.setName(component.getSecondaryComponentType());
        } else {
            desiredType.setName(component.getComponentType());
        }

        if (add) {
            manifest.getTypes().add(desiredType);
        }

        return desiredType;
    }

    private static boolean memberExists(List<PackageTypeMembers> members, PackageTypeMembers member) {
        return (Utils.isNotEmpty(members) ? members.contains(member) : false);
    }

    public Package getPackageManifestForComponentTypes(IProject project, String packageName, String[] componentTypes,
            boolean includeSubcomponentTypes) throws FactoryException {
        Package packageManifest = getPackageManifestFactory().getPackageManifest(project, packageName);
        Package tmpPackageManifest = new com.salesforce.ide.api.metadata.types.Package();
        tmpPackageManifest.setDescription(packageManifest.getDescription());
        tmpPackageManifest.setFullName(packageManifest.getFullName());
        tmpPackageManifest.setVersion(packageManifest.getVersion());

        getPackageTypeMembersForComponentTypes(componentTypes, packageManifest, tmpPackageManifest,
            includeSubcomponentTypes);

        if (logger.isDebugEnabled()) {
            logManifest(tmpPackageManifest);
        }

        return tmpPackageManifest;
    }

    /**
     * If given component type has subComponent types, this method will return subComponentType members stanza from
     * package.xml as well.
     *
     * @param componentTypes
     * @param packageManifest
     * @param tmpPackageManifest
     * @param includeSubcomponentTypes
     * @throws FactoryException
     */
    private void getPackageTypeMembersForComponentTypes(String[] componentTypes, Package packageManifest,
            Package tmpPackageManifest, boolean includeSubcomponentTypes) throws FactoryException {
        for (String componentType : componentTypes) {
            if (getComponentFactory().hasSubComponentTypesForComponentType(componentType)) {
                Component component = getComponentFactory().getComponentByComponentType(componentType);
                List<String> subComponentTypeList = component.getSubComponentTypes();
                String[] subComponentTypes = subComponentTypeList.toArray(new String[subComponentTypeList.size()]);
                getPackageTypeMembersForComponentTypes(subComponentTypes, packageManifest, tmpPackageManifest,
                    includeSubcomponentTypes);
            }
            PackageTypeMembers packageTypeMembers = getPackageType(packageManifest, componentType, true);
            tmpPackageManifest.getTypes().add(packageTypeMembers);
        }
    }

    public Package getPackageManifestForComponent(IProject project, String packageName, Component component)
            throws FactoryException {
        Package packageManifest = getPackageManifestFactory().getPackageManifest(project, packageName);
        Package tmpPackageManifest = new com.salesforce.ide.api.metadata.types.Package();
        tmpPackageManifest.setDescription(packageManifest.getDescription());
        tmpPackageManifest.setFullName(packageManifest.getFullName());
        tmpPackageManifest.setVersion(packageManifest.getVersion());

        PackageTypeMembers packageTypeMembers = getPackageTypeForComponent(packageManifest, component, true);
        tmpPackageManifest.getTypes().add(packageTypeMembers);
        return tmpPackageManifest;
    }

    public Package parsePackageManifest(File packageManifestFile) throws JAXBException, FileNotFoundException {

        if (packageManifestFile == null || !packageManifestFile.exists()) {
            throw new IllegalArgumentException("PackageManifest file cannot be null and/or must exist");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Parsing and creating package manifest instance from '"
                    + packageManifestFile.getAbsolutePath() + "'");
        }

        Unmarshaller unmarshaller =
                javax.xml.bind.JAXBContext.newInstance(com.salesforce.ide.api.metadata.types.Package.class).createUnmarshaller();
        // using FileInputStream to avoid file path containing special char, ex. #
        StreamSource streamSource = new StreamSource(new FileInputStream(packageManifestFile));
        JAXBElement<com.salesforce.ide.api.metadata.types.Package> root =
                unmarshaller.unmarshal(streamSource, com.salesforce.ide.api.metadata.types.Package.class);

        Package packageManifest = root.getValue();
        return packageManifest;
    }

    public com.sforce.soap.metadata.Package convert(Package packageManifest) {
        if (packageManifest == null) {
            throw new IllegalArgumentException("PackageManifest cannot be null");
        }

        // create returned package manifest and set values
        com.sforce.soap.metadata.Package returnedPackageManifest =
                new com.sforce.soap.metadata.Package();
        returnedPackageManifest.setDescription(packageManifest.getDescription());
        returnedPackageManifest.setFullName(packageManifest.getFullName());
        returnedPackageManifest.setVersion(packageManifest.getVersion());

        // loop thru types and members creating instances on returned package
        // manifest
        List<PackageTypeMembers> packageTypeMembers = packageManifest.getTypes();
        if (Utils.isNotEmpty(packageTypeMembers)) {
            com.sforce.soap.metadata.PackageTypeMembers[] tmpPackageTypeMembers =
                    new com.sforce.soap.metadata.PackageTypeMembers[packageTypeMembers.size()];
            for (int i = 0; i < packageTypeMembers.size(); i++) {
                tmpPackageTypeMembers[i] = new com.sforce.soap.metadata.PackageTypeMembers();
                tmpPackageTypeMembers[i].setName(packageTypeMembers.get(i).getName());
                int memberCnt = packageTypeMembers.get(i).getMembers().size();
                String[] members = packageTypeMembers.get(i).getMembers().toArray(new String[memberCnt]);
                tmpPackageTypeMembers[i].setMembers(members);
            }
            returnedPackageManifest.setTypes(tmpPackageTypeMembers);
        }

        return returnedPackageManifest;
    }

    public Package convert(com.sforce.soap.metadata.Package toConvertPackage) {
        if (toConvertPackage == null) {
            throw new IllegalArgumentException("PackageManifest cannot be null");
        }

        // create returned package manifest and set values
        com.salesforce.ide.api.metadata.types.Package returnedPackageManifest =
                new com.salesforce.ide.api.metadata.types.Package();
        returnedPackageManifest.setDescription(toConvertPackage.getDescription());
        returnedPackageManifest.setFullName(toConvertPackage.getFullName());
        returnedPackageManifest.setVersion(toConvertPackage.getVersion());

        // loop thru types and members creating instances on returned package
        // manifest
        com.sforce.soap.metadata.PackageTypeMembers[] packageTypeMembers = toConvertPackage.getTypes();
        if (Utils.isNotEmpty(packageTypeMembers)) {
            for (int i = 0; i < packageTypeMembers.length; i++) {
                com.salesforce.ide.api.metadata.types.PackageTypeMembers tmpPackageTypeMembers =
                        new com.salesforce.ide.api.metadata.types.PackageTypeMembers();
                tmpPackageTypeMembers.setName(packageTypeMembers[i].getName());
                String[] members = packageTypeMembers[i].getMembers();
                tmpPackageTypeMembers.getMembers().addAll(Arrays.asList(members));
                returnedPackageManifest.getTypes().add(tmpPackageTypeMembers);
            }
        }

        return returnedPackageManifest;
    }

    private static void logManifest(Package manifest) {
        if (logger.isDebugEnabled() && manifest != null) {
            logger.debug("Manifest for package '"
                    + (Utils.isNotEmpty(manifest.getFullName()) ? manifest.getFullName()
                            : Constants.DEFAULT_PACKAGED_NAME) + "', version [" + manifest.getVersion()
                    + "] is as follows:");
            logPackageTypeMembers(manifest.getTypes());
        }
    }

    private static void logPackageTypeMembers(List<PackageTypeMembers> members) {
        if (logger.isDebugEnabled() && Utils.isNotEmpty(members)) {
            for (PackageTypeMembers member : members) {
                logPackageTypeMember(member);
            }
        }
    }

    private static void logPackageTypeMember(PackageTypeMembers packageTypeMembers) {
        if (logger.isDebugEnabled() && packageTypeMembers != null) {
            StringBuffer strBuff = new StringBuffer();
            List<String> members = packageTypeMembers.getMembers();
            if (Utils.isNotEmpty(members)) {
                strBuff.append("Package directory for '" + packageTypeMembers.getName() + "' contains the following ")
                        .append("[").append(members.size()).append("] members: ");
                int membersCnt = 0;
                for (String member : members) {
                    strBuff.append("\n (").append(++membersCnt).append(") ").append(member);
                }
            }
            logger.debug(strBuff.toString());
        }
    }

    /**
     * Check if wildcard notion is used in package manifest for given component type.
     *
     * @param project
     * @param componentType
     * @return
     * @throws FactoryException
     */
    public boolean isWildCardUsedForComponentType(IProject project, String componentType) throws FactoryException {
        Package packageManifest =
                getPackageManifestFactory().getPackageManifest(project, Constants.DEFAULT_PACKAGED_NAME);
        PackageTypeMembers packageTypeMembers = getPackageType(packageManifest, componentType, false);
        assert packageTypeMembers.getName() == componentType;
        for (String member : packageTypeMembers.getMembers()) {
            if (member.equals("*")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Construct file path when explicit member entry is present in package manifest. Usage: as input for
     * RetrieveRequest.setSpecificFiles()
     *
     * @param project
     * @param componentType
     * @return
     * @throws FactoryException
     */
    public List<String> getFilePathsForComponentType(IProject project, String componentType) throws FactoryException {
        Package packageManifest =
                getPackageManifestFactory().getPackageManifest(project, Constants.DEFAULT_PACKAGED_NAME);
        PackageTypeMembers packageTypeMembers = getPackageType(packageManifest, componentType, false);
        Component componentInfo = getComponentFactory().getComponentByComponentType(componentType);
        List<String> filePathListForComponentType = new ArrayList<>();
        for (String member : packageTypeMembers.getMembers()) {
            if ("*".equals(member)) {
                continue;
            }
            String filePath = null;
            if (componentInfo.isWithinFolder() && !member.contains(Constants.PACKAGE_MANIFEST_FOLDER_SEPARATOR)) { // folder
                // member entry
                filePath = componentInfo.getDefaultFolder() + Constants.PACKAGE_MANIFEST_FOLDER_SEPARATOR + member;
            } else if (Constants.DOCUMENT.equals(componentType)) { // document
                // component type member has file extension specified in package.xml already
                filePath = componentInfo.getDefaultFolder() + Constants.PACKAGE_MANIFEST_FOLDER_SEPARATOR + member;
            } else {
                filePath =
                        componentInfo.getDefaultFolder() + Constants.PACKAGE_MANIFEST_FOLDER_SEPARATOR + member + "."
                                + componentInfo.getFileExtension();
            }
            filePathListForComponentType.add(filePath);
        }
        return filePathListForComponentType;
    }

    /**
     * Check if given component type has explicit member entry specified in package.xml. Cases where custom object that
     * could have * and specific member for standard object should work.
     *
     * @param project
     * @param componentType
     * @return
     * @throws FactoryException
     */
    public boolean hasExplicitMemberForComponentType(IProject project, String componentType) throws FactoryException {
        Package packageManifest =
                getPackageManifestFactory().getPackageManifest(project, Constants.DEFAULT_PACKAGED_NAME);
        PackageTypeMembers packageTypeMembers = getPackageType(packageManifest, componentType, false);
        boolean hasExpliciteMember = false;
        for (String member : packageTypeMembers.getMembers()) {
            if (!member.equals("*")) {
                hasExpliciteMember = true;
                break;
            }
        }
        return hasExpliciteMember;

    }
}
