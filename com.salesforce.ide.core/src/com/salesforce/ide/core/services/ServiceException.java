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

import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.remote.Connection;
import com.sforce.soap.metadata.AsyncResult;

public class ServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    protected AsyncResult asyncResultStatus = null;
    protected Connection connection = null;

    //   C O N S T R U C T O R S
    public ServiceException(String pDisplayMessage, Throwable pThrowable) {
        super(pDisplayMessage, ForceExceptionUtils.getRootCause(pThrowable));
    }

    public ServiceException(Throwable pThrowable) {
        super(ForceExceptionUtils.getRootCause(pThrowable));
    }

    public ServiceException(String pDisplayMessage, AsyncResult asyncResultStatus) {
        super(pDisplayMessage);
        this.asyncResultStatus = asyncResultStatus;
    }

    public ServiceException(Throwable pThrowable, Connection connection) {
        super(ForceExceptionUtils.getRootCause(pThrowable));
        this.connection = connection;
    }

    public ServiceException(ServiceException exception, Connection connection) {
        super(ForceExceptionUtils.getRootCause(exception));
        this.asyncResultStatus = exception.getAsyncResult();
        this.connection = connection;
    }

    public ServiceException(String pDisplayMessage) {
        super(pDisplayMessage);
    }

    //   M E T H O D S
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public AsyncResult getAsyncResult() {
        return asyncResultStatus;
    }

    public void setAsyncResultStatus(AsyncResult asyncResultStatus) {
        this.asyncResultStatus = asyncResultStatus;
    }

    public String getOperationId() {
        return asyncResultStatus != null ? asyncResultStatus.getId() : null;
    }
}
