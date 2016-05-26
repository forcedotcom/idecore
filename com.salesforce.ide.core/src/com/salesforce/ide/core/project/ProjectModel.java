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
package com.salesforce.ide.core.project;

import java.util.Set;

import org.eclipse.core.resources.IProject;

import com.salesforce.ide.core.model.OrgModel;
import com.salesforce.ide.core.remote.Connection;

public class ProjectModel extends OrgModel {

    protected Connection connection = null;
    protected boolean silentUpdate = false;
    protected int contentSelection = ProjectController.ALL_DEV_CODE_CONTENT;
    protected String selectedPackageName = null;
    protected Set<String> packageNames = null;
    protected PackageManifestModel packageManifestModel = null;

    //   C O N S T R U C T O R S
    public ProjectModel() {
        super();
    }

    public ProjectModel(IProject project) {
        super(project);
    }

    public ProjectModel(ForceProject forceProject) {
        super(forceProject);
    }

    public ProjectModel(IProject project, ForceProject forceProject) {
        super(project, forceProject);
    }

    public ProjectModel(IProject project, ForceProject forceProject, String projectName) {
        super(project, forceProject, projectName);
    }

    //   M E T H O D S
    public boolean isSilentUpdate() {
        return silentUpdate;
    }

    public void setSilentUpdate(boolean silentUpdate) {
        this.silentUpdate = silentUpdate;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getSelectedPackageName() {
        return selectedPackageName;
    }

    public void setSelectedPackageName(String selectedPackageName) {
        this.selectedPackageName = selectedPackageName;
        if (forceProject != null) {
            forceProject.setPackageName(selectedPackageName);
        }
    }

    public int getContentSelection() {
        return contentSelection;
    }

    public void setContentSelection(int contentSelection) {
        this.contentSelection = contentSelection;
    }

    public Set<String> getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(Set<String> packageNames) {
        this.packageNames = packageNames;
    }

    @Override
    public void setProject(IProject project) {
        super.setProject(project);
        // need project to move package manifest cache - when project is created w/ custom components via manifest editor 
        if (packageManifestModel != null) {
            packageManifestModel.setProject(project);
        }
    }

    public PackageManifestModel getPackageManifestModel() {
        if (packageManifestModel == null) {
            packageManifestModel = new PackageManifestModel();
        }
        return packageManifestModel;
    }

    public void setPackageManifestModel(PackageManifestModel packageManifestModel) {
        this.packageManifestModel = packageManifestModel;
    }

    public void clearConnections() {
        if (connection != null) {
            connection = null;
        }

        if (describeMetadataResultExt != null) {
            describeMetadataResultExt = null;
        }

    }

    public void clear() {
        if (packageNames != null) {
            packageNames.clear();
        }

        packageManifestModel = null;
    }
}
