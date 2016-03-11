/*******************************************************************************
* Copyright (c) 2016 Salesforce.com, inc..
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors:
*     Salesforce.com, inc. - initial API and implementation
*******************************************************************************/
package com.salesforce.ide.apex.internal.core.db;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import com.salesforce.ide.apex.core.ApexCore;

import apex.jorje.ide.db.api.GraphHandleProvider;

/**
 * Provides the location to store the database for the project.
 * 
 * @author nchen
 *
 */
public final class EclipseGraphHandleProvider implements GraphHandleProvider {
	private final IProject project;

	public EclipseGraphHandleProvider(IProject project) {
		this.project = project;
	}

	private IPath getStateLocation() {
		return ApexCore.getDefault().getStateLocation();
	}

	private IPath getProjectGraphLocation() {
		return getStateLocation().append(project.getName());
	}

	@Override
	public String getHandle() {
		return getProjectGraphLocation().toOSString();
	}

}
