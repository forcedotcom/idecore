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
package com.salesforce.ide.ui.editors.apex.outline;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * Content outline page that represents the contents of the current editor
 * 
 * @author nchen
 * 
 */
public class ApexContentOutlinePage extends ContentOutlinePage {

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.views.contentoutline.ContentOutlinePage#getTreeStyle()
     * There is no point to allow multiple selection in the outline view
     * Remove that behavior that was specified in parent class
     */
    @Override
    protected int getTreeStyle() {
        return SWT.H_SCROLL | SWT.V_SCROLL;
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        TreeViewer viewer = getTreeViewer();
        viewer.setContentProvider(new ApexOutlineContentProvider());
        viewer.setLabelProvider(new ApexLabelProvider());
    }

    public void update(OutlineViewVisitor outlineViewVisitor) {
        TreeViewer viewer = getTreeViewer();

        if (viewer != null) {
            Control control = viewer.getControl();
            if (control != null && !control.isDisposed()) {
                control.setRedraw(false);
                viewer.setInput(outlineViewVisitor);
                viewer.expandAll();
                control.setRedraw(true);
            }
        }
    }

    @Override
    public TreeViewer getTreeViewer() {
    	return super.getTreeViewer();
    }
}
