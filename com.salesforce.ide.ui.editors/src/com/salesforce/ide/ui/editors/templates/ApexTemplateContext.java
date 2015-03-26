package com.salesforce.ide.ui.editors.templates;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;

import com.salesforce.ide.core.internal.components.ComponentModel;

public final class ApexTemplateContext extends DocumentTemplateContext {
    private final ComponentModel componentModel;

    public ApexTemplateContext(TemplateContextType type, ComponentModel componentModel, Position position) {
        super(type, new Document(), position);
        this.componentModel= componentModel;
    }

    public ApexTemplateContext(TemplateContextType type, ComponentModel componentModel, int offset, int length) {
        super(type, new Document(), offset, length);
        this.componentModel= componentModel;
    }

    public ComponentModel getComponentModel() {
        return this.componentModel;
    }

}
