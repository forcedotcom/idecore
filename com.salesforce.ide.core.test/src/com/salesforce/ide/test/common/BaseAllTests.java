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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.test.common.utils.TestClassCollector;

public abstract class BaseAllTests extends TestSuite {

    private static final Logger logger = Logger.getLogger(BaseAllTests.class);

    @SuppressWarnings("unchecked")
	public BaseAllTests() {
        try {
            Class<?>[] testClasses = TestClassCollector.getTestClasses(getTestClass(), getAllTestClassName(), getSuffix(), getClassLoader());
            if (Utils.isNotEmpty(testClasses)) {
                for (Class<?> testClass : testClasses) {
                    addTestSuite((Class<? extends TestCase>) testClass);
                }
                logger.info("Added [" + countTestCases() + "] test classes for package '"
                    + getTestClass().getPackage().getName() + "'");
            } else {
                logger.info("No '" + getSuffix() + "' test classes found for package '" + getPackageName() + "'");
            }
        } catch (ClassNotFoundException e) {
            logger.error("Unable to get test class list for package '" + getPackageName() + "': Class does not exist '"
                + e.getMessage() + "'");
        } catch (Exception e) {
            logger.error("Unable to get test class list for package '" + getPackageName() + "'", e);
        }
    }

    protected abstract String getAllTestClassName();

    protected abstract String getSuffix();

    protected abstract String getPackageName();

    protected abstract Class<?> getTestClass();
    
    protected abstract ClassLoader getClassLoader();

}
