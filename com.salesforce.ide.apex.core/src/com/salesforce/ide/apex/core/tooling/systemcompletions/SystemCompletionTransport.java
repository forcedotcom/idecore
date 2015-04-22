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
package com.salesforce.ide.apex.core.tooling.systemcompletions;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.salesforce.ide.core.remote.AbstractHTTPTransport;
import com.salesforce.ide.core.remote.HTTPConnection;

/**
 * This is the transport that connects to the endpoint /services/data/vXX.X/tooling/completion. This endpoint is used
 * for stepping through Apex code.
 * 
 * @author nchen
 * 
 */
public class SystemCompletionTransport extends AbstractHTTPTransport {
    public static final String COMPLETIONS_ENDPOINT = "tooling/completions";

    public SystemCompletionTransport(HTTPConnection connection) {
        super(connection);
    }

    @Override
    protected String getMediaType() {
        return MediaType.APPLICATION_XML;
    }

    @Override
    public WebTarget getSessionEndpoint() {
        return connection.getEndpoint().path(COMPLETIONS_ENDPOINT).queryParam("type", "apex");
    }
}
