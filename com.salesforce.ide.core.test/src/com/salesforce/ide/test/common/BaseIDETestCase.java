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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.factories.FactoryLocator;
import com.salesforce.ide.core.factories.MetadataFactory;
import com.salesforce.ide.core.factories.PackageManifestFactory;
import com.salesforce.ide.core.factories.ProjectPackageFactory;
import com.salesforce.ide.core.factories.ToolingFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.ZipUtils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.SalesforceEndpoints;
import com.salesforce.ide.core.remote.metadata.DeployMessageExt;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.core.remote.metadata.RetrieveMessageExt;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.services.ApexService;
import com.salesforce.ide.core.services.MetadataService;
import com.salesforce.ide.core.services.PackageDeployService;
import com.salesforce.ide.core.services.PackageRetrieveService;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.core.services.ServiceLocator;
import com.salesforce.ide.test.common.utils.ConfigProps;
import com.salesforce.ide.test.common.utils.IdeTestUtil;
import com.salesforce.ide.test.common.utils.OrgTestingUtil;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ManageableState;
import com.sforce.soap.metadata.RetrieveMessage;

/**
 *
 *
 * @author cwall
 * @deprecated
 */
@Deprecated
public abstract class BaseIDETestCase extends BaseTestCase {

    private static final Logger logger = Logger.getLogger(BaseIDETestCase.class);

    protected static final String UNPACKAGE_DIR = "unpackaged-only.dir";
    protected static final String TOOLKIT_PACKAGE_DIR = "toolkitPackageTest-only.dir";
    protected static final String FILEMETADATA_COMPLETE = "complete";
    protected static final String FILEMETADATA_SIMPLE = "simple";

    private static ConfigProps _props = ConfigProps.getInstance();

    protected String[] componentTypesNonInternalNonFolder =
            { Constants.APEX_CLASS, Constants.APEX_COMPONENT, Constants.APEX_TRIGGER, Constants.APEX_PAGE,
                    Constants.CUSTOM_APPLICATION, Constants.CUSTOM_OBJECT, Constants.CUSTOM_TAB, Constants.DOCUMENT,
                    Constants.EMAIL_TEMPLATE, Constants.LAYOUT, Constants.LETTERHEAD, Constants.HOME_PAGE_COMPONENT,
                    Constants.HOME_PAGE_LAYOUT, Constants.PROFILE, Constants.SCONTROL, Constants.STATIC_RESOURCE,
                    Constants.WORKFLOW };

    protected String[] packageNames = new String[] { "unpackaged", "toolkitPackageTest" };
    protected int expectedUnpackagedZipCount = 47;

    protected ProjectPackageList unpackagedProjectPackageList = null;
    protected byte[] unpackagedZipFile = null;

    @Override
    public Object getBean(String id) throws ForceProjectException {
        return ContainerDelegate.getInstance().getBean(id);
    }

    public ServiceLocator getServiceLocator() throws ForceProjectException {
        return (ServiceLocator) getBean("serviceLocator");
    }

    public FactoryLocator getFactoryLocator() throws ForceProjectException {
        return (FactoryLocator) getBean("factoryLocator");
    }

    public ProjectService getProjectService() throws ForceProjectException {
        return getServiceLocator().getProjectService();
    }

    public PackageRetrieveService getPackageRetrieveService() throws ForceProjectException {
        return getServiceLocator().getPackageRetrieveService();
    }

    public PackageDeployService getPackageDeployService() throws ForceProjectException {
        return getServiceLocator().getPackageDeployService();
    }

    public MetadataService getMetadataService() throws ForceProjectException {
        return getServiceLocator().getMetadataService();
    }

    public ApexService getApexService() throws ForceProjectException {
        return getServiceLocator().getApexService();
    }

    public ComponentFactory getComponentFactory() throws ForceProjectException {
        return getFactoryLocator().getComponentFactory();
    }

    public ConnectionFactory getConnectionFactory() throws ForceProjectException {
        return getFactoryLocator().getConnectionFactory();
    }

    public MetadataFactory getMetadataFactory() throws ForceProjectException {
        return getFactoryLocator().getMetadataFactory();
    }
    
    public ToolingFactory getToolingFactory() throws ForceProjectException {
        return getFactoryLocator().getToolingFactory();
    }

    public PackageManifestFactory getPackageManifestFactory() throws ForceProjectException {
        return getFactoryLocator().getPackageManifestFactory();
    }

