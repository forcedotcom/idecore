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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.salesforce.ide.core.internal.utils.Constants;

/**
 * Managers IDE proxy interfacing
 *
 * @author cwall
 */
public class ProxyManager {

    private static final Logger logger = Logger.getLogger(ProxyManager.class);


    protected IProxyService proxyService = null;

    // C O N S T R U C T O R S
    public ProxyManager() {
        init();
    }

    // M E T H O D S
    public IProxy getProxy() {
        return proxyService.getProxy();
    }

    public void init() {
        setNetworkAddressCache("0", "0");

        if (isCoreNetAvailable()) {
            if (logger.isInfoEnabled()) {
                logger.info("Initializing '" + Constants.PROXY_BUNDLE_3_3 + "' proxy service");
            }
            proxyService = new NetworkConnectionProxyService();
        } else if (isBundleAvailable(Constants.PROXY_BUNDLE_3_2)) {
            if (logger.isInfoEnabled()) {
                logger.info("Initializing '" + Constants.PROXY_BUNDLE_3_2 + "' proxy service");
            }
            proxyService = new DefaultProxyService();
        } else {
            logger.warn("No proxy bundle found - default proxy service will be initialized");
            proxyService = new DefaultProxyService();
        }
    }

    public boolean isCoreNetAvailable() {
        return isBundleAvailable(Constants.PROXY_BUNDLE_3_3);
    }

    private static boolean isBundleAvailable(String bundleId) {
        Bundle proxyBundle = Platform.getBundle(bundleId);
        return proxyBundle != null && proxyBundle.getState() == Bundle.ACTIVE;
    }

    public void dispose() {
        setNetworkAddressCache("-1", "10");

        proxyService.dispose();
    }

    private static void setNetworkAddressCache(String positive, String negative) {
        // java.security to indicate the caching policy for successful and unsuccessful (negative)
        // name lookups from the name service
        System.setProperty("networkaddress.cache.ttl", positive);
        System.setProperty("networkaddress.cache.negative.ttl", negative);
    }
}
