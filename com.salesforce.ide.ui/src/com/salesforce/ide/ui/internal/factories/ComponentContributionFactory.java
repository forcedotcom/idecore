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
package com.salesforce.ide.ui.internal.factories;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.internal.factories.BaseContributionFactory;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.ui.ForceIdeUIPlugin;

public abstract class ComponentContributionFactory extends BaseContributionFactory {

    private static Logger logger = Logger.getLogger(ComponentContributionFactory.class);

    private ComponentFactory componentFactory = null;

    public ComponentContributionFactory() {
        super();
    }

    public void setComponentFactory(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    @Override
    protected Bundle getBundle() {
        return ForceIdeUIPlugin.getDefault().getBundle();
    }

    @Override
    public void initContributions() {
        if (logger.isDebugEnabled()) {
            logger.debug("***  R E G I S T E R   C O N T R I B U T I O N S   ***");
            ForceIdeUIPlugin.getStopWatch().start("ForceIdeUIPlugin.initContributions");
        }

        if (Utils.isEmpty(contributionTemplate)) {
            logger.warn("No contribution template found");
            return;
        }

        ComponentList componentList = componentFactory.getEnabledRegisteredComponents();
        if (Utils.isEmpty(componentList)) {
            return;
        }

        componentList.sort();

        try {
            String contributionTemplate = getContributionTemplate();
            if (Utils.isEmpty(contributionTemplate)) {
                throw new IllegalArgumentException("Contribution template not provided");
            }

            for (Component component : componentList) {
                String wizardClassName = component.getWizardClassName();
                if (Utils.isEmpty(wizardClassName)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No wizard class found for '" + component.getDisplayName() + "'");
                    }
                    continue;
                }

                if (!isValidComponentForContribution(component)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipping contribution for '" + component.getDisplayName() + "'");
                    }
                    continue;
                }

                String extensionPoint = getExtensionPoint();
                if (Utils.isEmpty(extensionPoint)) {
                    throw new IllegalArgumentException("Extension point not provided");
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Registering '" + extensionPoint + "' contribution for component type '"
                            + component.getDisplayName() + "'");
                }

                // register
                String tmpTemplate = replaceTokens(contributionTemplate, component, wizardClassName);
                registerContribution(extensionPoint, tmpTemplate);
            }
        } catch (IOException e) {
            logger.error("Unable to register contribution", e);
        } finally {
            if (logger.isDebugEnabled()) {
                ForceIdeUIPlugin.getStopWatch().stop("ForceIdeUIPlugin.initContributions");
            }
        }
    }

    protected abstract String getContributionTemplate() throws IOException;

    protected abstract boolean isValidComponentForContribution(Component component);

    protected abstract String getExtensionPoint() throws IOException;

    protected abstract String replaceTokens(String template, Component component, String wizardClassName);
}
