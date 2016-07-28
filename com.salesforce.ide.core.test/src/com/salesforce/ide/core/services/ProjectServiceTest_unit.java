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

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.salesforce.ide.core.model.ApexCodeLocation;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.remote.MetadataStubExt;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.remote.metadata.RunTestsResultExt;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.DeployStatus;
import com.sforce.soap.metadata.RetrieveResult;
import com.sforce.soap.metadata.RetrieveStatus;

public class ProjectServiceTest_unit extends TestCase {

    ProjectService service = new ProjectService();

    public void testgetLocationFromStackLine() throws Exception {
        final String stackTrace = "Class.testApexClass.t2: line 10, column 9\nExternal entry point";
        final ApexCodeLocation locationFromStackLine = service.getLocationFromStackLine("foo", stackTrace);
        assertEquals(10, locationFromStackLine.getLine().intValue());
        assertEquals(9, locationFromStackLine.getColumn().intValue());
    }

    public void testgetLocationFromStackLine_nullInputs() throws Exception {
        assertNull(service.getLocationFromStackLine("foo", null));
        assertNull(service.getLocationFromStackLine(null, "foo"));
    }

    public void testhandleRunTestMessages_NullRunTestResultHandler() throws Exception {
        ProjectService service = new ProjectService();
        ProjectPackageList projectPackageList = mock(ProjectPackageList.class);
        service.handleRunTestMessages(projectPackageList, null, new NullProgressMonitor());
    }

    public void testhandleCodeCoverageWarnings_NullRunTestResultHandler() throws Exception {
        ProjectService service = new ProjectService();
        ProjectPackageList projectPackageList = mock(ProjectPackageList.class);
        service.handleCodeCoverageWarnings(projectPackageList, null, new NullProgressMonitor());
    }

    public void testhandleRetrieveResult_NullRetrieveResultHandler() throws Exception {
        ProjectService service = new ProjectService();
        ProjectPackageList projectPackageList = mock(ProjectPackageList.class);
        assertFalse(service.handleRetrieveResult(projectPackageList, null, false, new NullProgressMonitor()));
    }

    public void testhandleDeployResult() throws Exception {
        ProjectService service = spy(new ProjectService());
        ProjectPackageList projectPackageList = mock(ProjectPackageList.class, Mockito.RETURNS_DEEP_STUBS);
        DeployResultExt deployResult = mock(DeployResultExt.class);
        service.handleDeployResult(projectPackageList, deployResult, false, new NullProgressMonitor());

        verify(service, times(1)).handleDeployErrorMessages(Matchers.<ProjectPackageList> any(),
            Matchers.<Collection<DeployMessage>> any(), Matchers.<IProgressMonitor> any());
        verify(service, times(1)).handleRetrieveResult(Matchers.<ProjectPackageList> any(),
            Matchers.<RetrieveResultExt> any(), anyBoolean(), Matchers.<IProgressMonitor> any());
        verify(service, times(1)).handleDeployWarningMessages(Matchers.<ProjectPackageList> any(),
            Matchers.<Collection<DeployMessage>> any(), Matchers.<IProgressMonitor> any());
        verify(service, times(1)).handleRunTestResult(Matchers.<ProjectPackageList> any(),
            Matchers.<RunTestsResultExt> any(), Matchers.<IProgressMonitor> any());
    }

    public void testhandleCodeCoverageWarnings_clearsMarkersBeforeProceeding() throws Exception {
        ProjectService service = spy(new ProjectService());
        IProject project = mock(IProject.class);
        ProjectPackageList projectPackageList = mock(ProjectPackageList.class);
        RunTestsResultExt runTestHandler = mock(RunTestsResultExt.class);

        when(projectPackageList.getProject()).thenReturn(project);

        service.handleCodeCoverageWarnings(projectPackageList, runTestHandler, new NullProgressMonitor());

        verify(service, times(1)).clearAllWarningMarkers(project);
    }

    public void testRetrieveDisplayInformation_whenNull() throws Exception {
        MetadataStubExt mockMetadataStub = mock(MetadataStubExt.class);
        when(mockMetadataStub.getServerName()).thenReturn("test-end-point-server");

        RetrieveResultAdapter retrieveResult = new RetrieveResultAdapter(mock(AsyncResult.class), mockMetadataStub);
        assertEquals("Polling server test-end-point-server for response",
            retrieveResult.retrieveRealTimeStatusUpdatesIfAny());
    }

    public void testRetrieveDisplayInformation_whenNotNull() throws Exception {
        MetadataStubExt mockMetadataStub = mock(MetadataStubExt.class);
        RetrieveResult mockRetrieveResult = mock(RetrieveResult.class);
        AsyncResult mockAsyncResult = mock(AsyncResult.class);

        when(mockAsyncResult.getId()).thenReturn("");
        when(mockRetrieveResult.getStatus()).thenReturn(RetrieveStatus.InProgress);
        when(mockMetadataStub.checkRetrieveStatus(anyString())).thenReturn(mockRetrieveResult);

        RetrieveResultAdapter retrieveResultAdapter = new RetrieveResultAdapter(mockAsyncResult, mockMetadataStub);
        retrieveResultAdapter.checkStatus();

        verify(mockAsyncResult, times(1)).getId();
        assertTrue(retrieveResultAdapter.retrieveRealTimeStatusUpdatesIfAny().contains("Request status: InProgress"));
    }

    public void testDeployDisplayInformation_whenNull() throws Exception {
        MetadataStubExt mockMetadataStub = mock(MetadataStubExt.class);
        when(mockMetadataStub.getServerName()).thenReturn("test-end-point-server");

        DeployResultAdapter deployResult = new DeployResultAdapter(mock(AsyncResult.class), mockMetadataStub);
        assertEquals("Polling server test-end-point-server for response",
            deployResult.retrieveRealTimeStatusUpdatesIfAny());
    }

    public void testDeployDisplayInformation_whenNotNull() throws Exception {
        MetadataStubExt mockMetadataStub = mock(MetadataStubExt.class);
        DeployResult mockDeployResult = mock(DeployResult.class);
        AsyncResult mockAsyncResult = mock(AsyncResult.class);

        when(mockAsyncResult.getId()).thenReturn("");
        when(mockDeployResult.getStatus()).thenReturn(DeployStatus.InProgress);
        when(mockDeployResult.getNumberComponentsDeployed()).thenReturn(1);
        when(mockDeployResult.getNumberComponentsTotal()).thenReturn(10);
        when(mockMetadataStub.checkDeployStatus(anyString())).thenReturn(mockDeployResult);

        DeployResultAdapter deployResultAdapter = new DeployResultAdapter(mockAsyncResult, mockMetadataStub);
        deployResultAdapter.checkStatus();

        verify(mockAsyncResult, times(1)).getId();
        assertTrue(deployResultAdapter.retrieveRealTimeStatusUpdatesIfAny()
                .contains("Deploy status: InProgress (1/10)"));
    }
}
