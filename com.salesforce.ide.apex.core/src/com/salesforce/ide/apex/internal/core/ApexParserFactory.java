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

import java.util.regex.Matcher;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.apache.log4j.Logger;

import apex.jorje.parser.impl.ApexLexer;
import apex.jorje.parser.impl.ApexParser;
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

    public static ApexParser create(String inputString) {
        try {
            String canonicalizedString = canonicalizeString(inputString);
            CharStream stream = CaseInsensitiveReaderStream.create(canonicalizedString);
            ApexLexer lexer = new ApexLexer(stream);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            return new ApexParser(tokenStream);
        } catch (UnhandledException ue) {
            logger.error(ue);
        }
        return null;
    }
    
    /**
     * Canonicalizes \r\n and \r into \n.
     * 
     * @param inputString
     * @return
     */
    public static String canonicalizeString(String inputString) {
        String text = inputString.replaceAll("(\\r\\n|\\r)", Matcher.quoteReplacement("\n")); 
        return text;
    }
}