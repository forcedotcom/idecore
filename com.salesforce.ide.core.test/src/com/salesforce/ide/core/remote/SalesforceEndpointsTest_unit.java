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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import junit.framework.TestCase;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.SalesforceEndpoints;

public class SalesforceEndpointsTest_unit extends TestCase {

    public void testSalesforceEndpoints_getAllEndpoints() throws Exception {
        SalesforceEndpoints salesforceEndpoints = ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getSalesforceEndpoints();
        assertNotNull("Salesforce endpoint object should not be null", salesforceEndpoints);
        TreeSet<String> endpoints = salesforceEndpoints.getAllEndpointServers();
        assertTrue("Endpoint properties should not be null or empty and contain at least 3 endpoints", Utils
                .isNotEmpty(endpoints)
                && endpoints.size() > 2);

        String currentSupportedVersion = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getLastSupportedEndpointVersion();
        assertTrue("Current supported API version should not be null or empty", Utils
                .isNotEmpty(currentSupportedVersion));

        List<String> defaultEndpoints = new ArrayList<String>(3);
        defaultEndpoints.add("login.salesforce.com");
        defaultEndpoints.add("test.salesforce.com");
        defaultEndpoints.add("prerellogin.pre.salesforce.com");

        assertTrue("Default endpoints - 'login.salesforce.com', 'test.salesforce.com', "
                + "'prerellogin.pre.salesforce.com' - not found or do not end with current supported API vesion '"
                + currentSupportedVersion + "'", endpoints.containsAll(defaultEndpoints));
    }
}
