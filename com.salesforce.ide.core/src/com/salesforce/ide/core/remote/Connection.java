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
package com.salesforce.ide.core.remote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.salesforce.ide.api.metadata.MetadataDebuggingInfoHandler;
import com.salesforce.ide.core.ForceIdeCorePlugin;
import com.salesforce.ide.core.internal.preferences.proxy.IProxy;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.sforce.soap.apex.SoapConnection;
import com.sforce.soap.partner.sobject.wsc.SObject;
import com.sforce.soap.partner.wsc.CallOptions_element;
import com.sforce.soap.partner.wsc.DeleteResult;
import com.sforce.soap.partner.wsc.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.wsc.DescribeSObjectResult;
import com.sforce.soap.partner.wsc.GetUserInfoResult;
import com.sforce.soap.partner.wsc.LoginResult;
import com.sforce.soap.partner.wsc.PartnerConnection;
import com.sforce.soap.partner.wsc.QueryResult;
import com.sforce.soap.partner.wsc.SaveResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * <p>
 * Encapsulates Salesforce.com connection functionality.
 * </p>
 * <p>
 * There are four separate WSDLs that we are consuming and each one has a different endpoint:
 * <ul>
 * <li>Partner</li>
 * <li>Metadata</li>
 * <li>Tooling</li>
 * <li>Apex - should be deprecated soon</li>
 * <ul>
 * <p>
 * 
 * @author cwall
 */
public class Connection {

    private static final Logger logger = Logger.getLogger(Connection.class);

    private final String partnerEndpointChar = "/u/";
    private final String apexEndpointChar = "/s/";
    private final String metadataEndpointChar = "/m/";
    private final String toolingEndpointChar = "/T/";
    private final String platform = "Eclipse";

    //value is critical for Eclipse-only API support
    private final String clientIdName = "apex_eclipse";

    // values are injected
    private String application = Constants.PLUGIN_NAME;
    private int loginTimeoutMinutes = 20; // relogin every so many minutes to proactively avoid session timeout
    private PartnerConnection partnerConnection;
    private final ConnectorConfig connectorConfig;
    private final ConnectorConfig metadataConnectorConfig;
    private final ConnectorConfig toolingConnectorConfig;
    private ForceProject forceProject;
    private long nextLogin = 0;
    private boolean debugExceptions;
    private String metadataServerUrl;
    private String toolingServerUrl;
    private String orgId;
    private String profileId;
    private SalesforceEndpoints salesforceEndpoints;

    public Connection() {
        super();
        connectorConfig = new ConnectorConfig();
        metadataConnectorConfig = new ConnectorConfig();
        toolingConnectorConfig = new ConnectorConfig();
    }

    public ForceProject getForceProject() {
        return forceProject;
    }

    public void setForceProject(ForceProject forceProject) {
        this.forceProject = forceProject;
    }

    public String getPartnerEndpointChar() {
        return partnerEndpointChar;
    }

    public String getApexEndpointChar() {
        return apexEndpointChar;
    }

    public String getMetadataEndpointChar() {
        return metadataEndpointChar;
    }

    public String getToolingEndpointChar() {
        return toolingEndpointChar;
    }

    public String getClientId() {
        String version = Constants.EMPTY_STRING;
        try {
            version = Utils.removeServiceLevelFromPluginVersion(ForceIdeCorePlugin.getBundleVersion());
        } catch (Exception e) {
            version = salesforceEndpoints.getDefaultApiVersion();
        }

        return getClientIdName() + "/" + version;
    }

    private String getClientIdName() {
        return clientIdName;
    }

