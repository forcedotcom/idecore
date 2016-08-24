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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.DebuggingHeader_element;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.DescribeMetadataObject;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.FileProperties;
import com.sforce.soap.metadata.ListMetadataQuery;
import com.sforce.soap.metadata.LogInfo;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.RetrieveRequest;
import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.SoapFaultException;

public class MetadataStubExt {

    private static final Logger logger = Logger.getLogger(MetadataStubExt.class);

    private Connection connection;
    private MetadataConnection metadataConnection;

    //TODO: The connection will already have the api version which we're setting through spring. We can get rid of this.
    private double apiVersion = 0.0;


    public MetadataStubExt() {}

    public Connection getConnection() {
        return connection;
    }

    public MetadataConnection getMetadataConnection() {
        return metadataConnection;
    }

    public void setMetadataConnection(MetadataConnection mc) {
        metadataConnection = mc;
    }

    public int getReadTimeout() {
        return metadataConnection.getConfig().getReadTimeout();
    }

    public double getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(double apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setTimeout(int milliSeconds) {
        if (metadataConnection != null) {
            metadataConnection.getConfig().setConnectionTimeout(milliSeconds);
            metadataConnection.getConfig().setReadTimeout(milliSeconds);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Timeout set to " + milliSeconds);
        }
    }

    public void initializeMetadataConnection(Connection connection) throws ForceRemoteException {
        if (connection == null) {
            logger.warn("Unable to initialize metadata connection - connection is null");
            return;
        }

        this.connection = connection;

        if (logger.isDebugEnabled()) {
            logger.debug("Preparing metadata stub for " + connection.getLogDisplay());
            logger.debug("Using metadata server url: " + connection.getMetadataServerUrl());
        }

        try {
            this.metadataConnection = new MetadataConnection(connection.getMetadataConnectorConfig());
        } catch (ConnectionException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not set metadata connection with connection:  " + connection.getLogDisplay());
            }
            ForceExceptionUtils.handleRemoteException(connection, e);
        }

        updateSessionId(connection.getSessionId());
        metadataConnection.setCallOptions(connection.getApplication(), null, null);

        if (logger.isDebugEnabled()) {
            logger.debug("Set client id on metadata stub '" + connection.getApplication() + "'");
        }
    }

    public void setMetadataDebugHeader(LogInfo[] logInfos) {
        if (metadataConnection == null) {
            logger.warn("Unable to set debug level - metadata stub is null");
            return;
        }

        //Does this do the same thing as below? if we even need it.
        DebuggingHeader_element header = metadataConnection.getDebuggingHeader();
        Map<String, LogInfo> logInfoMap = new HashMap<>();
        //Add old headers
        if(header!=null){
            for (LogInfo info : header.getCategories()) {
                logInfoMap.put(info.getCategory().name(), info);
            }
        }

        //Add new headers
        if(logInfos!=null){
            for (LogInfo info : logInfos) {
                logInfoMap.put(info.getCategory().name(), info);
            }
        }

        //Set the union of the old and new
        metadataConnection.setDebuggingHeader(logInfoMap.values().toArray(new LogInfo[logInfoMap.size()]), null);

        if (logger.isDebugEnabled()) {
            logger.debug("Display current debug headers in MetadataConnection...");
            for (LogInfo logInfo : logInfoMap.values()) {
                logger.debug("Header '" + logInfo.toString() + "' is currently in MetadataConnection");
            }
        }
    }

    public String getServerName() {
        if (connection == null) {
            return Constants.EMPTY_STRING;
        }
        return connection.getMetadataServerUrl();
    }

    public void updateSessionId(String sessionId) {
        if (metadataConnection != null) {
            metadataConnection.setSessionHeader(sessionId);

            if (logger.isDebugEnabled()) {
                logger.debug("Set session id on metadata stub '" + sessionId + "'");
            }
        }
    }

