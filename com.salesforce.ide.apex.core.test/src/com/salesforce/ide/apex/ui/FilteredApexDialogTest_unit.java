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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Map;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.salesforce.ide.apex.handlers.OpenTypeHandler.OpenTypeClassHolder;
import com.salesforce.ide.apex.ui.views.FilteredApexResourcesSelectionDialog;

import junit.framework.TestCase;

public class FilteredApexDialogTest_unit extends TestCase {

	private FilteredItemsSelectionDialog dialog;
	private Map<String, OpenTypeClassHolder> resources;
	private Shell shell;

	public void setUp() throws Exception {
		resources = Maps.newHashMap();
		shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		dialog = new FilteredApexResourcesSelectionDialog(shell, true, resources);
	}

	@Test
	public void testPressCancel() {
		dialog = mock(FilteredApexResourcesSelectionDialog.class);
		when(dialog.open()).thenReturn(Window.CANCEL);
		assertEquals(Window.CANCEL, dialog.open());
		assertNull(dialog.getResult());
	}

	@Test
	public void testOneResource() {
		String myFilePath = "MyProject/src/MyClass.cls";
		OpenTypeClassHolder resource = new OpenTypeClassHolder(null, "MyProject", myFilePath, 1);
		resources.put(myFilePath, resource);
		dialog = mock(FilteredApexResourcesSelectionDialog.class);
		when(dialog.open()).thenReturn(Window.OK);
		when(dialog.getResult()).thenReturn(resources.values().toArray());

		Object[] result = dialog.getResult();
		assertEquals(1, result.length);
		assertEquals(OpenTypeClassHolder.class, result[0].getClass());
		OpenTypeClassHolder selectedResource = (OpenTypeClassHolder) result[0];
		assertEquals(resource, selectedResource);
	}

	@Test
	public void testMultipleResources() {
		String yourFilePath = "YourProject/src/YourClass.cls";
		String myFilePath = "MyProject/src/MyClass.cls";
		String myOtherFilePath = "MyProject/src/MyOtherClass.cls";

		OpenTypeClassHolder yourResource = new OpenTypeClassHolder(null, "YourProject", yourFilePath, 1);
		resources.put(yourFilePath, yourResource);

		OpenTypeClassHolder myOtherResource = new OpenTypeClassHolder(null, "MyProject", myOtherFilePath, 1);
		resources.put(myOtherFilePath, myOtherResource);

		OpenTypeClassHolder myResource = new OpenTypeClassHolder(null, "MyProject", myFilePath, 1);
		resources.put(myFilePath, myResource);

		dialog = mock(FilteredApexResourcesSelectionDialog.class);
		when(dialog.open()).thenReturn(Window.OK);
		when(dialog.getResult()).thenReturn(resources.values().toArray());

		Object[] result = dialog.getResult();

		// Should be in alphabetical order regardless of what order they are inserted 
		assertEquals(3, result.length);
		OpenTypeClassHolder selectedResource = (OpenTypeClassHolder) result[0];
		assertEquals(myResource, selectedResource);
		selectedResource = (OpenTypeClassHolder) result[1];
		assertEquals(myOtherResource, selectedResource);
		selectedResource = (OpenTypeClassHolder) result[2];
		assertEquals(yourResource, selectedResource);
	}

	@Test
	public void testListLabelProvider() {
	}

	@Test
	public void testDetailsLabelProvider() {

	}

}

