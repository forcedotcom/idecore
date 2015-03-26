package com.salesforce.ide.ui.wizards.components.apex.page;

import org.eclipse.jface.text.templates.persistence.TemplateStore;

import com.salesforce.ide.ui.editors.templates.ApexPageTemplateContextType;
import com.salesforce.ide.ui.editors.visualforce.preferences.VisualforceTemplatePreferencePage;
import com.salesforce.ide.ui.wizards.components.apex.NewWizardMessages;
import com.salesforce.ide.ui.wizards.components.apex.VisualforceTemplateSelectionPage;

/**
 * Templates page in new file wizard. Allows users to select a new file
 * template to be applied in new file.
 */
public final class ApexPageTemplateSelectionPage extends VisualforceTemplateSelectionPage {

    public ApexPageTemplateSelectionPage(final TemplateStore templateStore) {
        super(
            ApexPageTemplateContextType.ID,
            templateStore,
            ApexPageTemplateSelectionPage.class.getSimpleName(),
            NewWizardMessages.PageTemplate_desc
        );
    }

    @Override
    protected String getLinkText() {
        return NewWizardMessages.PageTemplate_link;
    }

    @Override
    protected String getPreferencePageId() {
        return VisualforceTemplatePreferencePage.ID;
    }

    @Override
    protected String getHelpContextId() {
        // TODO: Provide a help context for the page's template selection page.
        return null;
    }

}