    public ProjectPackageFactory getProjectPackageFactory() throws ForceProjectException {
        return getFactoryLocator().getProjectPackageFactory();
    }

    public SalesforceEndpoints getSalesforceEndpoints() throws ForceProjectException {
        return (SalesforceEndpoints) getBean("salesforceEndpoints");
    }

    public ProjectPackageList getProjectPackageListInstance() throws ForceProjectException {
        return getProjectPackageFactory().getProjectPackageListInstance();
    }

    protected Connection getDefaultProdConnection() throws ForceConnectionException, ForceProjectException,
            InsufficientPermissionsException {
        return getConnectionFactory().getConnection(getDefaultProdForceProjectInstance());
    }

    protected Connection getDefaultDevConnection() throws ForceConnectionException, ForceProjectException,
            InsufficientPermissionsException {
        return getConnectionFactory().getConnection(getDefaultDevForceProjectInstance());
    }

    protected Connection getConnection(OrgTestingUtil orgTestingUtil) throws ForceConnectionException,
            ForceProjectException, InsufficientPermissionsException {
        return getConnectionFactory().getConnection(getForceProjectInstance(orgTestingUtil));
    }

    protected ForceProject getDefaultProdForceProjectInstance() throws ForceProjectException {
        ForceProject forceProject = getDefaultForceProjectInstance();
        forceProject.setUserName(_props.getProperty("default.prod.user"));
        forceProject.setPassword(_props.getProperty("default.prod.user.password"));
        logger.info("Create force project instance " + forceProject.getLogDisplay());
        return forceProject;
    }

    protected ForceProject getDefaultDevForceProjectInstance() throws ForceProjectException {
        ForceProject forceProject = getDefaultForceProjectInstance();
        forceProject.setUserName(_props.getProperty("default.de.user"));
        logger.info("Create force project instance " + forceProject.getLogDisplay());
        return forceProject;
    }

    protected ForceProject getForceProjectInstance(OrgTestingUtil orgTestingUtil) throws ForceProjectException {
        ForceProject forceProject = new ForceProject();
        forceProject.setUserName(orgTestingUtil.getOrgInfo().getUsername());
        return forceProject;
    }

    private ForceProject getDefaultForceProjectInstance() throws ForceProjectException {
        ForceProject forceProject = new ForceProject();
        forceProject.setEndpointServer(IdeTestUtil.getAppServerToHitForTests(false));
        forceProject.setPassword(_props.getProperty("default.password"));
        if (Utils.isNotEmpty(_props.getProperty("default.junit.use-https"))) {
            forceProject.setHttpsProtocol(Boolean.valueOf(_props.getProperty("default.junit.use-https")));
        }
        return forceProject;
    }

    private String getUnpackagedDir() {
        String filepath = null;
        try {
            filepath = _props.getProperty(UNPACKAGE_DIR, true);
            logger.info("Got file path '" + filepath + "'");
        } catch (Exception e) {
            logger.error("Unable to load " + UNPACKAGE_DIR, e);
        }
        return filepath;
    }

    public byte[] getUnpackagedZipFile() throws IOException {
        if (Utils.isEmpty(unpackagedZipFile)) {
            unpackagedZipFile = getZipFileAsBytes(getUnpackagedDir());
        }
        return unpackagedZipFile;
    }

    public byte[] getUnpackagedZipFileAsBytes() throws Exception {
        return getUnpackagedZipFile();
    }

