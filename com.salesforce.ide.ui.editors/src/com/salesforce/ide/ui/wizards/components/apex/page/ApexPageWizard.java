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
package com.salesforce.ide.ui.wizards.components.apex.page;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import com.salesforce.ide.core.internal.components.apex.page.ApexPageComponentController;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.templates.ApexPageTemplateContextType;
import com.salesforce.ide.ui.editors.templates.CodeTemplateContext;
import com.salesforce.ide.ui.wizards.components.AbstractTemplateSelectionPage;
import com.salesforce.ide.ui.wizards.components.ComponentWizardPage;
import com.salesforce.ide.ui.wizards.components.TemplateSelectionWizard;

/**
 * Wizard to create new Apex Class.
 * 
 * @author cwall
 */
public class ApexPageWizard extends TemplateSelectionWizard {

    public ApexPageWizard() throws ForceProjectException {
        super();
        controller = new ApexPageComponentController();
    }

    @Override
    protected ComponentWizardPage getComponentWizardPageInstance() {
        return new ApexPageWizardPage(this);
    }

    @Override
    public void addPages() {
        super.addPages();
        super.addPage(new ApexPageTemplateSelectionPage(getTemplateStore()));
    }

    @Override
    public boolean performFinish() {
        if (!getComponentController().canComplete()) {
            return false;
        }

        final ComponentWizardPage wizardPage = getWizardPage();

        // create component based on given user input
        try {
            wizardPage.saveUserInput();
            wizardPage.clearMessages();

            // set focus on label or name if focus is returned, eg remote name check fails
            if (wizardPage.getBaseComponentWizardComposite().getTxtLabel() != null) {
                wizardPage.getBaseComponentWizardComposite().getTxtLabel().setFocus();
            } else if (wizardPage.getBaseComponentWizardComposite().getTxtName() != null) {
                wizardPage.getBaseComponentWizardComposite().getTxtName().setFocus();
            }

            TemplateContextType contextType = getTemplateContextRegistry().getContextType(ApexPageTemplateContextType.ID);
            TemplateContext context = new CodeTemplateContext(contextType, getComponentWizardModel(), 0, 0);

            final AbstractTemplateSelectionPage page = (AbstractTemplateSelectionPage) getPage(ApexPageTemplateSelectionPage.class.getSimpleName());
            final String body = page.getTemplateString(context);
            if (null != body) {
                getComponentController().getComponent().initNewBody(body);
            }

            return executeCreateOperation();
        } catch (Exception e) {
            Utils.openError(e, true, "Unable to create " + getComponentType() + ".");
            return false;
        }
    }

    private static ContextTypeRegistry getTemplateContextRegistry() {
        // TODO: Inject the Apex template context registry.
        return ForceIdeEditorsPlugin.getDefault().getVisualforceTemplateContextRegistry();
    }

    private static TemplateStore getTemplateStore() {
        // TODO: Inject the Visualforce template store.
        return ForceIdeEditorsPlugin.getDefault().getVisualforceTemplateStore();
    }
}
