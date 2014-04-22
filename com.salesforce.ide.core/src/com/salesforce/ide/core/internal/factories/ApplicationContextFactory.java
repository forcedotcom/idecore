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
package com.salesforce.ide.core.internal.factories;


import com.salesforce.ide.core.factories.BaseFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;

public abstract class ApplicationContextFactory extends BaseFactory {

    public ApplicationContextFactory() {
        super();
    }

    public Object getBean(String beanId) throws ForceProjectException {
        if (Utils.isEmpty(beanId)) {
            throw new IllegalArgumentException("Bean id cannot be null");
        }

        return ContainerDelegate.getInstance().getBean(beanId);
    }
}
