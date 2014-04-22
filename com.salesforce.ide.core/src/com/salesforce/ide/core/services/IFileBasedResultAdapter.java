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

import org.apache.log4j.Logger;

import com.salesforce.ide.core.internal.utils.OperationStats;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.sforce.soap.metadata.AsyncResult;

/**
 * This adapts the (slight) differences between the Async and Deploy results for the purposes of retrieving and
 * deploying so we have one consistent interface.
 * 
 * @author nchen
 * 
 */
interface IFileBasedResultAdapter {
    // Retrieves the AsyncResult (common to both)
    AsyncResult getAsyncResult(); 
    
    // Checks the status of the retrieve/deploy
    IFileBasedResultAdapter checkStatus() throws ForceRemoteException;

    // Checks if retrieve/deploy is done
    boolean isDone();

    // Checks if retrieve/deploy is successful
    boolean isFailure();
    
    // Provide any information about deployment messages
    String logStatus(Logger logger);
    String logFailure(Logger logger);
    String logResult(Logger logger, OperationStats operationStats);
    
    // Provide additional real-time information
    String retrieveRealTimeStatusUpdatesIfAny();
}
