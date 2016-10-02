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

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.sforce.soap.tooling.DeleteResult;
import com.sforce.soap.tooling.DescribeGlobalResult;
import com.sforce.soap.tooling.DescribeSObjectResult;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.sobject.SObject;
import com.sforce.soap.tooling.SaveResult;
import com.sforce.soap.tooling.ToolingConnection;
import com.sforce.ws.ConnectionException;

/**
 * Wraps the Tooling Connection object and provides a facade to the general operations offered through SOAP. Also,
 * handles the initialization (and re-initialization) of the ToolingConnection for ToolingAPI.
 * 
 * @see MetadataStubExt
 * @author nchen
 * 
 */
public class ToolingStubExt {
    private static final Logger logger = Logger.getLogger(ToolingStubExt.class);

    private Connection connection;
    private ToolingConnection toolingConnection;

    public ToolingConnection getToolingConnection() {
        return toolingConnection;
    }

    public void initializeToolingConnection(Connection connection) throws ForceRemoteException {
        if (connection == null) {
            return;
        }

        this.connection = connection;

        try {
            this.toolingConnection = new ToolingConnection(connection.getToolingConnectorConfig());
        } catch (ConnectionException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not set tooling connection with connection:  " + connection.getLogDisplay());
            }
            ForceExceptionUtils.handleRemoteException(connection, e);
        }

        updateSessionId(connection.getSessionId());
        toolingConnection.setCallOptions(connection.getApplication(), null, null);

    }

    public Connection getConnection() {
        return connection;
    }

    public String getServerName() {
        if (connection == null) {
            return Constants.EMPTY_STRING;
        }
        return connection.getToolingServerUrl();
    }

    public void updateSessionId(String sessionId) {
        if (toolingConnection != null) {
            toolingConnection.setSessionHeader(sessionId);
        }
    }

    public SaveResult[] create(SObject[] sObjects) throws ForceRemoteException {
        SaveResult[] saveResults = null;

        try {
            saveResults = toolingConnection.create(sObjects);
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwTranslatedException(e, connection);
        }

        return saveResults;
    }

    public QueryResult query(String queryString) throws ForceRemoteException {
        QueryResult result = null;

        try {
            result = toolingConnection.query(queryString);
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwTranslatedException(e, connection);
        }

        return result;
    }
    
    public QueryResult queryMore(String queryLocator) throws ForceRemoteException {
        QueryResult result = null;

        try {
            result = toolingConnection.queryMore(queryLocator);
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwTranslatedException(e, connection);
        }

        return result;
    }

    public DeleteResult[] delete(String[] ids) throws ForceRemoteException {
        DeleteResult[] deleteResults = null;
        try {
            deleteResults = toolingConnection.delete(ids);
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwTranslatedException(e, connection);
        }
        return deleteResults;
    }

    public SaveResult[] update(SObject[] sObjects) throws ForceRemoteException {
        SaveResult[] saveResults = null;
        try {
            saveResults = toolingConnection.update(sObjects);
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwTranslatedException(e, connection);
        }
        return saveResults;
    }
    
    public DescribeGlobalResult describeGlobal() throws ForceRemoteException {
    	DescribeGlobalResult describeResult = null;
    	
    	try {
    		describeResult = toolingConnection.describeGlobal();
		} catch (ConnectionException e) {
			ForceExceptionUtils.throwTranslatedException(e, connection);
		}
    	
    	return describeResult;
    }
    
    public DescribeSObjectResult describe(String entityName) throws ForceRemoteException {
    	DescribeSObjectResult describeResult = null;
    	
    	try {
    		describeResult = toolingConnection.describeSObject(entityName);
		} catch (ConnectionException e) {
			ForceExceptionUtils.throwTranslatedException(e, connection);
		}
    	
    	return describeResult;
    }
}
