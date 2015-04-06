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
package com.salesforce.ide.ui.editors.apex;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * This scanner recognizes the Apex-style comments and multi line comments. These are identical to the ones in Java.
 */
public class ApexPartitionScanner extends RuleBasedPartitionScanner {

    public final static String APEX_MULTILINE_COMMENT = "__apex_multiline_comment"; //$NON-NLS-1$

    public final static String APEX_DOC = "__apex_javadoc"; //$NON-NLS-1$

    public final static String[] APEX_PARTITION_TYPES = new String[] { APEX_MULTILINE_COMMENT, APEX_DOC };

    /**
     *
     */
    static class WordPredicateRule extends WordRule implements IPredicateRule {

        private final IToken fSuccessToken;

        public WordPredicateRule(IToken successToken) {
            super(new IWordDetector() {
                @Override
                public boolean isWordStart(char c) {
                    return (c == '/');
                }

                @Override
                public boolean isWordPart(char c) {
                    return ((c == '*') || (c == '/'));
                }
            });

            fSuccessToken = successToken;
            addWord("/**/", fSuccessToken); //$NON-NLS-1$
        }

        /*
         * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(ICharacterScanner, boolean)
         */
        @Override
        public IToken evaluate(ICharacterScanner scanner, boolean resume) {
            return super.evaluate(scanner);
        }

        /*
         * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
         */
        @Override
        public IToken getSuccessToken() {
            return fSuccessToken;
        }
    }

    /**
     * Creates the partitioner and sets up the appropriate rules.
     */
    public ApexPartitionScanner() {
        super();

        IToken comment = new Token(APEX_MULTILINE_COMMENT);

        List<IPredicateRule> rules = new ArrayList<>();

        // Add rule for single line comments.
        rules.add(new EndOfLineRule("//", Token.UNDEFINED));

        // Add rule for strings and character constants.
        rules.add(new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\'));
        rules.add(new SingleLineRule("'", "'", Token.UNDEFINED, '\\'));

        // Add special case word rule.
        rules.add(new WordPredicateRule(comment));

        // Add rules for multi-line comments.
        rules.add(new MultiLineRule("/*", "*/", comment, (char) 0, true));

        IPredicateRule[] result = new IPredicateRule[rules.size()];
        rules.toArray(result);
        setPredicateRules(result);
    }
}
