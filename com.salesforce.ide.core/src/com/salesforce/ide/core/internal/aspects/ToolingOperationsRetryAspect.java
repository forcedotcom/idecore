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
package com.salesforce.ide.core.internal.aspects;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Aspect for retrying a Tooling operation when possible, e.g. an expired session. See the other classes in this package
 * for more examples.
 * 
 * @author nchen
 * 
 */
public class ToolingOperationsRetryAspect extends BaseRetryAspect {
    private static final Logger logger = Logger.getLogger(ToolingOperationsRetryAspect.class);

    public Object toolingOperationsRetry(ProceedingJoinPoint proceedingJoinPoint) throws Exception, Throwable {
        int numAttempts = 0;
        Exception exception = null;
        do {
            numAttempts++;

            if (logger.isDebugEnabled()) {
                logger.debug("Attempt [" + numAttempts + "] at executing:\n '"
                        + proceedingJoinPoint.getSignature().toLongString() + "'");
            }

            try {
                return proceedingJoinPoint.proceed();
            } catch (Exception ex) {
                logger.error(ex.getClass().getSimpleName()
                        + " occurred while attempting to perform a tooling operation");
                evaluateException(ex, proceedingJoinPoint);
                exception = ex;
            }
        } while (numAttempts <= this.maxRetries);

        logger.warn("Max retries reached");

        throw exception;
    }
}
