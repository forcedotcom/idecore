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

package com.salesforce.ide.core.remote.tooling;

import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.remote.BaseCommand;
import com.salesforce.ide.core.remote.HTTPAdapter;

/**
 * A Job to query with Tooling API through REST.
 * This is an alternative to ToolingConnection.query() SOAP call because
 * SFDC-WSC has its...quirks.
 * 
 * @author jwidjaja
 *
 */
public class ToolingQueryCommand extends BaseCommand<String> {

	private static final String QUERYING = "Querying";
	
	public ToolingQueryCommand(HTTPAdapter<String> transport) {
		super(QUERYING);
		this.transport = transport;
	}

	/**
	 * HTTP GET the query and return the query results
	 */
	@Override
	protected String execute(IProgressMonitor monitor) throws Throwable {
		try {
            monitor.beginTask(QUERYING, 2);

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
