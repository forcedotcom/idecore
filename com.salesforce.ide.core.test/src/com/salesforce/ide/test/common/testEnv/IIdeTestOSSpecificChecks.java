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
package com.salesforce.ide.test.common.testEnv;

/**
 * An Interface to define any checks that might be different for different Operating systems.
 * For ex. if you want to check if folder on the file system was opened successfully or not 
 * @author ssasalatti
 */
public interface IIdeTestOSSpecificChecks {

	/**
	 * For Mac, the preferences menu is under "Eclipse" and for others, its under "Window"
	 * @return the location of the "preference" menu. 
	 *  
	 */
	public String getPreferencesMenuLocation();
}
