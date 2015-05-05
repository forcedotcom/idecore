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

import com.salesforce.ide.core.factories.FactoryLocator;

public class ServiceLocator {

    protected MetadataService metadataService = null;
    protected PackageDeployService packageDeployService = null;
    protected PackageRetrieveService packageRetrieveService = null;
    protected ProjectService projectService = null;
    protected LoggingService loggingService = null;
    protected ApexService apexService = null;
    protected ToolingDeployService toolingDeployService = null;
    protected ToolingService toolingService = null;
    protected FactoryLocator factoryLocator = null;

    //   C O N S T R U C T O R
    public ServiceLocator() {
        super();
    }

    //   M E T H O D S
    public MetadataService getMetadataService() {
        return metadataService;
    }

    public void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    public PackageDeployService getPackageDeployService() {
        return packageDeployService;
    }

    public void setPackageDeployService(PackageDeployService packageDeployService) {
        this.packageDeployService = packageDeployService;
    }

    public PackageRetrieveService getPackageRetrieveService() {
        return packageRetrieveService;
    }

    public void setPackageRetrieveService(PackageRetrieveService packageRetrieveService) {
        this.packageRetrieveService = packageRetrieveService;
    }

    public ProjectService getProjectService() {
        return projectService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public LoggingService getLoggingService() {
        return loggingService ;
    }

    public void setLoggingService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    public ApexService getApexService() {
        return apexService;
    }

    public void setApexService(ApexService apexService) {
        this.apexService = apexService;
    }

    public ToolingDeployService getToolingDeployService() {
        return toolingDeployService;
    }

    public void setToolingDeployService(ToolingDeployService toolingDeployService) {
        this.toolingDeployService = toolingDeployService;
    }

    public ToolingService getToolingService() {
        return toolingService;
    }

    public void setToolingService(ToolingService toolingService) {
        this.toolingService = toolingService;
    }

    // factories
    public FactoryLocator getFactoryLocator() {
        return factoryLocator;
    }

    public void setFactoryLocator(FactoryLocator factoryLocator) {
        this.factoryLocator = factoryLocator;
    }
}
