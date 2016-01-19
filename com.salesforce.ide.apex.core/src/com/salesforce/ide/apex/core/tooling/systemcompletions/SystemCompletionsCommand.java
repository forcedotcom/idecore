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

package com.salesforce.ide.apex.core.tooling.systemcompletions;

import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.apex.core.Messages;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.Completions;
import com.salesforce.ide.core.remote.BaseCommand;
import com.salesforce.ide.core.remote.HTTPAdapter;

/**
 * Fetches the apex completions from the server.
 * 
 * @author nchen
 * 
 */
public class SystemCompletionsCommand extends BaseCommand<Completions> {
    private static final String FETCHING_SYSTEM_COMPLETIONS = Messages.SystemCompletionsCommand_Status;
    
    public SystemCompletionsCommand(HTTPAdapter<Completions> transport) {
        super(FETCHING_SYSTEM_COMPLETIONS);
        this.transport = transport;
    }

    @Override
    protected Completions execute(IProgressMonitor monitor) throws Throwable {
        try {
            monitor.beginTask(FETCHING_SYSTEM_COMPLETIONS, 2);

            transport.send(""); //$NON-NLS-1$
            monitor.worked(1);

            Completions response = transport.receive();

            monitor.worked(1);

            return response;
        } finally {
            monitor.done();
        }
    }

}
