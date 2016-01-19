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

package com.salesforce.ide.core.remote;

import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.internal.utils.Utils;

/**
 * Base command class with error handling
 * 
 * @author jwidjaja
 *
 * @param <T>
 */
public abstract class BaseCommand<T> extends PromiseableJob<T> {

	protected HTTPAdapter<T> transport;
	
	public BaseCommand(String name) {
		super(name);
	}

	protected abstract T execute(IProgressMonitor monitor) throws Throwable;
	
	public boolean wasError() {
		return Utils.isNotEmpty(transport) && Utils.isNotEmpty(transport.getResponse()) && 
				transport.getResponse().getStatus() != Response.Status.OK.getStatusCode();
	}
	
	public String getErrorMsg() {
		return Utils.isNotEmpty(transport) ? transport.getRawBodyWhenError() : null;
	}
}
