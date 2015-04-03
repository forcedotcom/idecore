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

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.project.ForceProjectException;


/**
 *
 *
 * @author cwall
 */
public class ContextHandler extends BaseContextHandler {

    //   C O N S T R U C T O R
    public ContextHandler() {
        super();
    }

    @Override
    protected String[] getApplicationContextFiles() {
        return new String[] { Constants.APPLICATION_CONTEXT};
    }

    @Override
    public void initApplicationContext() throws ForceProjectException {
        initApplicationContext(getApplicationContextFiles());
    }
}
