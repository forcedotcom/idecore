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
package com.salesforce.ide.ui.refactoring;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.model.ProjectPackage;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.project.ForceProjectException;

/**
 * Service methods for refactoring functionality.
 *
 * @author cwall
 */
public class ChangeRefactorController extends BaseRefactorController {
    private static final Logger logger = Logger.getLogger(ChangeRefactorController.class);

    public ChangeRefactorController() {
        super();
        refactorModel = new ChangeRefactorModel();
    }

    @Override
    public ChangeRefactorModel getRefactorModel() {
        return (ChangeRefactorModel) refactorModel;
    }

    /**
     * Validates copy/move request by evaluating the destination, object types permissions, and name uniqueness.
     *
     * @param changeElements
     * @param destinationResource
     * @param monitor
     * @return
     * @throws CoreException
     * @throws InterruptedException
     */
    public RefactoringStatus validateChangeDestination(IProgressMonitor monitor) throws CoreException,
            InterruptedException {
        monitorCheck(monitor);

        RefactoringStatus refactoringStatus = new RefactoringStatus();
        Set<IResource> changeCandidates = refactorModel.getChangeResources();

        for (IResource changeCandidate : changeCandidates) {
            if (changeCandidate == null) {
                StringBuffer strBuff = new StringBuffer(Constants.PLUGIN_NAME);
                strBuff.append(" does not support copying resource 'null' to destination '").append(
                    getRefactorModel().getDestinationPath()).append("'");
                logger.error(strBuff.toString());
                refactoringStatus.addEntry(createErrorRefactoringStatusEntry(strBuff.toString()));
                continue;
            }

            // check if resource already exists
            if (!isNameUnique(changeCandidate, monitor)) {
                StringBuffer strBuff = new StringBuffer("Resource '");
                strBuff.append(changeCandidate.getFullPath().toPortableString()).append(
                    "' already exists in destination project '").append(getRefactorModel().getDestinationProjectName())
                        .append("' and/or on server");
                logger.error(strBuff.toString());
                strBuff.append(".  Are you sure you want to overwrite?");
                refactoringStatus.addEntry(createWarningRefactoringStatusEntry(strBuff.toString()));
                continue;
            }

            // evaluate based on resource type
            if (changeCandidate.getType() == IResource.FILE) {
                List<RefactoringStatusEntry> entries = validateChangeFile((IFile) changeCandidate, monitor);
                for (RefactoringStatusEntry refactoringStatusEntry : entries) {
                    refactoringStatus.addEntry(refactoringStatusEntry);
                }
            } else if (changeCandidate.getType() == IResource.FOLDER) {
                List<RefactoringStatusEntry> entries = validateChangeFolder((IFolder) changeCandidate, monitor);
                for (RefactoringStatusEntry refactoringStatusEntry : entries) {
                    refactoringStatus.addEntry(refactoringStatusEntry);
                }
            } else if (changeCandidate.getType() == IResource.PROJECT) {
                StringBuffer strBuff = new StringBuffer(Constants.PLUGIN_NAME);
                strBuff.append(" does not support copying project '").append(changeCandidate.getName()).append(
                    "' to destination project '").append(getRefactorModel().getDestinationProjectName()).append("'");

                logger.error(strBuff.toString());
                refactoringStatus.addEntry(createFatalRefactoringStatusEntry(strBuff.toString()));
            }
        }

        return refactoringStatus;
    }

    private List<RefactoringStatusEntry> validateChangeFile(IFile changeCandidate, IProgressMonitor monitor)
            throws InterruptedException {
        monitorCheck(monitor);
        // container to hold status entries
        List<RefactoringStatusEntry> entries = new ArrayList<>();

        // get components to inspect
        ProjectPackageList projectPackageList = refactorModel.getProjectPackageList();
        if (projectPackageList == null) {
            String message =
                    "Cannot find component for file '" + changeCandidate.getFullPath().toPortableString() + "'";
            logger.error(message);
            entries.add(createFatalRefactoringStatusEntry(message));
            return entries;
        }

        ComponentList componentList = projectPackageList.getAllComponents();

        // get enabled object types to compare against
        String[] enabledComponentTypes = null;
        try {
            enabledComponentTypes = getEnabledComponentTypes(getRefactorModel().getDestinationProject(), monitor);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Unable to get enabled object types for project '"
                    + getRefactorModel().getDestinationProjectName() + "'");
        }

