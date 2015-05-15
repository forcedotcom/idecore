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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;

import com.salesforce.ide.api.metadata.types.Package;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.core.model.ComponentList;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.PackageManifestModel;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.project.ProjectModel;
import com.salesforce.ide.core.remote.Connection;
import com.salesforce.ide.core.remote.metadata.FileMetadataExt;
import com.salesforce.ide.test.common.utils.IdeOrgCache.OrgInfo;
import com.salesforce.ide.test.common.utils.IdeOrgCache.PackageInfo;
import com.sforce.soap.metadata.FileProperties;

/**
 * Project Fixture will create Eclipse workbench projects for testing. It also has utility methods to manipulate the
 * workbench project contents and the project itself. This is a singleton
 *
 * @author ssasalatti
 */
public class IdeProjectFixture {
    private static final Logger logger = Logger.getLogger(IdeProjectFixture.class);

    public static IdeProjectFixture instance = new IdeProjectFixture();

    public static IdeProjectFixture getInstance() {
        return instance;
    }

    // project cache //<projName, projInfo>
    private ConcurrentHashMap<String, ProjInfo> projCache = null;

    private IWorkspace workspace = null;

    private IdeProjectFixture() {
        projCache = new ConcurrentHashMap<String, ProjInfo>();
        workspace = ResourcesPlugin.getWorkspace();
    }

    public static class ProjInfo {
        ForceProject forceProject;
        OrgInfo orgInfo;

        public ProjInfo(ForceProject project, OrgInfo orgInfo) {
            this.forceProject = project;
            this.orgInfo = orgInfo;
        }

        public ForceProject getForceProject() {
            return forceProject;
        }

        public OrgInfo getOrgInfo() {
            return orgInfo;

        }

        @Override
        public String toString() {
            StringBuffer buff = new StringBuffer("\n-------ProjInfo:---------");
            buff.append("\nProject Name: " + forceProject.getProject().getName());
            buff.append("\n------------------------");

            return buff.toString();
        }

    }

    /**
     * Creates a project and updates the cache.
     *
     * @param orgType
     * @throws IdeTestException
     */
    public ProjInfo createProject(OrgTypeEnum orgType) throws IdeTestException {

        OrgInfo orgInfo = IdeTestOrgFactory.getOrgFixture().getOrg(orgType);
        return createProject(orgInfo);
    }

    /**
     * Creates a project and updates the cache. If you are using this method and the orgInfo you're sending is different
     * from what the annotation will use( i.e the orgType in this orgInfo is different from the one in the annotation) ,
     * you are responsible for deleting the project after you're done. Creates a project and gets everything
     *
     * @param orgInfo
     * @throws IdeTestException
     */
    public ProjInfo createProject(OrgInfo orgInfo) throws IdeTestException {
        return createProject(orgInfo, IdeProjectContentTypeEnum.ALL, null, null, null);

    }

    /**
     * Creates a project and updates the cache. Use for selective project create. - If project content selection is
     * SINGLE_PACKAGE or SPECIFIC_COMPONENTS only then do pkgNameToCreateProjectAgainst and
     * componentTypesForProjectCreate matter respectively. If project content selection is ONLY_WHAT_IS_UPLOADED only
     * then does packageManifest Matter
     *
     * @param orgInfo
     * @param projectContentSelection
     * @param componentTypesForProjectCreate
     * @param pkgNameToCreateProjectAgainst
     * @param packageManifest
     * @return
     * @throws IdeTestException
     */
    public ProjInfo createProject(OrgInfo orgInfo, IdeProjectContentTypeEnum projectContentSelection,
            ComponentTypeEnum[] componentTypesForProjectCreate, String pkgNameToCreateProjectAgainst,
            Package packageManifest) throws IdeTestException {
        ProjInfo tempPrjInfo =
                createProjectWorker(orgInfo, projectContentSelection, componentTypesForProjectCreate,
                    pkgNameToCreateProjectAgainst, packageManifest);
        return checkIfProjectExistsInWorkspace(tempPrjInfo.getForceProject().getProject().getName()) ? tempPrjInfo
                : null;

    }

