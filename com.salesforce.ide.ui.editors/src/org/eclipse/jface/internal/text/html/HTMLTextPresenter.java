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
package org.eclipse.jface.internal.text.html;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.internal.text.link.contentassist.LineBreakingReader;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;


/**
 * <p>
 * Moved into this package from <code>org.eclipse.jface.internal.text.revisions</code>.</p>
 */
public class HTMLTextPresenter implements DefaultInformationControl.IInformationPresenter, DefaultInformationControl.IInformationPresenterExtension {

	private static final String LINE_DELIM= System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	private int fCounter;
	private boolean fEnforceUpperLineLimit;

	public HTMLTextPresenter(boolean enforceUpperLineLimit) {
		super();
		fEnforceUpperLineLimit= enforceUpperLineLimit;
	}

	public HTMLTextPresenter() {
		this(true);
	}

	protected Reader createReader(String hoverInfo, TextPresentation presentation) {
		return new HTML2TextReader(new StringReader(hoverInfo), presentation);
	}

	protected void adaptTextPresentation(TextPresentation presentation, int offset, int insertLength) {

		int yoursStart= offset;
		int yoursEnd=   offset + insertLength -1;
		yoursEnd= Math.max(yoursStart, yoursEnd);

		Iterator<?> e= presentation.getAllStyleRangeIterator();
		while (e.hasNext()) {

			StyleRange range= (StyleRange) e.next();

			int myStart= range.start;
			int myEnd=   range.start + range.length -1;
			myEnd= Math.max(myStart, myEnd);

			if (myEnd < yoursStart)
				continue;

			if (myStart < yoursStart)
				range.length += insertLength;
			else
				range.start += insertLength;
		}
	}

	private void append(StringBuffer buffer, String string, TextPresentation presentation) {

		int length= string.length();
		buffer.append(string);

		if (presentation != null)
			adaptTextPresentation(presentation, fCounter, length);

		fCounter += length;
	}

	private static String getIndent(String line) {
		int length= line.length();

		int i= 0;
		while (i < length && Character.isWhitespace(line.charAt(i))) ++i;

		return (i == length ? line : line.substring(0, i)) + " "; //$NON-NLS-1$
	}

	/*
	 * @see IHoverInformationPresenter#updatePresentation(Display display, String, TextPresentation, int, int)
	 */
	@Override
    public String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight) {
		return updatePresentation((Drawable)display, hoverInfo, presentation, maxWidth, maxHeight);
	}

	/*
	 * @see IHoverInformationPresenterExtension#updatePresentation(Drawable drawable, String, TextPresentation, int, int)
	 */
	@Override
    public String updatePresentation(Drawable drawable, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight) {

		if (hoverInfo == null)
			return null;

		GC gc= new GC(drawable);
		try {

			StringBuffer buffer= new StringBuffer();
			int maxNumberOfLines= Math.round(maxHeight / gc.getFontMetrics().getHeight());

			fCounter= 0;
			LineBreakingReader reader= new LineBreakingReader(createReader(hoverInfo, presentation), gc, maxWidth);

			boolean lastLineFormatted= false;
			String lastLineIndent= null;

			String line=reader.readLine();
			boolean lineFormatted= reader.isFormattedLine();
			boolean firstLineProcessed= false;

			while (line != null) {

				if (fEnforceUpperLineLimit && maxNumberOfLines <= 0)
					break;

				if (firstLineProcessed) {
					if (!lastLineFormatted)
						append(buffer, LINE_DELIM, null);
					else {
						append(buffer, LINE_DELIM, presentation);
						if (lastLineIndent != null)
							append(buffer, lastLineIndent, presentation);
					}
				}

				append(buffer, line, null);
				firstLineProcessed= true;

				lastLineFormatted= lineFormatted;
				if (!lineFormatted)
					lastLineIndent= null;
				else if (lastLineIndent == null)
					lastLineIndent= getIndent(line);

				line= reader.readLine();
				lineFormatted= reader.isFormattedLine();

				maxNumberOfLines--;
			}

			if (line != null) {
				append(buffer, LINE_DELIM, lineFormatted ? presentation : null);
				append(buffer, HTMLMessages.getString("HTMLTextPresenter.ellipse"), presentation); //$NON-NLS-1$
			}

			return trim(buffer, presentation);

		} catch (IOException e) {
			return null;

		} finally {
			gc.dispose();
		}
	}

	private static String trim(StringBuffer buffer, TextPresentation presentation) {

		int length= buffer.length();

		int end= length -1;
		while (end >= 0 && Character.isWhitespace(buffer.charAt(end)))
			-- end;

		if (end == -1)
			return ""; //$NON-NLS-1$

		if (end < length -1)
			buffer.delete(end + 1, length);
		else
			end= length;

		int start= 0;
		while (start < end && Character.isWhitespace(buffer.charAt(start)))
			++ start;

		buffer.delete(0, start);
		presentation.setResultWindow(new Region(start, buffer.length()));
		return buffer.toString();
	}
}

