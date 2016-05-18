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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;

import com.salesforce.ide.core.internal.components.ComponentController;
import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.internal.components.lightning.AuraDefinitionBundleModel;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.ui.editors.ForceIdeEditorsPlugin;
import com.salesforce.ide.ui.editors.templates.CodeTemplateContext;
import com.salesforce.ide.ui.editors.templates.LightningTemplateContextType;

/**
 * The controller for an AuraDefinitionBundle.
 * 
 * @author nchen
 *         
 */
public class AuraDefinitionBundleComponentController extends ComponentController {
    
    public AuraDefinitionBundleComponentController() throws ForceProjectException {
        super(new AuraDefinitionBundleModel());
    }
    
    @Override
    protected void preSaveProcess(ComponentModel componentWizardModel, IProgressMonitor monitor)
        throws InterruptedException, InvocationTargetException {}
        
    private ContextTypeRegistry getTemplateContextRegistry() {
        return ForceIdeEditorsPlugin.getDefault().getLightningTemplateContextRegistry();
    }
    
    private String materializeTemplate(Template template) {
        try {
            TemplateContextType contextType =
                getTemplateContextRegistry().getContextType(LightningTemplateContextType.ID);
            TemplateContext context = new CodeTemplateContext(contextType, getComponentWizardModel(), 0, 0);
            return context.evaluate(template).getString();
        } catch (BadLocationException | TemplateException e) {
            final String msg = "Unable to create template for new component";
            final IStatus status = new Status(IStatus.WARNING, ForceIdeEditorsPlugin.PLUGIN_ID, msg, e);
            logger().log(status);
        }
        
        return "";
    }
    
    private static ILog logger() {
        return ForceIdeEditorsPlugin.getDefault().getLog();
    }
    
    public void prepareComponents(String componentName, LightningBundleType selectedBundleType) {
        AuraDefinitionBundleModel componentWizardModel = (AuraDefinitionBundleModel) getComponentWizardModel();
        
        // Primary component
        LightningElement primaryElement = selectedBundleType.primaryElement;
        String fileName = String.format("%s.%s", componentName, primaryElement.extension);
        componentWizardModel.setPrimaryComponent(
            componentName,
            fileName,
            primaryElement.extension,
            materializeTemplate(primaryElement.templateFn.get()));
            
        // Secondary components (nice to haves)
        selectedBundleType.secondaryElementsToInclude.stream().forEach(el -> {
            String fileName_ = String.format("%s.%s", el.nameFn.apply(componentName), el.extension);
            componentWizardModel
                .addSecondaryComponent(componentName, fileName_, el.extension, materializeTemplate(el.templateFn.get()));
        });
    }
    
}
