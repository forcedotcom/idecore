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

import org.eclipse.jface.text.BadLocationException;

import apex.jorje.data.Loc.RealLoc;
import apex.jorje.data.ast.BlockMember.FieldMember;
import apex.jorje.data.ast.BlockMember.InnerClassMember;
import apex.jorje.data.ast.BlockMember.InnerEnumMember;
import apex.jorje.data.ast.BlockMember.InnerInterfaceMember;
import apex.jorje.data.ast.BlockMember.MethodMember;
import apex.jorje.data.ast.BlockMember.PropertyMember;
import apex.jorje.data.ast.BlockMember.StaticStmntBlockMember;
import apex.jorje.data.ast.BlockMember.StmntBlockMember;
import apex.jorje.data.ast.ClassDecl;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.data.ast.EnumDecl;
import apex.jorje.data.ast.Identifier;
import apex.jorje.data.ast.InterfaceDecl;

import com.salesforce.ide.ui.editors.apex.outline.IOutlineViewElementHandlerOld;
import com.salesforce.ide.ui.editors.apex.util.ParserLocationTranslator;
import com.salesforce.ide.ui.editors.apex.util.ParserLocationTranslator.HighlightRange;

/**
 * Highlights the relevant element in the editor when a selection occurs in the outline view.
 * 
 * Class is package-protected so that we can use it for testing purposes. The handlers all return HighlightRange objects
 * for testing purposes.
 * 
 * @author nchen
 * 
 */
class OutlineViewSelectionHandlerOld implements IOutlineViewElementHandlerOld<HighlightRange> {
    private final ApexCodeEditor fEditor;

    OutlineViewSelectionHandlerOld(ApexCodeEditor codeEditor) {
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

    @Override
    public HighlightRange handle(TriggerDeclUnit element) {
        RealLoc loc = (RealLoc) element.name.loc;
        return highlightTokenInEditorIfPossible(loc);
    }

    @Override
    public HighlightRange handle(ClassDecl element) {
        RealLoc loc = (RealLoc) element.name.loc;
        return highlightTokenInEditorIfPossible(loc);
    }

    @Override
    public HighlightRange handle(InterfaceDecl element) {
        RealLoc loc = (RealLoc) element.name.loc;
        return highlightTokenInEditorIfPossible(loc);
    }

    @Override
    public HighlightRange handle(EnumDecl element) {
        RealLoc loc = (RealLoc) element.name.loc;
        return highlightTokenInEditorIfPossible(loc);
    }

    @Override
    public HighlightRange handle(InnerClassMember element) {
        RealLoc loc = (RealLoc) element.body.name.loc;
        return highlightTokenInEditorIfPossible(loc);
    }

    @Override
    public HighlightRange handle(InnerInterfaceMember element) {
        RealLoc loc = (RealLoc) element.body.name.loc;
        return highlightTokenInEditorIfPossible(loc);
    }

    @Override
    public HighlightRange handle(InnerEnumMember element) {
        RealLoc loc = (RealLoc) element.body.name.loc;
        return highlightTokenInEditorIfPossible(loc);
    }

    @Override
    public HighlightRange handle(FieldMember element) {
        // Because of the transformation that we do in com.salesforce.ide.ui.editors.apex.outline.ClassMemberFilter
        // We are guaranteed that there is only ever one decls in the FieldMember, so we can use get(0)
        RealLoc loc = (RealLoc) element.variableDecls.decls.get(0).name.loc;
        return highlightTokenInEditorIfPossible(loc);
    }

    @Override
    public HighlightRange handle(MethodMember element) {
        RealLoc loc = (RealLoc) element.methodDecl.name.loc;
        return highlightTokenInEditorIfPossible(loc);
    }

    @Override
    public HighlightRange handle(PropertyMember element) {
        RealLoc loc = (RealLoc) element.propertyDecl.name.loc;
        return highlightTokenInEditorIfPossible(loc);
    }

    @Override
    public HighlightRange handle(Identifier element) {
        RealLoc loc = (RealLoc) element.loc;
        return highlightTokenInEditorIfPossible(loc);
    }

    @Override
    public HighlightRange handle(StmntBlockMember element) {
        RealLoc loc = (RealLoc) element.loc;
        return placeCursorOnLineIfPossible(loc);
    }

    @Override
    public HighlightRange handle(StaticStmntBlockMember element) {
        RealLoc loc = (RealLoc) element.loc;
        return placeCursorOnLineIfPossible(loc);
    }
}
