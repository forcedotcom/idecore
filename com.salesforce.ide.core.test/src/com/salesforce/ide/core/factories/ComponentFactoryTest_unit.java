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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;

import com.salesforce.ide.api.metadata.types.CustomObject;
import com.salesforce.ide.api.metadata.types.MetadataExt;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.test.common.utils.ComponentTypeEnum;
import com.salesforce.ide.test.common.utils.IdeTestConstants;
import com.salesforce.ide.test.common.utils.IdeTestUtil;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ManageableState;

/**
 * Please ensure that this unit test is kept up-to-date. It's a bit of work (with some code duplication) but it gives us
 * more confidence that we have handled things properly. We can change how things are done when we have a
 * feature-complete metadata complete call.
 * 
 * @author nchen (and various)
 * 
 */
public class ComponentFactoryTest_unit extends TestCase {
	
    private ComponentFactory componentFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.componentFactory = IdeTestUtil.getComponentFactory();
    }

    // W-1773067
    // We can remove this after we are done deciding how to support this new type
    public void testInstalledPackageIsExcluded() {
        List<String> list = Arrays.asList(componentFactory.getDefaultDisabledComponentTypes());
        assertTrue("InstalledPackage component is excluded", list.contains("InstalledPackage"));
    }

    public void testStandardObjectComponentDoesNotHaveAWizard() throws Exception {
        Component stdObj = componentFactory.getComponentByComponentType(Constants.STANDARD_OBJECT);
        assertTrue(IdeTestUtil.isEmpty(stdObj.getWizardClassName()));
    }

    public void testFolderComponentContentisNonBinary() throws Exception {
        final Component folderComponent = componentFactory.getComponentByComponentType(Constants.FOLDER);
        assertTrue(folderComponent.isTextContent());
    }

    public void testComponentFactory_applicationContextFactory() throws Exception {
        assertNotNull(this.componentFactory.getBean("componentFactory"));
    }

    // Test subcomponents of custom object
    //////////////////////////////////////

    public void testActionOverrideExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("ActionOverride"));
    }

    public void testBusinessProcessExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("BusinessProcess"));
    }

    public void testCompactLayoutExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("CompactLayout"));
    }

    public void testCustomFieldExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("CustomField"));
    }

    public void testFieldSetExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("FieldSet"));
    }

    public void testListViewExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("ListView"));
    }

    public void testNamedFilterExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("NamedFilter"));
    }

    public void testPicklistExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("Picklist"));
    }

    public void testRecordTypeExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("RecordType"));
    }

    public void testSearchLayoutsExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("SearchLayouts"));
    }

    public void testSharingReasonExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("SharingReason"));
    }

    public void testSharingRecalculationExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("SharingRecalculation"));
    }

    public void testValidationRuleExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("ValidationRule"));
    }

    public void testWeblinkExists_AsSubtypeOfCustomObject() throws Exception {
        assertTrue(getCustomObjectSubtypes().contains("WebLink"));
    }
    
    // Test secondary component of custom object
    ////////////////////////////////////////////
    
    public void testSecondaryComponentOfCustomObject() {
        // To fetch a standard object, e.g., Account,
        // you actually have to use a type of CustomObject, not StandardObject in the package.xml
        // The secondaryComponent allows you to treat an object as a different type of another.
        // See https://developer.salesforce.com/docs/atlas.en-us.api_meta.meta/api_meta/manifest_samples.htm#manifest_standard_objects
        final Component customObject = componentFactory.getComponentByComponentType(Constants.STANDARD_OBJECT);
        String secondaryComponentType = customObject.getSecondaryComponentType();
        assertEquals(Constants.CUSTOM_OBJECT, secondaryComponentType);
    }

    // Test subcomponents of workflow
    /////////////////////////////////

    public void testWorkflowActionFlowExists_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowFlowAction"));
    }

    public void testWorkflowAlertExists_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowAlert"));
    }

    public void testWorkflowApexExists_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowApex"));
    }

    public void testWorkflowChatterPostExists_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowChatterPost"));
    }

    public void testWorkflowFieldUpdateExists_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowFieldUpdate"));
    }

    public void testWorkflowKnowledgePublishExists_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowKnowledgePublish"));
    }

    public void testWorkflowKnowledgePublish_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowOutboundMessage"));
    }

    public void testWorkflowOutboundMessageExists_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowOutboundMessage"));
    }

    public void testWorkflowQuickCreateExists_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowQuickCreate"));
    }

    public void testWorkflowRuleExists_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowRule"));
    }

    public void testWorkflowSend_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowSend"));
    }

    public void testWorkflowTaskExists_AsSubtypeOfWorkflow() throws Exception {
        assertTrue(getWorkflowSubtypes().contains("WorkflowTask"));
    }

    public void testWildcardSupportedTypeIncludesSubtypes() throws Exception {
        List<String> wildcardSupportedComponentTypes = componentFactory.getWildcardSupportedComponentTypes();
        assertTrue(wildcardSupportedComponentTypes.containsAll(componentFactory.getSubComponentTypes()));
    }

    public void testScontrolWizardUnavailable() throws Exception {
        Component scontrol = componentFactory.getComponentByComponentType(Constants.SCONTROL);
        assertNull(scontrol.getWizardClassName());
    }

    public void testRemoteSiteSetting() throws Exception {
        Component component = componentFactory.getComponentByComponentType(Constants.REMOTE_SITE_SETTING);
        assertNotNull(component);
    }

    public void testPermissionSetExists() throws Exception {
        final Component permissionSet = componentFactory.getComponentByComponentType(Constants.PERMISSIONSET);
        assertNotNull(permissionSet);
        assertEquals(stripDotFromExtension(ComponentTypeEnum.PermissionSet.getFileExtension()),
            permissionSet.getFileExtension());
        assertEquals(ComponentTypeEnum.PermissionSet.getParentFolderName(), permissionSet.getDefaultFolder());
        assertEquals(ComponentTypeEnum.PermissionSet.getDisplayName(), permissionSet.getDisplayName());
        assertEquals(ComponentTypeEnum.PermissionSet.getComponentTypeWebUrlPart(),
            permissionSet.getWebComponentTypeUrlPart());
        assertEquals(ComponentTypeEnum.PermissionSet.getComponentWebUrlPart(), permissionSet.getWebComponentUrlPart());
        assertFalse(permissionSet.isCaseSensitive());
        assertNotNull(permissionSet.getDefaultMetadataExtInstance());
    }

    public void testQueueExists() throws Exception {
        final Component queue = componentFactory.getComponentByComponentType(Constants.QUEUE);
        assertNotNull(queue);
        assertEquals(stripDotFromExtension(ComponentTypeEnum.Queue.getFileExtension()), queue.getFileExtension());
        assertEquals(ComponentTypeEnum.Queue.getParentFolderName(), queue.getDefaultFolder());
        assertEquals(ComponentTypeEnum.Queue.getDisplayName(), queue.getDisplayName());
        assertEquals(ComponentTypeEnum.Queue.getComponentTypeWebUrlPart(), queue.getWebComponentTypeUrlPart());
        assertEquals(ComponentTypeEnum.Queue.getComponentWebUrlPart(), queue.getWebComponentUrlPart());
        assertFalse(queue.isCaseSensitive());
        assertNotNull(queue.getDefaultMetadataExtInstance());
    }

    public void testFlowExists() throws Exception {
        final Component flow = componentFactory.getComponentByComponentType(Constants.FLOW);
        assertNotNull(flow);
        assertEquals(stripDotFromExtension(ComponentTypeEnum.Flow.getFileExtension()), flow.getFileExtension());
        assertEquals(ComponentTypeEnum.Flow.getParentFolderName(), flow.getDefaultFolder());
        assertEquals(ComponentTypeEnum.Flow.getDisplayName(), flow.getDisplayName());
        assertEquals(ComponentTypeEnum.Flow.getComponentTypeWebUrlPart(), flow.getWebComponentTypeUrlPart());
        assertEquals(ComponentTypeEnum.Flow.getComponentWebUrlPart(), flow.getWebComponentUrlPart());
        assertFalse(flow.isCaseSensitive());
        assertNotNull(flow.getDefaultMetadataExtInstance());
    }

    public void testDataCategoryGroupExists() throws Exception {
        final Component dataCategoryGroup = componentFactory.getComponentByComponentType(Constants.DATACATEGORYGROUP);
        assertNotNull(dataCategoryGroup);
        assertEquals(stripDotFromExtension(ComponentTypeEnum.DataCategoryGroup.getFileExtension()),
            dataCategoryGroup.getFileExtension());
        assertEquals(ComponentTypeEnum.DataCategoryGroup.getParentFolderName(), dataCategoryGroup.getDefaultFolder());
        assertEquals(ComponentTypeEnum.DataCategoryGroup.getTypeName(), dataCategoryGroup.getDisplayName());
        assertEquals(ComponentTypeEnum.DataCategoryGroup.getComponentTypeWebUrlPart(),
            dataCategoryGroup.getWebComponentTypeUrlPart());
        assertEquals(ComponentTypeEnum.DataCategoryGroup.getComponentWebUrlPart(),
            dataCategoryGroup.getWebComponentUrlPart());
        assertFalse(dataCategoryGroup.isCaseSensitive());
        assertNotNull(dataCategoryGroup.getDefaultMetadataExtInstance());
    }

    public void testApexRelatedNamesAreCaseInsensitive() throws Exception {
        assertFalse((componentFactory.getComponentByComponentType(Constants.APEX_CLASS)).isCaseSensitive());
        assertFalse((componentFactory.getComponentByComponentType(Constants.APEX_COMPONENT)).isCaseSensitive());
        assertFalse((componentFactory.getComponentByComponentType(Constants.APEX_TRIGGER)).isCaseSensitive());
    }

    public void testEntitlementTemplateExists() throws Exception {
        final Component entitlementTemplate =
                componentFactory.getComponentByComponentType(Constants.ENTITLEMENT_TEMPLATE);
        assertNotNull(entitlementTemplate);
        final String fileExtension = ComponentTypeEnum.EntitlementTemplate.getFileExtension();
        assertEquals(stripDotFromExtension(fileExtension), entitlementTemplate.getFileExtension());
        assertEquals(ComponentTypeEnum.EntitlementTemplate.getParentFolderName(),
            entitlementTemplate.getDefaultFolder());
        assertEquals(ComponentTypeEnum.EntitlementTemplate.getDisplayName(), entitlementTemplate.getDisplayName());
        assertEquals(ComponentTypeEnum.EntitlementTemplate.getComponentTypeWebUrlPart(),
            entitlementTemplate.getWebComponentTypeUrlPart());
        assertEquals(ComponentTypeEnum.EntitlementTemplate.getComponentWebUrlPart(),
            entitlementTemplate.getWebComponentUrlPart());
        assertFalse(entitlementTemplate.isCaseSensitive());
        assertNotNull(entitlementTemplate.getDefaultMetadataExtInstance());

    }

    private String stripDotFromExtension(final String dataCategoryFileExtension) {
        return dataCategoryFileExtension.substring(1);
    }

    public void testComponentFactory_baseFactory() throws Exception {
        assertNotNull(componentFactory.getPackageManifestFactory());
        assertNotNull(componentFactory.getConnectionFactory());
        assertNotNull(componentFactory.getComponentListInstance());
        assertNotNull(componentFactory.getServiceLocator());
        assertNotNull(componentFactory.getProjectService());
        assertNotNull(componentFactory.getProjectPackageFactory());
        assertNotNull(componentFactory.getProjectPackageListInstance());
    }

    public void testComponentFactory_getAttributes() throws Exception {

        assertTrue("Component registry should not be null or empty",
            Utils.isNotEmpty(componentFactory.getComponentRegistry()));
        assertTrue("Metadata file extension cannot be null or empty",
            Utils.isNotEmpty(componentFactory.getMetadataFileExtension()));
        assertTrue("Component registry cannot be null or empty",
            Utils.isNotEmpty(componentFactory.getComponentRegistry()));
        assertNotNull("ComponentList instance cannot be null or empty", componentFactory.getComponentListInstance());
    }

    public void testComponentFactory_getRegisteredComponentsForComponentTypes() throws Exception {
        ComponentList components = componentFactory.getRegisteredComponents();
        assertTrue("Registered components for given object types cannot be null or empty", Utils.isNotEmpty(components));
        final List<String> goldList = ComponentTypeEnum.getAllTypeNames();
        for (Component c : components) {
            assertTrue("Component Registry doesn't contain " + c.getComponentType(),
                goldList.contains(c.getComponentType()));
        }
    }

    public void testComponentFactory_getRegisteredComponentFolderByComponentType() throws ForceProjectException {
        String folder = componentFactory.getRegisteredComponentFolderByComponentType(Constants.APEX_CLASS);
        assertTrue("Registered folder for given object types cannot be null or empty", Utils.isNotEmpty(folder));
        assertTrue(Constants.APEX_CLASS + " folder must be 'classes'", "classes".equals(folder));
    }

    public void testComponentFactory_getComponentTypeByFolderName() throws Exception {
        String componentType = componentFactory.getComponentTypeByFolderName("scontrols");
        assertTrue("Object type for 'scontrols' folder cannot be null or empty", Utils.isNotEmpty(componentType));
        assertTrue("Object type for 'scontrols' must be " + Constants.SCONTROL,
            Constants.SCONTROL.equals(componentType));
    }
    
    public void testComponentFactory_getComponentByComponentTypes() throws Exception {
        for (Component componentType : componentFactory.getRegisteredComponents()) {
        	componentTypeTestWork(componentType);
        }
    }

    public void testComponentFactory_getFolderComponentByFilePath() throws Exception {
        ComponentList folderComponents = componentFactory.getFolderComponents();
        assertTrue("Component folder list should not be null or empty", Utils.isNotEmpty(folderComponents));

        Component component = null;
        for (Component folderComponent : folderComponents) {
            component =
                    componentFactory.getComponentByFilePath(folderComponent.getDefaultFolder() + "/Whatever-meta.xml");
            assertNotNull("Component for folder should not be null", component);
            assertTrue("Expected component of type " + Constants.FOLDER,
                Constants.FOLDER.equals(component.getComponentType()));
            assertTrue("Expected component of subtype " + folderComponent.getComponentType(), folderComponent
                .getComponentType().equals(component.getSecondaryComponentType()));
        }
    }

    private void componentTypeTestWork(Component component) {
        String componentType = component.getComponentType();
        
        assertNotNull("Component for object type '" + componentType + "' cannot be null", component);

        assertTrue("Expected '" + componentType + "' object type, got '" + component.getComponentType() + "'",
            componentType.equals(component.getComponentType()));

        assertTrue("Display name for object type '" + componentType + "' cannot be null or empty",
            Utils.isNotEmpty(component.getDisplayName()));

        if (shouldCheckForDefaultFolder(componentType, component)) {
            assertTrue("Default folder for object type '" + componentType + "' cannot be null or empty",
                Utils.isNotEmpty(component.getDefaultFolder()));
        }

        if (shouldCheckForFileExtension(componentType, component)) {
            assertTrue("File extension for object type '" + componentType + "' cannot be null or empty",
                Utils.isNotEmpty(component.getFileExtension()));
        }

        if (component.isMetadataComposite()) {
            assertTrue("Metadata extension for object type '" + componentType + "' cannot be null",
                Utils.isNotEmpty(component.getMetadataFileExtension()));
        }

        if (component.isCodeBody()) {
            apexComponents(component);
        }
    }

    private boolean shouldCheckForDefaultFolder(String componentType, Component component) {
        return !(Constants.UNKNOWN_COMPONENT_TYPE.equals(component.getComponentType()) || Constants.FOLDER
                .equals(componentType));
    }

    private boolean shouldCheckForFileExtension(String componentType, Component component) {
        return !(Constants.DOCUMENT.equals(component.getComponentType())
            || Constants.UNKNOWN_COMPONENT_TYPE.equals(component.getComponentType())
            || Constants.FOLDER.equals(componentType) 
            || Constants.AURA_DEFINITION_BUNDLE.equals(componentType));
    }

    private void apexComponents(Component component) {
        if (!Constants.SCONTROL.equals(component.getComponentType())) {
            assertTrue("Supported API versions for object type '" + component.getComponentType()
                + "' cannot be null or empty", Utils.isNotEmpty(component.getSupportedApiVersions()));
        }
    }

    public void testComponentFactory_getComponentsByExtension() throws Exception {
        for (Component component : componentFactory.getRegisteredComponents()) {
            String componentType = component.getComponentType();
            if (Constants.DOCUMENT.equals(componentType) 
                || Constants.FOLDER.equals(componentType)
                || Constants.UNKNOWN_COMPONENT_TYPE.equals(componentType)
                || Constants.AURA_DEFINITION_BUNDLE.equals(componentType)) {
                continue;
            }
            extensionTestWork(componentType);
        }
    }

    private void extensionTestWork(String componentType) throws Exception {

        Component tmpComponent = getComponentByComponentType(componentType);
        Component component = componentFactory.getComponentByExtension(tmpComponent.getFileExtension());
        assertNotNull("Component for extension '" + tmpComponent.getFileExtension() + "' cannot be null", component);
        assertTrue("Expected '" + tmpComponent.getFileExtension() + "' extension, got '" + component.getFileExtension()
            + "' for object type '" + componentType + "'",
            tmpComponent.getFileExtension().equals(component.getFileExtension()));
    }

    private Component getComponentByComponentType(String componentType) throws FactoryException, ForceProjectException {
        return componentFactory.getComponentByComponentType(componentType);
    }

    public void testComponentFactory_getComponentsByFilePath() throws Exception {
        String[][] filePathsAndExpectedTypes =
            { { "classes/Whatever.cls", Constants.APEX_CLASS },
                { "mypackage/classes/Whatever.cls", Constants.APEX_CLASS },
                { "objects/Whatever" + Constants.CUSTOM_OBJECT_SUFFIX + ".object", Constants.CUSTOM_OBJECT },
                { "scontrols/Test2.scf-meta.xml", Constants.SCONTROL },
                { "email/ChrisTemplateFolder/Whatever.email", Constants.EMAIL_TEMPLATE },
                { "mypackage/email/ChrisTemplateFolder", Constants.FOLDER },
                { "mypackage/email/ChrisTemplateFolder-meta.xml", Constants.FOLDER },
                { "mypackage/documents/DocFolder/mydoc.txt", Constants.DOCUMENT },
                { "package.xml", Constants.PACKAGE_MANIFEST },
                { "whatever/Whatever.cls-meta.xml", Constants.APEX_CLASS } };

        for (String[] filePathsAndExpectedType : filePathsAndExpectedTypes) {
            filepathTestWork(filePathsAndExpectedType);
        }
    }

    private void filepathTestWork(String[] filePathsAndExpectedType) throws Exception {
        Component component = componentFactory.getComponentByFilePath(filePathsAndExpectedType[0]);
        assertNotNull("Component for filepath '" + filePathsAndExpectedType[0] + "' cannot be null", component);
        assertTrue("Expected '" + filePathsAndExpectedType[1] + "' object type, got '" + component.getComponentType()
            + "'", filePathsAndExpectedType[1].equals(component.getComponentType()));
        if (Constants.FOLDER.equals(component.getComponentType()) && filePathsAndExpectedType[0].contains("email")) {
            assertTrue(
                "Expected '" + Constants.EMAIL_TEMPLATE + "' secondary object type, got '"
                        + component.getSecondaryComponentType() + "'",
                        Utils.isNotEmpty(component.getSecondaryComponentType())
                        && component.getSecondaryComponentType().equals(Constants.EMAIL_TEMPLATE));
        }
    }

    public void testComponentFactory_getComponentsByFolder() throws Exception {
        String[][] folderAndExpectedTypes =
            { { "classes", Constants.APEX_CLASS }, { "objects", Constants.CUSTOM_OBJECT },
                { "scontrols", Constants.SCONTROL }, { "whatever", Constants.UNKNOWN_COMPONENT_TYPE } };

        for (String[] folderAndExpectedType : folderAndExpectedTypes) {
            folderTestWork(folderAndExpectedType);
        }
    }

    public void testComponentFactory_getComponentByComponentTypeClass() throws Exception {
        Component component = loadAndCheckCustomObject();
        CustomObject customObject = (CustomObject) component.getMetadataExtFromBody();
        assertNotNull("Custom Object object should not be null", customObject);
        Component customObjectComponent = componentFactory.getComponentByComponentTypeClass(customObject);
        assertTrue("CustomObject component should not be null", customObjectComponent != null
                && Constants.CUSTOM_OBJECT.equals(customObjectComponent.getComponentType()));
        assertTrue("CustomObject component body should not be null", Utils.isNotEmpty(customObjectComponent.getBody()));
    }

    private void folderTestWork(String[] folderAndExpectedType) throws Exception {
        Component component = componentFactory.getComponentByFolderName(folderAndExpectedType[0]);
        assertNotNull("Component for folder '" + folderAndExpectedType[0] + "' cannot be null", component);
        assertTrue("Expected '" + folderAndExpectedType[1] + "' object type, got '" + component.getComponentType()
            + "'", folderAndExpectedType[1].equals(component.getComponentType()));
    }

    public void testComponentFactory_createComponent_unmanaged() throws Exception {
        Component component = createComponentWork(false);
        assertFalse("Expected 'false' object type, got '" + component.isInstalled() + "'", component.isInstalled());
    }

    public void testComponentFactory_createComponent_managed() throws Exception {
        Component component = createComponentWork(true);
        assertTrue("Expected 'true' managed object type, got '" + component.isInstalled() + "'",
            component.isInstalled());
    }

    private Component createComponentWork(boolean managed) throws Exception {
        ProjectPackage projectPackage = IdeTestUtil.getProjectPackageFactory().getProjectPackageInstance();
        String id = "12345";
        projectPackage.setId(id);
        projectPackage.setManaged(managed);
        projectPackage.setOrgId(id + "ABC");
        if (managed) {
            projectPackage.setInstalled(true);
        }

        byte[] file = "This is a body".getBytes();
        String filePath = "classes/Whatever.cls";
        String name = "whatever";
        Calendar cal = Calendar.getInstance();
        FileProperties fileProperties = new FileProperties();
        fileProperties.setCreatedById(id);
        fileProperties.setCreatedByName(name);
        fileProperties.setCreatedDate(cal);
        fileProperties.setFileName(filePath);
        fileProperties.setId(id);
        fileProperties.setLastModifiedById(id);
        fileProperties.setLastModifiedByName(name);
        fileProperties.setLastModifiedDate(cal);
        fileProperties.setManageableState(managed ? ManageableState.released : ManageableState.unmanaged);
        fileProperties.setNamespacePrefix(name);
        FileProperties[] filePropertiesArray = new FileProperties[] { fileProperties };
        FileMetadataExt fileMetadataHandler = new FileMetadataExt(filePropertiesArray);
        Component component = componentFactory.createComponent(projectPackage, filePath, file, fileMetadataHandler);
        assertNotNull("Component for filePath '" + filePath + "' cannot be null", component);
        assertTrue(
            "Expected '" + fileProperties.getCreatedById() + "' created by id, got '" + component.getCreatedById()
            + "'", fileProperties.getCreatedById().equals(component.getCreatedById()));
        assertTrue(
            "Expected '" + fileProperties.getCreatedByName() + "' created by name, got '"
                    + component.getCreatedByName() + "'",
                    fileProperties.getCreatedByName().equals(component.getCreatedByName()));
        assertTrue("Expected '" + fileProperties.getCreatedDate() + "' create date, got '" + component.getCreatedDate()
            + "'", fileProperties.getCreatedDate().equals(component.getCreatedDate()));
        assertTrue("Expected '" + fileProperties.getFileName() + "' file name, got '" + component.getMetadataFilePath()
            + "'", fileProperties.getFileName().equals(component.getMetadataFilePath()));
        assertTrue("Expected '" + fileProperties.getId() + "' id, got '" + component.getId() + "'", fileProperties
            .getId().equals(component.getId()));
        assertTrue(
            "Expected '" + fileProperties.getLastModifiedById() + "' object type, got '"
                    + component.getLastModifiedById() + "'",
                    fileProperties.getLastModifiedById().equals(component.getLastModifiedById()));
        assertTrue(
            "Expected '" + fileProperties.getLastModifiedByName() + "' last mod by name, got '"
                    + component.getLastModifiedByName() + "'",
                    fileProperties.getLastModifiedByName().equals(component.getLastModifiedByName()));
        assertTrue(
            "Expected '" + fileProperties.getLastModifiedDate() + "' last mod by date, got '"
                    + component.getLastModifiedDate() + "'",
                    fileProperties.getLastModifiedDate().equals(component.getLastModifiedDate()));
        assertTrue(
            "Expected '" + fileProperties.getNamespacePrefix() + "' namespace prefix, got '"
                    + component.getNamespacePrefix() + "'",
                    fileProperties.getNamespacePrefix().equals(component.getNamespacePrefix()));
        if (component.isMetadataComposite()) {
            assertTrue("Expected '" + filePath + componentFactory.getMetadataFileExtension()
                + "' metadata filepath, got '" + component.getCompositeMetadataFilePath() + "'",
                (filePath + componentFactory.getMetadataFileExtension()).equals(component
                    .getCompositeMetadataFilePath()));
        }
        return component;
    }

    public void testComponentFactory_getMetadataExtFromBody() throws Exception {
        Component component = loadAndCheckCustomObject();
        CustomObject customObject = (CustomObject) component.getMetadataExtFromBody();
        assertNotNull("Custom Object object should not be null", customObject);
        assertTrue("Custom Object object record types should not be null or empty",
            Utils.isNotEmpty(customObject.getRecordTypes()));
        assertTrue("Custom Object object fields should not be null or empty",
            Utils.isNotEmpty(customObject.getNameField()));
    }

    @SuppressWarnings("unchecked")
    public void testComponentFactory_getSubMetadataExtFromBody() throws Exception {
        Component component = loadAndCheckCustomObject();
        Class<? extends MetadataExt> recordTypeClass = Utils.getMetadataClassForComponentType(Constants.RECORD_TYPE);
        assertNotNull(recordTypeClass);
        Object result = component.getSubMetadataExtFromBody(recordTypeClass);
        assertTrue(result != null && result instanceof List && Utils.isNotEmpty((List<? extends MetadataExt>) result));
    }

    private Component loadAndCheckCustomObject() throws FactoryException, ForceProjectException, IOException,
    CoreException {
        Component component = getComponentByComponentType(Constants.CUSTOM_OBJECT);
        assertNotNull("Component should not be null", component);
        String customObjectFilePath =
                IdeTestConstants.FILEMETADATA_ROOT + "complete/objects/SampleCustomObject__c.object";
        URL url = IdeTestUtil.getFullUrlEntry(customObjectFilePath);
        assertNotNull("URL '" + customObjectFilePath + "' should not be null", url);
        File customObjectFile = new File(url.getFile());
        assertTrue("Custom Object file should not be null and exist",
            customObjectFile != null && customObjectFile.exists());
        String customObjectFileXmlStr = getContentString(customObjectFile);
        assertTrue("Custom Object XML should not be null and exist", Utils.isNotEmpty(customObjectFileXmlStr));
        component.setBody(customObjectFileXmlStr);
        return component;
    }

    @SuppressWarnings("unchecked")
    public void testComponentFactory_getSubMetadataExtFromBody_CustomField() throws Exception {
        Component component = loadAndCheckCustomObject();
        Object result = component.getSubMetadataExtFromBody("Field");
        assertTrue(result != null && result instanceof List && Utils.isNotEmpty((List<? extends MetadataExt>) result));

    }

    private Set<String> getCustomObjectSubtypes() throws FactoryException, ForceProjectException {
        final Component customObject = componentFactory.getComponentByComponentType(Constants.CUSTOM_OBJECT);
        Set<String> subtypes = new HashSet<String>(customObject.getSubComponentTypes());
        return subtypes;
    }

    private Set<String> getWorkflowSubtypes() throws FactoryException, ForceProjectException {
        final Component customObject = componentFactory.getComponentByComponentType(Constants.WORKFLOW);
        Set<String> subtypes = new HashSet<String>(customObject.getSubComponentTypes());
        return subtypes;
    }

    protected String getContentString(File file) throws IOException, CoreException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Invalid file passed in");

        }
        StringBuffer strBuff = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            if (line != null) {
                strBuff.append(line);
            }
            while ((line = reader.readLine()) != null) {
                strBuff.append(Constants.NEW_LINE);
                strBuff.append(line);
            }
        } finally {
            reader.close();
        }

        return new String(strBuff.toString().getBytes(), Constants.UTF_8);

    }
}
