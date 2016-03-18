/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.apex.ui;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ILabelProvider;
import com.salesforce.ide.apex.handlers.OpenTypeHandler.OpenTypeClassHolder;
import com.salesforce.ide.apex.ui.views.FilteredApexResourcesSelectionDialog;
import junit.framework.TestCase;

public class FilteredApexDialogTest_unit extends TestCase {

	private IResource mockResource = mock(IResource.class); 
	private IPath mockPath = mock(IPath.class);
	public void testListLabelProvider_NullInput() {
		ILabelProvider listLabelProvder = FilteredApexResourcesSelectionDialog.listLabelProvider;
		String returnedText = listLabelProvder.getText(null);
		assertNull(returnedText);
	}

	public void testListLabelProvider_NonOpenTypeClassHolder() {
		ILabelProvider listLabelProvder = FilteredApexResourcesSelectionDialog.listLabelProvider;
		String returnedText = listLabelProvder.getText("any string");
		assertNull(returnedText);
	}

	public void testListLabelProvider_OpenTypeClassHolder() {
		ILabelProvider listLabelProvder = FilteredApexResourcesSelectionDialog.listLabelProvider;
		OpenTypeClassHolder sampleResource = new OpenTypeClassHolder(mockResource, "MyProject", "MyClass", 1);
		String expected = "MyClass - MyProject";
		String actual = listLabelProvder.getText(sampleResource);
		assertEquals("Format of list text should have been ", expected, actual);
	}

	public void testDetailsLabelProvider_NullInput() {
		ILabelProvider detailsLabelProvder = FilteredApexResourcesSelectionDialog.detailsLabelProvider;
		String returnedText = detailsLabelProvder.getText(null);
		assertNull(returnedText);
	}

	public void testDetailsLabelProvider_NonOpenTypeClassHolder() {
		ILabelProvider detailsLabelProvder = FilteredApexResourcesSelectionDialog.detailsLabelProvider;
		String returnedText = detailsLabelProvder.getText("any string");
		assertNull(returnedText);
	}

	public void testDetailsLabelProvider_OpenTypeClassHolder() {
		ILabelProvider detailsLabelProvder = FilteredApexResourcesSelectionDialog.detailsLabelProvider;
		OpenTypeClassHolder sampleResource = new OpenTypeClassHolder(mockResource, "MyProject", "MyClass", 1);
		
		String mockFilePath = "src/classes/MyClass.cls";
		when(mockResource.getProjectRelativePath()).thenReturn(mockPath);
		when(mockPath.toString()).thenReturn(mockFilePath);
		String actual = detailsLabelProvder.getText(sampleResource);
		String expected = "MyProject - " + mockFilePath;
		assertEquals("Format of detail text should have been ", expected, actual);
	}
}

