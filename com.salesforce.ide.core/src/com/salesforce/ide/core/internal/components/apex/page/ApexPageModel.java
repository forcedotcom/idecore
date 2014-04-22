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
package com.salesforce.ide.core.internal.components.apex.page;

import com.salesforce.ide.core.internal.components.apex.ApexCodeModel;
import com.salesforce.ide.core.internal.utils.Constants;

/**
 * Encapsulates attributes for new Apex Page generation.
 * 
 * @author cwall
 */
public class ApexPageModel extends ApexCodeModel {

    // C O N S T R U C T O R
    public ApexPageModel() {
        super();
    }

    // M E T H O D S
    @Override
    public String getComponentType() {
        return Constants.APEX_PAGE;
    }
}
