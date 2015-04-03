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
 * Represents an exception occured while trying to create a compilation unit.
 * 
 * @author nchen
 * 
 */
public class InvalidCompilationASTException extends Exception {
    private static final long serialVersionUID = 5426757522059333795L;

    public InvalidCompilationASTException(Exception e) {
        super(e);
    }

}
