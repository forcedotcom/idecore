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

package com.salesforce.ide.core.internal.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.lang3.CharEncoding;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.SalesforceEndpoints;

public class ForceIdeUrlParser {
	
	private String url = "";
	private String orgName = "";
	private String orgId = "";
	private boolean isSecure = true;
	private String sessionId = "";
	private String address = "";
	private String username = "";
	private String password = "";
	private String packageName = "";
	private String command = "";
	private boolean isParsed = false;

	@SuppressWarnings("unused")
	private ForceIdeUrlParser() {	
	}
	
	public ForceIdeUrlParser(String url) {
		if (url != null) {
			this.url = url;
		}
	}

	private String extractInnerString(final String preFix, final String suffix, final boolean canTerminate)
			throws UnsupportedEncodingException {

		String innerValue = "";
		int startNdx = url.toLowerCase().indexOf(preFix.toLowerCase());
		if (-1 == startNdx) {
			return innerValue;
		}

		startNdx += preFix.length();

		int endNdx = url.toLowerCase().indexOf(suffix.toLowerCase(), startNdx);
		if (-1 == endNdx) {
			if (canTerminate) {
				endNdx = url.length();
			} else {
				return innerValue;
			}
		}
		innerValue = url.substring(startNdx, endNdx);

		return URLDecoder.decode(innerValue, CharEncoding.UTF_8);
	}

	// eg)
	// forceide://subscriberDebugProject/?myMachine%3A1234&cmd=createproject&secure=0&sessionId=111222333444555666777
	// or
	// eg)
	// forceide://subscriberDebugProject/?myMachine%3A1234&cmd=createproject&secure=0&un=foo@bar.com&pw=foobar
	private boolean parse() {
		try {
			this.orgName = extractInnerString("forceIde://", "/?", false);
			this.username = extractInnerString("un=", "&", true);
			this.password = extractInnerString("pw=", "&", true);
			this.address = extractInnerString("url=", "&", true);
			this.isSecure = extractInnerString("secure=", "&", true).equals("0") ? false : true;
			this.sessionId = extractInnerString("sessionId=", "&", true);
			this.orgId = extractInnerString("sessionId=", "!", false);
			this.packageName = extractInnerString("package=", "&", true);
			this.command = extractInnerString("cmd=", "&", true);
		} catch (UnsupportedEncodingException Uee) {}

		isParsed = true;
		return true;
	}

	public String getSessionId() {
		if (!isParsed)
			parse();
		return sessionId;
	}

	public String getOrgName() {
		if (!isParsed)
			parse();
		return orgName;
	}
	
	public String getOrgId() {
		if (!isParsed) {
			parse();
		}
		return orgId;
	}

	public boolean getIsSecure() {
		if (!isParsed)
			parse();
		return isSecure;
	}

	public String getAddress() {
		if (!isParsed)
			parse();
		return address;
	}

	public String getUsernamne() {
		if (!isParsed)
			parse();
		return username;
	}

	public String getPassword() {
		if (!isParsed)
			parse();
		return password;
	}

	public String getCommand() {
		if (!isParsed)
			parse();
		return command;
	}

	public String getPackageName() {
		if (!isParsed)
			parse();
		return packageName;
	}

	private boolean isSet(final String value) {
		return value != null && !value.isEmpty();
	}

	public boolean isValid() {
		if (!isParsed)
			parse();

		return isSet(this.address) && isSet(orgName) && isSet(command)
				&& (isSet(this.sessionId) || (isSet(this.username) && isSet(this.password)));
	}

	private static SalesforceEndpoints getSalesforceEndpoints() {
		return ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getSalesforceEndpoints();
	}

	public ForceProject asForceProject() {

		SalesforceEndpoints sfEndpoints = getSalesforceEndpoints();
		ForceProject forceProject = new ForceProject();
		
		if (!packageName.isEmpty())
			forceProject.setPackageName(packageName);
		if (!sessionId.isEmpty())
			forceProject.setSessionId(getSessionId());
		if (!username.isEmpty())
			forceProject.setUserName(getUsernamne());
		if (!password.isEmpty())
			forceProject.setPassword(getPassword());

		forceProject.setEndpointServer(getAddress());
		forceProject.setHttpsProtocol(getIsSecure());
		forceProject.setEndpointApiVersion(sfEndpoints.getDefaultApiVersion());
		forceProject.setMetadataFormatVersion(sfEndpoints.getDefaultApiVersion());
		forceProject.setEndpointEnvironment("Other (Specify)");

		return forceProject;
	}
}
