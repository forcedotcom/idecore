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
package com.salesforce.ide.core.internal.context;

import com.salesforce.ide.core.factories.FactoryLocator;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.services.ServiceLocator;

/**
 *
 *
 * @author cwall
 */
public abstract class BaseContainerDelegate implements IContainerDelegate {

    protected IContextHandler contextHandler = null;
    protected ServiceLocator serviceLocator = null;
    protected FactoryLocator factoryLocator = null;

    //   M E T H O D S
    @Override
    public IContextHandler getContextHandler() {
        return contextHandler;
    }

    protected abstract IContainerDelegate getContainerInstance();

    /* (non-Javadoc)
     * @see com.salesforce.ide.core.internal.context.IContainerDelegate#getBean(java.lang.Class)
     */
    @Override
    public Object getBean(Class<?> clazz)  {
        if (clazz == null || contextHandler == null) {
            return null;
        }

        try {
            return contextHandler.getBean(clazz);
        } catch (ForceProjectException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see com.salesforce.ide.core.internal.context.IContainerDelegate#getBean(java.lang.String)
     */
    @Override
    public Object getBean(String name) throws ForceProjectException {
        if (contextHandler == null || Utils.isEmpty(name)) {
            return null;
        }

        return contextHandler.getBean(name);
    }

    public ServiceLocator getServiceLocator() {
        if (serviceLocator == null) {
            serviceLocator = (ServiceLocator) getBean(ServiceLocator.class);
        }

        if (serviceLocator == null) {
            throw new RuntimeException("Unable to get instance of Service Locator");
        }

        return serviceLocator;
    }

    public FactoryLocator getFactoryLocator() {
        if (factoryLocator == null) {
            factoryLocator = (FactoryLocator) getBean(FactoryLocator.class);
        }

        if (factoryLocator == null) {
            throw new RuntimeException("Unable to get instance of Factory Locator");
        }

        return factoryLocator;
    }

    /* (non-Javadoc)
     * @see com.salesforce.ide.core.internal.context.IContainerDelegate#dispose()
     */
    @Override
    public void dispose() {
        if (contextHandler != null) {
            contextHandler.dispose();
        }
    }
}
