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
package com.salesforce.ide.core.internal.preferences.proxy;


/**
 * Manages interfacing between JVM-set proxy properties and Force.com IDE's connection proxy settings
 *
 * @author cwall
 */
public class DefaultProxyService implements IProxyService {

//    private static final Logger logger = Logger.getLogger(DefaultProxyService.class);

    protected DefaultProxy proxy = null;

    // C O N S T R U C T O R S
    public DefaultProxyService() {
        this.proxy = new DefaultProxy();
    }

    // M E T H O D S
    @Override
    public void dispose() {
    }

    @Override
    public IProxy getProxy() {
        return proxy;
    }
}
