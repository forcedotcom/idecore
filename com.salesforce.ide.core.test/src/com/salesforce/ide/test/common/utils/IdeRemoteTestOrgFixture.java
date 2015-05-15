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

import junit.framework.Assert;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.test.common.utils.IdeOrgCache.OrgInfo;

/**
 * concrete org fixture. contains implementations for orgs on remote servers. The Org Cache here needs to be revisited.
 * Basically the org cache is a mapping of org type to orgInfo. In case of local org, it makes sense 'cos we can request
 * which org to create. But for remote orgs, we need to mention what org type we're using explicitly.
 *
 * @author ssasalatti
 */
public class IdeRemoteTestOrgFixture extends IdeOrgFixture {
    private static final Logger logger = Logger.getLogger(IdeRemoteTestOrgFixture.class);

    IdeOrgCache orgCache = IdeRemoteOrgCache.getInstance();

    /*
     * (non-Javadoc)
     *
     * @see com.salesforce.ide.test.common.utils.IdeOrgFixture#getOrg(com.salesforce.ide.test.common.utils.OrgTypeEnum)
     */
    @Override
    //get org will  get the org based on the entries in test.properties. The entries in test.properties are defined by prefix specified in the annotation.
    //if the user calls this method outside of the framework( i.e. explicitly) and doesn't provide a prefix, then one of the standard test.property entries is used.
    //Cache is always overwritten . See IdeRemoteOrgCache.java
    public OrgInfo getOrg(OrgTypeEnum orgType) throws IdeTestException {
        String prefix = System.getProperty(IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX);
        if (IdeTestUtil.isEmpty(prefix)
                || prefix.toLowerCase().equalsIgnoreCase(IdeTestConstants.SYS_PROP_CUSTOM_ORG_PREFIX_VALUE_LOCAL))
            prefix = orgType.toString();
        return getOrg(orgType, prefix);
    }

    /**
     * Can request an org based on a specific prefix in test.properties. The orgType will be flagged as Custom. OrgCache
     * Will NOT be updated.
     *
     * @param prefix
     * @return
     * @throws IdeTestException
     */
    public OrgInfo getOrg(String prefix) throws IdeTestException {
        String[] orgEntries = resolvePrefix(prefix);
        return getOrgWorker(OrgTypeEnum.Custom, orgEntries[0], orgEntries[1], orgEntries[2]);
    }

    /**
     * user can request an org based on prefix in test.properties and force update the cache and set that value for the
     * orgType key.
     *
     * @param orgType
     * @param prefix
     * @return
     * @throws IdeTestException
     */
    public OrgInfo getOrg(OrgTypeEnum orgType, String prefix) throws IdeTestException {
        String[] orgEntries = resolvePrefix(prefix);
        return getOrg(orgType, orgEntries);
    }

    /**
     * This should be a protected method. Updates the Cache!
     *
     * @param orgType
     * @param params
     *            see resolvePrefix()
     * @return
     * @throws IdeTestException
     */
    public OrgInfo getOrg(OrgTypeEnum orgType, final String... params)
            throws IdeTestException {
        logger.debug("Setting up org on remote server...");
        OrgInfo orgInfo = orgCache.getOrgInfoFromCache(orgType);
        //if nothing was found in the cache then only Developer orgtype should be allowed.
        if (IdeTestUtil.isEmpty(orgInfo)) {
            orgInfo = getOrgWorker(orgType, params);
            Assert.assertNotNull("orgInfo cannot be null!", orgInfo);
            orgCache.addOrgToCache(orgType, orgInfo);
        }
        return orgInfo;
    }

