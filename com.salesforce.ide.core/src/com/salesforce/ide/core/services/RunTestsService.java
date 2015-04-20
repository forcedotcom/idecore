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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.LoggingInfo;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.DeployResultExt;
import com.salesforce.ide.core.remote.metadata.IDeployResultExt;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.LogInfo;

/**
 *
 * FIXME comment here
 *
 * @author fchang
 */
public class RunTestsService extends BaseService {
    private static final Logger logger = Logger.getLogger(RunTestsService.class);

    public IDeployResultExt runTests(IResource projectResource, IProgressMonitor monitor)
            throws ForceConnectionException, FactoryException, CoreException, InterruptedException, ServiceException, ForceRemoteException {
        Connection connection = getConnectionFactory().getConnection(projectResource.getProject());
        String[] testClazz = testCases(projectResource);
        ProjectPackageList projectPackageList =
                getProjectPackageFactory().loadProjectPackageList(projectResource, monitor);
        LogInfo[] logInfos =
                getLoggingService().getAllLogInfo(projectPackageList.getProject(),
                    LoggingInfo.SupportedFeatureEnum.RunTest);
        return runTests(connection, projectPackageList, testClazz, logInfos, monitor);
    }

    /**
     * Don't use this method other than tests: Declaration should be private.
     *
     * @throws InterruptedException
     * @throws ServiceException
     * @throws ForceRemoteException
     */
    public DeployResultExt runTests(Connection connection, ProjectPackageList projectPackageList, String[] tests,
            LogInfo[] logInfos, IProgressMonitor monitor) throws InterruptedException,
            ServiceException, ForceRemoteException {
        DeployOptions deployOptions = getPackageDeployService().getRunTestDeployOptions(tests);

        DeployResultExt deployResultExt = null;
        try {
            deployResultExt =
                    getPackageDeployService().deploy(connection, getPackageDeployService().getZip(projectPackageList),
                        deployOptions, logInfos, monitor);
        } catch (ServiceTimeoutException ex) {
            deployResultExt = getPackageDeployService().handleDeployServiceTimeoutException(ex, "Run Tests", monitor);
        }

        deployResultExt.getRetrieveResultHandler().setProjectPackageList(projectPackageList);
        return deployResultExt;
    }

    private String[] testCases(IResource resource) throws FactoryException, CoreException {
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }

        if (IResource.FOLDER == resource.getType()) {
            return testCases((IFolder) resource);
        } else if (IResource.FILE == resource.getType()) {
            return testCases((IFile) resource);
        } else {
            logger.warn("Resource '" + resource.getProjectRelativePath().toPortableString()
                    + "' is not a folder or file");
            return null;
        }
    }

    /**
     * Execute run tests on given classes.
     *
     * @param folder
     * @return
     * @throws FactoryException
     * @throws CoreException
     */
    private String[] testCases(IFolder folder) throws FactoryException, CoreException {
        if (folder == null) {
            throw new IllegalArgumentException("Folder cannot be null");
        }

        String namespacePrefix = null;
        String className = null;
        Set<String> classNames = new HashSet<>();
        IResource[] members = folder.members();

        if (Utils.isEmpty(members)) {
            logger.warn("No classes found in found " + folder.getProjectRelativePath().toPortableString());
            return null;
        }

        for (IResource resource : members) {
            if (IResource.FILE == resource.getType()) {
                IFile componentFile = (IFile) resource;

                if (!getProjectService().isManagedFile(componentFile)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unable to handle run test candidate "
                                + componentFile.getProjectRelativePath().toPortableString());
                    }
                    continue;
                }

                Component component = getComponentFactory().getComponentFromFile(componentFile);
                if (component == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unable to handle run test candidate "
                                + componentFile.getProjectRelativePath().toPortableString());
                    }
                    continue;
                }

                namespacePrefix = component.getNamespacePrefix();
                className = Utils.stripExtension(componentFile);
                boolean exits =
                        classNames.add(Utils.isEmpty(namespacePrefix) ? className : namespacePrefix + "." + className);
                if (exits) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Added class '" + className + "' as a run test candidate");
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Class '" + className + "' already exists as run test candidate");
                    }
                }
            }
        }
        return classNames.toArray(new String[classNames.size()]);
    }

    public String[] testCases(IFile componentFile) throws FactoryException {
        if (componentFile == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        if (!getProjectService().isManagedFile(componentFile)) {
            logger.warn("Unable to handle run test candidate "
                    + componentFile.getProjectRelativePath().toPortableString() + " - file is not a "
                    + Constants.PLUGIN_NAME + " managed resource");
            return null;
        }

        Component component = getComponentFactory().getComponentFromFile(componentFile);
        if (component == null) {
            logger.warn("Unable to handle run test candidate "
                    + componentFile.getProjectRelativePath().toPortableString() + " - component is null");
            return null;
        }

        String namespacePrefix = component.getNamespacePrefix();
        String className = Utils.stripExtension(componentFile);

        if (logger.isDebugEnabled()) {
            logger.debug("Added class '" + className + "' as a run test candidate");
        }

        return new String[] { Utils.isEmpty(namespacePrefix) ? className : namespacePrefix + "." + className };
    }

}
