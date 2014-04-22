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
package com.salesforce.ide.core.project;

import org.eclipse.core.resources.IFile;

import com.salesforce.ide.core.model.Component;

public class BuilderException extends Exception {
	
	private static final long serialVersionUID = 1L;

    private IFile file;
    private Component component;

    public BuilderException() {
        super();
    }
    
    public BuilderException(String message, Throwable th) {
        super(message, th);
    }

    public BuilderException(Component component, IFile file, Throwable cause) {
        super(cause);
        this.file = file;
        this.component = component;
    }

    public BuilderException(String message) {
        super(message);
    }

    public BuilderException(Throwable cause) {
        super(cause);
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }
    
    public IFile getFile() {
        return file;
    }
    
    public void setFile(IFile file) {
        this.file = file;
    }

}
