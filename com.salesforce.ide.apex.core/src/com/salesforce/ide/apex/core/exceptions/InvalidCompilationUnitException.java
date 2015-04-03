/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.apex.core.exceptions;

/**
 * Represents an exception occurred while trying to create a compilation AST node.
 * 
 * @author nchen
 * 
 */
public class InvalidCompilationUnitException extends Exception {
    private static final long serialVersionUID = -4913592430179727141L;

    public InvalidCompilationUnitException(Exception e) {
        super(e);
    }

}
