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
package com.salesforce.ide.apex.internal.core;

import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.parser.impl.ApexParserImpl;
import apex.jorje.semantic.ast.AstNodeFactory;
import apex.jorje.semantic.ast.compilation.Compilation;
import apex.jorje.semantic.compiler.Namespace;
import apex.jorje.semantic.compiler.SourceFile;
import apex.jorje.semantic.symbol.type.UserTypeInfo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.salesforce.ide.apex.core.ApexParserFactory;
import com.salesforce.ide.apex.core.exceptions.InvalidCompilationASTException;
import com.salesforce.ide.apex.core.exceptions.InvalidCompilationUnitException;

/**
 * Central point for getting any form of model information about the current project. Right now we have a jadt and
 * compilation unit cache.
 * 
 * @author nchen
 * 
 */
public class ApexModelManager {
    private static final Logger logger = Logger.getLogger(ApexModelManager.class);

    public static final ApexModelManager INSTANCE = new ApexModelManager();

    // The cache should only store the last "good" version of the parse tree and AST
    // If there is a parse error, you should not update the cache but leave the last version in there.

    // This is the parse tree - a lightweight model that should be good to store around
    private final LoadingCache<IFile, CompilationUnit> jadtCache = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<IFile, CompilationUnit>() {

                @Override
                public CompilationUnit load(IFile file) throws InvalidCompilationUnitException {
                    try {
                        ApexParserImpl parser = ApexParserFactory.create(new InputStreamReader(file.getContents()));
                        return parser.compilationUnit();
                    } catch (CoreException ce) {
                        throw new InvalidCompilationUnitException(ce);
                    } catch (RecognitionException re) {
                        throw new InvalidCompilationUnitException(re);
                    }
                }
            });

    // This is the AST - it allows you to visit the relevant nodes using the more familiar visitor pattern 
    // It can be more heavyweight since it contains symboltables.
    private final LoadingCache<IFile, Compilation> astCache = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<IFile, Compilation>() {

                @Override
                public Compilation load(IFile file) throws InvalidCompilationASTException {
                    try {
                        CompilationUnit compilationUnit = jadtCache.get(file);

                        // TODO: Maybe fill in the actual values since we can get them to make this a real source file
                        SourceFile virtualSourceFile =
                                SourceFile.builder().setSource("").setNamespace(Namespace.EMPTY).build();

                        return AstNodeFactory.create(virtualSourceFile, (UserTypeInfo) null, compilationUnit);
                    } catch (ExecutionException e) {
                        throw new InvalidCompilationASTException(e);
                    }
                }
            });

    private ApexModelManager() {}

    public CompilationUnit getCompilationUnit(IFile file) {
        return getCompilationUnit(file, false);
    }

    public CompilationUnit getCompilationUnit(IFile file, boolean forceRefresh) {
        try {
            if (forceRefresh)
                invalidateCaches(file);

            return jadtCache.get(file);
        } catch (ExecutionException e) {
            logger.error("Unable to get compilation unit for file " + file, e);
            return new CompilationUnit.InvalidDeclUnit();
        }
    }

    public Compilation getCompilation(IFile file) {
        return getCompilation(file, false);
    }

    public Compilation getCompilation(IFile file, boolean forceRefresh) {
        try {
            if (forceRefresh)
                invalidateCaches(file);

            return astCache.get(file);
        } catch (ExecutionException e) {
            logger.error("Unable to get compilation AST node for file " + file, e);
            return Compilation.INVALID;
        }
    }

    public void cacheCompilationUnit(IFile file, CompilationUnit unit) {
        jadtCache.put(file, unit);
    }

    public void cacheCompilation(IFile file, Compilation compilation) {
        astCache.put(file, compilation);
    }

    public void evictCompilationUnit(IFile file) {
        jadtCache.invalidate(file);
    }

    public void evictCompilation(IFile file) {
        astCache.invalidate(file);
    }

    public void invalidateCaches(IFile file) {
        jadtCache.invalidate(file);
        astCache.invalidate(file);
    }

    public void invalidAll() {
        jadtCache.invalidateAll();
    }
}
