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
package com.salesforce.ide.core.internal.components.object;

import com.salesforce.ide.core.internal.components.generic.GenericComponentModel;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;

/**
 * Encapsulates attributes for new Custom Object generation.
 * 
 * @author cwall
 */
public class CustomObjectModel extends GenericComponentModel {

    public CustomObjectModel() {
        super();
    }

    @Override
    public String getComponentType() {
        return Constants.CUSTOM_OBJECT;
    }

    @Override
    public void setFullName(String name) {
        if (Utils.isNotEmpty(name) && !name.endsWith(Constants.CUSTOM_OBJECT_SUFFIX)) {
            name += Constants.CUSTOM_OBJECT_SUFFIX;
        }
        component.setName(name);
    }
}
