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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.preferences.PreferenceConstants;
import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;

/**
 * Auto indent line strategy sensitive to brackets.
 */
public class ApexAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {

    private static final Logger logger = Logger.getLogger(ApexAutoIndentStrategy.class);
    
    private String indent;

    public ApexAutoIndentStrategy() {
        
        indent = indentStringFromEditorsUIPreferences();
    }
    
    /**
     * Returns the String to use for indenting based on the General/Editors/Text Editors preferences
     * that are respected by the underlying platform editing code.
     * 
     * @return the String to use for indenting
     */
    private static String indentStringFromEditorsUIPreferences() {
        
        IPreferencesService ps = Platform.getPreferencesService();
        boolean spacesForTabs = ps.getBoolean(
                EditorsUI.PLUGIN_ID,
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS,
                false,
                null
                );
        if (spacesForTabs) {
            int tabWidth = ps.getInt(
                    EditorsUI.PLUGIN_ID,
                    AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH,
                    4,
                    null
                    );
            StringBuilder sb = new StringBuilder(tabWidth);
            for (int i = 0; i < tabWidth; i++) {
                sb.append(" ");
            }
            return sb.toString();
        } else {
            return "\t";
        }
    }

    /*
     * (non-Javadoc) Method declared on IAutoIndentStrategy
     */
    @Override
    public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
        if ((c.length == 0) && (c.text != null) && endsWithDelimiter(d, c.text)) {
            smartIndentAfterNewLine(d, c);
        } else if ("}".equals(c.text)) { //$NON-NLS-1$
            smartInsertAfterBracket(d, c);
        }
    }

    /**
     * Returns whether or not the given text ends with one of the documents legal line delimiters.
     * 
     * @param d
     *            the document
     * @param txt
     *            the text
     * @return <code>true</code> if <code>txt</code> ends with one of the document's line delimiters,
     *         <code>false</code> otherwise
     */
    private static boolean endsWithDelimiter(IDocument d, String txt) {
        String[] delimiters = d.getLegalLineDelimiters();
        if (delimiters != null) {
            return TextUtilities.endsWith(delimiters, txt) > -1;
        }
        return false;
    }

    /**
     * Returns the line number of the next bracket after end.
     * 
     * @param document -
     *            the document being parsed
     * @param line -
     *            the line to start searching back from
     * @param end -
     *            the end position to search back from
     * @param closingBracketIncrease -
     *            the number of brackets to skip
     * @return the line number of the next matching bracket after end
     * @throws BadLocationException
     *             in case the line numbers are invalid in the document
     */
    protected int findMatchingOpenBracket(IDocument document, int line, int end, int closingBracketIncrease)
            throws BadLocationException {

        int start = document.getLineOffset(line);
        int brackcount = getBracketCount(document, start, end, false) - closingBracketIncrease;

        // sum up the brackets counts of each line (closing brackets count
        // negative,
        // opening positive) until we find a line the brings the count to zero
        while (brackcount < 0) {
            line--;
            if (line < 0) {
                return -1;
            }
            start = document.getLineOffset(line);
            end = start + document.getLineLength(line) - 1;
            brackcount += getBracketCount(document, start, end, false);
        }
        return line;
    }

    /**
     * Returns the bracket value of a section of text. Closing brackets have a value of -1 and open brackets have a
     * value of 1.
     * 
     * The braces in the commented region (// or /* .. are ignored ( not counted )
     * 
     * @param document -
     *            the document being parsed
     * @param start -
     *            the start position for the search
     * @param end -
     *            the end position for the search
     * @param ignoreCloseBrackets -
     *            whether or not to ignore closing brackets in the count
     * @return the bracket value of a section of text
     * @throws BadLocationException
     *             in case the positions are invalid in the document
     */
    private static int getBracketCount(IDocument document, int start, int end, boolean ignoreCloseBrackets)
            throws BadLocationException {

        int begin = start;
        int bracketcount = 0;
        while (begin < end) {
            char curr = document.getChar(begin);
            begin++;
            switch (curr) {
            case '/':
                if (begin < end) {
                    char next = document.getChar(begin);
                    if (next == '*') {
                        // a comment starts, advance to the comment end
                        begin = getCommentEnd(document, begin + 1, end);
                    } else if (next == '/') {
                        // '//'-comment: nothing to do anymore on this line
                        //  change the begin index to the start of the next line if it exists 
                        //  else change the begin index to end.
                        int lineNumber = document.getLineOfOffset(begin);
                        if(lineNumber + 1 >= document.getNumberOfLines()){
                            begin = end;
                        }else{
                            begin = document.getLineOffset(lineNumber+1);
                        }
                        
                    }
                }
                break;
            case '*':
                if (begin < end) {
                    char next = document.getChar(begin);
                    if (next == '/') {
                        // we have been in a comment: forget what we read before
                        bracketcount = 0;
                        begin++;
                    }
                }
                break;
            case '{':
                bracketcount++;
                ignoreCloseBrackets = false;
                break;
            case '}':
                if (!ignoreCloseBrackets) {
                    bracketcount--;
                }
                break;
            case '"':
            case '\'':
                begin = getStringEnd(document, begin, end, curr);
                break;
            default:
            }
        }
        return bracketcount;
    }

    /**
     * Returns the end position of a comment starting at the given <code>position</code>.
     * 
     * @param document -
     *            the document being parsed
     * @param position -
     *            the start position for the search
     * @param end -
     *            the end position for the search
     * @return the end position of a comment starting at the given <code>position</code>
     * @throws BadLocationException
     *             in case <code>position</code> and <code>end</code> are invalid in the document
     */
    private static int getCommentEnd(IDocument document, int position, int end) throws BadLocationException {
        int currentPosition = position;
        while (currentPosition < end) {
            char curr = document.getChar(currentPosition);
            currentPosition++;
            if (curr == '*') {
                if ((currentPosition < end) && (document.getChar(currentPosition) == '/')) {
                    return currentPosition + 1;
                }
            }
        }
        return end;
    }

    /**
     * Returns the content of the given line without the leading whitespace.
     * 
     * @param document -
     *            the document being parsed
     * @param line -
     *            the line being searched
     * @return the content of the given line without the leading whitespace
     * @throws BadLocationException
     *             in case <code>line</code> is invalid in the document
     */
    protected String getIndentOfLine(IDocument document, int line) throws BadLocationException {
        if (line > -1) {
            int start = document.getLineOffset(line);
            int end = start + document.getLineLength(line) - 1;
            int whiteend = findEndOfWhiteSpace(document, start, end);
            return document.get(start, whiteend - start);
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the position of the <code>character</code> in the <code>document</code> after <code>position</code>.
     * 
     * @param document -
     *            the document being parsed
     * @param position -
     *            the position to start searching from
     * @param end -
     *            the end of the document
     * @param character -
     *            the character you are trying to match
     * @return the next location of <code>character</code>
     * @throws BadLocationException
     *             in case <code>position</code> is invalid in the document
     */
    private static int getStringEnd(IDocument document, int position, int end, char character) throws BadLocationException {
        int currentPosition = position;
        while (currentPosition < end) {
            char currentCharacter = document.getChar(currentPosition);
            currentPosition++;
            if (currentCharacter == '\\') {
                // ignore escaped characters
                currentPosition++;
            } else if (currentCharacter == character) {
                return currentPosition;
            }
        }
        return end;
    }

    /**
     * Set the indent of a new line based on the command provided in the supplied document.
     * 
     * @param document -
     *            the document being parsed
     * @param command -
     *            the command being performed
     */
    protected void smartIndentAfterNewLine(IDocument document, DocumentCommand command) {

        int docLength = document.getLength();
        if ((command.offset == -1) || (docLength == 0)) {
            return;
        }

        try {
            int p = (command.offset == docLength ? command.offset - 1 : command.offset);

            int line = document.getLineOfOffset(p);
            StringBuffer buf = new StringBuffer(command.text);

            int start = document.getLineOffset(line);
            IPreferenceStore preferenceStore = getPreferenceStore();
            boolean closeBraces = preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_BRACES);

            int lastChar = findLastNonWhiteSpace(document, start, command.offset);
            int whiteend = findEndOfWhiteSpace(document, start, command.offset);

            // insert closing brace on new line after an unclosed opening brace
            if (getBracketCount(document, 0, docLength, false) > 0 && closeBraces
                    && (document.getChar(lastChar) == '{')) {
                buf.append(document.get(start, whiteend - start));
                buf.append(indent);
                command.caretOffset = command.offset + buf.length();
                command.shiftsCaret = false;
                buf.append('\n');
                buf.append(document.get(start, whiteend - start));
                buf.append('}');
            } else if ((command.offset < docLength) && (document.getChar(command.offset) == '}')) {
                int indLine = findMatchingOpenBracket(document, line, command.offset, 0);
                if (indLine == -1) {
                    indLine = line;
                }
                buf.append(getIndentOfLine(document, indLine));
            } else {
                buf.append(document.get(start, whiteend - start));
                if (getBracketCount(document, start, command.offset, true) > 0) {
                    buf.append(indent);
                }
            }
            command.text = buf.toString();

        } catch (BadLocationException excp) {
            logger.warn(EditorMessages.getString("ApexEditor.AutoIndent.error.bad_location_1"));
        }
        
    }

    /**
     * Set the indent of a bracket based on the command provided in the supplied document.
     * 
     * @param document -
     *            the document being parsed
     * @param command -
     *            the command being performed
     */
    protected void smartInsertAfterBracket(IDocument document, DocumentCommand command) {
        if ((command.offset == -1) || (document.getLength() == 0)) {
            return;
        }

        try {
            int p = (command.offset == document.getLength() ? command.offset - 1 : command.offset);
            int line = document.getLineOfOffset(p);
            int start = document.getLineOffset(line);
            int whiteend = findEndOfWhiteSpace(document, start, command.offset);

            // shift only when line does not contain any text up to the closing
            // bracket
            if (whiteend == command.offset) {
                // evaluate the line with the opening bracket that matches out
                // closing bracket
                int indLine = findMatchingOpenBracket(document, line, command.offset, 1);
                if ((indLine != -1) && (indLine != line)) {
                    // take the indent of the found line
                    StringBuffer replaceText = new StringBuffer(getIndentOfLine(document, indLine));
                    // add the rest of the current line including the just added
                    // close bracket
                    replaceText.append(document.get(whiteend, command.offset - whiteend));
                    replaceText.append(command.text);
                    // modify document command
                    command.length = command.offset - start;
                    command.offset = start;
                    command.text = replaceText.toString();
                }
            }
        } catch (BadLocationException excp) {
            logger.warn(EditorMessages.getString("ApexEditor.AutoIndent.error.bad_location_2")); //$NON-NLS-1$
        }
    }

    private static IPreferenceStore getPreferenceStore() {
        return ForceIdeEditorsPlugin.getDefault().getPreferenceStore();
    }

    protected int findLastNonWhiteSpace(IDocument document, int offset, int end) {
        try {
            IRegion region = document.getLineInformationOfOffset(offset);

            String lineText = document.get(offset, region.getOffset() + region.getLength() - offset);
            String[] tokens = lineText.split("\\s+"); //$NON-NLS-1$

            if (!Utils.isEmpty(tokens)) {
                for(int i = tokens.length - 1; i > -1; i--)
                {
                    String token = tokens[i];
                    if (!Utils.isEmpty(token)) {
                        offset += lineText.lastIndexOf(token) + token.length() - 1 ;
                        break;
                    }
                }
            }
        } catch (BadLocationException e) {
            logger.error("Unable to compute offset", e); //$NON-NLS-1$
        }

        return offset;
    }
}
