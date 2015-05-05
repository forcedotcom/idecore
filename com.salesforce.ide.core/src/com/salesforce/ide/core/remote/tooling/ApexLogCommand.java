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

package com.salesforce.ide.core.remote.tooling;

import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.remote.IHTTPTransport;
import com.salesforce.ide.core.remote.PromiseableJob;

/**
 * A Job to retrieve the body of Tooling API's ApexLog
 * 
 * @author jwidjaja
 *
 */
public class ApexLogCommand extends PromiseableJob<String> {
	
	private static final String GET_APEX_LOG = "Getting Apex Log";
	
	private final IHTTPTransport<String> transport;

	public ApexLogCommand(IHTTPTransport<String> transport) {
		super(GET_APEX_LOG);
		this.transport = transport;
	}

	/**
	 * Execute the HTTP request and return the ApexLog body.
	 */
	@Override
	protected String execute(IProgressMonitor monitor) throws Throwable {
		try {
            monitor.beginTask(GET_APEX_LOG, 2);

            transport.send("");
            monitor.worked(1);

            String response = transport.receive();
            monitor.worked(1);

            return response;
        } finally {
            monitor.done();
        }
	}
}
