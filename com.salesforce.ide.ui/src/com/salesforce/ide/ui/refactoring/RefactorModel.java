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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ProjectPackageList;

public class RefactorModel {
    private static final Logger logger = Logger.getLogger(RefactorModel.class);

    private IProject project = null;
    private Set<IResource> changeResources = null;
    private ProjectPackageList projectPackageList = null;

    //   C O N S T R U C T O R S
    public RefactorModel() {
        super();
        changeResources = new HashSet<>();
    }

    public RefactorModel(Set<IResource> changeResources) {
        super();
        this.changeResources = changeResources;
    }

    //   M E T H O D S

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public Set<IResource> getChangeResources() {
        return changeResources;
    }

    public void setChangeResources(Set<IResource> changeResources) {
        this.changeResources = changeResources;
    }

    public void addChangeResource(IResource changeResource) {
        addChangeResource(changeResource, false);
    }

    public void addChangeResources(Set<IResource> changeResources) {
        if (this.changeResources == null) {
            this.changeResources = changeResources;
        } else {
            this.changeResources.addAll(changeResources);
        }
    }

    public void addChangeResources(List<IResource> changeResources) {
        if (this.changeResources != null) {
            this.changeResources = new HashSet<>();
        }

        if (Utils.isNotEmpty(changeResources)) {
            this.changeResources.addAll(changeResources);
        }
    }

    public void addChangeResource(IResource changeResource, boolean updateProjectPackageList) {
        changeResources.add(changeResource);
        if (updateProjectPackageList) {
            updateProjectPackageList(changeResource);
        }
    }

    public void refreshChangeResources() {
        refreshChangeResources(true);
    }

    public void refreshChangeResources(boolean includeManifest) {
        if (Utils.isNotEmpty(projectPackageList)) {
            addChangeResources(projectPackageList.getAllComponentResources(includeManifest));
        }
    }

    public boolean isChangeResourcesEmpty() {
        return Utils.isEmpty(changeResources);
    }

    public IResource getChangeResource(IResource resource) {
        if (!isChangeResourcesEmpty() && changeResources.contains(resource)) {
            return resource;
        }
        return null;
    }

    public ProjectPackageList getProjectPackageList() {
        return projectPackageList;
    }

    public void setProjectPackageList(ProjectPackageList projectPackageList) {
        this.projectPackageList = projectPackageList;
    }

    public void updateProjectPackageList(IResource resource) {
        if (projectPackageList == null) {
            logger.warn("Unable to update project package list - project package list is null");
            return;
        }
    }

    public String getResourceListString() {
        final String TAB = ", ";
        StringBuffer retValue = new StringBuffer();
        retValue.append("Resources ( ")
            .append("count = ").append(changeResources.size()).append(TAB);
        int componentCnt = 0;
        for (IResource changeResource : changeResources) {
            retValue.append("\n (");
            retValue.append(++componentCnt);
            retValue.append(") ");
            retValue.append(changeResource.getFullPath().toPortableString());
        }
        retValue.append(" )");

        return retValue.toString();
    }
}
