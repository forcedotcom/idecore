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
package com.salesforce.ide.apex.core.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.antlr.runtime.RecognitionException;

import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.parser.impl.ApexParserImpl;
import apex.jorje.semantic.ast.AstNodeFactory;
import apex.jorje.semantic.ast.compilation.Compilation;
import apex.jorje.semantic.compiler.Namespace;
import apex.jorje.semantic.compiler.SourceFile;
import apex.jorje.semantic.symbol.type.UserTypeInfo;

import com.salesforce.ide.apex.core.ApexParserFactory;
import com.salesforce.ide.test.common.utils.IdeTestUtil;

/**
 * Some shared utils for initializing the parser.
 * 
 * @author nchen
 * 
 */
public class ParserTestUtil {

    public static ApexParserImpl parseFromFile(String path) throws IOException, URISyntaxException {
        String string = readFromFile(path);
        ApexParserImpl parser = ApexParserFactory.create(string);
        return parser;
    }

    // We still want to support Java 6 so we cannot use the new java.nio.file.* in Java 7
    private static String readFromFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        String NL = System.getProperty("line.separator");
        Scanner scanner = new Scanner(IdeTestUtil.getFullUrlEntry(path).openStream(), "UTF-8");
        try {
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine() + NL);
            }
        } finally {
            scanner.close();
        }

        return sb.toString();
    }

    public static ApexParserImpl parseFromString(String contents) throws IOException, URISyntaxException {
        ApexParserImpl parser = ApexParserFactory.create(contents);
        return parser;
    }

    /*
     * Convenience function for getting a compilation unit directly from a file path.
     */
    public static CompilationUnit parseCompilationUnitFromFile(String path) throws IOException, URISyntaxException,
            RecognitionException {
        ApexParserImpl parser = parseFromFile(path);
        return parser.compilationUnit();
    }

    /*
     * Convenience function for getting a compilation directly from a file path
     */
    public static Compilation parseCompilationFromFile(String path) throws IOException, URISyntaxException,
            RecognitionException {
        CompilationUnit compilationUnit = parseCompilationUnitFromFile(path);
        SourceFile virtualSourceFile = SourceFile.builder().setSource("").setNamespace(Namespace.EMPTY).build();
        return AstNodeFactory.create(virtualSourceFile, (UserTypeInfo) null, compilationUnit);
    }

    /*
     * Convenience function for getting a compilation unit directly from a string
     */
    public static CompilationUnit parseCompilationUnitFromString(String contents) throws IOException,
            URISyntaxException, RecognitionException {
        ApexParserImpl parser = parseFromString(contents);
        return parser.compilationUnit();
    }

    /*
     * Convenience function for getting a compilation directly from a string
     */
    public static Compilation parseCompilationFromString(String contents) throws IOException, URISyntaxException,
            RecognitionException {
        CompilationUnit compilationUnit = parseCompilationUnitFromString(contents);
        SourceFile virtualSourceFile = SourceFile.builder().setSource("").setNamespace(Namespace.EMPTY).build();
        return AstNodeFactory.create(virtualSourceFile, (UserTypeInfo) null, compilationUnit);
    }
}
