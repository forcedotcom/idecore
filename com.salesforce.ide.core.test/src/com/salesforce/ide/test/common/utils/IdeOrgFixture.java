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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.api.metadata.types.PackageTypeMembers;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.internal.utils.ZipUtils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.core.remote.metadata.IDeployResultExt;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.test.common.utils.IdeOrgCache.OrgInfo;
import com.salesforce.ide.test.common.utils.IdeOrgCache.PackageInfo;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.PartnerConnection;
import com.sforce.soap.partner.wsc.QueryResult;
import com.sforce.soap.partner.wsc.SaveResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * an abstract class for org fixtures. will let you create an org and talk to it for testing. most of the methods have
 * been taken from the framework prior to 156. Concrete classes will hold their respective caches and provide any
 * specific functionalities.
 *
 * @author ssasalatti
 */
public abstract class IdeOrgFixture {
    private static final Logger logger = Logger.getLogger(IdeOrgFixture.class);

    /**
     * concrete fixtures will have to implement this method to create an org.
     *
     * @param orgType
     * @return
     * @throws IdeTestException
     */
    public abstract OrgInfo getOrg(OrgTypeEnum orgType) throws IdeTestException;

    public abstract IdeOrgCache getOrgCacheInstance();

    /**
     * Adds meta data to an org. Essentially deploys stuff in addDataFromPath to org specified in orgType as a package
     * of type packageTypeEnum Caller should also send path to the destructive changes.
     *
     * @param orgType
     * @param addMetaDataFromRelPath
     *            //should be relative to the test fragment
     * @param packageType
     * @param removeRelPath
     * @return the name of the package that was deployed
     * @throws IdeTestException
     */
    public String addMetaDataToOrg(OrgInfo orgInfo, String addMetaDataFromRelPath, PackageTypeEnum packageType,
            String removeRelPath) throws IdeTestException {
        logger.debug("Adding metadata from " + addMetaDataFromRelPath + " To Org");
        if (IdeTestUtil.isEmpty(orgInfo)) {
            throw new IllegalArgumentException("OrgCache has to have an org at this point!");
        }

        // deploy package
        String packageName = deployFolderAsPackage(orgInfo, addMetaDataFromRelPath, packageType);

        // update packageinfo in OrgInfo
        // convert to OS specific string just to be sure.
        removeRelPath = IdeTestUtil.convertToOSSpecificPath(removeRelPath);
        orgInfo.addPackageToPackageList(packageName, packageType, addMetaDataFromRelPath, removeRelPath);

        return packageName;

    }

