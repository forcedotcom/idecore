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
package com.salesforce.ide.test;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;

public abstract class BaseTestSuite extends TestSuite {

    protected static Test getTest(String suiteClassName) {
        try {
            Class<?> clazz = Class.forName(suiteClassName);
            Method suiteMethod = clazz.getMethod("suite", new Class[0]);
            return (Test) suiteMethod.invoke(null, new Object[0]);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to find or run suite() on class '" + suiteClassName + "'", e);
        }
    }
}
