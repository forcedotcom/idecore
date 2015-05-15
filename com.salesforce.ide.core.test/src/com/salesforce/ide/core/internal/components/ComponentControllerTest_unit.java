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
package com.salesforce.ide.core.internal.components;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.sforce.soap.metadata.FileProperties;

public class ComponentControllerTest_unit extends TestCase {

    private Component component;
    private MockedComponentController componentController;
    final RetrieveResultExt mockedRetrieveResult = mock(RetrieveResultExt.class);

    public void testIsNameUniqueLocalCheck_WhenComponentIsCaseInsensitive() throws Exception {
        doReturn(false).when(componentController).checkInFolder(anyString(), anyString());
        when(component.isCaseSensitive()).thenReturn(false);

        assertFalse(componentController.isNameUniqueLocalCheck());
        verify(component, times(1)).isCaseSensitive();
        verify(componentController, times(1)).checkInFolder(anyString(), anyString());
    }

    public void testIsNameUniqueLocalCheck_WhenComponentIsCaseSensitive() throws Exception {
        when(component.isCaseSensitive()).thenReturn(true);
        assertTrue(componentController.isNameUniqueLocalCheck());
        verify(component, times(1)).isCaseSensitive();

    }

    public void testCheckInFolderIfFolderDoesntExist() throws Exception {
        IFolder folder = mock(IFolder.class);
        doReturn(folder).when(componentController).getSourceFolder(anyString());
        when(folder.exists()).thenReturn(false);
        assertTrue(componentController.checkInFolder(anyString(), anyString()));
    }

    public void testCheckInFolderReturnsTrueOnError() throws Exception {
        final IFolder folder = mock(IFolder.class);
        final CoreException coreException = new CoreException(mock(IStatus.class));
        final IPath mockPath = mock(IPath.class);
        doReturn("foo").when(componentController).generateLogMessageFromCoreException(eq(coreException));
        doReturn(folder).when(componentController).getSourceFolder(anyString());
        when(folder.exists()).thenReturn(true);
        when(folder.members()).thenThrow(coreException);
        when(folder.getProjectRelativePath()).thenReturn(mockPath);
        when(mockPath.toPortableString()).thenReturn("somePath");

        assertTrue(componentController.checkInFolder(anyString(), anyString()));

    }

    public void testCheckInFolder_WhenMemberExistsLocally() throws Exception {
        IFolder folder = mock(IFolder.class);
        when(folder.exists()).thenReturn(true);
        final IResource resource = mock(IResource.class);
        IResource[] resources = new IResource[] { resource };

        doReturn(folder).when(componentController).getSourceFolder(anyString());
        when(folder.members()).thenReturn(resources);
        final String fileName = "anyString";
        when(resource.getName()).thenReturn(fileName);
        when(resource.getType()).thenReturn(IResource.FILE);

        final String dirPath = "someDirPath";
        assertFalse(componentController.checkInFolder(dirPath, fileName));

        verify(folder, times(1)).members();
        verify(resource, times(1)).getType();
        verify(resource, times(1)).getName();
    }

    public void testisNameUnique() throws Exception {
        final ComponentController mockController = mock(ComponentController.class);

        when(mockController.isNameUniqueLocalCheck()).thenReturn(true, true, false, false);
        when(mockController.isNameUniqueRemoteCheck(nullMonitor)).thenReturn(true, false, true, false);
        when(mockController.isNameUnique(nullMonitor)).thenCallRealMethod();

        assertTrue(mockController.isNameUnique(nullMonitor));
        assertFalse(mockController.isNameUnique(nullMonitor));
        assertFalse(mockController.isNameUnique(nullMonitor));
        assertFalse(mockController.isNameUnique(nullMonitor));
    }

    public void testIsNameUniqueRemoteCheck() throws Exception {
        doReturn(true).when(componentController).isProjectOnlineEnabled();
        doReturn(true).when(componentController).checkIfComponentExistsOnServer(nullMonitor, component);
        assertTrue(componentController.isNameUniqueRemoteCheck(nullMonitor));
        doReturn(false).when(componentController).checkIfComponentExistsOnServer(nullMonitor, component);
        assertFalse(componentController.isNameUniqueRemoteCheck(nullMonitor));

    }

    public void testCheckIfComponentExistsOnServer() throws Exception {
        final FileMetadataExt mockFileMetadataExt = mock(FileMetadataExt.class);
        when(mockFileMetadataExt.hasFileProperties()).thenReturn(false, true);
        doReturn(mockFileMetadataExt).when(componentController).fireListMetadataQuery(nullMonitor, component);

        assertTrue(componentController.checkIfComponentExistsOnServer(nullMonitor, component));

        FileProperties fileProp1 = new FileProperties();
        fileProp1.setFullName("foo");
        FileProperties fileProp2 = new FileProperties();
        fileProp2.setFullName("someName");
        when(mockFileMetadataExt.getFileProperties()).thenReturn(new FileProperties[] { fileProp1 },
            new FileProperties[] { fileProp1, fileProp2 });

        assertTrue(componentController.checkIfComponentExistsOnServer(nullMonitor, component));
        assertFalse(componentController.checkIfComponentExistsOnServer(nullMonitor, component));
    }

    private final static NullProgressMonitor nullMonitor = new NullProgressMonitor();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        component = mock(Component.class);
        when(component.getName()).thenReturn("someName");
        when(component.getFileExtension()).thenReturn(".someExtention");
        when(component.getDefaultFolder()).thenReturn("someFolder");
        ComponentModel mockedCmpModel = mock(ComponentModel.class);
        componentController = spy(new MockedComponentController(mockedCmpModel));
        when(componentController.getComponent()).thenReturn(component);
        doNothing().when(componentController).init();
    }

    protected class MockedComponentController extends ComponentController {

		public MockedComponentController(ComponentModel componentWizardModel)
				throws ForceProjectException {
			super(componentWizardModel);
		}

		@Override
		protected void preSaveProcess(ComponentModel componentWizardModel,
				IProgressMonitor monitor) throws InterruptedException,
				InvocationTargetException {
		}
    }
}
