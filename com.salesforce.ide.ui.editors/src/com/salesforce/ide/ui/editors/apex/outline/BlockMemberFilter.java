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

import java.util.Arrays;
import java.util.List;

import apex.jorje.data.ast.BlockMember;
import apex.jorje.data.ast.BlockMember.FieldMember;
import apex.jorje.data.ast.BlockMember.InnerClassMember;
import apex.jorje.data.ast.BlockMember.InnerEnumMember;
import apex.jorje.data.ast.BlockMember.InnerInterfaceMember;
import apex.jorje.data.ast.BlockMember.MethodMember;
import apex.jorje.data.ast.BlockMember.PropertyMember;
import apex.jorje.data.ast.VariableDecl;
import apex.jorje.data.ast.VariableDecls;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.salesforce.ide.ui.editors.apex.outline.icon.OutlineViewIconProvider;
import com.salesforce.ide.ui.editors.apex.outline.text.OutlineViewElementTextProvider;

/**
 * Displays the block members for ClassOrInterfaceDecl.
 * 
 * <ul>
 * <li>Rewrites certain nodes to fit better with the outline view model.</li>
 * <li>Handles the case where we have null members because jADT does return partially constructed members (in that case,
 * we just don't show in outline view).</li>
 * 
 * My strategy for handling the null members in partially initialized is to check what we use to print the text label in
 * {@link OutlineViewElementTextProvider} and ensure that they are not null. We can avoid checking for thing used by
 * {@link OutlineViewIconProvider} because that just uses the modifiers. And the modifiers are always available (because
 * we parse from left to right). This is specific to the outline and not how you would handle it in general.
 * 
 * @author nchen
 * 
 */
class BlockMemberFilter extends BlockMember.SwitchBlockWithDefault {

    final private List<Object> children;

    public BlockMemberFilter(List<Object> children) {
        this.children = children;
    }

    /*
     * This method "rewrites"/"denormalizes" the FieldClassMembers into a list of FieldClassMembers.
     * Example:
     * public int a, b, c;
     * becomes
     * public int a;
     * public int b;
     * public int c;
     * 
     * This does not change the real structure, it just changes the representation that we are using 
     * so that it fits better with the outline view.
     * 
     * The reason for doing this is that algebraic data types do not have a parent-child relation.
     * Thus, it is necessary to retain all the information in a node (through containment) since the 
     * child node has no way to reference its parent to get more info.
     */
    @Override
    public void _case(final FieldMember x) {
        final VariableDecls toCopyFrom = x.variableDecls;

        children.addAll(FluentIterable.from(toCopyFrom.decls).filter(new Predicate<VariableDecl>() {

            @Override
            public boolean apply(VariableDecl variableDecl) {
                return (variableDecl != null && variableDecl.name != null && x.variableDecls.type != null);
            }
        }).transform(new Function<VariableDecl, Object>() {

            @Override
            public Object apply(VariableDecl variableDecl) {
                return BlockMember._FieldMember(VariableDecls._VariableDecls(toCopyFrom.modifiers, toCopyFrom.type,
                    Arrays.asList(variableDecl)));
            }
        }).toList());

    }

    @Override
    public void _case(MethodMember x) {
        if (x.methodDecl != null && x.methodDecl.name != null && x.methodDecl.formalParameters != null) {
            children.add(x);
        }
    }

    @Override
    public void _case(PropertyMember x) {
        if (x.propertyDecl != null && x.propertyDecl.name != null && x.propertyDecl.type != null) {
            children.add(x);
        }
    }

    @Override
    public void _case(InnerClassMember x) {
        if (x.body != null && x.body.name != null) {
            children.add(x);
        }
    }

    @Override
    public void _case(InnerInterfaceMember x) {
        if (x.body != null && x.body.name != null) {
            children.add(x);
        }
    }

    @Override
    public void _case(InnerEnumMember x) {
        if (x.body != null && x.body.name != null) {
            children.add(x);
        }
    }

    // For the remaining two cases of StmntBlockMember and StaticStmntBlockMember we just add them
    @Override
    protected void _default(BlockMember x) {
        children.add(x);
    }
}
