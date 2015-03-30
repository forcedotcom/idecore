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

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

import com.salesforce.ide.api.metadata.types.ApexCodeUnitStatus;
import com.salesforce.ide.ui.editors.internal.BaseComponentMultiPageEditorPart;
import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;
import com.salesforce.ide.ui.editors.properysheets.widgets.ComboWidget;
import com.salesforce.ide.ui.editors.properysheets.widgets.PackageVersionWidget;

/**
 * Holds the common set up for Apex Class and Triggers.
 * 
 * @author nchen
 *
 */
public abstract class ApexClassAndTriggerPropertySheet extends ApexPropertySheet {

    protected ComboWidget<ApexCodeUnitStatus> statusCombo;

    public ApexClassAndTriggerPropertySheet(BaseComponentMultiPageEditorPart multiPageEditor) {
        super(multiPageEditor);
    }

    @Override
    protected void createContent() {
        setUpLayout();
        createGeneralInformation();
        createPackageVersions();
    }

    abstract void createGeneralInformation();

    @Override
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
}
