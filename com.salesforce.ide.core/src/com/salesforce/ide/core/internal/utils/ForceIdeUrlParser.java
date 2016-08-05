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

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.SalesforceEndpoints;

public class ForceIdeUrlParser{
	
	@SuppressWarnings("unused")
	private ForceIdeUrlParser(){}
	
	private String url="";
	private String orgName="";
	private boolean isSecure=true;
	private String sessionId="";
	private String address="";
	private String username="";
	private String password="";
	private String packageName="";
	private String command="";
	private boolean isParsed = false;
	final String ENCODE_FORMAT = "UTF-8";

	
	public ForceIdeUrlParser(String url){
		this.url = url;
	}
	
	private String extractInnerString(final String preFix, final String suffix, final boolean canTerminate) throws UnsupportedEncodingException{
		
		String innerValue = "";
		int startNdx = url.toLowerCase().indexOf(preFix.toLowerCase());
		if (-1 == startNdx){
			return innerValue;
		}
		
		startNdx += preFix.length();
		
		int endNdx = url.toLowerCase().indexOf(suffix.toLowerCase(), startNdx);
		if (-1 == endNdx){
			if (canTerminate){
				endNdx = url.length();
			}else{
				return innerValue;
			}
		}
		innerValue = url.substring(startNdx, endNdx);
		
		return URLDecoder.decode(innerValue, ENCODE_FORMAT);
	}
	
	
	//eg) forceide://subscriberDebugProject/?dbaker-wsm%3A6109&cmd=createproject&secure=0&sessionId=111222333444555666777
	//   or
	//eg) forceide://subscriberDebugProject/?dbaker-wsm%3A6109&cmd=createproject&secure=0&un=obama@whitehouse.gov&pw=I<3michelle
	private boolean parse(){
	
		try{
		    this.orgName = 		extractInnerString("forceIde://", 	"/?", 	false);
			this.username = 	extractInnerString("un=", 			"&", 	true);
			this.password = 	extractInnerString("pw=", 			"&", 	true);
			this.address = 		extractInnerString("url=", 		    "&", 	true);
			this.isSecure = 	extractInnerString("secure=", 		"&", 	true).equals("0") ? false : true;
			this.sessionId = 	extractInnerString("sessionId=", 	"&", 	true);
			this.packageName = 	extractInnerString("package=", 		"&", 	true);
			this.command = 		extractInnerString("cmd=", 		    "&", 	true);
			
		}catch(UnsupportedEncodingException Uee){
			
		}
		
		isParsed = true;		
		return true;
	}		

	public String getSessionId(){
		if (! isParsed)
			parse();
		return sessionId;
	}

	public String getOrgName(){
		if (! isParsed)
			parse();
		return orgName;
	}
	
	public boolean getIsSecure(){
		if (! isParsed)
			parse();
		return isSecure;
	}
	
	public String getAddress(){
		if (! isParsed)
			parse();
		return address;
	}
	
	public String getUsernamne(){
		if (! isParsed)
			parse();
		return username;
	}

	public String getPassword(){
		if (! isParsed)
			parse();
		return password;
	}
	public String getCommand(){
		if (! isParsed)
			parse();
		return command;		
	}
	public String getPackageName(){
		if (! isParsed)
			parse();
		return packageName;		
	}
	private boolean isSet(final String value){
		return value != null && !value.isEmpty();
	}
	public boolean isValid(){
		if (!isParsed)
			parse();
		
		return isSet(this.address) && isSet(orgName) && isSet(command) &&
			 (isSet(this.sessionId) ||
			  (isSet(this.username) && isSet(this.password))
			 );
	}
	
    private static SalesforceEndpoints getSalesforceEndpoints() {
        return ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getSalesforceEndpoints();
    }
	
	public ForceProject asForceProject(){
		
        SalesforceEndpoints sfEndpoints = getSalesforceEndpoints();		
        ForceProject forceProject = new ForceProject();

        if (!packageName.isEmpty())
        	forceProject.setPackageName(packageName);
        
        forceProject.setEndpointServer(getAddress());
        if (!sessionId.isEmpty())
        	forceProject.setSessionId(getSessionId());
        if (!username.isEmpty())
        	forceProject.setUserName(getUsernamne());
        if (!password.isEmpty())
        	forceProject.setPassword(getPassword());
        
        forceProject.setHttpsProtocol(getIsSecure());
        forceProject.setEndpointApiVersion(sfEndpoints.getDefaultApiVersion());
        forceProject.setMetadataFormatVersion(sfEndpoints.getDefaultApiVersion());
        
        return forceProject;
	}
	
	public class ForceIdeUrlException extends Exception{
		private static final long serialVersionUID = 1L;
		private String message;
		public String getMessage(){
			return message;
		}
		ForceIdeUrlException (final String message){
			this.message = message; 
		}
	}
	
	public class COMMANDS{
		final String CREATE_PROJECT = "createproject";
	}
	
} //ForceIdeUrlParser


