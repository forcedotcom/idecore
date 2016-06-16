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

import apex.jorje.data.Location;
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
 * @author nchen
 *         
 */
public class OutlineViewSelectionHandler implements IOutlineViewElementHandler<HighlightRange> {
    private final ApexCodeEditor fEditor;
    
    OutlineViewSelectionHandler(ApexCodeEditor codeEditor) {
        fEditor = codeEditor;
    }
    
    /*
     * Tries to highlight in the editor if we have a valid location
     */
    private HighlightRange highlightTokenInEditorIfPossible(Location loc) {
        HighlightRange range;
        try {
            range = ParserLocationTranslator.computeHighlightRange(loc, fEditor.getDocument());
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
    	Location loc = userClass.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(UserInterface userInterface) {
    	Location loc = userInterface.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(UserTrigger userTrigger) {
    	Location loc = userTrigger.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(UserEnum userEnum) {
    	Location loc = userEnum.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(Method method) {
    	Location loc = method.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(Property property) {
    	Location loc = property.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
    
    @Override
    public HighlightRange handle(Field field) {
    	Location loc = field.getLoc();
        return highlightTokenInEditorIfPossible(loc);
    }
}