    public byte[] getZipFileAsBytes(String folderFilePath) throws IOException {
        if (Utils.isEmpty(folderFilePath)) {
            throw new IllegalArgumentException("Filepatch cannot be null");
        }
        logger.info("Getting file for package folder path '" + folderFilePath + "'");
        File folderRoot = new File(folderFilePath);
        assertTrue("Package folder does not exist", folderRoot.exists());
        File[] componentFolders = folderRoot.listFiles();
        if (Utils.isEmpty(componentFolders)) {
            logger.warn("No package folders found for folder root '" + folderFilePath + "'");
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        for (File componentFolder : componentFolders) {
            logger.info("Loading component folder '" + componentFolder.getAbsolutePath() + "'");
            ZipUtils.zipFile(componentFolder.getName(), componentFolder, zos);
        }

        zos.close();
        byte[] zipBytes = bos.toByteArray();
        logger.info("Got zip of size " + zipBytes.length + " for filepath '" + folderFilePath + "'");

        logFilePaths(zipBytes);

        return bos.toByteArray();
    }

    private void logFilePaths(byte[] zipFile) throws IOException {
        if (Utils.isEmpty(zipFile)) {
            logger.warn("Zip is null or empty");
            return;
        }

        List<String> filepaths = ZipUtils.getFilePaths(zipFile);
        if (Utils.isEmpty(filepaths)) {
            logger.warn("No filepaths found for zip");
            return;
        }

        Collections.sort(filepaths);
        int cnt = 0;
        StringBuffer strBuff = new StringBuffer("Got ").append(filepaths.size()).append(" filepaths from zip:");
        for (String filepath : filepaths) {
            strBuff.append("\n ");
            strBuff.append(" (");
            strBuff.append(++cnt);
            strBuff.append(") ");
            strBuff.append(filepath);
        }
        logger.info(strBuff.toString());
    }

    public byte[] getBytesForFileName(String filename) throws Exception {
        if (Utils.isEmpty(filename)) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        byte[] fileContent = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(getUnpackagedZipFileAsBytes());
        ZipInputStream zis = new ZipInputStream(bis);
        try {
            for (;;) {
                ZipEntry ze = zis.getNextEntry();
                if (ze == null) {
                    break;
                }
                fileContent = StreamUtils.getBytes(zis);
                String name = ze.getName();
                if (ze.isDirectory()) {
                    continue;
                }

                if (filename.endsWith(name)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found '" + name + "' body size [" + fileContent.length + "]");
                    }
                    return fileContent;
                }
            }
        } finally {
            zis.close();
        }
        return fileContent;
    }

    protected FileMetadataExt getFileMetadataExtForUnpackagedZip() throws IOException, Exception {
        return getFileMetadataExtForZip(getUnpackagedZipFileAsBytes());
    }

    protected FileMetadataExt getFileMetadataExtForZip(byte[] zipFile) throws IOException, Exception {
        if (Utils.isEmpty(zipFile)) {
            throw new IllegalArgumentException("File zip cannot be null");
        }

        List<FileProperties> filePropertiesList = new ArrayList<FileProperties>();
        List<String> filepaths = ZipUtils.getFilePaths(zipFile);

        if (Utils.isEmpty(filepaths)) {
            logger.warn("No filepaths found for zip");
            return null;
        }

        Collections.sort(filepaths);
        String id = "12345";
        Calendar cal = Calendar.getInstance();
        for (String filepath : filepaths) {
            FileProperties fileProperties = new FileProperties();
            String name = Utils.getNameFromFilePath(filepath);
            fileProperties.setCreatedById(id);
            fileProperties.setCreatedByName(name);
            fileProperties.setCreatedDate(cal);
            fileProperties.setFileName(filepath);
            fileProperties.setId(id + name);
            fileProperties.setLastModifiedById(id);
            fileProperties.setLastModifiedByName("cwall");
            fileProperties.setLastModifiedDate(cal);
            fileProperties.setManageableState(ManageableState.unmanaged);
            fileProperties.setNamespacePrefix("");
            filePropertiesList.add(fileProperties);
            logger.info("Added file properties for '" + filepath + "'");
        }

        FileMetadataExt fileMetadataExt =
                new FileMetadataExt(filePropertiesList.toArray(new FileProperties[filePropertiesList.size()]));
        return fileMetadataExt;
    }

    protected int randomInt(int lower, int upper) {
        return (new Random()).nextInt((upper - lower + 1)) + lower;
    }

    protected String getRandomFilePath(byte[] zipFile) throws IOException {
        if (Utils.isEmpty(zipFile)) {
            throw new IllegalArgumentException("File zip cannot be null");
        }

        List<String> filepaths = ZipUtils.getFilePaths(zipFile);

        if (Utils.isEmpty(filepaths)) {
            logger.warn("No filepaths found for zip");
            return null;
        }

        return filepaths.get(randomInt(0, filepaths.size() - 1));
    }

    protected Component getRandomComponent(ProjectPackageList projectPackageList) {
        if (Utils.isEmpty(projectPackageList)) {
            throw new IllegalArgumentException("File zip cannot be null");
        }
        ComponentList allComponents = projectPackageList.getAllComponents(false);

        Component component = null;
        for (;;) {
            int randomInt = randomInt(0, allComponents.size() - 1);
            component = allComponents.get(randomInt);
            if (!component.isInternal()) {
                break;
            }
        }

        return component;
    }