    /**
     * Deploys folder as package of type packageEnum package name is randomly generated except for when it is of type
     * unpackaged.
     *
     * @param orgInfo
     *            get from cache
     * @param addMetaDataFromRelPath
     * @param packageType
     * @param removeRelPath
     * @throws IdeTestException
     */
    protected String deployFolderAsPackage(OrgInfo orgInfo, String addMetaDataFromRelPath, PackageTypeEnum packageType)
            throws IdeTestException {
        try {
            logger.debug("Deploying metadata from " + addMetaDataFromRelPath + " as package" + packageType.toString());
            // set the package name. for unpackaged, it'll be "unpackaged" else
            // generate.
            String packageName = null;
            if (packageType == PackageTypeEnum.UNPACKAGED) {
                packageName = IdeTestConstants.DEFAULT_PACKAGED_NAME;
            } else {
                packageName = packageType.toString() + generateUniquePackageName();
            }
            // convert to OS specific string just to be sure.
            addMetaDataFromRelPath = IdeTestUtil.convertToOSSpecificPath(addMetaDataFromRelPath);
            deployFolderAsPackageWorker(orgInfo, addMetaDataFromRelPath, packageName);

            // mark package managed in org.
            if (packageType == PackageTypeEnum.MANAGED_DEV_PKG) {
                // check if org has namespace or not.
                // Annotation setup ensures that you can't continue without a namespaced org.
                // This check is if someone else uses it.
                if (IdeTestUtil.isEmpty(orgInfo.getNamespace()))
                    IdeTestException
                            .wrapAndThrowException("Namespace couldn't have been empty.check if org has namespace.");

                Connection conn = IdeTestUtil.getConnectionFactory().getConnection(orgInfo.getWrapperForceProject());

                ConnectorConfig connectorConfig = conn.getConnectorConfig();
                connectorConfig.setTraceMessage(true);
                String serviceEndpoint = connectorConfig.getServiceEndpoint();

                final String newVersion = "20.0";
                final String oldVersion = "16.0";
                try {
                    if (!serviceEndpoint.endsWith(newVersion)) {

                        connectorConfig.setServiceEndpoint(serviceEndpoint.replaceAll(oldVersion, newVersion));
                    }
                    PartnerConnection partner = conn.getPartnerConnection();
                    QueryResult result =
                            partner.query("SELECT ID FROM DevelopmentPackageVersion WHERE NAME='" + packageName + "'");
                    SObject[] resultObjects = result.getRecords();
                    Assert.isTrue(1 == resultObjects.length,
                        "Package names are unique.Queried for a single package, got back more.");

                    SObject pkg = new SObject();
                    pkg.setType("DevelopmentPackageVersion");
                    pkg.setId(resultObjects[0].getId());
                    Assert.isTrue(pkg.getType().equalsIgnoreCase("DevelopmentPackageVersion"),
                        "SObject type is not DevelopmentPackageVersion");
                    pkg.setField("PackageType", "1");
                    SaveResult[] results = partner.update(new SObject[] { pkg });
                    Assert.isTrue(results[0].isSuccess(), "Failed to set package as managed.");
                } finally {
                    if (serviceEndpoint.endsWith(newVersion)) {
                        connectorConfig.setServiceEndpoint(serviceEndpoint.replaceAll(newVersion, oldVersion));
                    }
                }

            }
            return packageName;

        } catch (ForceException e) {
            throw IdeTestException.getWrappedException(
                "Connection Exception while trying to deploy folder as package.", e);
        } catch (ServiceException e) {
            throw IdeTestException.getWrappedException("Deploy Exception while trying to deploy folder as package.", e);
        } catch (IOException e) {
            throw IdeTestException.getWrappedException("I/O Exception while trying to deploy folder as package.", e);
        } catch (FactoryException e) {
            throw IdeTestException.getWrappedException(
                "Ide Factory Exception while trying to deploy folder as package.", e);
        } catch (ForceProjectException e) {
            throw IdeTestException.getWrappedException(
                "Force Project Exception while trying to deploy folder as package.", e);
        } catch (InterruptedException e) {
            throw IdeTestException.getWrappedException(
                "Interrupted Exception while trying to deploy folder as package.", e);
        } catch (ConnectionException e) {
            throw IdeTestException.getWrappedException("API Connection Exception", e);
        }
    }

