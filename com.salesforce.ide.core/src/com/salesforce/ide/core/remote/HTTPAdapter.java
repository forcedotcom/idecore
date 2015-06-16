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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.Messages;

/**
 * This "adapts" the HTTP transport layer so that it uses the right verb (GET/POST/DELETE) and hits the right end-point.
 * Also of note: it deserializes either using a responseClass or genericType, whichever is non-null, in that order.
 * Usually you just need to use a responseClass, but if you have a generic collection such as a List<T>, the genericType
 * is more useful.
 * 
 * @author nchen
 * 
 */
public class HTTPAdapter<T> implements IHTTPAdapter<T, String>, IHTTPTransport<T> {
    private static final Logger logger = Logger.getLogger(HTTPAdapter.class);

    /**
     * These are the subset of HTTP methods that we are going to use for our protocol
     */
    public enum HTTPMethod {
        GET, POST, DELETE;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private Class<T> responseClass;
    private GenericType<T> genericType;
    private AbstractHTTPTransport transport;
    private Entity<String> payload;
    private HTTPMethod httpMethod;
    protected Response response;

    private long sendWallTime;
    private long receiveWallTime;
    private String errorBody;

    /*
     * Convenience method for instantiating since most of the commands will use POST
     */
    public HTTPAdapter(Class<T> responseClass, AbstractHTTPTransport transport) {
        this(responseClass, transport, HTTPMethod.POST);
    }

    public HTTPAdapter(GenericType<T> genericType, AbstractHTTPTransport transport) {
        this(genericType, transport, HTTPMethod.POST);
    }

    public HTTPAdapter(Class<T> responseClass, AbstractHTTPTransport transport, HTTPMethod httpMethod) {
        this.responseClass = responseClass;
        this.transport = transport;
        this.httpMethod = httpMethod;
    }

    public HTTPAdapter(GenericType<T> genericType, AbstractHTTPTransport transport, HTTPMethod httpMethod) {
        this.genericType = genericType;
        this.transport = transport;
        this.httpMethod = httpMethod;
    }

    @Override
    public T receive() throws ForceConnectionException {
        return unmarshallResponse();
    }

    @Override
    public void send(Object data) {
        Builder builder = transport.constructBuilder();
        response = send(builder, marshallRequest(data));
    }

    @Override
    public T unmarshallResponse() throws ForceConnectionException {

        if (response == null) {
            logger.warn(String.format(
                "%s for %s", Messages.HTTPAdapter_NoResponseErrorMessage, transport.getSessionEndpoint().getUri())); //$NON-NLS-1$
            receiveWallTime = System.currentTimeMillis();
            return null;
        }

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            errorBody = response.readEntity(String.class);
            logger.warn(String.format("%s: %s from %s with error: %s", response.getStatus(), //$NON-NLS-1$
                response.getStatusInfo().getReasonPhrase(), transport.getSessionEndpoint().getUri(), errorBody));
            receiveWallTime = System.currentTimeMillis();
            return null;
        }

        T returnValue;
        if (responseClass != null) {
            returnValue = response.readEntity(responseClass);
        } else {
            returnValue = response.readEntity(genericType);
        }

        receiveWallTime = System.currentTimeMillis();
        return returnValue;
    }

    @Override
    public Entity<String> marshallRequest(Object data) {
        payload = Entity.entity(data.toString(), transport.getMediaType());
        return payload;
    }

    @Override
    public Response send(Builder builder, Entity<String> payload) {
        return sendInternal(builder, payload);
    }

    public <X> Response sendInternal(Builder builder, Entity<X> payload) {
        sendWallTime = System.currentTimeMillis();

        try {
            Method method;

            switch (getHTTPMethod()) {
            case DELETE:
                method = Builder.class.getMethod(getHTTPMethod().toString());
                return (Response) method.invoke(builder);
            case GET:
                method = Builder.class.getMethod(getHTTPMethod().toString());
                return (Response) method.invoke(builder);
            case POST:
                method = Builder.class.getMethod(getHTTPMethod().toString(), Entity.class);
                return (Response) method.invoke(builder, payload);
            default:
                return null;
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            logger.error(Messages.HTTPAdapter_GenericInvokeRequestError, e);
        }
        return null;
    }

    public HTTPMethod getHTTPMethod() {
        return httpMethod;
    }

    @Override
    public long getRoundTripTime() {
        return receiveWallTime - sendWallTime;
    }

    @Override
    public String getConnectionInfo() {
        return String.format("%s|%s|", transport.getDebuggerSessionId(), transport.getDebuggerRequestId()); //$NON-NLS-1$
    }

    // For debugging/error handling
    ///////////////////////////////

    /**
     * Use this to get access to the status of the response and other error messages.
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Use this to get the raw body in the event of an error; The raw body is only set in the event of an error (such as
     * a 403, 404, etc). It is not set when it's a 200 response since you would be able to unmarshall to the
     * responseClass that you want.
     */
    public String getRawBodyWhenError() {
        return errorBody;
    }
}
