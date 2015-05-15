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

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.test.common.utils.IdeTestException;
import com.salesforce.ide.test.common.utils.IdeTestOrgFactory;
import com.salesforce.ide.test.common.utils.IdeTestUtil;
import com.salesforce.ide.test.common.utils.OrgTypeEnum;
import com.salesforce.ide.test.common.utils.PackageTypeEnum;

/**
 * Command to setup org with data but no project.Since you can't add data to an org without creating it, this special
 * command has been written to do both.
 * 
 * @author ssasalatti
 */
public class IdeTestSetupWithOrgWithMetaDataNoProjectCommand implements IdeTestCommand {
    private static final Logger logger = Logger.getLogger(IdeTestSetupWithOrgWithMetaDataNoProjectCommand.class);

    IdeSetupTest testConfig;

    public IdeTestSetupWithOrgWithMetaDataNoProjectCommand(IdeSetupTest testConfig) {
        this.testConfig = testConfig;
    }

    /*
     * Sometime during the execution of the tests (running in the UI thread), some task interrupted the UI thread while it was 
     * in a non-blocking operation. Thus, it never threw an InterruptedException but rather just captured the fact that it was interrupted
     * in the interrupted bit on the flag. 
     * 
     * For this particular IdeCommand that tries to zip/unzip files using Java NIO, this poses a problem. Java NIO is quite sensitive to interruptions 
     * and will cancel any operations and terminate it with a ClosedByInterruptedException.
     * 
     * Ideally, I want to find out who is interrupting the UI thread and see if we can act there. But right now, what I did is just remove the interrupted
     * status (using the badly named Thread.interrupted() which reads *and* clears the interrupt bit) before continuing.
     */
    
    public void executeSetup() throws IdeTestException {
        Thread.interrupted();
        logger.info("Setting up Metadata in org.....");

        //sanity check
        OrgTypeEnum orgType = testConfig.runForOrgType();
        PackageTypeEnum packageType = testConfig.addMetadataDataAsPackage();

        //can't add data as managed package if org type isn't namespaced
        if (packageType == PackageTypeEnum.MANAGED_DEV_PKG && !(orgType == OrgTypeEnum.Namespaced))
            throw IdeTestException
                    .getWrappedException("Cannot add Metadata as a managed package if the org being used isn't namespaced.Set runForOrgType in test annotation.");

        // setup the org.
        IdeTestOrgFactory.getTestSetupOrgCommand(testConfig).executeSetup();

        // add data---
        String addFromRelativePath = testConfig.addMetaDataFromPath();

        // check if that path is empty.
        if (IdeTestUtil.isEmpty(addFromRelativePath))
            throw IdeTestException
                    .getWrappedException("Invalid path specified when trying to add data from path. Path was: "
                            + addFromRelativePath);

        //setup should use data form filemetadata.

        addFromRelativePath = IdeTestUtil.convertToOSSpecificPath(addFromRelativePath);
        if (!addFromRelativePath.startsWith(File.separator + "filemetadata"))
            throw IdeTestException
                    .getWrappedException("Invalid path specified.Please add all test data to /filemetadata. Path was: "
                            + addFromRelativePath);

        // need to find the path of destructive changes.
        // setup will ensure that the path is within filemetadata.
        String removeRelativePath = IdeTestUtil.constructDestructiveChangesPath(addFromRelativePath);
        if (IdeTestUtil.isEmpty(removeRelativePath))
            throw IdeTestException
                    .getWrappedException("Invalid delete path was constructed when trying to add data from path. Path was: "
                            + removeRelativePath);

        //now really add the data.
        IdeTestOrgFactory.getOrgFixture().addMetaDataToOrg(
            IdeTestOrgFactory.getOrgFixture().getOrg(orgType), addFromRelativePath, packageType,
            removeRelativePath);
        logger.info("Setting up Metadata in org.....DONE");
    }

    public void executeTearDown() throws IdeTestException {
        Thread.interrupted();
        logger.info("Tearig down Metadata in org....");
        String addedFromRelPath = testConfig.addMetaDataFromPath();
        // check if that path is empty.
        if (Utils.isEmpty(addedFromRelPath))
            throw IdeTestException
                    .getWrappedException("Invalid path specified when trying to delete data from path. Path was: "
                            + testConfig.addMetaDataFromPath());

        PackageTypeEnum packageType = testConfig.addMetadataDataAsPackage();
        OrgTypeEnum orgType = testConfig.runForOrgType();

        if (!testConfig.skipMetadataRemovalDuringTearDown()) {
            logger.info("Removing data from org.");
            // remove data from the org
            addedFromRelPath = IdeTestUtil.convertToOSSpecificPath(addedFromRelPath);
            IdeTestOrgFactory.getOrgFixture().removeMetaDataFromOrg(
                IdeTestOrgFactory.getOrgFixture().getOrgCacheInstance().getOrgInfoFromCache(orgType),
                packageType, addedFromRelPath);
        }
        logger.info("Tearing down Metadata in org....DONE");

        IdeTestOrgFactory.getTestSetupOrgCommand(testConfig).executeTearDown();
    }

}
