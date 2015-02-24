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
package com.salesforce.ide.core.compatibility.auth;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IProject;

/**
 * IAuthroizationService acts a compatibility (interface) layer for eclipse platform 3.3 and 3.4 (and above)
 * The introduction of equinox security storage API,made the old platform-db method of storage obsolete.In order
 * to maintain backward compatibility IAuthorization Service interface acts as a adapter.
 * The methods retain the older signatures for compatibility.
 * reasons.
 * 
 */

public interface IAuthorizationService {

	/**
	 * Adds authorization information to secure storage.Uses equinox or platorm-db to store 
	 * sensitive information(e.g password)
	 * @param url
	 * @param project
	 * @param authType
	 * @param credentialMap
	 */
	void addAuthorizationInfo(String url, IProject project, String authType, Map<String, String> credentialMap);

	/**
	 * Returns the map of credentials stored in secure storage
	 * @param url
	 * @param projectName
	 * @param authType
	 * @return credentialMap
	 */
	Map<String, String> getCredentialMap(URL url, String projectName, String authType);

	/**
	 * Returns the password saved in secure storage.
	 * @param project
	 * @param strUrl
	 * @param authType
	 * @return 
	 */
	String getPassword(IProject project, String strUrl, String authType);
}
