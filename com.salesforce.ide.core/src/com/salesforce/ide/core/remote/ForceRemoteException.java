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

public class ForceRemoteException extends ForceException {

    private static final long serialVersionUID = 1L;

    //   C O N S T R U C T O R S
    public ForceRemoteException() {
        super();
    }

    public ForceRemoteException(String message, Connection connection, Throwable th) {
        super(message, connection, th);
    }

    public ForceRemoteException(String message, String exceptionCode, Connection connection, Throwable th) {
        super(message, exceptionCode, connection, th);
    }

    public ForceRemoteException(String message) {
        super(message);
    }

    public ForceRemoteException(String message, Connection connection) {
        super(message, connection);
    }

    public ForceRemoteException(Throwable th) {
        super(th);
    }

    public ForceRemoteException(Throwable th, Connection connection) {
        super(th, connection);
    }
}
