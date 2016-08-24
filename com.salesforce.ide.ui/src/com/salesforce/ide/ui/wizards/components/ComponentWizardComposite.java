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
package com.salesforce.ide.ui.wizards.components;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.composite.BaseComposite;
import com.salesforce.ide.ui.internal.utils.UIMessages;

/**
 * 
 * 
 * @author cwall
 */
public abstract class ComponentWizardComposite extends BaseComposite implements IComponentWizardComposite {

    private String componentTypeDisplayName = null;
    private Text txtName = null;
    private Text txtLabel = null;
    private Text txtPluralLabel = null;
    private Combo cmbObjects = null;
    private Combo cmbTemplateNames = null;
    private Button btnRefreshObjects = null;
    private IComponentWizardPage componentWizardPage = null;

    //   C O N S T R U C T O R
    public ComponentWizardComposite(Composite parent, int style, String componentTypeDisplayName) {
        super(parent, style);
        this.componentTypeDisplayName = componentTypeDisplayName;
    }

    //   M E T H O D S
    @Override
    public final void setComponentWizardPage(IComponentWizardPage componentWizardPage) {
        this.componentWizardPage = componentWizardPage;
    }

    public final IComponentWizardPage getComponentWizardPage() {
        return componentWizardPage;
    }

    public final String getComponentDisplayName() {
        return componentTypeDisplayName;
    }

    public final void setComponentDisplayName(String componentTypeDisplayName) {
        this.componentTypeDisplayName = componentTypeDisplayName;
    }

    protected final Group createPropertiesGroup(Composite composite) {
        Group grpProperties = new Group(composite, SWT.NONE);
        grpProperties.setText(componentTypeDisplayName + " Properties");
        grpProperties.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        grpProperties.setLayout(new GridLayout(5, false));
        return grpProperties;
    }

    protected final void createNameText(Group containingGroup) {
        createNameGroup(null, containingGroup);
    }

