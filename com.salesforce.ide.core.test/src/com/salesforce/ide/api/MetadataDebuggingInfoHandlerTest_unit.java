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
package com.salesforce.ide.api;

import junit.framework.TestCase;

import com.salesforce.ide.api.metadata.MetadataDebuggingInfoHandler;

public class MetadataDebuggingInfoHandlerTest_unit extends TestCase {
    final private MetadataDebuggingInfoHandler handler = new MetadataDebuggingInfoHandler();

    public void testHandleResponse_withLog() throws Exception {
        final String log = "This is my super cool log";
        final String xmlToParse =
        		"<tags><leadingUp><toTheLog><DontMatter><debugLog>" + log + "</debugLog></DontMatter></toTheLog></leadingUp></tags>";

        //Endpoint doesn't matter
        handler.handleResponse(null, xmlToParse.getBytes());
        assertEquals(log, MetadataDebuggingInfoHandler.getDebugLog());
    }

    public void testHandleResponse_withLogWithNewLines() throws Exception {
        final String log = "This is my super " +
        		"\n\ncool " +
        		"\nlog";
        final String xmlToParse =
                "<tags><leadingUp><toTheLog><DontMatter><debugLog>" + log + "</debugLog></DontMatter></toTheLog></leadingUp></tags>";

        //Endpoint doesn't matter
        handler.handleResponse(null, xmlToParse.getBytes());
        assertEquals(log, MetadataDebuggingInfoHandler.getDebugLog());
    }

    public void testHandleResponse_withoutLog() throws Exception {
        final String xmlToParse =
                "<tags><leadingUp><toTheLog><DontMatter></DontMatter></toTheLog></leadingUp></tags>";

        handler.handleResponse(null, xmlToParse.getBytes());
        assertNull(MetadataDebuggingInfoHandler.getDebugLog());
    }

    public void testHandleResponse_withBadXMLDoesntThrowException() throws Exception {
        final String xmlToParse =
                "<tags><ledingUp><toTheLog><DontMatter><tMatter></toTheLog></leadingUp<tags>";

        handler.handleResponse(null, xmlToParse.getBytes());
        assertNull(MetadataDebuggingInfoHandler.getDebugLog());
    }
}
