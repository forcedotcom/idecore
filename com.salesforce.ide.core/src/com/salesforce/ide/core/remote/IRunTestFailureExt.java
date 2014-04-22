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

public interface IRunTestFailureExt {

	public abstract String getId();

	public abstract java.lang.String getMessage();

	public abstract java.lang.String getMethodName();

	public abstract java.lang.String getName();

	public abstract java.lang.String getNamespace();

	public abstract java.lang.String getPackageName();

	public abstract java.lang.String getStackTrace();

	public abstract double getTime();

	public abstract java.lang.String getType();

}