    /**
     * removes the meta data from the org. It needs a path from where the meta data was added. It checks the cache and
     * gets the package list for the org type and then looks for a package with the given addedFromRelPath and uses the
     * corresponding removeRelPath. If remove path wasn't found,it skips the delete. CheckifOrgClean will then complain
     * in that case.
     *
     * @param orgType
     * @param packageType
     * @param addedMetaDataFromRelPath
     * @throws IdeTestException
     */
    public void removeMetaDataFromOrg(OrgInfo orgInfo, PackageTypeEnum packageType, String addedMetaDataFromRelPath)
            throws IdeTestException {
        if (IdeTestUtil.isEmpty(orgInfo)) {
            throw new IllegalArgumentException(
                    "While deleting stuff, orginfo can't be null. There has to be something in the cache");
        }
        try {
            logger.debug("removing metadata from that was added from " + addedMetaDataFromRelPath + " To Org");
            List<PackageInfo> pkgList = orgInfo.getPackageList();
            Iterator<PackageInfo> itr = pkgList.iterator();
            PackageInfo pkgToRemove = null;
            while (itr.hasNext()) {
                PackageInfo temp = itr.next();
                // skip pakages which weren't added from file system or the
                // annotation.
                if (Utils.isEmpty(temp.getAddFromRelPath()))
                    continue;

                if (temp.getAddFromRelPath().equalsIgnoreCase(addedMetaDataFromRelPath)
                        && temp.getPackageType() == packageType) {
                    pkgToRemove = temp;
                }
            }

            // remove that package
            if (!Utils.isEmpty(pkgToRemove)) {
                // remove package contents

                deployFolderAsPackageWorker(orgInfo, pkgToRemove.getRemoveRelPath(), pkgToRemove.getPackageName());

                // remove the package itself
                // need to do this only in case its a package
                if (packageType == PackageTypeEnum.MANAGED_DEV_PKG || packageType == PackageTypeEnum.UNMANAGED_DEV_PKG) {
                    Connection conn =
                            IdeTestUtil.getConnectionFactory().getConnection(orgInfo.getWrapperForceProject());
                    PartnerConnection partnerConn = conn.getPartnerConnection();
                    QueryResult queryRes =
                            partnerConn.query("SELECT ID FROM DevelopmentPackageVersion WHERE NAME ='"
                                    + pkgToRemove.getPackageName() + "'");
                    SObject[] resultObjects = queryRes.getRecords();
                    Assert.isTrue(1 == resultObjects.length, "Package was not retrieved.");
                    String pkgId = resultObjects[0].getId();
                    partnerConn.delete(new String[] { pkgId });
                }
                // update orgInfo with package info
                orgInfo.removePackageFromPackageList(pkgToRemove);
            }
        } catch (ForceConnectionException e) {
            throw IdeTestException.getWrappedException(
                "Connection Exception while trying to remove metadata from org.", e);
        } catch (ServiceException e) {
            throw IdeTestException.getWrappedException("Deploy Exception while trying to remove metadata from org.", e);
        } catch (IOException e) {
            throw IdeTestException.getWrappedException("I/O Exception while trying to remove metadata from org.", e);
        } catch (FactoryException e) {
            throw IdeTestException.getWrappedException(
                "Ide Factory Exception while trying to remove metadata from org.", e);
        } catch (ForceProjectException e) {
            throw IdeTestException.getWrappedException(
                "Force Project Exception while trying to remove metadata from org.", e);
        } catch (InterruptedException e) {
            throw IdeTestException.getWrappedException(
                "Interrupted Exception while trying to remove metadata from org.", e);
        } catch (ConnectionException e) {
            throw IdeTestException.getWrappedException(
                "API Connection Exception while trying to remove metadata from org.", e);
        } catch (InsufficientPermissionsException e) {
            throw IdeTestException.getWrappedException(e.getExceptionMessage(), e);
        } catch (ForceRemoteException e) {
            throw IdeTestException.getWrappedException(e.getExceptionMessage(), e);
        }

    }

    /**
     * Org should not have any packages. unpacakged content should be back to what it was. for this the orgInfo
     * maintains a field called orgretrieveziplength. it is set when the cache entry is created/updated. This length is
     * recalculated and checked with the one in orgCache.
     *
     * @return null string if clean else error message.
     * @throws IdeTestException
     */
    public String checkIfOrgClean(OrgInfo orgInfo) throws IdeTestException {

        String retFailureMessage = null;
        logger.debug("Checking if org is clean or not...");
        if (IdeTestUtil.isEmpty(orgInfo))
            return retFailureMessage = "While deleting stuff, orginfo can't be null.";

        List<FileProperties> originalOrgSnapshot = orgInfo.getOrgSnapshot();
        List<FileProperties> currentOrgSnapshot = getOrgSnapshot(orgInfo);
        //construct a dummy failure message
        StringBuilder stringBuilder =
                new StringBuilder("Org Unclean!! . Possibitlities: \n")
                        .append("1)A previous test didn't clean up something it explicitly added\n")
                        .append("2)Current test didn't clean up something it explicitly added\n")
                        .append("3) there was an Exception/Error in the test\n")
                        .append(
                            "Repurcusions:\n In case of test running agains local app, org might be recreated.\n Future tests might fail in case org from a remote server app was used.\n");
        if (!diffOrgSnapShots(originalOrgSnapshot, currentOrgSnapshot))
            return retFailureMessage =
                    stringBuilder.append(
                        "Packages' contents don't match. Look for 'Org Clean Check Failure' in logs for more details.")
                            .toString();
        return retFailureMessage;
    }

