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

import com.salesforce.ide.core.internal.utils.Messages;

public class InvalidLoginException extends ForceConnectionException {

    private static final long serialVersionUID = 1L;

    private static String UPDATE_ORG_CREDENTIALS_MESSAGE =
            Messages.getString("InsufficientOrgPermissions.UpdateCredentials.message");

    //   C O N S T R U C T O R S
    public InvalidLoginException() {
        super();
    }

    public InvalidLoginException(String message, Connection connection, Throwable th) {
        super(message, connection, th);
    }

    public InvalidLoginException(String message, String exceptionCode, Connection connection, Throwable th) {
        super(message, exceptionCode, connection, th);
    }

    public InvalidLoginException(String message) {
        super(message);
    }

    public InvalidLoginException(Throwable th) {
        super(th);
    }

    public InvalidLoginException(Throwable th, Connection connection) {
        super(th, connection);
    }

    public void setShowUpdateCredentialsMessage(boolean projectBase) {
        setExceptionMessage(projectBase ? getMessage() + "  " + UPDATE_ORG_CREDENTIALS_MESSAGE : getMessage());
    }
}
