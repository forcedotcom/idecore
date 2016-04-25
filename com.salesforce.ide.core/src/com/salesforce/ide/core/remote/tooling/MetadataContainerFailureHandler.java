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
package com.salesforce.ide.core.remote.tooling;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.ToolingStubExt;
import com.sforce.soap.tooling.Error;
import com.sforce.soap.tooling.sobject.MetadataContainer;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.SaveResult;
import com.sforce.soap.tooling.StatusCode;

/**
 * Handles the different errors that we might encounter when trying to interact with MetadataContainers.
 * 
 * @author nchen
 * 
 */
public class MetadataContainerFailureHandler {
    private static final Logger logger = Logger.getLogger(MetadataContainerFailureHandler.class);

    private ForceProject forceProject;
    private ToolingStubExt stub;

    public MetadataContainerFailureHandler(ForceProject forceProject, ToolingStubExt stub) {
        this.forceProject = forceProject;
        this.stub = stub;
    }

    public void handleCreationFailure(SaveResult[] containerResults) {
        assert containerResults.length == 1; // We only deal with one MetadataContainer at a time
        for (Error error : containerResults[0].getErrors()) {
            if (error.getStatusCode() == StatusCode.DUPLICATE_VALUE) {
                attemptToDeleteDuplicate();
            }
        }
    }

    private void attemptToDeleteDuplicate() {
        try {
            String projectIdentifier = forceProject.getProjectIdentifier();
            String soql = String.format("SELECT Id FROM MetadataContainer WHERE name = '%s'", projectIdentifier);
            QueryResult queryResult = stub.query(soql);
            MetadataContainer duplicateContainer = (MetadataContainer) queryResult.getRecords()[0];
            stub.delete(new String[] { duplicateContainer.getId() });
        } catch (ForceRemoteException e) {
            // Let's not try to do nested recovery from a failure handler. Log it.
            logger.debug(e);
        }
    }

}
