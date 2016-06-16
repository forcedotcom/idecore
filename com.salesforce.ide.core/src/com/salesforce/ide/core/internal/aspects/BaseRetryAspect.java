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

import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.springframework.core.Ordered;

import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.factories.MetadataFactory;
import com.salesforce.ide.core.factories.ToolingFactory;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.InvalidLoginException;
import com.salesforce.ide.core.remote.MetadataStubExt;
import com.salesforce.ide.core.remote.ToolingStubExt;
import com.sforce.ws.ConnectionException;

public abstract class BaseRetryAspect implements Ordered {

    private static final Logger logger = Logger.getLogger(BaseRetryAspect.class);

    private static final int DEFAULT_MAX_RETRIES = 2;

    protected ConnectionFactory connectionFactory = null;
    protected MetadataFactory metadataFactory = null;
    protected ToolingFactory toolingFactory = null;
    protected int maxRetries = DEFAULT_MAX_RETRIES;
    protected List<Object> retryableCodes = null;
    protected List<Object> reloginCodes = null;
    protected int order = 1;

    //   M E T H O D S
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public MetadataFactory getMetadataFactory() {
        return metadataFactory;
    }

    public void setMetadataFactory(MetadataFactory metadataFactory) {
        this.metadataFactory = metadataFactory;
    }

    public ToolingFactory getToolingFactory() {
        return toolingFactory;
    }

