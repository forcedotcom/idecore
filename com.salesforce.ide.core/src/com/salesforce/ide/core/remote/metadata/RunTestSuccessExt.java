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

import com.sforce.soap.metadata.RunTestSuccess;


public class RunTestSuccessExt implements IRunTestSuccessExt {

    protected RunTestSuccess runTestSuccess = null;

    public RunTestSuccessExt(RunTestSuccess runTestSuccess) {
        this.runTestSuccess = runTestSuccess;
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestSuccessExt#getId()
    */
    @Override
    public String getId() {
        return runTestSuccess.getId();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestSuccessExt#getMethodName()
    */
    @Override
    public java.lang.String getMethodName() {
        return runTestSuccess.getMethodName();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestSuccessExt#getName()
    */
    @Override
    public java.lang.String getName() {
        return runTestSuccess.getName();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestSuccessExt#getNamespace()
    */
    @Override
    public java.lang.String getNamespace() {
        return runTestSuccess.getNamespace();
    }

    /* (non-Javadoc)
    * @see com.salesforce.ide.core.remote.metadata.IRunTestSuccessExt#getTime()
    */
    @Override
    public double getTime() {
        return runTestSuccess.getTime();
    }
}
