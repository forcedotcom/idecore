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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.factories.ProjectPackageFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.core.services.ToolingService;

/**
 * Contains a list of components.
 * 
 * @author cwall
 */
public class ComponentList extends ArrayList<Component> {

    private static final Logger logger = Logger.getLogger(ComponentList.class);

    private static final long serialVersionUID = 1L;

    protected transient ProjectService projectService = null;

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

    public String[] getFileNames() {
        String[] fileNames = new String[size()];
        for (int i = 0; i < size(); i++) {
            fileNames[i] = get(i).getFileName();
        }
        return fileNames;
    }

    public String[] getFullNames() {
        String[] fullNames = new String[size()];
        for (int i = 0; i < size(); i++) {
            fullNames[i] = get(i).getFullName();
        }
        return fullNames;
    }

    public String[] getNames() {
        String[] names = new String[size()];
        for (int i = 0; i < size(); i++) {
            names[i] = get(i).getName();
        }
        return names;
    }

    public Component get(Component component) {
        Component retComponent = null;
        if (!isEmpty()) {
            for (Component tmpComponent : this) {
                if (component.equals(tmpComponent)) {
                    retComponent = tmpComponent;
                }
            }
        }
        return retComponent;
    }

    public boolean hasComponentByFilePath(String componentFilePath) {
        if (Utils.isEmpty(componentFilePath)) {
            return false;
        }

        for (Component component : this) {
            if (!component.isCaseSensitive() && component.getMetadataFilePath().equalsIgnoreCase(componentFilePath)) {
                return true;
            } else if (component.getMetadataFilePath().equals(componentFilePath)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasFolderComponent(Component component) {
        if (component == null 
            || !component.isWithinFolder() 
            || component.getFileResource() == null
            || component.getFileResource().getParent() == null
            || component.getFileResource().getParent().getType() != IResource.FOLDER) {
            return false;
        }

        IFolder parentFolder = (IFolder) component.getFileResource().getParent();
        String folderMetadataFilePath = Utils.stripSourceFolder(parentFolder.getProjectRelativePath().toPortableString());
        folderMetadataFilePath += Constants.DEFAULT_METADATA_FILE_EXTENSION;

        logger.debug("Looking for folder metadata component '" + folderMetadataFilePath + "'");

        return hasComponentByFilePath(folderMetadataFilePath);
    }

    public boolean hasComponentType(String componentType) {
        if (isEmpty() || Utils.isEmpty(componentType)) {
            return false;
        }

        for (Component component : this) {
            if (componentType.equals(component.getComponentType())
                || (Utils.isNotEmpty(component.getSecondaryComponentType()) && componentType.equals(component.getSecondaryComponentType()))) {
                return true;
            }
        }

        return false;
    }

    public Component getComponentByFileName(String fileName) {
        if (Utils.isEmpty(fileName)) {
            return null;
        }

        for (Component component : this) {
            if (component.fileName.equalsIgnoreCase(fileName)) {
                return component;
            }
        }

        return null;
    }

    public Component getComponentById(String id) {
        if (Utils.isEmpty(id)) {
            return null;
        }

        for (Component component : this) {
            if (component.id.equalsIgnoreCase(id)) {
                return component;
            }
        }

        return null;
    }

    public Component getComponentByFilePath(String componentFilePath) {
        if (Utils.isEmpty(componentFilePath)) {
            return null;
        }

        for (Component component : this) {
            if (!component.isCaseSensitive() && component.getMetadataFilePath().equalsIgnoreCase(componentFilePath)) {
                return component;
            } else if (component.getMetadataFilePath().equals(componentFilePath)) {
                return component;
            }
        }

        return null;
    }

    public Component getComponentByType(String componentType) {
        if (isEmpty() || Utils.isEmpty(componentType)) {
            return null;
        }

        for (Component component : this) {
            if (componentType.equals(component.getComponentType())
                    || (Utils.isNotEmpty(component.getSecondaryComponentType()) && componentType.equals(component
                        .getSecondaryComponentType()))) {
                return component;
            }
        }

        return null;
    }

    public List<String> getFilePaths() {
        return getFilePaths(false);
    }

    public List<String> getFilePaths(boolean stripSourcePrefix) {
        return getFilePaths(stripSourcePrefix, true);
    }

    public List<String> getFilePaths(boolean stripSourcePrefix, boolean includeManifest) {
        List<String> filePaths = null;
        if (isEmpty()) {
            return filePaths;
        }

        filePaths = new ArrayList<>();
        for (Component component : this) {
            String tmpFilePath = component.getMetadataFilePath();

            if (Utils.isEmpty(tmpFilePath)) {
                logger.warn("Filepath is empty for component " + component.getFullDisplayName());
                continue;
            }

            if (!includeManifest && tmpFilePath.endsWith(Constants.PACKAGE_MANIFEST_FILE_NAME)) {
                continue;
            }

            if (stripSourcePrefix && tmpFilePath.startsWith(Constants.SOURCE_FOLDER_NAME + "/")) {
                tmpFilePath = tmpFilePath.substring(tmpFilePath.indexOf("/") + 1);
            }
            filePaths.add(tmpFilePath);
        }
        return filePaths;
    }

    public Component getComponentForComponentType(String componentType) {
        if (isEmpty()) {
            return null;
        }

        for (Component component : this) {
            if (component.getComponentType().endsWith(componentType)) {
                return component;
            }
        }

        return null;
    }

    public ComponentList getComponentListForComponentType(String componentType) {
        if (isEmpty()) {
            return null;
        }

        ComponentList componentList = getComponentFactory().getComponentListInstance();
        for (Component component : this) {
            if (component.getComponentType().endsWith(componentType)) {
                componentList.add(component);
            }
        }

        return componentList;
    }

    public List<String> getComponentTypes() {
        if (isEmpty()) {
            return null;
        }

        List<String> componentTypes = new ArrayList<>();
        for (Component component : this) {
            componentTypes.add(component.getComponentType());
        }

        return componentTypes;
    }

    public Collection<? extends IResource> getResources() {
        List<IResource> resources = new ArrayList<>();
        for (Component component : this) {
            resources.add(component.getFileResource());
        }
        return resources;
    }

    @Override
    public boolean add(Component component) {
        return add(
            component,
            PackageConfiguration.builder().build());
    }
    
    public boolean add(Component component, boolean includeComposite) {
        return add(
            component,
            PackageConfiguration.builder()
            .setIncludeComposite(includeComposite)
            .build());
    }

    public boolean add(Component component, PackageConfiguration configuration) {
        if (component == null) {
            return false;
        }
        
        if(component.isBundle) {
            remove(component);
            // If already inside a folder, don't add it anymore
            if (component.getComponentType().equals(Constants.FOLDER)) {
                return false;
            } else {
                Component replacement = component.preComponentListAddition(configuration);
                return super.add(replacement);
            }
        }
        
        if (contains(component)) {
            if (configuration.removeComponent) {
                remove(component);
            } else {
                return false;
            }
        }

        boolean addSuccess = super.add(component);

        if (!configuration.includeComposite || !component.isMetadataComposite()) {
            return addSuccess;
        } else {
            return addComponentComposite(component, addSuccess);
        }
    }

    private boolean addComponentComposite(Component component, boolean addSuccess) {
        String compositeComponentFilePath = component.getCompositeMetadataFilePath();
        if (Utils.isEmpty(compositeComponentFilePath) 
            || projectService == null
            || component.getFileResource() == null) {
            return false;
        }
        
        IFile compositeComponentFile = projectService.getComponentFileForFilePath(
            component.getFileResource().getProject(),
            compositeComponentFilePath);

        if (compositeComponentFile == null || !compositeComponentFile.exists()) {
            logger.error(
                "Component composite file not found at filepath '" 
                + compositeComponentFilePath
                + "' for component " 
                + component.getFullDisplayName());
            return false;
        }

        try {
            // create composite instance for object type
            Component compositeComponent = getComponentFactory().getComponentFromFile(compositeComponentFile);

            if (compositeComponent == null) {
                logger.error(
                    "Component metadata not created for '"
                    + compositeComponentFile.getProjectRelativePath().toPortableString() 
                    + "' for component "
                    + component.getFullDisplayName());
                return false;
            }

            // set component composite props
            compositeComponent.setFilePath(compositeComponentFilePath);

            // save to component list
            addSuccess = super.add(compositeComponent);

            if (!addSuccess) {
                logger.error("Unable to add composite component '" + compositeComponentFilePath + "' to component list");
                return false;
            }

        } catch (FactoryException e) {
            logger.error(
                "Unable to get composite component from filepath '" 
                + compositeComponentFilePath + "'"
                + e.getMessage());
            return false;
        }

        return addSuccess;
    }

    public boolean remove(Component component) {
        if (super.remove(component)) {
            return true;
        }
        return false;
    }

    public boolean removeByFilePath(String filePath) {
        for (Component component : this) {
            if (!component.isCaseSensitive() && filePath.equalsIgnoreCase(component.getMetadataFilePath())) {
                return remove(component);
            } else if (filePath.equals(component.getMetadataFilePath())) {
                return remove(component);
            }
        }

        return false;
    }

    public void sort() {
        Collections.sort(this, new Comparator<Component>() {
            @Override
            public int compare(Component o1, Component o2) {
                if (o1 == o2) {
                    return 0;
                }
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    /*
     * Checks if all the components can be deployed through the Tooling API using
     * ContainerAsyncRequest. There are two conditions for this:
     * i) the components have to be supported as members of ContainerMember
     * ii) the components must already be created on the server (the ContainerMember needs a foreign ID)
     */
    public boolean isDeployableThroughContainerAsyncRequest() {
        return hasOnlySupportedContainerMembers() && hasOnlyExistentComponents();
    }

    boolean hasOnlySupportedContainerMembers() {
        return this.stream().allMatch(cmp -> {
            ToolingService toolingService = ContainerDelegate.getInstance().getServiceLocator().getToolingService();
            return toolingService.checkIfCanCreateContainerMember(cmp, ComponentList.this);
        });
    }
    
    boolean hasOnlyExistentComponents() {
        return this.stream().allMatch(cmp -> {
            return cmp.getId() != null && !cmp.getId().equals("");
        });
    }

    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("ComponentList ( ").append("count = ").append(size()).append(TAB);
        int componentCnt = 0;
        for (Component component : this) {
            retValue.append("\n (");
            retValue.append(++componentCnt);
            retValue.append(") ");
            retValue.append(component.toString());
        }
        retValue.append(" )");

        return retValue.toString();
    }

    public String toStringLite() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("ComponentList ( ").append("count = ").append(size()).append(TAB);
        int componentCnt = 0;
        for (Component component : this) {
            retValue.append("\n (");
            retValue.append(++componentCnt);
            retValue.append(") ");
            retValue.append(component.toStringLite());
        }
        retValue.append(" )");

        return retValue.toString();
    }
}
