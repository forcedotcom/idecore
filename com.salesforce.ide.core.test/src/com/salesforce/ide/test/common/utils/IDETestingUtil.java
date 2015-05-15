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
package com.salesforce.ide.test.common.utils;

import java.io.File;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.factories.FactoryLocator;
import com.salesforce.ide.core.factories.PackageManifestFactory;
import com.salesforce.ide.core.factories.ProjectPackageFactory;
import com.salesforce.ide.core.internal.components.ComponentController;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProjectException;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.services.MetadataService;
import com.salesforce.ide.core.services.PackageDeployService;
import com.salesforce.ide.core.services.PackageRetrieveService;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.core.services.ServiceLocator;

/**
 * Convenience class for IDE related utility methods
 * 
 * @author agupta
 * @deprecated Move these methods to IdeTestUtil
 */
@Deprecated
public class IDETestingUtil extends BaseTestingUtil {

    private static Logger logger = Logger.getLogger(IDETestingUtil.class);
    public static String TEST_FRAGMENT = "com.salesforce.ide.test";
    protected static ServiceLocator serviceLocator = null;
    protected static FactoryLocator factoryLocator = null;

    static {
        try {
            serviceLocator = (ServiceLocator) getBean("serviceLocator");
            factoryLocator = (FactoryLocator) getBean("factoryLocator");
        } catch (ForceProjectException e) {
            logger.error("Failed to instantiate TestingUtil. Failed to obtain serviceLocator bean. Cause: " + e);
            throw new Error(e);
        }
    }

    public static TreeSet<String> getSupportedEndpointVersions() {
        return getProjectService().getSupportedEndpointVersions();
    }

    public static ComponentController getComponentController(ComponentTypeEnum componentType)
            throws ForceProjectException {
        return componentType.getComponentController();
    }

    public static Class<?> getComponentTypeJaxbClass(ComponentTypeEnum componentType) throws ClassNotFoundException {
        return Class.forName("com.salesforce.ide.api.metadata.types." + componentType.name());
    }

    public static ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public static ProjectService getProjectService() {
        return serviceLocator.getProjectService();
    }

    public static PackageDeployService getPackageDeployService() {
        return serviceLocator.getPackageDeployService();
    }

    public static PackageRetrieveService getPackageRetrieveService() {
        return serviceLocator.getPackageRetrieveService();
    }

    public static MetadataService getMetadataService() {
        return serviceLocator.getMetadataService();
    }

    public static ProjectController getProjectController() throws ForceProjectException {
        return new ProjectController();
    }

    public static FactoryLocator getFactoryLocator() throws ForceProjectException {
        return factoryLocator;
    }

    public static ComponentFactory getComponentFactory() throws ForceProjectException {
        return factoryLocator.getComponentFactory();
    }

    public static ConnectionFactory getConnectionFactory() throws ForceProjectException {
        return factoryLocator.getConnectionFactory();
    }

    public static ProjectPackageFactory getProjectPackageFactory() {
        return factoryLocator.getProjectPackageFactory();
    }

    public static ProjectPackageList getProjectPackageListInstance() {
        return factoryLocator.getProjectPackageFactory().getProjectPackageListInstance();
    }

    public static PackageManifestFactory getPackageManifestFactory() {
        return factoryLocator.getPackageManifestFactory();
    }

    public static String getPdeTestFile(String fileNameNoPath) {
        String testSrcHome =
                ConfigProps.getInstance().getProperty("release.home")
                        + ConfigProps.getInstance().getProperty("client.path");
        return testSrcHome + File.separator + TEST_FRAGMENT + File.separator + fileNameNoPath;
    }

    public static File getFileFromIFile(IFile iFile) {
        String filePath = iFile.getLocation().toString();
        return new File(filePath);
    }
}