    protected final void createNameGroup(Composite composite, Group containingGroup) {
        if (composite == null && containingGroup == null) {
            throw new IllegalArgumentException("Composite and group cannot both be null");
        }

        Label lblName = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        lblName.setText("Name:");
        lblName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtName = new Text(containingGroup != null ? containingGroup : composite, SWT.BORDER);
        txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 0));
        txtName.addModifyListener(new ModifyListener() {
            @Override
            public final void modifyText(ModifyEvent e) {
                getComponentWizardPage().setComponentNameChanged(true);
                getComponentWizardPage().validateUserInput();
            }
        });
        Label filler31 = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        filler31.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
    }

    protected final void createNameText(Composite composite, Group containingGroup) {
        if (composite == null && containingGroup == null) {
            throw new IllegalArgumentException("Composite and group cannot both be null");
        }

        Label lblName = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        lblName.setText("Name:");
        lblName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtName = new Text(containingGroup != null ? containingGroup : composite, SWT.BORDER);
        txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 0));
        txtName.addModifyListener(new ModifyListener() {
            @Override
            public final void modifyText(ModifyEvent e) {
                getComponentWizardPage().setComponentNameChanged(true);
                getComponentWizardPage().validateUserInput();
            }
        });

        Label filler = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        filler.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
    }

    protected final void createLabelText(Composite composite, Group containingGroup) {
    	if (composite == null && containingGroup == null) {
            throw new IllegalArgumentException("Composite and group cannot both be null");
        }

        Label lblLabel = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        // label
        lblLabel.setText("Label:");
        lblLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtLabel = new Text(containingGroup != null ? containingGroup : composite, SWT.BORDER);
        txtLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 0));
        txtLabel.addFocusListener(new FocusListener() {
            @Override
            public final void focusGained(FocusEvent event) { /* not implemented */}

            @Override
            public final void focusLost(FocusEvent event) {
                if (isLabelNotEmpty() && !isNameNotEmpty()) {
                    String agumentedText = Utils.generateNameFromLabel(getLabelString());
                    getTxtName().setText(agumentedText);

                    if (txtPluralLabel != null && Utils.isEmpty(txtPluralLabel.getText())) {
                        String plural = Utils.getPlural(getLabelString());
                        txtPluralLabel.setText(plural);
                    }
                }
            }
        });
        txtLabel.addModifyListener(new ModifyListener() {
            @Override
            public final void modifyText(ModifyEvent e) {
                getComponentWizardPage().validateUserInput();
                getComponentWizardPage().setComponentNameChanged(true);
            }
        });
    }

    protected final void createLabelAndNameText(Composite composite, Group containingGroup) {
        createLabelAndNameText(composite, containingGroup, false);
    }

    protected final void createLabelAndNameText(Composite composite, Group containingGroup, boolean includePlural) {
        createLabelText(composite, containingGroup);

        Label filler1 = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        filler1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));

        // includes plural text box
        if (includePlural) {
            Label lblPluralLabel = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
            lblPluralLabel.setText("Plural Label:");
            lblPluralLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
            txtPluralLabel = new Text(containingGroup != null ? containingGroup : composite, SWT.BORDER);
            txtPluralLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 0));
            txtPluralLabel.addModifyListener(new ModifyListener() {
                @Override
                public final void modifyText(ModifyEvent e) {
                    getComponentWizardPage().validateUserInput();
                }
            });

            Label filler121 = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
            filler121.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));

            Label filler11 = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
            filler11.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));

            Label lblPluralLabelNotRequired =
                    new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
            lblPluralLabelNotRequired.setText(UIMessages
                    .getString("NewCustomObjectComponent.PluralNotRequired.message"));
            lblPluralLabelNotRequired.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 4, 0));
        }

        // this is the file name
        createNameText(composite, containingGroup);
    }

    protected final void createObjectTextAndRefreshButton(Composite composite, Group containingGroup) {
        if (composite == null && containingGroup == null) {
            throw new IllegalArgumentException("Composite and group cannot both be null");
        }

        final Composite parent = containingGroup != null ? containingGroup : composite;
        Label lblObject = new Label(parent, SWT.NONE);
        lblObject.setText("Object:");
        lblObject.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        cmbObjects = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        cmbObjects.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 0));
        cmbObjects.addModifyListener(new ModifyListener() {
            @Override
            public final void modifyText(ModifyEvent e) {
                getComponentWizardPage().validateUserInput();
            }
        });

        btnRefreshObjects = new Button(parent, SWT.NONE);
        btnRefreshObjects.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        btnRefreshObjects.setText("Refresh Objects");
        btnRefreshObjects.addSelectionListener(new SelectionListener() {
            @Override
            public final void widgetDefaultSelected(SelectionEvent e) {
                ((ComponentWizardPage) getComponentWizardPage()).refreshObjects();
            }

            @Override
            public final void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
    }

    protected final void createTemplateCombo(Group containingGroup, Set<String> templateNames) {
        Label lblApiVersion = new Label(containingGroup, SWT.NONE);
        lblApiVersion.setText("Template: ");
        lblApiVersion.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        cmbTemplateNames = new Combo(containingGroup, SWT.BORDER | SWT.READ_ONLY);
        cmbTemplateNames.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 4, 0));
        setTemplateComboValues(templateNames);
    }

    protected final void setTemplateComboValues(Set<String> templateNames) {
        if (Utils.isNotEmpty(templateNames)) {
            for (String templateName : templateNames) {
                cmbTemplateNames.add(templateName);
            }
            cmbTemplateNames.select(templateNames.size() - 1);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {

        if (cmbObjects != null) {
            cmbObjects.setEnabled(enabled);
        }

        if (btnRefreshObjects != null) {
            btnRefreshObjects.setEnabled(enabled);
        }

        if (cmbTemplateNames != null) {
            cmbTemplateNames.setEnabled(enabled);
        }

        if (txtName != null) {
            txtName.setEnabled(enabled);
        }

        if (txtLabel != null) {
            txtLabel.setEnabled(enabled);
        }

        if (txtPluralLabel != null) {
            txtPluralLabel.setEnabled(enabled);
        }
    }

    @Override
    public final void validateUserInput() {}

    protected final void initSize() {
        setSize(new Point(490, 174));
    }

    @Override
    public final Text getTxtName() {
        return txtName;
    }

    @Override
    public final String getComponentName() {
        return getNameString();
    }

    @Override
    public final String getNameString() {
        return getText(txtName);
    }

    public final void setName(String name) {
        setText(name, txtName);
    }

    public final boolean isNameNotEmpty() {
        return (getTxtName() != null && !Utils.isEmpty(getNameString()));
    }

    public final Text getTxtLabel() {
        return txtLabel;
    }

    @Override
    public final String getLabelString() {
        return getText(txtLabel);
    }

    public final boolean isLabelNotEmpty() {
        return (getTxtLabel() != null && !Utils.isEmpty(getLabelString()));
    }

    public final void setLabel(String label) {
        setText(label, txtLabel);
    }

    public final Text getTxtPluralLabel() {
        return txtPluralLabel;
    }

    public final String getPluralLabelString() {
        return getText(txtPluralLabel);
    }

    public final boolean isPluralLabelNotEmpty() {
        return (getTxtPluralLabel() != null && !Utils.isEmpty(getPluralLabelString()));
    }

    public final void setPluralLabel(String label) {
        setText(label, txtPluralLabel);
    }

    public final String getObject() {
        return cmbObjects.getText();
    }

    public final void setTriggerObject(String object) {
        cmbObjects.setText(object);
    }

    public final Combo getCmbObjects() {
        return cmbObjects;
    }

    public final String getObjectName() {
        return Utils.isEmpty(getText(cmbObjects)) ? Constants.EMPTY_STRING : getText(cmbObjects);
    }

    @Override
    public final Combo getCmbTemplateNames() {
        return cmbTemplateNames;
    }

    public final String getCmbTemplateNamesName() {
        return Utils.isEmpty(getText(cmbTemplateNames)) ? Constants.EMPTY_STRING : getText(cmbTemplateNames);
    }
}
