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

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.internal.components.apex.trigger.ApexTriggerModel;

public abstract class ApexTemplateContextType extends TemplateContextType {

    public ApexTemplateContextType() {
        addGlobalResolvers();
    }

    public ApexTemplateContextType(String id) {
        super(id);
        addGlobalResolvers();
    }

    public ApexTemplateContextType(String id, String name) {
        super(id, name);
        addGlobalResolvers();
    }

    private void addGlobalResolvers() {
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
    }

    @Override
    protected void validateVariables(TemplateVariable[] variables) throws TemplateException {
        // check for multiple cursor variables
        for (int i = 0; i < variables.length; i++) {
            TemplateVariable var = variables[i];
            if (var.getType().equals(GlobalTemplateVariables.Cursor.NAME)) {
                if (var.getOffsets().length > 1) {
                    throw new TemplateException("Template has multiple cursor variables.");
                }
            }
        }
    }

    protected static class ClassName extends TemplateVariableResolver {
        public ClassName() {
            super("class_name", "TODO: Add description of ${class_name}"); //$NON-NLS-1$
        }

        @Override
        protected String resolve(TemplateContext context) {
            if (context instanceof CodeTemplateContext) {
                final ComponentModel model = ((CodeTemplateContext) context).getComponentModel();
                if (null == model) return null;

                return model.getName();
            }
            return null;
        }

        @Override
        protected boolean isUnambiguous(TemplateContext context) {
            return resolve(context) != null;
        }
    }

    protected static class ObjectName extends TemplateVariableResolver {
        public ObjectName() {
            super("object_name", "TODO: Add description of ${object_name}"); //$NON-NLS-1$
        }

        @Override
        protected String resolve(TemplateContext context) {
            if (context instanceof CodeTemplateContext) {
                final ComponentModel model = ((CodeTemplateContext) context).getComponentModel();
                if (model instanceof ApexTriggerModel) {
                    return ((ApexTriggerModel) model).getObjectName();
                }
            }
            return null;
        }

        @Override
        protected boolean isUnambiguous(TemplateContext context) {
            return resolve(context) != null;
        }
    }

    protected static class TriggerOperations extends TemplateVariableResolver {
        public TriggerOperations() {
            super("trigger_operations", "TODO: Add description of ${trigger_operations}"); //$NON-NLS-1$
        }

        @Override
        protected String resolve(TemplateContext context) {
            if (context instanceof CodeTemplateContext) {
                final ComponentModel model = ((CodeTemplateContext) context).getComponentModel();
                if (model instanceof ApexTriggerModel) {
                    final Set<String> operations = ((ApexTriggerModel) model).getOperations();
                    return StringUtils.join(operations, ", ");
                }
            }
            return null;
        }

        @Override
        protected boolean isUnambiguous(TemplateContext context) {
            return resolve(context) != null;
        }
    }

}
