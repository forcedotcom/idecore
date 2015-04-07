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
package com.salesforce.ide.core.compatibility.auth.equinox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

import com.salesforce.ide.core.compatibility.auth.IAuthorizationService;

public class EquinoxAuthorization implements IAuthorizationService {
	private static final Logger logger = Logger.getLogger(EquinoxAuthorization.class);
	private static final String PROP_PASSWORD = "password";
	private static final String PROP_TOKEN = "token";
	private static final String FORCE_PROJECT = "Force Projects";
	private ISecurePreferences root;

	@Override
    public void addAuthorizationInfo(String AUTH_URL, IProject project, String AUTH_TYPE, Map<String, String> credentialMap) {
		root = SecurePreferencesFactory.getDefault();
		ISecurePreferences node = root.node(FORCE_PROJECT + "/" + project.getName());
		try {
			logger.info("Saving values in secure storage");
			node.put(PROP_PASSWORD, credentialMap.get(PROP_PASSWORD).toString(), true);
			node.put(PROP_TOKEN, credentialMap.get(PROP_TOKEN).toString(), true);

		} catch (StorageException e) {
			logger.error("Unable to store values in equinox secure storage", e);
		}

	}

	@Override
    public String getPassword(IProject project, String AUTH_URL, String AUTH_TYPE) {
		ISecurePreferences node = root.node(FORCE_PROJECT + "/" + project.getName());
		try {
			return node.get(PROP_PASSWORD, "");
		} catch (StorageException e) {
			logger.error("Unable to retrive values from equinox secure storage", e);
		}
		return null;
	}

	@Override
    public Map<String, String> getCredentialMap(URL url, String projectName, String authType) {
		try {
			ISecurePreferences root = SecurePreferencesFactory.getDefault();
			ISecurePreferences node = root.node(FORCE_PROJECT + "/" + projectName);
			Map<String, String> credentialMap = new HashMap<>();
			credentialMap.put(PROP_PASSWORD, node.get(PROP_PASSWORD, ""));
			credentialMap.put(PROP_TOKEN, node.get(PROP_TOKEN, ""));
			return credentialMap;

		} catch (StorageException e) {
			logger.error("Unable to store values in equinox security storage", e);
		}
		return null;
	}
}
