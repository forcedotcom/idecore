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

import java.util.Stack;

import org.apache.log4j.Logger;
import org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner;
import org.eclipse.jdt.internal.ui.text.Symbols;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension3;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

/**
 * 
 * Derives from org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor.BracketInserter and is
 * governed by the terms of the original copyright on that file.
 * 
 */
@SuppressWarnings({ "restriction" })
public class BracketInserter implements VerifyKeyListener, ILinkedModeListener {
    private static final Logger logger = Logger.getLogger(BracketInserter.class);

    private boolean fCloseBrackets = true;
    private boolean fCloseStrings = true;
    private boolean fCloseAngularBrackets = true;
    private final String CATEGORY = toString();
    private IPositionUpdater fUpdater = new ExclusivePositionUpdater(CATEGORY);
    private Stack<BracketLevel> fBracketLevelStack = new Stack<>();

    private AbstractDecoratedTextEditor editor;
    private ISourceViewer viewer;

    public BracketInserter(AbstractDecoratedTextEditor editor, ISourceViewer viewer) {
        this.editor = editor;
        this.viewer = viewer;
    }

    public void setCloseBracketsEnabled(boolean enabled) {
        fCloseBrackets = enabled;
    }

    public void setCloseStringsEnabled(boolean enabled) {
        fCloseStrings = enabled;
    }

    public void setCloseAngularBracketsEnabled(boolean enabled) {
        fCloseAngularBrackets = enabled;
    }

    private static boolean isAngularIntroducer(String identifier) {
        return identifier.length() > 0
                && (Character.isUpperCase(identifier.charAt(0)) || identifier.startsWith("final") //$NON-NLS-1$
                        || identifier.startsWith("public") //$NON-NLS-1$
                        || identifier.startsWith("public") //$NON-NLS-1$
                        || identifier.startsWith("protected") //$NON-NLS-1$
                || identifier.startsWith("private")); //$NON-NLS-1$
    }

    public ISourceViewer getSourceViewer() {
        return viewer;
    }

