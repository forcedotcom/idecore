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
package com.salesforce.ide.ui.internal.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.utils.UIMessages;

public abstract class BaseComposite extends Composite {
    Font font = null;
    
    //   C O N S T R U C T O R S
    public BaseComposite(Composite parent, int style) {
        super(parent, style);
        font = new Font(getDisplay(), "Arial", 8, SWT.BOLD);
        
        addDisposeListener(new DisposeListener()
        {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                font.dispose();
            }
        });
    }

    //   M E T H O D S
    protected final Label createValidateInfoLabel(Composite composite) {
        Label lblEndpoint = new Label(composite, SWT.NONE);
        lblEndpoint.setText(UIMessages.getString("BaseComposite.ValidateTabFocusOut.message"));
        lblEndpoint.setFont(font);
        return lblEndpoint;
    }

    protected void addValidateModifyListener(Text text) {
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateUserInput();
            }
        });
    }

    protected void addValidateModifyListener(Combo combo) {
        combo.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateUserInput();
            }
        });
    }

    protected void addValidateSelectionListener(Button btn) {
        btn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                validateUserInput();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
    }

    // Monitors user input and reports messages.
    public abstract void validateUserInput();

    // U T I L I T I E S
    protected String getText(Combo cbo) {
        return cbo != null && Utils.isNotEmpty(cbo.getText()) ? cbo.getText().trim() : null;
    }

    protected void selectComboContent(String str, Combo cbo) {
        if (cbo != null && cbo.getItemCount() > 0 && Utils.isNotEmpty(str)) {
            String[] cboStrings = cbo.getItems();
            for (int i = 0; i < cboStrings.length; i++) {
                if (Utils.isEqual(cboStrings[i], str)) {
                    cbo.select(i);
                    return;
                }
            }

            cbo.select(0);
        }
    }

    protected void setText(String str, Combo cbo) {
        if (cbo != null && Utils.isNotEmpty(str)) {
            cbo.setText(str.trim());
        }
    }

    protected void setText(String str, CCombo cbo) {
        if (cbo != null && Utils.isNotEmpty(str)) {
            cbo.setText(str.trim());
        }
    }

    protected String getText(CCombo cbo) {
        return cbo != null && Utils.isNotEmpty(cbo.getText()) ? cbo.getText().trim() : null;
    }

    protected String getText(Text txt) {
        return txt != null && Utils.isNotEmpty(txt.getText()) ? txt.getText().trim() : null;
    }

    protected void setText(String str, Text text) {
        if (text != null && Utils.isNotEmpty(str)) {
            text.setText(str.trim());
        }
    }

    protected boolean isEmpty(String str) {
        return Utils.isEmpty(str);
    }
}
