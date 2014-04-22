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

public interface ICodeCoverageResultExt {

	public abstract ICodeLocationExt[] getDmlInfo();

	public abstract ICodeLocationExt getDmlInfo(int i);

	public abstract String getId();

	public abstract ICodeLocationExt[] getLocationsNotCovered();

	public abstract ICodeLocationExt getLocationsNotCovered(int i);

	public abstract ICodeLocationExt[] getMethodInfo();

	public abstract ICodeLocationExt getMethodInfo(int i);

	public abstract java.lang.String getName();

	public abstract java.lang.String getNamespace();

	public abstract int getNumLocations();

	public abstract int getNumLocationsNotCovered();

	public abstract ICodeLocationExt[] getSoqlInfo();

	public abstract ICodeLocationExt getSoqlInfo(int i);

	public abstract java.lang.String getType();

}