    /**
     * imports the project from the given path and adds the project to the cache.
     *
     * @param orgInfo
     * @param relPath
     * @throws IdeTestException
     */
    public void importProjectFromFS(OrgInfo orgInfo, String relPath) throws IdeTestException {
        try {
            relPath = IdeTestUtil.convertToOSSpecificPath(relPath);
            URL projectLocationURL = IdeTestUtil.getFullUrlEntry(relPath);
            String projectPath = projectLocationURL.getPath();

            // copy the project to your current workspace.
            String workspaceLocation = workspace.getRoot().getLocation().toOSString();
            IdeTestUtil.copyFilesToDirRecursively(new File(workspaceLocation), new File(projectPath));

            // load the project description.
            String projectName =
                    workspace.loadProjectDescription(new Path(projectLocationURL.getPath() + ".project")).getName();

            // get to the .project file.
            Path pathToProjectFile =
                    new Path(workspaceLocation + File.separator + projectName + File.separator + ".project");
            final IProjectDescription projectDescription = workspace.loadProjectDescription(pathToProjectFile);

            ForceProject forceProject = orgInfo.getWrapperForceProject();

            // ----import the project.
            final IProject project = workspace.getRoot().getProject(projectDescription.getName());

            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    project.create(projectDescription, monitor);
                    project.open(IResource.NONE, monitor);
                }
            };
            // Only import the project if it doesn't appear to already exist. If
            // it looks like it
            // exists, tell the user about it.
            if (project.exists()) {
                logger.info("Project /cannedProjects/" + projectName + " already exists");
            } else
                workspace.run(runnable, workspace.getRuleFactory().modifyRule(workspace.getRoot()), IResource.NONE,
                    null);

            // need to some more work after importing the project so that the
            // future connections and server communications et. al. will work.
            ProjectModel model = new ProjectModel(forceProject);
            model.setEnvironment(forceProject.getEndpointEnvironment());
            model.setProjectName(projectName);

            Connection connection = IdeTestUtil.getConnectionFactory().getConnection(forceProject);
            model.setConnection(connection);
            // set the contents to custom components and the package manifest as
            // in the project.
            model.setContentSelection(ProjectController.CUSTOM_COMPONENTS);

            // need to set the packagemanifest model.
            URL packageManifestFileURL =
                    IdeTestUtil.getFullUrlEntry(relPath + File.separator + Constants.SOURCE_FOLDER_NAME
                            + File.separator + Constants.PACKAGE_MANIFEST_FILE_NAME);
            Document packageManifestDocument = Utils.loadDocument(packageManifestFileURL);
            PackageManifestModel packageManifestModel = new PackageManifestModel(packageManifestDocument);
            Package packageManifest =
                    IdeTestUtil.getPackageManifestFactory().createPackageManifest(packageManifestDocument);
            packageManifestModel.setPackageManifest(packageManifest);
            model.setPackageManifestModel(packageManifestModel);
            // set the project.
            model.setProject(project);

            final ProjectController projectController = IdeTestUtil.getProjectController();
            projectController.setModel(model);
            projectController.saveConnection(new NullProgressMonitor());
            projectController.saveSettings(new NullProgressMonitor());
            // --create the project.
            // WorkspaceModifyOperation projectCreateOperation = new
            // WorkspaceModifyOperation() {
            // @Override
            // protected void execute(IProgressMonitor monitor) throws
            // CoreException, InvocationTargetException,
            // InterruptedException {
            // try {
            // projectController.finish(monitor);
            // } catch (InterruptedException e) {
            // throw e;
            // } catch (Exception e) {
            // throw new InvocationTargetException(e);
            // }
            // }
            // };
            // projectCreateOperation.run(new NullProgressMonitor());
            // IProject project = model.getProject();
            // logProjectContents(project);
            // //set back some of the preferences. esp the ide version.
            // forceProject.setIdeVersion(ideVersion);
            // IdeTestUtil.getProjectService().saveForceProject(forceProject);

            // update forceProject
            forceProject.setProject(project);
            ProjInfo projInfo = new ProjInfo(forceProject, orgInfo);

            IdeTestUtil.logProjectContents(project);

