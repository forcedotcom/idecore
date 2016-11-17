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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;

import com.google.common.collect.Lists;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.ZipUtils;
import com.salesforce.ide.test.common.NoOrgSetupTest;

@SuppressWarnings("deprecation")
public class ProjectPackageListTest_unit extends NoOrgSetupTest {
    private static final Logger logger = Logger.getLogger(ProjectPackageListTest_unit.class);
    
    public void testProjectPackageList_parseUnpackagedZip() {
        logStart("testProjectPackageList_parseUnpackagedZip");
        try {
            ProjectPackageList projectPackageList = getLoadedUnpackagedProjectPackageList();
            byte[] zipFile = getUnpackagedZipFileAsBytes();
            
            // inspect packages
            for (ProjectPackage projectPackage : projectPackageList) {
                String packagedName = projectPackage.getName();
                assertTrue("Project package name should not be null or empty", Utils.isNotEmpty(packagedName));
                assertTrue(
                    "Project package name should be one of two expected names",
                    packagedName.equals(packageNames[0]) || packagedName.equals(packageNames[1]));
                    
                assertTrue(
                    "Project package mapping count should " + expectedUnpackagedZipCount + " not "
                        + projectPackage.getFilePathZipMappingCount(),
                    Utils.isNotEmpty(projectPackage.getFilePathZipMappingCount())
                        && projectPackage.getFilePathZipMappingCount() == expectedUnpackagedZipCount);
            }
            
            // look for specific file and test size
            Component expectedComponent = getRandomComponent(projectPackageList);
            logger.info("Randomly picked component from UnpackagedZipFile: " + expectedComponent);
            assertNotNull("Random component should not be null", expectedComponent);
            assertNotNull(
                "Random component filepath should not be null",
                Utils.isNotEmpty(expectedComponent.getMetadataFilePath()));
            byte[] randomFileBytes =
                projectPackageList.getFileBytesForFilePath(expectedComponent.getMetadataFilePath());
            assertTrue(
                "Bytes for '" + expectedComponent.getMetadataFilePath() + "' should not be null",
                Utils.isNotEmpty(randomFileBytes));
            assertTrue(
                "Bytes for '" + expectedComponent.getMetadataFilePath() + "' should be > 0 not "
                    + randomFileBytes.length,
                randomFileBytes.length > 0);
                
            assertNotNull(
                "Component for filename should not be null",
                projectPackageList.getComponentByFileName(expectedComponent.getFileName()));
            assertNotNull(
                "Component for name '" + expectedComponent.getName() + "' and type '"
                    + expectedComponent.getComponentType() + "' should not be null",
                projectPackageList
                    .getComponentByNameType(expectedComponent.getName(), expectedComponent.getComponentType()));
                    
            // test for random file
            List<String> filepaths = ZipUtils.getFilePaths(zipFile);
            if (Utils.isNotEmpty(filepaths)) {
                for (int i = 0; i < 3; i++) {
                    String tmpFilepath = filepaths.get(randomInt(0, filepaths.size() - 1));
                    byte[] tmpFileBytes = projectPackageList.getFileBytesForFilePath(tmpFilepath);
                    assertTrue("Bytes for '" + tmpFilepath + "' should not be null", Utils.isNotEmpty(tmpFileBytes));
                }
            } else {
                assertTrue("Filepaths should not be null or empty", false);
            }
            
            ComponentList componentList = projectPackageList.getAllComponents();
            assertTrue("All components should have been removed", projectPackageList.removeAllComponents());
            assertTrue("All components should have been removed", Utils.isEmpty(projectPackageList.getAllComponents()));
            projectPackageList.addAllComponents(componentList);
            assertTrue("All components should have been add", Utils.isNotEmpty(projectPackageList.getAllComponents()));
            
            assertTrue(
                "Should not be any referenced packages",
                Utils.isEmpty(projectPackageList.getReferencedPackages()));
                
            // addDeleteComponent will remove component param from ComponentList in ProjectPackage of ProjectPackageList
            projectPackageList.addDeleteComponent(expectedComponent);
            ProjectPackage projectPackage = projectPackageList.getProjectPackageForComponent(expectedComponent);
            assertNotNull("ProjectPackage for component should not be null", projectPackage);
            String deleteXml = projectPackage.getDeletePackageManifest().getXMLString();
            assertTrue("Delete manifest should not be null", Utils.isNotEmpty(deleteXml));
            assertTrue(
                "Delete manifest should contain given component name",
                deleteXml.contains(expectedComponent.getName()));
            assertNull(
                "Component should have been already been removed",
                projectPackageList.getComponentByFileName(expectedComponent.getFileName()));
                
        } catch (Exception e) {
            handleFailure("Unable to generate file mappings", e);
        } finally {
            logEnd("testProjectPackageList_parseUnpackagedZip");
        }
    }
    
