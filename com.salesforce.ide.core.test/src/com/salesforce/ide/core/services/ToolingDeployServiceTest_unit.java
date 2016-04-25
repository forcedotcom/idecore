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

import java.util.Timer;
import java.util.TimerTask;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.ToolingStubExt;
import com.sforce.soap.tooling.sobject.ContainerAsyncRequest;
import com.sforce.soap.tooling.ContainerAsyncRequestState;
import com.sforce.soap.tooling.QueryResult;
import com.sforce.soap.tooling.sobject.SObject;
import com.sforce.soap.tooling.SaveResult;

/**
 * Tests (parts) of the DeployService logic via mocking.
 * 
 * @author nchen
 * 
 */
public class ToolingDeployServiceTest_unit extends TestCase {

    private static final int DELAY = 3000;

    public void testPollUntilUnqueuedOrCancelled_unqueued() throws Exception {
        ToolingDeployService service = new ToolingDeployService();
        final ContainerAsyncRequest car = new ContainerAsyncRequest();
        car.setState(ContainerAsyncRequestState.Queued);

        ContainerAsyncRequest result = service.pollUntilUnqueuedOrCancelled(new ToolingStubExt() {
            @Override
            public QueryResult query(String queryString) throws ForceRemoteException {
                QueryResult queryResult = new QueryResult();
                ContainerAsyncRequest containerAsyncRequest = new ContainerAsyncRequest();
                containerAsyncRequest.setState(ContainerAsyncRequestState.Completed);
                queryResult.setRecords(new SObject[] { containerAsyncRequest });
                return queryResult;
            }
        }, new NullProgressMonitor(), "BOGUS", car);
        assertEquals("Should be completed", ContainerAsyncRequestState.Completed, result.getState());
    }

    public void testPollUntilUnqueuedOrCancelled_cancelled() throws Exception {
        ToolingDeployService service = new ToolingDeployService();
        ContainerAsyncRequest car = new ContainerAsyncRequest();
        car.setState(ContainerAsyncRequestState.Queued);
        final NullProgressMonitor monitor = new NullProgressMonitor();

        // Do in a separate thread to simulate how the cancellation will actually work.
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                monitor.setCanceled(true);
            }
        }, DELAY);

        ContainerAsyncRequest result = service.pollUntilUnqueuedOrCancelled(new ToolingStubExt() {
            @Override
            public QueryResult query(String queryString) throws ForceRemoteException {
                QueryResult queryResult = new QueryResult();
                ContainerAsyncRequest containerAsyncRequest = new ContainerAsyncRequest();
                containerAsyncRequest.setState(ContainerAsyncRequestState.Queued);
                queryResult.setRecords(new SObject[] { containerAsyncRequest });
                return queryResult;
            }

            @Override
            public SaveResult[] update(SObject[] sObjects) throws ForceRemoteException {
                return new SaveResult[] { new SaveResult() };
            }
        }, monitor, "BOGUS", car);
        assertEquals("Should be aborted", ContainerAsyncRequestState.Aborted, result.getState());

    }
}
