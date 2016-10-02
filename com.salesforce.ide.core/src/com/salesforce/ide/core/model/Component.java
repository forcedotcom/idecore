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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEventHandler;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;
import com.salesforce.ide.api.metadata.types.MetadataExt;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.sforce.soap.metadata.FileProperties;

/**
 * Encapsulates properties and functionality associated with Force.com components.
 * 
 * @author cwall
 */
@SuppressWarnings("unchecked")
public class Component extends ComponentResource {
    
    private static final Logger logger = Logger.getLogger(Component.class);
    private static final Charset UTF_8 = Charset.forName(Constants.UTF_8);
    
    protected String defaultFolder;
    protected String componentType;
    protected String componentTypeAlias;
    protected String displayName;
    protected String alternativeDisplayName;
    protected String fileExtension;
    protected Set<String> supportedApiVersions;
    protected String state = "n/a";
    
    // generic component properties
    // The presence of a non-null/non-empty id indicates that this object exists (at some point, since it could be deleted) on the server. 
    protected String id;
    protected String name;
    protected String fileName;
    protected String fullName;
    protected String apiVersion;
    protected String webComponentTypeUrlPart;
    protected String webComponentUrlPart;
    protected boolean caseSensitive = true;
    protected boolean wildCardSupported = true;
    
    // server properties
    protected String createdById;
    protected String createdByName;
    protected Calendar createdDate;
    protected String lastModifiedById;
    protected String lastModifiedByName;
    protected Calendar lastModifiedDate;
    protected boolean alphaNumeric = false;
    protected int maxLabelLength = 40;
    protected int maxNameLength = 40;
    
    // org specific properties
    protected String namespacePrefix;
    protected boolean installed = false;
    protected boolean active = true;
    protected String packageName;
    
    // composite related
    protected boolean metadataInstance = false;
    protected String metadataFileExtension = "";
    protected boolean metadataComposite = false;
    protected boolean withinFolder = false;
    protected String folderNameIfFolderTypeMdComponent = "";
    
    // Indicates this component types has associated component types that needs to be refresh altogether to return proper content.
    // Ex. Translation needs to be refresh with Custom Labels, Custom App, Custom Web Tabs, Custom Report Type, e.t.c.
    protected boolean hasAssociatedComponentTypes = false;
    protected String parentFolderNameIfComponentMustBeInFolder;
    
    // force related
    protected boolean codeBody = false;
    protected boolean packageManifest = false;
    protected String iconId;
    protected boolean internal = false;
    private boolean textContent = true;
    protected String secondaryComponentType;
    protected boolean remoteDeleteable = true;
    protected boolean remoteAdd = true;
    protected List<String> associatedComponentTypes;
    protected String wizardClassName;
    protected List<String> builtInSubFolders;
    
    // Indicates that this component is actually a bundle (e.g., AuraDefinitionBundle)
    // Items inside bundles must always be transported in a retrieve/deploy even if they haven't changed since the last deploy
    // because omission implies deletion of that item from the bundle.
    protected boolean isBundle = false;
    
    public Component() {
        super();
    }
    
    @Override
    public String getDefaultFolder() {
        if (Utils.isEmpty(defaultFolder)) {
            return getName();
        }
        return defaultFolder;
    }
    
    @Override
    public String getFileExtension() {
        return fileExtension;
    }
    
    @Override
    public String getComponentType() {
        return componentType;
    }
    
    @Override
    public String getComponentTypeAlias() {
        return componentTypeAlias;
    }
    
    public String getSecondaryComponentType() {
        return secondaryComponentType;
    }
    
    public void setSecondaryComponentType(String secondaryComponentType) {
        this.secondaryComponentType = secondaryComponentType;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public String getDisplayName() {
        return componentType.equals(Constants.FOLDER) && Utils.isNotEmpty(secondaryComponentType)
            ? secondaryComponentType + " " + displayName
            : displayName;
    }
    
    public String getAlternativeDisplayName() {
        return Utils.isNotEmpty(alternativeDisplayName) ? alternativeDisplayName : defaultFolder;
    }
    
    public void setAlternativeDisplayName(String alternativeDisplayName) {
        this.alternativeDisplayName = alternativeDisplayName;
    }
    
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
    
    public void setDefaultFolder(String defaultFolder) {
        this.defaultFolder = defaultFolder;
    }
    
    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }
    