    public AsyncResult retrieve(RetrieveRequest retrieveRequest) throws ForceRemoteException {
        if (metadataConnection == null) {
            throw new IllegalArgumentException("Metadata stub cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Timeout set to " + getReadTimeout() + " milliseconds");
        }

        AsyncResult asyncResult = null;
        try {
            asyncResult = metadataConnection.retrieve(retrieveRequest);
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwTranslatedException(e, connection);
        }

        return asyncResult;
    }

    public RetrieveResult checkRetrieveStatus(String asyncProcessId) throws ForceRemoteException {
        if (metadataConnection == null) {
            throw new IllegalArgumentException("Metadata stub cannot be null");
        }

        RetrieveResult retrieveResult = null;
        try {
            retrieveResult = metadataConnection.checkRetrieveStatus(asyncProcessId, true);
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwTranslatedException(e, connection);
        }
        return retrieveResult;
    }

    public DeployResult checkDeployStatus(String asyncProcessId) throws ForceRemoteException {
        if (metadataConnection == null) {
            throw new IllegalArgumentException("Metadata stub cannot be null");
        }

        DeployResult deployResult = null;
        try {
            deployResult = metadataConnection.checkDeployStatus(asyncProcessId, true);
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwTranslatedException(e, connection);
        }
        return deployResult;
    }

    public AsyncResult[] checkStatus(String[] asyncProcessId) throws ForceRemoteException {
        if (metadataConnection == null) {
            throw new IllegalArgumentException("Metadata stub cannot be null");
        }

        AsyncResult[] asyncResult = null;
        try {
            asyncResult = metadataConnection.checkStatus(asyncProcessId);
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwTranslatedException(e, connection);
        }
        return asyncResult;
    }

    public DescribeMetadataResult describeMetadata(double version) throws ForceRemoteException {
        if (metadataConnection == null) {
            throw new IllegalArgumentException("Metadata stub cannot be null");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Get describe metadata for version " + version);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Timeout set to " + getReadTimeout() + " milliseconds");
        }
        
        DescribeMetadataResult describeMetadataResult = null;
        try {
            describeMetadataResult = metadataConnection.describeMetadata(version);
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwTranslatedException(e, connection);
        }
        
