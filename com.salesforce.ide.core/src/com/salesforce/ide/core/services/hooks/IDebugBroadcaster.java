/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/

package com.salesforce.ide.core.services.hooks;

import org.eclipse.core.resources.IProject;

/**
 * Broadcast debugging info
 * 
 * @author jwidjaja
 *
 */
public interface IDebugBroadcaster {
	
	/**
	 * Return whether or not the project has an active debugging session
	 */
	public boolean isDebuggingActive(IProject project);
}
