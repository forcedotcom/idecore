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
package com.salesforce.ide.ui.editors.apex.propertysheets;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

import com.salesforce.ide.ui.editors.internal.BaseComponentMultiPageEditorPart;
import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;
import com.salesforce.ide.ui.editors.properysheets.MetadataFormPage;
import com.salesforce.ide.ui.editors.properysheets.widgets.PackageVersionWidget;
import com.salesforce.ide.ui.editors.properysheets.widgets.TextFieldWidget;

/**
 * <p>
 * This page is a "beautification" of the information available on the corresponding -meta.xml file for metadata types
 * that have a corresponding metadata file.
 * </p>
 * <p>
 * It is similar to the PDE editor that you have in Eclipse when you edit your plugin.xml, build.properties or
 * MANIFEST.MFs or MANIFEST.MF. It supports two-way editing, whenever possible.
 * </p>
 * <p>
 * This is the base class for the property sheet for Apex Class and Trigger since they share the same metadata
 * attributes but they are of different types. It's not easy to change the type hierarcy since that is generated from
 * JAXB from the WSDL/XSD. Thus, we handle it locally here through subclassing.
 * </p>
 * 
 * @author nchen
 * 
 */
public abstract class ApexPropertySheet extends MetadataFormPage {
    protected TextFieldWidget apiText;
    protected PackageVersionWidget packageVersionTable;
    protected static Pattern apiVersion = Pattern.compile("(\\d+)(.\\d+)?"); //$NON-NLS-1$

    public ApexPropertySheet(BaseComponentMultiPageEditorPart multiPageEditor) {
        super(multiPageEditor);
    }

    @Override
    protected void setFormTitle() {
        String name = multiPageEditor.getEditorInput().getName();
        form.setText(EditorMessages.getString("ApexEditor.PropertiesTab.title", new Object[] { name })); //$NON-NLS-1$
    }

    protected void setUpLayout() {
        ColumnLayout layout = new ColumnLayout();
        layout.topMargin = 0;
        layout.bottomMargin = 5;
        layout.leftMargin = 10;
        layout.rightMargin = 10;
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 10;
        layout.maxNumColumns = 2;
        layout.minNumColumns = 1;
        form.getBody().setLayout(layout);
    }

    protected void createPackageVersions() {
        Section packageVersions = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        packageVersions.setText(EditorMessages.getString("ApexMetadataFormPage.PackageVersionsSection")); //$NON-NLS-1$
        packageVersions.setDescription(EditorMessages
                .getString("ApexMetadataFormPage.PackageVersionsSection.Description")); //$NON-NLS-1$

        Composite sectionClient = toolkit.createComposite(packageVersions);
        sectionClient.setLayout(new GridLayout(2, false));

        packageVersionTable = new PackageVersionWidget();
        packageVersionTable.addTo(sectionClient);

        packageVersions.setClient(sectionClient);
    }

    protected Double retrieveApiVersionIfPossible() {
        String value = apiText.getValue();
        Matcher matcher = apiVersion.matcher(value);
        if (matcher.matches()) {
            try (final Scanner scanner = new Scanner(value)) {
                int apiVersion = scanner.useDelimiter("\\.").nextInt(); //$NON-NLS-1$
                return (double) apiVersion;
            }
        }
        return null;
    }

}
