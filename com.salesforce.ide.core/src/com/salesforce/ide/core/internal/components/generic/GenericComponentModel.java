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
package com.salesforce.ide.core.internal.components.generic;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.components.ComponentModel;

/**
 * Encapsulates generic attributes of an object type.
 * 
 * @author cwall
 */
public abstract class GenericComponentModel extends ComponentModel {
    private static final Logger logger = Logger.getLogger(GenericComponentModel.class);

    //   C O N S T R U C T O R
    public GenericComponentModel() {
        super();
    }

    //   M E T H O D S
    @Override
    public void loadAdditionalComponentAttributes() throws FactoryException, JAXBException {
        // prepare metadata body and component
        boolean success = saveMetadata(component);
        if (!success) {
            logger.warn("Unable to add component to list");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Prepared the following components for saving:\n " + componentList.toString());
            }
        }
    }
}
