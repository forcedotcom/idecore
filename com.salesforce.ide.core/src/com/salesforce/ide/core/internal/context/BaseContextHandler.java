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

import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.salesforce.ide.core.factories.FactoryLocator;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ForceProjectException;

/**
 *
 * @author cwall
 */
public abstract class BaseContextHandler implements IContextHandler {

    private static Logger logger = Logger.getLogger(BaseContextHandler.class);

    protected AbstractApplicationContext applicationContext = null;

    //   C O N S T R U C T O R
    public BaseContextHandler() {
        super();
    }

    public AbstractApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(AbstractApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected abstract String[] getApplicationContextFiles();

    protected String getApplicationContextFilesString(String[] contextLocations) {
        if (Utils.isEmpty(contextLocations)) {
            return Constants.EMPTY_STRING;
        }

        StringBuffer strBuff = new StringBuffer();
        for (String contextLocation : contextLocations) {
            strBuff.append(contextLocation).append(" ");
        }

        return strBuff.toString().trim();
    }

    /* (non-Javadoc)
     * @see com.salesforce.ide.core.internal.context.IContextHandler#initApplicationContext(java.lang.String[])
     */
    @Override
    public void initApplicationContext(String[] contextLocations) throws ForceProjectException {
        loadApplicationContext(contextLocations, true);
    }

    @Override
    public void loadApplicationContext(String[] contextLocations, boolean classpath) throws ForceProjectException {
        if (Utils.isEmpty(contextLocations)) {
            throw new IllegalArgumentException("Context locations cannot be null or empty");
        }

        try {
            if (classpath) {
                applicationContext = new ClassPathXmlApplicationContext(contextLocations, true, applicationContext);
            } else {
                applicationContext = new FileSystemXmlApplicationContext(contextLocations, true, applicationContext);
            }
        } catch (Exception e) {
            String contextLocationsStr = getApplicationContextFilesString(contextLocations);
            logger.error("Unable to load bean factory from " + contextLocationsStr, e);
            ForceExceptionUtils.throwNewForceProjectException(e, "Unable to load bean factory from "
                    + contextLocationsStr + ": " + e.getMessage());
        }

        if (applicationContext == null) {
            String contextLocationsStr = getApplicationContextFilesString(contextLocations);
            logger
                    .error("Unable to load bean factory from '" + contextLocationsStr
                            + "' - application content is null");
            ForceExceptionUtils.throwNewForceProjectException("Unable to load bean factory from '"
                    + contextLocationsStr + "'");
        }

        if (logger.isDebugEnabled()) {
            String contextLocationsStr = getApplicationContextFilesString(contextLocations);
            logger.debug("Loaded application context from '" + contextLocationsStr + "' registering ["
                    + applicationContext.getBeanDefinitionCount() + "] beans");
            Date date = new Date(applicationContext.getStartupDate());
            logger.debug("Beans loaded at " + date.toString());
        }

        logBeans();
    }

    public void reloadApplicationContext() throws ForceProjectException {
        if (applicationContext == null) {
            initApplicationContext();
        } else {
            applicationContext.refresh();
            if (logger.isInfoEnabled()) {
                logger.info("Reloaded application context registering [" + applicationContext.getBeanDefinitionCount()
                        + "] beans");
                Date date = new Date(applicationContext.getStartupDate());
                logger.info("Beans reloaded at " + date.toString());
            }
        }

        logBeans();
    }

    /* (non-Javadoc)
     * @see com.salesforce.ide.core.internal.context.IContextHandler#getBean(java.lang.Class)
     */
    @Override
    public Object getBean(Class<?> clazz) throws ForceProjectException {
        if (applicationContext == null) {
            initApplicationContext();
        }

        Object bean = null;
        String name = clazz.getSimpleName();
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        try {
            bean = applicationContext.getBean(name);
        } catch (Exception e) {
            logger.error("Unable to get bean for id '" + name + "'");
            logBeans();
            ForceExceptionUtils.throwNewForceProjectException(e, "Unable to get bean for id '" + name + "'");
        }

        if (bean == null) {
            logger.error("Unable to get bean for id '" + name + "'");
            logBeans();
            ForceExceptionUtils.throwNewForceProjectException("Unable to get bean for id '" + name + "'");
        } else if (logger.isDebugEnabled()) {
            logger.debug("Got bean of class '" + bean.getClass().getName() + "' for id '" + name + "'");
        }

        return bean;
    }

    /* (non-Javadoc)
     * @see com.salesforce.ide.core.internal.context.IContextHandler#getBean(java.lang.String)
     */
    @Override
    public Object getBean(String name) throws ForceProjectException {
        if (applicationContext == null) {
            initApplicationContext();
        }

        Object bean = null;
        try {
            bean = applicationContext.getBean(name);
        } catch (Exception e) {
            logBeans();
            ForceExceptionUtils.throwNewForceProjectException(e, "Unable to get bean for id '" + name + "'");
        }

        if (bean == null) {
            logger.warn("Unable to get bean for id '" + name + "'");
            logBeans();
            ForceExceptionUtils.throwNewForceProjectException("Unable to get bean for id '" + name + "'");
        } else if (logger.isDebugEnabled()) {
            logger.debug("Got bean of class '" + bean.getClass().getName() + "' for id '" + name + "'");
        }

        return bean;
    }

    /* (non-Javadoc)
     * @see com.salesforce.ide.core.internal.context.IContextHandler#dispose()
     */
    @Override
    public void dispose() {
        try {
            FactoryLocator factoryLocator = (FactoryLocator) getBean(FactoryLocator.class);
            if (factoryLocator != null) {
                factoryLocator.dispose();
            }
        } catch (ForceProjectException e) {
            logger.warn("Unable to dispose factory locator", e);
        }

        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    protected void logBeans() {
        if (!logger.isDebugEnabled()) {
            return;
        }

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        if (Utils.isEmpty(beanNames)) {
            logger.debug("Application context is empty");
            return;
        }

        StringBuffer strBuff =
                new StringBuffer("\nApplication context currently contains the following ").append("[").append(
                    beanNames.length).append("] beans: ");
        int beanNameCnt = 0;
        Arrays.sort(beanNames, String.CASE_INSENSITIVE_ORDER);
        for (String beanName : beanNames) {
            strBuff.append("\n (").append(++beanNameCnt).append(") ").append(beanName);
        }
        logger.debug(strBuff.toString());
    }
}
