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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

import com.google.common.collect.Lists;
import com.salesforce.ide.apex.internal.core.tooling.systemcompletions.model.AbstractCompletionProposalDisplayable;

/**
 * Class for common utilities for code completions.
 * 
 * @author nchen
 * 
 */
public class ApexCompletionUtils {
    public static final char FULLY_QUALIFIED_NAME_SEPARATOR_CHAR = '.';
    private static final String FULLY_QUALIFIED_NAME_REGEX = "\\.";
    public static final ApexCompletionUtils INSTANCE = new ApexCompletionUtils();

    /**
     * When we are auto-completing, we might have a partial string that doesn't fully compile yet. TODO: This is NOT
     * complete - it only handles system completions now. Need to augment to handle arbitrary types including
     * user-defined types (including inner classes). More importantly, we need to be able to support chaining of
     * type.method().method()...
     * 
     * 
     * @author nchen
     * 
     */
    public static class CompletionPrefix {
        public List<String> segments;

        public CompletionPrefix(String prefix) {
            prefix = prefix.toLowerCase();
            segments = Lists.newArrayList(prefix.split(FULLY_QUALIFIED_NAME_REGEX));

            // This happens if the user is actually interested in completing the next segment
            // e.g., someValue.<cursor here> so we should try to complete the next segment
            // So we add a placeholder so that we know that we are trying to complete the next segment.
            // Think of this as the empty string. The empty string is a prefix for all completions.
            if (prefix.endsWith(Character.toString(FULLY_QUALIFIED_NAME_SEPARATOR_CHAR))) {
                segments.add("");
            }
        }

        // <cursor>
        // SomeNamespac<cursor>
        public boolean shouldSuggestNamespace() {
            return segments.size() <= 1;
        }

        // Top-level types don't need namespace prefix
        // <cursor>
        // SomeTyp<cursor>
        public boolean shouldSuggestTopLevelType() {
            return segments.size() <= 1;
        }

        // SomeNamespace.SomeTyp<cursor>
        public boolean shouldSuggestNamespacedType() {
            return segments.size() == 2;
        }

        // SomeType.someMembe<cursor>
        public boolean shouldSuggestTopLevelMember() {
            return segments.size() == 2;
        }

        // SomeNamespace.SomeType.someMembe<cursor>
        public boolean shouldSuggestNamespacedMember() {
            return segments.size() == 3;
        }

        // new SomeTyp<cursor>
        public boolean shouldSuggestTopLevelConstructor() {
            return segments.size() <= 1;
        }

        // new SomeNamespace.SomeTyp<cursor>
        public boolean shouldSuggestNamespacedConstructor() {
            return segments.size() == 2;
        }

        // someVariabl<cursor>
        public boolean shouldSuggestVariableName() {
            return segments.size() == 1;
        }

        // someVariable.someMembe<cursor>
        public boolean shouldSuggestVariableMember() {
            return segments.size() == 2;
        }
    }

    private ApexCompletionUtils() {};

    public String getPrefix(ITextViewer viewer, int offset) throws BadLocationException {
        IDocument doc = viewer.getDocument();
        if (doc == null || offset > doc.getLength())
            return null;

        int length = 0;
        while (--offset >= 0) {
            char currentChar = doc.getChar(offset);
            if (Character.isJavaIdentifierPart(currentChar) || currentChar == FULLY_QUALIFIED_NAME_SEPARATOR_CHAR) {
                length++;
            } else {
                break;
            }
        }

        return doc.get(offset + 1, length);
    }

    // Can we do this more accurately to determine when the user has entered a "new" expression even on a prior line?
    // Seems like the parser should be able to handle it though it might signal a parse exception since it is expecting a complete new expression.
    public boolean hasInvokedNewOnSameLine(ITextViewer viewer, int offset) throws BadLocationException {
        IDocument doc = viewer.getDocument();
        if (doc == null || offset > doc.getLength())
            return false;

        IRegion lineInfo = doc.getLineInformationOfOffset(offset);
        String currentLine = doc.get(lineInfo.getOffset(), lineInfo.getLength());
        return currentLine.matches(".*(\\bnew\\b).*");
    }

    public ICompletionProposal[] createProposal(Iterable<AbstractCompletionProposalDisplayable> suggestions,
            String prefix, int offset, Image image) {
        return createProposal(Lists.newArrayList(suggestions), prefix, offset, image);
    }

    public ICompletionProposal[] createProposal(Collection<AbstractCompletionProposalDisplayable> suggestions,
            String prefix, int offset, Image image) {
        List<ICompletionProposal> proposals = Lists.newArrayList();

        for (AbstractCompletionProposalDisplayable suggestion : suggestions) {
            int prefixLength = prefix != null ? prefix.length() : 0;
            String replacement = suggestion.getReplacementString();
            String displayString = suggestion.getDisplayString();
            if (!StringUtils.isEmpty(replacement))
                proposals.add(new CompletionProposal(replacement, offset - prefixLength, prefix.length(), suggestion
                        .cursorPosition(), image, displayString, null, null));
        }
        return proposals.toArray(new ICompletionProposal[0]);
    }

    public ApexCompletionUtils.CompletionPrefix determineFullyQualifiedNameFromPrefix(String prefix) {
        return new CompletionPrefix(prefix);
    }

}