    /**
     * Generates an org snapshot. Basically retrieves packaged, unpackaged and isntalled content.
     *
     * @param orgInfo
     * @return
     * @throws IdeTestException
     */
    protected List<FileProperties> getOrgSnapshot(OrgInfo orgInfo) throws IdeTestException {
        List<FileProperties> fileProperties = new ArrayList<FileProperties>();
        try {

            //list the metadata from the org.

            //unpkgd, unmngd installed, dev pkgd(unmngd and mngd)
            Connection conn = IdeTestUtil.getConnectionFactory().getConnection(orgInfo.getWrapperForceProject());
            FileMetadataExt listMetadata =
                    IdeTestUtil.getMetadataService().listMetadata(conn, new NullProgressMonitor());
            if (IdeTestUtil.isNotEmpty(listMetadata) && listMetadata.hasFileProperties())
                fileProperties.addAll(Arrays.asList(listMetadata.getFileProperties()));

            //mngd installed
            RetrieveResultExt retrievedManagedInstalledPackages =
                    IdeTestUtil.getPackageRetrieveService().retrieveManagedInstalledPackages(conn,
                        new NullProgressMonitor());
            if (IdeTestUtil.isNotEmpty(retrievedManagedInstalledPackages)
                    && IdeTestUtil.isNotEmpty(retrievedManagedInstalledPackages.getRetrieveResult())) {
                fileProperties.addAll(Arrays.asList(retrievedManagedInstalledPackages.getRetrieveResult()
                        .getFileProperties()));
            }

        } catch (Exception e) {
            IdeTestException.wrapAndThrowException("There was an error while generating an org Snapshot", e);
        }
        return Collections.unmodifiableList(fileProperties);
    }

    /**
     * Diffs two org snapshots and logs discrepencies
     *
     * @param originalOrgSnapshot
     * @param currentOrgSnapshot
     * @return true if no differences were found.
     */
    private boolean diffOrgSnapShots(List<FileProperties> originalOrgSnapshot, List<FileProperties> currentOrgSnapshot) {
        FileProperties[] arr_originalOrgSnapshot =
                originalOrgSnapshot.toArray(new FileProperties[originalOrgSnapshot.size()]);
        FileProperties[] arr_currentOrgSnapshot =
                currentOrgSnapshot.toArray(new FileProperties[currentOrgSnapshot.size()]);
        List<String> componentsExistLocally = findDiscrepancies(arr_originalOrgSnapshot, arr_currentOrgSnapshot);
        List<String> componentsExistRemotely = findDiscrepancies(arr_currentOrgSnapshot, arr_originalOrgSnapshot);
        boolean isClean = true;
        StringBuffer strBuff =
                new StringBuffer("Org Clean Check Failure:\nThe following components exists locally, but NOT remotely:");
        int cnt = 0;
        if (Utils.isNotEmpty(componentsExistLocally)) {
            isClean = false;
            Collections.sort(componentsExistLocally);
            for (String componentName : componentsExistLocally) {
                strBuff.append("\n  ").append("(").append(++cnt).append(") ").append(componentName);
            }
        } else {
            strBuff.append("\nn/a");

        }

        strBuff.append("\n\nThe following components exists remotely, but NOT locally:");
        if (Utils.isNotEmpty(componentsExistRemotely)) {
            isClean = false;
            Collections.sort(componentsExistRemotely);
            cnt = 0;
            for (String componentName : componentsExistRemotely) {
                strBuff.append("\n  ").append("(").append(++cnt).append(") ").append(componentName);
            }
        } else {
            strBuff.append("\nn/a");
        }
        strBuff.append("\n");

        if (!isClean)
            logger.warn(strBuff.toString());
        return isClean;
    }

