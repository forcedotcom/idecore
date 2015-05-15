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

import com.salesforce.ide.core.project.ForceProjectException;


/**
 *
 * Exception only used in tests.
 *
 * @author fchang
 * @deprecated use ideTestException
 */
public class ForceTestSetupException extends ForceProjectException {

    private static final long serialVersionUID = 1L;

    public ForceTestSetupException(String displayMessage) {
        super(displayMessage);
    }

}
