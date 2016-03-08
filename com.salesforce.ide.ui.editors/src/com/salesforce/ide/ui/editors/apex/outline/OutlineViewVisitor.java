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
package com.salesforce.ide.ui.editors.apex.outline;

import apex.jorje.semantic.ast.compilation.Compilation;
import apex.jorje.semantic.ast.compilation.UserClass;
import apex.jorje.semantic.ast.compilation.UserEnum;
import apex.jorje.semantic.ast.compilation.UserInterface;
import apex.jorje.semantic.ast.compilation.UserTrigger;
import apex.jorje.semantic.ast.visitor.AdditionalPassScope;
import apex.jorje.semantic.ast.visitor.AstVisitor;

/**
 * A visitor for collecting the top-level {@see AstNode} of the current file in the text editor. The top-level node can
 * only be any of {UserClass, UserEnum, UserInterface, UserTrigger}.
 * 
 * @author nchen
 *         
 */
public class OutlineViewVisitor extends AstVisitor<AdditionalPassScope> {
    Compilation topLevel;
    
    public boolean hasValidTopLevel() {
        return topLevel != null;
    }
    
    public Compilation getTopLevel() {
        return topLevel;
    }
    
    @Override
    public void visitEnd(UserClass node, AdditionalPassScope scope) {
        topLevel = node;
    }
    
    @Override
    public void visitEnd(UserEnum node, AdditionalPassScope scope) {
        topLevel = node;
    }
    
    @Override
    public void visitEnd(UserInterface node, AdditionalPassScope scope) {
        topLevel = node;
    }
    
    @Override
    public void visitEnd(UserTrigger node, AdditionalPassScope scope) {
        topLevel = node;
    }
}
