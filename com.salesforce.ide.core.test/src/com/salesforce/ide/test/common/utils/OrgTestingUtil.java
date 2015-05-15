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
/*
 * Copyright, 2003, SALESFORCE.com All Rights Reserved Company Confidential
 */
package com.salesforce.ide.test.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.test.common.utils.remote.RemoteAppCall;
import com.salesforce.ide.test.common.utils.remote.RemoteOperationEnum;
import com.salesforce.ide.test.common.utils.remote.RemoteAppCall.RequestObject;
import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.PartnerConnection;
import com.sforce.soap.partner.wsc.QueryResult;
import com.sforce.soap.partner.wsc.SaveResult;
import com.sforce.ws.ConnectionException;

/**
 * A convenience class for org related operations
 * 
 * @author agupta
 * @deprecated
 */
public class OrgTestingUtil {

    private static final String TEST_ENV = "Other (Specify)";
    private final OrgInfo testOrg;
    public static final String ORG_PASSWORD = "123456";

    private static final Logger logger = Logger.getLogger(OrgTestingUtil.class);

    private static Connection conn;
    private final List<PackageInfo> packages = new ArrayList<PackageInfo>();
    private final ForceProject forceProject;
    private static ConfigProps _props = ConfigProps.getInstance();

    public static enum OrgEdition {
        DE("DE"), EE("EE"), UE("UE");

        private String edition;

        private OrgEdition(String val) {
            this.edition = val;
        }

        @Override
        public String toString() {
            return this.edition;
        }
    }

    public static class PackageInfo {
        private String packageName = null;
        private final boolean managed;

        private PackageInfo(String name, boolean isManaged) {
            this.packageName = name;
            this.managed = isManaged;
        }

        public String getPackageName() {
            return packageName;
        }

        public boolean isManaged() {
            return managed;
        }

        @Override
        public String toString() {
            return ((managed) ? "Managed" : "Unmanaged") + " Package Name: " + packageName;
        }
    }

    public static class OrgInfo {
        private String orgId;
        private String username;
        private OrgEdition orgEdition;
        private String namespace;
        private String soapEndPointServer;

        public String getOrgId() {
            return orgId;
        }

        public void setOrgId(String orgId) {
            this.orgId = orgId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public OrgEdition getOrgEdition() {
            return orgEdition;
        }

        public void setOrgEdition(OrgEdition orgEdition) {
            this.orgEdition = orgEdition;
        }

        @Override
        public String toString() {
            return ("Org ID: " + orgId + ", Username: " + username + ", Edition: " + orgEdition + ", Namespace: " + namespace);
        }

        public String getNamespace() {
            return namespace;
        }

        public String getSoapEndPointServer() {
            return soapEndPointServer;
        }

        public void setSoapEndPointServer(String soapEndPointServer) {
            this.soapEndPointServer = soapEndPointServer;
        }
    }

    public OrgTestingUtil(OrgInfo testOrg) throws ForceConnectionException, ForceProjectException,
            InsufficientPermissionsException {
        this.testOrg = testOrg;
        this.forceProject = getOrgTestingToolKitProject();
        conn = IDETestingUtil.getFactoryLocator().getConnectionFactory().getConnection(getForceProject());
    }

    public ForceProject getOrgTestingToolKitProject() {
        ForceProject testingForceProject = new ForceProject();
        testingForceProject.setUserName(getUsername());
        testingForceProject.setPassword(ORG_PASSWORD);
        testingForceProject.setEndpointServer(getSoapEndPointServer());
        testingForceProject.setEndpointEnvironment(TEST_ENV);
        testingForceProject.setReadTimeoutSecs(Constants.READ_TIMEOUT_IN_SECONDS_DEFAULT);
        if (Utils.isNotEmpty(_props.getProperty("default.pde.use-https"))) {
            testingForceProject.setHttpsProtocol(Boolean.valueOf(_props.getProperty("default.pde.use-https")));
        }
        logger.info("Create force project instance " + testingForceProject.getLogDisplay() + " for PDE testing.");
        return testingForceProject;
    }

    public OrgInfo getOrgInfo() {
        return testOrg;
    }

    public String getUsername() {
        return testOrg.username;
    }

    /*
     * public String getSoapEndPoint() { return testOrg.soapEndPoint; }
     */

    public String getSoapEndPointServer() {
        return testOrg.soapEndPointServer;
    }

    public boolean isNamespaced() {
        if (null != testOrg.namespace && testOrg.namespace.length() > 0)
            return true;
        return false;
    }

    /**
     * @param managed
     * @return packageName
     * @throws Exception
     */
    public String getPackage(boolean managed) throws Exception {
        String packageName = null;

        for (PackageInfo pack : this.packages) {
            if (pack.isManaged() == managed)
                packageName = pack.getPackageName();
        }

        if (null == packageName)
            packageName = createUniquePackage(managed);

        return packageName;
    }

