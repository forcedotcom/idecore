/*******************************************************************************
 * Copyright (c) 2016 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.ui.wizards.components.lightning;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.wizards.components.ComponentWizardPage;
import com.salesforce.ide.ui.wizards.components.TemplateSelectionWizard;

/**
 * Wizard to create new Lightning Bundles.
 * 
 * @author nchen
 *         
 */
public class AuraDefinitionBundleWizard extends TemplateSelectionWizard {
    
    public AuraDefinitionBundleWizard() throws ForceProjectException {
        controller = new AuraDefinitionBundleComponentController();
    }
    
    @Override
    protected ComponentWizardPage getComponentWizardPageInstance() {
        return new AuraDefinitionBundleWizardPage(this);
    }
    
    @Override
    public void addPages() {
        super.addPages();
    }
    
    @Override
    public boolean performFinish() {
        if (!getComponentController().canComplete()) {
            return false;
        }
        
        try {
            AuraDefinitionBundleWizardPage wizardPage = (AuraDefinitionBundleWizardPage) getWizardPage();
            wizardPage.saveUserInput();
            wizardPage.clearMessages();
            
            AuraDefinitionBundleComponentController auraController =
                (AuraDefinitionBundleComponentController) wizardPage.getComponentController();
            auraController.prepareComponents(wizardPage.getComponentName(), wizardPage.getSelectedBundleType());
                
            return executeCreateOperation();
        } catch (Exception e) {
            Utils.openError(e, true, "Unable to create " + getComponentType() + ".");
            return false;
        }
    }
    
}
