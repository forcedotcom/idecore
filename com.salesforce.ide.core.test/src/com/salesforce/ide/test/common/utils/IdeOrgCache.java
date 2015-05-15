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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.project.ForceProject;
import com.sforce.soap.metadata.FileProperties;

/**
 * Abstract org Cache. Concrete Caches will use all of the functionality here and will be used as singletons in the
 * respective org fixtures.
 *
 * @see {@link IdeLocalTestOrgFixture}, {@link IdeRemoteTestOrgFixture}
 * @author ssasalatti
 */
public abstract class IdeOrgCache {
    private static final Logger logger = Logger.getLogger(IdeOrgCache.class);
    /**
     * This guy will hold the orgs that are created. Everytime this fixture is asked for an org, this cache will be
     * checked. Trying to avoid org creation as much as possible.
     */
    protected HashMap<OrgTypeEnum, OrgInfo> orgCache;

    protected void addOrgToCache(OrgTypeEnum orgType, OrgInfo orgInfo) {
        logger.debug("Adding " + orgInfo.toString() + " as " + orgType.toString() + " to Org Cache");
        orgCache.put(orgType, orgInfo);
    }

    public OrgInfo getOrgInfoFromCache(OrgTypeEnum orgType) {
        return orgCache.get(orgType);
    }

    protected void removeOrgFromCache(OrgTypeEnum orgType) {
        orgCache.remove(orgType);
        logger.debug("Removed " + orgType.toString() + " entry from Org Cache");
    }

    public HashMap<OrgTypeEnum, OrgInfo> getOrgCache() {
        return orgCache;
    }

    public void purgeCache() {
        orgCache.clear();
    }

    //------------NESTED CLASSES--------------

    /**
     * represents a package.see orgInfo. It contains a package name, type and the paths from where it was created and
     * should be destroyed from. These strings can be null.
     */
    public static class PackageInfo {
        private final String packageName;
        private final PackageTypeEnum packageType;
        private final String addFromRelPath; // path from where data was added as package. can be null
        private final String removeRelPath; // path from where destructive changes will be used. can be null
        private final String pkgNameSpacePrefix;


        public PackageInfo(String packageName, PackageTypeEnum packageType, String addFromRelPath,
                String removeRelPath) {
            super();
            this.packageName = packageName;
            this.packageType = packageType;
            this.addFromRelPath = addFromRelPath;
            this.removeRelPath = removeRelPath;
            this.pkgNameSpacePrefix = null;

        }

        public PackageInfo(String packageName, PackageTypeEnum packageType, String addFromRelPath,
                String removeRelPath, String pkgNameSpacePrefix) {
            super();
            this.packageName = packageName;
            this.packageType = packageType;
            this.addFromRelPath = addFromRelPath;
            this.removeRelPath = removeRelPath;
            this.pkgNameSpacePrefix = pkgNameSpacePrefix;
        }
        public String getPkgNameSpacePrefix() {
            return pkgNameSpacePrefix;
        }

        public String getAddFromRelPath() {
            return addFromRelPath;
        }

        public String getRemoveRelPath() {
            return removeRelPath;
        }

        public String getPackageName() {
            return packageName;
        }

        public PackageTypeEnum getPackageType() {
            return packageType;
        }

        @Override
        public String toString() {
            StringBuffer buff =
                    new StringBuffer("Package Name: ").append(packageName).append(", PackageType: ").append(
                        packageType.toString()).append("\nAdded From Path: ").append(addFromRelPath).append(
                        "\nDestructiveChanges Path: ").append(removeRelPath).append("\nPackageNamespacePrefix: ").append(pkgNameSpacePrefix);
            return buff.toString();
        }
    }

    /**
     * Holds all the org related info. This is what you'll get back from the cache.
     */
    public static class OrgInfo {
        private final String orgId;
        private final OrgTypeEnum orgEdition;
        private final String userName;
        private final String password;
        private final String token;
        private final String namespace;
        private final String endPointServer;
        private final String apiVersion;
        private boolean needsHttps = true;
        private boolean keepEndpoint = true;

        List<PackageInfo> packageList = null; //holds package related info when things are uploaded during tests.
        List<FileProperties> orgSnapshot = null; //holds an org snapshot

        public List<FileProperties> getOrgSnapshot() {
            return orgSnapshot;
        }

        public void setOrgSnapshot(List<FileProperties> orgSnapshot) {
            this.orgSnapshot = orgSnapshot;
        }