    /*
     * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
     */
    @Override
    public void verifyKey(VerifyEvent event) {

        // early pruning to slow down normal typing as little as possible
        if (!event.doit || editor.getInsertMode() != ITextEditorExtension3.SMART_INSERT)
            return;
        switch (event.character) {
        case '(':
        case '<':
        case '[':
        case '\'':
        case '\"':
            break;
        default:
            return;
        }

        final ISourceViewer sourceViewer = getSourceViewer();
        IDocument document = sourceViewer.getDocument();

        final Point selection = sourceViewer.getSelectedRange();
        final int offset = selection.x;
        final int length = selection.y;

        try {
            IRegion startLine = document.getLineInformationOfOffset(offset);
            IRegion endLine = document.getLineInformationOfOffset(offset + length);

            JavaHeuristicScanner scanner = new JavaHeuristicScanner(document);
            int nextToken = scanner.nextToken(offset + length, endLine.getOffset() + endLine.getLength());
            String next =
                    nextToken == Symbols.TokenEOF ? null : document.get(offset, scanner.getPosition() - offset).trim();
            int prevToken = scanner.previousToken(offset - 1, startLine.getOffset());
            int prevTokenOffset = scanner.getPosition() + 1;
            String previous =
                    prevToken == Symbols.TokenEOF ? null : document.get(prevTokenOffset, offset - prevTokenOffset)
                            .trim();

            switch (event.character) {
            case '(':
                if (!fCloseBrackets || nextToken == Symbols.TokenLPAREN || nextToken == Symbols.TokenIDENT
                        || next != null && next.length() > 1)
                    return;
                break;

            case '<':
                if (!(fCloseAngularBrackets && fCloseBrackets) || nextToken == Symbols.TokenLESSTHAN
                        || prevToken != Symbols.TokenLBRACE && prevToken != Symbols.TokenRBRACE
                        && prevToken != Symbols.TokenSEMICOLON && prevToken != Symbols.TokenSYNCHRONIZED
                        && prevToken != Symbols.TokenSTATIC
                        && (prevToken != Symbols.TokenIDENT || !isAngularIntroducer(previous))
                        && prevToken != Symbols.TokenEOF)
                    return;
                break;

            case '[':
                if (!fCloseBrackets || nextToken == Symbols.TokenIDENT || next != null && next.length() > 1)
                    return;
                break;

            case '\'':
            case '"':
                if (!fCloseStrings || nextToken == Symbols.TokenIDENT || prevToken == Symbols.TokenIDENT
                        || next != null && next.length() > 1 || previous != null && previous.length() > 1)
                    return;
                break;

            default:
                return;
            }

            ITypedRegion partition =
                    TextUtilities.getPartition(document, IJavaPartitions.JAVA_PARTITIONING, offset, true);
            if (!IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType()))
                return;

            if (!editor.validateEditorInputState())
                return;

            final char character = event.character;
            final char closingCharacter = getPeerCharacter(character);
            final StringBuffer buffer = new StringBuffer();
            buffer.append(character);
            buffer.append(closingCharacter);

            document.replace(offset, length, buffer.toString());

            BracketLevel level = new BracketLevel();
            fBracketLevelStack.push(level);

            LinkedPositionGroup group = new LinkedPositionGroup();
            group.addPosition(new LinkedPosition(document, offset + 1, 0, LinkedPositionGroup.NO_STOP));

            LinkedModeModel model = new LinkedModeModel();
            model.addLinkingListener(this);
            model.addGroup(group);
            model.forceInstall();

            // set up position tracking for our magic peers
            if (fBracketLevelStack.size() == 1) {
                document.addPositionCategory(CATEGORY);
                document.addPositionUpdater(fUpdater);
            }
            level.fFirstPosition = new Position(offset, 1);
            level.fSecondPosition = new Position(offset + 1, 1);
            document.addPosition(CATEGORY, level.fFirstPosition);
            document.addPosition(CATEGORY, level.fSecondPosition);

            level.fUI = new EditorLinkedModeUI(model, sourceViewer);
            level.fUI.setSimpleMode(true);
            level.fUI.setExitPolicy(new ExitPolicy(closingCharacter, getEscapeCharacter(closingCharacter),
                    fBracketLevelStack));
            level.fUI.setExitPosition(sourceViewer, offset + 2, 0, Integer.MAX_VALUE);
            level.fUI.setCyclingMode(LinkedModeUI.CYCLE_NEVER);
            level.fUI.enter();

            IRegion newSelection = level.fUI.getSelectedRegion();
            sourceViewer.setSelectedRange(newSelection.getOffset(), newSelection.getLength());

            event.doit = false;

        } catch (BadLocationException e) {
            logger.error(e);
        } catch (BadPositionCategoryException e) {
            logger.error(e);
        }
    }

    /*
     * @see org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel, int)
     */
    @Override
    public void left(LinkedModeModel environment, int flags) {

        final BracketLevel level = fBracketLevelStack.pop();

        if (flags != ILinkedModeListener.EXTERNAL_MODIFICATION)
            return;

        // remove brackets
        final ISourceViewer sourceViewer = getSourceViewer();
        final IDocument document = sourceViewer.getDocument();
        if (document instanceof IDocumentExtension) {
            IDocumentExtension extension = (IDocumentExtension) document;
            extension.registerPostNotificationReplace(null, new IDocumentExtension.IReplace() {

                @Override
                public void perform(IDocument d, IDocumentListener owner) {
                    if ((level.fFirstPosition.isDeleted || level.fFirstPosition.length == 0)
                            && !level.fSecondPosition.isDeleted
                            && level.fSecondPosition.offset == level.fFirstPosition.offset) {
                        try {
                            document.replace(level.fSecondPosition.offset, level.fSecondPosition.length, ""); //$NON-NLS-1$
                        } catch (BadLocationException e) {
                            logger.error(e);
                        }
                    }

                    if (fBracketLevelStack.size() == 0) {
                        document.removePositionUpdater(fUpdater);
                        try {
                            document.removePositionCategory(CATEGORY);
                        } catch (BadPositionCategoryException e) {
                            logger.error(e);
                        }
                    }
                }
            });
        }

    }

    /*
     * @see org.eclipse.jface.text.link.ILinkedModeListener#suspend(org.eclipse.jface.text.link.LinkedModeModel)
     */
    @Override
    public void suspend(LinkedModeModel environment) {}

    /*
     * @see org.eclipse.jface.text.link.ILinkedModeListener#resume(org.eclipse.jface.text.link.LinkedModeModel, int)
     */
    @Override
    public void resume(LinkedModeModel environment, int flags) {}

    private static class BracketLevel {
        LinkedModeUI fUI;
        Position fFirstPosition;
        Position fSecondPosition;
    }

    /**
     * Position updater that takes any changes at the borders of a position to not belong to the position.
     *
     */
    private static class ExclusivePositionUpdater implements IPositionUpdater {

        /** The position category. */
        private final String fCategory;

        /**
         * Creates a new updater for the given <code>category</code>.
         * 
         * @param category
         *            the new category.
         */
        public ExclusivePositionUpdater(String category) {
            fCategory = category;
        }

        /*
         * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
         */
        @Override
        public void update(DocumentEvent event) {

            int eventOffset = event.getOffset();
            int eventOldLength = event.getLength();
            int eventNewLength = event.getText() == null ? 0 : event.getText().length();
            int deltaLength = eventNewLength - eventOldLength;

            try {
                Position[] positions = event.getDocument().getPositions(fCategory);

                for (int i = 0; i != positions.length; i++) {

                    Position position = positions[i];

                    if (position.isDeleted())
                        continue;

                    int offset = position.getOffset();
                    int length = position.getLength();
                    int end = offset + length;

                    if (offset >= eventOffset + eventOldLength)
                        // position comes
                        // after change - shift
                        position.setOffset(offset + deltaLength);
                    else if (end <= eventOffset) {
                        // position comes way before change -
                        // leave alone
                    } else if (offset <= eventOffset && end >= eventOffset + eventOldLength) {
                        // event completely internal to the position - adjust length
                        position.setLength(length + deltaLength);
                    } else if (offset < eventOffset) {
                        // event extends over end of position - adjust length
                        int newEnd = eventOffset;
                        position.setLength(newEnd - offset);
                    } else if (end > eventOffset + eventOldLength) {
                        // event extends from before position into it - adjust offset
                        // and length
                        // offset becomes end of event, length adjusted accordingly
                        int newOffset = eventOffset + eventNewLength;
                        position.setOffset(newOffset);
                        position.setLength(end - newOffset);
                    } else {
                        // event consumes the position - delete it
                        position.delete();
                    }
                }
            } catch (BadPositionCategoryException e) {
                // ignore and return
            }
        }
    }

    private class ExitPolicy implements IExitPolicy {

        final char fExitCharacter;
        final char fEscapeCharacter;
        final Stack<BracketLevel> fStack;
        final int fSize;

        public ExitPolicy(char exitCharacter, char escapeCharacter, Stack<BracketLevel> stack) {
            fExitCharacter = exitCharacter;
            fEscapeCharacter = escapeCharacter;
            fStack = stack;
            fSize = fStack.size();
        }

        /*
         * @see org.eclipse.jdt.internal.ui.text.link.LinkedPositionUI.ExitPolicy#doExit(org.eclipse.jdt.internal.ui.text.link.LinkedPositionManager, org.eclipse.swt.events.VerifyEvent, int, int)
         */
        @Override
        public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {

            if (fSize == fStack.size() && !isMasked(offset)) {
                if (event.character == fExitCharacter) {
                    BracketLevel level = fStack.peek();
                    if (level.fFirstPosition.offset > offset || level.fSecondPosition.offset < offset)
                        return null;
                    if (level.fSecondPosition.offset == offset && length == 0)
                        // don't enter the character if if its the closing peer
                        return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
                }
                // when entering an anonymous class between the parenthesis', we don't want
                // to jump after the closing parenthesis when return is pressed
                if (event.character == SWT.CR && offset > 0) {
                    IDocument document = getSourceViewer().getDocument();
                    try {
                        if (document.getChar(offset - 1) == '{')
                            return new ExitFlags(ILinkedModeListener.EXIT_ALL, true);
                    } catch (BadLocationException e) {}
                }
            }
            return null;
        }

        private boolean isMasked(int offset) {
            IDocument document = getSourceViewer().getDocument();
            try {
                return fEscapeCharacter == document.getChar(offset - 1);
            } catch (BadLocationException e) {}
            return false;
        }
    }

    private static char getEscapeCharacter(char character) {
        switch (character) {
        case '"':
        case '\'':
            return '\\';
        default:
            return 0;
        }
    }

    private static char getPeerCharacter(char character) {
        switch (character) {
        case '(':
            return ')';

        case ')':
            return '(';

        case '<':
            return '>';

        case '>':
            return '<';

        case '[':
            return ']';

        case ']':
            return '[';

        case '"':
            return character;

        case '\'':
            return character;

        default:
            throw new IllegalArgumentException();
        }
    }
}
