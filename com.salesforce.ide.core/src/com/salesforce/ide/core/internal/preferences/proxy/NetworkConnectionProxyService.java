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
import org.eclipse.core.net.proxy.IProxyChangeEvent;
import org.eclipse.core.net.proxy.IProxyChangeListener;
import org.eclipse.core.net.proxy.IProxyService;
import org.osgi.util.tracker.ServiceTracker;

import com.salesforce.ide.core.ForceIdeCorePlugin;
/**
 * Manages the interfacing between Eclipse's Network Connection preferences and Force.com IDE's connection proxy settings
 *
 * @author cwall
 */
public class NetworkConnectionProxyService implements com.salesforce.ide.core.internal.preferences.proxy.IProxyService {

    private static final Logger logger = Logger.getLogger(NetworkConnectionProxyService.class);

    protected ToolkitProxyChangeListener toolkitProxyChangeListener = new ToolkitProxyChangeListener();
    protected NetworkConnectionProxy proxy = null;

    class ToolkitProxyChangeListener implements IProxyChangeListener {

        private IProxyService proxyService = null;
        private ServiceTracker<IProxyService, IProxyService> tracker = null;

        public void init() {
            tracker = new ServiceTracker<>(ForceIdeCorePlugin.getDefault().getBundle().getBundleContext(),
                    IProxyService.class.getName(), null);
            tracker.open();
            proxyService = tracker.getService();

            if (proxyService == null) {
                logger.warn("Unable to set proxy settings - IProxyService is not available");
                return;
            }

            proxy.updateSettings(proxyService.isProxiesEnabled(), proxyService.getProxyData(), proxyService
                    .getNonProxiedHosts());
            proxyService.addProxyChangeListener(this);
        }

        public void dispose() {
            if (proxyService != null) {
                proxyService.removeProxyChangeListener(this);
            } else {
                logger.warn("Unable to detach listener - IProxyService or event is not available");
            }

            if (tracker != null) {
                tracker.close();
            } else {
                logger.warn("Unable to close tracker - tracker is null");
            }
        }

        @Override
        public void proxyInfoChanged(IProxyChangeEvent event) {
            if (proxyService == null || event == null) {
                logger.warn("Unable to update proxy settings - IProxyService or event is not available");
                return;
            }

            proxy.updateSettings(proxyService.isProxiesEnabled(), proxyService.getProxyData(),
                    proxyService.getNonProxiedHosts());

            logChange(event);
        }

        private void logChange(IProxyChangeEvent event) {
            if (logger.isDebugEnabled()) {
                StringBuffer strBuff = new StringBuffer("Updated proxy settings - ");
                switch (event.getChangeType()) {
                case IProxyChangeEvent.NONPROXIED_HOSTS_CHANGED:
                    strBuff.append("non-proxy hosts");
                    break;
                case IProxyChangeEvent.PROXY_DATA_CHANGED:
                    strBuff.append("proxy data");
                    break;
                case IProxyChangeEvent.PROXY_SERVICE_ENABLEMENT_CHANGE:
                    strBuff.append("enablement");
                    break;
                default:
                    strBuff.append("unknown");
                    break;
                }
                logger.debug(strBuff.toString());
            }
        }
    }

    // C O N S T R U C T O R S
    public NetworkConnectionProxyService() {
        this.proxy = new NetworkConnectionProxy();
        init();
    }

    // M E T H O D S
    private void init() {
        initListeners();
    }

    private void initListeners() {
        toolkitProxyChangeListener.init();
    }

    @Override
    public void dispose() {
        disposeListeners();
    }

    private void disposeListeners() {
        if (toolkitProxyChangeListener != null) {
            toolkitProxyChangeListener.dispose();
        }
    }

    @Override
    public IProxy getProxy() {
        return proxy;
    }
}