    public void setComponentTypeAlias(String componenttTypeAlias) {
        this.componentTypeAlias = componenttTypeAlias;
    }
    
    @Override
    public final String getBody() {
        final byte[] content = getFile();
        if (null == content) {
            logger.warn("Unable to set component body - file byte array is null");
            return null;
        }
        
        // don't get body for, say, binary types; metadata files are always text (see static resources)
        if (!isTextContent() && !isMetadataInstance()) {
            if (logger.isInfoEnabled()) {
                logger.info("Skipping body content setting - component type '" + componentType + "' is not text based");
            }
            return null;
        }
        
        return new String(content, UTF_8);
    }
    
    @Override
    public final void setBody(String body) {
        setFile(null == body ? null : body.getBytes(UTF_8));
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    public void setId(Object id) {
        this.id = (String) id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = Utils.stripExtension(name);
    }
    
    public void setNameFromFilePath(String filePath) {
        if (Utils.isEmpty(filePath) && filePath.lastIndexOf('/') > -1) {
            return;
        }
        
        setName(filePath.substring(filePath.lastIndexOf('/') + 1));
    }
    
    public String getPackageTypeMemberNameWithFolder() {
        if (!isWithinFolder() || Utils.isEmpty(getMetadataFilePath())) {
            return name;
        }
        
        String defaultFolderToken = defaultFolder + "/";
        return Constants.DOCUMENT.equals(componentType)
            ? getMetadataFilePath() .substring(
                getMetadataFilePath().indexOf(defaultFolderToken) + defaultFolderToken.length())
            : getMetadataFilePath().substring(
                getMetadataFilePath().indexOf(defaultFolderToken) + defaultFolderToken.length(),
                getMetadataFilePath().indexOf("." + fileExtension));
    }
    
    // filename is
    @Override
    public String getFileName() {
        if (Utils.isEmpty(fileName) && Utils.isNotEmpty(getMetadataFilePath())
            && getMetadataFilePath().lastIndexOf('/') > -1) {
            fileName = getMetadataFilePath().substring(getMetadataFilePath().lastIndexOf('/') + 1);
            logger.debug(
                "Derived file name '" 
                + fileName 
                + "' from metadata filepath '" 
                + getMetadataFilePath() 
                + "'");
        }
        return fileName;
    }
    
    public void setFileName(Object fileName) {
        this.fileName = (String) fileName;
        
        if (Utils.isEmpty(this.fileName)) {
            return;
        }
        
        if (this.fileName.lastIndexOf('/') > -1) {
            this.fileName = this.fileName.substring(this.fileName.lastIndexOf('/') + 1);
        }
        
        if (Utils.isNotEmpty(name)) {
            setName(this.fileName);
        }
    }
    
    public void setFileNameByFilePath(Object fileName) {
        String tmpFileName = (String) fileName;
        if (Utils.isNotEmpty(tmpFileName) && tmpFileName.lastIndexOf('/') > -1) {
            setFileName(tmpFileName.substring(tmpFileName.lastIndexOf('/') + 1));
        }
    }
    
    @Override
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public void setFullName(Object fullName) {
        setFullName((String) fullName);
    }
    
    public String getWebComponentTypeUrlPart() {
        return webComponentTypeUrlPart;
    }
    
    public void setWebComponentTypeUrlPart(String webComponentTypeUrlPart) {
        this.webComponentTypeUrlPart = webComponentTypeUrlPart;
    }
    
    public String getWebComponentUrlPart() {
        return webComponentUrlPart;
    }
    
    public void setWebComponentUrlPart(String webUrlPart) {
        this.webComponentUrlPart = webUrlPart;
    }
    
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    
    public String getApiVersion() {
        return apiVersion;
    }
    
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    
    @Override
    public boolean isMetadataComposite() {
        return metadataComposite;
    }
    
    public void setMetadataComposite(boolean metadata) {
        this.metadataComposite = metadata;
    }
    
    @Override
    public boolean isWithinFolder() {
        return withinFolder;
    }
    
    public void setWithinFolder(boolean withinFolder) {
        this.withinFolder = withinFolder;
    }
    
    public String getFolderNameIfFolderTypeMdComponent() {
        if (folderNameIfFolderTypeMdComponent == null) {
            folderNameIfFolderTypeMdComponent = getComponentType() + "Folder"; //$NON-NLS-1$
        }
        return folderNameIfFolderTypeMdComponent;
    }
    
    public void setFolderNameIfFolderTypeMdComponent(String componentFolderName) {
        this.folderNameIfFolderTypeMdComponent = componentFolderName;
    }
    
    @Override
    public String getParentFolderNameIfComponentMustBeInFolder() {
        return parentFolderNameIfComponentMustBeInFolder;
    }
    
    public void setParentFolderNameIfComponentMustBeInFolder(String folderName) {
        this.parentFolderNameIfComponentMustBeInFolder = folderName;
    }
    
    public Set<String> getSupportedApiVersions() {
        return supportedApiVersions;
    }
    
    public void setSupportedApiVersions(Set<String> supportedApiVersions) {
        this.supportedApiVersions = supportedApiVersions;
    }
    
    public String getLastSupportedApiVersion() {
        if (Utils.isNotEmpty(supportedApiVersions)) {
            TreeSet<String> tmpSupportedApiVersions = new TreeSet<>();
            tmpSupportedApiVersions.addAll(supportedApiVersions);
            return tmpSupportedApiVersions.last();
        }
        return "";
    }
    
    public String getFirstSupportedApiVersion() {
        if (Utils.isNotEmpty(supportedApiVersions)) {
            TreeSet<String> tmpSupportedApiVersions = new TreeSet<>();
            tmpSupportedApiVersions.addAll(supportedApiVersions);
            return tmpSupportedApiVersions.first();
        }
        return "";
    }
    
    @Override
    public String getCreatedById() {
        return createdById;
    }
    
    public void setCreatedById(Object createdById) {
        this.createdById = (String) createdById;
    }
    
    @Override
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(Object createdByName) {
        this.createdByName = (String) createdByName;
    }
    
    @Override
    public java.util.Calendar getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Object createdDate) {
        this.createdDate = (Calendar) createdDate;
    }
    
