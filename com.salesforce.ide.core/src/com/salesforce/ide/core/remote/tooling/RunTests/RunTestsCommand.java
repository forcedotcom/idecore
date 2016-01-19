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

package com.salesforce.ide.core.remote.tooling.RunTests;

import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.remote.BaseCommand;
import com.salesforce.ide.core.remote.HTTPAdapter;

/**
 * A Job to enqueue tests through Tooling API's runTestsAsynchronous
 * 
 * @author jwidjaja
 *
 */
public class RunTestsCommand extends BaseCommand<String> {

	private static final String SCHEDULE_TESTS = "Scheduling Apex Tests";
	
	private final String tests;

	public RunTestsCommand(HTTPAdapter<String> transport, String tests) {
		super(SCHEDULE_TESTS);
		this.transport = transport;
		this.tests = tests;
	}
	
	/**
	 * Execute the HTTP request and return test run ID if the request was okay.
	 */
	@Override
	protected String execute(IProgressMonitor monitor) throws Throwable {
		try {
            monitor.beginTask(SCHEDULE_TESTS, 2);

            transport.send(tests);
            monitor.worked(1);

            String response = transport.receive();
            monitor.worked(1);

            return response;
        } finally {
            monitor.done();
        }
	}
}
