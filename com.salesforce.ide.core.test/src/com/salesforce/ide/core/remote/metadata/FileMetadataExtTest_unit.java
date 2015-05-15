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
package com.salesforce.ide.core.remote.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.sforce.soap.metadata.FileProperties;

public class FileMetadataExtTest_unit extends TestCase {

	private static final String CUSTOM_OBJECT = "CustomObject";
	private static final String STANDARD_OBJECT = "StandardObject";

	public void testGetComponentNamesByComponentType() throws Exception {
		FileProperties fileProps = new FileProperties();
		fileProps.setType(CUSTOM_OBJECT);
		fileProps.setFullName("Account");
		FileMetadataExt ext = new FileMetadataExt(fileProps);
		List<String> componentNamesByComponentType = ext
				.getComponentNamesByComponentType(CUSTOM_OBJECT);
		assertTrue(componentNamesByComponentType.isEmpty());
	}

	public void testGetComponentTypes() throws Exception {
	    FileProperties fileProps1 = new FileProperties();
        fileProps1.setType(CUSTOM_OBJECT);
        fileProps1.setFullName("foo__c");

        FileProperties fileProps2 = new FileProperties();
        fileProps2.setType(CUSTOM_OBJECT);
        fileProps2.setFullName("foo__kav");

        final Set<String> componentTypes = new FileMetadataExt(fileProps1, fileProps2).getComponentTypes();
        assertNotNull(componentTypes);
        assertEquals(1, componentTypes.size());
        assertTrue(componentTypes.contains(CUSTOM_OBJECT));

        FileProperties fileProps3 = new FileProperties();
        fileProps3.setType(CUSTOM_OBJECT);
        fileProps3.setFullName("Account");
        final Set<String> componentTypes3 = new FileMetadataExt(fileProps3).getComponentTypes();
        assertNotNull(componentTypes3);
        assertEquals(1, componentTypes3.size());
        assertTrue(componentTypes3.contains(STANDARD_OBJECT));

        FileProperties fileProps4 = new FileProperties();
        fileProps4.setType("foo");
        fileProps4.setFullName("Account");
        final Set<String> componentTypes4 = new FileMetadataExt(fileProps4).getComponentTypes();
        assertNotNull(componentTypes4);
        assertEquals(1, componentTypes4.size());
        assertTrue(componentTypes4.contains("foo"));


    }

	public void testSort() throws Exception {
		FileProperties fileProps = new FileProperties();
		fileProps.setType("ABC");

		FileProperties fileProps2 = new FileProperties();
		fileProps2.setType("def");

		FileMetadataExt ext = new FileMetadataExt(fileProps, fileProps2);
		ext.sort(FileMetadataExt.SORT_BY_TYPE);
		assertEquals("ABC", ext.getFileProperties()[0].getType());
		assertEquals("def", ext.getFileProperties()[1].getType());
	}

	public void testGetFilePropertiesMap() throws Exception {
		FileProperties fileProp1 = new FileProperties();
		fileProp1.setType(CUSTOM_OBJECT);
		fileProp1.setFullName("foo__c");

		FileProperties fileProp2 = new FileProperties();
		fileProp2.setType(CUSTOM_OBJECT);
		fileProp2.setFullName("foo__kav");

		FileProperties fileProp3 = new FileProperties();
		fileProp3.setType(CUSTOM_OBJECT);
		fileProp3.setFullName("Account");

		FileMetadataExt ext= new FileMetadataExt(fileProp1,fileProp2,fileProp3);
		Map<String, List<FileProperties>> result = ext.getFilePropertiesMap();
		Set<String> keySet = result.keySet();
		assertEquals(2,keySet.size());
		assertTrue(keySet.contains(STANDARD_OBJECT));
		assertTrue(keySet.contains(CUSTOM_OBJECT));
		Collection<String> customObjectList = Collections2.transform(result.get(CUSTOM_OBJECT), new Function<FileProperties, String>(){

			public String apply(FileProperties arg0) {
				return arg0.getFullName();
			}

		});
		assertEquals(2,customObjectList.size());
		assertTrue(customObjectList.contains("foo__c"));
		assertTrue(customObjectList.contains("foo__kav"));
		List<FileProperties> stdObjList = result.get(STANDARD_OBJECT);
		assertEquals(1,stdObjList.size());
		assertEquals("Account",stdObjList.get(0).getFullName());
	}
}

