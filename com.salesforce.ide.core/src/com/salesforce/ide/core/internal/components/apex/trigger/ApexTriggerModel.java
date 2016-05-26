/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/
package com.salesforce.ide.core.internal.components.apex.trigger;

import java.util.ArrayList;
import java.util.Set;

import javax.xml.bind.JAXBException;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.components.apex.ForceCodeModel;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.ApexTrigger;
import com.salesforce.ide.core.model.Component;

/**
 * Encapsulates attributes for new Apex Trigger generation.
 * 
 * This class is unique in that attributes are captured separately than the metadata objects to generate the code body.
 * 
 * @author cwall
 */
public class ApexTriggerModel extends ForceCodeModel {

    private String objectName = null;
    private Set<String> operations = null;

    //  C O N S T R U C T O R
    public ApexTriggerModel() {
        super();
    }

    //  M E T H O D S
    @Override
    public String getComponentType() {
        return Constants.APEX_TRIGGER;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Set<String> getOperations() {
        return operations;
    }

    public void setOperations(Set<String> operations) {
        this.operations = operations;
    }

    @Override
    public void loadAdditionalComponentAttributes() throws FactoryException, JAXBException {
        // load code body and add to component list
        ApexTrigger apexTrigger = (ApexTrigger) component;
        apexTrigger.setObjectName(getObjectName());
        apexTrigger.setOperations(new ArrayList<>(operations));
        componentList.add(apexTrigger);

        // prepare metadata body and component
        Component metadataComponent = componentFactory.getCompositeComponentFromComponent(component);
        saveMetadata(metadataComponent);
    }
}
