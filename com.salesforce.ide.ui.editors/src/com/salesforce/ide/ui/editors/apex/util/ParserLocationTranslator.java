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
package com.salesforce.ide.ui.editors.apex.util;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import antlr.Token;
import apex.jorje.data.Location;

/**
 * Utilities for translating between the locations that the parser uses (based on Antlr's (@see {@link Token}) and
 * IDocument (@see {@link IDocument} locations that Eclipse JFace uses.
 * 
 * <ul>
 * <li>Documents are line-based (0...n) and column-based (0...n)</li>
 * <li>Loc are line-based (1..n) and column-based (0...n)</li>
 * </ul>
 * 
 * @author nchen
 * 
 */
public class ParserLocationTranslator {

    /**
     * Simple value-object to store what to highlight.
     * 
     * @author nchen
     * 
     */
    public static class HighlightRange {
        public final int startOffset;
        public final int length;

        public HighlightRange(int startOffset, int length) {
            this.startOffset = startOffset;
            this.length = length;
        }
    }

    /**
     * Given a loc, determine the staring offset and length for use in highlighting inside the editor
     * 
     * @param loc
     *            The location of the token we are interested in
     * @return The HighlighRange, or null if we cannot compute the range to highlight
     * @throws BadLocationException 
     */
    public static HighlightRange computeHighlightRange(Location loc, IDocument doc) throws BadLocationException {
        int startOffset = getStartOffset(loc, doc);
        int length = getLength(loc);
        return new HighlightRange(startOffset, length);
    }

    /**
     * Given a loc, determine the starting offset in the doc (just before the first character of the token)
     * 
     * @param loc
     *            The location of the token we are interested in
     * @param doc
     *            The IDocument that we want to translate the offsets to
     * @return The starting offset
     * @throws BadLocationException
     */
    public static int getStartOffset(Location loc, IDocument doc) throws BadLocationException {
        int lineStart = doc.getLineOffset(loc.line - 1);
        return lineStart + loc.column - 1;

    }

    /**
     * @param loc
     *            The {@link RealLoc} of the token we are interested in
     * @return The length of the location
     */
    public static int getLength(Location loc) {
        return loc.endIndex - loc.startIndex;
    }
}