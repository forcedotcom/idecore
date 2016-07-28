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
package com.salesforce.ide.ui.wizards.components.apex.clazz;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import com.salesforce.ide.core.internal.components.MultiClassComponentController;
import com.salesforce.ide.core.internal.components.apex.clazz.ApexClassComponentController;
import com.salesforce.ide.core.internal.components.apex.clazz.ApexClassModel;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.templates.ApexClassTemplateContextType;
import com.salesforce.ide.ui.editors.templates.CodeTemplateContext;
import com.salesforce.ide.ui.wizards.components.AbstractTemplateSelectionPage;
import com.salesforce.ide.ui.wizards.components.ComponentWizardComposite;
import com.salesforce.ide.ui.wizards.components.ComponentWizardPage;
import com.salesforce.ide.ui.wizards.components.TemplateSelectionWizard;

/**
 * Wizard to create new Apex Class.
 * 
 * @author cwall, kevin.ren
 */
public class ApexClassWizard extends TemplateSelectionWizard {

    public ApexClassWizard() throws ForceProjectException {
        controller = new ApexClassComponentController();
    }

    public ApexClassWizard(final Boolean saveMultipleFiles, final ProjectPackageList allPackages ) throws ForceProjectException {
        if (null != saveMultipleFiles && saveMultipleFiles.booleanValue()) {
            controller = new MultiClassComponentController(new ApexClassModel(), allPackages);
        } else {
            controller = new ApexClassComponentController();
        }
    }

    @Override
    protected ComponentWizardPage getComponentWizardPageInstance() {
        return new ApexClassWizardPage(this);
    }

    @Override
    public void addPages() {
        super.addPages();
        super.addPage(new ApexClassTemplateSelectionPage(getComponentWizardModel(), getTemplateStore()));
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
            final ComponentWizardComposite container = wizardPage.getBaseComponentWizardComposite();
            if (container.getTxtLabel() != null) {
                container.getTxtLabel().setFocus();
            } else if (container.getTxtName() != null) {
                container.getTxtName().setFocus();
            }

            TemplateContextType contextType = getTemplateContextRegistry().getContextType(ApexClassTemplateContextType.ID);
            TemplateContext context = new CodeTemplateContext(contextType, getComponentWizardModel(), 0, 0);

            final AbstractTemplateSelectionPage page = (AbstractTemplateSelectionPage) getPage(ApexClassTemplateSelectionPage.class.getSimpleName());
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

    private ContextTypeRegistry getTemplateContextRegistry() {
        // TODO: Inject the Apex template context registry.
        return ForceIdeEditorsPlugin.getDefault().getApexTemplateContextRegistry();
    }

    private TemplateStore getTemplateStore() {
        // TODO: Inject the Apex template store.
        return ForceIdeEditorsPlugin.getDefault().getApexTemplateStore();
    }
}
