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
package com.salesforce.ide.ui.editors.apex.assistance;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.util.ApexCodeColorProvider;
import com.salesforce.ide.ui.editors.apex.util.ApexCodeWhitespaceDetector;

/**
 * Derives from org.eclipse.jdt.internal.ui.text.javadoc.JavaDocScanner and is governed by the terms of the original
 * copyright on that file.
 */
public class ApexDocScanner extends RuleBasedScanner {

    private List<String> docKeywords = null;

    /**
     * Create a new javadoc scanner for the given color provider.
     * 
     * @param provider
     *            the color provider
     */
    public ApexDocScanner() {
        super();
    }

    //   M E T H O D S
    public List<String> getDocKeywords() {
        return docKeywords;
    }

    public void setDocKeywords(List<String> docKeywords) {
        this.docKeywords = docKeywords;
    }

    public void init() {
        ApexCodeColorProvider apexCodeColorProvider = ForceIdeEditorsPlugin.getApexCodeColorProvider();

        IToken keyword = new Token(new TextAttribute(apexCodeColorProvider.getColor(ApexCodeColorProvider.JAVADOC_KEYWORD)));
        IToken tag = new Token(new TextAttribute(apexCodeColorProvider.getColor(ApexCodeColorProvider.JAVADOC_TAG)));
        IToken link = new Token(new TextAttribute(apexCodeColorProvider.getColor(ApexCodeColorProvider.JAVADOC_LINK)));
        IToken doc = new Token(new TextAttribute(apexCodeColorProvider.getColor(ApexCodeColorProvider.JAVADOC_DEFAULT)));

        setDefaultReturnToken(new Token(doc));

        List<IRule> list = new ArrayList<>();

        // Add rule for tags.
        list.add(new SingleLineRule("<", ">", tag));

        // Add rule for links.
        list.add(new SingleLineRule("{", "}", link));

        // multi-line
        list.add(new MultiLineRule("/**", "*/", doc, (char) 0, true));

        // Add generic whitespace rule.
        list.add(new WhitespaceRule(new ApexCodeWhitespaceDetector()));

        if (Utils.isNotEmpty(docKeywords)) {
            // Add word rule for keywords.
            WordRule wordRule = new WordRule(new IWordDetector() {
                @Override
                public boolean isWordStart(char c) {
                    return (c == '@');
                }

                @Override
                public boolean isWordPart(char c) {
                    return Character.isLetter(c);
                }
            });
            for (String docKeyword : docKeywords) {
                wordRule.addWord(docKeyword, keyword);
            }
            list.add(wordRule);
        }
        IRule[] result = new IRule[list.size()];
        list.toArray(result);
        setRules(result);
    }
}
