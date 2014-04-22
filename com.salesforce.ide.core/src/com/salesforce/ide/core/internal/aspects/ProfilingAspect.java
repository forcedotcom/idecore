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

import com.salesforce.ide.core.ForceIdeCorePlugin;

public class ProfilingAspect {

    private static final Logger logger = Logger.getLogger(ProfilingAspect.class);

    protected int order = 1;

    public ProfilingAspect() {

    }

    //   M E T H O D S
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Object profile(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            ForceIdeCorePlugin.stopWatch.start(proceedingJoinPoint.getSignature().getName());
            return proceedingJoinPoint.proceed();
        } finally {
            ForceIdeCorePlugin.stopWatch.stop(proceedingJoinPoint.getSignature().getName());
            if (logger.isDebugEnabled()) {
                logger.debug(ForceIdeCorePlugin.stopWatch.prettyPrint(proceedingJoinPoint.getSignature().getName()));
            }
        }
    }
}