    public String getPlatform() {
        return platform;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public int getLoginTimeoutMinutes() {
        return loginTimeoutMinutes;
    }

    public void setLoginTimeoutMinutes(int loginTimeoutMinutes) {
        this.loginTimeoutMinutes = loginTimeoutMinutes;
    }

    public ConnectorConfig getConnectorConfig() {
        return connectorConfig;
    }

    public ConnectorConfig getMetadataConnectorConfig() {
        return metadataConnectorConfig;
    }
    
    public ConnectorConfig getToolingConnectorConfig() {
        return toolingConnectorConfig;
    }

    public boolean isDebugExceptions() {
        return debugExceptions;
    }

    public void setDebugExceptions(boolean debugExceptions) {
        this.debugExceptions = debugExceptions;
    }

    public String getMetadataServerUrl() {
        return metadataServerUrl;
    }

    public void setMetadataServerUrl(String metadataServerUrl) {
        this.metadataServerUrl = metadataServerUrl;
    }

    public String getToolingServerUrl() {
        return toolingServerUrl;
    }

    public void setToolingServerUrl(String toolingServerUrl) {
        this.toolingServerUrl = toolingServerUrl;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public void setOrgId(LoginResult loginResult) {
        if (loginResult.getUserInfo() == null) {
            return;
        }
        this.orgId = loginResult.getUserInfo().getOrganizationId();
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public void setProfileId(LoginResult loginResult) {
        if (loginResult.getUserInfo() == null) {
            return;
        }
        this.profileId = loginResult.getUserInfo().getProfileId();
    }

    public SalesforceEndpoints getSalesforceEndpoints() {
        return salesforceEndpoints;
    }

    public void setSalesforceEndpoints(SalesforceEndpoints salesforceEndpoints) {
        this.salesforceEndpoints = salesforceEndpoints;
    }

    /**
     * Login to the organization using the partner connection, and setup the metadataUrl to use later on.
     * 
     * @throws ForceConnectionException
     * @throws InsufficientPermissionsException
     */
    public void login() throws InvalidLoginException, ForceConnectionException, InsufficientPermissionsException {
        initializeConnectorConfig(connectorConfig);
        initializeConnectorConfig(metadataConnectorConfig);
        initializeConnectorConfig(toolingConnectorConfig);

        try {
            if (getPartnerConnection() == null) {
                ForceExceptionUtils.throwNewConnectionException("Unable to obtain Force.com connection.  "
                        + "Please check the supplied username and password and server availability.");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Successfully created new partner connection");
                logger.debug("Logging in...");
            }

            if (Utils.isEmpty(connectorConfig.getSessionId())) {
                LoginResult loginResult =
                        getPartnerConnection().login(connectorConfig.getUsername(), connectorConfig.getPassword());

                if (loginResult == null) {
                    logger.warn("Login result is null");
                    return;
                }

                logLoginResult(loginResult);

                // record session and org
                getPartnerConnection().setSessionHeader(loginResult.getSessionId());
                setOrgId(loginResult);

                if (!forceProject.isKeepEndpoint()) {
                    getPartnerConnection().getConfig().setServiceEndpoint(loginResult.getServerUrl());
                    connectorConfig.setAuthEndpoint(getPartnerConnection().getConfig().getAuthEndpoint());
                    connectorConfig.setServiceEndpoint(getPartnerConnection().getConfig().getServiceEndpoint());
                    // For some reason, the metadata server gets special treatment in the Partner WSDL and we can obtain it directly
                    setMetadataServerUrl(loginResult.getMetadataServerUrl());
                    setToolingServerUrl(getToolingServiceEndpoint(getPartnerConnection().getConfig().getServiceEndpoint()));
                } else {
                    String metadataServerUrl =
                            changeServer(loginResult.getMetadataServerUrl(), forceProject.getEndpointServer(), forceProject.isHttpsProtocol());
                    setMetadataServerUrl(metadataServerUrl);
                    String serverUrl = changeServer(loginResult.getServerUrl(), forceProject.getEndpointServer(), forceProject.isHttpsProtocol());
                    setToolingServerUrl(getToolingServiceEndpoint(serverUrl));
                }
            } else {
                // With a sessionId, use the getUserInfo to obtain the relevant information, then construct the other endpoints
                GetUserInfoResult userInfoResult = getPartnerConnection().getUserInfo();
                setProfileId(userInfoResult.getProfileId());
                setOrgId(userInfoResult.getOrganizationId());
                setMetadataServerUrl(getMetadataServiceEndpoint(getPartnerConnection().getConfig().getServiceEndpoint()));
                setToolingServerUrl(getToolingServiceEndpoint(getPartnerConnection().getConfig().getServiceEndpoint()));
            }

            // set for re-login to handle expired connections
            GregorianCalendar now = new GregorianCalendar();
            now.add(Calendar.MINUTE, getLoginTimeoutMinutes());
            nextLogin = now.getTimeInMillis();

            if (logger.isDebugEnabled()) {
                logger.debug("Next login will be at " + (new Date(nextLogin)).toString() + " (~"
                        + getLoginTimeoutMinutes() + " minutes)");
            }

        } catch (ConnectionException e) {
            ForceExceptionUtils.handleConnectionException(this, e);
        }

        setMetdataEndpointAndMessageHandler();
        setToolingEndPoint();
    }

    private static String changeServer(String url, String server, boolean isHttps) {
        if (Utils.isEmpty(url) || Utils.isEmpty(server) || !url.startsWith(Constants.HTTP)) {
            return url;
        }
        return (isHttps ? "https://" : "http://" ) + server + url.substring(url.indexOf("/", 9));
    }

    private void initializeConnectorConfig(ConnectorConfig connectorConfig) {
        if (forceProject == null) {
            throw new IllegalArgumentException("Object containing connection details cannot be null");
        }

        connectorConfig.setCompression(true);
        if (Utils.isEmpty(forceProject.getSessionId())) {
            connectorConfig.setManualLogin(true);
            connectorConfig.setUsername(forceProject.getUserName());
            connectorConfig.setPassword(forceProject.getPassword() + forceProject.getToken());
        } else {
            connectorConfig.setManualLogin(false);
            connectorConfig.setSessionId(forceProject.getSessionId());
        }

        connectorConfig.setReadTimeout(forceProject.getReadTimeoutMillis());
        connectorConfig.setConnectionTimeout(forceProject.getReadTimeoutMillis());

        if (Utils.isNotEmpty(forceProject.getEndpointServer())) {
            final String endpointUrl =
                    salesforceEndpoints.getFullEndpointUrl(forceProject.getEndpointServer(),
                        forceProject.isHttpsProtocol());
            connectorConfig.setAuthEndpoint(endpointUrl);
            connectorConfig.setServiceEndpoint(connectorConfig.getAuthEndpoint());
        } 
    }

    public void setProxy(IProxy proxy) {
        if (connectorConfig != null) {
            setProxyOnConnectorConfig(proxy, connectorConfig);
        }
        if (metadataConnectorConfig != null) {
            setProxyOnConnectorConfig(proxy, metadataConnectorConfig);
        }
        if (toolingConnectorConfig != null) {
            setProxyOnConnectorConfig(proxy, toolingConnectorConfig);
        }
    }

    private static void setProxyOnConnectorConfig(IProxy proxy, ConnectorConfig connectorConfig) {
        final String proxyHost = proxy.getProxyHost();
        final String proxyPort = proxy.getProxyPort();
        int portNumber = Utils.isNotEmpty(proxyPort) ? Integer.parseInt(proxyPort) : -1;
        if (portNumber >= 0 && portNumber <= 65535) {
            connectorConfig.setProxy(proxyHost, Integer.parseInt(proxyPort));
            if (logger.isDebugEnabled()) {
                logger.debug("Set proxy '" + connectorConfig.getProxy().address().toString());
            }
        }
        connectorConfig.setProxyUsername(proxy.getProxyUser());
        connectorConfig.setProxyPassword(proxy.getProxyPassword());
    }

    private static void logLoginResult(LoginResult loginResult) {
        if (loginResult == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Got the following user info from login:\n" + loginResult.getUserInfo().toString()
                    + "'\n serverUrl='" + loginResult.getServerUrl() + "'");
        }
    }

    /**
     * Create a new partner connection and passes some call options identifying ourselves as Eclipse
     * 
     * @throws ForceConnectionException
     */
    private void createPartnerConnection() throws ForceConnectionException {
        try {
            partnerConnection = new PartnerConnection(connectorConfig);
            if (logger.isDebugEnabled()) {
                StringBuffer strBuff = new StringBuffer("Created new partner connection with the following config:");
                strBuff.append("\n username = '").append(connectorConfig.getUsername())
                        .append("', service endpoint = '").append(connectorConfig.getServiceEndpoint())
                        .append("', conn timeout = ")
                        .append(Utils.timeoutToSecs(connectorConfig.getConnectionTimeout()))
                        .append(", read timeout = ").append(Utils.timeoutToSecs(connectorConfig.getReadTimeout()))
                        .append(", proxy = ")
                        .append(connectorConfig.getProxy() != null ? connectorConfig.getProxy().toString() : "n/a");
                logger.debug(strBuff.toString());
            }
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwNewConnectionException(this, e);
        }

        setClientCallOptions();
    }

    public void setClientCallOptions() throws ForceConnectionException {
        getPartnerConnection().setCallOptions(
            getClientId(),
            null,
            false,
            null,
            debugExceptions,
            getPlatform(),
            getApplication(),
            null,
            null);
        if (logger.isDebugEnabled()) {
            logger.debug("Set IDE call options");
        }
    }

    /**
     * Create a new partner connection and passes some call options identifying ourselves as Eclipse
     * 
     * @throws ForceConnectionException
     */
    public void setNonCallOptions() throws ForceConnectionException {
        partnerConnection.setCallOptions(
            "",
            null,
            false,
            null,
            debugExceptions,
            getPlatform(),
            getApplication(),
            null,
            null);
        if (logger.isDebugEnabled()) {
            logger.debug("Set non-IDE call options");
        }
    }

    public void relogin() throws InvalidLoginException, ForceConnectionException, InsufficientPermissionsException {
        if (logger.isInfoEnabled()) {
            logger.info("Re-logging in for connection:\n " + getLogDisplay());
        }

        partnerConnection = null;
        login();
    }

    public String getServerUrlRoot() {
        if (connectorConfig == null) {
            return Constants.EMPTY_STRING;
        }

        String url = connectorConfig.getServiceEndpoint();
        if (Utils.isNotEmpty(url)) {        	
            url = getRootUrlWithProtocol(url);
        }
        return url;
    }

    /**
     * @param url For example, http://na1.salesforce.com/home/home.jsp
     * @return For example, http://na1.salesforce.com/
     */
	private static String getRootUrlWithProtocol(String url) {
		url = url.substring(0, url.indexOf("/", url.indexOf("//") + 2));
		return url;
	}

    public String getWebUrlRoot() {
        if (connectorConfig == null) {
            return Constants.EMPTY_STRING;
        }

        String url = getServerUrlRoot();
        if (Utils.isNotEmpty(url)) {
            url = url.replace(Constants.API_URL_PART, "");
        }

        return url;
    }

    public String getServerName() {
        if (connectorConfig == null) {
            return Constants.EMPTY_STRING;
        }

        String endpoint = connectorConfig.getServiceEndpoint();
        return Utils.getServerNameFromUrl(endpoint);
    }

    public PartnerConnection getPartnerConnection() throws ForceConnectionException {
        if (partnerConnection == null) {
            createPartnerConnection();
        }
        return partnerConnection;
    }

    public ForceProject getConnectionInfo() {
        return forceProject;
    }

    public long getNextLogin() {
        return nextLogin;
    }

    public void setNextLogin(long nextLogin) {
        this.nextLogin = nextLogin;
    }

    public String getUsername() {
        String userName = null;
        if (connectorConfig != null) {
            userName = connectorConfig.getUsername();
        }
        return userName;
    }

    public boolean isStale() {
        if (getNextLogin() < (new GregorianCalendar()).getTimeInMillis()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Session will expire soon - will proactively re-loing");
            }
            return true;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Session still active - next auto-login will be at "
                        + (new Date(getNextLogin())).toString());
            }
            return false;
        }
    }

    public void setCallOptions(SoapConnection apexConnection, String clientId) throws ForceConnectionException {
        apexConnection.setCallOptions(clientId, null, null);
    }

    public void setTimeoutMillis(int timeoutMillis) {
        if (partnerConnection != null && partnerConnection.getConfig() != null && forceProject != null) {
            partnerConnection.getConfig().setReadTimeout(timeoutMillis);
            if (logger.isDebugEnabled()) {
                logger.debug("Set timeout to " + timeoutMillis + " ms");
            }
        }
    }

    public int getReadTimeout() {
        return partnerConnection != null && partnerConnection.getConfig() != null ? partnerConnection.getConfig()
                .getReadTimeout() / Constants.SECONDS_TO_MILISECONDS : -1;
    }

    public String getServiceEndpoint() {
        return partnerConnection.getConfig().getServiceEndpoint();
    }

    public String getSessionId() {
        String sessionId = null;
        try {
            sessionId = getSessionId(false);
        } catch (Exception e) {
            // won't be thrown if false is sent
            logger.warn("Unable to get session id: " + ForceExceptionUtils.getRootCauseMessage(e));
        }
        return sessionId;
    }

    public String getSessionId(boolean login) throws InvalidLoginException, ForceConnectionException,
            InsufficientPermissionsException {
        if (partnerConnection == null) {
            logger.warn("Unable to get session id - partner connection is null");
            return null;
        }

        String sessionId = partnerConnection.getSessionHeader().getSessionId();
        if (Utils.isEmpty(sessionId) && login) {
            relogin();
            sessionId = getSessionId(false);
        }
        return sessionId;
    }

    public String getApexServiceEndpoint(String serviceEndpoint) {
        if (Utils.isEmpty(serviceEndpoint)) {
            return null;
        }
        return serviceEndpoint.replace(getPartnerEndpointChar(), getApexEndpointChar());
    }

    public String getMetadataServiceEndpoint(String serviceEndpoint) {
        if (Utils.isEmpty(serviceEndpoint)) {
            return null;
        }
        return serviceEndpoint.replace(getPartnerEndpointChar(), getMetadataEndpointChar());
    }

    public String getToolingServiceEndpoint(String serviceEndpoint) {
        if (Utils.isEmpty(serviceEndpoint)) {
            return null;
        }
        return serviceEndpoint.replace(getPartnerEndpointChar(), getToolingEndpointChar());
    }

    //   R E M O T E   O P E R A T I O N S

    public SaveResult[] create(SObject[] sObjects) throws ForceConnectionException, InsufficientPermissionsException {
        SaveResult[] saveResults = null;
        try {
            logConnection();

            saveResults = getPartnerConnection().create(sObjects);
        } catch (ConnectionException e) {
            ForceExceptionUtils.handleConnectionException(this, e);
        }
        return saveResults;
    }

    public DeleteResult[] delete(SObject[] sObjects) throws ForceConnectionException, InsufficientPermissionsException {
        DeleteResult[] results = null;
        if (Utils.isNotEmpty(sObjects)) {
            String[] ids = new String[sObjects.length];
            for (int i = 0; i < sObjects.length; i++) {
                String id = sObjects[i].getId();
                if (Utils.isNotEmpty(id)) {
                    ids[i] = id;
                }
            }
            results = delete(ids);
        }
        return results;
    }

    public DeleteResult[] delete(String[] ids) throws ForceConnectionException, InsufficientPermissionsException {
        DeleteResult[] deleteResults = null;
        try {
            logConnection();

            deleteResults = getPartnerConnection().delete(ids);
        } catch (ConnectionException e) {
            ForceExceptionUtils.handleConnectionException(this, e);
        }
        return deleteResults;
    }

    /**
     * Equivalent of a DescribeGlobal api call without our client id set.
     * 
     * @return
     * @throws ForceConnectionException
     * @throws InsufficientPermissionsException
     */
    public String[] retrieveTypes() throws ForceConnectionException, InsufficientPermissionsException {
        return retrieveTypes(false);
    }

    /**
     * Equivalent of a describeGlobal() api call
     * 
     * @param withClientId
     *            . whether or not to use our eclipse specific client id.
     * @return
     * @throws ForceConnectionException
     * @throws InsufficientPermissionsException
     */
    public String[] retrieveTypes(boolean withClientId) throws ForceConnectionException,
            InsufficientPermissionsException {
        List<String> types = new ArrayList<>();

        if (!withClientId) {
            setNonCallOptions();
        }

        try {
            logConnection();
            DescribeGlobalSObjectResult[] sobjects = getPartnerConnection().describeGlobal().getSobjects();
            for (DescribeGlobalSObjectResult sobject : sobjects) {
                types.add(sobject.getName());
            }

        } catch (ConnectionException e) {
            ForceExceptionUtils.handleConnectionException(this, e);
        } finally {
            setClientCallOptions();
        }
        return types.toArray(new String[types.size()]);
    }

    public DescribeSObjectResult describeSObject(String sComponentType) throws ForceConnectionException,
            InsufficientPermissionsException {
        return describeSObject(sComponentType, true);
    }

    public DescribeSObjectResult describeSObject(String sComponentType, boolean withClientId)
            throws ForceConnectionException, InsufficientPermissionsException {
        DescribeSObjectResult result = null;

        if (!withClientId) {
            setNonCallOptions();
        }

        try {
            logConnection();

            result = getPartnerConnection().describeSObject(sComponentType);
        } catch (ConnectionException e) {
            ForceExceptionUtils.handleConnectionException(this, e);
        } finally {
            setClientCallOptions();
        }
        return result;
    }

    public DescribeSObjectResult[] describeSObjects(String[] sComponentType) throws ForceConnectionException,
            InsufficientPermissionsException {
        return describeSObjects(sComponentType, true);
    }

    public DescribeSObjectResult[] describeSObjects(String[] sComponentType, boolean withClientId)
            throws ForceConnectionException, InsufficientPermissionsException {

        if (!withClientId) {
            setNonCallOptions();
        }

        logConnection();

        DescribeSObjectResult[] result;
        try {
            result = null;
            if (Utils.isNotEmpty(sComponentType)) {
                // we're limited to the number of types we can send
                if (sComponentType.length > 100) {
                    List<DescribeSObjectResult> describeSObjectResultList = new ArrayList<>();
                    DescribeSObjectResult[] tmpResult = null;
                    List<String> sComponentTypeList = Arrays.asList(sComponentType);
                    int numOfTimes = 1 + (sComponentTypeList.size() / 100 > 0 ? sComponentTypeList.size() / 100 : 0);
                    int start = 0;
                    int end = 100;
                    for (int i = 0; i < numOfTimes; i++) {
                        List<String> tmpSComponentTypeList = null;
                        try {
                            tmpSComponentTypeList = sComponentTypeList.subList(start, end);
                        } catch (IndexOutOfBoundsException e) {
                            tmpSComponentTypeList = sComponentTypeList.subList(start, sComponentTypeList.size());
                        }
                        tmpResult = describeSObjects(tmpSComponentTypeList);
                        describeSObjectResultList.addAll(Arrays.asList(tmpResult));
                        start += 100;
                        end += 100;
                    }
                    result =
                            describeSObjectResultList.toArray(new DescribeSObjectResult[describeSObjectResultList
                                    .size()]);
                } else {
                    try {
                        result = getPartnerConnection().describeSObjects(sComponentType);
                    } catch (ConnectionException e) {
                        ForceExceptionUtils.handleConnectionException(this, e);
                    }
                }
            }
        } finally {
            setClientCallOptions();
        }

        return result;
    }

    private DescribeSObjectResult[] describeSObjects(List<String> subSComponentTypeList)
            throws ForceConnectionException, InsufficientPermissionsException {
        DescribeSObjectResult[] result = null;
        try {
            result =
                    getPartnerConnection().describeSObjects(
                        subSComponentTypeList.toArray(new String[subSComponentTypeList.size()]));
        } catch (ConnectionException e) {
            ForceExceptionUtils.handleConnectionException(this, e);
        }
        return result;
    }

    public QueryResult query(String queryString) throws ForceConnectionException, InsufficientPermissionsException {
        return query(queryString, true);
    }

    public QueryResult query(String queryString, boolean withClientId) throws ForceConnectionException,
            InsufficientPermissionsException {
        QueryResult result = null;

        if (!withClientId) {
            setNonCallOptions();
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Execution query statement:\n '" + queryString + "'");
                logConnection();
            }
            result = getPartnerConnection().query(queryString);

            if (result != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Returned [" + result.getSize() + "] records");
                }
            }

        } catch (ConnectionException e) {
            ForceExceptionUtils.handleConnectionException(this, e);
        } finally {
            setClientCallOptions();
        }
        return result;
    }

    public SObject[] retrieve(String fieldList, String sComponentType, String[] ids) throws ForceConnectionException,
            InsufficientPermissionsException {
        SObject[] sobjects = null;
        try {
            logConnection();

            sobjects = getPartnerConnection().retrieve(fieldList, sComponentType, ids);
        } catch (ConnectionException e) {
            ForceExceptionUtils.handleConnectionException(this, e);
        }
        return sobjects;
    }

    public SaveResult[] update(SObject[] sObjects) throws ForceConnectionException, InsufficientPermissionsException {
        SaveResult[] saveResults = null;
        try {
            logConnection();

            saveResults = getPartnerConnection().update(sObjects);
        } catch (ConnectionException e) {
            ForceExceptionUtils.handleConnectionException(this, e);
        }
        return saveResults;
    }

    public String getLogDisplay() {
        if (connectorConfig == null) {
            return "n/a";
        }
        StringBuffer strBuff = new StringBuffer("username='");
        strBuff.append(connectorConfig.getUsername()).append("', endpoint='")
                .append(connectorConfig.getServiceEndpoint()).append("', orgid='").append(getOrgId())
                .append("', conn timeout=").append(Utils.timeoutToSecs(connectorConfig.getConnectionTimeout()))
                .append(", read timeout=").append(Utils.timeoutToSecs(connectorConfig.getReadTimeout()));
        return strBuff.toString();
    }

    private void logConnection() {
        if (logger.isDebugEnabled()) {
            if (partnerConnection == null) {
                return;
            }

            ConnectorConfig config = partnerConnection.getConfig();
            StringBuffer strBuff = new StringBuffer("Executing connection with config:\n");
            strBuff.append(" username='" + config.getUsername() + "'").append("\n serviceEndpoint='")
                    .append(config.getServiceEndpoint() + "'").append("'")
                    .append("\n authPoint='" + config.getAuthEndpoint() + "'").append(config.getAuthEndpoint())
                    .append("'").append("\n conn timeout='").append(config.getConnectionTimeout()).append("'")
                    .append("\n read timeout='").append(config.getReadTimeout()).append("'").append("\n proxy='")
                    .append(config.getProxy() != null ? config.getProxy().toString() + "'" : "n/a").append("'")
                    .append("\n metadataUrl='").append(getMetadataServerUrl()).append("'").append("\n orgId='")
                    .append(getOrgId()).append("'");

            Map<String, String> headers = config.getHeaders();
            if (Utils.isNotEmpty(headers)) {
                Set<String> keys = headers.keySet();
                for (String key : keys) {
                    strBuff.append("\n ").append(key).append("='").append(headers.get(key)).append("'");
                }
            }

            CallOptions_element callOptions = partnerConnection.getCallOptions();
            if (callOptions != null) {
                strBuff.append("\n clientId='" + callOptions.getClient() + "'")
                        .append("\n clientLog='" + callOptions.getClientLog() + "'")
                        .append("\n defaultNamespace='" + callOptions.getDefaultNamespace() + "'")
                        .append("\n remoteApplication='" + callOptions.getRemoteApplication() + "'");
            } else {
                strBuff.append("\n call options = n/a");
            }

            logger.debug(strBuff.toString());
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((connectorConfig == null) ? 0 : connectorConfig.hashCode());
        result = prime * result + ((forceProject == null) ? 0 : forceProject.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Connection other = (Connection) obj;
        if (getSessionId() == null) {
            if (other.getSessionId() != null)
                return false;
        } else if (!getSessionId().equals(other.getSessionId()))
            return false;
        if (connectorConfig.getUsername() == null) {
            if (other.connectorConfig.getUsername() != null)
                return false;
        } else if (!connectorConfig.getUsername().equals(other.connectorConfig.getUsername()))
            return false;
        if (getOrgId() == null) {
            if (other.getOrgId() != null)
                return false;
        } else if (!getOrgId().equals(other.getOrgId()))
            return false;
        if (forceProject == null
                || (forceProject.getProject() == null && Utils.isEmpty(forceProject.getProject().getName()))) {
            if (other.forceProject != null && forceProject.getProject() != null
                    && Utils.isNotEmpty(forceProject.getProject().getName()))
                return false;
        } else if (!forceProject.getProject().getName().equals(other.forceProject.getProject().getName()))
            return false;
        return true;
    }

    void setMetdataEndpointAndMessageHandler() {
        metadataConnectorConfig.setServiceEndpoint(metadataServerUrl);
        metadataConnectorConfig.addMessageHandler(new MetadataDebuggingInfoHandler());
    }

    void setToolingEndPoint() {
        toolingConnectorConfig.setServiceEndpoint(toolingServerUrl);
        toolingConnectorConfig.setAuthEndpoint(toolingServerUrl);
    }
    
    public boolean connectsViaSessionId(){
    	return  getForceProject() != null &&
    		    getForceProject().getSessionId() != null &&
    		   !getForceProject().getSessionId().isEmpty();
    }
}