    /**
     * compares 2 sets of file properties. Used by diffOrgSnapshots
     *
     * @param tmp1FileProperties
     * @param tmp2FileProperties
     * @return list of names that are unique in the first parameter when compared to the second.
     */
    private List<String> findDiscrepancies(FileProperties[] tmp1FileProperties, FileProperties[] tmp2FileProperties) {
        List<String> tmpFileNames = new ArrayList<String>();
        for (FileProperties tmp1FileProperty : tmp1FileProperties) {
            boolean found = false;
            for (FileProperties tmp2FileProperty : tmp2FileProperties) {
                if (tmp1FileProperty.getFullName().equals(tmp2FileProperty.getFullName())) {
                    found = true;
                    continue;
                }
            }

            if (!found) {
                tmpFileNames.add("Type:" + tmp1FileProperty.getType() + ", FileName:" + tmp1FileProperty.getFileName()
                        + ", FullName:" + tmp1FileProperty.getFullName());
            }
        }
        return tmpFileNames;
    }

    // -------------PRIVATE HELPERS.

    protected String generateUniquePackageName() {
        return "TPkg" + IdeTestUtil.getRandomString(4);
    }

    /**
     * Retrieves unpackaged stuff from org and return the file size or the zip byte stream. Ideally, one should do a
     * checksum or a hashcode comparison. but that might be expensive when this thing runs per test.
     *
     * @param orgInfo
     * @throws ForceProjectException
     * @throws ForceConnectionException
     * @throws InterruptedException
     * @throws ForceRemoteException
     * @throws ServiceException
     */
    protected int getUnpackagedRetrieveZipLength(OrgInfo orgInfo) throws ForceConnectionException,
            ForceProjectException, ForceRemoteException, InterruptedException, ServiceException {
        return getUnpackagedRetrieveResult(orgInfo).getZipFile().length;
    }

    protected RetrieveResultExt getUnpackagedRetrieveResult(OrgInfo orgInfo) throws ForceConnectionException,
            ForceProjectException, ForceRemoteException, InterruptedException, ServiceException {
        Connection conn = IdeTestUtil.getConnectionFactory().getConnection(orgInfo.getWrapperForceProject());

        RetrieveResultExt result =
                IdeTestUtil.getPackageRetrieveService().retrieveDefaultPackage(conn, new NullProgressMonitor());

        Assert.isNotNull(result, "retrieve result can never be null");
        // uncomment me while debugging.
        //        ZipUtils.writeZipToTempDir(result.getZipFile(), orgInfo.getOrgId() + "-"
        //                + (new Long(System.currentTimeMillis())).toString() + ".zip");

        return result;
    }

