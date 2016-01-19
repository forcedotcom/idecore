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

package com.salesforce.ide.core.remote.tooling;

import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.remote.PromiseableJob;

/**
 * Base command class to enforce error handling
 * 
 * @author jwidjaja
 *
 * @param <T>
 */
public abstract class BaseCommandWithErrorHandling<T> extends PromiseableJob<T> {

	public BaseCommandWithErrorHandling(String name) {
		super(name);
	}

	protected abstract T execute(IProgressMonitor monitor) throws Throwable;
	public abstract boolean wasError();
	public abstract String getErrorMsg();
}
