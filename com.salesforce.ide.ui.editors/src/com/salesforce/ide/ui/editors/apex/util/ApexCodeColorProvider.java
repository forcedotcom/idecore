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
package com.salesforce.ide.ui.editors.apex.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Manager for colors used in the Apex Code editor
 */
public class ApexCodeColorProvider {

    public static final RGB MULTI_LINE_COMMENT = new RGB(63, 95, 191);
    public static final RGB SINGLE_LINE_COMMENT = new RGB(128, 128, 0);
    public static final RGB KEYWORD = new RGB(127, 0, 85);
    public static final RGB TYPE = new RGB(0, 0, 128);
    public static final RGB STRING = new RGB(0, 128, 0);
    public static final RGB DEFAULT = new RGB(0, 0, 0);
    public static final RGB JAVADOC_KEYWORD = new RGB(0, 128, 0);
    public static final RGB JAVADOC_TAG = new RGB(128, 128, 128);
    public static final RGB JAVADOC_LINK = new RGB(128, 128, 128);
    public static final RGB JAVADOC_DEFAULT = new RGB(63, 95, 191);
    public static final RGB ANNOTATION = new RGB(0, 128, 128);
    public static final RGB APEX_SPECIFIC = new RGB(142, 35, 35);
    public static final RGB SOBJECTS_SPECIFIC = new RGB(34, 24, 230);
    public static final RGB TRIGGER_OPERATIONS = new RGB(142, 35, 35);
    private static Map<RGB, Color> colorTable = new HashMap<>(10);

    public ApexCodeColorProvider() {
        super();
    }

    /**
     * Release all of the color resources held onto by the receiver.
     */
    public void dispose() {
        Iterator<Color> e = colorTable.values().iterator();
        while (e.hasNext()) {
            (e.next()).dispose();
        }
    }

    /**
     * Return the color that is stored in the color table under the given RGB value.
     * 
     * @param rgb
     *            the RGB value
     * @return the color stored in the color table for the given RGB value
     */
    public Color getColor(RGB rgb) {
        Color color = colorTable.get(rgb);
        if (color == null) {
            color = new Color(Display.getCurrent(), rgb);
            colorTable.put(rgb, color);
        }
        return color;
    }
}
