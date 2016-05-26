/*******************************************************************************
 * Copyright (c) 2015 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.wizards.components.apex.component;

import org.eclipse.jface.text.templates.persistence.TemplateStore;

import com.salesforce.ide.ui.editors.templates.ApexComponentTemplateContextType;
import com.salesforce.ide.ui.editors.visualforce.preferences.VisualforceTemplatePreferencePage;
import com.salesforce.ide.ui.wizards.components.NewWizardMessages;
import com.salesforce.ide.ui.wizards.components.VisualforceTemplateSelectionPage;

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
