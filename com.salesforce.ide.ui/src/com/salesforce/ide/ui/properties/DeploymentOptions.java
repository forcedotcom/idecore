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
package com.salesforce.ide.ui.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.salesforce.ide.core.project.ForceProject;
import com.salesforce.ide.core.project.ProjectController;
import com.salesforce.ide.ui.internal.utils.UIMessages;

/**
 * Preference page for controlling the deployment options. As of v31, we are letting the user decide if they would
 * prefer to use the Tooling API path when applicable (default). The Tooling API is faster but uses a different
 * execution path than the Metadata API path since not all components are supported yet. The speed is usually
 * significant enough to justify using a specialized path.
 * 
 * @author nchen
 * 
 */
public class DeploymentOptions extends BasePropertyPage {

    private Button preferToolingDeploymentCheckbox;
    private ProjectController projectController = null;
    private ForceProject forceProject;

    public DeploymentOptions() {
        super();
        projectController = new ProjectController();
    }

    public ProjectController getProjectController() {
        return projectController;
    }

    protected IProject getProject() {
        return (IProject) getElement();
    }

    public Button getPreferToolingDeploymentCheckbox() {
        return preferToolingDeploymentCheckbox;
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        getDefaultsButton().setEnabled(false);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite deploymentComposite = new Composite(parent, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        deploymentComposite.setLayout(gridLayout);
        deploymentComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));

        preferToolingDeploymentCheckbox = new Button(deploymentComposite, SWT.CHECK);
        preferToolingDeploymentCheckbox.setText(UIMessages.getString("DeploymentOptions_UseToolingAPI"));

        loadFromPreferences();

        return deploymentComposite;
    }

    private void loadFromPreferences() {
        forceProject = getProjectService().getForceProject(getProject());
        boolean preferToolingDeployment = forceProject.getPreferToolingDeployment();
        preferToolingDeploymentCheckbox.setSelection(preferToolingDeployment);

        projectController.getProjectModel().setForceProject(forceProject);
    }

    @Override
    public boolean performOk() {
        try {
            forceProject.setPreferToolingDeployment(preferToolingDeploymentCheckbox.getSelection());
            projectController.saveSettings(new NullProgressMonitor());
        } catch (InterruptedException e) {
            // Not possible with a NullProgressMonitor
        }

        return true;
    }
}
