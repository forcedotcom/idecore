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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;

import com.salesforce.ide.api.metadata.types.MetadataExt;
import com.salesforce.ide.core.internal.factories.ApplicationContextFactory;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.IComponent;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.remote.metadata.CustomObjectNameResolver;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.sforce.soap.metadata.FileProperties;

/**
 * Encapsulates functionality related to managing Salesforce.com object types.
 *
 * @author cwall
 */
public class ComponentFactory extends ApplicationContextFactory {

    private static final Logger logger = Logger.getLogger(ComponentFactory.class);

    private ComponentList componentRegistry = null;
    private ComponentList enabledRegisteredComponentRegistry = null;
    private String metadataFileExtension = null;
    private String[] defaultDisabledComponentTypes = null;
    private List<String> wildCardSupportedComponentTypes = null;
    private ComponentList enabledRegisteredWildCardComponentRegistry = null;

    public ComponentFactory() {}

    public String getMetadataFileExtension() {
        return metadataFileExtension;
    }

    public void setMetadataFileExtension(String metadataFileExtension) {
        this.metadataFileExtension = metadataFileExtension;
    }

    public String[] getDefaultDisabledComponentTypes() {
        return defaultDisabledComponentTypes;
    }

    public void setDefaultDisabledComponentTypes(String[] defaultDisabledComponentTypes) {
        this.defaultDisabledComponentTypes = defaultDisabledComponentTypes;
    }

    // lookup method injection by container
    public ComponentList getComponentListInstance() {
        return new ComponentList();
    }

    public ComponentList getComponentRegistry() {
        return componentRegistry;
    }

    public int getComponentRegistrySize() {
        return Utils.isNotEmpty(componentRegistry) ? componentRegistry.size() : 0;
    }

    public void setComponentRegistry(ComponentList componentRegistry) {
        this.componentRegistry = componentRegistry;

        if (logger.isDebugEnabled()) {
            StringBuffer strBuff = new StringBuffer();
            if (Utils.isEmpty(this.componentRegistry)) {
                strBuff.append("No components registered");
            } else {
                strBuff.append("Registered the following components:");
                int i = 0;
                for (IComponent component : this.componentRegistry) {
                    strBuff.append("\n  (").append(++i).append(") ").append(component.toStringLite());
                }
            }

            logger.debug(strBuff.toString());
        }
    }

    public ComponentList getRegisteredComponents() {
        return componentRegistry;
    }

    public ComponentList getRegisteredComponentsForComponentTypes(String[] componentTypes) {
        if (Utils.isEmpty(componentRegistry)) {
            return null;
        }

        if (Utils.isEmpty(componentTypes)) {
            return componentRegistry;
        }

        ComponentList components = getComponentListInstance();
        for (String componentType : componentTypes) {
            for (IComponent component : componentRegistry) {
                if (componentType.equals(component.getComponentType())) {
                    components.add((Component) component);
                    break;
                }
            }
        }

        return components;
    }

    public List<String> getWildcardSupportedComponentTypes() {
        if (Utils.isNotEmpty(wildCardSupportedComponentTypes)) {
            return wildCardSupportedComponentTypes;
        }
        wildCardSupportedComponentTypes = new ArrayList<>();

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return wildCardSupportedComponentTypes;
        }

        for (IComponent component : componentRegistry) {
            if (component.isWildCardSupported() && !component.isInternal()) {
                wildCardSupportedComponentTypes.add(component.getComponentType());
            }
        }

        // Add the subcomponent types that aren't supported as first-class
        // components, but which we think we can use this way.  See changelist 1452239.
        wildCardSupportedComponentTypes.addAll(getSubComponentTypes());

