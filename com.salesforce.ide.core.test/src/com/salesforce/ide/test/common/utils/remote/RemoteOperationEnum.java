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
package com.salesforce.ide.test.common.utils.remote;

import java.io.Serializable;

/**
 * Enum of operations that can be performed through the test servlet running on app server.
 *
 * @author agupta
 */
public enum RemoteOperationEnum implements Serializable{

    GET_OR_CREATE_ORG("GET_OR_CREATE_ORG"), CREATE_NEW_ORG("CREATE_NEW_ORG"), ADD_HOST_TO_WHITELIST(
            "ADD_HOST_TO_WHITELIST"), REMOVE_HOST_FROM_WHITELIST("REMOVE_HOST_FROM_WHITELIST"), SET_API_TOKEN(
            "SET_API_TOKEN"), ENABLE_PERM("ENABLE_PERM"), ENABLE_USER_PERM("ENABLE_USER_PERM"),
            USE_DEBUG_APEX_STREAMING_USER("USE_DEBUG_APEX_STREAMING_USER"),
            REVOKE_PERM("REVOKE_PERM"), ADD_NAMESPACE("ADD_NAMESPACE");

    /**
     * Enum of keys that can be passed back and forth between servlet and IDE.
     *
     */
    public static enum OperationKey {

        USERNAME("USERNAME"), ORG_ID("ORG_ID"), ORG_EDITION("ORG_EDITION"), API_TOKEN("API_TOKEN"),
        ORG_PERMISSION_BITS("ORG_PERMISSION_BITS"), USER_PERMISSION_BITS("USER_PERMISSION_BITS"), ORG_NAMESPACE("ORG_NAMESPACE");

        String name;

        private OperationKey(String val) {
            this.name = val;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    private String operation;

    private RemoteOperationEnum(String val) {
        this.operation = val;
    }

    @Override
    public String toString() {
        return this.operation;
    }

}
