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
package com.salesforce.ide.ui.editors.apex.errors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.MarkerUtilities;

import apex.jorje.data.Loc;
import apex.jorje.data.Loc.RealLoc;
import apex.jorje.data.Loc.SyntheticLoc;
import apex.jorje.data.errors.ApexUserException;
import apex.jorje.data.errors.SemanticError;
import apex.jorje.data.errors.SemanticError.DuplicateAnnotationName;
import apex.jorje.data.errors.SemanticError.InterfaceMethodsCannotHaveAnnotations;
import apex.jorje.data.errors.SemanticError.MissingReturn;
import apex.jorje.data.errors.SemanticError.UndeclaredVariable;
import apex.jorje.data.errors.UserError;
import apex.jorje.data.errors.UserError.Semantic;
import apex.jorje.services.errors.ErrorReporter;
import apex.jorje.services.printers.PrinterUtil;

import com.salesforce.ide.core.project.MarkerUtils;
import com.salesforce.ide.ui.editors.apex.util.ParserLocationTranslator;

/**
 * Displays the error markers in the editor based on input from the local compiler.
 * 
 * @author nchen
 * 
 */
public class ApexErrorMarkerHandler {
    private static final Logger logger = Logger.getLogger(ApexErrorMarkerHandler.class);

    private IFile fFile;
    private IDocument fDocument;

    public ApexErrorMarkerHandler(IFile fFile, IDocument fDocument) {
        this.fFile = fFile;
        this.fDocument = fDocument;
    }

    public void clearExistingMarkers() {
        MarkerUtils.getInstance().clearMarkers(fFile, null, MarkerUtils.MARKER_COMPILE_ERROR);
    }

    public void handleSyntaxErrors(Collection<ApexUserException> syntaxErrors) {
        for (ApexUserException apexException : syntaxErrors) {
            Map<String, Object> config = createSyntacticMarkerIfApplicable(apexException);
            if (config != null) {
                MarkerUtils.getInstance().createMarker(fFile, config, MarkerUtils.MARKER_COMPILE_ERROR);
            }
        }
    }

    private Map<String, Object> createSyntacticMarkerIfApplicable(ApexUserException apexException) {
        if (isDisplayableError(apexException)) {
            try {
                RecognitionException recognitionException = (RecognitionException) apexException.getCause();
                Map<String, Object> config = new HashMap<String, Object>();

                // There is the option to set the line number as well. However, that config is ignored if
                // we set the CharStart and CharEnd. So, we only set the latter.
                MarkerUtilities.setCharStart(config, getStartOffset(recognitionException));
                MarkerUtilities.setCharEnd(config, getEndOffset(recognitionException));

                // Need to implement the first, currently get a NotImplementedYet exception
                // MarkerUtilities.setMessage(config, PrinterUtil.ENGLISH.print(apexException.getError()));
                MarkerUtilities.setMessage(config, apexException.getMessage());

                // Not sure why there aren't any utilities methods for these fields in MarkerUtilities
                // Set them directly instead.
                config.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                config.put(IMarker.LOCATION, fFile.getFullPath().toString());

                return config;
            } catch (BadLocationException e) {
                logger.warn("Error calculating offset to document using parser position", e);
                return null;
            }
        }
        return null;
    }

    public void handleSemanticErrors(ErrorReporter<ApexUserException> semanticErrors) {
        for (ApexUserException apexException : semanticErrors.getErrors()) {
            Map<String, Object> config = createSemanticMarkerIfApplicable(apexException);
            if (config != null) {
                MarkerUtils.getInstance().createMarker(fFile, config, MarkerUtils.MARKER_COMPILE_ERROR);
            }
        }
    }

