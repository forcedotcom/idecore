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
package com.salesforce.ide.ui.editors.apex.outline;

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
 * The handler that is called and passed the opportunity to act on each element in the outline view.
 * 
 * @author nchen
 * 
 * @param <T>
 *            The return type for handling each type of element
 */
public interface IOutlineViewElementHandlerOld<T> {

    T handle(TriggerDeclUnit element);

    T handle(ClassDecl element);

    T handle(InterfaceDecl element);

    T handle(EnumDecl element);

    T handle(InnerClassMember element);

    T handle(InnerInterfaceMember element);

    T handle(InnerEnumMember element);

    T handle(StmntBlockMember element);

    T handle(StaticStmntBlockMember element);

    T handle(FieldMember element);

    T handle(MethodMember element);

    T handle(PropertyMember element);

    T handle(Identifier element);
}
