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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.wst.html.ui.internal.Logger;

import apex.jorje.data.Loc;
import apex.jorje.data.Optional.Some;
import apex.jorje.data.ast.BlockMember;
import apex.jorje.data.ast.ClassDecl;
import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.data.ast.CompilationUnit.ClassDeclUnit;
import apex.jorje.data.ast.CompilationUnit.EnumDeclUnit;
import apex.jorje.data.ast.CompilationUnit.InterfaceDeclUnit;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.data.ast.MethodDecl;
import apex.jorje.data.ast.Stmnt.BlockStmnt;
import apex.jorje.parser.impl.ApexParserImpl;

import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.apex.errors.ApexErrorMarkerHandler;
import com.salesforce.ide.ui.editors.apex.outline.ApexContentOutlinePage;
import com.salesforce.ide.ui.editors.apex.outline.ApexOutlineContentProvider;
import com.salesforce.ide.ui.editors.apex.parser.IdeApexParser;
import com.salesforce.ide.ui.editors.apex.preferences.PreferenceConstants;

/**
 * The runnable that is called each time the reconciler needs to run.
 * 
 * @author nchen
 *
 */
public class ApexParserRunnable implements ISafeRunnable {
    private ApexReconcilingStrategy apexReconcilingStrategy;
    private ApexParserImpl fParser;
    private ApexErrorMarkerHandler fMarkerHandler;
    private CompilationUnit fCompilationUnit;

    // For testing purposes
    protected ApexParserRunnable() {

    }

    public ApexParserRunnable(ApexReconcilingStrategy apexReconcilingStrategy) {
        this.apexReconcilingStrategy = apexReconcilingStrategy;
        IFile file = ((IFileEditorInput) apexReconcilingStrategy.fTextEditor.getEditorInput()).getFile();
        IDocument doc = apexReconcilingStrategy.fTextEditor.getDocument();
        fMarkerHandler = new ApexErrorMarkerHandler(file, doc);
    }
    
    private IAnnotationModel getAnnotationModel(ITextEditor editor) {
        return (IAnnotationModel) editor.getAdapter(ProjectionAnnotationModel.class);
    }

    @Override
    public void run() throws Exception {
        clearExistingErrorMarkers();
        if (checkShouldUpdate()) {
            parseCurrentEditorContents();
            reportParseErrors();
            updateOutlineViewIfPossible();
            
            
            final ApexCodeEditor editor=this.apexReconcilingStrategy.fTextEditor;
            final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
            
    
            final IAnnotationModel model = getAnnotationModel(editor);
            if (model != null) {
                            
	            fCompilationUnit.match(new CompilationUnit.MatchBlockWithDefault<Boolean>() {
	
	                @Override
	                public Boolean _case(ClassDeclUnit x) {
	                	Loc.RealLoc locBody=(Loc.RealLoc)x.body.loc;
	                	Position position = new Position(locBody.startIndex, locBody.endIndex-locBody.startIndex);
						model.addAnnotation(new ProjectionAnnotation(), position);
	                	
						Object[] objArray = ApexOutlineContentProvider.childrenOf(x.body);
	                    
						for(Object o : objArray){
							Loc.RealLoc loc=null;
							if(o.getClass() == (BlockMember.MethodMember.class)){
								BlockMember.MethodMember methodMember=(BlockMember.MethodMember) o;
								MethodDecl decl=(MethodDecl)methodMember.methodDecl;
								Some sm=(Some)methodMember.methodDecl.stmnt;
								BlockStmnt block=(BlockStmnt) sm.value;
								loc=(Loc.RealLoc)block.stmnts.loc;
							}
							if(o.getClass() == (BlockMember.InnerClassMember.class)){
								BlockMember.InnerClassMember innerClass=(BlockMember.InnerClassMember) o;
								loc=(Loc.RealLoc)innerClass.body.loc;
							}
							if(o.getClass() == (BlockMember.InnerInterfaceMember.class)){
								BlockMember.InnerInterfaceMember innerInterface=(BlockMember.InnerInterfaceMember) o;
								loc=(Loc.RealLoc)innerInterface.body.loc;
							}
							if(o.getClass() == (BlockMember.InnerEnumMember.class)){
								BlockMember.InnerEnumMember innerEnum=(BlockMember.InnerEnumMember) o;
								loc=(Loc.RealLoc)innerEnum.body.loc;
							}
						
							
							if(null==loc)
								continue;
							
								Position pos=null;
								
								try {
									int offset=loc.startIndex;
									int endLine = document.getLineOfOffset(loc.endIndex);
									int endOffset =document.getLineOffset(endLine+1);
									
									pos = new Position(offset, endOffset-offset);
									
									
								} catch (BadLocationException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
	                            
								if(null==pos)
									continue;
								
								model.addAnnotation(new ProjectionAnnotation(), pos);
			                
							
						}
	                    return x.body != null;
	                }
	
	                @Override
	                public Boolean _case(EnumDeclUnit x) {
	                    return x.body != null;
	                }
	
	                @Override
	                public Boolean _case(InterfaceDeclUnit x) {
	                    return x.body != null;
	                }
	
	                @Override
	                public Boolean _case(TriggerDeclUnit x) {
	                    return x.name != null;
	                }
	
	                @Override
	                protected Boolean _default(CompilationUnit arg0) {
	                	
	                    return false;
	                }
	            });
	            
            }
        }
    }

    protected boolean checkShouldUpdate() {
        IPreferenceStore preferenceStore = ForceIdeEditorsPlugin.getDefault().getPreferenceStore();
        boolean shouldUpdate = preferenceStore.getBoolean(PreferenceConstants.EDITOR_PARSE_WITH_NEW_COMPILER);
        return shouldUpdate;
    }

    protected void clearExistingErrorMarkers() {
        fMarkerHandler.clearExistingMarkers();
    }

    protected void parseCurrentEditorContents() throws Exception {
        fParser = IdeApexParser.initializeParser(this.apexReconcilingStrategy.fTextEditor.getText());
        fCompilationUnit = fParser.compilationUnit();
    }

    protected void reportParseErrors() {
        fMarkerHandler.handleSyntaxErrors(fParser.getSyntaxErrors());
    }

    protected void updateOutlineViewIfPossible() {
        if (canDisplayOutline()) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    ApexContentOutlinePage outline =
                            (ApexContentOutlinePage) apexReconcilingStrategy.fTextEditor
                                    .getAdapter(IContentOutlinePage.class);
                    outline.update(fCompilationUnit);
                }
            });
        }
    }

    /*
     * Sometimes the parser trips on things and returns us something that is
     * not-displayable (jADT nodes are empty). In that case, it is better to
     * just retain the previous outline view (which would be stale, but
     * presentable). We use some simple heuristic here.
     */
    private boolean canDisplayOutline() {
        if (fCompilationUnit != null) {
            return fCompilationUnit.match(new CompilationUnit.MatchBlockWithDefault<Boolean>() {

                @Override
                public Boolean _case(ClassDeclUnit x) {
                    return x.body != null;
                }

                @Override
                public Boolean _case(EnumDeclUnit x) {
                    return x.body != null;
                }

                @Override
                public Boolean _case(InterfaceDeclUnit x) {
                    return x.body != null;
                }

                @Override
                public Boolean _case(TriggerDeclUnit x) {
                    return x.name != null;
                }

                @Override
                protected Boolean _default(CompilationUnit arg0) {
                    return false;
                }
            });
        }

        return false;
    }

    @Override
    public void handleException(Throwable exception) {
        // This is for any other exceptions that we do not handle
        ApexReconcilingStrategy.logger.debug("Error occured during reconcile", exception);
    }
}