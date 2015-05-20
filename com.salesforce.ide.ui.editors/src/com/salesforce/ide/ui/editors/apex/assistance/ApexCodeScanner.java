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
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.remote.registries.DescribeObjectRegistry;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.util.ApexCodeColorProvider;
import com.salesforce.ide.ui.editors.apex.util.ApexCodeWhitespaceDetector;
import com.salesforce.ide.ui.editors.apex.util.ApexCodeWordDetector;
import com.sforce.soap.partner.wsc.DescribeSObjectResult;

public class ApexCodeScanner extends RuleBasedScanner {

    private static Logger logger = Logger.getLogger(ApexCodeScanner.class);

    private List<String> keywords = null;
    private List<String> triggerOperations = null;
    private ApexCodeColorProvider apexCodeColorProvider = null;

    //   C O N S T R U C T O R S
    /**
     * Creates a Java code scanner with the given color provider.
     * 
     * @param provider
     *            the color provider
     */
    public ApexCodeScanner() {
        super();
    }

    //   M E T H O D S
    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getTriggerOperations() {
        return triggerOperations;
    }

    public void setTriggerOperations(List<String> triggerOperations) {
        this.triggerOperations = triggerOperations;
    }

    public void init(IProject project) {
        apexCodeColorProvider = ForceIdeEditorsPlugin.getApexCodeColorProvider();
        if (apexCodeColorProvider == null) {
            logger.warn("Unable to init Apex code scanner - color provider is null");
            return;
        }

        IToken string = new Token(new TextAttribute(apexCodeColorProvider.getColor(ApexCodeColorProvider.STRING)));
        IToken comment =
                new Token(new TextAttribute(apexCodeColorProvider.getColor(ApexCodeColorProvider.SINGLE_LINE_COMMENT)));
        IToken annotations =
                new Token(new TextAttribute(apexCodeColorProvider.getColor(ApexCodeColorProvider.ANNOTATION)));

        List<IRule> rules = new ArrayList<>();

        // end of line rule
        rules.add(new EndOfLineRule("//", comment));

        // single and double quote strings
        rules.add(new SingleLineRule("'", "'", string, '\\'));
        rules.add(new SingleLineRule("\"", "\"", string, '\\'));

        // annotations
        rules.add(new SingleLineRule("@", " ", annotations, '\\'));

        // add generic whitespace rule
        rules.add(new WhitespaceRule(new ApexCodeWhitespaceDetector()));

        WordRule wordRule = generateKeywordRule(project);
        rules.add(wordRule);

        setRules(rules.toArray(new IRule[rules.size()]));
    }

    public WordRule generateKeywordRule(IProject project) {
        IToken keyword = new Token(getBoldTextAttribute(apexCodeColorProvider.getColor(ApexCodeColorProvider.KEYWORD)));
        IToken other = new Token(new TextAttribute(apexCodeColorProvider.getColor(ApexCodeColorProvider.DEFAULT)));

        // Add word rule for keywords, types, and constants.
        WordRule wordRule = new WordRule(new ApexCodeWordDetector(), other);

        if (Utils.isNotEmpty(triggerOperations)) {
            for (String triggerOperation : triggerOperations) {
                wordRule.addWord(triggerOperation, keyword);
            }
        }

        if (Utils.isNotEmpty(keywords)) {
            for (String element : keywords) {
                wordRule.addWord(element, keyword);
            }
        }

        if (project != null) {
            try {
                Collection<DescribeSObjectResult> describeSObjectResults =
                        getDescribeObjectRegistry().getCachedDescribeSObjectResultsIfAny(project);
                if (Utils.isNotEmpty(describeSObjectResults)) {
                    IToken sobjects =
                            new Token(new TextAttribute(
                                    apexCodeColorProvider.getColor(ApexCodeColorProvider.SOBJECTS_SPECIFIC)));

                    // Add word rule for keywords, types, and constants.
                    for (DescribeSObjectResult describeSObjectResult : describeSObjectResults) {
                        wordRule.addWord(describeSObjectResult.getName(), sobjects);
                    }
                }
            } catch (Exception e) {
                logger.warn("Unable to get describe object for type project '" + project.getName() + "'",
                    ForceExceptionUtils.getRootCause(e));
            }
        }

        return wordRule;
    }

    public DescribeObjectRegistry getDescribeObjectRegistry() {
        return (DescribeObjectRegistry) ContainerDelegate.getInstance().getBean(DescribeObjectRegistry.class);
    }

    @Override
    public IToken nextToken() {
        return super.nextToken();
    }

    @Override
    public int read() {
        return super.read();
    }

    private static TextAttribute getBoldTextAttribute(Color color) {
        TextAttribute textAttribute = new TextAttribute(color, null, SWT.BOLD);
        return textAttribute;
    }
}
