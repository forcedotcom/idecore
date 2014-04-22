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
package com.salesforce.ide.core.factories;

public class FactoryException extends Exception {

    private static final long serialVersionUID = 1L;

    public FactoryException() {
        super();
    }

    public FactoryException(String message, Throwable th) {
        super(message, th);
    }

    public FactoryException(String message) {
        super(message);
    }
    
    public FactoryException(Throwable th) {
        super(th);
    }
}