    public void setCreatedDateLong(long createdDate) {
        this.createdDate = Calendar.getInstance();
        this.createdDate.setTimeInMillis(createdDate);
    }
    
    @Override
    public String getLastModifiedById() {
        return lastModifiedById;
    }
    
    public void setLastModifiedById(Object lastModifiedById) {
        this.lastModifiedById = (String) lastModifiedById;
    }
    
    @Override
    public String getLastModifiedByName() {
        return lastModifiedByName;
    }
    
    public void setLastModifiedByName(Object lastModifiedByName) {
        this.lastModifiedByName = (String) lastModifiedByName;
    }
    
    @Override
    public Calendar getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    public void setLastModifiedDate(Object lastModifiedDate) {
        this.lastModifiedDate = (Calendar) lastModifiedDate;
    }
    
    public void setLastModifiedDateLong(long lastModifiedDate) {
        this.lastModifiedDate = Calendar.getInstance();
        this.lastModifiedDate.setTimeInMillis(lastModifiedDate);
    }
    
    @Override
    public String getNamespacePrefix() {
        return namespacePrefix;
    }
    
    @Override
    public void setNamespacePrefix(Object namespacePrefix) {
        this.namespacePrefix = (String) namespacePrefix;
    }
    
    public boolean isAlphaNumeric() {
        return alphaNumeric;
    }
    
    public void setAlphaNumeric(boolean alphaNumeric) {
        this.alphaNumeric = alphaNumeric;
    }
    
    public void setMaxLabelLength(int maxLabelLength) {
        this.maxLabelLength = maxLabelLength;
    }
    
    public int getMaxLabelLength() {
        return maxLabelLength;
    }
    
