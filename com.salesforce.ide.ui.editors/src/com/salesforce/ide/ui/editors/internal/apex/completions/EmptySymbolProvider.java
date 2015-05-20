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
package com.salesforce.ide.ui.editors.internal.apex.completions;

import apex.jorje.semantic.compiler.CodeUnit;
import apex.jorje.semantic.compiler.SymbolProvider;
import apex.jorje.semantic.symbol.resolver.SymbolResolver;
import apex.jorje.semantic.symbol.type.TypeInfo;

/**
 * Taken from apex-jorje as a way to not do any type resolution.
 * 
 * @author nchen
 * 
 */
class EmptySymbolProvider implements SymbolProvider {

    private static final EmptySymbolProvider INSTANCE = new EmptySymbolProvider();

    private EmptySymbolProvider() {}

    public static EmptySymbolProvider get() {
        return INSTANCE;
    }

    @Override
    public void reportParsed(CodeUnit codeUnit) {

    }

    @Override
    public TypeInfo find(final SymbolResolver resolver, final String lowerCaseFullName) {
        return null;
    }

    @Override
    public TypeInfo fetch(SymbolResolver symbols, String fullName) {
        return null;
    }

    @Override
    public TypeInfo getVfComponentType(SymbolResolver symbols, apex.jorje.semantic.compiler.Namespace namespace,
            String name) {
        return null;
    }

    @Override
    public TypeInfo getAuraComponentType(SymbolResolver symbols, apex.jorje.semantic.compiler.Namespace namespace,
            String name) {
        return null;
    }

    @Override
    public TypeInfo getFlowInterviewType(SymbolResolver symbols, apex.jorje.semantic.compiler.Namespace namespace,
            String name) {
        return null;
    }

}