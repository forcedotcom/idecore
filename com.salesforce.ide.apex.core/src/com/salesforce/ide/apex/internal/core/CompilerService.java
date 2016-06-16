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
package com.salesforce.ide.apex.internal.core;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.salesforce.ide.core.internal.utils.QualifiedNames;

import apex.jorje.semantic.ast.visitor.AdditionalPassScope;
import apex.jorje.semantic.ast.visitor.AstVisitor;
import apex.jorje.semantic.common.TestAccessEvaluator;
import apex.jorje.semantic.common.TestQueryValidators;
import apex.jorje.semantic.compiler.ApexCompiler;
import apex.jorje.semantic.compiler.CodeUnit;
import apex.jorje.semantic.compiler.CompilationInput;
import apex.jorje.semantic.compiler.CompilerContext;
import apex.jorje.semantic.compiler.CompilerOperation;
import apex.jorje.semantic.compiler.CompilerStage;
import apex.jorje.semantic.compiler.Namespace;
import apex.jorje.semantic.compiler.Namespaces;
import apex.jorje.semantic.compiler.SourceFile;
import apex.jorje.semantic.compiler.sfdc.AccessEvaluator;
import apex.jorje.semantic.compiler.sfdc.QueryValidator;
import apex.jorje.semantic.compiler.sfdc.SymbolProvider;

/**
 * Central point for interfacing with the compiler.
 * 
 * @author nchen
 *         
 */
public class CompilerService {
    Function<? super IFile, ? extends SourceFile> RESOURCE_TO_SOURCE = new Function<IFile, SourceFile>() {

        @Override
        public SourceFile apply(IFile resource) {
            String body = "";
            try {
                body = canonicalizeString(CharStreams.toString(new InputStreamReader(resource.getContents())));
            } catch (CoreException | IOException e) {}
            return SourceFile.builder()
                .setBody(body)
                .setKnownName(resource.getFullPath().toOSString())
                .setNamespace(RESOURCE_NAMEPSACE.apply(resource))
                .build();
        }};
   
    Function<? super IFile, ? extends Namespace> RESOURCE_NAMEPSACE = new Function<IFile, Namespace>() {

        @Override
        public Namespace apply(IFile file) {
            try {
                String namespace = file.getPersistentProperty(QualifiedNames.QN_NAMESPACE_PREFIX);
                return isNullOrEmpty(namespace) ? Namespaces.EMPTY : Namespaces.create(namespace);
            } catch (CoreException e) { }
            return Namespaces.EMPTY;
        }
    };

    private static final Logger logger = Logger.getLogger(CompilerService.class);
    
    public static final CompilerService INSTANCE = new CompilerService();
    private final SymbolProvider symbolProvider;
    private final AccessEvaluator accessEvaluator;
    private QueryValidator queryValidator;
    
    /**
     * Configure a compiler with the default configurations:
     * @param symbolProvider - EmptySymbolProvider, doesn't provide any symbols that are not part of source.
     * @param accessEvaluator - TestAccessEvaluator, doesn't provide any validation.
     * @param queryValidator - TestQueryValidators.Noop, no validation of queries.
     */
    CompilerService() {
        this(new EmptySymbolProvider(), new TestAccessEvaluator(), new TestQueryValidators.Noop());
    }
    
    /**
     * Configure a compiler with the following configurations:
     * @param symbolProvider - a way to retrieve symbols, where symbols are names of types.
     * @param accessEvaluator - a way to check for accesses to certain fields in types.
     * @param queryValidator - a way to validate your queries.
     */
    public CompilerService(SymbolProvider symbolProvider, AccessEvaluator accessEvaluator, QueryValidator queryValidator) {
        this.symbolProvider = symbolProvider;
        this.accessEvaluator = accessEvaluator;
        this.queryValidator = queryValidator;
    }
    
    // These method names are verbose because they have similar parameter types, i.e., List<X> files, Lists<X> resources.
    // Type erasure in Java will collapse them to the same thing (List) and won't compile if the methods are overloaded.
    
    public ApexCompiler visitAstFromString(String source, AstVisitor<AdditionalPassScope> visitor) {
        return visitAstsFromStrings(ImmutableList.of(source), visitor, CompilerStage.POST_TYPE_RESOLVE);
    }

