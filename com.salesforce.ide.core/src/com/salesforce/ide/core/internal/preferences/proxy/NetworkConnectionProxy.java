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
package com.salesforce.ide.core.internal.preferences.proxy;

import java.util.Date;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.net.proxy.IProxyData;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;

/**
 * Responsible for obtaining proxy settings from Eclipse's Network Connection preferences
 * 
 * @author cwall
 */
public class NetworkConnectionProxy extends AbstractProxy {

    private static final Logger logger = Logger.getLogger(NetworkConnectionProxy.class);

    private IProxyData[] proxyData = null;
    private String[] excludedHosts = null;
    private boolean proxiesEnabled = false;

    //   C O N S T R U C T O R S
    public NetworkConnectionProxy() {}

    public NetworkConnectionProxy(boolean proxiesEnabled, IProxyData[] proxyData, String[] excludedHosts) {
        this.proxiesEnabled = proxiesEnabled;
        this.proxyData = proxyData;
        this.excludedHosts = excludedHosts;
        this.lastUpdated = (new Date()).getTime();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.model.IProxy#getProxyData()
    */
    public IProxyData[] getProxyData() {
        return proxyData;
    }

    public void setProxyData(IProxyData[] proxyData) {
        this.proxyData = proxyData;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.model.IProxy#hasProxyData()
    */
    public boolean hasProxyData() {
        return Utils.isNotEmpty(proxyData);
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.model.IProxy#isProxiesEnabled()
    */
    @Override
    public boolean isProxiesEnabled() {
        if (logger.isDebugEnabled()) {
            logger.debug("Proxy is " + (proxiesEnabled ? "enabled" : "disabled"));
        }
        return proxiesEnabled;
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.ide.model.IProxy#setProxiesEnabled(boolean)
     */
    @Override
    public void setProxiesEnabled(boolean proxiesEnabled) {
        this.proxiesEnabled = proxiesEnabled;
    }

    public IProxyData getHttpProxyData() {
        return getProxyData(IProxyData.HTTP_PROXY_TYPE);
    }

    public IProxyData getHttpsProxyData() {
        return getProxyData(IProxyData.HTTP_PROXY_TYPE);
    }

    private IProxyData getProxyData(String type) {
        if (Utils.isNotEmpty(proxyData)) {
            for (IProxyData proxyDataInstance : proxyData) {
                if (type.equals(proxyDataInstance.getType())) {
                    return proxyDataInstance;
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.model.IProxy#getProxyHost()
    */
    @Override
    public String getProxyHost() {
        if (!isProxiesEnabled()) {
            return Constants.EMPTY_STRING;
        }

        IProxyData httpProxyData = getHttpProxyData();
        if (httpProxyData != null && Utils.isNotEmpty(httpProxyData.getHost())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Returning '" + httpProxyData.getHost() + "' as proxy host ");
            }
            return httpProxyData.getHost();
        } else {
            return Constants.EMPTY_STRING;
        }
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.model.IProxy#getProxyPort()
    */
    @Override
    public String getProxyPort() {
        if (!isProxiesEnabled()) {
            return Constants.EMPTY_STRING;
        }

        IProxyData httpProxyData = getHttpProxyData();
        if (httpProxyData != null && Utils.isNotEmpty(httpProxyData.getPort())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Returning '" + httpProxyData.getPort() + "' as proxy port ");
            }
            return String.valueOf(httpProxyData.getPort());
        } else {
            return Constants.EMPTY_STRING;
        }
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.model.IProxy#getProxyUser()
    */
    @Override
    public String getProxyUser() {
        if (!isProxiesEnabled()) {
            return Constants.EMPTY_STRING;
        }

        IProxyData httpProxyData = getHttpProxyData();
        if (httpProxyData == null) {
            return Constants.EMPTY_STRING;
        }

        if (!httpProxyData.isRequiresAuthentication()) {
            return Constants.EMPTY_STRING;
        }

        if (Utils.isNotEmpty(httpProxyData.getUserId())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Returning '" + httpProxyData.getUserId() + "' as proxy user");
            }
            return httpProxyData.getUserId();
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Returning blank/null proxy user");
            }
            return Constants.EMPTY_STRING;
        }
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.model.IProxy#getProxyPassword()
    */
    @Override
    public String getProxyPassword() {
        if (!isProxiesEnabled()) {
            return Constants.EMPTY_STRING;
        }

        IProxyData httpProxyData = getHttpProxyData();
        if (httpProxyData == null) {
            return Constants.EMPTY_STRING;
        }

        if (!httpProxyData.isRequiresAuthentication()) {
            return Constants.EMPTY_STRING;
        }

        if (Utils.isNotEmpty(httpProxyData.getPassword())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Returning '***' as proxy password ");
            }
            return httpProxyData.getPassword();
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Returning blank/null proxy password ");
            }
            return Constants.EMPTY_STRING;
        }
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.model.IProxy#hasNonProxyHost()
    */
    @Override
    public boolean hasNonProxyHost() {
        return Utils.isNotEmpty(excludedHosts);
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.model.IProxy#getNonProxyHosts()
    */
    @Override
    public String getNonProxyHosts() {
        if (!hasNonProxyHost()) {
            return Constants.EMPTY_STRING;
        }

		StringBuffer strBuff = new StringBuffer();
		for (int i = 0; i < excludedHosts.length; i++) {
		    strBuff.append(excludedHosts[i]);
		    if (i < excludedHosts.length - 1) {
		        strBuff.append(NON_PROXY_HOST_DELIMITOR);
		    }
		}

		if (logger.isDebugEnabled()) {
		    logger.debug("Returning '" + strBuff.toString() + "' as proxy non-hosts ");
		}

		return strBuff.toString();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.model.IProxy#isServerExcluded(java.lang.String)
    */
    @Override
    public boolean isServerExcluded(String serverName) {
        if (Utils.isEmpty(serverName) || Utils.isEmpty(excludedHosts)) {
            return false;
        }

        if (serverName.contains(":")) {
            serverName = serverName.substring(0, serverName.lastIndexOf(":"));
        }

        for (String excludedHost : excludedHosts) {
            if (excludedHost.contains(".")) {
                excludedHost = excludedHost.replace(".", "\\.");
            }

            if (excludedHost.contains("*")) {
                excludedHost = excludedHost.replace("*", ".*");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Comparing server '" + serverName + "' against regex pattern '" + excludedHost + "'");
            }

            Pattern p = Pattern.compile(excludedHost);
            if (p.matcher(serverName).matches()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Excluding server '" + serverName + "' from proxy");
                }
                return true;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Using proxy for server '" + serverName + "'");
        }

        return false;
    }

    public void updateSettings(boolean proxiesEnabled, IProxyData[] proxyData, String[] excludedHosts) {
        this.proxiesEnabled = proxiesEnabled;
        this.proxyData = proxyData;
        this.excludedHosts = excludedHosts;
    }
}
