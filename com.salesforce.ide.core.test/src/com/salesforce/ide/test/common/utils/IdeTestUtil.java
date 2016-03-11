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
package com.salesforce.ide.test.common.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipOutputStream;

import javax.imageio.stream.FileImageInputStream;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.factories.FactoryLocator;
import com.salesforce.ide.core.factories.PackageManifestFactory;
import com.salesforce.ide.core.factories.ProjectPackageFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.templates.TemplateRegistry;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.ZipUtils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.metadata.DeployMessageExt;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.services.MetadataService;
import com.salesforce.ide.core.services.PackageDeployService;
import com.salesforce.ide.core.services.PackageRetrieveService;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceLocator;
import com.salesforce.ide.core.services.ToolingService;
import com.sforce.soap.metadata.CodeCoverageResult;
import com.sforce.soap.metadata.CodeLocation;
import com.sforce.soap.metadata.RunTestFailure;
import com.sforce.soap.metadata.RunTestSuccess;
import com.sforce.soap.metadata.RunTestsResult;

/**
 * Convenience class for IDE related utility methods. Merging all old util
 * classes.
 *
 * @author ssasalatti
 */
public class IdeTestUtil {

    public static abstract class MetadataZipper {
        public byte[] generateZipBytes(final String folderRelPath,
                final String packageName) throws IdeTestException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // resolve path to a folder
            URL url = getFullUrlEntry(folderRelPath);
            if (isEmpty(url))
                throw IdeTestException
                .getWrappedException("Couldn't generate absolute path from relative path:"
                        + folderRelPath + ". Can't deploy. ");

            File folderToDeploy = new File(url.getFile());
            // check folder
            if (isEmpty(folderToDeploy))
                throw IdeTestException
                .getWrappedException("Invalid folder to deploy.:"
                        + folderToDeploy);
            if (!(folderToDeploy.exists()))
                throw IdeTestException
                .getWrappedException("Folder "
                        + folderToDeploy.getName()
                        + " does not exist.  Check path in test.properties and filemetadata ");

            String workspacePath = ResourcesPlugin.getWorkspace().getRoot()
                    .getLocation().toString();
            // need a temp directory.
            File tempDir = new File(workspacePath + File.separator + "temp");

            try {
                // copy files to the temp dir
                copyFilesToDirRecursively(tempDir, folderToDeploy);
                File baseDir = new File(tempDir + File.separator
                    + folderToDeploy.getName());

                // if it's an unpacakged zip, then get rid of the entire pacakge
                // tag.
                if (isNotEmpty(packageName)
                        && packageName
                        .equals(IdeTestConstants.DEFAULT_PACKAGED_NAME)) {
                    IdeTestFileMetadataReplacementUtil removePackageTagIfUnpacakged = new IdeTestFileMetadataReplacementUtil();
                    removePackageTagIfUnpacakged
                    .add(
                        Constants.PACKAGE_MANIFEST,
                        IdeTestConstants.PACKAGE_FULL_NAME_ELEMENT_WITH_TOKEN,
                            "");
                    removePackageTagIfUnpacakged.checkAndReplace(baseDir);
                }
                // any other specific replacements.
                IdeTestFileMetadataReplacementUtil fileReplacementsUtil = getFileReplacementsUtil();
                if (isNotEmpty(fileReplacementsUtil)) {
                    fileReplacementsUtil.checkAndReplace(baseDir);
                }
                File[] files = baseDir.listFiles();

                List<File> filesToAdd = new ArrayList<File>();
                for (File file : files) {
                    filesToAdd.add(file);
                }

                // zip up the bytes.
                ZipOutputStream zos = new ZipOutputStream(bos);
                ZipUtils.zipFiles("", filesToAdd.toArray(new File[filesToAdd
                                                                  .size()]), zos);
                zos.close();
            } catch (IOException e) {
                IdeTestException
                .wrapAndThrowException("IO Exception while trying to zip bytes.", e);
            } finally {
                // delete the temp directory that was created.
                deleteDirectoryRecursively(tempDir);
            }
            return bos.toByteArray();
        }

