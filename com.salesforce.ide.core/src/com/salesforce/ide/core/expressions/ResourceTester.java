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
package com.salesforce.ide.core.expressions;

import org.apache.log4j.Logger;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.salesforce.ide.core.factories.ComponentFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.services.ProjectService;
import com.salesforce.ide.core.services.ServiceLocator;

/**
 * Tester implementation for contribution enablement.
 * 
 * @author cwall
 */
public class ResourceTester extends PropertyTester {

    private static final Logger logger = Logger.getLogger(ResourceTester.class);

    public static final String PATH_CONTAINS = "pathContains";
    public static final String PATH_STARTS_WITH = "pathStartsWith";
    public static final String IS_SOURCE_RESOURCE = "isSourceResource";
    public static final String IS_SOURCE_ROOT = "isSourceRoot";
    public static final String IS_REFRESHABLE_RESOURCE = "isRefreshableResource";
    public static final String IS_DEPLOYABLE_RESOURCE = "isDeployableResource";
    public static final String IS_SOURCE_COMPONENT_FOLDER = "isSourceComponentFolder";
    public static final String IS_PRJ_OR_NON_REF_PKG_FOLDERS = "isPrjOrNonRefPkgFolders";
    public static final String IS_RUNTEST_ENABLED_RESOURCES = "isRunTestEnabledResources";

    private ProjectService projectService = null;
    private ComponentFactory componentFactory = null;

    public ResourceTester() {
        super();
        ServiceLocator serviceLocator = ContainerDelegate.getInstance().getServiceLocator();
        projectService = serviceLocator.getProjectService();
        componentFactory = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory();
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver == null || !(receiver instanceof IResource)) {
            return false;
        }

        if (!projectService.isInManagedProject((IResource) receiver)) {
            return false;
        }

        if (property.equals(PATH_CONTAINS)) {
            return testPathContains(receiver, property, args, expectedValue);
        } else if (property.equals(PATH_STARTS_WITH)) {
            return testPathStartsWith(receiver, property, args, expectedValue);
        } else if (property.equals(IS_SOURCE_RESOURCE)) {
            return testIsSourceResource(receiver, property, args, expectedValue);
        } else if (property.equals(IS_SOURCE_ROOT)) {
            return testIsSourceRoot(receiver, property, args, expectedValue);
        } else if (property.equals(IS_REFRESHABLE_RESOURCE)) {
            return testIsRefreshableResource(receiver, property, args, expectedValue);
        } else if (property.equals(IS_DEPLOYABLE_RESOURCE)) {
            return testIsDeployableResource(receiver, property, args, expectedValue);
        } else if (property.equals(IS_SOURCE_COMPONENT_FOLDER)) {
            return testIsSourceComponentFolder(receiver, property, args, expectedValue);
        } else if (property.equals(IS_PRJ_OR_NON_REF_PKG_FOLDERS)) {
            return testIsPrjOrNonRefPkgFolders(receiver, property, args, expectedValue);
        } else if (property.equals(IS_RUNTEST_ENABLED_RESOURCES)) {
            return testIsRunTestEnabledResources(receiver, property, args, expectedValue);
        }