    public void testProjectPackageList_getComponentsById() {
        logStart("testProjectPackageList_getComponentsById");
        try {
            ProjectPackageList projectPackageList = getLoadedUnpackagedProjectPackageList();
            Component expectedComponent = getRandomComponent(projectPackageList);
            
            List<Component> matchedComponents = projectPackageList.getComponentsById(expectedComponent.getId());
            int originalSize = matchedComponents.size();
            assertTrue(
                "Unable to find expectedComponent in ProjectPackageList with id =" + expectedComponent.getId(),
                originalSize > 0);
                
            projectPackageList.removeComponent(expectedComponent);
            assertNull(
                "Component should have been already been removed",
                projectPackageList.getComponentByFileName(expectedComponent.getFileName()));
                
            matchedComponents = projectPackageList.getComponentsById(expectedComponent.getId());
            assertTrue(
                "Component should have been already been removed",
                matchedComponents.size() == (originalSize - 1));
                
        } catch (Exception e) {
            handleFailure("Unable to complete testing on testProjectPackageList_getComponentsById", e);
        } finally {
            logEnd("testProjectPackageList_getComponentsById");
        }
    }
    
    public void testProjectPackageList_getComponentResourcesForComponentTypes() {
        logStart("testProjectPackageList_getComponentResourcesForComponentTypes");
        try {
            ProjectPackageList projectPackageList = getLoadedUnpackagedProjectPackageList();
            
            List<IResource> matchedComponentResource = projectPackageList
                .getComponentResourcesForComponentTypes(new String[] { Constants.APEX_CLASS, Constants.APEX_TRIGGER });
            assertTrue(
                "Expected 4 " + Constants.APEX_CLASS + " resources, and 1 " + Constants.APEX_TRIGGER
                    + " resources. Total 8 resources should be returned not " + matchedComponentResource.size(),
                matchedComponentResource.size() == 5);
                
        } catch (Exception e) {
            handleFailure(
                "Unable to complete testing on testProjectPackageList_getComponentResourcesForComponentTypes",
                e);
        } finally {
            logEnd("testProjectPackageList_getComponentResourcesForComponentTypes");
        }
    }
    
    public void testProjectPackageList_getComponentByMetadataFilePath() {
        logStart("testProjectPackageList_getComponentByMetadataFilePath");
        try {
            ProjectPackageList projectPackageList = getLoadedUnpackagedProjectPackageList();
            
            byte[] zip = getUnpackagedZipFileAsBytes();
            Component expectedComponent = getRandomComponent(zip);
            assertNotNull("Random component should not be null", expectedComponent);
            assertNotNull(
                "Random component filepath should not be null",
                Utils.isNotEmpty(expectedComponent.getMetadataFilePath()));
            Component component =
                projectPackageList.getComponentByMetadataFilePath(expectedComponent.getMetadataFilePath());
            assertNotNull(
                "Component should not be null for filepath '" + expectedComponent.getMetadataFilePath() + "'",
                component);
            assertTrue(
                "Component metadata patch should be '" + expectedComponent.getMetadataFilePath() + "'",
                Utils.isNotEmpty(component.getMetadataFilePath())
                    && component.getMetadataFilePath().equals(component.getMetadataFilePath()));
            String id = "12345" + Utils.getNameFromFilePath(expectedComponent.getMetadataFilePath());
            assertTrue(
                "Component id should be '" + id + "', not '" + component.getId() + "'",
                Utils.isNotEmpty(component.getId()) && id.equals(component.getId()));
            assertTrue("Component should be unmanaged", !component.isInstalled());
            
            assertTrue("Component should exist in project package list", projectPackageList.hasComponent(component));
            getPackageManifestFactory().attachDeleteManifests(projectPackageList);
            projectPackageList.removeComponentByFilePath(component.getMetadataFilePath(), true, true);
        } catch (Exception e) {
            handleFailure("Unable to testProjectPackageList_getComponentByMetadataFilePath", e);
        } finally {
            logEnd("testProjectPackageList_getComponentByMetadataFilePath");
        }
    }
    
    public void testIsDesiredComponentType_whenPackageManifest() throws Exception {
        ProjectPackageList projectPackageList = getLoadedUnpackagedProjectPackageList();
        Component mComponent = mock(Component.class);
        when(mComponent.isPackageManifest()).thenReturn(true);
        
        assertTrue(projectPackageList.isDesiredComponentType(Lists.newArrayList(), mComponent));
    }

    public void testIsDesiredComponentType_whenDesignatedSaveComponentsEmptyOrNull() throws Exception {
        ProjectPackageList projectPackageList = getLoadedUnpackagedProjectPackageList();
        Component mComponent = mock(Component.class);
        
        assertTrue(projectPackageList.isDesiredComponentType(Lists.newArrayList(), mComponent));
    }

    public void testIsDesiredComponentType_whenDesignatedComponentContainsComponent() throws Exception {
        ProjectPackageList projectPackageList = getLoadedUnpackagedProjectPackageList();
        Component mComponent = mock(Component.class);
        when(mComponent.getComponentType()).thenReturn(Constants.APEX_CLASS);
        
        assertTrue(projectPackageList.isDesiredComponentType(Lists.newArrayList(Constants.APEX_CLASS), mComponent));
    }

    public void testIsDesiredComponentType_whenSecondaryComponentContainsComponent() throws Exception {
        ProjectPackageList projectPackageList = getLoadedUnpackagedProjectPackageList();
        Component mComponent = mock(Component.class);
        when(mComponent.getComponentType()).thenReturn(Constants.STANDARD_OBJECT);
        when(mComponent.getSecondaryComponentType()).thenReturn(Constants.CUSTOM_OBJECT);
        
        assertTrue(projectPackageList.isDesiredComponentType(Lists.newArrayList(Constants.CUSTOM_OBJECT), mComponent));
    }
}
