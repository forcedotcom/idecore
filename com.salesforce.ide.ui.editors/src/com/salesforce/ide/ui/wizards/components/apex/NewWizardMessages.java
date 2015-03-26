package com.salesforce.ide.ui.wizards.components.apex;

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