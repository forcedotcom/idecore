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

import java.lang.reflect.Field;
import junit.framework.TestCase;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Synchronizer;

import com.salesforce.ide.core.internal.utils.DialogUtils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.project.MarkerUtils;
import com.salesforce.ide.core.services.ToolingDeployService;
import com.sforce.soap.tooling.sobject.ContainerAsyncRequest;
import com.sforce.soap.tooling.ContainerAsyncRequestState;
import com.sforce.soap.tooling.DeployMessage;

/**
 * Exercises the logic inside the ContainerAsyncRequestMessageHandler for the various scenarios. It checks that the
 * proper methods are called. This test increase confidence that we are handling the different cases properly. It does
 * <b>not</b> provide any guarantees that the server is giving us the proper values.
 * 
 * @author nchen
 * 
 */
public class ContainerAsyncMessageHandlerTest_unit extends TestCase {
	
    public class MockedContainerAsyncRequestMessageHandler extends ContainerAsyncRequestMessageHandler {
        public MockedContainerAsyncRequestMessageHandler(ComponentList list, ContainerAsyncRequest car) {
            super(list, car);
        }

        @Override
        public ToolingDeployService getToolingDeployService() {
            if (service == null) {
                service = new MockedToolingDeployService();
            }
            return service;
        }

        @Override
        public DialogUtils getDialogUtils() {
            if (dialogUtils == null) {
                dialogUtils = new MockedDialogUtils();
            }
            return dialogUtils;
        }

        @Override
        public MarkerUtils getMarkerUtils() {
            if (markerUtils == null) {
                markerUtils = new MockedMarkerUtils();
            }
            return markerUtils;
        }
    }

    public static class MockedDialogUtils extends DialogUtils {
        static boolean closeMessagedCalled;

        @Override
        public int closeMessage(String title, String message) {
            closeMessagedCalled = true;
            return -1;
        }

        public static void resetFlags() {
            closeMessagedCalled = false;
        }
    }

    public static class MockedMarkerUtils extends MarkerUtils {
        static boolean saveErrorMarkerWithDefaultLineCalled;
        static boolean saveErrorMarkerWithLineCalled;
        static boolean saveErrorMarkerWithLineAndColumnCalled;

        @Override
        public void applySaveErrorMarker(IResource resource, String msg) {
        	saveErrorMarkerWithDefaultLineCalled = true;
        }

        @Override
        public void applySaveErrorMarker(IResource resource, Integer line, Integer charStart, Integer charEnd, String msg) {
            if (charStart > 1) {
            	saveErrorMarkerWithLineAndColumnCalled = true;
            } else {
            	saveErrorMarkerWithLineCalled = true;
            }
        }

        public static void resetFlags() {
        	saveErrorMarkerWithDefaultLineCalled = false;
        	saveErrorMarkerWithLineCalled = false;
        	saveErrorMarkerWithLineAndColumnCalled = false;

        }
    }

    public class MockedToolingDeployService extends ToolingDeployService {

        // Instance variables so no need to reset since we re-create with each new object of MockedToolingDeployService
        boolean clearSaveLocallyOnlyMarkersCalled;
        boolean clearCompileErrorMarkers;
        boolean createSaveLocallyOnlyMarkers;

        @Override
        public void clearSaveLocallyOnlyMarkers(ComponentList list) {
            clearSaveLocallyOnlyMarkersCalled = true;
        }

        @Override
        public void clearSaveErrorMarkers(ComponentList list) {
            clearCompileErrorMarkers = true;
        }

        @Override
        public void createSaveLocallyOnlyMarkers(ComponentList list) {
            createSaveLocallyOnlyMarkers = true;
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        MockedDialogUtils.resetFlags();
        MockedMarkerUtils.resetFlags();
        super.tearDown();
    }

    public void testHandleInvalidatedCase() throws Exception {
        assertFalse("Close Message Dialog Flag should be false", MockedDialogUtils.closeMessagedCalled);
        ContainerAsyncRequest car = new ContainerAsyncRequest();
        car.setState(ContainerAsyncRequestState.Invalidated);
        MockedContainerAsyncRequestMessageHandler handler =
                new MockedContainerAsyncRequestMessageHandler(new ComponentList(), car);
        
        int before = reflectAndGetRunnableCount();
        handler.handle();
        int after = reflectAndGetRunnableCount();
        
        assertEquals("Expected DialogUtils.closeMessagedCalled to schedule a Runnable", before + 1, after);
    }

