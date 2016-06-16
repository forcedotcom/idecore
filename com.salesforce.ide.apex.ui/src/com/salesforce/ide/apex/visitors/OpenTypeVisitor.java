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
package com.salesforce.ide.apex.visitors;

import java.util.Map;

import com.google.common.collect.Maps;

import apex.jorje.data.Location;
import apex.jorje.data.Locations;
import apex.jorje.semantic.ast.AstNode;
import apex.jorje.semantic.ast.compilation.UserClass;
import apex.jorje.semantic.ast.compilation.UserEnum;
import apex.jorje.semantic.ast.compilation.UserInterface;
import apex.jorje.semantic.ast.compilation.UserTrigger;
import apex.jorje.semantic.ast.visitor.AdditionalPassScope;
import apex.jorje.semantic.ast.visitor.AstVisitor;

/**
 * This visits all the user classes (class and inner class) and triggers 
 * to prepare for the open apex type dialog.
 * 
 * @author wchow
 *
 */
public class OpenTypeVisitor extends AstVisitor<AdditionalPassScope> {

	private Map<String, Integer> numberLineMapping = Maps.newHashMap();
	
	public Map<String, Integer> getNumberLineMapping() {
		return numberLineMapping;
	}
	
	void addNumberLineMappingEntryIfPossible(final AstNode node) {
		Location loc = node.getLoc();
		if (Locations.isReal(loc)) {
			String apexName = node.getDefiningType().getApexName();
			int index = apexName.lastIndexOf('.'); 
			if (index == -1) {
				numberLineMapping.put(apexName, loc.line);
			} else {
				apexName = apexName.substring(index+1, apexName.length());
				numberLineMapping.put(apexName, loc.line);
			}
			
		}
	}
	
	@Override
	public boolean visit(UserClass node, AdditionalPassScope scope) {
		return true;
	}
	
	@Override
	public void visitEnd(UserClass node, AdditionalPassScope scope) {
		addNumberLineMappingEntryIfPossible(node);
	}
	
	@Override
	public boolean visit(UserInterface node, AdditionalPassScope scope) {
		return true;
	}
	
	@Override
	public void visitEnd(UserInterface node, AdditionalPassScope scope) {
		addNumberLineMappingEntryIfPossible(node);
	}
	
	@Override
	public boolean visit(UserTrigger node, AdditionalPassScope scope) {
		return true;
	}
	
	@Override
	public void visitEnd(UserTrigger node, AdditionalPassScope scope) {
		addNumberLineMappingEntryIfPossible(node);
	}
	
	@Override
    public boolean visit(final UserEnum node, AdditionalPassScope scope) {
        return true;
    }

	@Override
    public void visitEnd(final UserEnum node, AdditionalPassScope scope) {
		addNumberLineMappingEntryIfPossible(node);
    }
	
}