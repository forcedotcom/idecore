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
package com.salesforce.ide.core.factories;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.api.metadata.types.PackageTypeMembers;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.test.common.utils.IdeTestUtil;

/**
 * @author ssasalat
 *
 */
public class PackageManifestFactoryTest_unit extends TestCase {

	private PackageManifestFactory factory;
	private Package manifest;
	private PackageTypeMembers customObjectMember;
	private PackageTypeMembers apexClassMember;
	private Component component;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		factory = new PackageManifestFactory();
		manifest = new Package();

		component = mock(Component.class);
		when(component.isPackageManifest()).thenReturn(false);

		customObjectMember = new PackageTypeMembers();
		customObjectMember.setName(Constants.CUSTOM_OBJECT);
		manifest.getTypes().add(customObjectMember);
		
		apexClassMember = new PackageTypeMembers();
		apexClassMember.setName(Constants.APEX_CLASS);
		manifest.getTypes().add(apexClassMember);
	}
	
	
	/**
	 * Verify that a custom object is inserted into the manifest correctly.
	 */
	public void testAddCustomObject() {
		final String customObjectName = "a custom object";

		when(component.getName()).thenReturn(customObjectName);
		when(component.getComponentType()).thenReturn(Constants.CUSTOM_OBJECT);
		
		factory.addComponentToManifest(manifest, component);
		
		assertTrue(customObjectMember.getMembers().contains(customObjectName));
	}
	
	/**
	 * Verify that standard objects are inserted into the deployment manifest
	 * in the same type member used for custom objects. 
	 */
	public void testAddStandardObject() {
		final String stdObjectName = "a Std object";

		when(component.getName()).thenReturn(stdObjectName);
		when(component.getComponentType()).thenReturn(Constants.STANDARD_OBJECT);

		factory.addComponentToManifest(manifest, component);
	
		assertTrue(customObjectMember.getMembers().contains(stdObjectName));
		assertTrue(customObjectMember.getName().equals(Constants.CUSTOM_OBJECT));
	}

	/**
	 * Verify that one more kind of object gets inserted correctly, because
	 * our other two tests both insert into the same type member (if they run correctly, that is).
	 * 
	 * @throws Exception
	 */
	public void testAddSomeOtherKindOfObject() throws Exception {
		String componentName = "an apex class";
		when(component.getName()).thenReturn(componentName);
		when(component.getComponentType()).thenReturn(Constants.APEX_CLASS);

		factory.addComponentToManifest(manifest, component);
		
		assertTrue(apexClassMember.getMembers().contains(componentName));
		assertTrue(apexClassMember.getName().equals(Constants.APEX_CLASS));
	}
	
    // W-1773067
    // We can remove this after we are done deciding how to support this new type
	public void testInstalledPackageExcluded() {
	    PackageManifestFactory packageManifestFactory = IdeTestUtil.getPackageManifestFactory();
	    assertTrue("InstalledPackage component is not excluded", packageManifestFactory.getDefaultDisabledRetrieveComponentTypes().contains("InstalledPackage"));
	}
}
