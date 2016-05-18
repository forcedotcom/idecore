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
package com.salesforce.ide.ui.editors.templates;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;

import com.salesforce.ide.core.internal.components.ComponentModel;

public final class CodeTemplateContext extends DocumentTemplateContext {
    private final ComponentModel componentModel;

    public CodeTemplateContext(TemplateContextType type, ComponentModel componentModel, Position position) {
        super(type, new Document(), position);
        this.componentModel= componentModel;
    }

    public CodeTemplateContext(TemplateContextType type, ComponentModel componentModel, int offset, int length) {
        super(type, new Document(), offset, length);
        this.componentModel= componentModel;
    }

    public ComponentModel getComponentModel() {
        return this.componentModel;
    }

}
