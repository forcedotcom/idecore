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

import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;

/**
 * Represents the things that can go into bundle.
 * 
 * @author nchen
 *         
 */
public enum LightningElement {
    APP("Application", "app", generateTemplate("app")),
    CMP("Component", "cmp", generateTemplate("cmp")),
    CONTROLLER("Controller", "js", generateTemplate("controller"), s -> s + "Controller"),
    DESIGN("Design", "design", generateTemplate("design")),
    AURADOC("Documentation", "auradoc", generateTemplate("auradoc")),
    SVG("Icon", "svg", generateTemplate("svg")),
    INTF("Interface", "intf", generateTemplate("intf")),
    EVT("Event", "evt", generateTemplate("evt")),
    HELPER("Helper", "js", generateTemplate("helper"), s -> s + "Helper"),
    TOKENS("Tokens", "tokens", generateTemplate("tokens")),
    RENDERER("Renderer", "js", generateTemplate("renderer"), s -> s + "Renderer"),
    CSS("Style", "css", generateTemplate("style"));
    
    final String extension;
    final String label;
    final Function<String, String> nameFn;
    final Supplier<Template> templateFn;
    
    LightningElement(String label, String extension, Supplier<Template> templateFn) {
        this.label = label;
        this.extension = extension;
        this.templateFn = templateFn;
        this.nameFn = s -> s;
    }
    
    LightningElement(String label, String extension, Supplier<Template> templateFn, Function<String, String> nameFn) {
        this.label = label;
        this.extension = extension;
        this.templateFn = templateFn;
        this.nameFn = nameFn;
    }
    
    public static ContextTypeRegistry getTemplateContextRegistry() {
        return ForceIdeEditorsPlugin.getDefault().getLightningTemplateContextRegistry();
    }
    
    public static TemplateStore getTemplateStore() {
        return ForceIdeEditorsPlugin.getDefault().getLightningTemplateStore();
    }
    
    private static Supplier<Template> generateTemplate(String id) {
        return () -> {
            String fqn = String.format("com.salesforce.ide.ui.editor.templates.aura.%s.default", id);
            return getTemplateStore().findTemplateById(fqn);
        };
    }
}
