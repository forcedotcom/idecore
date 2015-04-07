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
package com.salesforce.ide.deployment.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.salesforce.ide.core.model.OrgModel;
import com.salesforce.ide.core.project.ForceProject;

public class DeploymentWizardModel extends OrgModel {

    protected List<IResource> deployResources = null;
    private File sourceArchivePath = null;
    private File destinationArchivePath = null;
    private ForceProject destinationOrg = null;

    // C O N S T R U C T O R S
    public DeploymentWizardModel() {
        super();
    }

    public DeploymentWizardModel(IProject project) {
        super(project);
        this.deployResources = new ArrayList<>(1);
        deployResources.add(project);
    }

    public DeploymentWizardModel(IProject project, List<IResource> deployResources) {
        super(project);
        this.deployResources = deployResources;
    }

    // M E T H O D S
    public List<IResource> getDeployResources() {
        return deployResources;
    }

    public void setDeployResource(List<IResource> deployResources) {
        this.deployResources = deployResources;
    }

    public File getSourceArchivePath() {
        return sourceArchivePath;
    }

    public void setSourceArchivePath(File deploymentArchiveDir) {
        this.sourceArchivePath = deploymentArchiveDir;
    }

    public ForceProject getDestinationOrg() {
        return destinationOrg;
    }

    public void setDestinationOrg(ForceProject destinationOrg) {
        this.destinationOrg = destinationOrg;
    }

    public File getDestinationArchivePath() {
        return destinationArchivePath;
    }

    public void setDestinationArchivePath(File snapshotArchiveDir) {
        this.destinationArchivePath = snapshotArchiveDir;
    }
}
