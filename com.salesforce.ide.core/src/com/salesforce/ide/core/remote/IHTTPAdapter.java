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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;


/**
 * An interface that specifies two-way adaption
 * <ul>
 * <li>Command to HTTP request</li>
 * <li>HTTP response to command POJO</li>
 * </ul>
 * 
 * @author nchen
 * 
 */
public interface IHTTPAdapter<R, S> {
    public Entity<S> marshallRequest(Object data);

    public Response send(Builder builder, Entity<String> payload);

    public R unmarshallResponse() throws ForceConnectionException;
}
