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

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

import com.salesforce.ide.api.metadata.types.ApexCodeUnitStatus;
import com.salesforce.ide.api.metadata.types.ApexPage;
import com.salesforce.ide.ui.editors.internal.BaseComponentMultiPageEditorPart;
import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;
import com.salesforce.ide.ui.editors.properysheets.widgets.CheckBoxWidget;
import com.salesforce.ide.ui.editors.properysheets.widgets.ComboWidget;
import com.salesforce.ide.ui.editors.properysheets.widgets.TextFieldWidget;

/**
 * Property sheet for Apex Page
 * 
 * @author nchen
 * 
 */
public class ApexPagePropertySheet extends ApexPropertySheet {
    private static final Logger logger = Logger.getLogger(ApexPagePropertySheet.class);

    private TextFieldWidget labelText;

    private TextFieldWidget descriptionText;

    private CheckBoxWidget isAvailableInTouchCheckbox;

    private CheckBoxWidget isConfirmationTokenRequiredCheckbox;

    private ApexPage apexPageMetadata;

    protected ComboWidget<ApexCodeUnitStatus> statusCombo;

    public ApexPagePropertySheet(BaseComponentMultiPageEditorPart multiPageEditor) {
        super(multiPageEditor);
    }

    @Override
    protected void createContent() {
        setUpLayout();
        createGeneralInformation();
        createPackageVersions();
    }

    protected void createGeneralInformation() {
        Section generalInformation = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        generalInformation.setText(EditorMessages.getString("ApexMetadataFormPage.GeneralInformationSection")); //$NON-NLS-1$
        generalInformation.setDescription(EditorMessages
                .getString("ApexMetadataFormPage.GeneralInformationSection.Description")); //$NON-NLS-1$

        Composite sectionClient = toolkit.createComposite(generalInformation);
        sectionClient.setLayout(new GridLayout(2, false));

        // Label
        labelText = new TextFieldWidget(toolkit, EditorMessages.getString("ApexMetadataFormPage.LabelLabel"), ""); //$NON-NLS-1$ 
        labelText.addTo(sectionClient);

        // Description
        descriptionText =
                new TextFieldWidget(toolkit, EditorMessages.getString("ApexMetadataFormPage.DescriptionLabel"), ""); //$NON-NLS-1$ 
        descriptionText.addTo(sectionClient);

        // Api Version
        apiText = new TextFieldWidget(toolkit, EditorMessages.getString("ApexMetadataFormPage.APIVersionLabel"), "0.0"); //$NON-NLS-1$ 
        apiText.addTo(sectionClient);

        // Touch
        isAvailableInTouchCheckbox =
                new CheckBoxWidget(toolkit, EditorMessages.getString("ApexMetadataFormPage.MobileAppsLabel"), false); //$NON-NLS-1$
        isAvailableInTouchCheckbox.addTo(sectionClient);

        // Confirmation token
        isConfirmationTokenRequiredCheckbox =
                new CheckBoxWidget(toolkit, EditorMessages.getString("ApexMetadataFormPage.CSRFLabel"), false); //$NON-NLS-1$
        isConfirmationTokenRequiredCheckbox.addTo(sectionClient);

        generalInformation.setClient(sectionClient);
    }

    @Override
    public void syncToMetadata() {
        retrieveFormValues();

        try {
            String marshall = marshall(apexPageMetadata);
            setTextOfMetadataEditor(marshall);
        } catch (JAXBException e) {
            // If we have an error, don't sync back to the metadata tab
            // The next time we flip the tab we will have the opportunity to try again
            logger.debug("Error trying to sync to Metadata tab from Apex property sheet: ", e); //$NON-NLS-1$
        }

    }

    @Override
    public void syncFromMetadata() {
        // Start with a clean, empty metadata file.
        // In the event of a parse error from the raw XML, display this empty element
        apexPageMetadata = new ApexPage();

        try {
            apexPageMetadata = unmarshall(getTextFromMetadataEditor(), ApexPage.class);
        } catch (Exception e) {
            logger.debug("Error trying to sync from Metadata tab for Apex property sheet: ", e);
        }

        setFormValues();
    }

    private void setFormValues() {
        // Turn off listeners for internal modifications
        removeListeners();

        labelText.setValue(apexPageMetadata.getLabel());
        descriptionText.setValue(apexPageMetadata.getDescription());

        apiText.setValue(String.format("%.1f", apexPageMetadata.getApiVersion())); //$NON-NLS-1$
        packageVersionTable.setPackageVersions(apexPageMetadata.getPackageVersions());

        isAvailableInTouchCheckbox.setValue(apexPageMetadata.isAvailableInTouch() != null ? apexPageMetadata
                .isAvailableInTouch() : false);
        isConfirmationTokenRequiredCheckbox.setValue(apexPageMetadata.isConfirmationTokenRequired() != null
                ? apexPageMetadata.isConfirmationTokenRequired() : false);

        // Turn on listeners again
        installListeners();
    }

    private void removeListeners() {
        labelText.getTextField().removeListener(SWT.Modify, modificationListener);
        descriptionText.getTextField().removeListener(SWT.Modify, modificationListener);
        apiText.getTextField().removeListener(SWT.Modify, modificationListener);
        packageVersionTable.removeListener(cellEditorListener);
        isAvailableInTouchCheckbox.getCheckBox().removeListener(SWT.Selection, modificationListener);
        isConfirmationTokenRequiredCheckbox.getCheckBox().removeListener(SWT.Selection, modificationListener);

    }

    private void installListeners() {
        labelText.getTextField().addListener(SWT.Modify, modificationListener);
        descriptionText.getTextField().addListener(SWT.Modify, modificationListener);
        apiText.getTextField().addListener(SWT.Modify, modificationListener);
        packageVersionTable.addListener(cellEditorListener);
        isAvailableInTouchCheckbox.getCheckBox().addListener(SWT.Selection, modificationListener);
        isConfirmationTokenRequiredCheckbox.getCheckBox().addListener(SWT.Selection, modificationListener);
    }

    private void retrieveFormValues() {
        apexPageMetadata.setLabel(labelText.getValue());
        apexPageMetadata.setDescription(descriptionText.getValue());

        Double version = retrieveApiVersionIfPossible();
        if (version != null) {
            apexPageMetadata.setApiVersion(version);
        }

        // Don't need to set packageVersions since that is passed by reference

        apexPageMetadata.setAvailableInTouch(isAvailableInTouchCheckbox.getValue());
        apexPageMetadata.setConfirmationTokenRequired(isConfirmationTokenRequiredCheckbox.getValue());
    }
}
