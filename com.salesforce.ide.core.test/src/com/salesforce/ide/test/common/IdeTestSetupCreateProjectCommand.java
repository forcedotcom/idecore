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
package com.salesforce.ide.test.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.ResourcesPlugin;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.test.common.utils.ComponentTypeEnum;
import com.salesforce.ide.test.common.utils.IdeOrgCache.OrgInfo;
import com.salesforce.ide.test.common.utils.IdeOrgCache.PackageInfo;
import com.salesforce.ide.test.common.utils.IdeProjectContentTypeEnum;
import com.salesforce.ide.test.common.utils.IdeProjectFixture;
import com.salesforce.ide.test.common.utils.IdeTestConstants;
import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestOrgFactory;
import com.salesforce.ide.test.common.utils.IdeTestUtil;
import com.salesforce.ide.test.common.utils.OrgTypeEnum;
import com.salesforce.ide.test.common.utils.PackageTypeEnum;

/**
 * Command to setup project only.
 * 
 * @author ssasalatti
 */
public class IdeTestSetupCreateProjectCommand extends IdeTestSetupBaseProjectCommand {

    private static final Logger logger = Logger.getLogger(IdeTestSetupCreateProjectCommand.class);

    public IdeTestSetupCreateProjectCommand(IdeSetupTest testConfig) {
        this.testConfig = testConfig;
    }

    @Override
    public void executeSetup() throws IdeTestException {
        logger.info("Setting up project before test...");

        if (!IdeProjectFixture.getInstance().checkIfProjectCacheClean()) {
            logger.error("Project Cache was unlean!!! purging all projects so that tests can continue.");
            IdeProjectFixture.getInstance().purgeAllProjectsFromWorkspace();
            // clear the cache.
            IdeProjectFixture.getInstance().flushProjCache();
            // throw IdeTestException.getWrappedException("Project Cache
            // supposed to be clean for a new test.Aborting test.");
        }

        OrgTypeEnum orgType = testConfig.runForOrgType();
        IdeProjectContentTypeEnum projectContentSelection = testConfig.setProjectContentConfig();
        ComponentTypeEnum[] componentTypesForProjectCreate = testConfig.setComponentListIfSelectiveProjectCreate();
        Package packageManifest = null;

        // if SINGLE_PACKAGE was selected for project content, then the test
        // better have needData() set.
        String pkgNameToCreateProjectAgainst = IdeTestConstants.DEFAULT_PACKAGED_NAME;

        // if content selection was not specific components ignore the
        // componentTypesForProjectCreate list.
        if (projectContentSelection != IdeProjectContentTypeEnum.SPECIFIC_COMPONENTS)
            componentTypesForProjectCreate = null;
        switch (projectContentSelection) {

        case SINGLE_PACKAGE:

            if (!(testConfig.needMoreMetadataDataInOrg() && testConfig.addMetadataDataAsPackage() != PackageTypeEnum.UNPACKAGED))
                throw IdeTestException
                        .getWrappedException("If you need the project pre-created against a specific package, then you need to add data to the org as a package. Set needDataInOrg, addDataAsPackage, and addDataFromPath in the test annotation.");
            OrgInfo orgInfo = IdeTestOrgFactory.getOrgFixture().getOrgCacheInstance().getOrgInfoFromCache(orgType);
            List<PackageInfo> pkgList = orgInfo.getPackageList();
            for (PackageInfo p : pkgList) {
                String path = testConfig.addMetaDataFromPath();
                path = IdeTestUtil.convertToOSSpecificPath(path);
                if (p.getPackageType().equals(testConfig.addMetadataDataAsPackage())
                        && p.getAddFromRelPath().equalsIgnoreCase(path))
                    pkgNameToCreateProjectAgainst = p.getPackageName();
                break;
            }
            if (IdeTestUtil.isEmpty(pkgNameToCreateProjectAgainst))
                throw IdeTestException.getWrappedException("Package Name should've been set");
            break;

        case ONLY_WHAT_IS_BEING_UPLOADED:

            // if content selection was only what was uploaded, create a list of
            // only those components and send that.

            // get the package.xml file location
            URL url =
                    IdeTestUtil.getFullUrlEntry(IdeTestUtil.convertToOSSpecificPath(testConfig.addMetaDataFromPath())
                            + File.separator + Constants.PACKAGE_MANIFEST_FILE_NAME);
            try {
                //TODO: extract into a reusable method.
                File packageManifestFile = new File(url.getFile());
                String tempDirPath =
                        ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + File.separator + "temp";
                File tempDir = new File(tempDirPath);
                IdeTestUtil.copyFilesToDirRecursively(tempDir, packageManifestFile);
                String tempPackageManifestFilePath =
                        tempDirPath + File.separator + Constants.PACKAGE_MANIFEST_FILE_NAME;
                File tempPackageManifestFile = new File(tempPackageManifestFilePath);
                // parse it for the components

                projectContentSelection = IdeProjectContentTypeEnum.ONLY_WHAT_IS_BEING_UPLOADED;
                try {
                    String packageFileContent = new String(Utils.getBytesFromFile(tempPackageManifestFile));

                    //if the requested package type is unpackaged, then get rid of <fullname>@@packageName@@<fullname>
                    //else replace @@packagaName@@ with it.
                    if (!pkgNameToCreateProjectAgainst.equalsIgnoreCase(IdeTestConstants.DEFAULT_PACKAGED_NAME)) {
                        //replace <fullName>tag
                        packageFileContent =
                                packageFileContent.replace(IdeTestConstants.PACKAGE_NAME_TOKEN,
                                    pkgNameToCreateProjectAgainst);

                    } else {
                        packageFileContent =
                                packageFileContent.replace(IdeTestConstants.PACKAGE_FULL_NAME_ELEMENT_WITH_TOKEN, "");
                    }
                    //write the new content back to the file.
                    FileOutputStream fos = new FileOutputStream(tempPackageManifestFile);
                    fos.write(packageFileContent.getBytes());
                    fos.close();
                    packageManifest = IdeTestUtil.getPackageManifestFactory().parsePackageManifest(tempPackageManifestFile);
                } catch (IOException e) {
                    IdeTestException.wrapAndThrowException("Could not manipulate the package manifest file.", e);
                } finally {
                    IdeTestUtil.deleteDirectoryRecursively(tempDir);
                }
                
            } catch (JAXBException e) {
                throw IdeTestException
                        .getWrappedException(
                            "Couldn't parse package.xml file for components while trying to create project from data that was uploaded.Bad file or file not found.",
                            e);
            } 

        default:
            break;
        }

        // set the builder to on/off based on setting.
        IdeProjectFixture.getInstance().switchAutoBuild(testConfig.autoBuildOn());

        IdeProjectFixture.getInstance().createProject(orgType, projectContentSelection, componentTypesForProjectCreate,
            pkgNameToCreateProjectAgainst, packageManifest);
        IdeProjectFixture.getInstance().refreshWorkspace();
        logger.info("Setting up project before test...DONE");
    }

}
