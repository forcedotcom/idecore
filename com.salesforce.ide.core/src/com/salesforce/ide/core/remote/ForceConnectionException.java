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


public class ForceConnectionException extends ForceException {

    private static final long serialVersionUID = 1L;

    //   C O N S T R U C T O R S
    public ForceConnectionException() {
        super();
    }

    public ForceConnectionException(String message, Connection connection, Throwable th) {
        super(message, connection, th);
    }

    public ForceConnectionException(String message, String exceptionCode, Connection connection, Throwable th) {
        super(message, exceptionCode, connection, th);
    }

    public ForceConnectionException(String message) {
        super(message);
    }

    public ForceConnectionException(Throwable th) {
        super(th);
    }

    public ForceConnectionException(Throwable th, Connection connection) {
        super(th, connection);
    }
}
