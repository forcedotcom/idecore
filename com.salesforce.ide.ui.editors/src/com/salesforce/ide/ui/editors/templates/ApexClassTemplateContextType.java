package com.salesforce.ide.ui.editors.templates;

public final class ApexClassTemplateContextType extends ApexTemplateContextType {

    public static final String ID = "com.salesforce.ide.ui.editors.templates.contextType.apexClass"; //$NON-NLS-1$

    public ApexClassTemplateContextType() {
        super();
        addLocalResolvers();
    }

    public ApexClassTemplateContextType(String id) {
        super(id);
        addLocalResolvers();
    }

    public ApexClassTemplateContextType(String id, String name) {
        super(id, name);
        addLocalResolvers();
    }

    private void addLocalResolvers() {
        addResolver(new ClassName());
    }

}
