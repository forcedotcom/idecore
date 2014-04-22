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
package com.salesforce.ide.core.internal.components.tab;

import com.salesforce.ide.core.internal.components.generic.GenericComponentModel;
import com.salesforce.ide.core.internal.utils.Constants;

/**
 * Encapsulates attributes for new Custom Tab generation.
 * 
 * @author cwall
 */
public class CustomTabModel extends GenericComponentModel {

    // C O N S T R U C T O R
    public CustomTabModel() {
        super();
    }

    // M E T H O D S
    @Override
    public String getComponentType() {
        return Constants.CUSTOM_TAB;
    }
}