    public void setMaxNameLength(int maxNameLength) {
        this.maxNameLength = maxNameLength;
    }
    
    public int getMaxNameLength() {
        return maxNameLength;
    }
    
    @Override
    public String getPackageName() {
        return packageName;
    }
    
    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    // lookup method injection by container
    public MetadataExt getDefaultMetadataExtInstance() {
        return new MetadataExt();
    }
    
    @Override
    public final boolean isInstalled() {
        return installed;
    }
    
    @Override
    public final void setInstalled(boolean installed) {
        this.installed = installed;
    }
    
    @Override
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
        if (logger.isDebugEnabled()) {
            logger.debug("Component '" + getMetadataFilePath() + "' state is '" + this.state + "'");
        }
    }
    
    public void setManagedFromFilePath(String filePath) {
        if (Utils.isEmpty(filePath)) {
            return;
        }
        
        if (filePath.startsWith(Constants.SOURCE_FOLDER_NAME)) {
            setInstalled(false);
        } else if (filePath.startsWith(Constants.REFERENCED_PACKAGE_FOLDER_NAME)) {
            setInstalled(true);
        }
    }
    
    @Override
    public boolean isPackageManifest() {
        return Utils.isNotEmpty(componentType) && Constants.PACKAGE_MANIFEST.equals(componentType);
    }
    
    public void setPackageManifest(boolean packageManifest) {
        this.packageManifest = packageManifest;
    }
    
    @Override
    public boolean isUnknown() {
        return Utils.isNotEmpty(componentType) && Constants.UNKNOWN_COMPONENT_TYPE.equals(componentType);
    }
    
    public String getIconId() {
        return iconId;
    }
    
    public void setIconId(String iconId) {
        this.iconId = iconId;
    }
    
    @Override
    public boolean isInternal() {
        return internal;
    }
    
    public void setInternal(boolean internal) {
        this.internal = internal;
    }
    
    @Override
    public boolean isTextContent() {
        return textContent;
    }
    
    public void setTextContent(boolean textContent) {
        this.textContent = textContent;
    }
    
    @Override
    public boolean isMetadataInstance() {
        return metadataInstance;
    }
    
    @Override
    public void setMetadataInstance(boolean metadataInstance) {
        this.metadataInstance = metadataInstance;
    }
    
    @Override
    public String getMetadataFileExtension() {
        return getFileExtension() + getMetadataFileExtensionPart();
    }
    
    public String getMetadataFileExtensionPart() {
        return metadataFileExtension;
    }
    
    public void setMetadataFileExtension(String metadataExtension) {
        this.metadataFileExtension = metadataExtension;
    }
    
    @Override
    public String getCompositeMetadataFilePath() {
        return getCompositeMetadataPath(getMetadataFilePath());
    }
    
    public String getCompositeResourceFilePath() {
        return getCompositeMetadataPath(getResourceFilePath());
    }
    
    public String getCompositeFileName() {
        if (Utils.isEmpty(getName()) || Utils.isEmpty(getMetadataFileExtension())) {
            return null;
        }
        
        String rootName;
        if (metadataInstance) {
            rootName = getName().substring(0, getMetadataFilePath().lastIndexOf(getMetadataFileExtensionPart()));
        } else {
            rootName = getMetadataFileExtensionPart();
        }
        
        return rootName;
    }
    
    private String getCompositeMetadataPath(String fromPath) {
        if (Utils.isEmpty(fromPath) || Utils.isEmpty(getMetadataFileExtensionPart())) {
            return null;
        }
        
        String rootFilePath = null;
        if (metadataInstance
            || Constants.FOLDER.equals(componentType) && fromPath.contains(getMetadataFileExtensionPart())) {
            rootFilePath = fromPath.substring(0, fromPath.lastIndexOf(getMetadataFileExtensionPart()));
        } else if (!fromPath.endsWith(getMetadataFileExtensionPart())) {
            rootFilePath = fromPath + getMetadataFileExtensionPart();
        }
        return rootFilePath;
    }
    
    public Map<String, String> getTemplates() {
        return new HashMap<>(0);
    }
    
    public Set<String> getTemplateNames() {
        return getTemplateNames(false);
    }
    
    public Set<String> getTemplateNames(boolean sort) {
        Map<String, String> templates = getTemplates();
        if (Utils.isEmpty(templates)) {
            logger.debug("No templates found");
            return null;
        }
        
        return sort ? new TreeSet<>(templates.keySet()) : templates.keySet();
    }
    
    public String getTemplate(String templateName) {
        Map<String, String> templates = getTemplates();
        if (Utils.isEmpty(templates)) {
            logger.debug("No templates found");
            return Constants.EMPTY_STRING;
        }
        
        return templates.get(templateName);
    }
    
    public String getDefaultTemplateString() {
        String defaultTemplate = getTemplate(Constants.DEFAULT_TEMPLATE_NAME);
        return Utils.isNotEmpty(defaultTemplate)
            ? getNewBodyFromTemplateString(defaultTemplate)
            : Constants.EMPTY_STRING;
    }
    
    public String getNewBodyFromTemplateString(String template) {
        if (Utils.isEmpty(template)) {
            logger.warn("Template is empty");
            return template;
        }
        
        String tmpTemplate = new String(template);
        
        if (Utils.isNotEmpty(getName())) {
            tmpTemplate = tmpTemplate.replace("@@NAME@@", getName());
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Generate the following body from template:\n" + tmpTemplate);
        }
        return tmpTemplate;
    }
    
    public void setBodyFromTemplateName(String templateName) {
        if (Utils.isEmpty(templateName)) {
            logger.warn("Template name is null or empty");
            return;
        }
        
        String template = getTemplate(templateName);
        setBodyFromTemplateString(template);
    }
    
    public void setBodyFromTemplateString(String template) {
        if (Utils.isEmpty(template)) {
            logger.warn("Template is empty");
            return;
        }
        
        String tmpTemplate = new String(template);
        
        if (Utils.isNotEmpty(getName())) {
            tmpTemplate = tmpTemplate.replace("@@NAME@@", getName());
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Generate the following body from template:\n" + tmpTemplate);
        }
        
        initNewBody(tmpTemplate);
    }
    
    public boolean isCodeBody() {
        return codeBody;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void setCodeBody(boolean codeBody) {
        this.codeBody = codeBody;
    }
    
    @Override
    public boolean isRemoteDeleteable() {
        return remoteDeleteable;
    }
    
    public void setRemoteDeleteable(boolean remoteDeleteable) {
        this.remoteDeleteable = remoteDeleteable;
    }
    
    public boolean isRemoteAdd() {
        return remoteAdd;
    }
    
    public void setRemoteAdd(boolean remoteAdd) {
        this.remoteAdd = remoteAdd;
    }
    
    @Override
    public List<String> getAssociatedComponentTypes() {
        return associatedComponentTypes;
    }
    
    public void setAssociatedComponentTypes(List<String> associatedComponentTypes) {
        this.associatedComponentTypes = associatedComponentTypes;
    }
    
    public String getWizardClassName() {
        return wizardClassName;
    }
    
    public void setWizardClassName(String wizardClassName) {
        this.wizardClassName = wizardClassName;
    }
    
    // lookup method injection by container
    public List<String> getSubComponentTypes() {
        return new ArrayList<>();
    }
    
    public boolean hasSubComponentTypes() {
        return Utils.isNotEmpty(getSubComponentTypes());
    }
    
    public boolean hasAssociatedComponentTypes() {
        return hasAssociatedComponentTypes;
    }
    
    public void setHasAssociatedComponentTypes(boolean hasAssociatedComponentTypes) {
        this.hasAssociatedComponentTypes = hasAssociatedComponentTypes;
    }
    
    @Override
    public boolean isWildCardSupported() {
        return wildCardSupported;
    }
    
    public void setWildCardSupported(boolean wildCardSupported) {
        this.wildCardSupported = wildCardSupported;
    }
    
    public List<String> getBuiltInSubFolders() {
        return builtInSubFolders;
    }
    
    public void setBuiltInSubFolders(List<String> builtInSubFolders) {
        this.builtInSubFolders = builtInSubFolders;
    }
    
    public boolean isBundle() {
        return isBundle;
    }
    
    public void setBundle(boolean isBundle) {
        this.isBundle = isBundle;
    }
    
    public MetadataExt getMetadataExtFromBody() throws JAXBException {
        MetadataExt metadataExt = getDefaultMetadataExtInstance();
        return metadataExt.getComponentFromXML(getBody());
    }
    
    public MetadataExt getMetadataExtFromBody(boolean validate, ValidationEventHandler validationEventHandler)
        throws JAXBException {
        MetadataExt metadataExt = getDefaultMetadataExtInstance();
        return metadataExt.getComponentFromXML(getBody(), validate, validationEventHandler);
    }
    
    public Object getSubMetadataExtFromBody(Class<? extends MetadataExt> componentClass)
        throws IllegalAccessException, JAXBException, IllegalArgumentException, InvocationTargetException {
        String className = componentClass.getSimpleName();
        return getSubMetadataExtFromBody(className);
    }
    
    public Object getSubMetadataExtFromBody(String componentType)
        throws IllegalAccessException, JAXBException, IllegalArgumentException, InvocationTargetException {
        if (Utils.isEmpty(getBody()) || Utils.isEmpty(componentType)) {
            logger.warn("Component body and/or to-be-retrieved component type are null or empty");
            return null;
        }
        
        Object value = null;
        MetadataExt metadataExt = getMetadataExtFromBody();
        Method getterMethod = Utils.getGetterMethod(metadataExt.getClass(), componentType + "s");
        
        if (getterMethod == null) {
            logger.warn(
                "No getter method found for property with name '" + componentType + "' on class '"
                    + metadataExt.getClass() + "' - trying plural");
            return value;
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug(
                "Invoking method '" + getterMethod.getName() + "' on instance of '" + metadataExt.getClass() + "'");
        }
        
        Object[] args = null;
        return getterMethod.invoke(metadataExt, args);
    }
    
    @Override
    public String getFullDisplayName() {
        String tempComponentType = Utils.isNotEmpty(getComponentType()) ? getComponentType() : "";
        String subComponentType =
            Utils.isNotEmpty(getSecondaryComponentType()) ? " [" + getSecondaryComponentType() + "]" : "";
        String metadataFilePath = Utils.isNotEmpty(getMetadataFilePath()) ? " '" + getMetadataFilePath() + "'" : "";
        String packageName = Utils.isNotEmpty(getPackageName()) ? " [" + getPackageName() + "]" : "";
        StringBuffer strBuff = new StringBuffer(tempComponentType);
        strBuff.append(subComponentType).append(metadataFilePath).append(packageName);
        return strBuff.toString();
    }
    
    public final void initNewBody(String body) {
        setBody(body);
    }
    
    @Override
    public final void loadComponentProperties(IFile file) {
        setId(getId(file));
        setFileName(getFileName(file));
        setFullName(getFullName(file));
        setFilePath(file.getProjectRelativePath().toPortableString());
        setNamespacePrefix(getNamespacePrefix(file));
        setPackageName(getPackageName(file));
        setState(getState(file));
        
        setCreatedByName(getCreatedByName(file));
        setCreatedById(getCreatedById(file));
        
        String createDate = getCreatedDate(file);
        if (Utils.isNotEmpty(createDate)) {
            long createdDate = Long.parseLong(createDate);
            setCreatedDateLong(createdDate);
        }
        
        setLastModifiedByName(getLastModifiedByName(file));
        setLastModifiedById(getLastModifiedById(file));
        
        String modifiedDate = getLastModifiedDate(file);
        if (Utils.isNotEmpty(modifiedDate)) {
            long createdDate = Long.parseLong(modifiedDate);
            setLastModifiedDateLong(createdDate);
        }
    }
    
    public void setFileProperties(FileProperties fileProperties) {
        if (fileProperties == null) {
            logger.warn("No properties to set on " + getFullDisplayName() + " - FileProperties is null");
            return;
        }
        
        List<String> properties = Utils.getProperties(FileProperties.class);
        for (String propertyName : properties) {
            Object propertyValue = null;
            try {
                propertyValue = Utils.getPropertyValue(fileProperties, propertyName);
                setProperty(propertyName, propertyValue);
            } catch (Exception e) {
                logger.error("Unable to get property '" + propertyName + "' for file '" + fileName + "'", e);
            }
        }
        
        if (fileProperties.getManageableState() != null) {
            setState(fileProperties.getManageableState().name());
        }
    }
    
    protected void setProperty(String propertyName, Object propertyValue) {
        try {
            Method setterMethod = getSetterMethod(this, propertyName);
            if (setterMethod != null) {
                setterMethod.invoke(this, propertyValue);
            }
        } catch (Exception e) {
            logger.warn("Unable to set property '" + propertyName + "' with value '" + propertyValue + "'", e);
        }
    }
    
    private static Method getSetterMethod(Component component, String methodName) throws ClassNotFoundException {
        Method setterMethod = null;
        Class<?> componentClass = Class.forName(component.getClass().getName());
        Method[] methods = componentClass.getMethods();
        if (Utils.isNotEmpty(methods)) {
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().equals("set" + methodName)) {
                    setterMethod = method;
                    break;
                }
            }
        }
        return setterMethod;
    }
    
    public boolean hasRemoteChanged(Component anotherComponent, IProgressMonitor monitor) throws InterruptedException {
        return hasRemoteChanged(anotherComponent, true, monitor);
    }
    
    public boolean hasRemoteChanged(Component anotherComponent, boolean includeIdCheck, IProgressMonitor monitor)
        throws InterruptedException {
        monitorCheck(monitor);
        if (equals(anotherComponent, includeIdCheck)) {
            return hasChanged(anotherComponent, getRemoteChangedPredicates());
        }
        logger.warn("Conflict found! Attribute compare of local and remote " + getFullDisplayName() + " are NOT equal");
        return true;
    }
    
    public boolean hasRemoteBundleChanged(ProjectPackage pkg, IProgressMonitor monitor) throws InterruptedException {
        throw new UnsupportedOperationException();
    }
    
    private final Predicate<Component> somethingChangedPredicate =
        remoteComponent -> getBodyChecksum() != remoteComponent.getBodyChecksum();
    private final Predicate<Component> remoteChangedPredicate =
        remoteComponent -> getOriginalBodyChecksum() != remoteComponent.getBodyChecksum();
    private final Predicate<Component> localChangedPredicate =
        remoteComponent -> getBodyChecksum() != getOriginalBodyChecksum();
        
    public List<Predicate<Component>> getRemoteChangedPredicates() {
        return Lists.newArrayList(somethingChangedPredicate, remoteChangedPredicate);
    }
    
    public List<Predicate<Component>> getLocalChangedPredicates() {
        return Lists.newArrayList(somethingChangedPredicate, localChangedPredicate);
    }
    
    private static boolean hasChanged(Component remoteComponent, List<Predicate<Component>> predicates) {
        for (Predicate<Component> predicate : predicates) {
            if (!predicate.test(remoteComponent)) {
                return false;
            }
        }
        return true;
    }
    
    public final boolean hasLocalChanged() {
        boolean hasChanged = false;
        if (getBodyChecksum() != getOriginalBodyChecksum()) {
            if (logger.isInfoEnabled()) {
                logger.info(
                    "Current local " + getFullDisplayName() + " has changed.  Current body checksum ["
                        + getBodyChecksum() + "] and original body checksum [" + getOriginalBodyChecksum()
                        + "] NOT equal");
            }
            return true;
        }
        
        return hasChanged;
    }
    
    public boolean hasEitherChanged(Component anotherComponent, IProgressMonitor monitor) throws InterruptedException {
        monitorCheck(monitor);
        return hasChanged(anotherComponent, getLocalChangedPredicates())
            || hasChanged(anotherComponent, getRemoteChangedPredicates());
    }
    
    public void encodeName() {
        if (Utils.isEmpty(getName())) {
            return;
        }
        setName(Utils.encode(getName()));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        return equals(obj, false);
    }
    
    public boolean equals(Object obj, boolean includeIdCheck) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Component other = (Component) obj;
        
        if (includeIdCheck) {
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
        }
        
        if (getComponentType() == null) {
            if (other.getComponentType() != null)
                return false;
        } else if (!getComponentType().equals(other.getComponentType()))
            return false;
            
        if (getMetadataFilePath() == null) {
            if (other.getMetadataFilePath() != null)
                return false;
        } else if (!isCaseSensitive() && !getMetadataFilePath().equalsIgnoreCase(other.getMetadataFilePath())) {
            return false;
        } else if (!getMetadataFilePath().equals(other.getMetadataFilePath())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Constructs a <code>String</code> with all attributes in name = value format.
     * 
     * @return a <code>String</code> representation of this object.
     */
    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("Component ( ").append("componentType=").append(this.componentType).append(TAB)
            .append("subComponentType=").append(this.secondaryComponentType).append(TAB).append("id=").append(this.id)
            .append(TAB).append("name=").append(this.name).append(TAB).append("fileName=").append(this.fileName)
            .append(TAB).append("fullName=").append(this.fullName).append(TAB).append("packageName=")
            .append(this.packageName).append(TAB).append(super.toString()).append(TAB).append("defaultFolder=")
            .append(this.defaultFolder).append(TAB).append("displayName=").append(this.displayName).append(TAB)
            .append("fileExtension=").append(this.fileExtension).append(TAB).append("state=").append(this.state)
            .append(TAB).append("apiVersion=").append(this.apiVersion).append(TAB).append("webComponentTypeUrlPart=")
            .append(this.webComponentTypeUrlPart).append(TAB).append("webUrlPart=").append(this.webComponentUrlPart)
            .append(TAB).append("caseSensitive=").append(this.caseSensitive).append(TAB).append("createdById=")
            .append(this.createdById).append(TAB).append("createdByName=").append(this.createdByName).append(TAB)
            .append("createdDate=").append(Utils.getDisplayDate(this.createdDate)).append(TAB)
            .append("lastModifiedById=").append(this.lastModifiedById).append(TAB).append("lastModifiedByName=")
            .append(this.lastModifiedByName).append(TAB).append("lastModifiedDate=")
            .append(Utils.getDisplayDate(this.lastModifiedDate)).append(TAB).append("alphaNumeric=")
            .append(this.alphaNumeric).append(TAB).append("namespacePrefix=").append(this.namespacePrefix).append(TAB)
            .append("managed=").append(this.installed).append(TAB).append("active=").append(this.active).append(TAB)
            .append("metadataFileExtension=").append(this.metadataFileExtension).append(TAB)
            .append("metadataComposite=").append(this.metadataComposite).append(TAB).append("withinFolder=")
            .append(this.withinFolder).append(TAB).append("folderName=")
            .append(this.parentFolderNameIfComponentMustBeInFolder).append(TAB).append("codeBody=")
            .append(this.codeBody).append(TAB).append("packageManifest=").append(this.packageManifest).append(TAB)
            .append("remoteDeleteable=").append(this.remoteDeleteable).append(TAB).append("associatedComponentTypes=")
            .append(this.associatedComponentTypes).append(TAB).append("builtInSubFolders=")
            .append(this.builtInSubFolders).append(TAB);
        if (isTextContent() || (Utils.isNotEmpty(fileName) && fileName.endsWith("txt"))) {
            retValue.append("body =\n").append(getBody());
        }
        retValue.append(")");
        return retValue.toString();
    }
    
    @Override
    public String toStringLite() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("Component ( ").append("displayName=").append(getFullDisplayName()).append(TAB).append("path=")
            .append(getResourceFilePath()).append(TAB).append("id=").append(this.id).append(TAB).append("metadata=")
            .append(this.metadataComposite).append(TAB).append("packageManifest=").append(this.packageManifest)
            .append(TAB).append("managed=").append(this.installed).append(TAB).append(super.toStringLite())
            .append(" )");
        return retValue.toString();
    }

    @Override
    public Component preComponentListAddition(PackageConfiguration config) {
        return this;
    }
}
