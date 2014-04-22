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
package com.salesforce.ide.ui.editors.properysheets.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A generic widget that shows a checkbox
 * 
 * @author nchen
 *
 */
public class CheckBoxWidget {
    private boolean value;
    private String label;
    private Button checkBox;
    private FormToolkit toolkit;

    public CheckBoxWidget(FormToolkit toolkit, String label, boolean initialValue) {
        this.toolkit = toolkit;
        this.label = label;
        this.value = initialValue;
    }

    /*
     * Assumes that the parent composite is a two-column GridLayout
     */
    public void addTo(Composite composite) {
        checkBox = toolkit.createButton(composite, label, SWT.CHECK);

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;

        checkBox.setLayoutData(gridData);
    }

    public boolean getValue() {
        return checkBox.getSelection();
    }

    public void setValue(boolean value) {
        this.value = value;
        checkBox.setSelection(this.value);
    }

    public Button getCheckBox() {
        return checkBox;
    }

}
