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
package com.salesforce.ide.apex.internal.core.builder;

import org.eclipse.core.resources.IProject;

import com.salesforce.ide.apex.internal.core.db.EclipseGraphHandleProvider;

import apex.jorje.ide.db.api.GraphDatabaseService;
import apex.jorje.ide.db.api.operation.GraphOperations;
import apex.jorje.ide.db.api.repository.TypeInfoRepository;
import apex.jorje.ide.db.impl.StandardGraphDatabaseService;
import apex.jorje.semantic.ast.compilation.UserClass;
import apex.jorje.semantic.ast.compilation.UserEnum;
import apex.jorje.semantic.ast.compilation.UserInterface;
import apex.jorje.semantic.ast.compilation.UserTrigger;
import apex.jorje.semantic.ast.visitor.AdditionalPassScope;
import apex.jorje.semantic.ast.visitor.AstVisitor;

/**
 * Builds the model for the local compilation units.
 * 
 * @author nchen
 * 
 */
public class ModelBuilder extends AstVisitor<AdditionalPassScope> {
	private final GraphOperations graph;
	private final TypeInfoRepository typeInfoRepo;

	public ModelBuilder(IProject project) {
		GraphDatabaseService service = StandardGraphDatabaseService.INSTANCE;
		graph = service.getOrCreateNoTx(new EclipseGraphHandleProvider(project));
		typeInfoRepo = new TypeInfoRepository(graph);
	}

	public void done() {
		graph.commit();
		graph.shutdown();
	}

	@Override
	public boolean visit(UserClass node, AdditionalPassScope scope) {
		return true;
	}

	@Override
	public boolean visit(UserEnum node, AdditionalPassScope scope) {
		return true;
	}

	@Override
	public boolean visit(UserInterface node, AdditionalPassScope scope) {
		return true;
	}

	@Override
	public boolean visit(UserTrigger node, AdditionalPassScope scope) {
		return true;
	}

	@Override
	public void visitEnd(UserClass node, AdditionalPassScope scope) {
		typeInfoRepo.save(node.getDefiningType());
	}

	@Override
	public void visitEnd(UserEnum node, AdditionalPassScope scope) {
		typeInfoRepo.save(node.getDefiningType());
	}

	@Override
	public void visitEnd(UserInterface node, AdditionalPassScope scope) {
		typeInfoRepo.save(node.getDefiningType());
	}

	@Override
	public void visitEnd(UserTrigger node, AdditionalPassScope scope) {
		typeInfoRepo.save(node.getDefiningType());
	}
}
