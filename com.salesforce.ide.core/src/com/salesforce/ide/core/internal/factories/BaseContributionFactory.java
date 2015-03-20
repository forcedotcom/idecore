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

import java.io.ByteArrayInputStream;

import org.apache.log4j.Logger;
import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public abstract class BaseContributionFactory implements IContributionFactory {

    private static Logger logger = Logger.getLogger(BaseContributionFactory.class);

    protected String contributionTemplate = null;
    protected IExtensionRegistry registry = null;
    protected Object key = null;
    protected IContributor contributor = null;

    public BaseContributionFactory() {
        super();
        registry = RegistryFactory.getRegistry();
        key = ((ExtensionRegistry) registry).getTemporaryUserToken();
        contributor = ContributorFactoryOSGi.createContributor(getBundle());
    }

    protected abstract Bundle getBundle();

    /* (non-Javadoc)
     * @see com.salesforce.ide.ui.internal.factories.IContributionFactory#setContributionTemplate(java.lang.String)
     */
    @Override
    public void setContributionTemplate(String contributionTemplate) {
        this.contributionTemplate = contributionTemplate;
    }

    protected void registerContribution(String type, String template) {
        if (logger.isDebugEnabled()) {
            logger.debug("Registering the following contribution\n" + template);
        }

        ByteArrayInputStream is = new ByteArrayInputStream(template.getBytes());
        try {
            boolean result = Platform.getExtensionRegistry().addContribution(is, contributor, false, null, null, key);
            if (result) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Successfully added '" + type + "' contribution");
                }
            } else {
                logger.warn("Failed to add '" + type + "' contribution");
            }
        } catch (Exception e) {
            logger.error("Failed to add '" + type + "' contribution", e);
        }
    }
}
