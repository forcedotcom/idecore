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
package com.salesforce.ide.core.factories;

import com.salesforce.ide.core.services.ServiceLocator;


public class FactoryLocator {

    public ConnectionFactory connectionFactory = null;
    public ComponentFactory componentFactory = null;
    public ProjectPackageFactory projectPackageFactory = null;
    public PackageManifestFactory packageManifestFactory = null;
    public MetadataFactory metadataFactory = null;
    public ToolingFactory toolingFactory = null;
    protected ServiceLocator serviceLocator = null;

    //   C O N S T R U C T O R
    public FactoryLocator() {
        super();
    }

    //   M E T H O D S
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public ComponentFactory getComponentFactory() {
        return componentFactory;
    }

    public void setComponentFactory(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    public ProjectPackageFactory getProjectPackageFactory() {
        return projectPackageFactory;
    }

    public void setProjectPackageFactory(ProjectPackageFactory projectPackageFactory) {
        this.projectPackageFactory = projectPackageFactory;
    }

    public PackageManifestFactory getPackageManifestFactory() {
        return packageManifestFactory;
    }

    public void setPackageManifestFactory(PackageManifestFactory packageManifestFactory) {
        this.packageManifestFactory = packageManifestFactory;
    }

    public MetadataFactory getMetadataFactory() {
        return metadataFactory;
    }

    public void setMetadataFactory(MetadataFactory metadataFactory) {
        this.metadataFactory = metadataFactory;
    }

    public ToolingFactory getToolingFactory() {
        return toolingFactory;
    }

    public void setToolingFactory(ToolingFactory toolingFactory) {
        this.toolingFactory = toolingFactory;
    }

    // services
    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public void setServiceLocator(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public void dispose() {
        if (connectionFactory != null) {
            connectionFactory.dispose();

            if (connectionFactory.getDescribeObjectRegistry() != null) {
                connectionFactory.getDescribeObjectRegistry().dispose();
            }
        }

        if (metadataFactory != null) {
            metadataFactory.dispose();
        }
    }
}
