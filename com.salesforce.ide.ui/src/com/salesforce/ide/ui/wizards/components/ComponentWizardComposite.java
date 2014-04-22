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

    protected String componentTypeDisplayName = null;
    protected Text txtName = null;
    protected Text txtLabel = null;
    protected Text txtPluralLabel = null;
    @Deprecated
    protected Combo cmbPackageNames = null;
    protected Combo cmbProjectNames = null;
    protected Combo cmbObjects = null;
    protected Combo cmbTemplateNames = null;
    protected Button btnRefreshObjects = null;
    protected IComponentWizardPage componentWizardPage = null;

    //   C O N S T R U C T O R
    public ComponentWizardComposite(Composite parent, int style, String componentTypeDisplayName) {
        super(parent, style);
        this.componentTypeDisplayName = componentTypeDisplayName;
    }

    //   M E T H O D S
    public void setComponentWizardPage(IComponentWizardPage componentWizardPage) {
        this.componentWizardPage = componentWizardPage;
    }

    public IComponentWizardPage getComponentWizardPage() {
        return componentWizardPage;
    }

    public String getComponentDisplayName() {
        return componentTypeDisplayName;
    }

    public void setComponentDisplayName(String componentTypeDisplayName) {
        this.componentTypeDisplayName = componentTypeDisplayName;
    }

    @Deprecated
    protected final void createPackageNamesGrp(Composite composite) {
        Group grpPackage = new Group(composite, SWT.NONE);
        grpPackage.setText("Package");
        grpPackage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        grpPackage.setLayout(new GridLayout(5, false));
        Label lblEndpoint = new Label(grpPackage, SWT.NONE);
        lblEndpoint.setText("Name:");
        lblEndpoint.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        cmbPackageNames = new Combo(grpPackage, SWT.BORDER | SWT.READ_ONLY);
        cmbPackageNames.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 4, 0));
        cmbPackageNames.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                getComponentWizardPage().validateUserInput();
            }

            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
    }

    protected Group createPropertiesGroup(Composite composite) {
        Group grpProperties = new Group(composite, SWT.NONE);
        grpProperties.setText(componentTypeDisplayName + " Properties");
        grpProperties.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        grpProperties.setLayout(new GridLayout(5, false));
        return grpProperties;
    }

    protected GridLayout getPropertiesGridLayout() {
        return new GridLayout(5, true);
    }

    protected void createNameText(Group containingGroup) {
        createNameGroup(null, containingGroup);
    }

    protected void createNameGroup(Composite composite, Group containingGroup) {
        if (composite == null && containingGroup == null) {
            throw new IllegalArgumentException("Composite and group cannot both be null");
        }

        Label lblName = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        lblName.setText("Name:");
        lblName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtName = new Text(containingGroup != null ? containingGroup : composite, SWT.BORDER);
        txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 0));
        txtName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getComponentWizardPage().setComponentNameChanged(true);
                getComponentWizardPage().validateUserInput();
            }
        });
        Label filler31 = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        filler31.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
    }

    protected void createNameText(Composite composite, Group containingGroup) {
        if (composite == null && containingGroup == null) {
            throw new IllegalArgumentException("Composite and group cannot both be null");
        }

        Label lblName = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        lblName.setText("Name:");
        lblName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtName = new Text(containingGroup != null ? containingGroup : composite, SWT.BORDER);
        txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 0));
        txtName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getComponentWizardPage().setComponentNameChanged(true);
                getComponentWizardPage().validateUserInput();
            }
        });
        Label filler31 = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        filler31.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
    }

    protected void createLabelText(Composite composite, Group containingGroup) {
        if (composite == null && containingGroup == null) {
            throw new IllegalArgumentException("Composite and group cannot both be null");
        }

        Label lblLabel = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        lblLabel.setText("Label:");
        lblLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtLabel = new Text(containingGroup != null ? containingGroup : composite, SWT.BORDER);
        txtLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 0));
        txtLabel.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getComponentWizardPage().setComponentNameChanged(true);
                getComponentWizardPage().validateUserInput();
            }
        });
        @SuppressWarnings("unused")
        Label filler31 = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
    }

    protected void createLabelAndNameText(Composite composite, Group containingGroup) {
        createLabelAndNameText(composite, containingGroup, false);
    }

    protected void createLabelAndNameText(Composite composite, Group containingGroup, boolean includePlural) {
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
            public void focusGained(FocusEvent event) { /* not implemented */}

            public void focusLost(FocusEvent event) {
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
            public void modifyText(ModifyEvent e) {
                getComponentWizardPage().validateUserInput();
                getComponentWizardPage().setComponentNameChanged(true);
            }
        });

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
                public void modifyText(ModifyEvent e) {
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
        Label lblName = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        lblName.setText("Name:");
        lblName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtName = new Text(containingGroup != null ? containingGroup : composite, SWT.BORDER);
        txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 0));
        txtName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getComponentWizardPage().setComponentNameChanged(true);
                getComponentWizardPage().validateUserInput();
            }
        });

        Label filler = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        filler.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
    }

    protected void createObjectTextAndRefreshButton(Composite composite, Group containingGroup) {
        if (composite == null && containingGroup == null) {
            throw new IllegalArgumentException("Composite and group cannot both be null");
        }

        Label lblObject = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        lblObject.setText("Object:");
        lblObject.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        cmbObjects = new Combo(containingGroup != null ? containingGroup : composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        cmbObjects.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 0));
        cmbObjects.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getComponentWizardPage().validateUserInput();
            }
        });

        btnRefreshObjects = new Button(containingGroup != null ? containingGroup : composite, SWT.NONE);
        btnRefreshObjects.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 1, 0));
        btnRefreshObjects.setText("Refresh Objects");
        btnRefreshObjects.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ((ComponentWizardPage) getComponentWizardPage()).refreshObjects();
            }

            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });

        Label filler31 = new Label(containingGroup != null ? containingGroup : composite, SWT.NONE);
        filler31.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 0));
    }

    protected final void createTemplateCombo(Group containingGroup, Set<String> templateNames) {
        Label lblApiVersion = new Label(containingGroup, SWT.NONE);
        lblApiVersion.setText("Template: ");
        lblApiVersion.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        cmbTemplateNames = new Combo(containingGroup, SWT.BORDER | SWT.READ_ONLY);
        cmbTemplateNames.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 4, 0));
        setTemplateComboValues(templateNames);
    }

    protected void setTemplateComboValues(Set<String> templateNames) {
        if (Utils.isNotEmpty(templateNames)) {
            for (String templateName : templateNames) {
                cmbTemplateNames.add(templateName);
            }
            cmbTemplateNames.select(templateNames.size() - 1);
        }
    }

    public void disableAllControls() {
        disableComponentNameFields();

        if (cmbPackageNames != null) {
            cmbPackageNames.setEnabled(false);
        }

        if (cmbProjectNames != null) {
            cmbProjectNames.setEnabled(false);
        }

        if (cmbObjects != null) {
            cmbObjects.setEnabled(false);
        }

        if (btnRefreshObjects != null) {
            btnRefreshObjects.setEnabled(false);
        }

        if (cmbTemplateNames != null) {
            cmbTemplateNames.setEnabled(false);
        }

        disableControls();
    }

    public abstract void disableControls();

    @Override
    public void validateUserInput() {
    }

    protected void initSize() {
        setSize(new Point(490, 174));
    }

    public Text getTxtName() {
        return txtName;
    }

    public String getComponentName() {
        return getNameString();
    }

    public String getNameString() {
        return getText(txtName);
    }

    public void setName(String name) {
        setText(name, txtName);
    }

    public boolean isNameNotEmpty() {
        return (getTxtName() != null && !Utils.isEmpty(getNameString()));
    }

    public Text getTxtLabel() {
        return txtLabel;
    }

    public String getLabelString() {
        return getText(txtLabel);
    }

    public boolean isLabelNotEmpty() {
        return (getTxtLabel() != null && !Utils.isEmpty(getLabelString()));
    }

    public void setLabel(String label) {
        setText(label, txtLabel);
    }

    public Text getTxtPluralLabel() {
        return txtPluralLabel;
    }

    public String getPluralLabelString() {
        return getText(txtPluralLabel);
    }

    public boolean isPluralLabelNotEmpty() {
        return (getTxtPluralLabel() != null && !Utils.isEmpty(getPluralLabelString()));
    }

    public void setPluralLabel(String label) {
        setText(label, txtPluralLabel);
    }

    public void disableComponentNameFields() {
        txtName.setEnabled(false);

        if (txtLabel != null) {
            txtLabel.setEnabled(false);
        }

        if (txtPluralLabel != null) {
            txtPluralLabel.setEnabled(false);
        }
    }

    @Deprecated
    public void disableComponentPackageNameField() {
        cmbPackageNames.setEnabled(false);
    }

    @Deprecated
    public String getPackageName() {
        return Utils.isEmpty(getText(cmbPackageNames)) ? Constants.EMPTY_STRING : getText(cmbPackageNames);
    }

    @Deprecated
    public Combo getCmbPackageName() {
        return cmbPackageNames;
    }

    public void setPackageName(String packageName) {
        selectComboContent(packageName, cmbPackageNames);
    }

    public String getObject() {
        return cmbObjects.getText();
    }

    public void setTriggerObject(String object) {
        cmbObjects.setText(object);
    }

    public Combo getCmbObjects() {
        return cmbObjects;
    }

    public String getObjectName() {
        return Utils.isEmpty(getText(cmbObjects)) ? Constants.EMPTY_STRING : getText(cmbObjects);
    }

    public Combo getCmbTemplateNames() {
        return cmbTemplateNames;
    }

    public String getCmbTemplateNamesName() {
        return Utils.isEmpty(getText(cmbTemplateNames)) ? Constants.EMPTY_STRING : getText(cmbTemplateNames);
    }
}
