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
package com.salesforce.ide.core.factories;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;
import com.salesforce.ide.core.remote.SalesforceEndpoints;
import com.salesforce.ide.core.remote.registries.DescribeObjectRegistry;
import com.salesforce.ide.core.remote.registries.MergeFieldsRegistry;

/**
 * Encapsulates functionality related to creating, storing, and removing Salesforce.com connections.
 * 
 * @author cwall
 */
public class ConnectionFactory extends BaseFactory {

    private static final Logger logger = Logger.getLogger(ConnectionFactory.class);

    private static ConcurrentMap<ForceProject, Connection> connections =
            new ConcurrentHashMap<>();

    protected DescribeObjectRegistry describeObjectRegistry = null;
    protected MergeFieldsRegistry mergeFieldsRegistry = null;
    protected SalesforceEndpoints salesforceEndpoints = null;

    //   C O N S T R U C T O R S
    public ConnectionFactory() {}

    //   M E T H O D S
    public DescribeObjectRegistry getDescribeObjectRegistry() {
        return describeObjectRegistry;
    }

    public void setDescribeObjectRegistry(DescribeObjectRegistry describeObjectRegistry) {
        this.describeObjectRegistry = describeObjectRegistry;
    }

    public MergeFieldsRegistry getMergeFieldsRegistry() {
        return mergeFieldsRegistry;
    }

    public void setMergeFieldsRegistry(MergeFieldsRegistry mergeFieldsRegistry) {
        this.mergeFieldsRegistry = mergeFieldsRegistry;
    }

    public SalesforceEndpoints getSalesforceEndpoints() {
        return salesforceEndpoints;
    }

    public void setSalesforceEndpoints(SalesforceEndpoints salesforceEndpoints) {
        this.salesforceEndpoints = salesforceEndpoints;
    }

    // lookup method injection by container
    public Connection getConnectionInstance() {
        return new Connection();
    }

    protected Connection getNewConnection(ForceProject forceProject) throws ForceConnectionException,
            InsufficientPermissionsException {
        Connection connection = getConnectionInstance();
        connection.setForceProject(forceProject);
        connection.login();
        return connection;
    }

    public boolean removeConnection(ForceProject forceProject) {
        if (forceProject == null) {
            return false;
        }

        Connection obsoleteConnection = connections.remove(forceProject);
        if (forceProject.getProject() != null && Utils.isNotEmpty(forceProject.getProject().getName())) {
            describeObjectRegistry.remove(forceProject.getProject().getName());
        }

        return obsoleteConnection != null ? true : false;
    }

    public boolean removeConnection(String projectName) {
        if (Utils.isEmpty(projectName)) {
            return false;
        }

        Set<ForceProject> forceProjects = connections.keySet();
        if (Utils.isNotEmpty(forceProjects)) {
            for (ForceProject forceProject : forceProjects) {
                if (forceProject.getProject() != null && projectName.equals(forceProject.getProject().getName())) {
                    return removeConnection(forceProject);
                }
            }
        }

        return false;
    }

    public Connection getConnection(ForceProject forceProject) throws ForceConnectionException,
            InsufficientPermissionsException {
        if (forceProject == null) {
            throw new IllegalArgumentException("Connection info must be specified to obtain a connection.");
        }

        if (logger.isDebugEnabled()) {
            logStoredConnections();
        }

        Connection connection = null;
        if (connections != null && Utils.isNotEmpty(connections.keySet())) {
            logger.debug("Checking cache for existing connection for force project:\n "
                    + forceProject.getFullLogDisplay() + ", hash = " + forceProject.hashCode());
            connection = connections.get(forceProject);
        }

        if (connection == null) {
            connection = storeConnection(forceProject);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Got cached connection");
            }

            // test connection's session
            if (connection.isStale()) {
                connection.relogin();
            }
            // ensure that force options, eg clientid, are set
                connection.setClientCallOptions();
        }

        return connection;
    }

    public Connection getConnection(IProject project) throws ForceConnectionException, InsufficientPermissionsException {
        if (project == null) {
            throw new IllegalArgumentException("Project must be specified to get a connection.");
        }

        ForceProject forceProject = getProjectService().getForceProject(project);
        return getConnection(forceProject);
    }

    public Connection storeConnection(ForceProject forceProject) throws ForceConnectionException,
            InsufficientPermissionsException {
        if (forceProject == null) {
            throw new ForceConnectionException("Connection info must be specific to get a connection.");
        }

        connections.remove(forceProject);
        Connection connection = getNewConnection(forceProject);
        connections.put(forceProject, connection);

        if (logger.isDebugEnabled()) {
            logger.debug("Add new connection to cache:\n " + connection.getLogDisplay());
            logStoredConnections();
        }

        return connection;
    }

    public Connection refreshConnection(ForceProject forceProject) throws ForceConnectionException,
            InsufficientPermissionsException {
        Connection connection = connections.get(forceProject);
        connection.relogin();

        if (logger.isDebugEnabled()) {
            logger.debug("Refreshed cached connection:\n " + connection.getLogDisplay());
            logStoredConnections();
        }

        return connection;
    }

    public Connection refreshConnection(Connection connection) throws ForceConnectionException,
            InsufficientPermissionsException {
        return refreshConnection(connection.getForceProject());
    }

    public String getSavedSessionId(ForceProject forceProject) throws ForceConnectionException,
            InsufficientPermissionsException {
        String sessionId = "";
        Connection connection = getConnection(forceProject);
        if (connection != null) {
            sessionId = connection.getSessionId();
        }
        return sessionId;
    }

    public boolean testConnection(ForceProject forceProject) throws ForceConnectionException,
            InsufficientPermissionsException {
        getNewConnection(forceProject);
        if (logger.isDebugEnabled()) {
            logger.debug("Connection test was successful");
        }
        return true;
    }

    public void clearCache() {
        if (Utils.isEmpty(connections)) {
            logger.warn("No cached connections");
            return;
        }
        logger.warn("Clearing the following cached connections");
        logStoredConnections();
        connections.clear();
    }

    public int getCacheSize() {
        return Utils.isEmpty(connections) ? 0 : connections.size();
    }

    public void dispose() {
        clearCache();
    }

    private static void logStoredConnections() {
        if (connections == null || Utils.isEmpty(connections.keySet())) {
            logger.info("No cached connections");
            return;
        }

        Set<ForceProject> forceProjects = connections.keySet();
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("Cached connections [" + connections.size() + "] are:");
        int connectionCnt = 0;
        for (ForceProject forceProject : forceProjects) {
            Connection connection = connections.get(forceProject);
            strBuffer.append("\n (").append(++connectionCnt).append(") ").append("project: ").append(
                forceProject.getFullLogDisplay()).append(", hash = ").append(forceProject.hashCode()).append(
                "\n     connection: ");
            if (connection != null) {
                strBuffer.append(connection.getLogDisplay());
            } else {
                strBuffer.append("n/a");
            }
        }
        logger.info(strBuffer.toString());
    }
}
