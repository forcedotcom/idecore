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

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.test.common.utils.BaseTestingUtil;
import com.salesforce.ide.test.common.utils.SimpleTestCase;

/**
 * @deprecated
 *
 */
public abstract class BaseTestCase extends SimpleTestCase {

    private static final Logger logger = Logger.getLogger(BaseTestCase.class);

    public BaseTestCase() {}

    public BaseTestCase(String name) {
        super(name);
    }

    protected void handleFailure(String message, Exception e) {
        Throwable rootException = ForceExceptionUtils.getRootCause(e);
        if (rootException != null && Utils.isNotEmpty(rootException.getMessage())) {
            logger.error("Test failed!", rootException);
            rootException.printStackTrace();
            fail(message + ": " + rootException.getMessage());
        } else {
            logger.error("Test failed!", e);
            e.printStackTrace();
            fail(message + ": " + e.getMessage());
        }
    }

    protected void logStart(String testName) {
        logger.info("");
        String className = getClass().getSimpleName();
        logger.info("**** START -- " + className + "." + testName + " -- START ****");
    }

    protected void logEnd(String testName) {
        String className = getClass().getSimpleName();
        logger.info("**** END -- " + className + "." + testName + " -- END ****");
    }

    protected Object getBean(String name) throws ForceProjectException {
        return BaseTestingUtil.getBean(name);
    }
}
