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

public abstract class AbstractProxy implements IProxy {

    private static final Logger logger = Logger.getLogger(AbstractProxy.class);

    protected static final String NON_PROXY_HOST_DELIMITOR = "|";

    protected long lastUpdated = -1;

    public AbstractProxy() {
        super();
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    protected void clearAxisProxySettings() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("http.nonProxyHosts");
        System.clearProperty("http.proxyUser");
        System.clearProperty("http.proxyPassword");

        if (logger.isDebugEnabled()) {
            logger.debug("Cleared Axis proxy settings");
        }
    }

}
