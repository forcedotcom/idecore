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
package com.salesforce.ide.core.model;

import org.eclipse.core.resources.IProject;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.IModel;
import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.remote.metadata.DescribeMetadataResultExt;

public class OrgModel implements IModel {

    protected ForceProject forceProject = null;
    protected String projectName = null;
    protected String environment = null;
    protected DescribeMetadataResultExt describeMetadataResultExt = null;

    public OrgModel() {
        super();
    }

    public OrgModel(IProject project) {
        super();
        setProject(project);
    }

    public OrgModel(IProject project, ForceProject forceProject) {
        super();
        this.forceProject = forceProject;
        setProject(project);
    }

    public OrgModel(IProject project, ForceProject forceProject, String projectName) {
        super();
        this.forceProject = forceProject;
        setProject(project);
        this.projectName = projectName;
    }

    public OrgModel(ForceProject forceProject) {
        super();
        this.forceProject = forceProject;
    }

    public ForceProject getForceProject() {
        return forceProject;
    }

    public void setForceProject(ForceProject forceProject) {
        this.forceProject = forceProject;
    }

    @Override
    public IProject getProject() {
        if (forceProject == null) {
            return null;
        }
        return forceProject.getProject();
    }

    @Override
    public void setProject(IProject project) {
        if (forceProject == null) {
            forceProject = new ForceProject();
        }
        forceProject.setProject(project);

        if (project != null) {
            this.projectName = project.getName();
        }
    }

    public String getProjectName() {
        return getProject() != null && Utils.isNotEmpty(getProject().getName()) ? getProject().getName() : projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public DescribeMetadataResultExt getDescribeMetadataResultExt() {
        return describeMetadataResultExt;
    }

    public void setDescribeMetadataResultExt(DescribeMetadataResultExt describeMetadataResultExt) {
        this.describeMetadataResultExt = describeMetadataResultExt;

        if (describeMetadataResultExt != null && forceProject != null) {
            forceProject.setNamespacePrefix(describeMetadataResultExt.getOrganizationNamespace());
        }
    }

}
