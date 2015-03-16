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

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;

/**
 * Interfaces between JVM-set proxy properties and Force.com IDE's connection proxy settings
 * 
 * @author cwall
 */
public class DefaultProxy extends AbstractProxy {

    // http
    public static final String HTTP_PROXY_ENABLED = "http.proxySet";
    public static final String HTTP_PROXY_HOST = "http.proxyHost";
    public static final String HTTP_PROXY_PORT = "http.proxyPort";
    public static final String HTTP_PROXY_NON_HOSTS = "http.nonProxyHosts";
    public static final String HTTP_PROXY_USER = "http.proxyUser";
    public static final String HTTP_PROXY_USERNAME = "http.proxyUserName";
    public static final String HTTP_PROXY_PASSOWRD = "http.proxyPassword";
    // https
    public static final String HTTPS_PROXY_ENABLED = "https.proxySet";
    public static final String HTTPS_PROXY_HOST = "https.proxyHost";
    public static final String HTTPS_PROXY_PORT = "https.proxyPort";
    public static final String HTTPS_PROXY_NON_HOSTS = "https.nonProxyHosts";
    public static final String HTTPS_PROXY_USER = "https.proxyUser";
    public static final String HTTPS_PROXY_USERNAME = "https.proxyUserName";
    public static final String HTTPS_PROXY_PASSWORD = "https.proxyPassword";

    private static final Logger logger = Logger.getLogger(DefaultProxy.class);

    //   C O N S T R U C T O R S
    public DefaultProxy() {}

    //   M E T H O D S
    /* (non-Javadoc)
    * @see com.salesforce.toolkit.model.IProxy#isProxiesEnabled()
    */
    @Override
    public boolean isProxiesEnabled() {
        String value = getSysProperty(HTTP_PROXY_ENABLED);
        boolean enabled = Utils.isNotEmpty(value) ? Boolean.valueOf(value) : false;
        if (logger.isDebugEnabled()) {
            logger.debug("Proxy is " + (enabled ? "enabled" : "disabled"));
        }
        return enabled;
    }

    /* (non-Javadoc)
    * @see com.salesforce.toolkit.model.IProxy#setProxiesEnabled(boolean)
    */
    @Override
    public void setProxiesEnabled(boolean proxiesEnabled) {}

    private static String getSysProperty(String key) {
        Properties sysProps = System.getProperties();
        if (sysProps != null) {
            String value = sysProps.getProperty(key);
            if (Utils.isNotEmpty(value)) {
                return value;
            }
        }

        return Constants.EMPTY_STRING;
    }

    /* (non-Javadoc)
    * @see com.salesforce.toolkit.model.IProxy#getProxyHost()
    */
    @Override
    public String getProxyHost() {
        if (!isProxiesEnabled()) {
            return Constants.EMPTY_STRING;
        }

        String value = getSysProperty(HTTP_PROXY_HOST);
        if (logger.isDebugEnabled()) {
            logger.debug("Found proxy host '" + value + "' as System property");
        }
        return value;
    }

    /* (non-Javadoc)
    * @see com.salesforce.toolkit.model.IProxy#getProxyPort()
    */
    @Override
    public String getProxyPort() {
        if (!isProxiesEnabled()) {
            return Constants.EMPTY_STRING;
        }

        String value = getSysProperty(HTTP_PROXY_PORT);
        if (logger.isDebugEnabled()) {
            logger.debug("Found proxy port '" + value + "' as System property");
        }
        return value;
    }

    /* (non-Javadoc)
    * @see com.salesforce.toolkit.model.IProxy#getProxyUser()
    */
    @Override
    public String getProxyUser() {
        if (!isProxiesEnabled()) {
            return Constants.EMPTY_STRING;
        }

        String value = getSysProperty(HTTP_PROXY_USERNAME);
        if (Utils.isEmpty(value) && Utils.isNotEmpty(getSysProperty(HTTP_PROXY_USER))) {
            value = getSysProperty(HTTP_PROXY_USER);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found proxy user '" + (Utils.isNotEmpty(value) ? value : "n/a")
                    + "' as System property");
        }
        return value;
    }

    /* (non-Javadoc)
    * @see com.salesforce.toolkit.model.IProxy#getProxyPassword()
    */
    @Override
    public String getProxyPassword() {
        if (!isProxiesEnabled()) {
            return Constants.EMPTY_STRING;
        }

        String value = getSysProperty(HTTP_PROXY_PASSOWRD);
        if (logger.isDebugEnabled()) {
            logger.debug("Found proxy password '" + (Utils.isNotEmpty(value) ? "***" : "n/a")
                    + "' as System property");
        }
        return value;
    }

    /* (non-Javadoc)
    * @see com.salesforce.toolkit.model.IProxy#hasNonProxyHost()
    */
    @Override
    public boolean hasNonProxyHost() {
        return Utils.isNotEmpty(getNonProxyHosts());
    }

    /* (non-Javadoc)
    * @see com.salesforce.toolkit.model.IProxy#getNonProxyHosts()
    */
    @Override
    public String getNonProxyHosts() {
        //return "localhost|*blitz04.*.salesforce.com|10.0.63.186:8081";
        String value = getSysProperty(HTTP_PROXY_NON_HOSTS);
        if (logger.isDebugEnabled()) {
            logger.debug("Found proxy non-host '" + value + "' as System property");
        }
        return value;
    }

    /* (non-Javadoc)
    * @see com.salesforce.toolkit.model.IProxy#isServerExcluded(java.lang.String)
    */
    @Override
    public boolean isServerExcluded(String serverName) {
        if (Utils.isEmpty(serverName) || Utils.isEmpty(getNonProxyHosts())) {
            return false;
        }

        if (serverName.contains(":")) {
            serverName = serverName.substring(0, serverName.lastIndexOf(":"));
        }

        StringTokenizer excludedTokens = new StringTokenizer(getNonProxyHosts(), NON_PROXY_HOST_DELIMITOR);
        if (!excludedTokens.hasMoreTokens()) {
            return false;
        }

        while (excludedTokens.hasMoreElements()) {
            String excludedHost = (String) excludedTokens.nextElement();

            if (excludedHost.contains(":")) {
                excludedHost = excludedHost.substring(0, excludedHost.lastIndexOf(":"));
            }

            if (excludedHost.contains(".")) {
                excludedHost = excludedHost.replace(".", "\\.");
            }

            if (excludedHost.contains("*")) {
                excludedHost = excludedHost.replace("*", ".*");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Comparing server '" + serverName + "' against proxy pattern '" + excludedHost + "'");
            }

            Pattern p = Pattern.compile(excludedHost);
            if (p.matcher(serverName).matches()) {
                if (logger.isInfoEnabled()) {
                    logger.info("Excluding server '" + serverName + "' from proxy");
                }
                return true;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Using proxy for server '" + serverName + "'");
        }

        return false;
    }
}
