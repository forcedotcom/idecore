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

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.google.common.net.HttpHeaders;

/**
 * Parent class for collecting all the common functionality across all strongly-typed HTTP Transports.
 * 
 * @author nchen
 * 
 * @param <T>
 */
public abstract class AbstractHTTPTransport {

    protected HTTPConnection connection;
    protected Response response;
    protected Request request;

    public AbstractHTTPTransport(HTTPConnection connection) {
        this.connection = connection;
    }

    public Builder constructBuilder() {
        Builder builder = getSessionEndpoint().request(getMediaType());
        builder.header("Authorization", "OAuth " + connection.getActiveSessionToken());
        builder.header("Content-Type", getMediaType());
        builder.header(HttpHeaders.ACCEPT_ENCODING, "gzip");
        return builder;
    }

    public HTTPConnection getConnection() {
        return connection;
    }

    protected String getMediaType() {
        return MediaType.APPLICATION_JSON;
    }

    public abstract WebTarget getSessionEndpoint();

    public String getDebuggerSessionId() {
        return "";
    }

    public String getDebuggerRequestId() {
        return "";
    }
}
