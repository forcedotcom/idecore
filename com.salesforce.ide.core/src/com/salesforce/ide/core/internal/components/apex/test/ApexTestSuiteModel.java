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
package com.salesforce.ide.core.internal.components.apex.test;

import com.salesforce.ide.core.internal.components.generic.GenericComponentModel;
import com.salesforce.ide.core.internal.utils.Constants;

/**
 * A model for ApexTestSuite component.
 * 
 * @author jwidjaja
 */
public class ApexTestSuiteModel extends GenericComponentModel {

	public ApexTestSuiteModel() {
		super();
	}
	
	@Override
	public String getComponentType() {
		return Constants.APEX_TEST_SUITE;
	}
}
