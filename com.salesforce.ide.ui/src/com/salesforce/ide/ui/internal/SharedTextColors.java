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
package com.salesforce.ide.ui.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 *
 *
 * @author cwall
 */
public class SharedTextColors implements ISharedTextColors {

    /** The display table. */
    private Map<Display, Map<RGB, Color>> fDisplayTable;

    /** Creates an returns a shared color manager. */
    public SharedTextColors() {
        super();
    }

    /*
     * @see ISharedTextColors#getColor(RGB)
     */
    @Override
    public Color getColor(RGB rgb) {
        if (rgb == null) {
            return null;
        }

        if (fDisplayTable == null) {
            fDisplayTable = new HashMap<>(2);
        }

        Display display = Display.getCurrent();

        Map<RGB, Color> colorTable = fDisplayTable.get(display);
        if (colorTable == null) {
            colorTable = new HashMap<>(10);
            fDisplayTable.put(display, colorTable);
        }

        Color color = colorTable.get(rgb);
        if (color == null) {
            color = new Color(display, rgb);
            colorTable.put(rgb, color);
        }

        return color;
    }

    /*
     * @see ISharedTextColors#dispose()
     */
    @Override
    public void dispose() {
        if (fDisplayTable != null) {
            Iterator<Map<RGB, Color>> j = fDisplayTable.values().iterator();
            while (j.hasNext()) {
                Iterator<Color> i = j.next().values().iterator();
                while (i.hasNext()) {
                    (i.next()).dispose();
                }
            }
        }
    }

}
