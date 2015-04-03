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
package com.salesforce.ide.ui.wizards.project;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.ui.ForceIdeUIPlugin;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.internal.wizards.BaseWizard;

/**
 * Wizard creates new Force.com Project.
 *
 * @author cwall
 */
public class ProjectCreateWizard extends BaseWizard {
    private static final Logger logger = Logger.getLogger(ProjectCreateWizard.class);

    private ProjectOrganizationPage projectOrganizationPage = null;
    private ProjectProjectContentPage projectProjectContentPage = null;
    private ProjectCreateOperation createProjectOperation = null;

    public ProjectCreateWizard() {
        super();
        init();
    }

    private final void init() {
        controller = new ProjectController(project);

        // create connection operation to perform new project creation
        createProjectOperation = new ProjectCreateOperation(getProjectController(), getContainer());
        if (logger.isDebugEnabled()) {
            logger.debug("***   P R O J E C T   C R E A T E   W I Z A R D   ***");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Force.com IDE: '" + ForceIdeUIPlugin.getPluginId() + "' ForceIdeUIPlugin, version "
                    + ForceIdeUIPlugin.getBundleVersion());
        }
    }

    public ProjectController getProjectController() {
        return (ProjectController) controller;
    }

    public ProjectCreateOperation getCreateProjectOperation() {
        return createProjectOperation;
    }

    public ProjectOrganizationPage getProjectOrganizationPage() {
        return projectOrganizationPage;
    }

    public void setProjectOrganizationPage(ProjectOrganizationPage projectOrganizationPage) {
        this.projectOrganizationPage = projectOrganizationPage;
    }

    public ProjectProjectContentPage getProjectProjectContentPage() {
        return projectProjectContentPage;
    }

    public void setProjectProjectContentPage(ProjectProjectContentPage projectProjectContentPage) {
        this.projectProjectContentPage = projectProjectContentPage;
    }

    @Override
    protected String getWindowTitleString() {
        return UIMessages.getString("ProjectCreateWizard.title");
    }

    @Override
    public void addPages() {
        projectOrganizationPage = new ProjectOrganizationPage(this);
        addPage(projectOrganizationPage);
        projectProjectContentPage = new ProjectProjectContentPage(this);
        addPage(projectProjectContentPage);
    }

    /**
     * Create project from gather connection settings.
     *
     * @return boolean
     */
    @Override
    public boolean performFinish() {
        try {
            projectOrganizationPage.saveUserInput();
            projectProjectContentPage.saveUserInput();
            return createProject();
        } catch (Exception e) {
            logger.error("Unable to create project.", e);
            Utils.openError(new InvocationTargetException(e), true, "Unable to create project.");
            return false;
        }
    }

    @Override
    public boolean performCancel() {
        try {
            createProjectOperation.deleteProject(new NullProgressMonitor());
            return true;
        } catch (Exception e) {
            logger.error("Unable to create project.", e);
            Utils.openError(new InvocationTargetException(e), true, "Unable to delete project.");
            return false;
        }
    }

    /**
    *
    * @return boolean
    */
    public boolean createProject() throws InvocationTargetException, InterruptedException {
        // perform project creation
        return createProjectOperation.create();
    }
}
