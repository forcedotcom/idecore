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
package com.salesforce.ide.ui.wizards.components;

import org.eclipse.osgi.util.NLS;

public class NewWizardMessages extends NLS {
    public static String WizardPage_0;
    public static String WizardPage_1;
    public static String WizardPage_2;
    public static String WizardPage_3;
    public static String WizardPage_4;
    public static String WizardPage_5;

    public static String ClassTemplate_desc;
    public static String ClassTemplate_link;

    public static String ComponentTemplate_desc;
    public static String ComponentTemplate_link;

    public static String PageTemplate_desc;
    public static String PageTemplate_link;

    public static String TriggerTemplate_desc;
    public static String TriggerTemplate_link;

    static {
        // load message values from bundle file
        NLS.initializeMessages(NewWizardMessages.class.getName(), NewWizardMessages.class);
    }

    private NewWizardMessages() {
        // cannot create new instance
    }
}
