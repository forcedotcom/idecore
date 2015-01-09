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
package com.salesforce.ide.apex.core;

import apex.jorje.semantic.ast.AstNode;

/**
 * Convenience methods for navigating through the AST.
 * 
 * @author nchen
 * 
 */
public class ASTUtil {

    // Line number based
    ////////////////////

    /**
     * @param startingPoint
     *            The starting AstNode to begin searching from.
     * @param node
     *            A class that represents the type we are looking for.
     * @param lineNumber
     *            The line number to start from (1-based)
     * @return The closest node of that type or null
     */
    public AstNode closestAncestor(AstNode startingPoint, Class<?> typeRef, int lineNumber) {
        return null;
    }
}
