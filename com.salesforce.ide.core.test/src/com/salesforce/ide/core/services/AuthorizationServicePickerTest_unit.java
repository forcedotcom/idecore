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

import junit.framework.TestCase;

import com.salesforce.ide.core.compatibility.auth.IAuthorizationService;
import com.salesforce.ide.core.compatibility.auth.equinox.EquinoxAuthorization;

public class AuthorizationServicePickerTest_unit extends TestCase {		
	public void testOnlyUsePlatformForNow() throws Exception {
		AuthorizationServicePicker factory = new AuthorizationServicePicker();
		IAuthorizationService result = factory.makeAuthorizationService();
		assertTrue(result instanceof EquinoxAuthorization);
	}
}
