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

package com.salesforce.ide.core.remote.tooling.RunTests;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.salesforce.ide.core.remote.AbstractHTTPTransport;
import com.salesforce.ide.core.remote.HTTPConnection;

/**
 * HTTP transport for Tooling API's runTestsAsynchronous
 * 
 * @author jwidjaja
 *
 */
public class RunTestsTransport extends AbstractHTTPTransport {
	public static final String RUNTESTSASYNC_ENDPOINT = "tooling/runTestsAsynchronous/";
	public static final String RUNTESTSSYNC_ENDPOINT = "tooling/runTestsSynchronous/";
	
	private final boolean isAsync;

	public RunTestsTransport(HTTPConnection connection, boolean isAsync) {
		super(connection);
		this.isAsync = isAsync;
	}

	@Override
	public WebTarget getSessionEndpoint() {
		return (this.isAsync) ? connection.getEndpoint().path(RUNTESTSASYNC_ENDPOINT) 
				: connection.getEndpoint().path(RUNTESTSSYNC_ENDPOINT);
	}

	@Override
    protected String getMediaType() {
        return MediaType.APPLICATION_JSON;
    }
}