            // add project to cache.
            projCache.put(project.getName(), projInfo);

        } catch (Exception e) {
            throw IdeTestException.getWrappedException("Unable to complete project import", e);
        }
    }

    /**
     * Creates a project and updates the cache. Use for selective project create. - If project content selection is
     * SINGLE_PACKAGE or SPECIFIC_COMPONENTS, only then do pkgNameToCreateProjectAgainst and
     * componentTypesForProjectCreate matter respectively.
     *
     * @param orgType
     * @param projectContentSelection
     * @param componentTypesForProjectCreate
     * @param pkgNameToCreateProjectAgainst
     * @param packageManifest
     * @return
     * @throws IdeTestException
     */
    public ProjInfo createProject(OrgTypeEnum orgType, IdeProjectContentTypeEnum projectContentSelection,
            ComponentTypeEnum[] componentTypesForProjectCreate, String pkgNameToCreateProjectAgainst,
            Package packageManifest) throws IdeTestException {
        OrgInfo orgInfo = IdeTestOrgFactory.getOrgFixture().getOrg(orgType);
        return createProject(orgInfo, projectContentSelection, componentTypesForProjectCreate,
            pkgNameToCreateProjectAgainst, packageManifest);

    }

    /**
     * deletes all projects that have been created against this type of org.Checks the org cache for this. removes
     * project entry from project Cache
     *
     * @param orgType
     * @throws IdeTestException
     */
    public boolean deleteAllProjectsFromWorkspace(OrgTypeEnum orgType) throws IdeTestException {
        OrgInfo orgInfo = IdeTestOrgFactory.getOrgFixture().getOrgCacheInstance().getOrgInfoFromCache(orgType);
        return deleteAllProjectsFromWorkspace(orgInfo);

    }

    /**
     * deletes all projects that have been created against this org. checks org cache for this org. removes project
     * entry from project cache
     *
     * @param orgInfo
     * @throws IdeTestException
     * @return true if project was deleted. if multiple projects were for this org info, it returns true even if a
     *         single one was deleted.
     */

    public boolean deleteAllProjectsFromWorkspace(OrgInfo orgInfo) throws IdeTestException {
        return deleteProjectWorker(orgInfo);

    }

    /**
     * Deletes a project from the workspace. Does not update the project cache! Can be used when the project was created
     * outside of test setup. For example while testing project wizard
     *
     * @param projectName
     * @return true if project was found and deleted.
     * @throws CoreException
     * @throws IdeTestException
     *             if project not found
     */
    public boolean deleteProjectFromWorkspace(String projectName) throws IdeTestException {
        if (IdeTestUtil.isEmpty(projectName))
            throw IdeTestException.getWrappedException("Project name cannot be empty");
        if (!checkIfProjectExistsInWorkspace(projectName))
            throw IdeTestException.getWrappedException("Project not found for Deletion");

        IProject project = workspace.getRoot().getProject(projectName);

        try {
            project.delete(true, true, new NullProgressMonitor());
        } catch (CoreException e) {
            throw IdeTestException.getWrappedException("Couldn't delete project. Core Exception", e);
        }

        return !checkIfProjectExistsInWorkspace(projectName);
    }

    /**
     * CAREFUL! Purges all projects from the workspace. Deletes content also. Does not update the project cache! Do not
     * use if your projects are being maintained in the cache. 'cos it will delete the projects but since cache won't be
     * updated, test teardown will complain that projects still exist as it will check the proj cache.
     *
     * @throws IdeTestException
     *
     */
    public void purgeAllProjectsFromWorkspace() throws IdeTestException {

        try {
            IProject[] projects = workspace.getRoot().getProjects();
            Assert.assertNotNull(projects);
            for (IProject p : projects) {
                p.delete(true, true, new NullProgressMonitor());
            }

            Assert.assertEquals("looks like workspace wasn't cleaned", 0, workspace.getRoot().getProjects().length);
        } catch (CoreException e) {
            throw IdeTestException.getWrappedException("Core Exception while purging all projects from workspace.");
        }
    }

    /**
     * Checks if project exists in Workspace or not.
     *
     * @param projectName
     * @return true if exists
     * @throws IdeTestException
     */
    public boolean checkIfProjectExistsInWorkspace(String projectName) throws IdeTestException {
        if (IdeTestUtil.isEmpty(projectName))
            throw IdeTestException.getWrappedException("Project name cannot be empty");
        boolean exists = false;
        IProject[] projects = workspace.getRoot().getProjects();
        Assert.assertNotNull(projects);
        for (IProject p : projects) {
            if (p.getName().equalsIgnoreCase(projectName)) {
                exists = true;
                break;
            }
        }

        return exists;
    }

    /**
     * tries to find a project in the workspace
     *
     * @param projectName
     * @return project if found else null
     * @throws IdeTestException
     */
    public IProject findProjectInWorkspace(String projectName) throws IdeTestException {
        if (IdeTestUtil.isEmpty(projectName))
            throw IdeTestException.getWrappedException("Project name cannot be empty");
        IProject foundProject = null;
        IProject[] projects = workspace.getRoot().getProjects();
        Assert.assertNotNull(projects);
        for (IProject p : projects) {
            if (p.getName().equalsIgnoreCase(projectName)) {
                foundProject = p;
            }
        }

        return foundProject;
    }

    public boolean checkIfProjectCacheClean() {
        return projCache.isEmpty();
    }

    public ConcurrentHashMap<String, ProjInfo> getProjCache() {
        return projCache;
    }

    public void flushProjCache() {
        projCache.clear();
    }

    /**
     * returns the project info for the project created against a give org type. if multiple projects were found,
     * returns the first one. Usually only one will exist if project was setup up in test setup. if user created another
     * one specifically then he'll obviously have the projInfo for it.
     *
     * @param orgInfo
     * @return null if projInfo not found.
     * @throws IdeTestException
     */
    public ProjInfo getProjectInfoForOrgType(OrgInfo orgInfo) throws IdeTestException {
        if (Utils.isEmpty(orgInfo))
            return null;
        Collection<ProjInfo> projects = projCache.values();
        Iterator<ProjInfo> itr = projects.iterator();
        ProjInfo retProj = null;
        while (itr.hasNext()) {
            ProjInfo temp = itr.next();
            if (temp.getOrgInfo().equals(orgInfo)) {
                retProj = temp;
                break;
            }
        }
        return Utils.isEmpty(retProj) ? null : retProj;
    }

    /**
     * Refreshes the workspace file with the changes made to the file out of the workspace
     *
     * @throws IdeTestException
     */
    public void refreshResource(IResource resource) throws IdeTestException {
        try {
            resource.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            throw IdeTestException.getWrappedException("Couldn't refresh the resource " + resource.getName(), e);
        }
    }

    /**
     * refreshes the workspace
     *
     * @throws IdeTestException
     */
    public void refreshWorkspace() throws IdeTestException {
        try {
            workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            throw IdeTestException.getWrappedException("Couldn't refresh workspace", e);
        }
    }

    /**
     * given a project name, lists all the members under it.Does not include the Iproject
     *
     * @param projectName
     * @param includeHiddenMembers
     * @return
     * @throws IdeTestException
     */
    public List<String> listProjectMembers(final String projectName, final boolean includeHiddenMembers)
            throws IdeTestException {
        List<IResource> projectMembers = getProjectMembers(projectName, includeHiddenMembers);
        List<String> memberNames = new ArrayList<String>();
        for (IResource pm : projectMembers)
            memberNames.add(pm.getName());
        return Collections.unmodifiableList(memberNames);
    }

    /**
     * lists all the project members given an iproject
     *
     * @param iproject
     * @param includeHiddenMembers
     * @return
     * @throws IdeTestException
     */
    public List<String> listProjectMembers(final IProject iproject, final boolean includeHiddenMembers)
            throws IdeTestException {
        List<IResource> projectMembers = getProjectMembers(iproject, includeHiddenMembers);
        List<String> memberNames = new ArrayList<String>();
        for (IResource pm : projectMembers)
            memberNames.add(pm.getName());
        return Collections.unmodifiableList(memberNames);
    }

    /**
     * Lists all the folder members.
     *
     * @param ifolder
     * @param includeHiddenMembers
     * @return
     * @throws IdeTestException
     */
    public List<String> listFolderMembers(final IFolder ifolder, final boolean includeHiddenMembers)
            throws IdeTestException {
        List<IResource> projectMembers = getFolderMembers(ifolder, includeHiddenMembers);
        List<String> memberNames = new ArrayList<String>();
        for (IResource pm : projectMembers)
            memberNames.add(pm.getName());
        return memberNames;
    }

    /**
     * Given a project name, returns all the members within it, including files and folders, Note: Doesn't include the
     * IProject itself.
     *
     * @param projectName
     * @return
     * @throws IdeTestException
     */
    public List<IResource> getProjectMembers(String projectName, boolean includeHiddenMembers) throws IdeTestException {
        if (IdeTestUtil.isEmpty(projectName))
            throw IdeTestException.getWrappedException("Project name cannot be empty");

        if (!IdeProjectFixture.getInstance().checkIfProjectExistsInWorkspace(projectName))
            throw IdeTestException.getWrappedException("No Project found with project name: " + projectName);
        return getProjectMembers(IdeProjectFixture.getInstance().getWorkspace().getRoot().getProject(projectName),
            includeHiddenMembers);

    }

    /**
     * given an IProject, returns all members contained in the project. Note: doesn't include the iproject itself.
     *
     * @param iproject
     * @return
     * @throws IdeTestException
     */
    public List<IResource> getProjectMembers(IProject iproject, boolean includeHiddenMembers) throws IdeTestException {
        if (IdeTestUtil.isEmpty(iproject))
            throw IdeTestException.getWrappedException("IProject cannot be empty");

        if (!IdeProjectFixture.getInstance().checkIfProjectExistsInWorkspace(iproject.getName()))
            throw IdeTestException.getWrappedException("No Project found with project name: " + iproject.getName());
        List<IResource> list = new ArrayList<IResource>();
        try {
            list.addAll(Arrays.asList(getProjectMembersWorker(iproject.members(), includeHiddenMembers)));
        } catch (CoreException e) {
            throw IdeTestException.getWrappedException(
                "Could not get Project member in project: " + iproject.getName(), e);
        }
        return list;

    }

    /**
     * Given an iFolder object, returns all members contained in the folder.recursively goes through all of em.
     *
     * @param ifolder
     * @param includeHiddenMembers
     * @return
     * @throws IdeTestException
     */
    public List<IResource> getFolderMembers(IFolder ifolder, boolean includeHiddenMembers) throws IdeTestException {
        if (IdeTestUtil.isEmpty(ifolder))
            throw IdeTestException.getWrappedException("Ifolder passed in cannot be empty");

        List<IResource> list = new ArrayList<IResource>();
        try {
            list.addAll(Arrays.asList(getProjectMembersWorker(ifolder.members(), includeHiddenMembers)));
        } catch (CoreException e) {
            throw IdeTestException.getWrappedException("Could not get folder members in folder: " + ifolder.getName(),
                e);
        }
        return list;

    }

    /**
     * Tries to find an file in the project.
     *
     * @param project
     * @param resourceName
     * @return the file if found else null
     * @throws IdeTestException
     */
    public IFile findFileInProject(IProject project, String resourceName) throws IdeTestException {
        if (IdeTestUtil.isEmpty(project))
            throw IdeTestException.getWrappedException("IProject cannot be empty");
        if (IdeTestUtil.isEmpty(resourceName))
            throw IdeTestException.getWrappedException("resource name cannot be empty");

        IResource resource;
        try {
            resource = findResourceInProject(project.members(), resourceName);
            return resource instanceof IFile ? (IFile) resource : null;
        } catch (CoreException e) {
            throw IdeTestException.getWrappedException("Core Exception while trying to find a file: " + resourceName
                    + " in the project " + project.getName(), e);
        }
    }

    /**
     * Tries to find an folder in the project.
     *
     * @param project
     * @param resourceName
     * @return the file if found else null
     * @throws IdeTestException
     */
    public IFolder findFolderInProject(IProject project, String resourceName) throws IdeTestException {
        if (IdeTestUtil.isEmpty(project))
            throw IdeTestException.getWrappedException("IProject cannot be empty");
        if (IdeTestUtil.isEmpty(resourceName))
            throw IdeTestException.getWrappedException("resource name cannot be empty");

        IResource resource;
        try {
            resource = findResourceInProject(project.members(), resourceName);
            return resource instanceof IFolder ? (IFolder) resource : null;
        } catch (CoreException e) {
            throw IdeTestException.getWrappedException("Core Exception while trying to find a file: " + resourceName
                    + " in the project " + project.getName(), e);
        }
    }

    /**
     * recursively find a resource a project. becuase this is a recursive function, it expects an array.
     *
     * @param projMembers
     * @param resourceName
     * @return
     * @throws IdeTestException
     * @throws CoreException
     */
    public IResource findResourceInProject(IResource[] projMembers, String resourceName) throws IdeTestException {
        if (IdeTestUtil.isEmpty(resourceName))
            throw IdeTestException.getWrappedException("resource name cannot be empty");

        for (IResource member : projMembers) {
            if (member.getName().equalsIgnoreCase(resourceName)) {
                return member;
            }
        }
        try {
            for (IResource member : projMembers) {
                if (member instanceof IFolder) {
                    IResource returnedResource;

                    returnedResource = findResourceInProject(((IFolder) member).members(), resourceName);

                    if (null != returnedResource)
                        return returnedResource;
                }
            }
        } catch (CoreException e) {
            throw IdeTestException
                    .getWrappedException("Core Exception while trying to find a resource in a project", e);
        }
        return null;

    }

    /**
     * Force deletes a resource in the workspace.
     *
     * @param project
     * @param cType
     * @param fileNameForCreatedComponent
     * @throws IdeTestException
     * @return true if delete was successful
     */
    public boolean deleteResourceFromWorkspace(IProject project, String... resourceNames) throws IdeTestException {
        if (IdeTestUtil.isEmpty(project))
            throw IdeTestException.getWrappedException("IProject cannot be empty");
        if (IdeTestUtil.isEmpty(resourceNames))
            throw IdeTestException.getWrappedException("resource names cannot be empty");

        boolean retVal = false;
        try {
            for (String resourceName : resourceNames) {
                if (resourceName.equalsIgnoreCase(project.getName())) {
                    deleteProjectFromWorkspace(project.getName());
                    retVal = true;
                    break;
                }
                IResource resource = findResourceInProject(project.members(), resourceName);
                if (IdeTestUtil.isEmpty(resource))
                    throw IdeTestException.getWrappedException("Couldn't find resource:" + resourceName
                            + " to delete in Project");
                resource.delete(true, new NullProgressMonitor());
                refreshWorkspace();
                if (IdeTestUtil.isEmpty(findResourceInProject(project.members(), resourceName)))
                    retVal = true;
            }
        } catch (CoreException e) {
            throw IdeTestException.getWrappedException("Core Exception while trying to delete a resource in a project",
                e);
        }
        if (!retVal)
            throw IdeTestException.getWrappedException("couldn't delete resources from the workspace");
        return retVal;
    }

    public IWorkspace getWorkspace() {
        return workspace;
    }

    /**
     * replaces the package.xml contained within the project with the one contained in the directory whose path is
     * folderThatHasNewManifestRelPath
     *
     * @param project
     * @param folderThatHasNewManifestRelPath
     * @param orgTypeEnum
     * @return
     * @throws IdeTestException
     */
    public IFile replaceProjectManifest(IProject project, String folderThatHasNewManifestRelPath,
            OrgTypeEnum orgTypeEnum) throws IdeTestException {

        URL url =
                IdeTestUtil.getFullUrlEntry(folderThatHasNewManifestRelPath + File.separator
                        + Constants.PACKAGE_MANIFEST_FILE_NAME);
        File newPackageManifestFile = new File(url.getFile());

        // copy newPackageManifestFile to temp dir.
        String tempDirPath =
                ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + File.separator + "temp";
        File tempDir = new File(tempDirPath);
        IdeTestUtil.copyFilesToDirRecursively(tempDir, newPackageManifestFile);
        String tempPackageManifestFilePath = tempDirPath + File.separator + Constants.PACKAGE_MANIFEST_FILE_NAME;
        File tempPackageManifestFile = new File(tempPackageManifestFilePath);

        IFile oldPackageXML = IdeTestUtil.getPackageManifestFactory().getPackageManifestFile(project);
        try {
            String oldPackageXMLContent = new String(Utils.getBytesFromStream(oldPackageXML.getContents(), 1024));
            List<PackageInfo> pList = IdeTestOrgFactory.getOrgFixture().getOrg(orgTypeEnum).getPackageList();
            String deployedPackageName = IdeTestUtil.isEmpty(pList) ? null : pList.get(0).getPackageName();
            // Having fullName element in old pacakge.xml meaning it's in
            // package scope, so retain fullName element to new package.xml.
            if (oldPackageXMLContent.contains("<fullName>") && oldPackageXMLContent.contains("</fullName>")) {
                int startIndex = oldPackageXMLContent.indexOf("<fullName>");
                int endIndex = oldPackageXMLContent.indexOf("</fullName>");
                String packageName = oldPackageXMLContent.substring(startIndex, endIndex);
                // TODO: assuming pacakge.xml in fileMetadata always has
                // <fullName> element
                IdeTestUtil.replaceFileContent(tempPackageManifestFile,
                    IdeTestConstants.PACKAGE_FULL_NAME_ELEMENT_WITH_TOKEN, "<fullName>" + packageName + "</fullName>");
            }
            // used when test deploys data to org w/ empty project then replace
            // package.xml and refresh.
            else if (Utils.isNotEmpty(deployedPackageName)
                    && !deployedPackageName.equalsIgnoreCase(PackageTypeEnum.UNPACKAGED.toString())) {
                IdeTestUtil.replaceFileContent(tempPackageManifestFile,
                    IdeTestConstants.PACKAGE_FULL_NAME_ELEMENT_WITH_TOKEN, "<fullName>" + deployedPackageName
                            + "</fullName>");
            }
            // Otherwise, remove <fullName>@packagename@</fullName> from new
            // package.xml if present.
            else {
                IdeTestUtil.removeFileContent(tempPackageManifestFile,
                    IdeTestConstants.PACKAGE_FULL_NAME_ELEMENT_WITH_TOKEN);
            }

            // change package.xml
            oldPackageXML.setContents(new FileInputStream(tempPackageManifestFile), IResource.FORCE,
                new NullProgressMonitor());
            IdeProjectFixture.getInstance().refreshWorkspace();

        } catch (CoreException e) {
            throw IdeTestException
                    .getWrappedException(
                        "Core Exception while tryin to save package.xml while testing refresh from server on project level ",
                        e);
        } catch (IOException e) {
            throw IdeTestException.getWrappedException(
                "IO Exception while tryin to save package.xml while testing refresh from server on project level", e);
        } finally {
            IdeTestUtil.deleteDirectoryRecursively(tempDir);
        }

        return oldPackageXML;
    }

    /**
     * disables the autobuild on the workspace
     *
     * @throws IdeTestException
     */
    public void disableAutoBuild() throws IdeTestException {
        logger.debug("disabling autobuild...");
        switchAutoBuild(false);
    }

    /**
     * enables autobuild on the workspace.
     *
     * @throws IdeTestException
     */
    public void enableAutoBuild() throws IdeTestException {
        logger.debug("enabling autobuild...");
        switchAutoBuild(true);
    }

    // ------------------------ PRIVATE HELPERS----------------------------
    private ProjInfo createProjectWorker(OrgInfo orgInfo, IdeProjectContentTypeEnum projectContentSelection,
            ComponentTypeEnum[] componentTypesForProjectCreate, String pkgNameToCreateProjectAgainst,
            Package packageManifest) throws IdeTestException {
        ProjInfo projInfo = null;
        try {
            ForceProject forceProject = orgInfo.getWrapperForceProject();

            // create the project
            ProjectModel model = new ProjectModel(forceProject);
            model.setEnvironment(forceProject.getEndpointEnvironment());
            model.setProjectName(forceProject.getUserName() + "_" + IdeTestUtil.getRandomString(5));
            Connection connection = IdeTestUtil.getConnectionFactory().getConnection(forceProject);
            model.setConnection(connection);
            switch (projectContentSelection) {

            case NONE:
                model.setContentSelection(ProjectController.NONE);
                break;
            case SINGLE_PACKAGE:
                model.setContentSelection(ProjectController.SPECIFIC_PACKAGE);
                if (IdeTestUtil.isEmpty(pkgNameToCreateProjectAgainst))
                    throw IdeTestException
                            .getWrappedException("PackageName can't be empty if specific package is selected while project create.");
                model.setSelectedPackageName(pkgNameToCreateProjectAgainst);
                break;
            case ONLY_WHAT_IS_BEING_UPLOADED:

                if (packageManifest == null) {
                    IdeTestException.wrapAndThrowException("PackageManifest cannot null if projectContentConfig is "
                            + projectContentSelection + ".Need to provide package.xml");
                }
                model.setContentSelection(ProjectController.CUSTOM_COMPONENTS);
                PackageManifestModel packageManifestModel = new PackageManifestModel();
                packageManifestModel.setPackageManifest(packageManifest);
                model.setPackageManifestModel(packageManifestModel);
                break;
            case SPECIFIC_COMPONENTS:
                model.setContentSelection(ProjectController.CUSTOM_COMPONENTS);
                if (IdeTestUtil.isEmpty(componentTypesForProjectCreate))
                    throw IdeTestException
                            .getWrappedException("ComponentTypes to create project against cannot be empty");

                List<ComponentTypeEnum> componentTypesForProjectCreate_List =
                        Arrays.asList(componentTypesForProjectCreate);
                List<String> componentTypeNamesForProjectCreate_List = new ArrayList<String>();
                // forming a list of names of  componentTypes for project Create
                for (ComponentTypeEnum cte : componentTypesForProjectCreate_List) {
                    componentTypeNamesForProjectCreate_List.add(cte.getTypeName());
                }

                //figure out which ones are wildcard supported and which ones are not.
                List<String> wildCardSupportedComponentTypesNames_List = new ArrayList<String>();
                List<String> nonWildCardSupportedComponentTypesNames_List = new ArrayList<String>();
                ComponentList enabledComponents =
                        ContainerDelegate.getInstance().getFactoryLocator().getComponentFactory().getEnabledRegisteredComponents();

                //will hold explicit component names for components that don't support wild cards, so that they can be added to the packagemanifest later.
                Map<String, List<String>> packageManifestMapForNonWildCardSupportedComponents =
                        new HashMap<String, List<String>>(); // <componentTypeName, List<componentNames>>

                // Create a list for wildcard supported and non-wildcard supported components
                for (Component c : enabledComponents) {
                    if (componentTypeNamesForProjectCreate_List.contains(c.getComponentType())) {

                        if (IdeTestUtil.getComponentFactory().isWildCardSupportedComponentType(c.getComponentType()))

                            wildCardSupportedComponentTypesNames_List.add(c.getComponentType());
                        else {
                            nonWildCardSupportedComponentTypesNames_List.add(c.getComponentType());
                            packageManifestMapForNonWildCardSupportedComponents.put(c.getComponentType(),
                                new ArrayList<String>());
                        }
                    }
                }

                // Create Package manifest for wildcard supported components
                if (IdeTestUtil.isNotEmpty(wildCardSupportedComponentTypesNames_List)) {

                    packageManifest =
                            IdeTestUtil.getPackageManifestFactory().createPackageManifestForComponentTypes(
                                pkgNameToCreateProjectAgainst,
                                wildCardSupportedComponentTypesNames_List
                                        .toArray(new String[wildCardSupportedComponentTypesNames_List.size()]));
                } else {
                    packageManifest = IdeTestUtil.getPackageManifestFactory().createGenericDefaultPackageManifest();
                }

                // For non wildcard supported components query ListMetaData and add to the manifest
                if (IdeTestUtil.isNotEmpty(nonWildCardSupportedComponentTypesNames_List)) {

                    //query it from listmetadata
                    String[] nonWildCardSupportedComponentTypeNames_Arr =
                            nonWildCardSupportedComponentTypesNames_List
                                    .toArray(new String[nonWildCardSupportedComponentTypesNames_List.size()]);
                    FileMetadataExt fileMetadataExt =
                            IdeTestUtil.getMetadataService().listMetadata(connection,
                                nonWildCardSupportedComponentTypeNames_Arr, new NullProgressMonitor());

                    FileProperties[] fileProperties = fileMetadataExt.getFileProperties();
                    //need to remove any installed components in the folders.
                    FileProperties[] strippedFileProps =
                            Utils.removePackagedFiles(fileProperties, orgInfo.getNamespace());

                    for (FileProperties fp : strippedFileProps) {
                        List<String> listOfComponentNames =
                                packageManifestMapForNonWildCardSupportedComponents.get(fp.getType());
                        String fullName = fp.getFullName();
                        //check if this component belongs to a folder.
                        String possibleFolderName = null;
                        if (fullName.contains("/")) {
                            possibleFolderName = fullName.substring(0, fullName.indexOf("/"));
                        }

                        //add an entry for the component.
                        if (!listOfComponentNames.contains(fullName) && IdeTestUtil.isNotEmpty(fullName)) {
                            listOfComponentNames.add(fullName);
                        }
                        //add an entry for just the folder.
                        if (!listOfComponentNames.contains(possibleFolderName)
                                && IdeTestUtil.isNotEmpty(possibleFolderName)) {
                            listOfComponentNames.add(possibleFolderName);
                        }

                    }

                    //add to packageManifest.
                    IdeTestUtil.getPackageManifestFactory().addFileNamesToManifest(packageManifest,
                        packageManifestMapForNonWildCardSupportedComponents);

                }

                //set the package manifest model into package.xml
                packageManifestModel = new PackageManifestModel();
                packageManifestModel.setPackageManifest(packageManifest);
                model.setPackageManifestModel(packageManifestModel);

                break;
            case ALL:
            default:
                model.setContentSelection(ProjectController.ALL_CONTENT);
                break;
            }

            // create project
            final ProjectController projectController = IdeTestUtil.getProjectController();
            projectController.setModel(model);

            projectController.finish(new NullProgressMonitor());

            // need to call performCreateProject directly. This *cannot* be run
            // in a ui thread because
            // 1) if unit/pde headless tests hits this, they don't have a
            // display.
            // 2) if pde_ui hit this, we are already in a ui thread, so no
            // problem.
            // ReflectionTestUtils.executeMethodInClass(projectController,
            // "performCreateProject", new Class<?>[]{IProgressMonitor.class},
            // new Object[]{new NullProgressMonitor()});

            IProject project = model.getProject();
            IdeTestUtil.logProjectContents(project);

            // update forceProject
            forceProject.setProject(project);
            projInfo = new ProjInfo(forceProject, orgInfo);

            // add project to cache.
            projCache.put(project.getName(), projInfo);

            logger.info("Created project: " + project.getName());

        } catch (Exception e) {
            throw IdeTestException.getWrappedException("Unable to complete project create", e);
        }
        return projInfo;
    }

    public void switchAutoBuild(boolean bool) throws IdeTestException {
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc = ws.getDescription();
        desc.setAutoBuilding(bool);
        try {
            ws.setDescription(desc);
        } catch (CoreException e) {
            IdeTestException.wrapAndThrowException("Unable to disable auto building: " + e);
        }

        logger.debug((bool ? "Enabled" : "Disabled") + " autobuild");
    }

    private boolean deleteProjectWorker(OrgInfo orgInfo) throws IdeTestException {
        if (checkIfProjectCacheClean())
            return true;

        // delete the project
        Collection<ProjInfo> projectCacheEntries = projCache.values();
        boolean gotDeleted = false;
        for (ProjInfo tempProjInfo : projectCacheEntries) {
            if (tempProjInfo.getOrgInfo().equals(orgInfo)) {
                IProject iProj = tempProjInfo.getForceProject().getProject();
                if (IdeTestUtil.isEmpty(iProj))
                    IdeTestException.wrapAndThrowException("IProject in the project Cache can't be null. ");
                gotDeleted = deleteProjectFromWorkspace(iProj.getName());
                // remove from cache
                if (gotDeleted) {
                    projCache.remove(iProj.getName());
                }
            }
        }

        return gotDeleted;
    }

    /**
     * Given the members of an iproject, it returns all the members contained in it. Recursively scans through folders
     * too. Includes hidden files in the workspace if flag set., doesn't include the iproject itself.
     *
     * @param members
     * @param includeHiddenMembers
     * @return
     * @throws CoreException
     */
    private IResource[] getProjectMembersWorker(IResource[] members, final boolean includeHiddenMembers)
            throws CoreException {
        ArrayList<IResource> tempList = new ArrayList<IResource>();
        for (IResource member : members) {
            //TODO try to get the system attributes of the IResource instead of going by infront of the file name.
            boolean isResourceHidden = false;
            if (member.getName().startsWith("."))
                isResourceHidden = true;
            if (isResourceHidden && !includeHiddenMembers)
                continue; // skip a member that is hidden but
            // includeHiddenMembers is false.
            tempList.add(member);
            if (member instanceof IFolder)
                tempList.addAll(Arrays.asList(getProjectMembersWorker(((IFolder) member).members(),
                    includeHiddenMembers)));
        }
        return tempList.toArray(new IResource[tempList.size()]);
    }

}
