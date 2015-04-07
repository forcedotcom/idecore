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
package com.salesforce.ide.ui.sync;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.salesforce.ide.core.model.Component;

class ComponentStorage implements IStorage {

	private final Component component;

	ComponentStorage(Component component) {
		this.component = component;
	}

	@Override
    public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(component.getFile());
	}

	@Override
    public IPath getFullPath() {
		IFile file = component.getFileResource();
		if (file == null) {
			return null;
		}
		return file.getFullPath();
	}

	@Override
    public String getName() {
		return component.getName();
	}

	@Override
    public boolean isReadOnly() {
		return true;
	}

	@Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}
}
