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

public class MetadataOperationsRetryAspect extends BaseRetryAspect {

    private static final Logger logger = Logger.getLogger(MetadataOperationsRetryAspect.class);

    public Object metadataOperationsRetry(ProceedingJoinPoint proceedingJoinPoint) throws Exception, Throwable {
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
                        + " occurred while attempting to perform a metadata operation");
                evaluateException(ex, proceedingJoinPoint);
                exception = ex;
            }
        } while (numAttempts <= this.maxRetries);

        logger.warn("Max retries reached");

        throw exception;
    }
}