    public void setToolingFactory(ToolingFactory toolingFactory) {
        this.toolingFactory = toolingFactory;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setRetryableCodes(List<Object> retryableCodes) {
        this.retryableCodes = retryableCodes;
    }

    public void setReloginCodes(List<Object> reloginCodes) {
        this.reloginCodes = reloginCodes;
    }

    public void evaluateLoginException(ForceConnectionException ex, JoinPoint joinPoint) throws InvalidLoginException,
            ForceConnectionException {
    	
        if (connectsViaSessionId(ex))
        	return;

        if (isLoginExceptionRetryable(ex, joinPoint) || isConnectionExceptionRetryable(ex, joinPoint)) {
            return;
        }

        throw ex;
    }
    
    private boolean connectsViaSessionId(final ForceConnectionException fe){
    	return 	null != fe.getConnection() &&
    			fe.getConnection().connectsViaSessionId();
    }

    private boolean isLoginExceptionRetryable(ForceConnectionException ex, JoinPoint joinPoint) {
        Throwable th = ForceExceptionUtils.getRootCause(ex);
        String message = Utils.isNotEmpty(th.getMessage()) ? th.getMessage() : ex.getMessage();

        logThrowable(th, message);
        
        if (th instanceof java.net.SocketException) {
            if (Utils.isNotEmpty(message) && message.contains("Connection reset")) {
                if (logger.isInfoEnabled()) {
                    logger.info("Exception deemed retry worthy");
                }
                return true;
            }
        }

        logger.warn("Login evaluation deemed exception not retry-able:\n " + th.getClass().getSimpleName() + ": "
                + message);

        return false;
    }

    public void evaluateConnectionException(ForceConnectionException ex, JoinPoint joinPoint)
            throws ForceConnectionException {
        if (!isConnectionExceptionRetryable(ex, joinPoint)) {
            throw ex;
        }
    }

    private boolean isConnectionExceptionRetryable(ForceConnectionException ex, JoinPoint joinPoint) {
        Throwable th = ForceExceptionUtils.getRootCause(ex);
        String message = Utils.isNotEmpty(th.getMessage()) ? th.getMessage() : ex.getMessage();

        logThrowable(th, message);

        if (th instanceof java.net.SocketException) {
            if (Utils.isNotEmpty(message) && message.contains("Connection reset")) {
                if (logger.isInfoEnabled()) {
                    logger.info("Exception deemed retry worthy");
                }
                refreshConnection(joinPoint);
                return true;
            }
        } else if (th instanceof com.sforce.soap.partner.fault.wsc.ApiFault) {
            String code = ForceExceptionUtils.getExceptionCode(th);
            if (evaluateExceptionCode(code)) {
                relogin(code, joinPoint);
                return true;
            }
        }

        logger.warn("Connection evaluation deemed exception not retry-able:\n " + th.getClass().getSimpleName() + ": "
                + message);

        return false;
    }

    public void evaluateOperationsException(ForceConnectionException ex, JoinPoint joinPoint)
            throws ForceConnectionException {
        if (isConnectionExceptionRetryable(ex, joinPoint) || isOperationsExceptionRetryable(ex, joinPoint)) {
            return;
        }

        throw ex;
    }

    private boolean isOperationsExceptionRetryable(ForceConnectionException ex, JoinPoint joinPoint) {
        Throwable th = ForceExceptionUtils.getRootCause(ex);
        String message = Utils.isNotEmpty(th.getMessage()) ? th.getMessage() : ex.getMessage();

        logThrowable(th, message);

        if (th instanceof java.rmi.RemoteException) {
            if (evaluateExceptionCode(message)) {
                relogin(message, joinPoint);
                return true;
            }
        }

        logger.warn("Operataions evaluation deemed exception not retry-able:\n " + th.getClass().getSimpleName() + ": "
                + message);

        return false;
    }

    public void evaluateException(Exception ex, JoinPoint joinPoint) throws Exception {
        if (!isExceptionRetryable(ex, joinPoint)) {
            throw ex;
        }
    }

    private boolean isExceptionRetryable(Exception ex, JoinPoint joinPoint) {
        Throwable th = ForceExceptionUtils.getRootCause(ex);
        String message = Utils.isNotEmpty(th.getMessage()) ? th.getMessage() : ex.getMessage();

        logThrowable(th, message);

        if (th instanceof java.net.SocketException) {
            if (Utils.isNotEmpty(message) && message.contains("Connection reset")) {
                if (logger.isInfoEnabled()) {
                    logger.info("Exception deemed retry worthy");
                }
                refreshConnection(joinPoint);
                return true;
            }
        } else if (th instanceof com.sforce.soap.partner.fault.wsc.ApiFault) {
            String code = ForceExceptionUtils.getExceptionCode(th);
            if (evaluateExceptionCode(code)) {
                relogin(code, joinPoint);
                return true;
            }
        } else if (th instanceof java.rmi.RemoteException || th instanceof ConnectionException) {
            if (evaluateExceptionCode(message)) {
                relogin(message, joinPoint);
                return true;
            }
        }

        logger.warn("General evaluation deemed exception not retry-able:\n " + th.getClass().getSimpleName() + ": "
                + message);

        return false;
    }

    private boolean evaluateExceptionCode(String code) {
        if (Utils.isEmpty(code)) {
            return false;
        }

        for (Object retryableCode : retryableCodes) {
            if (code.equals(retryableCode.toString()) || code.contains(retryableCode.toString())) {
                if (logger.isInfoEnabled()) {
                    logger.info("Exception deemed retry worthy");
                }
                return true;
            }
        }

        return false;
    }

    private void relogin(String code, JoinPoint joinPoint) {
        if (Utils.isEmpty(code)) {
            return;
        }

        for (Object reloginCode : reloginCodes) {
            if (code.equals(reloginCode.toString()) || code.contains(reloginCode.toString())) {
                if (logger.isInfoEnabled()) {
                    logger.info("Exception calls for relogin");
                }
                refreshConnection(joinPoint);
                return;
            }
        }
    }

    protected Connection getConnection(JoinPoint joinPoint) {
        Object obj = joinPoint.getTarget();
        if (obj instanceof Connection) {
            return (Connection) joinPoint;
        } else if (obj instanceof MetadataStubExt) {
            return ((MetadataStubExt) obj).getConnection();
        } else if (obj instanceof ToolingStubExt) {
            return ((ToolingStubExt) obj).getConnection();
        }

        return null;
    }

    protected void refreshConnection(JoinPoint joinPoint) {
        Object obj = joinPoint.getTarget();
        try {
            if (obj instanceof Connection) {
                Connection connection = (Connection) obj;
                connection = connectionFactory.refreshConnection(connection);
            } else if (obj instanceof MetadataStubExt) {
                MetadataStubExt metadataStubExt = (MetadataStubExt) obj;
                Connection connection = metadataStubExt.getConnection();
                connection = connectionFactory.refreshConnection(connection);
                metadataStubExt = metadataFactory.refreshMetadataStubExt(connection);
            } else if (obj instanceof ToolingStubExt) {
                ToolingStubExt toolingStubExt = (ToolingStubExt) obj;
                Connection connection = toolingStubExt.getConnection();
                connection = connectionFactory.refreshConnection(connection);
                toolingStubExt = toolingFactory.refreshToolingStubExt(connection);
            }
        } catch (Exception e) {
            logger.warn("Unable to refresh connection", e);
        }
    }

    protected void logThrowable(Throwable th, String message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Evaluating " + th.getClass().getSimpleName() + " for recovery and/or retry:\n " + message);
        }
    }
}
