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
package com.salesforce.ide.ui.editors.apex.parser;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;

import apex.jorje.parser.impl.ApexLexerImpl;
import apex.jorje.parser.impl.ApexParserImpl;
import apex.jorje.parser.impl.CaseInsensitiveReaderStream;

/**
 * Convenience class to interface with our parser
 * 
 * @author nchen
 *
 */
public class IdeApexParser {

    public static ApexParserImpl initializeParser(String inputString) {
        CharStream stream = CaseInsensitiveReaderStream.create(inputString);
        ApexLexerImpl lexer = new ApexLexerImpl(stream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        return new ApexParserImpl(tokenStream);
    }
}
