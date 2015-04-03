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

import java.util.List;

import apex.jorje.data.ast.BlockMember.StaticStmntBlockMember;
import apex.jorje.data.ast.BlockMember.StmntBlockMember;

/**
 * This special cases for TriggerDeclUnit. TriggerDeclUnit is a bit weird and it can actually contain statements at the
 * top-level. We want to filter those out.
 * 
 * @author nchen
 * 
 */
class TriggerMemberFilter extends BlockMemberFilter {

    public TriggerMemberFilter(List<Object> children) {
        super(children);
    }

    @Override
    public void _case(StaticStmntBlockMember x) {
        // do nothing
    }

    @Override
    public void _case(StmntBlockMember x) {
        // do nothing
    }

}
