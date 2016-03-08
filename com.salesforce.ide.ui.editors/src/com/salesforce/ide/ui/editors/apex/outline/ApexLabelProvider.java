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
package com.salesforce.ide.ui.editors.apex.outline;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.salesforce.ide.ui.editors.apex.outline.icon.OutlineViewIconProvider;
import com.salesforce.ide.ui.editors.apex.outline.text.OutlineViewElementTextProvider;

/**
 * Provides the labeling for the different elements in the outline view.
 * 
 * @author nchen
 *         
 */
public class ApexLabelProvider extends LabelProvider {
    public static final String ELLIPSIS_SUFFIX = "{...}";
    public static final String INNERCLASS_SUFFIX = "() " + ELLIPSIS_SUFFIX;
    
    private static final OutlineViewIconProvider iconProvider = new OutlineViewIconProvider();
    private OutlineViewDispatcher<Image> fIconProvider = new OutlineViewDispatcher<>(iconProvider);
    
    private static final OutlineViewElementTextProvider textProvider = new OutlineViewElementTextProvider();
    private OutlineViewDispatcher<String> fTextProvider = new OutlineViewDispatcher<>(textProvider);
    
    @Override
    public Image getImage(Object element) {
        if (element != null) {
            return fIconProvider.dispatch(element);
        }
        return super.getImage(element); // Fallback
    }
    
    @Override
    public String getText(Object element) {
        if (element != null) {
            return fTextProvider.dispatch(element);
        }
        return super.getText(element); // Fallback
    }
    
}
