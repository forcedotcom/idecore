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
package com.salesforce.ide.api.metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sforce.ws.MessageHandler;

/**
 * How hard? This hard.
 *
 * @author beidson
 */
public class MetadataDebuggingInfoHandler extends DefaultHandler implements MessageHandler {

    private static ThreadLocal<String> LOCAL = new ThreadLocal<String>();
    private final ThreadLocal<Boolean> isDebugLog = new ThreadLocal<Boolean>();
    private final ThreadLocal<Boolean> isDebugLogFound = new ThreadLocal<Boolean>();
    private final ThreadLocal<String> log = new ThreadLocal<String>();

    public MetadataDebuggingInfoHandler() {}

    /**
     * Gets the debug log from the last call made on this thread.
     */
    public static String getDebugLog() {
        return LOCAL.get();
    }

    public void handleRequest(URL endpoint, byte[] request) {
        // Don't need to do anything on the request
    }

    public void handleResponse(URL endpoint, byte[] response) {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(new ByteArrayInputStream(response), this);
        }
        //Silently fail on a parser exception. We only care about well formatted headers with debugLog in it.
        catch (SAXException se) {} catch (ParserConfigurationException pce) {} catch (IOException ie) {}
    }

    @Override
    public void startDocument() throws SAXException {
        //Ensure variables are initialized
        isDebugLog.set(false);
        isDebugLogFound.set(false);
        log.set("");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("DebugLog")) {
            isDebugLog.set(true);
            isDebugLogFound.set(true);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isDebugLog.get()) {
            log.set(log.get() + new String(ch, start, length));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("DebugLog")) {
            isDebugLog.set(false);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        if (isDebugLogFound.get()) {
            LOCAL.set(log.get());
        } else {
            LOCAL.set(null);
        }
    }
}
