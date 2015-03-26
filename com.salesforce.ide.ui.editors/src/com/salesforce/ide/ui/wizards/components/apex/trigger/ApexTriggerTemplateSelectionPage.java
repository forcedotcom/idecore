package com.salesforce.ide.ui.wizards.components.apex.trigger;

import org.eclipse.jface.text.templates.persistence.TemplateStore;

import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.ui.editors.apex.preferences.ApexTemplatePreferencePage;
import com.salesforce.ide.ui.editors.templates.ApexTriggerTemplateContextType;
import com.salesforce.ide.ui.wizards.components.apex.ApexCodeTemplateSelectionPage;
import com.salesforce.ide.ui.wizards.components.apex.NewWizardMessages;

/**
 * Templates page in new file wizard. Allows users to select a new file
 * template to be applied in new file.
 */
public final class ApexTriggerTemplateSelectionPage extends ApexCodeTemplateSelectionPage {
    public ApexTriggerTemplateSelectionPage(final ComponentModel componentModel, final TemplateStore templateStore) {
        super(
            componentModel,
            ApexTriggerTemplateContextType.ID,
            templateStore,
            ApexTriggerTemplateSelectionPage.class.getSimpleName(),
            NewWizardMessages.TriggerTemplate_desc
        );
    }

    @Override
    protected String getLinkText() {
        return NewWizardMessages.TriggerTemplate_link;
    }

    @Override
    protected String getPreferencePageId() {
        return ApexTemplatePreferencePage.ID;
    }

    @Override
    protected String getHelpContextId() {
        // TODO: Provide a help context for the trigger's template selection page.
        return null;
    }

}