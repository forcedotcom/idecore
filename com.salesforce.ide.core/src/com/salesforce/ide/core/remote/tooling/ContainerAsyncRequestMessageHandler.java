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
import org.eclipse.swt.widgets.Display;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.project.MarkerUtils;
import com.salesforce.ide.core.services.ToolingDeployService;
import com.sforce.soap.tooling.sobject.ContainerAsyncRequest;
import com.sforce.soap.tooling.ContainerAsyncRequestState;
import com.sforce.soap.tooling.DeployMessage;

/**
 * Handles the different kinds of error messages from a ContainerAsyncRequest.
 * 
 * @author nchen
 * 
 */
public class ContainerAsyncRequestMessageHandler {
    private final Logger logger = Logger.getLogger(ToolingDeployService.class);

    private final ContainerAsyncRequest car; // We need this to get the status, compile failures and/or errors

    private final ComponentList list; // We need this to map back to the actual file resource

    public ContainerAsyncRequestMessageHandler(ComponentList list, ContainerAsyncRequest car) {
        this.list = list;
        this.car = car;
    }

    public void handle() {
        ContainerAsyncRequestState state = car.getState();
        if (state == ContainerAsyncRequestState.Invalidated) {
            handleInvalidatedCase();
        } else if (state == ContainerAsyncRequestState.Completed) {
            handleCompletedCase();
        } else if (state == ContainerAsyncRequestState.Failed) {
            handleFailedCase();
        } else if (state == ContainerAsyncRequestState.Error) {
            handleErrorCase();
        } else if (state == ContainerAsyncRequestState.Aborted) {
            handleAbortedCase();
        }
    }

    protected void handleAbortedCase() {
        getToolingDeployService().createSaveLocallyOnlyMarkers(list);
        logger.debug("ContainerAsyncRequest was aborted: " + car); //$NON-NLS-1$
    }

    protected void handleErrorCase() {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                getDialogUtils().closeMessage(Messages.getString("ContainerAsyncMessagesHandler.FailToSave.Title"), car.getErrorMsg()); //$NON-NLS-1$
            }
        });
    }

    protected void handleInvalidatedCase() {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                getDialogUtils().closeMessage(Messages.getString("ContainerAsyncMessagesHandler.FailToSave.Title"), //$NON-NLS-1$
                    Messages.getString("ContainerAsyncMessagesHandler.DeploymentChangedInProgress.message")); //$NON-NLS-1$

            }
            
        });
    }

    protected void handleFailedCase() {
        DeployMessage[] failures = car.getDeployDetails().getComponentFailures();
        for (DeployMessage failure : failures) {
            displayErrorMarker(failure);
        }
    }

    void displayErrorMarker(DeployMessage failure) {
        Component cmp = list.getComponentById(failure.getId());
        if (cmp != null) {
            MarkerUtils markerUtils = getMarkerUtils();
            if (failure.getLineNumber() > 0) { // Has line number
                if (failure.getColumnNumber() > 1) { // Has column number
                    markerUtils.applySaveErrorMarker(cmp.getFileResource(), failure.getLineNumber(),
                        failure.getColumnNumber(), failure.getColumnNumber() + 1, failure.getProblem());
                } else {
                    markerUtils.applySaveErrorMarker(cmp.getFileResource(), failure.getLineNumber(), 1, 1,
                        failure.getProblem());
                }
            } else {
                markerUtils.applySaveErrorMarker(cmp.getFileResource(), failure.getProblem());
            }
        }
    }

    protected void handleCompletedCase() {
        ToolingDeployService toolingDeployService = getToolingDeployService();
        toolingDeployService.clearSaveLocallyOnlyMarkers(list);
        toolingDeployService.clearSaveErrorMarkers(list);
    }

    // FOR TESTING/MOCKING
    //////////////////////

    protected ToolingDeployService service;
    protected DialogUtils dialogUtils;
    protected MarkerUtils markerUtils;

    protected ToolingDeployService getToolingDeployService() {
        if (service == null) {
            service = ContainerDelegate.getInstance().getServiceLocator().getToolingDeployService();
        }
        return service;
    }

    protected DialogUtils getDialogUtils() {
        if (dialogUtils == null) {
            dialogUtils = DialogUtils.getInstance();
        }
        return dialogUtils;
    }

    protected MarkerUtils getMarkerUtils() {
        if (markerUtils == null) {
            markerUtils = MarkerUtils.getInstance();
        }
        return markerUtils;
    }
}
