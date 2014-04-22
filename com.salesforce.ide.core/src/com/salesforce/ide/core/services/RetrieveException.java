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

import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.remote.Connection;
import com.sforce.soap.metadata.RetrieveRequest;

public class RetrieveException extends ServiceException {

    private static final long serialVersionUID = 1L;

    private ProjectPackageList projectPackageList = null;
    private RetrieveRequest retrieveRequest = null;

    //   C O N S T R U C T O R S
    public RetrieveException(String pDisplayMessage, Throwable pThrowable) {
        super(pDisplayMessage, pThrowable);
    }

    public RetrieveException(Throwable pThrowable) {
        super(pThrowable);
    }

    public RetrieveException(String pDisplayMessage) {
        super(pDisplayMessage);
    }

    public RetrieveException(Throwable throwable, Connection connection, RetrieveRequest retrieveRequest) {
        super(throwable, connection);
        this.projectPackageList = null;
        this.retrieveRequest = retrieveRequest;
    }

    public RetrieveException(Throwable throwable, Connection connection, ProjectPackageList projectPackageList,
            RetrieveRequest retrieveRequest) {
        super(throwable, connection);
        this.projectPackageList = projectPackageList;
        this.retrieveRequest = retrieveRequest;
    }

    public RetrieveException(ServiceException exception, Connection connection, ProjectPackageList projectPackageList,
            RetrieveRequest retrieveRequest) {
        super(exception, connection);
        this.projectPackageList = projectPackageList;
        this.retrieveRequest = retrieveRequest;
    }

    //   M E T H O D S
    public RetrieveRequest getRetrieveRequest() {
        return retrieveRequest;
    }

    public void setRetrieveRequest(RetrieveRequest retrieveRequest) {
        this.retrieveRequest = retrieveRequest;
    }

    public ProjectPackageList getProjectPackageList() {
        return projectPackageList;
    }

    public void setProjectPackageList(ProjectPackageList projectPackageList) {
        this.projectPackageList = projectPackageList;
    }
}