    public void testHandleCompletedCase() {
        ContainerAsyncRequest car = new ContainerAsyncRequest();
        car.setState(ContainerAsyncRequestState.Completed);
        MockedContainerAsyncRequestMessageHandler handler =
                new MockedContainerAsyncRequestMessageHandler(new ComponentList(), car);
        handler.handle();
        MockedToolingDeployService toolingDeployService =
                (MockedToolingDeployService) handler.getToolingDeployService();
        assertTrue("Clear Compile Error Markers should be true", toolingDeployService.clearCompileErrorMarkers);
        assertTrue("Clear Save Locally Only Markers should be true",
            toolingDeployService.clearSaveLocallyOnlyMarkersCalled);
    }

    public void testHandleErrorCase() throws Exception {
        assertFalse("Close Message Dialog Flag should be false", MockedDialogUtils.closeMessagedCalled);
        ContainerAsyncRequest car = new ContainerAsyncRequest();
        car.setState(ContainerAsyncRequestState.Error);
        MockedContainerAsyncRequestMessageHandler handler =
                new MockedContainerAsyncRequestMessageHandler(new ComponentList(), car);
        
        int before = reflectAndGetRunnableCount();
        handler.handle();
        int after = reflectAndGetRunnableCount();
        
        assertEquals("Expected DialogUtils.closeMessagedCalled to schedule a Runnable", before + 1, after);
    }
    
    public void testHandleAbortedCase() {
        ContainerAsyncRequest car = new ContainerAsyncRequest();
        car.setState(ContainerAsyncRequestState.Aborted);
        MockedContainerAsyncRequestMessageHandler handler =
                new MockedContainerAsyncRequestMessageHandler(new ComponentList(), car);
        handler.handle();
        MockedToolingDeployService toolingDeployService =
                (MockedToolingDeployService) handler.getToolingDeployService();
        assertTrue("Create Save Locally Flag should be true", toolingDeployService.createSaveLocallyOnlyMarkers);
    }

    public void testHandleFailedCase_WithoutLineNumber() {
        assertFalse("Save Error Marker with Default Line should be false",
            MockedMarkerUtils.saveErrorMarkerWithDefaultLineCalled);

        ContainerAsyncRequest car = new ContainerAsyncRequest();
        car.setState(ContainerAsyncRequestState.Failed);

        ComponentList list = new ComponentList();
        Component component = new Component();
        component.setId("BOGUS_ID");
        list.add(component);

        MockedContainerAsyncRequestMessageHandler handler = new MockedContainerAsyncRequestMessageHandler(list, car);
        DeployMessage failure = new DeployMessage();
        failure.setId("BOGUS_ID");

        handler.displayErrorMarker(failure);
        assertTrue("Save Error Marker with Line should be true",
            MockedMarkerUtils.saveErrorMarkerWithDefaultLineCalled);
    }

    public void testHandleFailedCase_WithLineNumber() {
        assertFalse("Save Error Marker with Line should be false",
            MockedMarkerUtils.saveErrorMarkerWithDefaultLineCalled);

        ContainerAsyncRequest car = new ContainerAsyncRequest();
        car.setState(ContainerAsyncRequestState.Failed);

        ComponentList list = new ComponentList();
        Component component = new Component();
        component.setId("BOGUS_ID");
        list.add(component);

        MockedContainerAsyncRequestMessageHandler handler = new MockedContainerAsyncRequestMessageHandler(list, car);
        DeployMessage failure = new DeployMessage();
        failure.setId("BOGUS_ID");
        failure.setLineNumber(3);

        handler.displayErrorMarker(failure);
        assertTrue("Save Error Marker with Line should be true", MockedMarkerUtils.saveErrorMarkerWithLineCalled);
    }

    public void testHandleFailedCase_WithLineAndColumnNumber() {
        assertFalse("Save Error Marker with Line and Column should be false",
            MockedMarkerUtils.saveErrorMarkerWithDefaultLineCalled);

        ContainerAsyncRequest car = new ContainerAsyncRequest();
        car.setState(ContainerAsyncRequestState.Failed);

        ComponentList list = new ComponentList();
        Component component = new Component();
        component.setId("BOGUS_ID");
        list.add(component);

        MockedContainerAsyncRequestMessageHandler handler = new MockedContainerAsyncRequestMessageHandler(list, car);
        DeployMessage failure = new DeployMessage();
        failure.setId("BOGUS_ID");
        failure.setLineNumber(3);
        failure.setColumnNumber(2);

        handler.displayErrorMarker(failure);
        assertTrue("Save Error Marker with Line and Column should be true",
            MockedMarkerUtils.saveErrorMarkerWithLineAndColumnCalled);
    }

    private int reflectAndGetRunnableCount() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	Synchronizer sync = Display.getDefault().getSynchronizer();
        Field messageCount = Synchronizer.class.getDeclaredField("messageCount");
        messageCount.setAccessible(true);
        int msgCount = (int) messageCount.get(sync);
        return msgCount;
    }
}
