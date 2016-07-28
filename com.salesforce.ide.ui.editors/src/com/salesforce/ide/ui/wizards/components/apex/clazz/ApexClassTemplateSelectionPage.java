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
package com.salesforce.ide.ui.wizards.components.apex.clazz;

import org.eclipse.jface.text.templates.persistence.TemplateStore;

import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.ui.editors.apex.preferences.ApexTemplatePreferencePage;
import com.salesforce.ide.ui.editors.templates.ApexClassTemplateContextType;
import com.salesforce.ide.ui.wizards.components.ApexCodeTemplateSelectionPage;
import com.salesforce.ide.ui.wizards.components.NewWizardMessages;

/**
 * Templates page in new file wizard. Allows users to select a new file
 * template to be applied in new file.
 */
public final class ApexClassTemplateSelectionPage extends ApexCodeTemplateSelectionPage {
    public ApexClassTemplateSelectionPage(final ComponentModel componentModel, final TemplateStore templateStore) {
        super(
            componentModel,
            ApexClassTemplateContextType.ID,
            templateStore,
            ApexClassTemplateSelectionPage.class.getSimpleName(),
            NewWizardMessages.ClassTemplate_desc
        );
    }

    @Override
    protected String getLinkText() {
        return NewWizardMessages.ClassTemplate_link;
    }

    @Override
    protected String getPreferencePageId() {
        return ApexTemplatePreferencePage.ID;
    }

    @Override
    protected String getHelpContextId() {
        // TODO: Provide a help context for the class's template selection page.
        return null;
    }

}
