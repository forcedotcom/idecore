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

import apex.jorje.semantic.compiler.CodeUnit;
import apex.jorje.semantic.compiler.Namespace;
import apex.jorje.semantic.compiler.sfdc.SymbolProvider;
import apex.jorje.semantic.symbol.resolver.SymbolResolver;
import apex.jorje.semantic.symbol.type.TypeInfo;


/**
 * Taken from apex-jorje as a way to not do any type resolution.
 * 
 * @author jspagnola
 * 
 */
public class EmptySymbolProvider implements SymbolProvider {

    private static final EmptySymbolProvider INSTANCE = new EmptySymbolProvider();

    private EmptySymbolProvider() {}

    public static EmptySymbolProvider get() {
        return INSTANCE;
    }

    @Override
    public void reportParsed(final CodeUnit codeUnit) {}

    @Override
    public TypeInfo find(final SymbolResolver symbols, final TypeInfo referencingType, final String lowerCaseFullName) {
        return null;
    }

    @Override
    public TypeInfo fetch(final SymbolResolver symbols, final TypeInfo referencingType, final String fullName) {
        return null;
    }

    @Override
    public TypeInfo getVfComponentType(final SymbolResolver symbols, final TypeInfo referencingType,
            final Namespace namespace, final String name) {
        return null;
    }

    @Override
    public TypeInfo getFlowInterviewType(final SymbolResolver symbols, final Namespace namespace, final String name) {
        return null;
    }

    @Override
    public TypeInfo getSObjectType(final TypeInfo referencingType, final String name) {
        return null;
    }

    @Override
    public String getPageReference(final TypeInfo referencingType, final String name) {
        return null;
    }

    @Override
    public boolean hasLabelField(final Namespace namespace, final String name) {
        return false;
    }
}