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
package com.salesforce.ide.apex.internal.core.builder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import apex.jorje.semantic.compiler.ApexCompiler;
import apex.jorje.semantic.compiler.CodeUnit;
import apex.jorje.semantic.compiler.CompilationInput;
import apex.jorje.semantic.compiler.CompilerContext;
import apex.jorje.semantic.compiler.CompilerOperation;
import apex.jorje.semantic.compiler.CompilerStage;
import apex.jorje.semantic.tester.TestAccessEvaluator;
import apex.jorje.semantic.tester.TestQueryValidators;
import apex.jorje.semantic.tester.TestSymbolProvider;

/**
 * Builder that uses the Jorje compiler to calculate metadata about your source code.
 * 
 * @author nchen
 */
public class JorjeBuilder extends IncrementalProjectBuilder {
    private static final Logger logger = Logger.getLogger(JorjeBuilder.class);
    
    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
        switch (kind) {
            case FULL_BUILD:
                fullBuild(getProject());
                break;
            case INCREMENTAL_BUILD:
                partialBuild(getProject());
                break;
            default:
                
        }
        
        return null;
    }
    
    private void fullBuild(IProject project) throws CoreException {
        ApexResourcesVisitor visitor = new ApexResourcesVisitor();
        project.accept(visitor, IResource.NONE);
        
        ModelBuilder modelBuilder = new ModelBuilder(project);
        try {
            CompilationInput input =
                    new CompilationInput(
                            visitor.getSources(),
                            new TestSymbolProvider(),
                            new TestAccessEvaluator(),
                            new TestQueryValidators.Noop(),
                            modelBuilder);
            ApexCompiler compiler = ApexCompiler.builder().setInput(input).build();
            compiler.compile(CompilerStage.POST_TYPE_RESOLVE);
			callAdditionalPassVisitor(compiler);
		} finally {
			modelBuilder.done();
		}
	}

    /**
     * This is temporary workaround to bypass the validation stage of the compiler while *still* doing the additional_validate stage.
     * We are bypassing the validation stage because it does a deep validation that we don't have all the parts for yet in 
     * the offline compiler. Rather than stop all work on that, we bypass it so that we can still do useful things like
     * find all your types, find all your methods, etc.
     * 
     */
	@SuppressWarnings("unchecked")
	private void callAdditionalPassVisitor(ApexCompiler compiler) {
		try {
			List<CodeUnit> allUnits = (List<CodeUnit>) FieldUtils.readDeclaredField(compiler, "allUnits", true);
			CompilerContext compilerContext = (CompilerContext) FieldUtils.readDeclaredField( compiler, "compilerContext", true);

			for (CodeUnit unit : allUnits) {
				Method getOperation = CompilerStage.ADDITIONAL_VALIDATE.getDeclaringClass().getDeclaredMethod("getOperation");
				getOperation.setAccessible(true);
				CompilerOperation operation = (CompilerOperation) getOperation.invoke(CompilerStage.ADDITIONAL_VALIDATE);
				operation.invoke(compilerContext, unit);
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			logger.error("Failed to inokve additional validator", e);
		}
	}
    
    private void partialBuild(IProject project) throws CoreException {
    }
}
