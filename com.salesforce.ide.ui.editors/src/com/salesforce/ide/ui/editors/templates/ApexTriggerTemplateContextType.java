package com.salesforce.ide.ui.editors.templates;

public final class ApexTriggerTemplateContextType extends ApexTemplateContextType {

    public static final String ID = "com.salesforce.ide.ui.editors.templates.contextType.apexTrigger"; //$NON-NLS-1$

    public ApexTriggerTemplateContextType() {
        super();
        addLocalResolvers();
    }

    public ApexTriggerTemplateContextType(String id) {
        super(id);
        addLocalResolvers();
    }

    public ApexTriggerTemplateContextType(String id, String name) {
        super(id, name);
        addLocalResolvers();
    }

    private void addLocalResolvers() {
        addResolver(new ClassName());
        addResolver(new ObjectName());
        addResolver(new TriggerOperations());
    }

}
