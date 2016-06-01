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

import com.salesforce.ide.core.remote.ForceConnectionException;

public class LoginRetryAspect extends BaseRetryAspect {

    private static final Logger logger = Logger.getLogger(LoginRetryAspect.class);

    public void loginRetry(ProceedingJoinPoint proceedingJoinPoint) throws ForceConnectionException, Throwable {
        int numAttempts = 0;
        ForceConnectionException forceConnectionException = null;
        do {
            numAttempts++;

            if (logger.isDebugEnabled()) {
                logger.debug("Attempt [" + numAttempts + "] at executing:\n '"
                        + proceedingJoinPoint.getSignature().toLongString() + "'");
            }

            try {
                proceedingJoinPoint.proceed();
                return;
            } catch (ForceConnectionException ex) {
                evaluateLoginException(ex, proceedingJoinPoint);
                forceConnectionException = ex;
            }
        } while (numAttempts <= this.maxRetries && !connectsViaSessionId(forceConnectionException));

        logger.warn("Max retries reached");

        throw forceConnectionException;
    }
    
    private boolean connectsViaSessionId(final ForceConnectionException fe){
    	return null != fe &&
    		   null != fe.getConnection() &&
    		   fe.getConnection().connectsViaSessionId();
    }
}
