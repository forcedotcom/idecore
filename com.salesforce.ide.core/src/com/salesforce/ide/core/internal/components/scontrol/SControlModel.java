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
package com.salesforce.ide.core.internal.components.scontrol;

import javax.xml.bind.JAXBException;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.components.ComponentModel;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.Component;

/**
 * Encapsulates attributes for new S-Control generation.
 * 
 * @author cwall
 */
public class SControlModel extends ComponentModel {

    //   C O N S T R U C T O R
    public SControlModel() {
        super();
    }

    //   M E T H O D S
    @Override
    public String getComponentType() {
        return Constants.SCONTROL;
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
