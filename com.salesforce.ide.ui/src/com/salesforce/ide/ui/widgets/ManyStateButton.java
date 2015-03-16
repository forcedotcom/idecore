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
package com.salesforce.ide.ui.widgets;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class ManyStateButton extends Canvas {
    List<Image> images;
    protected String text;
    PaintListener paintListener;
    int state = 0;

    public ManyStateButton(Composite parent, int style) {
        super(parent, style | SWT.INHERIT_DEFAULT);
        init();
    }

    protected void init() {
        paintListener = new ManyStatePaintListener();
        addPaintListener(paintListener);
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    private class ManyStatePaintListener implements PaintListener {
        @Override
        public void paintControl(PaintEvent event) {
            int imageWidth = 0;

            if (images != null && images.size() > state) {
                Image image = images.get(state);
                event.gc.drawImage(image, 0, 0);
                imageWidth = image.getBounds().width + 5;
            }

            if (text != null && text.length() > 0) {
                event.gc.drawText(text, imageWidth, 0);
            }
        }
    }
}