    /**
     * Deploy worker
     *
     * @param connection
     * @param folder
     * @param packageName
     * @return
     * @throws IOException
     * @throws ForceProjectException
     * @throws FactoryException
     * @throws ForceConnectionException
     * @throws InterruptedException
     * @throws IdeTestException
     * @throws ServiceException
     * @throws InsufficientPermissionsException
     * @throws Exception
     */
    private IDeployResultExt deployFolderAsPackageWorker(final OrgInfo orgInfo, String folderRelPath,
            final String packageName) throws IOException, FactoryException, ForceProjectException,
            ForceConnectionException, InterruptedException, IdeTestException, ServiceException, ForceRemoteException {

        IdeTestUtil.MetadataZipper metadataZipper = new IdeTestUtil.MetadataZipper() {

            @Override
            protected IdeTestFileMetadataReplacementUtil getFileReplacementsUtil() {
                // substitute strings if it's required for specific component types to deploy
                IdeTestFileMetadataReplacementUtil replaceUtil = new IdeTestFileMetadataReplacementUtil();
                replaceUtil.add(Constants.DASHBOARD, IdeTestConstants.RUNNING_USER_REPLACEMENT_TOKEN, orgInfo
                        .getUserName());
                replaceUtil.add(Constants.DOCUMENT, IdeTestConstants.DEV_NAME_REPLACEMENT_TOKEN, "ideTestDevName"
                        + IdeTestUtil.getRandomString(4));
                replaceUtil.add(Constants.ANALYTIC_SNAPSHOT, IdeTestConstants.RUNNING_USER_REPLACEMENT_TOKEN, orgInfo
                        .getUserName());
                replaceUtil.add(Constants.PACKAGE_MANIFEST, IdeTestConstants.PACKAGE_NAME_TOKEN, packageName);
                return replaceUtil;

            }

        };
        final byte[] zipBytes = metadataZipper.generateZipBytes(folderRelPath, packageName);

        //uncomment to see what's being deployed.
        ZipUtils.writeZipToTempDir(zipBytes, "deleteme");

        // deploy
        Assert.isNotNull(orgInfo, "Org Info cannot be null while deploying data to org");
        Connection conn = IdeTestUtil.getConnectionFactory().getConnection(orgInfo.getWrapperForceProject());
        DeployResultExt dre = IdeTestUtil.getPackageDeployService().deploy(conn, zipBytes, new NullProgressMonitor());
        String result = IdeTestUtil.getFailedResultMessageIfAny(dre);
        if (IdeTestUtil.isNotEmpty(result))
            throw IdeTestException.getWrappedException("Deploy failed while deploying from folder" + folderRelPath
                    + ". Got the following deploy message." + result);
        return dre;
    }

    /**
     * Generate destructive changes instance from org content.
     *
     * @param orgInfo
     * @return
     * @throws ForceConnectionException
     * @throws ForceProjectException
     * @throws ForceRemoteException
     * @throws InterruptedException
     * @throws FactoryException
     */
    public Package generateDestructiveChanges(OrgInfo orgInfo) throws ForceConnectionException, ForceProjectException,
            ForceRemoteException, InterruptedException, FactoryException {
        Connection connection = IdeTestUtil.getConnectionFactory().getConnection(orgInfo.getWrapperForceProject());
        Package destructiveChanges = new Package();
        destructiveChanges.setVersion(IdeTestUtil.getProjectService().getLastSupportedEndpointVersion());
        FileMetadataExt fileMetadataExt =
                IdeTestUtil.getMetadataService().listMetadata(connection, new NullProgressMonitor());
        if (fileMetadataExt != null && fileMetadataExt.hasFileProperties()) {
            Map<String, List<FileProperties>> filePropertiesMap = fileMetadataExt.getFilePropertiesMap();
            TreeSet<String> componentTypes = new TreeSet<String>();
            componentTypes.addAll(filePropertiesMap.keySet());
            for (String componentType : componentTypes) {
                PackageTypeMembers type = new PackageTypeMembers();
                type.setName(componentType);
                List<FileProperties> filePropertyList = filePropertiesMap.get(componentType);
                for (FileProperties fileProperties : filePropertyList) {
                    // skip metadata files
                    if (fileProperties.getFileName().endsWith("-meta.xml")) {
                        continue;
                    }

                    // strip leading component type path, ie "labels" in "labels/MyLabel.label"
                    // stip file extension
                    // REVIEWME: maybe getFullName would work
                    String fileName = fileProperties.getFileName();
                    if (fileName.contains("/")) {
                        fileName = fileName.substring(fileName.indexOf("/") + 1);
                    }

                    if (fileName.contains(".")) {
                        fileName = fileName.substring(0, fileName.indexOf("."));
                    }

                    type.getMembers().add(fileName);
                }

                destructiveChanges.getTypes().add(type);
            }
        }

        // use destructiveChanges.getXMLString() to write to string/file, if needed
        return destructiveChanges;
    }

}
