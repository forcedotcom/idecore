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
package com.salesforce.ide.core.internal.components.apex;

import javax.xml.bind.JAXBException;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.model.Component;

/**
 * Encapsulates common methods for new Force.com code generation.
 * 
 * @author cwall
 */
public abstract class ForceCodeModel extends ComponentModel {

    public ForceCodeModel() {
        super();
    }

    @Override
    public void loadAdditionalComponentAttributes() throws FactoryException, JAXBException {
        // load code body and add to component list
        componentList.add(component);

        // prepare metadata body and component
        Component metadataComponent = componentFactory.getCompositeComponentFromComponent(component);
        saveMetadata(metadataComponent);
    }
}