    protected Component getRandomComponent(byte[] zipFile) throws IOException, FactoryException, ForceProjectException {
        if (Utils.isEmpty(zipFile)) {
            throw new IllegalArgumentException("File zip cannot be null");
        }

        List<String> filepaths = ZipUtils.getFilePaths(zipFile);

        if (Utils.isEmpty(filepaths)) {
            logger.warn("No filepaths found for zip");
            return null;
        }

        Component component = null;
        for (;;) {
            int randInt = randomInt(0, (filepaths.size() - 1));
            logger.info("Getting random component at index " + randInt);
            String filepath = filepaths.get(randInt);
            component = getComponentFactory().getComponentByFilePath(filepath);
            if (!component.isInternal()) {
                break;
            }
        }

        return component;
    }

    protected ProjectPackageList getLoadedUnpackagedProjectPackageList() throws Exception {
        if (unpackagedProjectPackageList == null) {
            byte[] unpackagedZipFile = getUnpackagedZipFileAsBytes();
            assertTrue("Unpackaged zip file should not be null", Utils.isNotEmpty(unpackagedZipFile));

            unpackagedProjectPackageList = getProjectPackageListInstance();
            assertNotNull("Project package list should not be null", unpackagedProjectPackageList);

            unpackagedProjectPackageList.parseZip(unpackagedZipFile, new NullProgressMonitor());
            FileMetadataExt fileMetadataExt = getFileMetadataExtForUnpackagedZip();
            assertNotNull("File metadata should not be null", fileMetadataExt);
            assertTrue("File metadata count should " + expectedUnpackagedZipCount + " not "
                    + fileMetadataExt.getFilePropertiesCount(),
                fileMetadataExt.getFilePropertiesCount() == expectedUnpackagedZipCount);

            unpackagedProjectPackageList.generateComponents(unpackagedZipFile, fileMetadataExt);
            assertTrue("Project package list size should equal " + unpackagedZipFile.length,
                unpackagedProjectPackageList.size() == 1);
            assertTrue("Project package list component count should " + expectedUnpackagedZipCount + " not "
                    + unpackagedProjectPackageList.getAllComponents().size(), Utils
                    .isNotEmpty(unpackagedProjectPackageList.getAllComponents())
                    && unpackagedProjectPackageList.getAllComponents().size() == expectedUnpackagedZipCount);
        }

        return unpackagedProjectPackageList;
    }

    protected ProjectPackageList getLoadedProjectPackageList(byte[] zipFile) throws Exception {
        ProjectPackageList projectPackageList = getProjectPackageListInstance();
        assertNotNull("Project package list should not be null", projectPackageList);
        assertTrue("Zip file should not be null", Utils.isNotEmpty(zipFile));
        return projectPackageList;
    }

    protected void evaluateBasicDeployResultHandler(DeployResultExt deployResultHandler) {
        evaluateBasicDeployResultHandler(deployResultHandler, logger);
    }

    protected void evaluateBasicDeployResultHandler(DeployResultExt deployResultHandler, Logger logger) {
        assertNotNull("DeployResultHandler should not be null", deployResultHandler);
        DeployMessageExt messageHandler = deployResultHandler.getMessageHandler();
        if (messageHandler != null) {
            messageHandler.logMessage();
        }
    }

    protected void evaluateBasicRetrieveResultHandler(RetrieveResultExt retrieveResultHandler, Logger logger) {
        assertNotNull("RetrieveResultHandler should not be null", retrieveResultHandler);
        if (retrieveResultHandler.getMessageCount() > 0) {
            StringBuffer strBuff = new StringBuffer();
            RetrieveMessageExt messageHandler = retrieveResultHandler.getMessageHandler();
            if (messageHandler != null) {
                RetrieveMessage[] retrieveMessages = messageHandler.getMessages();
                strBuff.append("\nGot the following deployment messages:");
                int msgCnt = 0;
                for (RetrieveMessage retrieveMessage : retrieveMessages) {
                    strBuff.append("\n (").append(++msgCnt).append(") ").append("fullname = ").append(
                        retrieveMessage.getFileName()).append(", problem = ").append(retrieveMessage.getProblem());
                }
            }
            logger.info(strBuff.toString());
        }
    }

    protected IProgressMonitor getMonitor() {
        return new NullProgressMonitor();
    }


}
