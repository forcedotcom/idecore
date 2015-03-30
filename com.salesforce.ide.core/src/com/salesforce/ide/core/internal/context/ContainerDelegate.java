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
package com.salesforce.ide.core.internal.context;

import com.salesforce.ide.core.project.ForceProjectException;

/**
 *
 *
 * @author cwall
 */
public class ContainerDelegate extends BaseContainerDelegate {

    private static ContainerDelegate _instance = null;


    //   C O N S T R U C T O R
    private ContainerDelegate() {
        contextHandler = new ContextHandler();
        try {
            contextHandler.initApplicationContext();
        } catch (ForceProjectException e) {
            throw new RuntimeException(e);
        }
    }

    public static ContainerDelegate getInstance() {
        if (_instance == null) {
            _instance = new ContainerDelegate();
        }
        return _instance;
    }

    public static ContainerDelegate init() {
        return getInstance();
    }

    @Override
    protected IContainerDelegate getContainerInstance() {
        return _instance;
    }
}
