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

import com.salesforce.ide.api.metadata.types.ApexClass;
import com.salesforce.ide.ui.editors.internal.BaseComponentMultiPageEditorPart;
import com.salesforce.ide.ui.editors.internal.utils.EditorMessages;
import com.salesforce.ide.ui.editors.properysheets.widgets.TextFieldWidget;

/**
 * Property sheet for Apex class.
 * 
 * This is very similar to {@link ApexTriggerPropertySheet}. We just need two separate classes because ApexClasses and
 * ApexTriggers do not share a common hierarchy.
 * 
 * @author nchen
 * 
 */
public class ApexClassPropertySheet extends ApexClassAndTriggerPropertySheet {

    private static final Logger logger = Logger.getLogger(ApexClassPropertySheet.class);

    private ApexClass apexClassMetadata;

    public ApexClassPropertySheet(BaseComponentMultiPageEditorPart multiPageEditor) {
        super(multiPageEditor);
    }

    @Override
    protected void createGeneralInformation() {
        Section generalInformation = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        generalInformation.setText(EditorMessages.getString("ApexMetadataFormPage.GeneralInformationSection")); //$NON-NLS-1$
        generalInformation.setDescription(EditorMessages
                .getString("ApexMetadataFormPage.GeneralInformationSection.Description")); //$NON-NLS-1$

        Composite sectionClient = toolkit.createComposite(generalInformation);
        sectionClient.setLayout(new GridLayout(2, false));

        // Api Version
        apiText = new TextFieldWidget(toolkit, EditorMessages.getString("ApexMetadataFormPage.APIVersionLabel"), "0.0"); //$NON-NLS-1$ //$NON-NLS-2$
        apiText.addTo(sectionClient);

        generalInformation.setClient(sectionClient);
    }

    @Override
    public void syncToMetadata() {
        retrieveFormValues();

        try {
            String marshall = marshall(apexClassMetadata);
            setTextOfMetadataEditor(marshall);
        } catch (JAXBException e) {
            // If we have an error, don't sync back to the metadata tab
            // The next time we flip the tab we will have the opportunity to try again
            logger.debug("Error trying to sync to Metadata tab from Apex property sheet: ", e);
        }
    }

    @Override
    public void syncFromMetadata() {
        // Start with a clean, empty metadata file.
        // In the event of a parse error from the raw XML, display this empty element
        apexClassMetadata = new ApexClass();

        try {
            apexClassMetadata = unmarshall(getTextFromMetadataEditor(), ApexClass.class);
        } catch (Exception e) {
            logger.debug("Error trying to sync from Metadata tab for Apex property sheet: ", e);
        }

        setFormValues();
    }

    private void setFormValues() {
        // Turn off listeners for internal modifications
        removeListeners();

        apiText.setValue(String.format("%.1f", apexClassMetadata.getApiVersion()));
        packageVersionTable.setPackageVersions(apexClassMetadata.getPackageVersions());

        // Turn on listeners again
        installListeners();
    }

    private void installListeners() {
        apiText.getTextField().addListener(SWT.Modify, modificationListener);
        packageVersionTable.addListener(cellEditorListener);
    }

    private void removeListeners() {
        apiText.getTextField().removeListener(SWT.Modify, modificationListener);
        packageVersionTable.removeListener(cellEditorListener);
    }

    private void retrieveFormValues() {

        Double version = retrieveApiVersionIfPossible();
        if (version != null) {
            apexClassMetadata.setApiVersion(version);
        }

        // Don't need to set packageVersions since that is passed by reference
    }

}
