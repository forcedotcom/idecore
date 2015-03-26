package com.salesforce.ide.ui.wizards.components.apex.component;

import org.eclipse.jface.text.templates.persistence.TemplateStore;

import com.salesforce.ide.ui.editors.templates.ApexComponentTemplateContextType;
import com.salesforce.ide.ui.editors.visualforce.preferences.VisualforceTemplatePreferencePage;
import com.salesforce.ide.ui.wizards.components.apex.NewWizardMessages;
import com.salesforce.ide.ui.wizards.components.apex.VisualforceTemplateSelectionPage;

/**
 * Templates page in new file wizard. Allows users to select a new file
 * template to be applied in new file.
 */
public final class ApexComponentTemplateSelectionPage extends VisualforceTemplateSelectionPage {
    public ApexComponentTemplateSelectionPage(final TemplateStore templateStore) {
        super(
            ApexComponentTemplateContextType.ID,
            templateStore,
            ApexComponentTemplateSelectionPage.class.getSimpleName(),
            NewWizardMessages.ComponentTemplate_desc
        );
    }

    @Override
    protected String getLinkText() {
        return NewWizardMessages.ComponentTemplate_link;
    }

    @Override
    protected String getPreferencePageId() {
        return VisualforceTemplatePreferencePage.ID;
    }

    @Override
    protected String getHelpContextId() {
        // TODO: Provide a help context for the component's template selection page.
        return null;
    }

}