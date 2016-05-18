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
package com.salesforce.ide.core.internal.components.lightning;

import java.util.List;

import javax.xml.bind.JAXBException;

import com.google.common.collect.Lists;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.components.apex.ForceCodeModel;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.AuraDefinitionBundleComponent;
import com.salesforce.ide.core.model.Component;

/**
 * Represents the AuraDefinitionBundle that we are ultimately operating on.
 * 
 * @author nchen
 */
public class AuraDefinitionBundleModel extends ForceCodeModel {
    AuraDefinitionBundleComponent primaryComponent;
    List<AuraDefinitionBundleComponent> secondaryComponents = Lists.newArrayList();
    
    public AuraDefinitionBundleModel() {
        super();
    }
    
    @Override
    public String getComponentType() {
        return Constants.AURA_DEFINITION_BUNDLE;
    }
    
    public void setPrimaryComponent(String name, String fileName, String extension, String body) {
        AuraDefinitionBundleComponent auraDefinition = createAuraDefinitionBundle(name, fileName, extension, body);
        primaryComponent = auraDefinition;
    }
    
    public void addSecondaryComponent(String name, String fileName, String extension, String body) {
        AuraDefinitionBundleComponent auraDefinition = createAuraDefinitionBundle(name, fileName, extension, body);
        secondaryComponents.add(auraDefinition);
    }
    
    private AuraDefinitionBundleComponent createAuraDefinitionBundle(String name, String fileName, String extension, String body) {
        AuraDefinitionBundleComponent auraDefinition = (AuraDefinitionBundleComponent) getComponentFactory().getComponentById(Constants.AURA_DEFINITION_BUNDLE);
        auraDefinition.setPackageName(component.getPackageName());
        auraDefinition.setName(name);
        auraDefinition.setFileExtension(extension);
        auraDefinition.setFileName(fileName);
        auraDefinition.setFullName(name + "/" + fileName);
        auraDefinition.setFilePath(Constants.SOURCE_FOLDER_NAME + "/" + Constants.AURA + "/" + name + "/" + fileName);
        auraDefinition.setBody(body);
        return auraDefinition;
    }
    
    @Override
    public void loadAdditionalComponentAttributes() throws FactoryException, JAXBException {
        componentList.add(primaryComponent);
        Component metadataComponent = componentFactory.getCompositeComponentFromComponent(primaryComponent);
        saveMetadata(metadataComponent);
        
        secondaryComponents.stream().forEach(c -> componentList.add(c));
    }
    
}
