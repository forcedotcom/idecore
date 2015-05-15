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
package com.salesforce.ide.test.common.utils;

import com.salesforce.ide.core.internal.context.BaseContextHandler;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.project.ForceProjectException;

/**
 * Init Test application context.
 * 
 * @author fchang
 * 
 */
public class IdeContextHandler extends BaseContextHandler {

    @Override
    protected String[] getApplicationContextFiles() {
        return new String[] { Constants.APPLICATION_CONTEXT /*, EditorConstants.APPLICATION_CONTEXT, UIConstants.APPLICATION_CONTEXT*/};
    }

    public void initApplicationContext() throws ForceProjectException {
        initApplicationContext(getApplicationContextFiles());
    }

}
