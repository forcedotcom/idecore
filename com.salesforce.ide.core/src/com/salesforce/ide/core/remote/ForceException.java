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

import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;

public class ForceException extends Exception {

    private static final long serialVersionUID = 1L;

    public static final String FORCE_METADATA_API_UNEXPECTED_ERROR = "FORCE_METADATA_API_UNEXPECTED_ERROR";

    protected String exceptionCode = null;
    protected String exceptionMessage = null;
    protected Connection connection = null;

    //   C O N S T R U C T O R S
    public ForceException() {
        super();
    }

    public ForceException(String message, Connection connection, Throwable th) {
        super(message, th);
        parseMessage(message);
        this.connection = connection;
    }

    public ForceException(String message, String exceptionCode, Connection connection, Throwable th) {
        super(message, th);
        parseMessage(message);
        this.connection = connection;
        this.exceptionCode = exceptionCode;
    }

    public ForceException(String message) {
        super(message);
        parseMessage(message);
    }

    public ForceException(String message, Connection connection) {
        super(message);
        this.connection = connection;
    }

    public ForceException(Throwable th) {
        super(th);
    }

    public ForceException(Throwable th, Connection connection) {
        super(th);
        this.connection = connection;
    }

    //   M E T H O D S
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public String getExceptionMessage() {
        return Utils.isNotEmpty(exceptionMessage) ? exceptionMessage : getMessage();
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getStrippedExceptionMessage() {
        return ForceExceptionUtils.getStrippedExceptionMessage(exceptionMessage);
    }

    public void setSupplementMessage(String message) {
        exceptionMessage += message;
    }

    private void parseMessage(String message) {
        try {
            if (message.indexOf("faultstring:") > 0) {
                String[] parts = message.split("faultstring:");
                String exMessage = parts[1];
                String[] otherParts = exMessage.split(": ");
                exceptionCode = otherParts[0].trim();
                exceptionMessage = otherParts[1].trim();
            } else {
                exceptionCode = FORCE_METADATA_API_UNEXPECTED_ERROR;
                exceptionMessage = message;
            }
        } catch (Exception e) {
            // TODO: catching an exception in an exception?
            exceptionCode = FORCE_METADATA_API_UNEXPECTED_ERROR;
            exceptionMessage = message;
        }
    }
}
