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

import java.util.Properties;

import junit.framework.Assert;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.test.common.utils.IdeOrgCache.OrgInfo;
import com.salesforce.ide.test.common.utils.remote.RemoteAppCall;
import com.salesforce.ide.test.common.utils.remote.RemoteAppCall.RequestObject;
import com.salesforce.ide.test.common.utils.remote.RemoteOperationEnum;
import com.salesforce.ide.test.common.utils.remote.RemoteOperationEnum.OperationKey;
import com.sforce.soap.clienttest.Connector;
import com.sforce.soap.clienttest.CreateOrgWithInstalledPackageRequest;
import com.sforce.soap.clienttest.CreateOrgWithInstalledPackageResult;
import com.sforce.soap.clienttest.SoapConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * concrete org fixture. contains specific implementations for orgs running on local app server.
 * 
 * @author ssasalatti
 */
public class IdeLocalTestOrgFixture extends IdeOrgFixture {

    private static final Logger logger = Logger.getLogger(IdeLocalTestOrgFixture.class);

    IdeOrgCache orgCache = IdeLocalTestOrgCache.getInstance();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.salesforce.ide.test.common.utils.IdeOrgFixture#getOrg(com.salesforce
     * .ide.test.common.utils.OrgTypeEnum)
     */
    @Override
    public OrgInfo getOrg(OrgTypeEnum orgType) throws IdeTestException {
        return getOrg(orgType, false);
    }

    /**
     * Retrieves org from cache. If it isn't there, creates it and updates the cache. Can force create orgs
     * too.IMPORTANT! - NOT RECOMMENDED IF YOU WANT TO CREATE AN ORG FOR YOURSELF. If you really need an org, use
     * createOrg() and maintain a local copy of the orgInfo that it returns.
     * 
     * @param orgType
     * @return orgInfo object
     * @throws IdeTestException
     */
    public OrgInfo getOrg(OrgTypeEnum orgType, boolean forceCreate) throws IdeTestException {
        logger.debug("Trying to retrive Org from Cache...");
        OrgInfo orgInfo = orgCache.getOrgInfoFromCache(orgType);
        if (IdeTestUtil.isEmpty(orgInfo) && logger.isDebugEnabled()) {
            logger.debug(orgType.toString() + " org not found in local cache.Will have to create.");
        }
        if (null == orgInfo || forceCreate) {
            logger.debug("Creating org on local app server...");
            orgInfo = getOrgWorker(orgType);
            if (IdeTestUtil.isEmpty(orgInfo))
                IdeTestException.wrapAndThrowException("orgInfo cannot be null!");
            logger.debug("Adding org on local org Cache...");
            orgCache.addOrgToCache(orgType, orgInfo);
        }
        return orgInfo;
    }

    /**
     * creates a local org but does not update the cache.
     * 
     * @param orgType
     * @return orgInfo for the created org.
     * @throws IdeTestException
     */
    public OrgInfo createOrg(OrgTypeEnum orgType) throws IdeTestException {
        return getOrgWorker(orgType);
    }

    /**
     * Adds a namespace to a given org.
     * 
     * @param orgInfo
     * @param name
     * @return valid namespace if the operation was successful
     * @throws IdeTestException
     */
    public String addNamespaceToOrg(String orgId, String userName, String name) throws IdeTestException {
        Assert.assertFalse("Invalid namespace for org", Utils.isEmpty(name));
        Assert.assertFalse("Invalid orgId for org", Utils.isEmpty(orgId));
        Assert.assertFalse("Invalid username for org", Utils.isEmpty(userName));

        logger.info("Adding namespace to the org: " + name);
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString(), orgId);
        params.setProperty(RemoteOperationEnum.OperationKey.USERNAME.toString(), userName);

        if (null != name && name.length() > 0)
            params.setProperty(RemoteOperationEnum.OperationKey.ORG_NAMESPACE.toString(), name);

        RequestObject rObject = new RequestObject(null, RemoteOperationEnum.ADD_NAMESPACE, params);
        Properties result = RemoteAppCall.sendRequest(rObject);
        name = result.getProperty(RemoteOperationEnum.OperationKey.ORG_NAMESPACE.toString());
        if (IdeTestUtil.isEmpty(name))
            logger.info("Couldn't add namespace to the org successfully.");
        else
            logger.info("Added namespace " + name + " to the org successfully.");
        return name;
    }

    /**
     * Adds a given namespace to the given org
     * 
     * @param orgInfo
     * @param name
     * @return valid namespace if it was added succesfully.
     * @throws IdeTestException
     */
    public String addNamespaceToOrg(OrgInfo orgInfo, String name) throws IdeTestException {
        Assert.assertFalse("Invalid Org Info object", IdeTestUtil.isEmpty(orgInfo));
        return addNamespaceToOrg(orgInfo.getOrgId(), orgInfo.getUserName(), name);
    }

    /**
     * Enables a set of permissions in the given org.
     * 
     * @param orgInfo
     * @param permissions
     * @return false if perms weren't set.
     * @throws IdeTestException
     */
    public boolean enableOrgPerm(OrgInfo orgInfo, String... permissions) throws IdeTestException {
        Assert.assertFalse("Invalid Org Info object", IdeTestUtil.isEmpty(orgInfo));
        return enableOrgPerm(orgInfo.getOrgId(), orgInfo.getUserName(), permissions);

    }

    /**
     * Enables a set of permissions in the given org.
     * 
     * @param orgId
     * @param userName
     * @param permissions
     * @return
     * @throws IdeTestException
     */
    public boolean enableOrgPerm(String orgId, String userName, String... permissions) throws IdeTestException {
        Assert.assertFalse("Invalid orgId for org", Utils.isEmpty(orgId));
        Assert.assertFalse("Invalid username for org", Utils.isEmpty(userName));
        Assert.assertFalse("Invalid permission list", Utils.isEmpty(permissions));

        logger.info("Enabling Org perms: " + permissions);
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString(), orgId);
        params.setProperty(RemoteOperationEnum.OperationKey.USERNAME.toString(), userName);
        String permList = "";
        for (String perm : permissions) {
            permList = permList + "," + perm;
        }
        permList = permList.substring(1);
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_PERMISSION_BITS.toString(), permList);
        RequestObject rObject = new RequestObject(null, RemoteOperationEnum.ENABLE_PERM, params);
        @SuppressWarnings("unused")
        Properties result = RemoteAppCall.sendRequest(rObject);
        logger.info("Enabled perms successully.");
        // TODO: evaluate the result here
        return true;
    }

    public boolean enableUserPerm(OrgInfo orgInfo, String... permissions) throws IdeTestException {
        Assert.assertFalse("Invalid Org Info object", IdeTestUtil.isEmpty(orgInfo));
        return enableUserPerm(orgInfo.getOrgEdition(), orgInfo.getOrgId(), orgInfo.getUserName(), permissions);
    }
        
    /**
     * Enables a set of user permissions in the given org. Because of the way user permission works, this will need to
     * return a new user. The effects of returning the new user is transparent because we are going to store it into the
     * cache.
     * 
     * @param orgTypeEnum
     * 
     * @param orgId
     * @param permissions
     * @return
     * @throws IdeTestException
     */
    public boolean enableUserPerm(OrgTypeEnum orgTypeEnum, String orgId, String userName, String... permissions)
            throws IdeTestException {
        Assert.assertFalse("Invalid orgId for org", Utils.isEmpty(orgId));
        Assert.assertFalse("Invalid username for org", Utils.isEmpty(userName));
        Assert.assertFalse("Invalid user permission list", Utils.isEmpty(permissions));

        logger.info("Enabling user perms: " + permissions);
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString(), orgId);
        params.setProperty(RemoteOperationEnum.OperationKey.USERNAME.toString(), userName);
        String permList = "";
        for (String perm : permissions) {
            permList = permList + "," + perm;
        }
        permList = permList.substring(1);
        params.setProperty(RemoteOperationEnum.OperationKey.USER_PERMISSION_BITS.toString(), permList);
        RequestObject rObject = new RequestObject(null, RemoteOperationEnum.ENABLE_USER_PERM, params);
        Properties result = RemoteAppCall.sendRequest(rObject);
        OrgInfo orgInfo = createOrgInfo(orgTypeEnum, result);
        orgCache.addOrgToCache(orgTypeEnum, orgInfo);
        logger.info("Enabled user perms successully.");
        return true;
    }
 
    public boolean useApexDebugStreamingUser(OrgInfo orgInfo) throws IdeTestException {
        Assert.assertFalse("Invalid Org Info object", IdeTestUtil.isEmpty(orgInfo));
        return useApexDebugStreamingUser(orgInfo.getOrgEdition(), orgInfo.getOrgId());
    }

    public boolean useApexDebugStreamingUser(OrgTypeEnum orgTypeEnum, String orgId) throws IdeTestException {        
        logger.info("useApexDebugStreamingUser begin");
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString(), orgId);
        
        RequestObject rObject = new RequestObject(null, RemoteOperationEnum.USE_DEBUG_APEX_STREAMING_USER, params);
        Properties result = RemoteAppCall.sendRequest(rObject);
        final String userName = result.getProperty(OperationKey.USERNAME.toString());
        
        OrgInfo orgInfo = createOrgInfo(orgTypeEnum, userName, orgId);
        orgCache.addOrgToCache(orgTypeEnum, orgInfo);
        
        return true;
    }
    
    public void revokeOrgPerm(OrgInfo orgInfo, String... permissions) throws IdeTestException {
        Assert.assertFalse("Invalid Org Info object", IdeTestUtil.isEmpty(orgInfo));
        revokeOrgPerm(orgInfo.getOrgId(), orgInfo.getUserName(), permissions);
    }

    /**
     * revokes a set of permissions on the org.
     * 
     * @param orgId
     * @param userName
     * @param permissions
     * @return false if revocation failed.
     * @throws IdeTestException
     */
    public boolean revokeOrgPerm(String orgId, String userName, String... permissions) throws IdeTestException {
        Assert.assertFalse("Invalid orgId for org", Utils.isEmpty(orgId));
        Assert.assertFalse("Invalid username for org", Utils.isEmpty(userName));
        Assert.assertFalse("Invalid permission list", Utils.isEmpty(permissions));

        logger.info("Revoking Org perms: " + permissions);
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString(), orgId);
        params.setProperty(RemoteOperationEnum.OperationKey.USERNAME.toString(), userName);
        String permList = "";
        for (String perm : permissions) {
            permList = permList + "," + perm;
        }
        permList = permList.substring(1);
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_PERMISSION_BITS.toString(), permList);
        RequestObject rObject = new RequestObject(null, RemoteOperationEnum.REVOKE_PERM, params);
        @SuppressWarnings("unused")
        Properties result = RemoteAppCall.sendRequest(rObject);
        logger.info("Revoked perms successully.");
        return true;
    }

    /**
     * Revoke given org type from cache; therefore, next test setup will re-create this org type.
     * 
     * @param runForOrgType
     */
    public void forceRevokeOrgFromCache(OrgTypeEnum runForOrgType) {
        OrgInfo toBeRemovedOrgInfo = orgCache.getOrgInfoFromCache(runForOrgType);
        if (IdeTestUtil.isNotEmpty(toBeRemovedOrgInfo)) {
            logger.info("\n Revoke following org from cache: \n" + toBeRemovedOrgInfo.toString());
            orgCache.removeOrgFromCache(runForOrgType);
        }
    }

    // PRIVATE HELPERS.
    /**
     * creates an org and returns orginfo for the cache.
     * 
     * @param orgType
     * @return
     * @throws IdeTestException
     */
    protected OrgInfo getOrgWorker(OrgTypeEnum orgType) throws IdeTestException {
        OrgInfo orgInfo = null;

        // send request to create the org
        Properties params = new Properties();
        params.setProperty(RemoteOperationEnum.OperationKey.ORG_EDITION.toString(), orgType.getOrgTypeKey());
        RequestObject orgCreateRequest = new RequestObject(null, RemoteOperationEnum.CREATE_NEW_ORG, params);
        logger.info("CREATING ORG ON SERVER");
        Properties result = RemoteAppCall.sendRequest(orgCreateRequest);
        // increment ide test stats
        // TODO: is this the best place for this? aspects?
        IdeTestStats.getInstance().incrementOrgsCreatedCount();

        orgInfo = createOrgInfo(orgType, result);

        return orgInfo;
    }

    public OrgInfo createOrgInfo(OrgTypeEnum orgType, Properties result) throws IdeTestException {
        final String orgId = result.getProperty(RemoteOperationEnum.OperationKey.ORG_ID.toString());
        
        return createOrgInfo(orgType, result.getProperty(OperationKey.USERNAME.toString()), orgId);
    }

	private OrgInfo createOrgInfo(OrgTypeEnum orgType, String userName,
			final String orgId) throws IdeTestException {
		OrgInfo orgInfo;
        final String apiVersion = IdeTestUtil.getProjectService().getLastSupportedEndpointVersion();
        // set everything in orgInfo
        
        final String password = IdeTestConstants.DEFAULT_PASSWORD;
        final String endpointServer = IdeTestUtil.getAppServerToHitForTests(false);

        // check if the org needs a namespace
        String nameSpace = null;
        if (orgType == OrgTypeEnum.Namespaced) {
            nameSpace = addNamespaceToOrg(orgId, userName, generateUniqueNamespaceForOrg());
        }
        // create orgInfo
        orgInfo = new OrgInfo(orgId, orgType, userName, password, null,
        		nameSpace, endpointServer, apiVersion, false, true);

        // take a snapshot of what's already in the org, so that it can be used
        // later for checks.
        // orgInfo.setPackageList(createPkgInfoListFromEverythingInOrg(orgInfo));
        orgInfo.setOrgSnapshot(getOrgSnapshot(orgInfo));

        logger.info("Created Org - " + orgInfo.toString());
        return orgInfo;
	}

    /**
     * generates a unique namespace for org
     * 
     * @return unique namespace
     */
    protected String generateUniqueNamespaceForOrg() {
        return "TNmsp" + IdeTestUtil.getRandomString(4);
    }

    @Override
    public IdeOrgCache getOrgCacheInstance() {
        return orgCache;
    }

    public OrgInfo createOrgWithInstalledPkgInIt(OrgTypeEnum destinationOrgType, String addMetaDataFromPath,
            PackageTypeEnum installedPkgType) throws IdeTestException {
        logger.debug("Creating Org With Installed Package in it.");

        OrgInfo orgInfo = null;
        
        //create the request.
        //create a zip file for a package first
        final String packageName = installedPkgType.toString() + generateUniquePackageName();
        IdeTestUtil.MetadataZipper metadataZipper = new IdeTestUtil.MetadataZipper() {

            @Override
            protected IdeTestFileMetadataReplacementUtil getFileReplacementsUtil() {
                IdeTestFileMetadataReplacementUtil replaceUtil = new IdeTestFileMetadataReplacementUtil();
                //server side doesn't need this for deploying, it will deploy zip as unpackaged in src and then create the pkg and then upload and install.
                replaceUtil.add(Constants.PACKAGE_MANIFEST, IdeTestConstants.PACKAGE_FULL_NAME_ELEMENT_WITH_TOKEN, "");
                return replaceUtil;
            }

        };
        byte[] createdZipBytes = metadataZipper.generateZipBytes(addMetaDataFromPath, packageName);
        CreateOrgWithInstalledPackageRequest request = new CreateOrgWithInstalledPackageRequest();
        com.sforce.soap.clienttest.PackageTypeEnum installedPkgTypeEnum = null;
        if(installedPkgType==com.salesforce.ide.test.common.utils.PackageTypeEnum.MANAGED_INSTALLED_PKG){
            installedPkgTypeEnum = com.sforce.soap.clienttest.PackageTypeEnum.MANAGED;
        }else if(installedPkgType==com.salesforce.ide.test.common.utils.PackageTypeEnum.UNMANAGED_INSTALLED_PKG){
            installedPkgTypeEnum = com.sforce.soap.clienttest.PackageTypeEnum.UNMANAGED;
        }
        request.setInstalledPkgType(installedPkgTypeEnum);
        request.setOrgTypeKey(destinationOrgType.getOrgTypeKey());
        request.setZipByteBlob(createdZipBytes);
        request.setPackageNameToSet(packageName);
        
        //send it to the server
        ConnectorConfig c = new ConnectorConfig();
        
        String endpoint = "http://" + IdeTestUtil.getAppServerToHitForTests(false) + "/sfdc/qa/Soap/t/20.0";

		c.setAuthEndpoint(endpoint);
		c.setServiceEndpoint(endpoint);
        c.setManualLogin(false);
        c.setPrettyPrintXml(true);
        c.setTraceMessage(true);
        c.setValidateSchema(true);
        c.setCompression(false);
        c.setReadTimeout(5*60*1000); //give it 5 minutes 
        SoapConnection newConnection;
        CreateOrgWithInstalledPackageResult response = null;
        logger.debug("==========SENDING ORG CREATE WITH INSTALLED PKG REQUEST TO SERVER================");
        try {
            newConnection = Connector.newConnection(c);
             response = newConnection.createOrgWithInstalledPackage(request);
        } catch (ConnectionException e) {
            IdeTestException.wrapAndThrowException("Exception when trying to communicate with testing API on the server",e);
        }
        assert null!=response :"Should have gotten back some result from the server";
        if(!response.isSuccess()){
            IdeTestException.wrapAndThrowException("Org Create with installed Pkg request did not return with a successfull result.");
        }
        //populate orgInfo based on result.
        orgInfo =
                new OrgInfo(response.getOrganizationId(), destinationOrgType, response.getUserName(),
                        IdeTestConstants.DEFAULT_PASSWORD, null, null, IdeTestUtil.getAppServerToHitForTests(false),
                        IdeTestUtil.getProjectService().getLastSupportedEndpointVersion(), false, true);
        orgInfo.addPackageToPackageList(packageName, installedPkgType, addMetaDataFromPath, IdeTestUtil.constructDestructiveChangesPath(addMetaDataFromPath),response.getInstalledPkgNameSpacePrefix());
        logger.debug("Adding org on local org Cache...");
        orgCache.addOrgToCache(destinationOrgType, orgInfo);
        
        return orgInfo;

    }
}
