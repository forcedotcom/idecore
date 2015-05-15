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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

/**
 * The base class for creating test suite. This makes sure that only the tests declared in the added TestClass are run.
 * @author agupta
 * 
 */
public class SimpleTestSuite extends TestSuite {

    private static final Logger logger = Logger.getLogger(SimpleTestSuite.class);

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test suite is executed.
     */
    protected void setUpSuite() {
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after all tests in suite are executed.
     */
    protected void tearDownSuite() {
    }

    /**
     * Executes the setUpSuite before calling the super run method to run the tests.
     * After test suite completion calls the tearDownSuite method.
     */
    @Override
    public void run(TestResult result) {
        setUpSuite();
        super.run(result);
        tearDownSuite();
    }

    private boolean isPublicTestMethod(Method m) {
        return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
    }

    private boolean isTestMethod(Method m) {
        String name= m.getName();
        Class<?>[] parameters= m.getParameterTypes();
        Class<?> returnType= m.getReturnType();
        return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
    }

    @Override
    public void addTestSuite(Class testClass) {
        TestSuite testSuite = new TestSuite(testClass.getName());

        Method[] methods= testClass.getDeclaredMethods();
        for (int i= 0; i < methods.length; i++) {
            if (isPublicTestMethod(methods[i]))
                testSuite.addTest(createTest(testClass, methods[i].getName()));
        }

        if (testSuite.testCount() == 0)
            testSuite.addTest(warning("No tests found in "+testClass.getName()));

        addTest(testSuite);
    }

    public void logStart(String testName) {
        String border = "********************************************************";
        logger.info("\n" + border + "\n " + testName + "\n" + border);
    }
}
