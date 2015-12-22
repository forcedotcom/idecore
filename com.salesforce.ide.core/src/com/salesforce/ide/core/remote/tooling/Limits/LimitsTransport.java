/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/

package com.salesforce.ide.core.remote.tooling.Limits;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.salesforce.ide.core.remote.AbstractHTTPTransport;
import com.salesforce.ide.core.remote.HTTPConnection;

/**
 * HTTP transport for Limits
 * 
 * @author jwidjaja
 *
 */
public class LimitsTransport extends AbstractHTTPTransport {
	
	public static final String LIMITS_ENDPOINT = "limits/";

	public LimitsTransport(HTTPConnection connection) {
		super(connection);
	}

	@Override
	public WebTarget getSessionEndpoint() {
		return connection.getEndpoint().path(LIMITS_ENDPOINT);
	}

	@Override
    protected String getMediaType() {
        return MediaType.APPLICATION_JSON;
    }
}
