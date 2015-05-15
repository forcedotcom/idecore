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
package com.salesforce.ide.test.common;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.InsufficientPermissionsException;

/**
 * @deprecated
 *
 */
public class NoOrgSetupTest extends BaseIDETestCase {
    private static final Logger logger = Logger.getLogger(NoOrgSetupTest.class);

    protected Connection devConnection = null;
    protected Connection prodConnection = null;

    @Override
    protected void setUp() throws Exception {
    // not implemented but override org creation in basetest
    }

    @Override
    protected void tearDown() throws Exception {
        // not implemented but override basetest

        disableAutoBuild();
    }

    public Connection getDevConnection() throws ForceConnectionException, ForceProjectException,
            InsufficientPermissionsException {
        if (devConnection == null) {
            devConnection = getDefaultDevConnection();
        }

        if (devConnection == null) {
            throw new ForceProjectException("Dev connection is null");
        }

        logger.info(devConnection.getLogDisplay());
        return devConnection;
    }

    public Connection getProdConnection() throws ForceConnectionException, ForceProjectException,
            InsufficientPermissionsException {
        if (prodConnection == null) {
            prodConnection = getDefaultProdConnection();
        }

        if (prodConnection == null) {
            throw new ForceProjectException("Prod connection is null");
        }

        logger.info(prodConnection.getLogDisplay());
        return prodConnection;
    }
}
