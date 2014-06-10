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
package com.salesforce.ide.core.internal.aspects;

import java.net.Proxy;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.eclipse.core.resources.IProject;
import org.springframework.core.Ordered;

import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.internal.preferences.PreferenceManager;
import com.salesforce.ide.core.internal.preferences.proxy.IProxy;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.MetadataStubExt;
import com.salesforce.ide.core.remote.ToolingStubExt;

/**
 * Manages applying Eclipse capture proxy settings to outbound Salesforce.com API calls
 *
 * @author cwall
 *
 */
public class ProxyAspect implements Ordered {

    private static final Logger logger = Logger.getLogger(ProxyAspect.class);

    protected int order = 1;
    protected PreferenceManager preferenceManager = PreferenceManager.getInstance();
    protected ConnectionFactory connectionFactory = null;

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Handles Connection - login and CRUD operations - proxy settings based on workbench Network Connection preference
     *
     * @param joinPoint
     */
    public void setConnectionProxy(JoinPoint joinPoint) {
        // connection to-be inspect/adjusted
        Connection connection = null;

        if (joinPoint.getTarget() instanceof Connection) {
            connection = (Connection) joinPoint.getTarget();
        } else if (joinPoint.getTarget() instanceof MetadataStubExt) {
            MetadataStubExt stub = (MetadataStubExt) joinPoint.getTarget();
            connection = stub.getConnection();
        } else if (joinPoint.getTarget() instanceof ToolingStubExt) {
            ToolingStubExt stub = (ToolingStubExt) joinPoint.getTarget();
            connection = stub.getConnection();
        } else if (Utils.isNotEmpty(joinPoint.getArgs())) {
            Object[] objects = joinPoint.getArgs();
            for (Object object : objects) {
                if (object instanceof IProject && connectionFactory != null) {
                    try {
                        connection = connectionFactory.getConnection((IProject) object);
                    } catch (Exception e) {
                        logger.warn("Unable to get connection for project");
                    }
                    break;
                }
            }
        } else {
            logger.warn("Unable to set proxy settings - could not get Connection from joinpoint");
            return;
        }

        if (connection == null) {
            logger.warn("Unable to set proxy settings - could not get Connection from joinpoint");
            return;
        }

        // current proxy settings
        IProxy proxy = preferenceManager.getProxyManager().getProxy();

        // connection to-be inspect/adjusted

        // evaluate server root for exclusion
        String endpoint = connection.getServerName();
        if (Utils.isEmpty(endpoint)) {
            endpoint = connection.getForceProject().getEndpointServer();
        }

        if (proxy.isProxiesEnabled() && !proxy.isServerExcluded(endpoint)) {
            // update w/ last proxy settings
            updateConnectionProxy(connection, proxy);
        } else {
            // proxy is disabled, clear jvm-wide settings
            connection.getConnectorConfig().setProxy(Proxy.NO_PROXY);
            //clearConnectionProxySettings(connection);
        }

    }

    protected void updateConnectionProxy(Connection connection, IProxy proxy) {
        if (proxy == null) {
            logger.warn("Unable to update connection proxy settings - proxy is null");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Setting proxies on connection " + connection.getLogDisplay() + ": ");
        }

        connection.setProxy(proxy);
    }
}
