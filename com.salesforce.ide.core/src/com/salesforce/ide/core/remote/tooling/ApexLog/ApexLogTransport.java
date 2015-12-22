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

package com.salesforce.ide.core.remote.tooling.ApexLog;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.salesforce.ide.core.remote.AbstractHTTPTransport;
import com.salesforce.ide.core.remote.HTTPConnection;

/**
 * HTTP transport for Tooling API's ApexLog
 * 
 * @author jwidjaja
 *
 */
public class ApexLogTransport extends AbstractHTTPTransport {

	public String APEXLOGBODY_ENDPOINT = "tooling/sobjects/ApexLog/%s/Body";
	
	public ApexLogTransport(HTTPConnection connection, String logId) {
		super(connection);
		APEXLOGBODY_ENDPOINT = String.format(APEXLOGBODY_ENDPOINT, logId);
	}

	@Override
	public WebTarget getSessionEndpoint() {
		return connection.getEndpoint().path(APEXLOGBODY_ENDPOINT);
	}
	
	@Override
    protected String getMediaType() {
        return MediaType.TEXT_PLAIN;
    }
}
