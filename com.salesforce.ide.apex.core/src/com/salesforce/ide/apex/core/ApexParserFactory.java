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
package com.salesforce.ide.apex.core;

import java.io.Reader;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.apache.log4j.Logger;

import apex.jorje.parser.impl.ApexLexerImpl;
import apex.jorje.parser.impl.ApexParserImpl;
import apex.jorje.parser.impl.CaseInsensitiveReaderStream;
import apex.jorje.services.exception.UnhandledException;

/**
 * Convenience class to interface with our parser.
 * 
 * @author nchen
 * 
 */
public class ApexParserFactory {
    private static final Logger logger = Logger.getLogger(ApexParserFactory.class);

    public static ApexParserImpl create(Reader reader) {
        try {
            CharStream stream = CaseInsensitiveReaderStream.create(reader);
            ApexLexerImpl lexer = new ApexLexerImpl(stream);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            return new ApexParserImpl(tokenStream);
        } catch (UnhandledException ue) {
            logger.error(ue);
        }
        return null;
    }

    public static ApexParserImpl create(String inputString) {
        try {
            CharStream stream = CaseInsensitiveReaderStream.create(inputString);
            ApexLexerImpl lexer = new ApexLexerImpl(stream);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            return new ApexParserImpl(tokenStream);
        } catch (UnhandledException ue) {
            logger.error(ue);
        }
        return null;
    }
}