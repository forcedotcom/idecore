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
package com.salesforce.ide.deployment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.google.common.collect.Lists;
import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.api.metadata.types.PackageTypeMembers;

import junit.framework.TestCase;

public class PackageManifestTest_unit extends TestCase {

	private static final String CUSTOM_OBJECT = "CustomObject";
	private static final String TYPE_THAT_IS_NOT_HERE = "BogusType";
	private static final String ASTERISK_TYPE = "ItsAllHere";
	private Package metadata;
	private PackageManifest manifest;
	private List<PackageTypeMembers> typeList;
	private PackageTypeMembers customObjectType;
	private PackageTypeMembers asteriskType;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		metadata = mock(Package.class);
		customObjectType = mock(PackageTypeMembers.class);
		asteriskType = mock(PackageTypeMembers.class);
		typeList = Lists.newArrayList(customObjectType, asteriskType);
		when(metadata.getTypes()).thenReturn(typeList );
		when(customObjectType.getMembers()).thenReturn(Lists.newArrayList("pizza"));
		when(customObjectType.getName()).thenReturn(CUSTOM_OBJECT);
		when(asteriskType.getMembers()).thenReturn(Lists.newArrayList("*"));
		when(asteriskType.getName()).thenReturn(ASTERISK_TYPE);

		manifest = new PackageManifest(metadata);
	}
	
	public void testDoesContain() throws Exception {
		boolean contains = manifest.contains(CUSTOM_OBJECT, "pizza");
		assertTrue(contains);
	}
	
	public void testDoesNotContain() throws Exception {
		boolean contains = manifest.contains(CUSTOM_OBJECT, "lasagna");
		assertFalse(contains);
	}
	
	public void testDoesNotContainWrongType() throws Exception {
		boolean contains = manifest.contains(TYPE_THAT_IS_NOT_HERE, "pizza");
		assertFalse(contains);
	}
	
	public void testDoesContainWithAsterisk() throws Exception {
		boolean contains = manifest.contains(ASTERISK_TYPE, "04984508");
		assertTrue(contains);
	}
}