        return wildCardSupportedComponentTypes;
    }

    public List<String> getRegisteredComponentTypes() {
        List<String> componentTypes = new ArrayList<>();

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return componentTypes;
        }
		return componentRegistry.getComponentTypes();
    }

    public List<String> getInternalComponentTypes() {
        List<String> componentTypes = new ArrayList<>();

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return componentTypes;
        }

        for (IComponent component : componentRegistry) {
            if (component.isInternal()) {
                componentTypes.add(component.getComponentType());
            }
        }

        return componentTypes;
    }

    public List<String> getEnabledRegisteredComponentTypes() {
        List<String> componentTypes = new ArrayList<>();
        ComponentList enabledComponents = getEnabledRegisteredComponents();

        if (Utils.isEmpty(enabledComponents)) {
            logger.warn("Enabled omponent registry is null or empty");
            return componentTypes;
        }

        return enabledComponents.getComponentTypes();
    }

    public ComponentList getEnabledRegisteredComponents() {
        if (enabledRegisteredComponentRegistry != null) {
            return enabledRegisteredComponentRegistry;
        }

        enabledRegisteredComponentRegistry = getComponentListInstance();

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return enabledRegisteredComponentRegistry;
        }

        for (Component component : componentRegistry) {
            String componentType = component.getComponentType();
            if (!componentType.equals(component.getComponentType())) {
                continue;
            }

            if (!isValidComponentByComponentType(component) && isDisabledComponentType(componentType)) {
                continue;
            }

            enabledRegisteredComponentRegistry.add(component);
        }

        return enabledRegisteredComponentRegistry;
    }

    public ComponentList getEnabledRegisteredWildCardSupportedComponents() {
        if (enabledRegisteredWildCardComponentRegistry != null) {
            return enabledRegisteredWildCardComponentRegistry;
        }

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return enabledRegisteredWildCardComponentRegistry;
        }

        enabledRegisteredWildCardComponentRegistry = getComponentListInstance();
        for (Component component : componentRegistry) {
            String componentType = component.getComponentType();
            if (!componentType.equals(component.getComponentType())) {
                continue;
            }

            if (!isValidComponentByComponentType(component) || isDisabledComponentType(componentType)
                || !isWildCardSupportedComponentType(componentType)) {
                continue;
            }
            
            enabledRegisteredWildCardComponentRegistry.add(component);
        }

        return enabledRegisteredWildCardComponentRegistry;
    }

    public ComponentList getEnabledRegisteredComponents(String[] componentTypes) {
        ComponentList enabledComponents = getEnabledRegisteredComponents();

        if (Utils.isEmpty(enabledComponents) || Utils.isEmpty(componentTypes)) {
            return enabledComponents;
        }

        ComponentList components = getComponentListInstance();
        for (String componentType : componentTypes) {
            for (Component enabledComponent : enabledComponents) {
                if (componentType.equals(enabledComponent.getComponentType())) {
                    components.add(enabledComponent);
                    break;
                }
            }
        }

        return components;
    }

    public String getRegisteredComponentFolderByComponentType(String componentType) {
        if (Utils.isEmpty(componentType)) {
            logger.error("Object type cannot be null");
            throw new IllegalArgumentException("Object type cannot be null");
        }

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return null;
        }

        for (IComponent component : componentRegistry) {
            if (componentType.equals(component.getComponentType())) {
                return component.getDefaultFolder();
            }
        }

        return null;
    }

    public List<String> getDeleteableRegisteredComponentTypes() {
        List<String> componentTypes = new ArrayList<>();

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return componentTypes;
        }

        for (IComponent component : componentRegistry) {
            if (component.isRemoteDeleteable()) {
                componentTypes.add(component.getComponentType());
            }
        }

        return componentTypes;
    }

    public boolean isWildCardSupportedComponentType(String componentType) {
        List<String> componentTypes = getWildcardSupportedComponentTypes();
        return componentTypes.contains(componentType);
    }

    public boolean isDisabledComponentType(String componentType) {
        if (Utils.isEmpty(defaultDisabledComponentTypes)) {
            return false;
        }

        for (String disabledComponentType : defaultDisabledComponentTypes) {
            if (componentType.equals(disabledComponentType)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidComponentByComponentType(IComponent component) {
        return !component.isInternal() && !component.isPackageManifest() && !component.isPackageManifest();
    }

    public boolean isEnabledComponentType(String componentType) {
        ComponentList components = getEnabledRegisteredComponents(new String[] { componentType });
        if (Utils.isEmpty(components)) {
            return false;
        }
		return true;
    }

    public boolean isRegisteredComponentType(String componentType) {
        return Utils.isNotEmpty(componentRegistry) && componentRegistry.hasComponentType(componentType);
    }

    public String getComponentTypeByFolderName(String folderName) {
        if (Utils.isEmpty(folderName)) {
            logger.error("Folder name is null");
            throw new IllegalArgumentException("Folder name cannot be null");
        }

        Component component = getComponentByFolderName(folderName);
        if (component == null) {
            logger.warn("Unable to determine object type for folder '" + folderName + "'");
            return null;
        }

        return component.getComponentType();
    }

    public List<String> getComponentFolderNames(String[] componentTypes) {
        if (Utils.isEmpty(componentTypes)) {
            return null;
        }

        List<String> folderNames = new ArrayList<>();
        Component component = null;
        for (String componentType : componentTypes) {
            component = getComponentFactory().getComponentByComponentType(componentType);
            if (component != null) {
                folderNames.add(component.getDefaultFolder());
            }
        }

        return folderNames;
    }

    public String getComponentFolderName(String componentType) {
        if (Utils.isEmpty(componentType)) {
            return null;
        }

        Component component = getComponentFactory().getComponentByComponentType(componentType);
        if (component != null) {
            return component.getDefaultFolder();
        }

        return null;
    }

    public Component getComponentById(String id) {
        return getComponentById(id, null);
    }

    public Component getComponentById(String id, String componentType) {
        Component component = null;
        // if id is not found, get unknown type and set object type manually
        if (Utils.isEmpty(id)) {
            id = Constants.UNKNOWN_COMPONENT_TYPE;
            component = getComponentBean(id);
            if (Utils.isNotEmpty(componentType)) {
                component.setComponentType(componentType);
                component.setDisplayName(componentType);
            }
        } else {
            component = getComponentBean(id);
        }
        return component;
    }

    /**
     * Create new component instance base on given extension.
     *
     * @param extension
     * @return
     */
    public Component getComponentByExtension(String extension) {
        if (Utils.isEmpty(extension)) {
            throw new IllegalArgumentException("Extension cannot be null");
        }

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return null;
        }

        String id = getComponentByIdByExtension(extension);

        // get component from context
        Component component = getComponentById(id, id);

        if (component == null) {
            return null;
        }

        checkComponentMetadata(extension, component);

        return component;
    }

    public String getComponentByIdByExtension(String extension) {
        if (Utils.isEmpty(extension)) {
            throw new IllegalArgumentException("Extension cannot be null");
        }

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return null;
        }

        String id = null;
        for (IComponent component : componentRegistry) {
            if (extension.equals(component.getFileExtension()) || isComponentMetadataMatch(extension, component)) {
                id = component.getComponentType();
                break;
            }
        }

        return id;
    }

    /**
     * Create new component instance base on given object type.
     *
     * @param componentType
     * @return
     */
    public Component getComponentByComponentType(String componentType) {
        if (Utils.isEmpty(componentType)) {
            throw new IllegalArgumentException("Object type cannot be null");
        }

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return null;
        }

        // get component from context
        Component component = getComponentById(componentType, componentType);

        if (component == null) {
            return null;
        }

        return component;
    }

    public Component getComponentByComponentTypeClass(MetadataExt metadataExt) throws JAXBException {
        if (metadataExt == null) {
            throw new IllegalArgumentException("MetadataExt instance cannot be null");
        }

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return null;
        }

        String componentType = metadataExt.getClass().getSimpleName();
        Component component = getComponentByComponentType(componentType);
        if (component != null) {
            component.setBody(metadataExt.getXMLString());
            component.setName(metadataExt.getFullName());
        }

        return component;
    }

    /**
     * Create new component instance base on given file path.
     *
     * @param filePath
     * @return
     */
    public Component getComponentByFilePath(String filePath) {

        if (Utils.isEmpty(filePath)) {
            throw new IllegalArgumentException("Filepath cannot be null");
        }

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return null;
        }

        // ignore src/ and referenced.../ part of filepath
        String tmpfilePath = Utils.stripSourceFolder(filePath);

        // id to use for bean lookup
        String id = null;

        // use parts for further investigation, if needed, and to determine if component is a folder component
        String[] pathParts = tmpfilePath.split(Constants.FOWARD_SLASH);
        assert null != pathParts;
        assert 0 < pathParts.length;

        if (filePath.endsWith(Constants.PACKAGE_MANIFEST_FILE_NAME)) {
            id = Constants.PACKAGE_MANIFEST;
            // Because Aura's file extensions are not unique
        } else if(Constants.AURA.equalsIgnoreCase(pathParts[0])) { 
            id = Constants.AURA_DEFINITION_BUNDLE;
        } else {
            String fileExtension = Utils.getExtensionFromFilePath(pathParts[pathParts.length - 1]);
            if (Utils.isNotEmpty(fileExtension)) {
            	if (Constants.RULE_EXTENSIONS.contains(fileExtension)) {
            		id = getComponentTypeByFolderName(pathParts[0]);
            	} else {
            		id = getComponentByIdByExtension(fileExtension);
            	}
            }
        }

        // if id still not determined, look at path, specifically the parent folders
        if (Utils.isEmpty(id) && Utils.isNotEmpty(pathParts)) {
            // first the component folder
            // handles:
            //   <component-folder>/
            //   <component-folder>/<component>
            //   <component-folder>/<user-folder>/<component>
            id = getComponentIdForPathFolderPart(pathParts[0]);

            // it could be that the component folder is two back if the component is in a user-defined folder
            // handles:
            //   <pkg>/<component-folder>
            //   <pkg>/<component-folder>/<component>
            //   <pkg>/<component-folder>/<user-folder>/<component>
            if (Utils.isEmpty(id) && pathParts.length > 1) {
                id = getComponentIdForPathFolderPart(pathParts[1]);
            }
        }

        // unable to determine
        if (Utils.isEmpty(id)) {
            id = Constants.UNKNOWN_COMPONENT_TYPE;
        }

        Component component = getComponentBean(id);

        // if component is a folder, get folder component and set sub type
        if (isFolderComponent(component, pathParts)) {
            component = getComponentBean(Constants.FOLDER);
            component.setSecondaryComponentType(id);
            setFolderName(component, pathParts);
        } else {
            component = getComponentBean(id);
        }

        if (component == null) {
            logger.error("Bean not found for id '" + id + "' found for filepath '" + filePath + "'");
            return null;
        }

        if (CustomObjectNameResolver
            .getCheckerForStandardObject()
            .check(Utils.getNameFromFilePath(filePath), component.getComponentType())) {
            component = getComponentBean(Constants.STANDARD_OBJECT);
        }
        
        component.setFilePath(filePath);
        component.setNameFromFilePath(filePath);
        component.setManagedFromFilePath(filePath);

        if (component.isWithinFolder()) {
            setFolderName(component, pathParts);
        }

        // determine if file is metadata
        checkComponentMetadata(filePath, component);

        return component;
    }

    private String getComponentIdForPathFolderPart(String pathComponentFolderPart) {
        if (Utils.isNotEmpty(pathComponentFolderPart)) {
            for (IComponent component : componentRegistry) {
                if (Utils.isNotEmpty(pathComponentFolderPart)
                        && pathComponentFolderPart.equals(component.getDefaultFolder())) {
                    return component.getComponentType();
                }
            }
        }

        return null;
    }

    private static void setFolderName(Component component, String[] pathParts) {
        if (Utils.isEmpty(pathParts) || pathParts.length < 2) {
            return;
        }

        String folderName = pathParts[1];
        if (folderName.endsWith(component.getMetadataFileExtensionPart())) {
            folderName = folderName.substring(0, folderName.indexOf(component.getMetadataFileExtensionPart()));
        }
        component.setParentFolderNameIfComponentMustBeInFolder(folderName);
    }

    public boolean isFolderComponent(String filePath) {
        if (Utils.isEmpty(filePath)) {
            throw new IllegalArgumentException("Filepath cannot be null");
        }

        if (!filePath.endsWith(metadataFileExtension)) {
            return false;
        }

        List<String> folderNames = getFolderNamesForFolderComponents();
        String[] folderTokens = filePath.split("/");
        for (String folderName : folderNames) {
            if (folderName.equals(folderTokens[folderTokens.length - 1])) {
                return true;
            }
        }

        return false;
    }

    public boolean isFolderComponent(Component component, String[] filePath) {
        if (component == null || Utils.isEmpty(filePath)) {
            return false;
        }

        // only consider components configured as within a folder
        if (!component.isWithinFolder()) {
            return false;
        }

        // filepath has to be 2 < len < 4, see below
        if (filePath.length == 1 || filePath.length == 4) {
            return false;
        }

        /* handles:
         * when filePath.length == 2 and component.isWithinFolder()==true, component will always be a user-folder
         *   <component-folder>/<user-folder>/<component>
         */
        if (filePath.length == 2) {
            return true;
        }

        /* when filePath.length == 3, component is user-folder when default component folder is in position 2
        *   <component-folder>/<user-folder>/README<component>
        *   <pkg>/<component-folder>/<user-folder>/<component>
        *
        * the one risk here being that user-folder name == default folder
        */
        if (filePath.length == 3 && filePath[1].equals(component.getDefaultFolder())) {
            return true;
        }

        return false;
    }

    public boolean isFolderSubComponentType(String componentType) {
        if (Utils.isEmpty(componentType)) {
            logger.warn("Component registry is null or empty");
            return false;
        }

        Component component = getComponentBean(componentType);
        return component != null && component.isWithinFolder();
    }

    public List<String> getFolderNamesForFolderComponents() {
        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return null;
        }

        List<String> folderNames = new ArrayList<>();
        for (IComponent component : componentRegistry) {
            if (component.isWithinFolder()) {
                folderNames.add(component.getDefaultFolder());
            }
        }
        return folderNames;
    }

    public ComponentList getFolderComponents() {
        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return null;
        }

        ComponentList components = getComponentListInstance();
        for (Component component : componentRegistry) {
            if (component.isWithinFolder()) {
                components.add(component);
            }
        }
        return components;
    }

    public boolean isComponentMetadata(IFile file) {
        return file.getName().endsWith(metadataFileExtension);
    }

    /*
     * Create new component instance base on given folder name.
     */
    public Component getComponentByFolderName(String folderName) {
        if (Utils.isEmpty(folderName)) {
            throw new IllegalArgumentException("Folder name cannot be null");
        }

        if (Utils.isEmpty(componentRegistry)) {
            logger.warn("Component registry is null or empty");
            return null;
        }

        String id = null;
        for (IComponent component : componentRegistry) {
            if (folderName.equals(component.getDefaultFolder())) {
                id = component.getComponentType();
                break;
            }
        }

        // get component from context
        Component component = getComponentById(id, id);

        if (component == null) {
            logger.error("Bean not found for id '" + id);
            return null;
        }

        // get empty component for the folder's sub-type to load defaults, if applicable
        setFolderComponentDefaults(component);

        return component;
    }

    /*
     * Get existing component with given file resource. The component's body will be included.
     */
    public Component getComponentFromFile(IFile file) throws FactoryException {
        return getComponentFromFile(file, true);
    }

    /*
     * Get existing component with given file resource. Boolean determines whether the body is included.
     */
    public Component getComponentFromFile(IFile file, boolean includeBody) throws FactoryException {
        if (file == null) {
            logger.error("File cannot be null");
            throw new IllegalArgumentException("File cannot be null");
        }

        // get object type specific instance of file and set file to new object
        Component component = getComponentByFilePath(file.getProjectRelativePath().toPortableString());

        if (component == null) {
            throw new FactoryException("Unable to create component from path "
                    + file.getProjectRelativePath().toPortableString());
        }

        component.setFileResource(file);
        try {
            component.loadFromFile(includeBody);
        } catch (Exception e) {
            throw new FactoryException(e);
        }

        if (Utils.isEmpty(component.getPackageName())) {
            String packageName = getProjectService().getPackageName(file.getProject());
            component.setPackageName(packageName);
        }

        // get empty component for the folder's sub-type to load defaults, if applicable
        setFolderComponentDefaults(component);

        return component;
    }

    public Component getComponentFromSubFolder(IFolder folder, boolean includeBody) throws FactoryException {
        if (folder == null) {
            throw new IllegalArgumentException("Folder cannot be null");
        }

        // get associated metadata file
        String filePath = folder.getProjectRelativePath().toPortableString() + Constants.DEFAULT_METADATA_FILE_EXTENSION;
        IFile folderMetadataFile = folder.getProject().getFile(filePath);

        Component component = null;
        if (folderMetadataFile.exists()) {
            component = getComponentFromFile(folderMetadataFile, true);
            if (component == null) {
                return null;
            }
        } else if (folder.getParent() != null 
            && folder.getParent().getType() == IResource.FOLDER
            && folder.getParent().exists()) {

            IFolder parent = (IFolder) folder.getParent();
            component = getComponentByFolderName(parent.getName());
            if (component == null) {
                logger.warn("Unable to get component from parent '" + parent.getName() + "'");
                return null;
            }

            component.setName(folder.getName());
            component.setFilePath(folderMetadataFile.getProjectRelativePath().toPortableString());
            component.setSecondaryComponentType(component.getComponentType());
            component.setComponentType(Constants.FOLDER);
            component.setDisplayName(Constants.FOLDER);

            String packageName = getProjectService().getPackageName(folder.getProject());
            component.setPackageName(packageName);
            // get empty component for the folder's sub-type to load defaults, if applicable
            setFolderComponentDefaults(component);
        }

        return component;
    }

    public ComponentList getAssociatedComponents(Component component) throws FactoryException {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }

        ComponentList componentList = getComponentListInstance();
        // get parent sub-folder component
        if (component.isWithinFolder() && component.getFileResource() != null
                && component.getFileResource().getParent() != null
                && component.getFileResource().getParent().getType() == IResource.FOLDER) {
            IFolder parentFolder = (IFolder) component.getFileResource().getParent();

            Component folderComponent = getComponentFromSubFolder(parentFolder, true);
            componentList.add(folderComponent);
        }
        // could be used for workflow-object, layout-object, etc component
        return componentList;
    }

    private void setFolderComponentDefaults(Component componentMetadata) {
        if (Constants.FOLDER.equals(componentMetadata.getComponentType())
                && Utils.isNotEmpty(componentMetadata.getSecondaryComponentType())) {
            Component component = getComponentByComponentType(componentMetadata.getSecondaryComponentType());
            if (component != null) {
                componentMetadata.setWebComponentTypeUrlPart(component.getWebComponentTypeUrlPart());
                componentMetadata.setWebComponentUrlPart(component.getWebComponentUrlPart());
            }
        }
    }

    public ComponentList getComponentsFromResources(IResource[] resources) throws FactoryException {
        if (Utils.isEmpty(resources)) {
            logger.error("Resources cannot be null");
            throw new IllegalArgumentException("Resources cannot be null");
        }

        ComponentList componentList = getComponentListInstance();
        for (IResource resource : resources) {
            if (resource.getType() != IResource.FILE) {
                continue;
            }
            Component component = getComponentFromFile((IFile) resource, true);
            componentList.add(component);
        }
        return componentList;
    }

    public Component getCompositeComponentFromFile(IFile file) throws FactoryException {
        return getCompositeComponentFromFile(file, true);
    }

    /*
     * Get existing composite component with given file resource. Boolean determines whether the body is included.
     */
    private Component getCompositeComponentFromFile(IFile file, boolean includeBody) throws FactoryException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        Component component = getComponentFromFile(file);
        String compositeComponentFilePath = component.getCompositeMetadataFilePath();
        if (Utils.isEmpty(compositeComponentFilePath)) {
            throw new FactoryException("Unable to create component - composite filepath for component "
                    + component.getFullDisplayName() + " is null or empty");
        }

        Component compositeComponent = getComponentByFilePath(compositeComponentFilePath);
        if (compositeComponent == null) {
            throw new FactoryException("Unable to find component composite from path '" + compositeComponentFilePath
                    + "'");
        }

        setCompositeComponentAttributes(compositeComponent, component);

        IFile compositeComponentFile = compositeComponent.getFileResource(file.getProject());
        if (compositeComponentFile == null || !compositeComponentFile.exists()) {
            throw new FactoryException("Unable to find composite file from path '" + compositeComponentFilePath + "'");
        }

        compositeComponent.setFileResource(compositeComponentFile);
        try {
            compositeComponent.loadFromFile(includeBody);
        } catch (Exception e) {
            throw new FactoryException(e);
        }

        return compositeComponent;
    }

    /*
     * Get existing composite component with given associated component. Boolean determines whether the body is
     * included.
     */
    public Component getCompositeComponentFromComponent(Component component) throws FactoryException {
        return getCompositeComponentFromComponent(component, true);
    }

    /*
     * Get existing composite component with given associated component. The component's body will be included.
     */
    private Component getCompositeComponentFromComponent(Component component, boolean includeBody)
            throws FactoryException {
        if (component == null || (Utils.isEmpty(component.getCompositeMetadataFilePath())
            && Utils.isEmpty(component.getMetadataFileExtension()))) {
            throw new IllegalArgumentException(
                "Component and/or component composite filepath/metadata extension cannot be null");
        }
        
        Component compositeComponent = null;
        if (Utils.isNotEmpty(component.getCompositeMetadataFilePath())) {
            compositeComponent = getComponentByFilePath(component.getCompositeMetadataFilePath());
        } else {
            compositeComponent = getComponentByExtension(component.getMetadataFileExtension());
        }

        if (compositeComponent == null) {
            throw new FactoryException(
                "Unable to find component composite from path '" 
                + component.getCompositeMetadataFilePath() 
                + "'");
        }

        setCompositeComponentAttributes(compositeComponent, component);
        if (component.getFileResource() == null || component.getFileResource().getProject() == null) {
            logger.warn(
                "Unable to load existing '" 
                + compositeComponent.getMetadataFilePath()
                + "' from project - associated component's file resource is null.  Assuming new.");
            return compositeComponent;
        }

        IFile compositeComponentFile = compositeComponent.getFileResource(component.getFileResource().getProject());
        if (compositeComponentFile == null || !compositeComponentFile.exists()) {
            logger.warn(
                "Unable to load '" 
                + compositeComponent.getMetadataFilePath()
                + "' from project - composite component file resource is null");
            return compositeComponent;
        }

        compositeComponent.setFileResource(compositeComponentFile);
        try {
            compositeComponent.loadFromFile(includeBody);
        } catch (Exception e) {
            throw new FactoryException(e);
        }

        return compositeComponent;
    }

    public MetadataExt getMetadataExt(Component component) throws JAXBException {
        if (component == null || component.getFileResource() == null || !component.getFileResource().exists()) {
            throw new IllegalArgumentException("Component and/or file resource cannot be null and must exist");
        }

        IFile componentFile = component.getFileResource();

        MetadataExt metadataExt = component.getDefaultMetadataExtInstance();
        Unmarshaller unmarshaller = JAXBContext.newInstance(metadataExt.getClass()).createUnmarshaller();
        JAXBElement<? extends MetadataExt> root =
                unmarshaller.unmarshal(new StreamSource(componentFile.getRawLocation().toFile()), metadataExt
                        .getClass());
        return root.getValue();
    }

    private Component getComponentBean(String id) {
        Component component = null;
        try {
            component = (Component) getBean(id);
        } catch (Exception e) {
            logger.info("Unable to get component for id '" + id + "': " + e.getMessage());
            return null;
        }
        return component;
    }

    private static void setCompositeComponentAttributes(Component compositeComponent, Component component) {
        compositeComponent.setId(component.getId());
        compositeComponent.setMetadataComposite(true);
        compositeComponent.setFilePath(component.getCompositeMetadataFilePath());
        compositeComponent.setName(component.getName());
        compositeComponent.setPackageName(component.getPackageName());
        compositeComponent.setNamespacePrefix(component.getNamespacePrefix());
        compositeComponent.setPackageManifest(false);
        compositeComponent.setInstalled(component.isInstalled());
    }

    public Component createComponent(
        ProjectPackage projectPackage,
        String filePath,
        byte[] file,
        FileMetadataExt fileMetadataHandler) {

        // strip package name from filepath, if applicable
        String tmpFilePath = filePath;
        if (Utils.isNotEmpty(projectPackage.getName()) && filePath.startsWith(projectPackage.getName() + "/")) {
            tmpFilePath = filePath.substring(filePath.indexOf("/") + 1);
        }

        // get object type specific instance of file and set file to new object
        Component component = getComponentByFilePath(tmpFilePath);

        return loadComponentProperties(projectPackage, component, filePath, file, fileMetadataHandler);
    }

    private static Component loadComponentProperties(
        ProjectPackage projectPackage,
        Component component,
        String filePath,
        byte[] file,
        FileMetadataExt fileMetadataHandler) {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null for filepath '" + filePath + "'");
        }

        component.setFilePath(filePath);
        component.setPackageName(projectPackage.getName());
        component.setInstalled(projectPackage.isInstalled());
        if (file != null) {
            component.setFile(file);
        }

        if (filePath.endsWith(Constants.DEFAULT_METADATA_FILE_EXTENSION)) {
            handleComponentMetadata(projectPackage, component, fileMetadataHandler);
        } else {
            setRemoteProperties(component, fileMetadataHandler);
        }

        return component;
    }

    private static void setRemoteProperties(Component component, FileMetadataExt fileMetadataHandler) {
        setRemoteProperties(component, component.getMetadataFilePath(), fileMetadataHandler);
    }

    private static void setRemoteProperties(Component component, String filePath, FileMetadataExt fileMetadataHandler) {
        if (component == null || fileMetadataHandler == null || Utils.isEmpty(filePath)) {
            throw new IllegalArgumentException("Component, filePath,and/or FileMetadataHandler cannot be null");
        }

        // set file properties
        FileProperties fileProperties = fileMetadataHandler.getFilePropertiesByFilePath(filePath);

        if (fileProperties == null) {
            logger.warn("Unable to load file properties for '" + filePath + "' - FileProperties is null");
            return;
        }

        component.setFileProperties(fileProperties);
    }

    private static void handleComponentMetadata(ProjectPackage projectPackage, Component component,
            FileMetadataExt fileMetadataHandler) {
        if (projectPackage == null || component == null || fileMetadataHandler == null) {
            throw new IllegalArgumentException("Component, project package, and/or FileMetadataHandler cannot be null");
        }

        String metadataFilePath = component.getMetadataFilePath();
        String compositeFilePath = component.getCompositeMetadataFilePath();
        if (Utils.isEmpty(compositeFilePath)) {
            logger.warn("Unable to handle metadata component - associated filepath is null");
            return;
        }

        Map<String, byte[]> filePathZipMapping = projectPackage.getFilePathZipMapping();
        if (Utils.isEmpty(filePathZipMapping)) {
            logger.warn("Filepath zip is null or empty.  Skipping.");
            return;
        }

        byte[] file = filePathZipMapping.get(component.getMetadataFilePath());
        if (Utils.isEmpty(file)) {
            logger.warn("Unable to find metadata file '" + component.getMetadataFilePath() + "' in filepath zip");
            return;
        }

        setRemoteProperties(component, compositeFilePath, fileMetadataHandler);

        component.setFilePath(metadataFilePath);
        component.setFileName(metadataFilePath);
    }

    private static boolean isComponentMetadataMatch(String filePath, IComponent component) {
        if (Utils.isEmpty(filePath) || component == null || Utils.isEmpty(component.getFileExtension())) {
            return false;
        }

        boolean hasMetadata = component.isMetadataComposite();

        if (hasMetadata && Utils.isEmpty(component.getMetadataFileExtension())) {
            logger.warn("Unable to determine component metadata for component type '" + component.getComponentType()
                    + "' - metadata file extension is null or empty");
            return false;
        }

        return (hasMetadata && filePath.endsWith(component.getMetadataFileExtension()));
    }

    private void checkComponentMetadata(String path, Component component) {
        if (Utils.isEmpty(path) || component == null) {
            return;
        }

        // check extension for expected metadata part, but folders metatadata, which end in expected part, are not
        // considered metadata, but instead an actual component
        if (path.endsWith(getMetadataFileExtension()) && !Constants.FOLDER.equals(component.getComponentType())) {
            component.setMetadataInstance(true);
        }
    }

    public boolean hasAssociatedComponentTypes(String componentType) {
        Component component = getComponentByComponentType(componentType);
        return Utils.isEmpty(component) ? false : component.hasAssociatedComponentTypes();
    }

    public List<String> getSubComponentTypes(String componentType) {
        Component component = getComponentByComponentType(componentType);
        return Utils.isEmpty(component) ? null : component.getSubComponentTypes();
    }

    public boolean hasSubComponentTypesForComponentType(String componentType) {
        Component component = getComponentByComponentType(componentType);
        return Utils.isEmpty(component) ? false : component.hasSubComponentTypes();
    }

    public List<String> getSubComponentTypes() {
        List<String> componentTypes = getRegisteredComponentTypes();
        List<String> childComponentTypes = new ArrayList<>();

        if (Utils.isNotEmpty(componentTypes)) {
            Component component = null;
            for (String componentType : componentTypes) {
                component = getComponentByComponentType(componentType);
                if (component != null && component.hasSubComponentTypes()) {
                    childComponentTypes.addAll(component.getSubComponentTypes());
                }
            }
        }

        return childComponentTypes;
    }

    public List<String> getParentTypesWithSubComponentTypes() {
        List<String> componentTypes = getRegisteredComponentTypes();
        List<String> parentComponentTypes = new ArrayList<>();

        if (Utils.isNotEmpty(componentTypes)) {
            Component component = null;
            for (String componentType : componentTypes) {
                component = getComponentByComponentType(componentType);
                if (component != null && component.hasSubComponentTypes()) {
                    parentComponentTypes.add(component.getComponentType());
                }
            }
        }

        return parentComponentTypes;
    }
}