    public void addHostToWhiteList() throws Exception {
        logger.info("Adding localhost to org whitelist");
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString(), testOrg.getOrgId());
        RequestObject rObject = new RequestObject(null, RemoteOperationEnum.ADD_HOST_TO_WHITELIST, params);
        RemoteAppCall.sendRequest(rObject);
        logger.info("Added localhost to org whitelist");
    }

    public void removeHostFromWhiteList() throws Exception {
        logger.info("Removing localhost to org whitelist");
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString(), testOrg.getOrgId());
        RequestObject rObject = new RequestObject(null, RemoteOperationEnum.REMOVE_HOST_FROM_WHITELIST, params);
        RemoteAppCall.sendRequest(rObject);
        logger.info("Removed localhost to org whitelist");
    }

    public void setAPIToken(String token) throws Exception {
        logger.info("Setting Api Token for current user");
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString(), testOrg.getOrgId());
        params.setProperty(RemoteOperationEnum.OperationKey.USERNAME.toString(), testOrg.getUsername());
        params.setProperty(RemoteOperationEnum.OperationKey.API_TOKEN.toString(), token);
        RequestObject rObject = new RequestObject(null, RemoteOperationEnum.SET_API_TOKEN, params);
        RemoteAppCall.sendRequest(rObject);
        logger.info("Api token has been set successfully as " + token);
    }

    public void enableOrgPerm(String... permissionBit) throws Exception {
        logger.info("Enabling Org perms: " + permissionBit);
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString(), testOrg.getOrgId());
        params.setProperty(RemoteOperationEnum.OperationKey.USERNAME.toString(), testOrg.getUsername());
        String permList = "";
        for (String perm : permissionBit) {
            permList = permList + "," + perm;
        }
        permList = permList.substring(1);
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_PERMISSION_BITS.toString(), permList);
        RequestObject rObject = new RequestObject(null, RemoteOperationEnum.ENABLE_PERM, params);
        RemoteAppCall.sendRequest(rObject);
        logger.info("Enabled perms successully.");
    }

    public void revokeOrgPerm(String... permissionBit) throws Exception {
        logger.info("Revoking Org perms: " + permissionBit);
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString(), testOrg.getOrgId());
        params.setProperty(RemoteOperationEnum.OperationKey.USERNAME.toString(), testOrg.getUsername());
        String permList = "";
        for (String perm : permissionBit) {
            permList = permList + "," + perm;
        }
        permList = permList.substring(1);
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_PERMISSION_BITS.toString(), permList);
        RequestObject rObject = new RequestObject(null, RemoteOperationEnum.REVOKE_PERM, params);
        RemoteAppCall.sendRequest(rObject);
        logger.info("Revoked perms successully.");
    }

    public String addNamespace(String name) throws Exception {
        logger.info("Adding namespace to the org: " + name);
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString(), testOrg.getOrgId());
        params.setProperty(RemoteOperationEnum.OperationKey.USERNAME.toString(), testOrg.getUsername());

        if (null != name && name.length() > 0)
            params.setProperty(RemoteOperationEnum.OperationKey.ORG_NAMESPACE.toString(), name);

        RequestObject rObject = new RequestObject(null, RemoteOperationEnum.ADD_NAMESPACE, params);
        Properties result = RemoteAppCall.sendRequest(rObject);
        name = result.getProperty(RemoteOperationEnum.OperationKey.ORG_NAMESPACE.toString());
        this.testOrg.namespace = name;
        logger.info("Added namespace " + name + " to the org successfully.");
        return name;
    }

    public String addUniqueNamespace() throws Exception {
        return addNamespace(null);
    }

    public void createPackage(String packageName, boolean isManaged) throws ForceConnectionException,
            ConnectionException {

        if (isManaged)
            assert this.getOrgInfo().getNamespace() != null : "Can't create managed package for org without namespace";

        PartnerConnection partner = conn.getPartnerConnection();
        SObject pkg = new SObject();
        pkg.setType("Project");
        pkg.setField("Name", packageName);
        pkg.setField("IsManaged", isManaged);
        SaveResult[] results = partner.create(new SObject[] { pkg });
        assert results[0].isSuccess() : "Failed to create pakage. Error: " + results[0].getErrors();
        this.packages.add(new PackageInfo(packageName, isManaged));
    }

    public String createUniquePackage(boolean isManaged) throws ForceConnectionException, ConnectionException {
        String pkgName;
        PartnerConnection partner = conn.getPartnerConnection();
        QueryResult qr = null;

        do {
            pkgName = "z" + BaseTestingUtil.getRandomString(5);
            qr = partner.query("Select id, name from Project where name = '" + pkgName + "'");
        } while (null != qr.getRecords() && qr.getRecords().length > 0);

        createPackage(pkgName, isManaged);
        return pkgName;
    }

    public List<PackageInfo> getPackages() {
        return Collections.unmodifiableList(packages);
    }

    public ForceProject getForceProject() {
        return forceProject;
    }
}
