/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.editors.apex.outline;

import org.apache.log4j.Logger;

import apex.jorje.data.ast.BlockMember.FieldMember;
import apex.jorje.data.ast.BlockMember.InnerClassMember;
import apex.jorje.data.ast.BlockMember.InnerEnumMember;
import apex.jorje.data.ast.BlockMember.InnerInterfaceMember;
import apex.jorje.data.ast.BlockMember.MethodMember;
import apex.jorje.data.ast.BlockMember.PropertyMember;
import apex.jorje.data.ast.BlockMember.StaticStmntBlockMember;
import apex.jorje.data.ast.BlockMember.StmntBlockMember;
import apex.jorje.data.ast.ClassDecl;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.data.ast.EnumDecl;
import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.InterfaceDecl;

/**
 * This class provides some form of type sanity for the outline view. The outline view in Eclipse is dispatched based on
 * Object as its type signature, forcing us to do instanceof tests to define behavior. This class contains all the
 * expected types and will dispatch to surrogate methods that must be implemented.
 * 
 * Ideally, I would like a visitor, but the Eclipse outline view, does not expose it as such, possibly, because we want
 * to be reactive and only dispatch when the user displays a part of the outline and not visit the whole tree at once.
 * 
 * @author nchen
 */
public class OutlineViewDispatcherOld<T> {
    private static final Logger logger = Logger.getLogger(OutlineViewDispatcherOld.class);

    private IOutlineViewElementHandlerOld<T> handler;

    public OutlineViewDispatcherOld(IOutlineViewElementHandlerOld<T> handler) {
        this.handler = handler;
    }

    public T dispatch(Object element) {
        if (element instanceof TriggerDeclUnit) {
            return handler.handle((TriggerDeclUnit) element);
        } else if (element instanceof ClassDecl) {
            return handler.handle((ClassDecl) element);
        } else if (element instanceof InterfaceDecl) {
            return handler.handle((InterfaceDecl) element);
        } else if (element instanceof EnumDecl) {
            return handler.handle((EnumDecl) element);
        } else if (element instanceof InnerClassMember) {
            return handler.handle((InnerClassMember) element);
        } else if (element instanceof InnerInterfaceMember) {
            return handler.handle((InnerInterfaceMember) element);
        } else if (element instanceof InnerEnumMember) {
            return handler.handle((InnerEnumMember) element);
        } else if (element instanceof StmntBlockMember) {
            return handler.handle((StmntBlockMember) element);
        } else if (element instanceof StaticStmntBlockMember) {
            return handler.handle((StaticStmntBlockMember) element);
        } else if (element instanceof FieldMember) {
            return handler.handle((FieldMember) element);
        } else if (element instanceof MethodMember) {
            return handler.handle((MethodMember) element);
        } else if (element instanceof PropertyMember) {
            return handler.handle((PropertyMember) element);
        } else if (element instanceof Identifier) {
            return handler.handle((Identifier) element);
        } else {
            return handleUnknownElementType(element);
        }
    }

    protected T handleUnknownElementType(Object element) {
        logger.debug("Encountered an unexpected element in the outline view: " + element);
        return null;
    }
}
