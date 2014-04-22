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

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.core.project.ProjectModel;
import com.salesforce.ide.core.remote.SalesforceEndpoints;
import com.salesforce.ide.ui.internal.utils.UIConstants;
import com.salesforce.ide.ui.internal.utils.UIUtils;
import com.salesforce.ide.ui.internal.wizards.BaseOrgWizardPage;

public abstract class BaseProjectCreatePage extends BaseOrgWizardPage {

    protected ProjectCreateWizard projectWizard = null;
    protected ProjectController projectController = null;
    protected int step = 0;

    public BaseProjectCreatePage(String wizardName) {
        super(wizardName);
    }

    public BaseProjectCreatePage(String wizardName, ProjectCreateWizard projectWizard) {
        super(wizardName);
        this.projectWizard = projectWizard;
        this.projectController = projectWizard.getProjectController();
    }

    //   M E T H O D S
    public ProjectCreateWizard getProjectWizard() {
        return projectWizard;
    }

    public ProjectController getProjectController() {
        return projectController;
    }

    public void setProjectController(ProjectController projectController) {
        this.projectController = projectController;
    }

    protected ProjectModel getProjectModel() {
        return getProjectController().getProjectModel();
    }

    protected SalesforceEndpoints getSalesforceEndpoints() {
        return ContainerDelegate.getInstance().getFactoryLocator().getConnectionFactory().getSalesforceEndpoints();
    }

    protected void setStep(int step) {
        this.step = step;
    }

    protected int getStep() {
        return this.step;
    }

    protected String getStepString() {
        return "(Step " + step + " of " + projectWizard.getPageCount() + ")";
    }

    protected void setComplete(boolean complete) {
        getProjectController().setCanComplete(complete);
        setPageComplete(complete);
    }

    // for lack of a better way to size the wizard
    protected void setWizardCosmetics(Composite parent) {
        parent.getShell().setSize(new Point(575, 735));
        if (Display.getDefault().getActiveShell() != null
                && !UIConstants.NEW_PROJECT_PARENT_SHELL_TEXT.equals(Display.getDefault().getActiveShell().getText())) {
            UIUtils.placeDialogInCenter(Display.getDefault().getActiveShell(), parent.getShell());
        }
    }

    @Override
    public IWizardPage getNextPage() {
        return projectWizard.getNextPage(this);
    }

    protected void initEndpoints(Combo cmbEnvironment, Combo cmbEndpointServers) {
        prepareEnvironments(cmbEnvironment);
        prepareEndpointServers(cmbEndpointServers);
    }

    protected void prepareEnvironments(Combo cmbEnvironment) {
        cmbEnvironment.removeAll();
        Set<String> endpointLabels = getSalesforceEndpoints().getDefaultEndpointLabels();

        if (Utils.isEmpty(endpointLabels)) {
            return;
        }

        for (String endpointLabel : endpointLabels) {
            cmbEnvironment.add(endpointLabel);
        }
        cmbEnvironment.add(OTHER_LABEL_NAME);
    }

    protected void prepareEndpointServers(Combo cmbEndpointServers) {
        cmbEndpointServers.removeAll();
        TreeSet<String> endpointServers = getSalesforceEndpoints().getUserEndpointServers();

        if (Utils.isEmpty(endpointServers)) {
            return;
        }

        for (String endpointServer : endpointServers) {
            cmbEndpointServers.add(endpointServer);
        }
    }
}
