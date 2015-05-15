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

public abstract class BaseAllUnitTests extends BaseAllTests {

    public static final String ALL_TEST_CLASS_NAME  = "AllUnitTests";
    public static final String SUFFIX  = "_unit";

    public BaseAllUnitTests() {
        super();
    }

    @Override
    protected String getAllTestClassName() {
        return ALL_TEST_CLASS_NAME;
    }

    @Override
    protected String getSuffix() {
        return SUFFIX;
    }

}
