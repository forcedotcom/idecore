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
package com.salesforce.ide.core.project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.test.common.NoOrgSetupTest;
import com.salesforce.ide.test.common.utils.IdeTestConstants;
import com.salesforce.ide.test.common.utils.IdeTestUtil;
import com.sforce.soap.metadata.FileProperties;

@SuppressWarnings("deprecation")
public class ProjectControllerTest_unit extends NoOrgSetupTest {

    private static final Logger logger = Logger.getLogger(ProjectControllerTest_unit.class);

    public void testProjectController_generateSummaryText_PackageManifestModel() throws Exception {

        InputStream in = null;
        try {
            ProjectController projectController = new ProjectController();
            assertNotNull("ProjectController should not be null", projectController);

            String packageManifestFilePath =
                    IdeTestConstants.FILEMETADATA_ROOT + IdeTestConstants.FILEMETADATA_MANIFESTS
                            + "/complete-package.xml";
            URL url = IdeTestUtil.getFullUrlEntry(packageManifestFilePath);
            assertNotNull("URL for '" + packageManifestFilePath + "' should not be null", url);

            File packageManifestFile = new File(url.getFile());
            assertTrue("Package Manifest file should not be null and exist", packageManifestFile != null
                    && packageManifestFile.exists());

            DOMParser parser = new DOMParser();
            in = url.openStream();
            InputSource source = new InputSource(in);
            parser.parse(source);
            Document document = parser.getDocument();
            assertNotNull("Document for '" + packageManifestFilePath + "' should not be null", document);
            PackageManifestModel packageManifestModel = new PackageManifestModel(document);

            Map<String, Boolean> nodeAllSelection = new HashMap<String, Boolean>(11);
            nodeAllSelection.put("CustomObjectForWorkflow__c", true);
            nodeAllSelection.put("Dependent__c", false);
            nodeAllSelection.put("LayoutTestChild__c", true);
            nodeAllSelection.put("LayoutTest__c", false);
            nodeAllSelection.put("listview__c", true);
            nodeAllSelection.put("myobj__c", false);
            nodeAllSelection.put("NoCrudCustomObject__c", true);
            nodeAllSelection.put("SampleCustomAnother__c", false);
            nodeAllSelection.put("SampleCustomObject__c", true);
            nodeAllSelection.put("SampleCustomForPage__c", false);
            nodeAllSelection.put("TranslatedCustomObject__c", true);
            packageManifestModel.setNodeAllSelectionMap(nodeAllSelection);

            List<String> summaries =
                    projectController.getProjectContentSummaryAssembler().generateSummaryText(packageManifestModel);
            assertTrue("Summary text should not be null or empty", Utils.isNotEmpty(summaries));
            StringBuffer summary = new StringBuffer();
            for (String tmpSummary : summaries) {
                summary.append(tmpSummary);
            }
            assertTrue("Summary text should not be null or empty", Utils.isNotEmpty(summary));
            logger.info("\n" + summary.toString());

            Package packageManifest = getPackageManifestFactory().createPackageManifest(document);
            assertNotNull("Package Manifest for Document should not be null", packageManifest);
            assertTrue("Package Manifest types should not be null or empty", Utils.isNotEmpty(packageManifest
                    .getTypes()));

            // TODO more in-depth investigation
            
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.warn("Unable to close input stream for package manifest file");
                }
            }
        }
    }



    public void testProjectController_generateSummaryText_FileProperties() throws Exception {
        FileProperties apexClassFileProperties = new FileProperties();
        apexClassFileProperties.setType(Constants.APEX_CLASS);
        apexClassFileProperties.setFullName("TestApexClass.cls");

        FileProperties apexClassMetadataFileProperties = new FileProperties();
        apexClassMetadataFileProperties.setType(Constants.APEX_CLASS);
        apexClassMetadataFileProperties.setFullName("TestApexClass");
        apexClassMetadataFileProperties.setFileName("classes/TestApexClass.cls-meta.xml");

        FileProperties apexPageFileProperties = new FileProperties();
        apexPageFileProperties.setType(Constants.APEX_PAGE);
        apexPageFileProperties.setFullName("TestApePage.page");

        FileProperties apexPageMetadataFileProperties = new FileProperties();
        apexPageMetadataFileProperties.setType(Constants.APEX_PAGE);
        apexPageMetadataFileProperties.setFullName("TestApePage");
        apexPageMetadataFileProperties.setFileName("pages/TestApePage.page-meta.xml");

        FileProperties workflowFileProperties = new FileProperties();
        workflowFileProperties.setType(Constants.WORKFLOW);
        workflowFileProperties.setFullName("TestWorkflow");

        FileMetadataExt fileMetadataExt =
                new FileMetadataExt(new FileProperties[] { apexClassFileProperties, apexClassMetadataFileProperties,
                        apexPageFileProperties, apexPageMetadataFileProperties, workflowFileProperties });
        assertTrue("FileMetadataExt should not be null and have 5 FileProperties", fileMetadataExt != null
                && fileMetadataExt.getFilePropertiesCount() == 5);

        ProjectController projectController = new ProjectController();
        assertNotNull("ProjectController should not be null", projectController);

        List<String> summaries =
                projectController.getProjectContentSummaryAssembler().generateSummaryText(fileMetadataExt,
                    new String[] { Constants.APEX_PAGE, Constants.APEX_CLASS }, false);
        assertTrue("Summary text should not be null or empty", Utils.isNotEmpty(summaries));
        StringBuffer summary = new StringBuffer();
        for (String tmpSummary : summaries) {
            summary.append(tmpSummary);
        }
        logger.info("\n" + summary.toString());

        assertTrue("Summary text should not be null or empty", Utils.isNotEmpty(summary));
        String summaryStr = summary.toString();
        assertTrue("Summary text should not have '" + Constants.WORKFLOW + "'", !summaryStr
                .contains(Constants.WORKFLOW));
        assertTrue("Summary text should not have 'Subscribe to new'", !summaryStr.contains("Subscribe to new"));

    }

}