        // in every other case
        return false;
    }

    private boolean testIsRunTestEnabledResources(Object receiver, String property, Object[] args, Object expectedValue) {
        IResource resource = null;
        if (receiver instanceof IResource) {
            resource = (IResource) receiver;
        } else {
            return false;
        }
        String apexClassFolderName;
        String apexClassFileExtensionName;
        Component component = componentFactory.getComponentByComponentType(Constants.APEX_CLASS);
        apexClassFolderName = Utils.isNotEmpty(component) ? component.getDefaultFolder() : null;
        apexClassFileExtensionName = Utils.isNotEmpty(component) ? component.getFileExtension() : null;
        if ((resource.getType() == IResource.FOLDER && resource.getName().equals(apexClassFolderName))
                || (resource.getType() == IResource.FILE && resource.getFileExtension().equals(
                    apexClassFileExtensionName))) {
            if (!projectService.isReferencedPackageResource(resource)) {
                return true;
            }
        }

        return false;
    }

    private boolean testIsPrjOrNonRefPkgFolders(Object receiver, String property, Object[] args, Object expectedValue) {
        IResource resource = null;
        if (receiver instanceof IResource) {
            resource = (IResource) receiver;
        } else {
            return false;
        }

        if ((receiver instanceof IFolder || receiver instanceof IProject)
                && !projectService.isReferencedPackageResource(resource)) {
            return true;
        }
        return false;
    }

    private static boolean testPathContains(Object receiver, String property, Object[] args, Object expectedValue) {
        String fullpath = null;
        IResource resource = null;
        if (receiver instanceof IResource) {
            resource = (IResource) receiver;
            fullpath = resource.getProjectRelativePath().toPortableString();
        } else {
            return false;
        }

        if (Utils.isNotEmpty(fullpath) && Utils.isEmpty(expectedValue.toString())) {
            return true;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Testing if path '" + fullpath + "' for path contains '" + expectedValue.toString() + "'");
        }

        if (Utils.isNotEmpty(fullpath)) {
            return fullpath.contains(expectedValue.toString());
        }

        // in every other case
        return false;
    }

    private static boolean testPathStartsWith(Object receiver, String property, Object[] args, Object expectedValue) {
        String fullpath = null;
        IResource resource = null;
        if (receiver instanceof IResource) {
            resource = (IResource) receiver;
            fullpath = resource.getProjectRelativePath().toPortableString();
        } else {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Testing resource for path starts with '" + fullpath + "'");
        }

        if (Utils.isNotEmpty(fullpath)) {
            return fullpath.startsWith(expectedValue.toString());
        }

        // in every other case
        return false;
    }

    private boolean testIsSourceResource(Object receiver, String property, Object[] args, Object expectedValue) {
        IResource resource = null;
        if (receiver instanceof IResource) {
            resource = (IResource) receiver;
        } else {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Testing resource is a source resource '"
                    + resource.getProjectRelativePath().toPortableString() + "'");
        }

        if (projectService.isPackageManifestFile(resource)) {
            return false;
        }

        if (projectService.isSourceResource(resource)) {
            return true;
        }
		if (logger.isDebugEnabled()) {
		    logger.debug("Resource '" + resource.getName() + "', project path: '"
		            + resource.getProjectRelativePath().toPortableString() + "' is not a source folder resource");
		}
		return false;
    }

    private boolean testIsSourceRoot(Object receiver, String property, Object[] args, Object expectedValue) {
        IResource resource = null;
        if (receiver instanceof IResource) {
            resource = (IResource) receiver;
        } else {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger
                    .debug("Testing if '" + resource.getProjectRelativePath().toPortableString()
                            + "' is the source root");
        }

        if (projectService.isPackageManifestFile(resource)) {
            return false;
        }

        return projectService.isSourceFolder(resource);
    }

    private boolean testIsRefreshableResource(Object receiver, String property, Object[] args, Object expectedValue) {
        IResource resource = null;
        if (receiver instanceof IResource) {
            resource = (IResource) receiver;
        } else {
            return false;
        }

        // for files, don't refresh...
        //   package.xml
        //   schema file
        //   files in installed, managed packages (we take a holistic approach to referenced package content)
        if (resource.getType() == IResource.FILE
                && (Constants.PACKAGE_MANIFEST_FILE_NAME.equals(resource.getName())
                        || Constants.SCHEMA_FILENAME.equals(resource.getName()) || projectService
                        .isReferencedPackageResource(resource))) {
            return false;
        }

        // for folders, don't refresh...
        //   installed, managed packages component folders (we take a holistic approach to referenced package content)
        if (resource.getType() == IResource.FOLDER
                && (projectService.isReferencedPackageResource(resource.getParent()) && resource
                        .getProjectRelativePath().toPortableString().split("/").length > 2)) {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Refreshing resource '" + resource.getName() + "', project path: '"
                    + resource.getProjectRelativePath().toPortableString() + "'");
        }

        return true;
    }

    private boolean testIsDeployableResource(Object receiver, String property, Object[] args, Object expectedValue) {
        IResource resource = null;
        if (receiver instanceof IResource) {
            resource = (IResource) receiver;
        } else {
            return false;
        }

        if (projectService.isReferencedPackageResource(resource)
                || Constants.PACKAGE_MANIFEST_FILE_NAME.equals(resource.getName())
                || Constants.SCHEMA_FILENAME.equals(resource.getName())) {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Refreshing resource '" + resource.getName() + "', project path: '"
                    + resource.getProjectRelativePath().toPortableString() + "'");
        }

        return true;
    }

    private boolean testIsSourceComponentFolder(Object receiver, String property, Object[] args, Object expectedValue) {
        IResource resource = null;
        if (receiver instanceof IResource) {
            resource = (IResource) receiver;
        } else {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Testing if resource '" + resource.getName() + "', project path '"
                    + resource.getProjectRelativePath().toPortableString() + "' is a source component folder");
        }

        return projectService.isSourceResource(resource);
    }
}
