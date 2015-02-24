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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.text.source.DefaultAnnotationHover;

/**
 * The JavaAnnotationHover provides the hover support for java editors.
 */
@SuppressWarnings( { "restriction" })
public class ApexAnnotationHover extends DefaultAnnotationHover {

    /*
     * Formats a message as HTML text.
     */
    @Override
    protected String formatSingleMessage(String message) {
        StringBuffer buffer = new StringBuffer();
        HTMLPrinter.addPageProlog(buffer);
        HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(message));
        HTMLPrinter.addPageEpilog(buffer);
        return buffer.toString();
    }

    /*
     * Formats several message as HTML text.
     */
	@Override
    protected String formatMultipleMessages(@SuppressWarnings("rawtypes") List messages) {
        StringBuffer buffer = new StringBuffer();
        HTMLPrinter.addPageProlog(buffer);
        HTMLPrinter.addParagraph(buffer, HTMLPrinter
                .convertToHTMLContent(JavaUIMessages.JavaAnnotationHover_multipleMarkersAtThisLine));

        HTMLPrinter.startBulletList(buffer);
        Iterator<?> e = messages.iterator();
        while (e.hasNext())
            HTMLPrinter.addBullet(buffer, HTMLPrinter.convertToHTMLContent((String) e.next()));
        HTMLPrinter.endBulletList(buffer);

        HTMLPrinter.addPageEpilog(buffer);
        return buffer.toString();
    }
}
