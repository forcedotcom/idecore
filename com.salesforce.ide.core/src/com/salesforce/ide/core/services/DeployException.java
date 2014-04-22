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
package com.salesforce.ide.core.services;

import com.salesforce.ide.core.remote.Connection;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.LogInfo;

public class DeployException extends ServiceException {

    private static final long serialVersionUID = 1L;

    private byte[] zipFile = null;
    private DeployOptions deployOptions = null;
    private LogInfo[] logInfos = null;

    public DeployException(Throwable throwable, Connection connection, byte[] zipFile, DeployOptions deployOptions,
            LogInfo[] logInfos) {
        super(throwable, connection);
        this.zipFile = zipFile;
        this.deployOptions = deployOptions;
        this.logInfos = logInfos;
    }

    public DeployException(ServiceException exception, Connection connection, byte[] zipFile, DeployOptions deployOptions,
            LogInfo[] logInfos) {
        super(exception, connection);
        this.zipFile = zipFile;
        this.deployOptions = deployOptions;
        this.logInfos = logInfos;
    }

    public DeployException(String pDisplayMessage, Throwable pThrowable) {
        super(pDisplayMessage, pThrowable);
    }

    //  M E T H O D S
    public byte[] getZipFile() {
        return zipFile;
    }

    public void setZipFile(byte[] zipFile) {
        this.zipFile = zipFile;
    }

    public DeployOptions getDeployOptions() {
        return deployOptions;
    }

    public void setDeployOptions(DeployOptions deployOptions) {
        this.deployOptions = deployOptions;
    }

    public LogInfo[] getLogInfo() {
        return logInfos;
    }

    public void setLogInfo(LogInfo[] logInfos) {
        this.logInfos = logInfos;
    }
}
