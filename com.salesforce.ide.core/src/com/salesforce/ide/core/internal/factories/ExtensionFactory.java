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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.context.IContainerDelegate;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;

/**
 * Proxy class between Eclipse's extension point manager and beans within the force's container.
 * 
 * Extension points that reference com.salesforce.ide.core.internal.factories.ExtensionFactory are direct to the factory
 * for container instantiation. The container handles all dependencies through injection. This was done for the
 * following reasons: - separation of Eclipse framework from application framework including view-based class from
 * service layer - anticipation of multiple SFDC API support - better control of component model outside of compiled
 * code - separate configuration-base properties and reducing hardcoded settings from compiled code
 * 
 * Example: The following stanza defines a class for a given extension point. When Eclipse initiates this class, the
 * ExtensionFactory is called to delegate the creation of the bean referenced on the right-side of the colon. In this
 * case, the "projectCreateWizard" bean within the application-context.xml configuration file will be instantiated and
 * injected with all configured dependencies. The colon notion is standard to extension point class definitions.
 * 
 * class="com.salesforce.ide.core.internal.factories.ExtensionFactory:projectCreateWizard"
 * 
 * 
 * @author cwall
 */
public class ExtensionFactory implements IExecutableExtension, IExecutableExtensionFactory {
    private static final Logger logger = Logger.getLogger(ExtensionFactory.class);

    private Object bean = null;
    private String beanRef = null;

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
            throws CoreException {
        beanRef = (String) data;

        if (logger.isDebugEnabled()) {
            logger.debug("Initializing bean '" + beanRef + "' for extension " + config.getAttribute("id"));
        }

        try {
            bean = getContainerInstance().getBean(beanRef);
            if (logger.isDebugEnabled()) {
                logger.debug("Got bean of class '" + bean.getClass().getName() + "'");
            }
        } catch (Exception e) {
            logger.error("Unable to get bean instance for bean id '" + beanRef + "' of class '"
                    + (bean != null ? bean.getClass().getName() : "n/a") + "'", e);
            ForceExceptionUtils.throwNewCoreException(e);
        }
    }

    @Override
    public Object create() throws CoreException {

        if (bean == null) {
            logger.warn("Return null bean instance for bean id '" + beanRef + "'");
            return bean;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Return bean instance of class '" + bean.getClass().getName() + "' for bean id '" + beanRef
                    + "'");
        }

        return bean;
    }

    protected IContainerDelegate getContainerInstance() {
        return ContainerDelegate.getInstance();
    }

}
