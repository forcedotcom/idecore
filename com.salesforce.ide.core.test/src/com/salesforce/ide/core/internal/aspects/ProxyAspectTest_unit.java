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
package com.salesforce.ide.core.internal.aspects;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.Proxy;

import junit.framework.TestCase;

import com.salesforce.ide.core.internal.preferences.proxy.IProxy;
import com.salesforce.ide.core.remote.Connection;
/**
 * @author ssasalatti
 */
public class ProxyAspectTest_unit extends TestCase {

    private ProxyAspect pa;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pa = new ProxyAspect();
    }

    public void testUpdateConnectionProxy_WhenNullProxy() throws Exception {
        final Connection connection = new Connection();
        pa.updateConnectionProxy(connection, null);
        final Proxy proxy = connection.getConnectorConfig().getProxy();
        assertEquals(Proxy.Type.DIRECT, proxy.type());
        assertNull(proxy.address());
    }

    public void testUpdateConnectionProxy_WhenOutOfRangeProxy() throws Exception {
        final Connection connection = new Connection();
        final IProxy proxy = mock(IProxy.class);
        when(proxy.getProxyHost()).thenReturn("someHost");
        when(proxy.getProxyPort()).thenReturn("-1","65536");
        pa.updateConnectionProxy(connection, proxy);
        assertNull(connection.getConnectorConfig().getProxy().address());
        pa.updateConnectionProxy(connection, proxy);
        assertNull(connection.getConnectorConfig().getProxy().address());
    }

    public void testUpdateConnectionProxy_WhenMalFormedProxy() throws Exception {
        final Connection connection = new Connection();
        final IProxy mockProxy = mock(IProxy.class);
        when(mockProxy.getProxyHost()).thenReturn(null,"");
        when(mockProxy.getProxyPort()).thenReturn(null,"");
        pa.updateConnectionProxy(connection, mockProxy);
        assertNull(connection.getConnectorConfig().getProxy().address());
        pa.updateConnectionProxy(connection, mockProxy);
        assertNull(connection.getConnectorConfig().getProxy().address());
    }

    public void testUpdateConnectionProxy_WhenGoodProxy() throws Exception {
        final Connection connection = new Connection();
        final IProxy mockProxy = mock(IProxy.class);
        when(mockProxy.getProxyHost()).thenReturn("validHost");
        when(mockProxy.getProxyPort()).thenReturn("666");
        pa.updateConnectionProxy(connection, mockProxy);
        assertNotNull(connection.getConnectorConfig().getProxy().address());
        assertEquals("validHost:666",connection.getConnectorConfig().getProxy().address().toString());
    }

    public void testUpdateConnectionProxy_UpdatesMetadataConfig() throws Exception {
        final Connection connection = new Connection();
        final IProxy mockProxy = mock(IProxy.class);
        when(mockProxy.getProxyHost()).thenReturn("validHost");
        when(mockProxy.getProxyPort()).thenReturn("666");
        pa.updateConnectionProxy(connection, mockProxy);
        assertNotNull(connection.getMetadataConnectorConfig().getProxy().address());
        assertEquals("validHost:666",connection.getMetadataConnectorConfig().getProxy().address().toString());
    }

    public void testUpdateConnectionProxy_UpdatesToolingConfig() throws Exception {
        final Connection connection = new Connection();
        final IProxy mockProxy = mock(IProxy.class);
        when(mockProxy.getProxyHost()).thenReturn("validHost");
        when(mockProxy.getProxyPort()).thenReturn("666");
        pa.updateConnectionProxy(connection, mockProxy);
        assertNotNull(connection.getToolingConnectorConfig().getProxy().address());
        assertEquals("validHost:666", connection.getToolingConnectorConfig().getProxy().address().toString());
    }
}