        protected abstract IdeTestFileMetadataReplacementUtil getFileReplacementsUtil();

    }

    private static Logger logger = Logger.getLogger(IdeTestUtil.class);
    protected static ServiceLocator serviceLocator = null;
    protected static FactoryLocator factoryLocator = null;
    protected static TemplateRegistry templateRegistry = null;

    static {
        try {
            serviceLocator = (ServiceLocator) getBean("serviceLocator");
            factoryLocator = (FactoryLocator) getBean("factoryLocator");
            templateRegistry = (TemplateRegistry) getBean("templateRegistry");
        } catch (ForceProjectException e) {
            logger.error("Failed to obtain serviceLocator bean. Cause: " + e);
            throw new Error(e);
        }
    }

    /**
     * tries to resolve the app server to be used for the tests. Looks for the
     * system property AppServerForTest. If not set, uses the entry from
     * test.properties.
     *
     * @param prependHttpProtocol
     * @return http://<the app server> or just the app server based on if
     *         protocal prepend was asked for.
     */
    public static String getAppServerToHitForTests(boolean prependHttpProtocol) {
        // get the vm argument first.
        String appServerForTestValue = System
                .getProperty(IdeTestConstants.APP_SERVER_FOR_TEST_KEY);
        // if that's not set, then use what's there in test.properties.
        if (IdeTestUtil.isEmpty(appServerForTestValue)) {
            appServerForTestValue = ConfigProps.getInstance().getProperty(
                IdeTestConstants.APP_SERVER_FOR_TEST_KEY);
        }
        return prependHttpProtocol ? (new StringBuffer("http://")
        .append(appServerForTestValue)).toString()
        : appServerForTestValue;

    }

    public static TreeSet<String> getSupportedEndpointVersions() {
        return getProjectService().getSupportedEndpointVersions();
    }

    public static Object getBean(String name) throws ForceProjectException {
        return ContainerDelegate.getInstance().getBean(name);
    }

    public static ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public static ProjectService getProjectService() {
        return serviceLocator.getProjectService();
    }

    public static PackageDeployService getPackageDeployService() {
        return serviceLocator.getPackageDeployService();
    }

    public static PackageRetrieveService getPackageRetrieveService() {
        return serviceLocator.getPackageRetrieveService();
    }

    public static MetadataService getMetadataService() {
        return serviceLocator.getMetadataService();
    }

    public static ToolingService getToolingService() {
        return serviceLocator.getToolingService();
    }

    public static ProjectController getProjectController()
            throws ForceProjectException {
        return new ProjectController();
    }

    public static FactoryLocator getFactoryLocator()
            throws ForceProjectException {
        return factoryLocator;
    }

    public static ComponentFactory getComponentFactory()
            throws ForceProjectException {
        return factoryLocator.getComponentFactory();
    }

    public static ConnectionFactory getConnectionFactory()
            throws ForceProjectException {
        return factoryLocator.getConnectionFactory();
    }

    public static ProjectPackageFactory getProjectPackageFactory() {
        return factoryLocator.getProjectPackageFactory();
    }

    public static ProjectPackageList getProjectPackageListInstance() {
        return factoryLocator.getProjectPackageFactory()
                .getProjectPackageListInstance();
    }

    public static PackageManifestFactory getPackageManifestFactory() {
        return factoryLocator.getPackageManifestFactory();
    }
    
    public static TemplateRegistry getTemplateRegistry() {
        return templateRegistry;
    }

    /**
     * returns the absolute path of a file if the input path relative to the
     * test fragment.
     *
     * @param fileNameNoPath
     * @return
     */
    @Deprecated
    public static String getPdeTestFile(String testRelativePath) {
        String testSrcHome = getTestHomeAbsolutePath();
        Path absPath = new Path(testSrcHome + testRelativePath);
        return absPath.toOSString();
    }

    /**
     * returns the absolute path of the test home . for ex:
     * /home/ssasalatti/dev/
     * app/main/clients/toolkit/3.3/test/src/com.salesforce.toolkit.test
     *
     * @return
     */
    @Deprecated
    public static String getTestHomeAbsolutePath() {
        return ConfigProps.getInstance().getProperty(
            IdeTestConstants.TEST_PROPERTIES_RELEASE_HOME)
            + File.separator
            + ConfigProps.getInstance().getProperty(
                IdeTestConstants.TEST_PROPERTIES_TEST_HOME);

    }

    /**
     * returns url entry for a given resource in the plugin.
     *
     * @param resourceRelPath
     * @return
     */
    public static URL getUrlEntry(String resourceRelPath) {
        if (ForceIdeCorePlugin.getDefault() == null
                || Utils.isEmpty(resourceRelPath)) {
            return null;
        }
        return ForceIdeCorePlugin.getFullUrlResource(resourceRelPath);
    }

    /**
     * returns the full path to a given resource in this plugin.
     *
     * @param resourceRelPath
     * @return
     */
    public static URL getFullUrlEntry(String resourceRelPath) {
        URL urlResource = getUrlEntry(resourceRelPath);
        if (urlResource != null) {
            try {
                urlResource = FileLocator.toFileURL(urlResource);
            } catch (IOException e) {
                logger.error("Unable to get full url for resource '"
                        + resourceRelPath + "'");
            }
        }
        return urlResource;
    }

    /**
     * Returns a file from a iFile.
     *
     * @param iFile
     * @return
     */
    public static File getFileFromIFile(IFile iFile) {
        String filePath = iFile.getLocation().toString();
        return new File(filePath);
    }

    /**
     * Returns a random numeric String of length maxLength.
     *
     * @param maxLength
     * @return
     */
    public static String getRandomString(int maxLength) {
        String randomString = new Long(System.currentTimeMillis()).toString();
        return randomString.substring(randomString.length() - maxLength);
    }

    /**
     * delete files recursively from a given path in the file system
     *
     * @param path
     * @return
     */
    public static boolean deleteDirectoryRecursively(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectoryRecursively(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    /**
     * Copies files from srcFiles to destination directory in the file system.
     * srcFiles could be a directory too. NOTE: DESTINATION MUST BE A DIRECTORY.
     *
     * @param destDir
     * @param srcFiles
     * @throws IdeTestException
     */
    public static void copyFilesToDirRecursively(File destDir, File... srcFiles)
            throws IdeTestException {

        if (!destDir.exists())
            destDir.mkdir();

        if (destDir.exists() && !destDir.isDirectory())
            throw IdeTestException.getWrappedException(destDir
                + " must be a directory");

        File destPath;
        try {
            for (File srcPath : srcFiles) {
                if (srcPath.exists()) {
                    destPath = new File(destDir, srcPath.getName());
                    if (srcPath.isDirectory()) {
                        copyFilesToDirRecursively(destPath, srcPath.listFiles());
                    } else {
                        FileOutputStream fos = new FileOutputStream(destPath);
                        fos.write(Utils.getBytesFromFile(srcPath));
                        fos.close();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw IdeTestException.getWrappedException(
                "FileNotFound Exception while trying to copy files.", e);
        } catch (IOException e) {
            throw IdeTestException.getWrappedException(
                "IOException while trying to copy files.", e);
        }
    }

    /**
     * Utility method to check the success of deploy result. Also logs all the
     * deployment messages.
     *
     * @param deployResult
     * @return empty string if successful, deploy result if failed.
     */
    public static String getFailedResultMessageIfAny(
            DeployResultExt deployResult) {
        Assert.assertNotNull("DeployResultHandler is null", deployResult);
        String border = "\n------------------------------------------------------------------\n";
        StringBuffer strBuff = new StringBuffer(border);
        DeployMessageExt messageHandler = deployResult.getMessageHandler();
        if (messageHandler != null) {
            messageHandler.logMessage(strBuff, false);
        }

        int msgCnt = 0;

        // Logging the RunTestResults
        if (deployResult.getRunTestsResult() != null) {
            RunTestsResult runTestResult = deployResult.getRunTestsResult();
            strBuff.append("\nGot the following run test results:");
            // Logging the Coverage Report
            if (null != runTestResult.getCodeCoverage()) {
                strBuff.append("\nCoverage Results:");
                msgCnt = 0;
                for (CodeCoverageResult codeCoverage : runTestResult
                        .getCodeCoverage()) {
                    strBuff
                    .append("\n (" + ++msgCnt + ") ")
                    .append("Name = ")
                    .append(codeCoverage.getName())
                    .append(
                        "Number of Uncovered Locations = "
                                + codeCoverage
                                .getNumLocationsNotCovered());
                    if (null != codeCoverage.getLocationsNotCovered()) {
                        strBuff.append("Uncovered Locations are:");
                        int loc = 0;

                        for (CodeLocation codeLocation : codeCoverage
                                .getLocationsNotCovered()) {
                            strBuff.append("\n\t (" + ++loc + ") ").append(
                                "Line # = " + codeLocation.getLine())
                                .append(
                                    "Column # = "
                                            + codeLocation.getColumn());
                        }
                    }
                }
            } else {
                strBuff.append("\n No Test Coverage Details.");
            }

            // Logging the Test Failures
            if (null != runTestResult.getFailures()) {
                strBuff.append("\nTest Failures:");
                msgCnt = 0;
                for (RunTestFailure testFailure : runTestResult.getFailures()) {
                    strBuff.append("\n (" + ++msgCnt + ") ").append(
                            "packageName = ").append(
                                testFailure.getPackageName()).append("Name = ")
                                .append(testFailure.getName()).append(
                                        "methodName = ").append(
                                            testFailure.getMethodName()).append(
                                                    "mesage = ").append(
                                                        testFailure.getMessage());
                }
            } else {
                strBuff.append("\n No Test Failures.");
            }

            // Logging the Test Successes
            if (null != runTestResult.getSuccesses()) {
                strBuff.append("\nTest Successes:");
                msgCnt = 0;
                for (RunTestSuccess testSuccess : runTestResult.getSuccesses()) {
                    strBuff.append("\n (" + ++msgCnt + ") ").append("Name = ")
                    .append(testSuccess.getName()).append(
                            "methodName = ").append(
                                testSuccess.getMethodName());
                }
            } else {
                strBuff.append("\n No Test Successes.");
            }

            logger.debug(strBuff.toString());
        }

        strBuff.append(border);
        if (!deployResult.isSuccess()) {
            // Assert.fail("Deployment failed. See messages below." +
            // strBuff.toString());
            return strBuff.toString();
        }
        return "";
    }

    public static boolean isEqual(String str, String str2) {
        if (Utils.isEmpty(str)) {
            if (Utils.isNotEmpty(str2)) {
                return false;
            }
        } else if (!str.equals(str2)) {
            return false;
        }
        return true;
    }

    public static boolean isNotEqual(String str, String str2) {
        return !isEqual(str, str2);
    }

    public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isEmpty(Object obj) {
        return (obj == null);
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static boolean isEmpty(byte[] objs) {
        return objs == null || objs.length == 0;
    }

    public static boolean isNotEmpty(byte[] objs) {
        return !isEmpty(objs);
    }

    public static boolean isEmpty(Object[] objs) {
        return objs == null || objs.length == 0;
    }

    public static boolean isNotEmpty(Object[] objs) {
        return !isEmpty(objs);
    }

    public static boolean isEmpty(Collection<?> col) {
        return col == null || col.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> col) {
        return !isEmpty(col);
    }

    public static boolean isEmpty(List<?> col) {
        return col == null || col.isEmpty();
    }

    public static boolean isNotEmpty(List<?> col) {
        return !isEmpty(col);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * Deletes a project component from both project and org by using the
     * refactoring api. The method checks if the component is delete supported
     * in ComponentTypeEnum, else throws a RuntimeException
     *
     * @param project
     * @param component
     * @throws IdeTestException
     */
    public static void deleteComponentListFromOrgAndProject(IProject project,
            ComponentList componentList) throws IdeTestException {
        String result = deleteComponentListFromOrg(project, componentList);
        if (IdeTestUtil.isEmpty(result)) {
            try {
                deleteComponentListFromLocalFS(componentList);
            } catch (FactoryException e) {
                throw IdeTestException
                .getWrappedException(
                    "Factory Exception while trying to delete component from the file system",
                    e);
            } catch (ForceProjectException e) {
                throw IdeTestException
                .getWrappedException(
                    "Force Project Exception while trying to delete component from the file system",
                    e);
            } catch (CoreException e) {
                throw IdeTestException
                .getWrappedException(
                    "Core Exception while trying to delete component from the file system",
                    e);
            }
        } else
            Assert
            .fail("Could not delete from the org. Got the following message"
                    + result);
    }

    public static void deleteComponentFromOrgAndProject(IProject project,
            Component component) throws IdeTestException {
        ComponentList componentList = new ComponentList();
        componentList.add(component);
        deleteComponentListFromOrgAndProject(project, componentList);
    }

    /**
     * deletes a component in the project from the org.
     *
     * @param project
     * @param component
     * @return empty string if succesful, failure message if delete failed.
     * @throws IdeTestException
     */
    public static String deleteComponentFromOrg(IProject project,
            Component component) throws IdeTestException {
        ComponentList componentList = new ComponentList();
        componentList.add(component);
        return deleteComponentListFromOrg(project, componentList);
    }

    /**
     * Deletes a project component from the org only but not from project. The
     * method checks if the component is delete supported in ComponentTypeEnum,
     * else throws an ideTestException
     *
     * @param conn
     * @param component
     * @throws IdeTestException
     * @throws Exception
     * @return empty string if succesful, failure message if delete failed.
     */
    public static String deleteComponentListFromOrg(IProject project,
            ComponentList componentList) throws IdeTestException {
        String retVal = null;

        ProjectPackageList projectPackageList = getProjectPackageFactory()
                .getProjectPackageListInstance();
        projectPackageList.setProject(project);
        projectPackageList.addComponents(componentList, false);
        ProjectPackage unpackaged = projectPackageList.get(0);

        for (Component component : componentList) {
            if (null!=component &&ComponentTypeEnum.valueOf(component.getComponentType())
                    .isDeletable()) {
                if (null != unpackaged.getComponentByFileName(component
                    .getFileName())) {
                    try {
                        unpackaged.addDeleteComponent(component);
                    } catch (ForceProjectException e) {
                        throw IdeTestException.getWrappedException(
                            "Force Project exception while trying to delete "
                                    + component.getName() + " from org", e);
                    } catch (FactoryException e) {
                        throw IdeTestException.getWrappedException(
                            "Factory Project exception while trying to delete "
                                    + component.getName() + " from org", e);
                    }
                }
            } else {
                throw IdeTestException
                .getWrappedException("Deleting the component type not supported. Type:"
                        + component.getComponentType());
            }
        }

        // perform remote delete only if there is a component deleted, include
        // equal due to package manifest was added
        // afterwards.
        if (unpackaged.getDeletePackageManifest() != null) {
            DeployResultExt dre = null;
            try {
                dre = IdeTestUtil.getPackageDeployService().deployDelete(
                    projectPackageList, false, new NullProgressMonitor());
            } catch (ForceConnectionException e) {
                throw IdeTestException.getWrappedException(
                    "Force Connection while trying to delete from org", e);
            } catch (ServiceException e) {
                throw IdeTestException.getWrappedException(
                    "Deployment Exception while trying to delete from org",
                    e);
            } catch (InterruptedException e) {
                throw IdeTestException
                .getWrappedException(
                    "Interrupted Exception while trying to delete from org",
                    e);
            } catch (InsufficientPermissionsException e) {
                throw IdeTestException.getWrappedException(e
                    .getExceptionMessage(), e);
            } catch (ForceRemoteException e) {
                throw IdeTestException.getWrappedException(e
                    .getExceptionMessage(), e);
            }
            retVal = getFailedResultMessageIfAny(dre);
            // return IdeTestUtil.isNotEmpty(dre) ? dre.isSuccess() : false;
        }
        return retVal;
    }

    /**
     * deletes a file which is of type componentTypeEnum and asserts that it got
     * deleted from the workspace and the org.
     *
     * @param project
     * @param cType
     * @param fileNameForCreatedComponent
     * @throws IdeTestException
     */
    public static boolean deleteAndAssertThatComponentIsDeletedFromOrgAndProject(
            IProject project, ComponentTypeEnum cType,
            String fileNameForCreatedComponent) throws IdeTestException {
        Component toDelete = null;
        List<Component> cList;
        try {
            cList = IdeTestUtil
                    .getProjectService()
                    .getComponentsForComponentType(project, cType.getTypeName());
            for (Component c : cList) {
                if (c.getFileName().equalsIgnoreCase(
                    fileNameForCreatedComponent)) {
                    toDelete = c;
                    break;
                }
            }
            Assert.assertNotNull("A deleteable component for "
                    + fileNameForCreatedComponent + " should've been found.",
                    toDelete);
            IdeTestUtil.deleteComponentFromOrgAndProject(project, toDelete);

            Assert.assertTrue("Looks like the " + cType.getTypeName()
                + " file " + fileNameForCreatedComponent
                + " did not get deleted", Utils.isEmpty(IdeProjectFixture
                    .getInstance().findFileInProject(project,
                        fileNameForCreatedComponent)));
        } catch (CoreException e) {
            throw IdeTestException.getWrappedException(
                "Core Exception while trying to delete component "
                        + fileNameForCreatedComponent, e);
        } catch (FactoryException e) {
            throw IdeTestException.getWrappedException(
                "Factory Exception while trying to delete component "
                        + fileNameForCreatedComponent, e);
        }
        return true;
    }

    /**
     * deletes a file which is of type componentTypeEnum and asserts that it got
     * deleted from the workspace and the org.
     *
     * @param project
     * @param cType
     * @param fileNameForCreatedComponent
     * @throws IdeTestException
     */
    public static boolean deleteAndAssertThatComponentIsNotDeletedFromOrgAndProject(
            IProject project, ComponentTypeEnum cType,
            String fileNameForCreatedComponent) throws IdeTestException {
        Component toDelete = null;
        List<Component> cList;
        try {
            cList = IdeTestUtil
                    .getProjectService()
                    .getComponentsForComponentType(project, cType.getTypeName());
            for (Component c : cList) {
                if (c.getFileName().equalsIgnoreCase(
                    fileNameForCreatedComponent)) {
                    toDelete = c;
                    break;
                }
            }

            Assert
            .assertNotNull(
                "A deleteable component for " + fileNameForCreatedComponent + " should've been found.", toDelete); //$NON-NLS-1$ //$NON-NLS-2$
            String retVal = IdeTestUtil.deleteComponentFromOrg(project,
                toDelete);

            if (Utils.isEmpty(retVal)) {
                throw IdeTestException
                .getWrappedException("Looks like the " + cType.getTypeName() + " file " + fileNameForCreatedComponent + " was deleted when it should not have been."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        } catch (CoreException e) {
            throw IdeTestException
            .getWrappedException(
                "Core Exception while trying to delete component " + fileNameForCreatedComponent, e); //$NON-NLS-1$
        } catch (FactoryException e) {
            throw IdeTestException
            .getWrappedException(
                "Factory Exception while trying to delete component " + fileNameForCreatedComponent, e); //$NON-NLS-1$
        }
        return true;
    }

    /**
     * returns the IFolder in which the component is stored
     *
     * @param project
     * @param component
     * @throws IdeTestException
     */
    public static IFolder getFolder(IProject project, Component component) {
        String path = Constants.SOURCE_FOLDER_NAME
                + "/" + component.getDefaultFolder(); //$NON-NLS-1$
        return project.getFolder(path);
    }

    /**
     * returns the first IFile within the specified folder of the specified type
     *
     * @param folder
     * @param component
     * @throws IdeTestException
     */
    public static IFile getFirstResource(IFolder folder, Component component)
            throws IdeTestException {
        try {
            IResource[] resources = folder.members();
            for (IResource resource : resources) {
                if (resource.getType() != IResource.FILE
                        && !resource.getName().endsWith(
                            component.getFileExtension())) {
                    continue;
                }

                return (IFile) resource;
            }
        } catch (CoreException e) {
            throw IdeTestException
            .getWrappedException("Unable to obtain resources from IFolder " + folder.getFullPath().toPortableString()); //$NON-NLS-1$
        }

        return null;
    }

    /**
     * returns the first IFile within the specified project of the specified
     * type
     *
     * @param project
     * @param component
     * @throws IdeTestException
     */
    public static IFile getFirstResource(IProject project, Component component)
            throws IdeTestException {
        return getFirstResource(getFolder(project, component), component);
    }

    /**
     * returns the first IFile within the specified project of the specified
     * type
     *
     * @param project
     * @param component
     * @throws IdeTestException
     */
    public static IFile getResource(IProject project, Component component)
            throws IdeTestException {
        return getFirstResource(getFolder(project, component), component);
    }

    /**
     * replaces all "/" in a given path to the OS specific file separator. eg:
     * /filemetadata/simple would become \filemetadata\simple on windows.
     *
     * @param relPath
     * @return null if input was empty
     */
    public static String convertToOSSpecificPath(String relPath) {
        if (isEmpty(relPath))
            return null;
        return relPath.replaceAll("[/]+", "\\" + File.separator);
    }

    /**
     * given a relative path to filemetadata, it constructs the delete path. eg:
     * /filemetadata/simple would return /filemetadata/delete/simple.
     *
     * @param addFromRelativePath
     * @return null if input was empty.
     */
    public static String constructDestructiveChangesPath(
            String addFromRelativePath) {
        if (isEmpty(addFromRelativePath))
            return null;
        addFromRelativePath = convertToOSSpecificPath(addFromRelativePath);
        String removeRelativePath = new String(File.separator);
        StringTokenizer tok = new StringTokenizer(addFromRelativePath,
            File.separator);
        while (tok.hasMoreTokens()) {
            String temp = tok.nextToken();
            if (temp.equalsIgnoreCase("filemetadata")) {
                removeRelativePath += temp + File.separator + "delete";
                continue;
            }
            removeRelativePath += File.separator + temp;
        }
        return convertToOSSpecificPath(removeRelativePath);
    }

    /**
     * Replace token with replacement string in given file content or recursive
     * replace all in dir. Usage: for example, dashboard filemetadata needs
     * replace runningUser as test running user on the fly.
     *
     * @param root
     *            - can either be file or directory
     * @param token
     * @param replacement
     * @throws IdeTestException
     */
    public static void replaceFileContent(File root, String token,
            String replacement) throws IdeTestException {
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            for (File file : files) {
                replaceFileContent(file, token, replacement);
            }
        } else {
            try {
                String fileContent = new String(Utils.getBytesFromFile(root));
                if (fileContent.indexOf(token) == -1)
                    return;
                fileContent = fileContent.replace(token, replacement);
                FileOutputStream fos = new FileOutputStream(root);
                fos.write(fileContent.getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                throw IdeTestException
                .getWrappedException(
                    "FileNotFound exception while trying to replace string in file",
                    e);
            } catch (IOException e) {
                throw IdeTestException.getWrappedException(
                    "IO exception while trying to replace string in file",
                    e);
            }
        }
    }

    /**
     * Remove given string in given file content or recursive remove from all in
     * dir. Usage: for example, When copy package.xml from fileMetadata to
     * project, need to remove extra <fullName> element.
     *
     * @param root
     *            - can either be file or directory
     * @param removalString
     *            - string to be removed
     * @throws IdeTestException
     */
    public static void removeFileContent(File root, String removalString)
            throws IdeTestException {
        replaceFileContent(root, removalString, "");
    }

    /**
     * Polls for a condition.For example, you want to wait till a shell appears and then want a handle on that shell.
     * let's say some condition takes a while to be executed. in that case, it'll run the same check till the timeout.
     * waits for waitInterval.
     *
     * @param <T>
     * @param condition
     * @param timeout
     * @param waitInterval
     * @return T or null based on condition at the end of the timeout.
     * @throws IdeTestException
     */
    public static <T> T pollForCondition(IdeTestCondition<T> condition,
            long timeout, long waitInterval) throws IdeTestException {
        long currentTimeStamp = System.currentTimeMillis();
        long timeoutTimeStamp = currentTimeStamp + timeout;
        T retObj = null;
        while (System.currentTimeMillis() < timeoutTimeStamp) {
            try {
                retObj = condition.testMe();
            } catch (Exception e) {
                throw IdeTestException.getWrappedException(condition.failWithThisMessage(), e);
            }
            if (IdeTestUtil.isNotEmpty(retObj)) {
                return retObj;
            }
            try {
                Thread.sleep(waitInterval);
            } catch (InterruptedException e) {
                throw IdeTestException.getWrappedException(
                    "Wait interrupted", e);
            }

        }
        throw IdeTestException.getWrappedException("Timed out:"
                + condition.failWithThisMessage());
    }

    /**
     * polls for a Object condition.for ex. you want to wait till a shell
     * appears and then want a handle on that shell. let's say some condition
     * takes a while to be executed. in that case, it'll run the same check till
     * the timeout. waits for waitInterval.
     *
     * @param <T>
     *
     * @param condition
     * @param timeout
     * @param waitInterval
     * @return Object or null based on condition at the end of the timeout.
     * @throws IdeTestException
     */
    public static <T> T pollForCondition(IdeTestCondition<T> condition,
            long timeout) throws IdeTestException {
        return pollForCondition(condition, timeout,
            IdeTestConstants.TEST_WAIT_INTERVAL);
    }

    /**
     * polls for a condition.for ex. you want to wait till a shell appears and then want a handle on that shell. let's
     * say some condition takes a while to be executed. in that case, it'll run the same check till the timeout. waits
     * for waitInterval.
     *
     * @param <T>
     *
     * @param condition
     * @param timeout
     * @param waitInterval
     * @return Object or null based on condition at the end of the timeout.
     * @throws IdeTestException
     */
    public static <T> T pollForCondition(IdeTestCondition<T> condition)
            throws IdeTestException {
        return pollForCondition(condition, IdeTestConstants.TEST_WAIT_TIMEOUT,
            IdeTestConstants.TEST_WAIT_INTERVAL);
    }

    /**
     * Generates a random integer between lower n upper.
     *
     * @param lower
     * @param upper
     * @return
     */
    public static int randomInt(int lower, int upper) {
        return (new Random()).nextInt((upper - lower + 1)) + lower;
    }

    /**
     * compare 2 file contents. Note: ignores whitespaces and carriage returns
     *
     * @return true if the file contents are equal.
     * @param file1
     * @param file2
     * @throws IdeTestException
     */
    public static boolean compareFiles(File file1, File file2)
            throws IdeTestException {

        char[] file1Chars = readCharsFromFile(file1);
        char[] file2Chars = readCharsFromFile(file2);

        return Arrays.equals(file1Chars, file2Chars);

    }

    /**
     * reads a file and returns a character array of the characters. Note:
     * IGNORES WHITESPACES, '\n', '\r'
     *
     * @param file
     * @return char array
     * @throws FileNotFoundException
     * @throws IOException
     * @throws IdeTestException
     */
    public static char[] readCharsFromFile(File file) throws IdeTestException {
        try {
            if (IdeTestUtil.isEmpty(file) || !file.exists()) {
                throw IdeTestException
                .getWrappedException("File not found or doesn't exist");
            }
            // remove whitespaces from both the files
            FileImageInputStream inStreamFile = new FileImageInputStream(file);
            String inStreamFileLine = null;
            StringBuffer inStreamFileBuffer = new StringBuffer();
            while (IdeTestUtil.isNotEmpty(inStreamFileLine = inStreamFile
                    .readLine())) {
                inStreamFileLine = inStreamFileLine
                        .replaceAll("[* \n\t\r]", "");
                inStreamFileBuffer.append(inStreamFileLine);
            }
            return inStreamFileBuffer.toString().toCharArray();
        } catch (FileNotFoundException e) {
            throw IdeTestException
            .getWrappedException(
                "File not found exception while trying to compare files",
                e);
        } catch (IOException e) {
            throw IdeTestException.getWrappedException(
                "IO Exception while trying to compare files", e);
        }
    }

    // -----PRIVATE-HELPERS-----
    /**
     * deletes component from file system
     *
     * @param componentList
     * @throws FactoryException
     * @throws ForceProjectException
     * @throws CoreException
     */
    private static void deleteComponentListFromLocalFS(
            ComponentList componentList) throws FactoryException,
            ForceProjectException, CoreException {
        for (Component component : componentList) {
            IFile fileResource = component.getFileResource();
            if (component.isMetadataComposite()) {
                Component metadataComponent = getComponentFactory()
                        .getCompositeComponentFromComponent(component);
                IFile metadataResource = metadataComponent.getFileResource();
                metadataResource.delete(true, new NullProgressMonitor());
            }
            fileResource.delete(true, new NullProgressMonitor());
        }
    }

    public static String getContentString(IFile file) throws IOException,
    CoreException {
        String contentStr = null;
        if (file != null && file.exists()) {
            contentStr = getContentString(file.getContents());
        }

        return contentStr;
    }

    public static String getContentString(InputStream contents)throws IOException, CoreException {
        if (contents == null) {
            return null;
        }
        String contentStr = null;
        StringBuffer strBuff = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(contents,
                Constants.UTF_8));
            String line = reader.readLine();
            if (line != null) {
                strBuff.append(line);
            }
            while ((line = reader.readLine()) != null) {
                strBuff.append(Constants.NEW_LINE);
                strBuff.append(line);
            }
        } catch (IOException e) {
            logger.error("Unable to load body from stream", e);
            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Loaded body size ["
                    + strBuff.toString().getBytes().length
                    + "] bytes from stream");
        }

        contentStr = strBuff.toString();
        try {
            contentStr = new String(strBuff.toString().getBytes(),
                Constants.UTF_8);
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to set body", e);
        }

        return contentStr;
    }

    public static void sleep(int millis) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Sleeping for ~" + (double) millis / 1000
                    + " secs");
            }
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    /**
     * logs the project contents.
     *
     * @param project
     */
    public static void logProjectContents(IProject project) {
        logger.info("Project content structure:\n" + project.getName()
            + getProjectContentsTree(project));
    }

    public static String getProjectContentsTree(IProject project) {
        String tree = null;
        if (System.getProperty("os.name").indexOf("Linux") != -1) {
            Process process = null;
            BufferedReader input = null;
            try {
                process = Runtime.getRuntime().exec("/usr/bin/tree", null,
                    project.getLocation().toFile());
                process.waitFor();
                input = new BufferedReader(new InputStreamReader(process
                    .getInputStream()));
                StringBuffer strBuff = new StringBuffer();
                String line = "";
                while ((line = input.readLine()) != null) {
                    strBuff.append("\n" + line);
                }
                tree = strBuff.toString();
            } catch (Exception e) {
                logger
                .info("Unable to execute 'tree' command to log project contents.  Is 'tree' install (sudo apt-get install tree)?: "
                        + e.getMessage());
            } finally {
                if (process != null) {
                    process.destroy();
                }

                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                    }
                }
            }
        } else {
            logger.info("Sorry, logging project structure not implemented for "
                    + System.getProperty("os.name"));
        }

        return tree;
    }

    /**
     * Returns the current Eclipse MajorVersion.
     *
     * @return
     * @throws IdeTestException
     */
    public static String getCurrentEclipseMajorVersion()
            throws IdeTestException {

        String osgiFrameworkVersion = System.getProperty("osgi.framework.version");
        if (IdeTestUtil.isEmpty(osgiFrameworkVersion))
            throw IdeTestException.getWrappedException(
            		"Couldn't retrieve osgi.framework.version system Property. Cannot determine Eclipse version.");
        return osgiFrameworkVersion.substring(0, 3);
    }

    /**
     * returns the current operating system Information i.e. OS and Architecture
     *
     * @return
     */
    public static Object getCurrentOperatingSystemInfo() {
        return (new StringBuffer("OS:").append(getCurrentOS()).append(
                ".   Arch:").append(Platform.getOSArch())).toString();
    }

    /**
     * @return current OS.
     */
    public static String getCurrentOS() {
        return Platform.getOS();
    }

    /**
     * Find out what's extra string in src list.
     *
     * @param srcList
     * @param refList
     * @return String - what's extra in src list
     */
    public static String getExtraInSrcList(List<String> srcList,
            List<String> refList) {
        List<String> srcCopied = cloneStringList(srcList);
        List<String> refCopied = cloneStringList(refList);
        srcCopied.removeAll(refCopied);

        String srcExtraStr = "";
        for (String s : srcCopied) {
            srcExtraStr = srcExtraStr + s + "\n";
        }
        return srcExtraStr;
    }

    private static List<String> cloneStringList(List<String> list) {
        List<String> copiedList = new ArrayList<String>();
        for (String s : list) {
            copiedList.add(s);
        }
        return copiedList;
    }

    public static File[] listFilesRecursively(File[] files) {
        List<File> retfiles = new ArrayList<File>();
        for (File f : files) {
            if (f.isDirectory()) {
                retfiles.addAll(Arrays.asList(listFilesRecursively(f
                    .listFiles())));
            }
            retfiles.add(f);
        }
        return retfiles.toArray(new File[retfiles.size()]);
    }

    public static Component getRandomComponentForComponentType(byte[] zipFile,
            String componentType) throws IOException, FactoryException,
            ForceProjectException {
        if (Utils.isEmpty(zipFile) || Utils.isEmpty(componentType)) {
            throw new IllegalArgumentException("File zip cannot be null");
        }

        List<String> filepaths = ZipUtils.getFilePaths(zipFile);

        if (Utils.isEmpty(filepaths)) {
            logger.warn("No filepaths found for zip");
            return null;
        }

        Component component = getComponentFactory()
                .getComponentByComponentType(componentType);
        if (component == null) {
            logger.warn("No component found for type");
            return null;
        }

        ComponentList componentList = getComponentFactory()
                .getComponentListInstance();
        for (String filepath : filepaths) {
            if (filepath.contains(component.getDefaultFolder() + "/")) {
                componentList.add(getComponentFactory().getComponentByFilePath(
                    filepath));
            }
        }

        if (Utils.isEmpty(componentList)) {
            logger.warn("No components of type found");
            return null;
        }

        return componentList.get(randomInt(0, componentList.size() - 1));
    }

    public static IFolder createFolder(IProject project, String name) throws CoreException {
        IFolder folder = project.getFolder(name);
        folder.create(true, true, new NullProgressMonitor());
        return folder;
    }

    public static IProject createProject(String name) throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(name);
        project.create(new NullProgressMonitor());
        project.open(new NullProgressMonitor());
        return project;
    }
}
