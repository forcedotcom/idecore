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
package com.salesforce.ide.core.model;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;

public class Org {

    protected String userName = null;
    protected String password = null;
    protected String token = null;
    protected String sessionId = null;  // For internal use
    protected String namespacePrefix = null;
    protected String endpointServer = null;
    protected String endpointApiVersion = null;
    protected String metadataFormatVersion = null;
    protected boolean keepEndpoint = false;
    protected boolean httpsProtocol = true;

    public Org() {
        super();
    }

    public Org(String username, String password, String token, String sessionId, String endpointServer) {
        this.userName = username;
        this.password = password;
        this.token = token;
        this.sessionId = sessionId;
        this.endpointServer = endpointServer;
    }

    public boolean isOrgChange(ForceProject oldCi) {
        return !oldCi.getUserName().equals(getUserName()) || !oldCi.getPassword().equals(getPassword())
                || !oldCi.getToken().equals(getToken()) || !oldCi.getEndpointServer().equals(getEndpointServer())
                || !oldCi.getEndpointServer().equals(getEndpointServer()) || oldCi.isKeepEndpoint() != isKeepEndpoint()
                || !oldCi.getSessionId().equals(getSessionId())
                || oldCi.isHttpsProtocol() != isHttpsProtocol() ? true : false;
    }

    /**
     * @return whether enough details of the user are specified
     */
    public boolean areUserCredentialsSpecified() {
    	return  !Utils.isEmpty(getEndpointServer()) &&
    		(!Utils.isEmpty(getSessionId()) || (!Utils.isEmpty(getUserName()) && !Utils.isEmpty(getPassword())));
    }

    public String getEndpointApiVersion() {
        return endpointApiVersion;
    }

    public void setEndpointApiVersion(String endpointApiVersion) {
        this.endpointApiVersion = endpointApiVersion;
    }

    public String getMetadataFormatVersion() {
        return metadataFormatVersion;
    }

    public void setMetadataFormatVersion(String metadataFormatVersion) {
        this.metadataFormatVersion = metadataFormatVersion;
    }

    public String getEndpointServer() {
        return endpointServer == null ? "" : endpointServer;
    }

    public void setEndpointServer(String endpointServer) {
        this.endpointServer = endpointServer;
    }

    public boolean isKeepEndpoint() {
        return keepEndpoint;
    }

    public void setKeepEndpoint(boolean keepEndpoint) {
        this.keepEndpoint = keepEndpoint;
    }

    public boolean isHttpsProtocol() {
        return httpsProtocol;
    }

    public void setHttpsProtocol(boolean httpsProtocol) {
        this.httpsProtocol = httpsProtocol;
    }

    public String getPassword() {
        return password == null ? "" : password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSessionId() {
    	return this.sessionId == null ? "" : sessionId;
    }

    public void setSessionId(String sessionId) {
    	this.sessionId = sessionId;
    }

    public String getToken() {
        return token == null ? "" : token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserName() {
        return userName == null ? "" : userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }

    public String getLogDisplay() {
        return "user '" + getUserName() + "' at '" + getEndpointServer() + "'";
    }

    public String getFullLogDisplay() {
        StringBuffer strBuff = new StringBuffer("username = '" + getUserName());
        strBuff.append("', endpointServer = '").append(getEndpointServer()).append("/").append(getEndpointApiVersion());
        return strBuff.toString();
    }

    /**
     * Constructs a <code>String</code> with all attributes in name = value format.
     *
     * @return a <code>String</code> representation of this object.
     */
    @Override
    public String toString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("Org ( ").append(super.toString()).append(TAB).append("userName = ").append(this.userName)
                .append(TAB).append("token = ").append(this.token).append(TAB).append("namespacePrefix = ")
                .append(this.namespacePrefix).append(TAB).append("endpointServer = ").append(this.endpointServer)
                .append(TAB).append("endpointApiVersion = ").append(this.endpointApiVersion).append(TAB)
                .append("metadataFormatVersion = ").append(this.metadataFormatVersion).append(TAB)
                .append("keepEndpoint = ").append(this.keepEndpoint).append(TAB).append("httpsProtocol = ")
                .append(this.httpsProtocol).append(TAB).append(" )");
        return retValue.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpointApiVersion == null) ? 0 : endpointApiVersion.hashCode());
        result = prime * result + ((endpointServer == null) ? 0 : endpointServer.hashCode());
        result = prime * result + (httpsProtocol ? 1231 : 1237);
        result = prime * result + (keepEndpoint ? 1231 : 1237);
        result = prime * result + ((metadataFormatVersion == null) ? 0 : metadataFormatVersion.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
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
        Org other = (Org) obj;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (endpointApiVersion == null) {
            if (other.endpointApiVersion != null)
                return false;
        } else if (!endpointApiVersion.equals(other.endpointApiVersion))
            return false;
        if (endpointServer == null) {
            if (other.endpointServer != null)
                return false;
        } else if (!endpointServer.equals(other.endpointServer))
            return false;
        if (httpsProtocol != other.httpsProtocol)
            return false;
        if (keepEndpoint != other.keepEndpoint)
            return false;
        if (metadataFormatVersion == null) {
            if (other.metadataFormatVersion != null)
                return false;
        } else if (!metadataFormatVersion.equals(other.metadataFormatVersion))
            return false;
        return true;
    }

}