        return describeMetadataResult;
    }

    public DescribeMetadataResult describeMetadata() throws ForceRemoteException {
        return describeMetadata(apiVersion);
    }

    public FileProperties[] listMetadata(ListMetadataQuery[] allQueriesArray, IProgressMonitor monitor)
            throws ForceRemoteException {
    	if (monitor==null) {
    		monitor = new NullProgressMonitor();
    	}
        if (metadataConnection == null) {
            throw new IllegalArgumentException("Metadata stub cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Querying metadata for FileProperties");
        }

        if (Utils.isEmpty(allQueriesArray)) {
        	return new FileProperties[] {};
        }

        // Remove unsupported components from queries.
        Set<String> supportedComponents = getSupportedMetadataComponents();
        ArrayList<ListMetadataQuery> allQueries = Lists.newArrayList(allQueriesArray);
		Iterator<ListMetadataQuery> it = allQueries.iterator();
        while (it.hasNext()) {
        	ListMetadataQuery query = it.next();
        	if (!supportedComponents.contains(query.getType())) {
        		it.remove();
        	}
        }
        
        //break request into 3 queries per api call (api constraint)
        final int QUERIES_PER_CALL = 3;
        List<FileProperties> filePropertiesList = new ArrayList<>();
        try {
	        for (List<ListMetadataQuery> queries : Lists.partition(allQueries, QUERIES_PER_CALL)) {
	            try {
	                filePropertiesList.addAll(getFileProperties(queries, monitor));
	            } catch (ConnectionException e) {
	            	//Invalid type or timeout
	                if (ForceExceptionUtils.isReadTimeoutException(e) || e instanceof SoapFaultException) {
		            	filePropertiesList.addAll(tryOneByOne(queries, monitor));
	                } else {
	                    ForceExceptionUtils.throwTranslatedException(e, connection);
	                }
	            }
	        }
        } catch (MonitorCanceledException e) {
			// nothing to do, just return
		}

        return filePropertiesList.toArray(new FileProperties[filePropertiesList.size()]);
    }

    public Set<String> getSupportedMetadataComponents2() throws ForceRemoteException {
    	List<DescribeMetadataObject> metadataObjects = Lists.newArrayList(describeMetadata().getMetadataObjects());
		Function<DescribeMetadataObject, String> metadataObjectToName = new Function<DescribeMetadataObject, String>() {
			@Override
            public String apply(DescribeMetadataObject metadataObject) { return metadataObject.getXmlName(); }
		};
    	HashSet<String> supportedNames = new HashSet<>();
		supportedNames.addAll(Lists.transform(metadataObjects, metadataObjectToName));
		return supportedNames;
    }

    public Set<String> getSupportedMetadataComponents() throws ForceRemoteException {
    	List<DescribeMetadataObject> metadataObjects = Lists.newArrayList(describeMetadata().getMetadataObjects());
    	HashSet<String> supportedNames = new HashSet<>();
    	for (DescribeMetadataObject object : metadataObjects) {
    		String name = object.getXmlName();

    		supportedNames.add(name);

            for (String child : object.getChildXmlNames()) {
                supportedNames.add(child);
            }
			
    		Component component = null;
			component = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory()
                    .getComponentByComponentType(name);
			//Check the md folder in the bean
			if (component != null && object.isInFolder()) {
				supportedNames.add(component.getFolderNameIfFolderTypeMdComponent());
			}
    	}
		return supportedNames;
    }

	private List<FileProperties> tryOneByOne(List<ListMetadataQuery> queries,
			IProgressMonitor monitor) throws MonitorCanceledException,
			ForceRemoteException {
		List<FileProperties> filePropertiesSubList = new ArrayList<>();
		for (List<ListMetadataQuery> listofOneQuery : Lists.partition(Lists.newArrayList(queries), 1)) {
			try {
				filePropertiesSubList.addAll(getFileProperties(listofOneQuery, monitor));
			} catch (ConnectionException e) {
		        if (e instanceof SoapFaultException) {
		        	logger.warn(e.getLocalizedMessage());
				} else 
				if (ForceExceptionUtils.isReadTimeoutException(e)) {
		            logTimeout(listofOneQuery.get(0));
		        } else {
		            ForceExceptionUtils.throwTranslatedException(e, connection);
		        }
			}
		}
		return filePropertiesSubList;
	}

	private static void logTimeout(ListMetadataQuery query) {
		logger.warn("Timeout while retrying to retrieve listMetadata for for component type "
		                + query.getType()
		                + (Utils.isNotEmpty(query.getFolder()) ? "[" + query.getFolder() + "]" : "")
		                + " - will skip and continue");
	}

    /**
     * Get FileProperties for all queries.
     * @return a non-null array of FileProperties
     * @throws MonitorCanceledException
     * @throws ConnectionException
     */
	private List<FileProperties> getFileProperties(List<ListMetadataQuery> queries,
			IProgressMonitor monitor) throws MonitorCanceledException, ConnectionException {

		checkMonitorIsCanceled(monitor);
		logQueries(queries);
		monitor.subTask(getMonitorMessage(queries));

		FileProperties[] tmpFileProperties = null;
            tmpFileProperties = metadataConnection.listMetadata(
            		queries.toArray(new ListMetadataQuery[queries.size()]),
                    getDefaultApiVersion()
            );
		List<FileProperties> properties = arrayToList(tmpFileProperties);

	    logger.debug("Got [" + properties.size() + "] file properties for component types");
		checkMonitorIsCanceled(monitor);
		monitor.worked(queries.size());

		return properties;
	}

	private Double getDefaultApiVersion() {
		return new Double(connection.getSalesforceEndpoints().getDefaultApiVersion());
	}

	private static void checkMonitorIsCanceled(IProgressMonitor monitor) throws MonitorCanceledException {
		if (monitor.isCanceled()) {
			throw new MonitorCanceledException();
		}
	}

	private static ArrayList<FileProperties> arrayToList(
			FileProperties[] tmpFileProperties) {
		return tmpFileProperties == null ? Lists.<FileProperties>newArrayList() : Lists.newArrayList(tmpFileProperties);
	}

	private static String getMonitorMessage(List<ListMetadataQuery> tmpListMetadataQueryList) {
        final StringBuffer strBuff = new StringBuffer();
        Set<String> componentTypes = new HashSet<>();
        for (Iterator<ListMetadataQuery> iterator = tmpListMetadataQueryList.iterator(); iterator.hasNext();) {
            ListMetadataQuery listMetadataQuery = iterator.next();
            if (Utils.isNotEmpty(listMetadataQuery.getFolder())) {
                componentTypes.add(Utils.getPlural(listMetadataQuery.getType()));
            } else {
                componentTypes.add(listMetadataQuery.getType());
            }
        }

        for (Iterator<String> iterator = componentTypes.iterator(); iterator.hasNext();) {
            strBuff.append(iterator.next());
            if (iterator.hasNext()) {
                strBuff.append(", ");
            }
        }

        return strBuff.toString() + "...";
	}

	private static void logQueries(List<ListMetadataQuery> tmpListMetadataQueryList) {
		if (logger.isDebugEnabled()) {
			StringBuffer strBuff = new StringBuffer();
			for (Iterator<ListMetadataQuery> iterator = tmpListMetadataQueryList.iterator(); iterator.hasNext();) {
			    ListMetadataQuery listMetadataQuery = iterator.next();
			    strBuff.append(listMetadataQuery.getType());
			    if (Utils.isNotEmpty(listMetadataQuery.getFolder())) {
			        strBuff.append(" [").append(listMetadataQuery.getFolder()).append("]");
			    }

			    if (iterator.hasNext()) {
			        strBuff.append(", ");
			    }
			}
			logger.debug("Calling listMetadata for component types\n: " + strBuff.toString());
		}
	}

    public AsyncResult deploy(byte[] zipFile, DeployOptions deployOptions) throws ForceRemoteException {
        if (metadataConnection == null) {
            throw new IllegalArgumentException("Metadata stub cannot be null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Timeout set to " + getReadTimeout() + " milliseconds");
        }

        AsyncResult asyncResult = null;
        try {
            asyncResult = metadataConnection.deploy(zipFile, deployOptions);
        } catch (ConnectionException e) {
            ForceExceptionUtils.throwTranslatedException(e, connection);
        }
        return asyncResult;
    }

    public String getLogDisplay() {
        return connection.getLogDisplay();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(apiVersion);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((connection == null) ? 0 : connection.hashCode());
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
        final MetadataStubExt other = (MetadataStubExt) obj;
        if (Double.doubleToLongBits(apiVersion) != Double.doubleToLongBits(other.apiVersion))
            return false;
        if (connection == null) {
            if (other.connection != null)
                return false;
        } else if (!connection.equals(other.connection))
            return false;
        return true;
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
        retValue.append("MetadataStubExt ( ").append(super.toString()).append(TAB).append("connection = ").append(
            connection != null ? connection.getLogDisplay() : "n/a").append(TAB).append(", apiVersion = ").append(
            this.apiVersion).append(TAB).append(", timeout = ").append(
                metadataConnection != null ? metadataConnection.getConfig().getReadTimeout() : "n/a").append(TAB).append(" )");
        return retValue.toString();
    }

    public String toStringLite() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("apiVersion = ").append(this.apiVersion).append(TAB).append("timeout = ").append(
            metadataConnection != null ? Utils.timeoutToSecs(metadataConnection.getConfig().getReadTimeout()) : "n/a");
        return retValue.toString();
    }
}
