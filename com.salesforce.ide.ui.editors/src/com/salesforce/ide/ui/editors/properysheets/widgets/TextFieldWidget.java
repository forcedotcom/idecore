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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A generic text form widget. Contains a label component and a text field component.
 * 
 * @author nchen
 * 
 */
public class TextFieldWidget {
    private String value;
    private String label;
    private Text textField;
    private FormToolkit toolkit;

    public TextFieldWidget(FormToolkit toolkit, String label, String initialValue) {
        this.toolkit = toolkit;
        this.label = label;
        this.value = initialValue;
    }

    /*
     * Assumes that the parent composite is a two-column GridLayout
     */
    public void addTo(Composite composite) {
        toolkit.createLabel(composite, label);
        textField = toolkit.createText(composite, value.toString(), SWT.BORDER);
        textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    public String getValue() {
        return textField.getText();
    }

    public void setValue(String value) {
        if (value != null) {
            this.value = value;
            textField.setText(value.toString());
        }
    }

    public Text getTextField() {
        return textField;
    }
}
