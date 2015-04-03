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
package com.salesforce.ide.ui.wizards.components.generic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.wizards.components.ComponentWizardComposite;

public class GenericComponentWizardComposite extends ComponentWizardComposite {

    protected Label lblTemplateOverview = null;

    public GenericComponentWizardComposite(Composite parent, int style, String componentTypeDisplayName) {
        super(parent, style, componentTypeDisplayName);
    }

    public Label getLblTemplateOverview() {
        return lblTemplateOverview;
    }

    public void setLblTemplateOverview(Label lblTemplateOverview) {
        this.lblTemplateOverview = lblTemplateOverview;
    }

    protected final void initialize() {
        setLayout(new GridLayout(2, false));
        setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        initSize();
        createTemplateLabel(this);
        new Label(this, SWT.NONE); // filler

        initializeControls();
    }

    protected void initializeControls() {
        Group grpProperties = createPropertiesGroup(this);
        createLabelAndNameText(null, grpProperties);
    }

    protected void createTemplateLabel(final Composite parent) {
        lblTemplateOverview = new Label(parent, SWT.WRAP);
        lblTemplateOverview.setText(getTemplateComponentMessage());
        GridData gridData = new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1);
        Rectangle rect = parent.getClientArea();
        gridData.widthHint = rect.width;
        lblTemplateOverview.setLayoutData(gridData);

        parent.addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Rectangle rect = parent.getClientArea();
                GridData gridData = (GridData) lblTemplateOverview.getLayoutData();
                gridData.widthHint = rect.width;
                parent.layout(true);
            }
        });
    }

    protected String getTemplateComponentMessage() {
        String[] values =
                new String[] { getComponentDisplayName(), getComponentDisplayName(), getComponentDisplayName() };
        return UIMessages.getString("NewComponent.GenericComponentWizardOverview.message", values);
    }

}
