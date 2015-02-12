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
package com.salesforce.ide.core.project;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.springframework.beans.BeansException;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.QualifiedNames;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.services.ServiceLocator;

public abstract class BaseBuilder extends IncrementalProjectBuilder {
    private static final Logger logger = Logger.getLogger(BaseBuilder.class);

    public static final String BEAN_ID = "beanId";

    protected BuilderController builderController = null;

    //   C O N S T R U C T O R
    public BaseBuilder() {
        super();
    }

    //   M E T H O D S
    public BuilderController getBuilderController() {
        return builderController;
    }

    public void setBuilderController(BuilderController builderController) {
        this.builderController = builderController;
    }

    protected ServiceLocator getServiceLocator() {
        return (builderController != null ? ContainerDelegate.getInstance().getServiceLocator() : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
            throws CoreException {
        super.setInitializationData(config, propertyName, data);
        if (data instanceof Hashtable) {
            Hashtable<String, String> parameters = Hashtable.class.cast(data);
            try {
                String controllerId = parameters.get(BEAN_ID);
                if (Utils.isEmpty(controllerId)) {
                    logger.error("Unable to get BuilderController instance - bean id not provided");
                    return;
                }

                builderController = (BuilderController) ContainerDelegate.getInstance().getBean(controllerId);
                if (logger.isDebugEnabled()) {
                    logger.debug("Created build controller instance from bean id '" + controllerId + "'");
                }
            } catch (BeansException e) {
                logger.error("Unable to get BuilderController instance", e);
            } catch (ForceProjectException e) {
                logger.error("Unable to load applicaiton context", e);
            }
        }
    }

    //   M E T H O D S
    protected boolean checkSkipBuilder() {
        if (getProject() == null) {
            return true;
        }

        if (getServiceLocator() != null) {
            ForceProject forceProject = getServiceLocator().getProjectService().getForceProject(getProject());
            if (forceProject == null || !forceProject.areUserCredentialsSpecified()) {
                logger.warn("Skipping build - user credentials not found");
                return true;
            }
        }

        try {
            Boolean newProject = (Boolean) getProject().getSessionProperty(QualifiedNames.QN_SKIP_BUILDER);
            if (newProject != null && newProject) {
                getProject().setSessionProperty(QualifiedNames.QN_SKIP_BUILDER, false);
                return true;
            }
			return false;
        } catch (CoreException e) {
            return false;
        }
    }
}