        public OrgInfo(String orgId, OrgTypeEnum orgEdition, String userName, String password, String token,
                String namespace, String endPointServer, String apiVersion, boolean needsHttps, boolean keepEndpoint)
                throws IdeTestException {
            super();
            if (IdeTestUtil.isEmpty(orgEdition) || IdeTestUtil.isEmpty(userName) || IdeTestUtil.isEmpty(password)
                    || IdeTestUtil.isEmpty(apiVersion) || IdeTestUtil.isEmpty(endPointServer))
                throw IdeTestException
                        .getWrappedException("one of orgEdition or username or password or api version or endpoint server is not set.Cannot create org info");
            this.orgId = orgId;
            this.orgEdition = orgEdition;
            this.userName = userName;
            this.password = password;
            this.token = token;
            this.namespace = namespace;
            this.endPointServer = endPointServer;
            this.apiVersion = apiVersion;
            this.needsHttps = needsHttps;
            this.keepEndpoint = keepEndpoint;
            this.packageList = new ArrayList<PackageInfo>();
        }

        public String getOrgId() {
            return orgId;
        }

        public String getUserName() {
            return userName;
        }

        public String getPassword() {
            return password;
        }

        public String getToken() {
            return token;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getEndPointServer() {
            return endPointServer;
        }

        public String getApiVersion() {
            return apiVersion;
        }

        public List<PackageInfo> getPackageList() {
            return packageList;
        }

        public boolean isNeedsHttps() {
            return needsHttps;
        }

        public boolean isKeepEndpoint() {
            return keepEndpoint;
        }

        public void setPackageList(List<PackageInfo> packageList) {
            this.packageList = packageList;
        }

        public OrgTypeEnum getOrgEdition() {
            return orgEdition;
        }

		@Override
        public String toString() {
            StringBuffer buff = new StringBuffer("\n-------OrgInfo:---------");
            buff.append("\nOrg Edition: " + orgEdition.toString());
            buff.append("\nOrgId: " + orgId);
            buff.append("\nUsername: " + userName);
            buff.append("\nPassword: " + password);
            buff.append("\nToken: " + token);
            buff.append("\nNamespace: " + namespace);
            buff.append("\nEndPointServer: " + endPointServer);
            buff.append("\napiVersion: " + apiVersion);
            buff.append("\nhttps: " + new Boolean(needsHttps).toString());
            buff.append("\nendpoint retained: " + new Boolean(keepEndpoint).toString());
            buff.append("\n------------------------");

            return buff.toString();
        }

        /**
         * Constructs a temporary ForceProject object with this org info so that it can be used in various force calls.
         * I could have just stored org info within a force project instance variable instead of storing them
         * separately. But force project is just a wrapper around a "ide project" with org info. To increase
         * readability, I went with the former approach. This method achieves the latter in case you need to use it.
         *
         * @return
         */
        public ForceProject getWrapperForceProject() {
            ForceProject forceProject = new ForceProject(userName, password, token, null, endPointServer);
            forceProject.setHttpsProtocol(needsHttps);
            forceProject.setKeepEndpoint(keepEndpoint);
            forceProject.setEndpointApiVersion(apiVersion);
            forceProject.setMetadataFormatVersion(apiVersion);
            return forceProject;
        }

        /**
         * adds a package entry to package list
         *
         * @param packageName
         * @param packageType
         * @param addDataFromRelPath
         * @param removeRelPath
         */
        public void addPackageToPackageList(String packageName, PackageTypeEnum packageType, String addDataFromRelPath,
                String removeRelPath) {
            packageList.add(new PackageInfo(packageName, packageType, addDataFromRelPath, removeRelPath));
        }

        public List<String> getPackageNames() {
            List<String> pkgNames = new ArrayList<String>();
            for(PackageInfo pInfo: packageList)
                pkgNames.add(pInfo.getPackageName());
            return Collections.unmodifiableList(pkgNames);
        }

        /**
         * removes a package entry
         *
         * @param pkgToRemove
         */
        public void removePackageFromPackageList(PackageInfo pkgToRemove) {
            packageList.remove(pkgToRemove);

        }

        public void addPackageToPackageList(String packageName, PackageTypeEnum packageType,
                String addMetaDataFromPath, String removeRelPath, String installedPkgNameSpacePrefix) {
            packageList.add(new PackageInfo(packageName, packageType, addMetaDataFromPath, removeRelPath,installedPkgNameSpacePrefix));

        }

    }

}