    public ApexCompiler visitAstsFromStrings(List<String> sources, AstVisitor<AdditionalPassScope> visitor) {
        return visitAstsFromStrings(sources, visitor, CompilerStage.POST_TYPE_RESOLVE);
    }

    public ApexCompiler visitAstFromFile(IFile resource, AstVisitor<AdditionalPassScope> visitor) {
        return visitAstsFromFiles(ImmutableList.of(resource), visitor, CompilerStage.POST_TYPE_RESOLVE);
    }

    public ApexCompiler visitAstsFromFiles(List<IFile> resources, AstVisitor<AdditionalPassScope> visitor) {
        return visitAstsFromFiles(resources, visitor, CompilerStage.POST_TYPE_RESOLVE);
    }

    public ApexCompiler visitAstFromString(String source, AstVisitor<AdditionalPassScope> visitor, CompilerStage compilerStage) {
        return visitAstsFromStrings(ImmutableList.of(source), visitor, compilerStage);
    }

    public ApexCompiler visitAstFromFile(IFile resource, AstVisitor<AdditionalPassScope> visitor, CompilerStage compilerStage) {
        return visitAstsFromFiles(ImmutableList.of(resource), visitor, compilerStage);
    }

    public ApexCompiler visitAstsFromStrings(List<String> sources, AstVisitor<AdditionalPassScope> visitor, CompilerStage compilerStage) {
        List<SourceFile> sourceFiles =
            sources.stream().map(s -> SourceFile.builder().setBody(canonicalizeString(s)).build()).collect(Collectors.toList());
        CompilationInput compilationUnit = createCompilationInput(sourceFiles, visitor);
        return compile(compilationUnit, visitor, compilerStage);
    }

    public ApexCompiler visitAstsFromFiles(List<IFile> resources, AstVisitor<AdditionalPassScope> visitor, CompilerStage compilerStage) {
        List<SourceFile> sourceFiles = resources.stream().map(RESOURCE_TO_SOURCE).collect(Collectors.toList());
        CompilationInput compilationUnit = createCompilationInput(sourceFiles, visitor);
        return compile(compilationUnit, visitor, compilerStage);
    }

    private ApexCompiler compile(
        CompilationInput compilationInput,
        AstVisitor<AdditionalPassScope> visitor,
        CompilerStage compilerStage
    ) {
        ApexCompiler compiler = ApexCompiler.builder().setInput(compilationInput).build();
        compiler.compile(compilerStage);
        callAdditionalPassVisitor(compiler);
        return compiler;
    }
    
    private CompilationInput createCompilationInput(
        List<SourceFile> sourceFiles,
        AstVisitor<AdditionalPassScope> visitor
    ) {
        return new CompilationInput(sourceFiles, symbolProvider, accessEvaluator, queryValidator, visitor);
    }
    
    /**
     * This is temporary workaround to bypass the validation stage of the compiler while *still* doing the
     * additional_validate stage. We are bypassing the validation stage because it does a deep validation that we don't
     * have all the parts for yet in the offline compiler. Rather than stop all work on that, we bypass it so that we
     * can still do useful things like find all your types, find all your methods, etc.
     * 
     */
    @SuppressWarnings("unchecked")
    private void callAdditionalPassVisitor(ApexCompiler compiler) {
        try {
            List<CodeUnit> allUnits = (List<CodeUnit>) FieldUtils.readDeclaredField(compiler, "allUnits", true);
            CompilerContext compilerContext =
                (CompilerContext) FieldUtils.readDeclaredField(compiler, "compilerContext", true);
                
            for (CodeUnit unit : allUnits) {
                Method getOperation =
                    CompilerStage.ADDITIONAL_VALIDATE.getDeclaringClass().getDeclaredMethod("getOperation");
                getOperation.setAccessible(true);
                CompilerOperation operation =
                        (CompilerOperation) getOperation.invoke(CompilerStage.ADDITIONAL_VALIDATE);
                operation.invoke(compilerContext, unit);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Failed to inokve additional validator", e);
        }
    }
    
    public static String canonicalizeString(String inputString) {
        String text = inputString.replaceAll("(\\r\\n|\\r)", Matcher.quoteReplacement("\n")); 
        return text;
    }
}