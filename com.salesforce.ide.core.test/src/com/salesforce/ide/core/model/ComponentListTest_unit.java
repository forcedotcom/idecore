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
package com.salesforce.ide.core.model;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;

import junit.framework.TestCase;

public class ComponentListTest_unit extends TestCase {
	private ComponentFactory componentFactory;

	public void testDocumentFolder() throws Exception {
		ComponentList componentList = new ComponentList();
		componentList.add(componentFactory.getComponentByComponentType(Constants.DOCUMENT));
		assertEquals(1, componentList.size());
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		componentFactory = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory();
	}
	
	

}
