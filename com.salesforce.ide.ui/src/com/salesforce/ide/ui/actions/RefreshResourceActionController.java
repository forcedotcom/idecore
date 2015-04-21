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
package com.salesforce.ide.ui.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.core.factories.FactoryException;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.MessageDialogRunnable;
import com.salesforce.ide.core.internal.utils.Messages;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ProjectPackageList;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.ForceConnectionException;
import com.salesforce.ide.core.remote.ForceRemoteException;
import com.salesforce.ide.core.remote.metadata.RetrieveResultExt;
import com.salesforce.ide.core.services.ServiceException;
import com.salesforce.ide.core.services.ServiceTimeoutException;

public class RefreshResourceActionController extends ActionController {
    private static final Logger logger = Logger.getLogger(RefreshResourceActionController.class);

    private static final String OPERATION = "refresh resource(s)";
    private boolean refreshResult = true;

    //   C O N S T R U C T O R S
    public RefreshResourceActionController() {
        super();
    }

    //   M E T H O D S
    @Override
    public WizardDialog getWizardDialog() {
        return null;
    }

    @Override
    public boolean preRun() {
        if (Utils.isEmpty(selectedResources)) {
            logger.info("Operation cancelled.  Resources not provided.");
            return false;
        }

        // skip if not a force managed resource
        if (!getProjectService().isManagedResources(selectedResources)) {
            Utils.openError("Not Managed Resource", "Unable to refresh resource '" + getSelectedResource().getName()
                    + "'.  Resource is not a " + Constants.PRODUCT_NAME + " resource.");
            return false;
        }

        for (IResource selectedResource : selectedResources) {
            // if dirty, ask user what he/she wants to do
            if (checkForDirtyResource(selectedResource)) {
                StringBuffer strBuffer = new StringBuffer("One or more of the resources or their child resources ");
                strBuffer.append("you are trying to refresh is open, unsaved, and/or dirty.\n\n").append(
                    "Continue to refresh this resource replacing it with the latest from Salesforce?");
                boolean result = Utils.openQuestion("Confirm Refresh Open File", strBuffer.toString());
                if (!result) {
                    if (logger.isInfoEnabled()) {
                        logger.info("User cancelled.  File is open for edit or dirty.");
                    }
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Refresh given resources fetching latest instance(s) from org and saving to project
     * 
     * @param monitor
     * @return
     * @throws InterruptedException
     * @throws ForceConnectionException
     * @throws FactoryException
     * @throws CoreException
     * @throws IOException
     * @throws ForceRemoteException
     * @throws InvocationTargetException
     * @throws ServiceException
     * @throws MetadataServiceException
     */
    public boolean refreshResources(IProgressMonitor monitor) throws InterruptedException, ForceConnectionException,
            FactoryException, CoreException, IOException, ForceRemoteException, InvocationTargetException,
            ServiceException, Exception {

        if (Utils.isEmpty(selectedResources)) {
            logger.warn("Unable to refresh resources - resources is null or empty");
            return false;
        }

        if (logger.isDebugEnabled()) {
            StringBuffer strBuffer =
                    new StringBuffer("Refreshing the following resource roots [" + selectedResources.size() + "]:");
            int resourceCnt = 0;
            for (IResource resource : selectedResources) {
                strBuffer.append("\n (").append(++resourceCnt).append(") ").append(
                    resource.getProjectRelativePath().toPortableString());
            }
            logger.debug(strBuffer.toString());
        }

        monitorCheck(monitor);
        monitorSubTask(monitor, "Retrieving remote components...");

        monitorCheck(monitor);
        // refresh at project root - fetch and save everything and be done w/ it
        if (Utils.isNotEmpty(getProjectService().getResourcesByType(selectedResources, IResource.PROJECT))) {
            IFolder folder = getProjectService().getSourceFolder(project);
            if (!refreshSourceFolder(folder, monitor)) {
                failRefreshResult();
            }

            // must fetch separately due to returned package structure - referenced packages
            // maintains the package-prefixed hierarchy
            if (!retrieveInstalledPackages(project, monitor)) {
                failRefreshResult();
            }

            // project level refresh takes care of everything
            return refreshResult;
        }

        // handle if source root was selected
        handleSourceRefresh(monitor);

        // if only source root was selected was selected, let's end here
        if (selectedResources.size() == 1
                && Utils.isNotEmpty(getProjectService().getFolder(selectedResources, Constants.SOURCE_FOLDER_NAME))) {
            return refreshResult;
        }

        // handle source component folders
        handleSourceComponentFolderRefresh(monitor);

        // handle source component files
        handleSourceComponentFileRefresh(monitor);

        // handle if referenced packages dir was selected
        handleReferencePackagesRefresh(monitor);

        // handle if individual/multiple reference package folder was selected
        handleReferencePackageFolderRefresh(monitor);

        if (logger.isDebugEnabled()) {
            logger.debug("Overall refresh process was" + (refreshResult ? " " : " NOT") + " successful");
        }

        return refreshResult;
    }

    protected void handleSourceRefresh(IProgressMonitor monitor) throws InterruptedException, ForceConnectionException,
            ForceRemoteException, FactoryException, CoreException, IOException, InvocationTargetException,
            ServiceException, Exception {
        monitorCheck(monitor);
        if (Utils.isNotEmpty(getProjectService().getFolder(selectedResources, Constants.SOURCE_FOLDER_NAME))) {
            IFolder sourceFolder = getProjectService().getSourceFolder(project);
            if (!refreshSourceFolder(sourceFolder, monitor)) {
                logger.warn("Unable to refresh source root");
                failRefreshResult();
            }
        }
    }

    // refresh folder
    private boolean refreshSourceFolder(IFolder folder, IProgressMonitor monitor) throws FactoryException,
            ForceConnectionException, CoreException, InterruptedException, IOException, ForceRemoteException,
            ServiceException, Exception {
        if (folder == null || !folder.exists()) {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Refreshing folder '" + folder.getProjectRelativePath().toPortableString() + "' resource");
        }

        String packageName = getProjectService().getPackageName(project);

        // perform retrieve
        RetrieveResultExt retrieveResultHandler = null;
        try {
            retrieveResultHandler =
                    getServiceLocator().getPackageRetrieveService().retrievePackage(project, packageName, monitor);
        } catch (ServiceTimeoutException ex) {
            retrieveResultHandler =
                getServiceLocator().getPackageRetrieveService().handleRetrieveServiceTimeoutException(ex, OPERATION,
                        monitor);
        }

        if (retrieveResultHandler == null) {
            logger.warn("Unable to refresh folder '" + folder.getName() + "' - retrieve result is null");
            return false;
        }

        monitorWork(monitor);

        ProjectPackageList projectPackageList =
                getProjectPackageFactory().getProjectPackageListInstance(new String[] { packageName });
        projectPackageList.setProject(project);

        // handle result including messages and saving resources
        return getProjectService().handleRetrieveResult(projectPackageList, retrieveResultHandler, true, null, monitor);
    }

    protected void handleReferencePackagesRefresh(IProgressMonitor monitor) throws InterruptedException,
            ForceConnectionException, ForceRemoteException, FactoryException, CoreException, IOException,
            InvocationTargetException, ServiceException, Exception {
        monitorCheck(monitor);

        IFolder referencePkgFolder =
                getProjectService().getFolder(selectedResources, Constants.REFERENCED_PACKAGE_FOLDER_NAME);
        if (referencePkgFolder != null) {
            if (!retrieveInstalledPackages(project, monitor)) {
                logger.warn("Unable to refresh installed packages");
                failRefreshResult();
            }
        }
    }

    protected void handleReferencePackageFolderRefresh(IProgressMonitor monitor) throws InterruptedException,
            ForceConnectionException, ForceRemoteException, FactoryException, CoreException, ServiceException,
            IOException, InvocationTargetException, Exception {
        monitorCheck(monitor);
        List<IResource> folders = getProjectService().getResourcesByType(selectedResources, IResource.FOLDER);
        List<String> pkgNames = new ArrayList<>();
        if (Utils.isNotEmpty(folders)) {
            for (IResource folder : folders) {
                if (getProjectService().isReferencedPackageFolder(folder)) {
                    // set reference pkg folder contents to readonly=false so save of retrieve content
                    // doesn't prompt with 'unable to write'
                    Utils.adjustResourceReadOnly(folder, false, true);

                    pkgNames.add(folder.getName());
                }
            }

            if (Utils.isNotEmpty(pkgNames)) {
                RetrieveResultExt retrieveResultHandler = null;
                try {
                    retrieveResultHandler =
                        getServiceLocator().getPackageRetrieveService().retrieveInstalledPackages(project,
                                pkgNames.toArray(new String[pkgNames.size()]), monitor);
                    retrieveResultHandler.getProjectPackageList().setProject(project);
                } catch (ServiceTimeoutException ex) {
                    retrieveResultHandler =
                        getServiceLocator().getPackageRetrieveService().handleRetrieveServiceTimeoutException(ex,
                                OPERATION, monitor);
                    if (retrieveResultHandler != null) {
                        ProjectPackageList projectPackageList =
                                getProjectPackageFactory().getProjectPackageListInstance(
                                    pkgNames.toArray(new String[pkgNames.size()]));
                        if (projectPackageList != null) {
                            projectPackageList.setProject(project);
                        }
                        retrieveResultHandler.setProjectPackageList(projectPackageList);
                    }
                }

                if (retrieveResultHandler == null
                        || !getProjectService().handleRetrieveResult(retrieveResultHandler, true, monitor)) {
                    logger.warn("Unable to refresh component folders");
                    failRefreshResult();
                }
            }
        }
    }

    protected boolean retrieveInstalledPackages(IProject project, IProgressMonitor monitor)
            throws InterruptedException, ForceConnectionException, ForceRemoteException, CoreException, IOException, ServiceException, Exception {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Fetching and saving all installed, managed components for '" + project.getName() + "'");
        }

        // set reference pkg folder contents to readonly=false so save of retrieve content
        // doesn't prompt with 'unable to write'
        IFolder referencePkgFolder = getProjectService().getReferencedPackagesFolder(project);
        Utils.adjustResourceReadOnly(referencePkgFolder, false, true);

        monitorCheck(monitor);
        RetrieveResultExt retrieveResultHandler = null;
        try {
            retrieveResultHandler =
                getServiceLocator().getPackageRetrieveService().retrieveManagedInstalledPackages(project, monitor);
        } catch (ServiceTimeoutException ex) {
            retrieveResultHandler =
                getServiceLocator().getPackageRetrieveService().handleRetrieveServiceTimeoutException(ex, OPERATION,
                        monitor);
            if (retrieveResultHandler != null) {
                Connection connection = getConnectionFactory().getConnection(project);
                ProjectPackageList projectPackageList =
                        getProjectPackageFactory().getManagedInstalledProjectPackages(connection);
                if (projectPackageList != null) {
                    projectPackageList.setProject(project);
                }
                retrieveResultHandler.setProjectPackageList(projectPackageList);
            }
        }

        if (retrieveResultHandler == null) {
            logger.warn("Unable to refresh installed packages - retrieve result is null");
            return false;
        }

        if (retrieveResultHandler.getZipFileCount() == 0) {
            return true;
        }

        if (Utils.isNotEmpty(retrieveResultHandler.getProjectPackageList())) {
            retrieveResultHandler.getProjectPackageList().setProject(project);
        }

        monitorCheck(monitor);
        return getProjectService().handleRetrieveResult(retrieveResultHandler.getProjectPackageList(),
            retrieveResultHandler, true, null, monitor);
    }

    protected void handleSourceComponentFolderRefresh(IProgressMonitor monitor) throws InterruptedException,
            ForceConnectionException, ForceRemoteException, FactoryException, CoreException, IOException,
            ServiceException, Exception {
        monitorCheck(monitor);
        List<IResource> folders = getProjectService().getResourcesByType(selectedResources, IResource.FOLDER);
        List<String> componentTypes = new ArrayList<>();
        if (Utils.isNotEmpty(folders)) {
            for (IResource folder : folders) {
                if (getProjectService().isComponentFolder(folder)) {
                    Component component = getComponentFactory().getComponentByFolderName(folder.getName());
                    componentTypes.add(component.getComponentType());
                } else if (getProjectService().isSubComponentFolder(folder)) {
                    Component component = getComponentFactory().getComponentFromSubFolder((IFolder) folder, false);
                    componentTypes.add(component.getSecondaryComponentType());
                }
                // reference package folders is handled in methods specific for reference packages.
            }

            if (Utils.isNotEmpty(componentTypes)) {
                // Bug #206315: In package project, prompt user that refresh on component folder is not support, and
                // either cancel or refresh from src.
                Package packageManifest = getPackageManifestFactory().getPackageManifest(getProject());
                if (Utils.isEmpty(packageManifest)) {
                    throw new FactoryException(Messages.getString(
                        "Retrieve.PackageProject.RefreshComponentFolder.MissPackageManifest.Exception.message",
                        new String[] { getProject().getName() }));
                }
                if (Utils.isNotEmpty(packageManifest.getFullName())) {
                    String dialogMsg =
                            Messages.getString("Retrieve.PackageProject.RefreshComponentFolder.Dialog.message");
                    MessageDialogRunnable messageDialogRunnable =
                            new MessageDialogRunnable("Refresh from server", null, dialogMsg,
                                    MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL,
                                            IDialogConstants.CANCEL_LABEL }, 0);
                    Display.getDefault().syncExec(messageDialogRunnable);

                    if (messageDialogRunnable.getAction() == Window.CANCEL) {
                        String cancelMsg =
                                Messages.getString("Retrieve.PackageProject.RefreshComponentFolder.Cancel.message");
                        logger.info(cancelMsg);
                        throw new InterruptedException(cancelMsg);
                    }
                }

                // refresh component dirs
                ProjectPackageList projectPackageList =
                        getProjectPackageFactory().getProjectPackageListInstance(project);

                // only save these types
                String[] saveComponentTypes = componentTypes.toArray(new String[componentTypes.size()]);

                // perform retrieve
                RetrieveResultExt retrieveResultHandler = null;
                try {
                    retrieveResultHandler =
                        getServiceLocator().getPackageRetrieveService().retrieveSelective(projectPackageList,
                                saveComponentTypes, monitor);
                } catch (ServiceTimeoutException ex) {
                    retrieveResultHandler =
                        getServiceLocator().getPackageRetrieveService().handleRetrieveServiceTimeoutException(ex,
                                OPERATION, monitor);
                }

                if (retrieveResultHandler == null
                        || !getProjectService().handleRetrieveResult(projectPackageList, retrieveResultHandler, true,
                            saveComponentTypes, monitor)) {
                    logger.warn("Unable to refresh component folders");
                    failRefreshResult();
                }
            }
        }
    }

    protected void handleSourceComponentFileRefresh(IProgressMonitor monitor) throws InterruptedException,
            ForceConnectionException, ForceRemoteException, FactoryException, CoreException, IOException,
            InvocationTargetException, ServiceException, Exception {
        monitorCheck(monitor);
        List<IResource> files = getProjectService().getResourcesByType(selectedResources, IResource.FILE);
        if (Utils.isNotEmpty(files)) {
            List<IResource> sourceResources = new ArrayList<>(files.size());
            for (IResource file : files) {
                if (getProjectService().isSourceResource(file)) {
                    sourceResources.add(file);
                }
            }

            ProjectPackageList projectPackageList = getProjectService().getProjectContents(sourceResources, monitor);
            projectPackageList.setProject(project);
            monitorCheck(monitor);
            RetrieveResultExt retrieveResultHandler = null;
            try {
                retrieveResultHandler =
                    getServiceLocator().getPackageRetrieveService().retrieveSelective(projectPackageList, true, monitor);
            } catch (ServiceTimeoutException ex) {
                retrieveResultHandler =
                    getServiceLocator().getPackageRetrieveService().handleRetrieveServiceTimeoutException(ex, OPERATION,
                            monitor);
            }

            if (retrieveResultHandler == null
                    || !getProjectService().handleRetrieveResult(retrieveResultHandler, true, monitor)) {
                logger.warn("Unable to refresh component files");
                failRefreshResult();
            }
        }
    }

    @Override
    public void postRun() {}

    private boolean checkForDirtyResource(IResource resource) {
        try {
            if (checkForMarkers(resource)) {
                return true;
            }

            if (resource.getType() == IResource.FILE && checkForOpenFileWork((IFile) resource)) {
                return true;
            } else if (resource.getType() == IResource.FOLDER) {
                return checkForOpenFile((IFolder) resource);
            } else if (resource.getType() == IResource.PROJECT) {
                IFolder srcFolder = getProjectService().getSourceFolder(project);
                return checkForOpenFile(srcFolder);
            }
        } catch (CoreException e) {
            String logMessage = Utils.generateCoreExceptionLog(e);
            logger.warn("Unable to test for markers on " + resource.getProjectRelativePath().toPortableString() + ": "
                    + logMessage, e);
        }

        return false;
    }

    private boolean checkForMarkers(IResource resource) throws CoreException {
        if (resource == null || !resource.exists()) {
            return false;
        }

        IMarker[] markers = resource.findMarkers("com.salesforce.ide.core.problem", true, IResource.DEPTH_INFINITE);

        // no markers, let's go
        if (Utils.isEmpty(markers)) {
            return false;
        }

        // ignore package.xml markers - typically general warnings are applied to package.xml and the probably
        // will not overwrite it anyway
        if (markers.length == 1 && getProjectService().isPackageManifestFile(markers[0].getResource())) {
            return false;
        }

        // markers were found, let's warn
        return true;
    }

    private boolean checkForOpenFile(IFolder folder) throws CoreException {
        if (folder == null || Utils.isEmpty(folder.members())) {
            return false;
        }

        IResource[] members = folder.members();
        for (IResource resource : members) {
            if (resource.getType() == IResource.FILE && checkForOpenFileWork((IFile) resource)) {
                return true;
            } else if (resource.getType() == IResource.FOLDER) {
                return checkForOpenFile((IFolder) resource);
            }
        }

        return false;
    }

    private static boolean checkForOpenFileWork(IFile file) {
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        if (Utils.isNotEmpty(windows)) {
            // loop thru open windows to check if file is open if open, check w/ user
            for (IWorkbenchWindow window : windows) {
                IWorkbenchPage page = window.getActivePage();
                IEditorPart editor = page.getActiveEditor();

                if (editor != null && editor instanceof IFileEditorInput) {
                    IFileEditorInput fei = (IFileEditorInput) editor.getEditorInput();
                    IFile editFile = fei.getFile();
                    if (file.equals(editFile)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void failRefreshResult() {
        if (refreshResult) {
            refreshResult = false;
        }
    }
}
