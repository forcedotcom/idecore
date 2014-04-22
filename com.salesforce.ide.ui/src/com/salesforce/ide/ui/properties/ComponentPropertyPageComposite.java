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
package com.salesforce.ide.ui.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.ui.internal.composite.BaseComposite;

public class ComponentPropertyPageComposite extends BaseComposite {

    private Text txtName = null;
    private Text txtType = null;
    private Text txtEntityId = null;
    private Text txtPackageName = null;
    private Text txtManaged = null;
    private Text txtCreatedBy = null;
    private Text txtCreatedDate = null;
    private Text txtFileName = null;
    private Text txtModifiedBy = null;
    private Text txtModifiedDate = null;
    private Text txtNamespacePrefix = null;

    public ComponentPropertyPageComposite(Composite parent, int style) {
        super(parent, style);
        initialize();
    }

    private void initialize() {
        setLayout(new GridLayout());
        createPropertiesGroup();
    }

    private void createPropertiesGroup() {
        Group grpProperties = new Group(this, SWT.NONE);
        grpProperties.setText("Component Properties");
        grpProperties.setLayout(new GridLayout(4, false));
        grpProperties.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Color backgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

        // name
        Label lblName = new Label(grpProperties, SWT.NONE);
        lblName.setText("Name:");
        lblName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtName = new Text(grpProperties, SWT.NONE);
        txtName.setEditable(false);
        txtName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 0));
        txtName.setBackground(backgroundColor);

        // type
        Label lblType = new Label(grpProperties, SWT.NONE);
        lblType.setText("Type: ");
        lblType.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtType = new Text(grpProperties, SWT.NONE);
        txtType.setBackground(backgroundColor);
        txtType.setEditable(false);
        txtType.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 0));

        // entity id
        Label lblEntityId = new Label(grpProperties, SWT.NONE);
        lblEntityId.setText("Entity ID: ");
        lblEntityId.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtEntityId = new Text(grpProperties, SWT.NONE);
        txtEntityId.setBackground(backgroundColor);
        txtEntityId.setEditable(false);
        txtEntityId.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 0));

        // package name
        Label lblPackage = new Label(grpProperties, SWT.NONE);
        lblPackage.setText("Package Name:");
        lblPackage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtPackageName = new Text(grpProperties, SWT.NONE);
        txtPackageName.setEditable(false);
        txtPackageName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 0));
        txtPackageName.setBackground(backgroundColor);

        // state
        Label lblManaged = new Label(grpProperties, SWT.NONE);
        lblManaged.setText("State:");
        lblManaged.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtManaged = new Text(grpProperties, SWT.NONE);
        txtManaged.setBackground(backgroundColor);
        txtManaged.setEditable(false);
        txtManaged.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 0));

        // namespace
        Label lblNamespace = new Label(grpProperties, SWT.NONE);
        lblNamespace.setText("Namespace Prefix:");
        lblNamespace.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtNamespacePrefix = new Text(grpProperties, SWT.NONE);
        txtNamespacePrefix.setEditable(false);
        txtNamespacePrefix.setBackground(backgroundColor);
        txtNamespacePrefix.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 0));

        // filename
        Label lblFileName = new Label(grpProperties, SWT.NONE);
        lblFileName.setText("Filename:");
        lblFileName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtFileName = new Text(grpProperties, SWT.NONE);
        txtFileName.setEditable(false);
        txtFileName.setBackground(backgroundColor);
        txtFileName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 0));

        Label filler = new Label(grpProperties, SWT.NONE);
        filler.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 4, 0));

        // created by
        Label lblCreatedBy = new Label(grpProperties, SWT.NONE);
        lblCreatedBy.setText("Created By:");
        lblCreatedBy.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtCreatedBy = new Text(grpProperties, SWT.NONE);
        txtCreatedBy.setEditable(false);
        txtCreatedBy.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtCreatedBy.setBackground(backgroundColor);

        // created date
        Label lblCreated = new Label(grpProperties, SWT.NONE);
        lblCreated.setText("Create Date:");
        lblCreated.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtCreatedDate = new Text(grpProperties, SWT.NONE);
        txtCreatedDate.setBackground(backgroundColor);
        txtCreatedDate.setEditable(false);
        txtCreatedDate.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));

        // last modified
        Label lblModifiedBy = new Label(grpProperties, SWT.NONE);
        lblModifiedBy.setText("Modified By:");
        lblModifiedBy.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtModifiedBy = new Text(grpProperties, SWT.NONE);
        txtModifiedBy.setEditable(false);
        txtModifiedBy.setBackground(backgroundColor);
        txtModifiedBy.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));

        // last modified
        Label lblModified = new Label(grpProperties, SWT.NONE);
        lblModified.setText("Modified Date:");
        lblModified.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
        txtModifiedDate = new Text(grpProperties, SWT.NONE);
        txtModifiedDate.setEditable(false);
        txtModifiedDate.setBackground(backgroundColor);
        txtModifiedDate.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 0));
    }

    public String getFilenameText() {
        return txtFileName.getText();
    }

    public Text getTxtName() {
        return txtName;
    }

    public void setTxtName(Text txtName) {
        this.txtName = txtName;
    }

    public void setTxtName(String txtName) {
        this.txtName.setText(Utils.isNotEmpty(txtName) ? txtName : "n/a");
    }

    public Text getTxtType() {
        return txtType;
    }

    public void setTxtType(Text txtType) {
        this.txtType = txtType;
    }

    public void setTxtType(String txtType) {
        this.txtType.setText(Utils.isNotEmpty(txtType) ? txtType : "n/a");
    }

    public Text getTxtEntityId() {
        return txtEntityId;
    }

    public void setTxtEntityId(Text txtEntityId) {
        this.txtEntityId = txtEntityId;
    }

    public void setTxtEntityId(String txtEntityId) {
        this.txtEntityId.setText(Utils.isNotEmpty(txtEntityId) ? txtEntityId : "n/a");
    }

    public Text getTxtPackageName() {
        return txtPackageName;
    }

    public void setTxtPackageNameText(Text txtPackageName) {
        this.txtPackageName = txtPackageName;
    }

    public void setTxtPackageName(String txtPackageName) {
        this.txtPackageName.setText(Utils.isNotEmpty(txtPackageName) ? txtPackageName : "n/a");
    }

    public Text getTxtManaged() {
        return txtManaged;
    }

    public void setTxtManagedText(Text txtManaged) {
        this.txtManaged = txtManaged;
    }

    public void setTxtManaged(String txtManaged) {
        this.txtManaged.setText(Utils.isNotEmpty(txtManaged) ? txtManaged : "n/a");
    }

    public Text getTxtCreatedBy() {
        return txtCreatedBy;
    }

    public void setTxtCreatedBy(Text txtCreatedBy) {
        this.txtCreatedBy = txtCreatedBy;
    }

    public void setTxtCreatedBy(String txtCreatedBy) {
        this.txtCreatedBy.setText(Utils.isNotEmpty(txtCreatedBy) ? txtCreatedBy : "n/a");
    }

    public Text getTxtCreatedDate() {
        return txtCreatedDate;
    }

    public void setTxtCreatedDate(String txtCreatedDate) {
        this.txtCreatedDate.setText(Utils.isNotEmpty(txtCreatedDate) ? txtCreatedDate : "n/a");
    }

    public void setTxtCreatedDate(Text txtCreatedDate) {
        this.txtCreatedDate = txtCreatedDate;
    }

    public Text getTxtFileName() {
        return txtFileName;
    }

    public void setTxtFileName(Text txtFileName) {
        this.txtFileName = txtFileName;
    }

    public void setTxtFileName(String txtFileName) {
        this.txtFileName.setText(Utils.isNotEmpty(txtFileName) ? txtFileName : "n/a");
    }

    public Text getTxtModifiedBy() {
        return txtModifiedBy;
    }

    public void setTxtModifiedBy(Text txtModifiedBy) {
        this.txtModifiedBy = txtModifiedBy;
    }

    public void setTxtModifiedBy(String txtModifiedBy) {
        this.txtModifiedBy.setText(Utils.isNotEmpty(txtModifiedBy) ? txtModifiedBy : "n/a");
    }

    public Text getTxtModifiedDate() {
        return txtModifiedDate;
    }

    public void setTxtModifiedDate(Text txtModifiedDate) {
        this.txtModifiedDate = txtModifiedDate;
    }

    public void setTxtModifiedDate(String txtModifiedDate) {
        this.txtModifiedDate.setText(Utils.isNotEmpty(txtModifiedDate) ? txtModifiedDate : "n/a");
    }

    public Text getTxtNamespacePrefix() {
        return txtNamespacePrefix;
    }

    public void setTxtNamespacePrefix(Text txtNamespacePrefix) {
        this.txtNamespacePrefix = txtNamespacePrefix;
    }

    public void setTxtNamespacePrefix(String txtNamespacePrefix) {
        this.txtNamespacePrefix.setText(Utils.isNotEmpty(txtNamespacePrefix) ? txtNamespacePrefix : "n/a");
    }

    @Override
    public void validateUserInput() {

    }
}
