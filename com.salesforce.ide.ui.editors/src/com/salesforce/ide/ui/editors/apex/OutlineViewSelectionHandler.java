/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.editors.apex;

import org.eclipse.jface.text.BadLocationException;

import com.salesforce.ide.ui.editors.apex.outline.IOutlineViewElementHandler;
import com.salesforce.ide.ui.editors.apex.util.ParserLocationTranslator;
import com.salesforce.ide.ui.editors.apex.util.ParserLocationTranslator.HighlightRange;

import apex.jorje.data.Loc.RealLoc;
import apex.jorje.semantic.ast.compilation.UserClass;
import apex.jorje.semantic.ast.compilation.UserEnum;
import apex.jorje.semantic.ast.compilation.UserInterface;
import apex.jorje.semantic.ast.compilation.UserTrigger;
import apex.jorje.semantic.ast.member.Field;
import apex.jorje.semantic.ast.member.Method;
import apex.jorje.semantic.ast.member.Property;

/**
 * Highlights the relevant element in the editor when a selection occurs in the outline view.
 * 
 * Class is package-protected so that we can use it for testing purposes. The handlers all return HighlightRange objects
 * for testing purposes.
 * 
 * @author nchen
 *         
 */
class OutlineViewSelectionHandler implements IOutlineViewElementHandler<HighlightRange> {
    private final ApexCodeEditor fEditor;
    
    OutlineViewSelectionHandler(ApexCodeEditor codeEditor) {
        fEditor = codeEditor;
    }
    
    /*
     * Tries to highlight in the editor if we have a valid location
     */
    protected HighlightRange highlightTokenInEditorIfPossible(RealLoc loc) {
        HighlightRange range;
        try {
            range = ParserLocationTranslator.computeHighlightRange(loc, fEditor.getDocument());
            fEditor.setSelection(range, true);
            return range;
        } catch (BadLocationException e) {
            return null;
        }
    }
    
    /*
     * Tries to place the cursor on the token (0-length selection) if we have a valid location
     */
    protected HighlightRange placeCursorOnLineIfPossible(RealLoc loc) {
        try {
            int startOffset = ParserLocationTranslator.getStartOffset(loc, fEditor.getDocument());
            HighlightRange range = new HighlightRange(startOffset, 0);
            fEditor.setSelection(range, true);
            return range;
        } catch (BadLocationException e) {
            return null;
        }
    }
    
    /**
     * Assumes that all locations are RealLoc because of the filtering done in {@see ApexOutlineContentProvider}.
     */
    
    @Override
    public HighlightRange handle(UserClass userClass) {
        RealLoc loc = (RealLoc) userClass.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(UserInterface userInterface) {
        RealLoc loc = (RealLoc) userInterface.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(UserTrigger userTrigger) {
        RealLoc loc = (RealLoc) userTrigger.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(UserEnum userEnum) {
        RealLoc loc = (RealLoc) userEnum.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(Method method) {
        RealLoc loc = (RealLoc) method.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(Property property) {
        RealLoc loc = (RealLoc) property.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(Field field) {
        RealLoc loc = (RealLoc) field.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
}