    //TODO: When this is more stable, factor out into its class instead of deeply-nesting it
    private Map<String, Object> createSemanticMarkerIfApplicable(final ApexUserException apexException) {
        return apexException.getError().match(new UserError.MatchBlockWithDefault<Map<String, Object>>() {

            /*
             * Only handle the case of Semantic exceptions. Leave the rest out.
             */
            @Override
            public Map<String, Object> _case(Semantic semantic) {
                return semantic.error.match(new SemanticError.MatchBlockWithDefault<Map<String, Object>>() {

                    // The following are all cases that explicitly report the position of error
                    @Override
                    public Map<String, Object> _case(UndeclaredVariable x) {
                        return createMarkerWithLocation(x, x.loc);
                    }

                    @Override
                    public Map<String, Object> _case(MissingReturn x) {
                        return createMarkerWithLocation(x, x.loc);
                    }

                    @Override
                    public Map<String, Object> _case(DuplicateAnnotationName x) {
                        return createMarkerWithLocation(x, x.loc);
                    }

                    @Override
                    public Map<String, Object> _case(InterfaceMethodsCannotHaveAnnotations x) {
                        return createMarkerWithLocation(x, x.loc);
                    }

                    private Map<String, Object> createMarkerWithLocation(final SemanticError error, Loc loc) {
                        return loc.match(new Loc.MatchBlock<Map<String, Object>>() {

                            @Override
                            public Map<String, Object> _case(RealLoc loc) {
                                try {
                                    Map<String, Object> config = new HashMap<String, Object>();
                                    MarkerUtilities.setMessage(config,
                                        PrinterUtil.INSTANCE.print(apexException.getError()));

                                    MarkerUtilities.setCharStart(config,
                                        ParserLocationTranslator.getStartOffset(loc, fDocument));
                                    MarkerUtilities.setCharEnd(config,
                                        ParserLocationTranslator.getEndOffset(loc, fDocument));

                                    config.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                                    config.put(IMarker.LOCATION, fFile.getFullPath().toString());

                                    return config;
                                } catch (BadLocationException e) {
                                    return _default(error);
                                }
                            }

                            @Override
                            public Map<String, Object> _case(SyntheticLoc loc) {
                                return _default(error);
                            }
                        });
                    }

                    /*
                     * Everything else, just put on the first line since we don't have any other information
                     */
                    @Override
                    protected Map<String, Object> _default(SemanticError x) {
                        Map<String, Object> config = new HashMap<String, Object>();

                        MarkerUtilities.setLineNumber(config, 1);
                        // Need to implement the first, currently get a NotImplementedYet exception
                        MarkerUtilities.setMessage(config, PrinterUtil.INSTANCE.print(apexException.getError()));

                        // Not sure why there aren't any utilities methods for these fields in MarkerUtilities
                        // Set them directly instead.
                        config.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                        config.put(IMarker.LOCATION, fFile.getFullPath().toString());
                        return config;
                    }
                });
            }

            @Override
            protected Map<String, Object> _default(UserError x) {
                return null;
            }
        });
    }

    /*
     * There can many different types of errors and we want to only handle the ones that can provide a location
     *(so that we can display them properly)
     * TODO: Perhaps create a super interface LocationApplicableException in the parser?
     */
    private boolean isDisplayableError(ApexUserException apexException) {
        return apexException.getCause() instanceof RecognitionException;
    }

    /*
     * Translates to offset-based start location that IDocument uses.
     */
    private int getStartOffset(RecognitionException recognitionException) throws BadLocationException {
        int line = recognitionException.line;
        int column = recognitionException.charPositionInLine;
        int lineStart;
        lineStart = fDocument.getLineOffset(line - 1);
        return lineStart + (column);
    }

    /*
     * Translates to offset-based end location that IDocument uses.
     * Tries to find the length of the token. If it cannot find one, then we set the end to be (start + 1)
     * so we still get the squiggly line.
     */
    private int getEndOffset(RecognitionException recognitionException) throws BadLocationException {
        String text = recognitionException.token.getText() == null ? " " : recognitionException.token.getText();
        return getStartOffset(recognitionException) + text.length();
    }

}
