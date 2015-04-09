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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.MarkerUtilities;

import apex.jorje.data.Loc;
import apex.jorje.data.Loc.RealLoc;
import apex.jorje.data.Loc.SyntheticLoc;
import apex.jorje.data.errors.UserError;
import apex.jorje.services.exception.ParseException;
import apex.jorje.services.printers.PrintContext;
import apex.jorje.services.printers.PrinterUtil;

import com.salesforce.ide.core.project.MarkerUtils;

/**
 * Displays the error markers in the editor based on input from the local compiler.
 * 
 * @author nchen
 * 
 */
public class ApexErrorMarkerHandler {
    private static final Logger logger = Logger.getLogger(ApexErrorMarkerHandler.class);
    private final PrinterUtil printerUtil = PrinterUtil.get();
    private final PrintContext printContext = new PrintContext();

    private final IFile fFile;
    private final IDocument fDocument;

    public ApexErrorMarkerHandler(IFile fFile, IDocument fDocument) {
        this.fFile = fFile;
        this.fDocument = fDocument;
    }

    public void clearExistingMarkers() {
        MarkerUtils.getInstance().clearMarkers(fFile, null, MarkerUtils.MARKER_COMPILE_ERROR);
    }

    public void handleSyntaxErrors(List<ParseException> parseExceptions) {
        for (ParseException parseException : parseExceptions) {
            Map<String, Object> config = createSyntacticMarkerIfApplicable(parseException);
            if (config != null) {
                MarkerUtils.getInstance().createMarker(fFile, config, MarkerUtils.MARKER_COMPILE_ERROR);
            }
        }
    }

    private Map<String, Object> createSyntacticMarkerIfApplicable(final ParseException parseException) {
        Loc loc = parseException.getLoc();
        return loc.match(new Loc.MatchBlock<Map<String, Object>>() {

            @Override
            public Map<String, Object> _case(RealLoc x) {
                try {
                    UserError userError = parseException.getUserError();
                    Map<String, Object> config = new HashMap<>();

                    // There is the option to set the line number as well. However, that config is ignored if
                    // we set the CharStart and CharEnd. So, we only set the latter.
                    MarkerUtilities.setCharStart(config, getStartOffset(x));
                    MarkerUtilities.setCharEnd(config, getEndOffset(x));

                    MarkerUtilities.setMessage(config,
                        printerUtil.getFactory().userErrorPrinter().print(userError, printContext));

                    // Not sure why there aren't any utilities methods for these fields in MarkerUtilities
                    // So set them directly instead.
                    config.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                    config.put(IMarker.LOCATION, fFile.getFullPath().toString());
                    return config;
                } catch (BadLocationException ble) {
                    logger.warn("Error calculating offset to document using parser position", ble);
                    return null;
                }
            }

            @Override
            public Map<String, Object> _case(SyntheticLoc x) {
                return null;
            }
        });
    }

    /*
     * Translates to offset-based start location that IDocument uses.
     */
    private int getStartOffset(RealLoc rl) throws BadLocationException {
        int line = rl.line;
        int column = rl.column;
        int lineStart;
        lineStart = fDocument.getLineOffset(line - 1);
        return lineStart + (column);
    }

    /*
     * Translates to offset-based end location that IDocument uses.
     * Tries to find the length of the token. If it cannot find one, then we set the end to be (start + 1)
     * so we still get the squiggly line.
     */
    private int getEndOffset(RealLoc rl) throws BadLocationException {
        int offset = rl.startIndex - rl.endIndex;
        return getStartOffset(rl) + offset;
    }

}
