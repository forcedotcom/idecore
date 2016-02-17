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
package com.salesforce.ide.apex.core.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.google.common.io.CharStreams;
import com.salesforce.ide.apex.internal.core.ApexModelManager;
import com.salesforce.ide.apex.internal.core.EmptySymbolProvider;
import apex.jorje.semantic.ast.compilation.Compilation;
import apex.jorje.semantic.ast.visitor.AstVisitor;
import apex.jorje.semantic.ast.visitor.SymbolScope;
import apex.jorje.semantic.compiler.ApexCompiler;
import apex.jorje.semantic.compiler.CompilationInput;
import apex.jorje.semantic.compiler.Namespaces;
import apex.jorje.semantic.compiler.SourceFile;
import apex.jorje.semantic.exception.Errors;
import apex.jorje.semantic.symbol.resolver.StandardSymbolResolver;
/**
 * 
 * @author nchen
 *
 */
public class ApexVisitorUtil {

	public static ApexVisitorUtil INSTANCE = new ApexVisitorUtil();

	protected ApexVisitorUtil() {}

	public void traverse(AstVisitor<SymbolScope> visitor, IResource resource) {
		SymbolScope scope = buildSymbolScope(resource);
		Compilation compilation = getCompilation((IFile) resource);
		compilation.traverse(visitor, scope);
	}

	public Compilation getCompilation(IFile file) {
		return ApexModelManager.INSTANCE.getCompilation(file);
	}

	public SymbolScope buildSymbolScope(IResource resource) {
		IFile file = (IFile) resource; 
		String body = "";

		try {
			body = CharStreams.toString(new InputStreamReader(file.getContents()));
		} catch (CoreException | IOException e) {}

		SourceFile virtualSourceFile = SourceFile.builder().setBody(body).setNamespace(Namespaces.EMPTY).build();
		ApexCompiler compiler = ApexCompiler.builder().setInput(new CompilationInput(Collections.singleton(virtualSourceFile), 
				EmptySymbolProvider.get(), null, null, null)).build();
		SymbolScope scope = new SymbolScope(new StandardSymbolResolver(compiler), new Errors());
		return scope;
	}
}
