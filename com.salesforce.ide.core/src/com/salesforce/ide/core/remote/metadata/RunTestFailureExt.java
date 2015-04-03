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
package com.salesforce.ide.core.remote.metadata;

import com.salesforce.ide.core.remote.IRunTestFailureExt;
import com.sforce.soap.metadata.RunTestFailure;

public class RunTestFailureExt implements IRunTestFailureExt {

    protected RunTestFailure runTestFailure = null;

    public RunTestFailureExt(RunTestFailure runTestFailure) {
        this.runTestFailure = runTestFailure;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestFailureExt#getId()
    */
    @Override
    public String getId() {
        return runTestFailure.getId();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestFailureExt#getMessage()
    */
    @Override
    public java.lang.String getMessage() {
        return runTestFailure.getMessage();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestFailureExt#getMethodName()
    */
    @Override
    public java.lang.String getMethodName() {
        return runTestFailure.getMethodName();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestFailureExt#getName()
    */
    @Override
    public java.lang.String getName() {
        return runTestFailure.getName();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestFailureExt#getNamespace()
    */
    @Override
    public java.lang.String getNamespace() {
        return runTestFailure.getNamespace();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestFailureExt#getPackageName()
    */
    @Override
    public java.lang.String getPackageName() {
        return runTestFailure.getPackageName();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestFailureExt#getStackTrace()
    */
    @Override
    public java.lang.String getStackTrace() {
        return runTestFailure.getStackTrace();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestFailureExt#getTime()
    */
    @Override
    public double getTime() {
        return runTestFailure.getTime();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestFailureExt#getType()
    */
    @Override
    public java.lang.String getType() {
        return runTestFailure.getType();
    }
}
