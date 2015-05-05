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
package com.salesforce.ide.core.services;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.factories.FactoryLocator;
import com.salesforce.ide.core.factories.MetadataFactory;
import com.salesforce.ide.core.factories.PackageManifestFactory;
import com.salesforce.ide.core.factories.ProjectPackageFactory;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.remote.registries.DescribeObjectRegistry;

public abstract class BaseService {
    private static final Logger logger = Logger.getLogger(BaseService.class);

    protected ServiceLocator serviceLocator = null;
    protected FactoryLocator factoryLocator = null;

    //   C O N S T R U C T O R
    public BaseService() {
        super();
    }

    //   M E T H O D S
    // services
    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public void setServiceLocator(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public ProjectService getProjectService() {
        return serviceLocator.getProjectService();
    }

    public MetadataService getMetadataService() {
        return serviceLocator.getMetadataService();
    }

    public LoggingService getLoggingService() {
        return serviceLocator.getLoggingService();
    }

    public PackageDeployService getPackageDeployService() {
        return serviceLocator.getPackageDeployService();
    }

    public PackageRetrieveService getPackageRetrieveService() {
        return serviceLocator.getPackageRetrieveService();
    }

    //   F A C T O R I E S
    public FactoryLocator getFactoryLocator() {
        return factoryLocator;
    }

    public void setFactoryLocator(FactoryLocator factoryLocator) {
        this.factoryLocator = factoryLocator;
    }

    public ConnectionFactory getConnectionFactory() {
        return factoryLocator.getConnectionFactory();
    }

    public MetadataFactory getMetadataFactory() {
        return factoryLocator.getMetadataFactory();
    }

    public ComponentFactory getComponentFactory() {
        return factoryLocator.getComponentFactory();
    }

    public ProjectPackageFactory getProjectPackageFactory() {
        return factoryLocator.getProjectPackageFactory();
    }

    public ProjectPackageList getProjectPackageListInstance() {
        return factoryLocator.getProjectPackageFactory().getProjectPackageListInstance();
    }

    public PackageManifestFactory getPackageManifestFactory() {
        return factoryLocator.getPackageManifestFactory();
    }

    public DescribeObjectRegistry getDescribeObjectRegistry() {
        return getConnectionFactory().getDescribeObjectRegistry();
    }

    //   M O N I T O R
    protected void monitorWorkCheck(IProgressMonitor monitor, String subtask) throws InterruptedException {
        monitorCheck(monitor);
        monitorWork(monitor, subtask);
    }

    protected void monitorWorkCheck(IProgressMonitor monitor) throws InterruptedException {
        monitorCheck(monitor);
        monitorWork(monitor);
    }

    protected void monitorCheckSubTask(IProgressMonitor monitor, String subtask) throws InterruptedException {
        monitorCheck(monitor);
        monitorSubTask(monitor, subtask);
    }

    protected void monitorCheck(IProgressMonitor monitor) throws InterruptedException {
        if (monitor != null) {
            if (monitor.isCanceled()) {
                throw new InterruptedException("Operation cancelled");
            }
        }
    }

    protected void monitorWork(IProgressMonitor monitor, String subtask) {
        if (monitor == null) {
            return;
        }

        monitor.subTask(subtask);
        monitor.worked(1);
        if (logger.isDebugEnabled()) {
            logger.debug(subtask);
        }
    }

    protected void monitorSubTask(IProgressMonitor monitor, String subtask) {
        if (monitor == null) {
            return;
        }

        monitor.subTask(subtask);
        if (logger.isDebugEnabled()) {
            logger.debug(subtask);
        }
    }

    protected void monitorWork(IProgressMonitor monitor) {
        if (monitor == null) {
            return;
        }

        monitor.worked(1);
    }

    protected void monitorDone(IProgressMonitor monitor) {
        if (monitor == null) {
            return;
        }

        monitor.done();
    }

}
