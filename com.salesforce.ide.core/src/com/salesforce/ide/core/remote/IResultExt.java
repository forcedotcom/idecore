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
package com.salesforce.ide.core.remote;

public interface IResultExt {

	public abstract String getName();

	public abstract void setName(String name);

	public abstract String getNamespace();

	public abstract void setNamespace(String namespace);

	public abstract boolean isSuccess();

	public abstract boolean isApexClass();

	public abstract boolean isApexTrigger();

	public abstract String getProblem();

}
