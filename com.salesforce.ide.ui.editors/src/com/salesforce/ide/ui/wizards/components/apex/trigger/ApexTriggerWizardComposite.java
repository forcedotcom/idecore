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
package com.salesforce.ide.ui.wizards.components.apex.trigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ApexTrigger;
import com.salesforce.ide.ui.wizards.components.apex.CodeWizardComposite;

public class ApexTriggerWizardComposite extends CodeWizardComposite {

    private Group grpTriggerOperations = null;
    private List<Button> checkboxOperationButtons = new ArrayList<>();
    private ApexTrigger component = null;
    private List<String> triggerOperationsOptions = null;

    //   C O N S T R U C T O R S
    public ApexTriggerWizardComposite(Composite parent, int style, String displayName, Set<String> supportedApiVesions) {
        super(parent, style, displayName);
        initialize(supportedApiVesions);
    }

    public ApexTriggerWizardComposite(Composite parent, int style, String displayName,
            Set<String> supportedApiVesions, List<String> triggerOperationsOptions) {
        super(parent, style, displayName);
        this.triggerOperationsOptions = triggerOperationsOptions;
        initialize(supportedApiVesions);
    }

    //   M E T H O D S
    public List<String> getTriggerOperationOptions() {
        return triggerOperationsOptions;
    }

    public void setTriggerOperationOptions(List<String> triggerOptions) {
        this.triggerOperationsOptions = triggerOptions;
    }

    public ApexTrigger getComponent() {
        return component;
    }

    public void setComponent(ApexTrigger component) {
        this.component = component;
    }

    protected void initialize(Set<String> supportedApiVesions) {
        setLayout(new GridLayout());
        setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Group grpProperties = createPropertiesGroup(this);
        createNameText(grpProperties);
        createApiVersionCombo(grpProperties, supportedApiVesions);
        createDetailsGroup(grpProperties);
        createOperationGroup();
        initSize();
    }

    private void createDetailsGroup(Group grpProperties) {
        createObjectTextAndRefreshButton(null, grpProperties);
    }

    private void createOperationGroup() {
        grpTriggerOperations = new Group(this, SWT.NONE);
        grpTriggerOperations.setText("Apex Trigger Operations");
        grpTriggerOperations.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        grpTriggerOperations.setLayout(new GridLayout(4, false));
        checkboxOperationButtons = new ArrayList<>();
        // dynamically create trigger operation checkboxes
        if (Utils.isNotEmpty(triggerOperationsOptions)) {
            for (String operation : triggerOperationsOptions) {
                createTriggerOperationButton(operation);
            }
        }
    }

    private Button createTriggerOperationButton(String text) {
        SelectionListener selectionListener = new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                getComponentWizardPage().validateUserInput();
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        };

        Button checkboxOperaionButton = new Button(grpTriggerOperations, SWT.CHECK);
        checkboxOperaionButton.setText(text);
        checkboxOperaionButton.addSelectionListener(selectionListener);
        checkboxOperationButtons.add(checkboxOperaionButton);
        return checkboxOperaionButton;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (null != grpTriggerOperations) {
            grpTriggerOperations.setEnabled(enabled);
        }
        List<Button> triggerOperationsOptions = getCheckboxOperationButtons();
        for (Button triggerOperationsOption : triggerOperationsOptions) {
            triggerOperationsOption.setEnabled(enabled);
        }
    }

    public List<Button> getCheckboxOperationButtons() {
        return checkboxOperationButtons;
    }

    public List<String> getSelectedTriggerOperations() {
        List<String> selectedTriggerOperations = new ArrayList<>();
        for (Button checkboxOperationButton : checkboxOperationButtons) {
            if (checkboxOperationButton.getSelection()) {
                selectedTriggerOperations.add(checkboxOperationButton.getText());
            }
        }
        return selectedTriggerOperations;
    }

    public void setCheckboxOperationButtons(List<Button> checkboxOperationButtons) {
        this.checkboxOperationButtons = checkboxOperationButtons;
    }
}
