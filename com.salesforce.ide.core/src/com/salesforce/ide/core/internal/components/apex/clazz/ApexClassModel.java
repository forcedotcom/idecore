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
package com.salesforce.ide.core.internal.components.apex.clazz;

import com.salesforce.ide.core.internal.components.apex.ForceCodeModel;
import com.salesforce.ide.core.internal.utils.Constants;

/**
 * Encapsulates attributes for new Apex Class generation.
 * 
 * @author cwall
 */
public class ApexClassModel extends ForceCodeModel {

    //   C O N S T R U C T O R
    public ApexClassModel() {
        super();
    }

    //   M E T H O D S
    @Override
    public String getComponentType() {
        return Constants.APEX_CLASS;
    }
}