    /**
     * returns an array of strings that are used for getting the org info.
     *
     * @param prefix
     * @return
     * @throws IdeTestException
     */
    private String[] resolvePrefix(String prefix) throws IdeTestException {
        if (IdeTestUtil.isEmpty(prefix))
            IdeTestException.wrapAndThrowException("prefix to be used for entries in test.properties is empty");

        prefix += ".";

        final String endpointServer =
                ConfigProps.getInstance().getProperty(
                    prefix + IdeTestConstants.TEST_PROPERTIES_TEST_AGAINST_INSTANCE_SUFFIX_ENDPOINT);
        final String userName =
                ConfigProps.getInstance().getProperty(
                    prefix + IdeTestConstants.TEST_PROPERTIES_TEST_AGAINST_INSTANCE_SUFFIX_USERNAME);
        final String password =
                ConfigProps.getInstance().getProperty(
                    prefix + IdeTestConstants.TEST_PROPERTIES_TEST_AGAINST_INSTANCE_SUFFIX_PASSWD);
        final String namespace =
                ConfigProps.getInstance().getProperty(
                    prefix + IdeTestConstants.TEST_PROPERTIES_TEST_AGAINST_INSTANCE_SUFFIX_NAMESPACE);

        return new String[] { endpointServer, userName, password, namespace };
    }

    /**
     * sets up org info by retrieving entries from test.properties and connecting to that org. test.properties needs to
     * have entries in a set format for this to work.
     *
     * <pre>
     * ex: for this org on blitz02, note the entries.
     * blitz.de.endpoint = na1-blitz02.soma.salesforce.com
     * blitz.de.username = scratch@ideAutoTest_de_na1.com
     * blitz.de.password = 123456
     * </pre>
     *
     * "blitz.de" should be the prefix to search entries on. the method will then construct the endpoint, username and
     * password property keys. the prefix should be specified in the annotation for the test, as it is set in the system
     * property during setup.
     *
     * @param orgType
     * @param params
     * @return
     * @throws IdeTestException
     */
    private OrgInfo getOrgWorker(final OrgTypeEnum orgType, final String... params) throws IdeTestException {
        if (IdeTestUtil.isEmpty(params))
            IdeTestException
                    .wrapAndThrowException("Cannot setup a remote org if the params aren't specified(username, passwd, endpointServer et. al.)");
        if (params.length < 3)
            IdeTestException
                    .wrapAndThrowException("Cannot setup a remote org if the minimum params aren't specifiedin the order (enpdpointserver, username, passwd)");

        OrgInfo orgInfo;
        final String apiVersion = IdeTestUtil.getProjectService().getLastSupportedEndpointVersion();
        final String userName = params[1];
        final String password = params[2];
        final String endpointServer = params[0];
        final String namespace = (params.length > 3) ? params[3] : null;

        // get the org id for this org
        ForceProject toolkitProject = new ForceProject(userName, password, null, null, endpointServer);
        toolkitProject.setHttpsProtocol(true);
        toolkitProject.setEndpointApiVersion(apiVersion);
        if (IdeTestUtil.isNotEmpty(namespace))
            toolkitProject.setNamespacePrefix(namespace);
        Connection conn;
        String orgId = null;
        try {
            conn = IdeTestUtil.getConnectionFactory().getConnection(toolkitProject);
            conn.login();
            orgId = conn.getOrgId();
        } catch (ForceConnectionException e) {
            StringBuffer _errMsg =
                    new StringBuffer(
                            "Connection Exception.Credentials ok? Maybe the server was down or was being rebuilt.Tried to Connect to ")
                            .append(endpointServer).append(" with username ").append(userName).append(" and password ")
                            .append(password);
            throw IdeTestException.getWrappedException(_errMsg.toString(), e);
        } catch (ForceProjectException e) {
            throw IdeTestException.getWrappedException("Force Project Exception", e);
        } catch (InsufficientPermissionsException e) {
            throw IdeTestException.getWrappedException(e.getMessage(), e);
        }
        // create orgInfo
        orgInfo =
                new OrgInfo(orgId, orgType, userName, password, null, namespace, endpointServer, apiVersion, true, true);
        //take a snapshot of what's already in the org, so that it can be used later for checks.
        orgInfo.setOrgSnapshot(getOrgSnapshot(orgInfo));

        logger.debug("Using Org: " + orgInfo.toString());
        return orgInfo;
    }

    @Override
    public IdeOrgCache getOrgCacheInstance() {
        return orgCache;
    }

}
