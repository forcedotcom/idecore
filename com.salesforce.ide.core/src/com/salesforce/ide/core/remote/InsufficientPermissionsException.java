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
import com.salesforce.ide.core.internal.utils.Utils;

public class InsufficientPermissionsException extends ForceRemoteException {

    private static final long serialVersionUID = 1L;

    public static final String STD_ORG_MESSAGE = Messages.getString("InsufficientPermissions.Org.message");
    public static final String STD_USER_MESSAGE = Messages.getString("InsufficientPermissions.User.message");
    public static final String UPDATE_USER_CREDENTIALS_MESSAGE =
            Messages.getString("InsufficientPermissions.User.UpdateCredentials.message");

    //   C O N S T R U C T O R S
    public InsufficientPermissionsException(Connection connection) {
        super(UPDATE_USER_CREDENTIALS_MESSAGE);
        this.connection = connection;
        setShowUpdateCredentialsMessage(true);
    }

    public InsufficientPermissionsException(String message, Connection connection) {
        super(STD_ORG_MESSAGE + (Utils.isNotEmpty(message) ? "\n\n" + message : ""));
        this.connection = connection;
    }

    public InsufficientPermissionsException(Connection connection, Throwable th) {
        super(UPDATE_USER_CREDENTIALS_MESSAGE, connection, th);
        setShowUpdateCredentialsMessage(true);
    }

    public InsufficientPermissionsException(String message, Throwable th, Connection connection) {
        super(message, connection, th);
        setShowUpdateCredentialsMessage(true);
    }

    public void setShowUpdateCredentialsMessage(boolean credentials) {
        if (getMessage() != null && !Utils.startsWith(getMessage(), STD_ORG_MESSAGE)) {
            setExceptionMessage(credentials ? UPDATE_USER_CREDENTIALS_MESSAGE : STD_USER_MESSAGE);
        }
    }
}
