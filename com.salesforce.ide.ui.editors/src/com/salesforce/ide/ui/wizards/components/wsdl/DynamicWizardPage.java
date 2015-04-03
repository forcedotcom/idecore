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
package com.salesforce.ide.ui.wizards.components.wsdl;

import org.eclipse.jface.wizard.WizardPage;

/**
 * Used for creating pages that need dynamic widgets
 * 
 * @author kevin.ren
 * 
 */
public abstract class DynamicWizardPage extends WizardPage {

    protected DynamicWizardPage(String pageName) {
        super(pageName);
    }

    @Override
    public DynamicWizardPage getNextPage() {
        return (DynamicWizardPage) super.getNextPage();
    }

    /**
     * This method is used for adding any dynamic widgets onto the page
     */
    public abstract void onEnterPage();

}