        for (Component component : componentList) {
            // skip if package.xml or metadata file (assumes associated composite file is also in list)
            if (component.isPackageManifest() || component.isMetadataInstance()) {
                continue;
            }

            // check against enabled types
            if (!isEnabledComponentType(enabledComponentTypes, component)) {
                String message =
                        "Object type '" + component.getComponentType() + "' is not enabled for project '"
                                + getRefactorModel().getDestinationProjectName() + "'";
                logger.error(message);
                entries.add(createFatalRefactoringStatusEntry(message));
            }

            // further inspect change - destination folder, et al
            RefactoringStatusEntry entry = validateChangeComponent(component, changeCandidate, monitor);
            if (entry != null) {
                entries.add(entry);
            }
        }

        return entries;
    }

    private RefactoringStatusEntry validateChangeComponent(Component component, IFile changeCandidate,
            IProgressMonitor monitor) {
        String componentFolderName = component.getDefaultFolder();

        if (ContainerDelegate.getInstance().getServiceLocator().getProjectService().isComponentFolder(getRefactorModel().getDestinationResource())
                && componentFolderName.equals(getRefactorModel().getDestinationName())) {
            // component is to-be copy to component folder
            if (logger.isInfoEnabled()) {
                logger.info("Valid copy request - copying component '" + component.getFullDisplayName() + "' to '"
                        + getRefactorModel().getDestinationPath() + "'");
            }
            return createInfoRefactoringStatusEntry("Copying component '" + component.getFullDisplayName() + "' to '"
                    + getRefactorModel().getDestinationPath() + "'");
        }

		StringBuffer strBuff = new StringBuffer();
		strBuff.append(Constants.PLUGIN_NAME).append(" does not support copying file '").append(
		    changeCandidate.getFullPath().toPortableString()).append("' to destination '").append(
		    getRefactorModel().getDestinationPath()).append("'");
		logger.error("Invalid copy request - " + strBuff.toString());
		// copy destination not valid
		return createFatalRefactoringStatusEntry(strBuff.toString());
    }

    private List<RefactoringStatusEntry> validateChangeFolder(IFolder changeFolder, IProgressMonitor monitor)
            throws CoreException, InterruptedException {
        List<RefactoringStatusEntry> entries = new ArrayList<>();

        if (ContainerDelegate.getInstance().getServiceLocator().getProjectService().isSourceFolder(changeFolder)
                && ContainerDelegate.getInstance().getServiceLocator().getProjectService().isSourceFolder(getRefactorModel().getDestinationResource())) {
            // evaluate folders against object type permissions
            entries.add(evaluateComponentTypeEnablementForSourceFolder(changeFolder, monitor));

            // everything looks good!
            if (logger.isInfoEnabled()) {
                logger.info("Valid copy request - copying folder '" + changeFolder.getName() + "' to project '"
                        + getRefactorModel().getDestinationProjectName() + "'");
            }
            entries.add(createInfoRefactoringStatusEntry("Moving package '" + changeFolder.getName() + "' to project '"
                    + getRefactorModel().getDestinationProjectName() + "'"));

        } else if (ContainerDelegate.getInstance().getServiceLocator().getProjectService().isComponentFolder(changeFolder)
                && ContainerDelegate.getInstance().getServiceLocator().getProjectService().isSourceFolder(getRefactorModel().getDestinationResource())) {

            // evaluate folders against object type permissions
            RefactoringStatusEntry entry = evaluateComponentTypeEnablementForComponentFolder(changeFolder, monitor);
            if (entry != null) {
                entries.add(entry);
            }

            // everything looks good!
            StringBuffer strBuff = new StringBuffer("Copying all '");
            strBuff.append(changeFolder.getName()).append("' components to package '").append(
                getRefactorModel().getDestinationName()).append("' in project '").append(
                getRefactorModel().getDestinationProjectName()).append("'");
            if (logger.isInfoEnabled()) {
                logger.info("Valid copy request - " + strBuff.toString());
            }
            entries.add(createInfoRefactoringStatusEntry(strBuff.toString()));
        } else {
            // change is not support
            StringBuffer strBuff = new StringBuffer(Constants.PLUGIN_NAME);
            strBuff.append(" does not support copying folder '").append(changeFolder.getFullPath().toPortableString())
                    .append("' to destination '").append(getRefactorModel().getDestinationPath()).append(
                        "'.  The target folder may not be a a valid parent folder.");

            logger.error("Invalid copy request - " + strBuff.toString());
            entries.add(createFatalRefactoringStatusEntry(strBuff.toString()));
        }
        return entries;
    }

    // evaluate permissions on change components - per component folder
    protected RefactoringStatusEntry evaluateComponentTypeEnablementForComponentFolder(IFolder componentFolder,
            IProgressMonitor monitor) throws InterruptedException {
        monitorCheck(monitor);

        // get enabled object types to compare against
        String[] enabledComponentTypes = null;
        try {
            enabledComponentTypes = getEnabledComponentTypes(getRefactorModel().getDestinationProject(), monitor);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unable to get enabled object types for project '"
                    + getRefactorModel().getDestinationProject() + "'");
        }

        if (Utils.isEmpty(enabledComponentTypes)) {
            logger.warn("Unable to get enabled object types for project '" + getRefactorModel().getDestinationProject()
                    + "'");
            return null;
        }

        List<IFolder> componentFolders = new ArrayList<>();
        componentFolders.add(componentFolder);

        return evaluateComponentTypeEnablement(enabledComponentTypes, componentFolders, monitor);
    }

    // evaluate permissions on change components - per package folder
    protected RefactoringStatusEntry evaluateComponentTypeEnablementForSourceFolder(IFolder sourceFolder,
            IProgressMonitor monitor) throws CoreException, InterruptedException {
        monitorCheck(monitor);

        // get enabled object types to compare against
        String[] enabledComponentTypes = null;
        try {
            enabledComponentTypes = getEnabledComponentTypes(getRefactorModel().getDestinationProject(), monitor);
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unable to get enabled object types for project '"
                    + getRefactorModel().getDestinationProject() + "'");
        }

        if (Utils.isEmpty(enabledComponentTypes)) {
            logger.warn("Unable to get enabled object types for project '" + getRefactorModel().getDestinationProject()
                    + "'");
            return null;
        }

        monitorCheck(monitor);

        List<IFolder> componentFolders = ContainerDelegate.getInstance().getServiceLocator().getProjectService().getComponentFolders(sourceFolder);

        if (Utils.isEmpty(componentFolders)) {
            return null;
        }

        return evaluateComponentTypeEnablement(enabledComponentTypes, componentFolders, monitor);
    }

    // evaluate permissions on change components - per folder list
    protected RefactoringStatusEntry evaluateComponentTypeEnablement(String[] enabledComponentTypes,
            List<IFolder> componentFolders, IProgressMonitor monitor) throws InterruptedException {
        if (Utils.isEmpty(enabledComponentTypes) || Utils.isEmpty(componentFolders)) {
            return null;
        }

        monitorCheck(monitor);

        List<IFolder> restrictedComponentFolders = null;
        for (IFolder componentFolder : componentFolders) {
            String folderName = componentFolder.getName();
            boolean areEnabled = false;
            for (String enabledComponentType : enabledComponentTypes) {
                Component component = null;
                component = ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getComponentByComponentType(enabledComponentType);

                if (component == null) {
                    logger.warn("Unable to get component for object type '" + enabledComponentType + "'");
                    continue;
                }

                if (component.getDefaultFolder().equals(folderName)) {
                    areEnabled = true;
                    break;
                }
            }

            if (!areEnabled) {
                if (restrictedComponentFolders == null) {
                    restrictedComponentFolders = new ArrayList<>();
                }
                restrictedComponentFolders.add(componentFolder);
            }
            monitorCheck(monitor);
        }

        if (null != restrictedComponentFolders && !restrictedComponentFolders.isEmpty()) {
            StringBuffer strBuff = new StringBuffer();
            for (IFolder restrictedComponentFolder : restrictedComponentFolders) {
                strBuff.append("'").append(restrictedComponentFolder.getName()).append("' ");
            }
            logger.error("Object type(s) for folder(s) " + strBuff.toString() + "are not enabled for project '"
                    + getRefactorModel().getDestinationProject() + "'");
            return createFatalRefactoringStatusEntry("Object type(s) for folder(s) " + strBuff.toString()
                    + "are not enabled for project '" + getRefactorModel().getDestinationProject() + "'");
        }

        return null;
    }

    // TODO: if resource is a folder, allow, but filter out candidates that are existing in destination
    // TODO: check remotely
    protected boolean isNameUnique(IResource moveResource, IProgressMonitor monitor) throws InterruptedException {
        boolean unique = isNameUniqueLocalCheck(moveResource, monitor);
        if (unique) {
            unique = isNameUniqueRemoteCheck(moveResource, monitor);
        }

        return unique;
    }

    protected boolean isNameUniqueLocalCheck(IResource moveResource, IProgressMonitor monitor)
            throws InterruptedException {
        monitorCheck(monitor);
        String moveResourceName = moveResource.getName();
        IProject project = getRefactorModel().getDestinationProject();
        String destinationPath = getRefactorModel().getDestinationPath();
        IResource testResource = project.findMember(destinationPath + "/" + moveResourceName);
        if (logger.isDebugEnabled()) {
            logger.debug("Resource '" + destinationPath + "/" + moveResourceName + "' "
                    + (testResource != null && testResource.exists() ? "exists" : "does not exist") + " in project '"
                    + project.getName() + "'");
        }

        return !(testResource != null && testResource.exists());
    }

    // TODO
    protected boolean isNameUniqueRemoteCheck(IResource moveResource, IProgressMonitor monitor)
            throws InterruptedException {
        monitorCheck(monitor);
        logger.warn("TODO: check server for unique name");
        return true;
    }

    //   P E R F O R M   O P E R A T I O N S
    /**
     * Operation to execute move based on given resource and project package contents.
     *
     * @param changeElements
     * @param destinationResource
     * @param monitor
     * @throws InterruptedException
     */
    public void performMove(IProgressMonitor monitor) throws OperationCanceledException {
    }

    @Override
    public void finish(IProgressMonitor monitor) throws OperationCanceledException, FactoryException,
            ForceProjectException, InterruptedException, InvocationTargetException, Exception {
        performCopy(monitor);
    }

    /**
     * Operation to execute copy based on given resource and project package contents.
     *
     * @param changeElements
     * @param destinationResource
     * @param monitor
     * @throws Exception
     */
    protected void performCopy(IProgressMonitor monitor) throws OperationCanceledException, Exception {
        // get stored project package list containing package(s) and component(s) to delete
        monitorCheck(monitor);

        // get stored project package list containing package(s) and component(s) to copy
        // currently, perform copy only supports processing of one candidate per time
        ProjectPackageList projectPackageList = refactorModel.getProjectPackageList();
        if (Utils.isEmpty(projectPackageList)) {
            logger.warn("Cannot perform change - project package list is null");
            throw new ForceProjectException("Cannot perform change - project package list is null");
        }

        // create new project package for destination
        ProjectPackage projectPackage =
                ContainerDelegate.getInstance().getServiceLocator().getProjectService().getProjectPackageFactory().getProjectPackage(getRefactorModel().getDestinationProject(), true);

        if (projectPackage == null) {
            throw new ForceProjectException("Cannot determine new package name");
        }

        monitorCheck(monitor);

        // add each component to new project package
        ComponentList componentList = projectPackageList.getAllComponents();
        for (Component component : componentList) {
            if (component.isPackageManifest()) {
                continue;
            }

            String origPackageName = component.getPackageName();
            String metadataFilePath = component.getMetadataFilePath();
            metadataFilePath = metadataFilePath.replace(origPackageName, projectPackage.getName());
            component.setMetadataFilePath(metadataFilePath);
            component.setPackageName(projectPackage.getName());
            projectPackage.addComponent(component);
        }

        // add to list
        final ProjectPackageList destinationProjectPackageList = projectPackageList.getProjectPackageListInstance();
        destinationProjectPackageList.add(projectPackage);
        destinationProjectPackageList.setProject(getRefactorModel().getDestinationProject());

        monitorCheck(monitor);

        if (logger.isDebugEnabled()) {
            logger.debug("Copying the following components:\n" + projectPackageList.getAllComponents().toStringLite());
        }

        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    try {
                        destinationProjectPackageList.saveResources(new SubProgressMonitor(monitor, 3));
                    } catch (Exception e) {
                        throw new CoreException(new Status(IStatus.ERROR, Constants.FORCE_PLUGIN_PREFIX, 0, e
                                .getMessage(), e));
                    }
                }
            }, null, IResource.NONE, monitor);
        } catch (CoreException e) {
            if ((Exception) e.getCause() instanceof InterruptedException) {
                throw new OperationCanceledException(((Exception) e.getCause()).getMessage());
            }
			throw (Exception) e.getCause();

        }
    }
}
