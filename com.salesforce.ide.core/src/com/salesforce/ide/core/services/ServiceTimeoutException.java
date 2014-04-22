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

import com.salesforce.ide.core.internal.utils.OperationStats;
import com.salesforce.ide.core.remote.MetadataStubExt;
import com.salesforce.ide.core.remote.metadata.IMetadataResultExt;
import com.sforce.soap.metadata.AsyncResult;

public class ServiceTimeoutException extends ServiceException {

    private static final long serialVersionUID = 1L;

    private MetadataStubExt metadataStubExt = null;
    private OperationStats operationStats = null;
    private IMetadataResultExt metadataResultExt = null;

    //   C O N S T R U C T O R S
    public ServiceTimeoutException(String pDisplayMessage, MetadataStubExt metadataStubExt,
            AsyncResult asyncResultStatus, OperationStats operationStats) {
        super(pDisplayMessage, asyncResultStatus);
        this.metadataStubExt = metadataStubExt;
        this.operationStats = operationStats;
    }

    public MetadataStubExt getMetadataStubExt() {
        return metadataStubExt;
    }

    public OperationStats getOperationStats() {
        return operationStats;
    }

    public IMetadataResultExt getMetadataResultExt() {
        return metadataResultExt;
    }

    public void setMetadataResultExt(IMetadataResultExt metadataResultExt) {
        this.metadataResultExt = metadataResultExt;
    }

}
