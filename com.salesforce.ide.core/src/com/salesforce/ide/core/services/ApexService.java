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
package com.salesforce.ide.core.services;

import org.eclipse.core.resources.IProject;

import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.LoggingInfo;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.apex.ExecuteAnonymousResultExt;
import com.sforce.soap.apex.Connector;
import com.sforce.soap.apex.DebuggingInfo_element;
import com.sforce.soap.apex.ExecuteAnonymousResult;
import com.sforce.soap.apex.LogInfo;
import com.sforce.soap.apex.LogType;
import com.sforce.soap.apex.SoapConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * Common routines for compiling, testing and running apex code
 * 
 * @author cwall
 */
public class ApexService extends BaseService {

    public ApexService() {
        super();
    }

    public ExecuteAnonymousResultExt executeAnonymous(String code, IProject project) {
        LogInfo[] apexLogInfo =
                getLoggingService().getAllApexApiLogInfo(project, LoggingInfo.SupportedFeatureEnum.ExecuteAnonymous);
        return executeAnonymous(code, apexLogInfo, project);
    }

    public ExecuteAnonymousResultExt executeAnonymous(String code, LogInfo[] apexLogInfo, IProject project) {
        int readTimeout = getProjectService().getReadTimeoutInMilliSeconds(project);
        Connection connection = null;
        try {
            connection = getConnectionFactory().getConnection(project);
            return executeAnonymous(code, apexLogInfo, connection, readTimeout);
        } catch (Exception e) {
            ExecuteAnonymousResult er =
                    errorExecuteAnonymousResult(connection != null ? connection.getConnectorConfig() : null, e);
            return new ExecuteAnonymousResultExt(er, null);
        }
    }

    public ExecuteAnonymousResultExt executeAnonymous(String code, LogInfo[] logInfo, Connection connection,
            int readTimeout) {
        ConnectorConfig connectorConfig = connection.getConnectorConfig();
        
        boolean orig_traceMsg = connectorConfig.isTraceMessage();
        boolean orig_prettyPrintXml = connectorConfig.isPrettyPrintXml();
        String orig_sessionId = connectorConfig.getSessionId();
        String orig_serviceEndpoint = connectorConfig.getServiceEndpoint();
        int orig_readTimeout = connectorConfig.getReadTimeout();
        
        connectorConfig.setTraceMessage(true);
        connectorConfig.setPrettyPrintXml(true);
        connectorConfig.setSessionId(connection.getSessionId());
        connectorConfig.setServiceEndpoint(connection.getApexServiceEndpoint(connection.getServiceEndpoint()));
        connectorConfig.setReadTimeout(readTimeout);
        
        SoapConnection apex = null;
        try {
            apex = Connector.newConnection(connectorConfig);
            apex.setDebuggingHeader(logInfo, LogType.None);
            return new ExecuteAnonymousResultExt(apex.executeAnonymous(code), apex.getDebuggingInfo());
        } catch (ConnectionException e) {
            ExecuteAnonymousResult er = errorExecuteAnonymousResult(connectorConfig, e);
            ExecuteAnonymousResultExt erx = new ExecuteAnonymousResultExt(er, null == apex ? null : apex.getDebuggingInfo());
            DebuggingInfo_element dbi = new DebuggingInfo_element();
            dbi.setDebugLog(e.getMessage());
            erx.setDebugInfo(dbi);
            return erx;
        } finally {
            connectorConfig.setTraceMessage(orig_traceMsg);
            connectorConfig.setPrettyPrintXml(orig_prettyPrintXml);
            connectorConfig.setSessionId(orig_sessionId);
            connectorConfig.setServiceEndpoint(orig_serviceEndpoint);
            connectorConfig.setReadTimeout(orig_readTimeout);
        }
    }

    private static ExecuteAnonymousResult errorExecuteAnonymousResult(ConnectorConfig apexCfg, Exception exception) {
        ExecuteAnonymousResult er = new ExecuteAnonymousResult();
        er.setCompiled(true);
        er.setSuccess(false);
        String msg = ForceExceptionUtils.getConnectionCauseExceptionMessage(apexCfg, exception);
        er.setExceptionMessage(msg);
        if (Utils.isNotEmpty(exception.getStackTrace())) {
            StackTraceElement[] stackTraces = exception.getStackTrace();
            StringBuffer trace = new StringBuffer();
            for (StackTraceElement element : stackTraces) {
                trace.append(element.getClassName()).append(".").append(element.getMethodName()).append(" line ")
                        .append(element.getLineNumber()).append("\n");
            }
            er.setExceptionStackTrace(trace.toString());
        }
        return er;
    }
}
