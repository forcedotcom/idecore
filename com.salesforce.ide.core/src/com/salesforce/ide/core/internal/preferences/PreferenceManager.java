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
package com.salesforce.ide.core.internal.preferences;

import com.salesforce.ide.core.internal.preferences.proxy.ProxyManager;

public class PreferenceManager {

//    private static final Logger logger = Logger.getLogger(PreferenceManager.class);

    private static PreferenceManager _instance = new PreferenceManager();
    private static ProxyManager proxyManager = new ProxyManager();

    // C O N S T R U C T O R S
    private PreferenceManager() {}

    public static PreferenceManager getInstance() {
        return _instance;
    }

    // M E T H O D S
    public void dispose() {
        proxyManager.dispose();
    }

    public ProxyManager getProxyManager() {
        return proxyManager;
    }
}
