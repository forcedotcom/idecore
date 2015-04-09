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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import apex.jorje.data.ast.BlockMember;
import apex.jorje.data.ast.BlockMember.InnerClassMember;
import apex.jorje.data.ast.BlockMember.InnerEnumMember;
import apex.jorje.data.ast.BlockMember.InnerInterfaceMember;

/**
 * Shows the inner clases/interfaces/enum for a class or interface.
 * 
 * Handles the case where we have null members because jADT does return partially constructed members (in that case, we
 * just don't show in outline view).
 * 
 * @author nchen
 * 
 */
public class NestedClassMemberFilter extends BlockMember.SwitchBlockWithDefault {
    private final List<Object> children;

    // We don't want to display a combination of the pseudo Inner*Members nodes and the actual body since that creates two nodes, which is ugly and redundant.
    // Instead, we want to compact it and remove the middle man.
    // Ideally, we want to remove Inner*Members but without that pseudo node, we cannot easily distinguish that the body is a nested class (not properties on ClassOrInterfaceDecl to tell us that).
    // So we keep the pseudo node and attach the members directly to it.

    public NestedClassMemberFilter() {
        this.children = new ArrayList<>();
    }

    @Override
    public void _case(InnerClassMember x) {
        if (x.body != null && x.body.members != null) {
            children.addAll(Arrays.asList(ApexOutlineContentProvider.validBlockMembers(x.body.members.values)));
        }
    }

    @Override
    public void _case(InnerInterfaceMember x) {
        if (x.body != null && x.body.members != null) {
            children.addAll(Arrays.asList(ApexOutlineContentProvider.validBlockMembers(x.body.members.values)));
        }
    }

    @Override
    public void _case(InnerEnumMember x) {
        if (x.body != null && x.body.members != null) {
            children.addAll(ApexOutlineContentProvider.validIdentifiers(x.body.members.values).toList());
        }
    }

    /*
     * The rest of the types do not have nested children
     */
    @Override
    protected void _default(BlockMember x) {}

    public List<Object> getChildren() {
        return children;
    }
}
