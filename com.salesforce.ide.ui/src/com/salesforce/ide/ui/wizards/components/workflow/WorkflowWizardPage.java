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
package com.salesforce.ide.ui.wizards.components.workflow;

import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.components.workflow.WorkflowModel;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.internal.utils.UIMessages;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.generic.GenericComponentWizardPage;

public class WorkflowWizardPage extends GenericComponentWizardPage {

    private static final Logger logger = Logger.getLogger(WorkflowWizardPage.class);

    public WorkflowWizardPage(ComponentWizard componentWizard) {
        super(componentWizard);
        setTitle(UIMessages.getString("NewWorkflowComponent.title"));
        setDescription(UIMessages.getString("NewWorkflowComponent.description"));
    }

    @Override
    public String getComponentName() {
        return componentWizardComposite.getComponentName();
    }

    @Override
    public void createComposite(Composite parent) {
        Component component = componentWizard.getComponentController().getComponent();
        componentWizardComposite = new WorkflowWizardComposite(parent, SWT.NULL, component.getDisplayName());
        componentWizardComposite.setComponentWizardPage(this);
    }

    @Override
    protected void additionalInitialize(Composite parent) {
        super.additionalInitialize(parent);
        loadObjects(false);
    }

    @Override
    protected void selectedProjectChanged(IProject project) {
        super.selectedProjectChanged(project);
        loadObjects(false);
    }

    // load available/workflow-able objects
    private void loadObjects(boolean refresh) {
        if (getWorkflowWizardComposite().getCmbObjects() != null) {
            getWorkflowWizardComposite().getCmbObjects().removeAll();
        }

        try {
            SortedSet<String> workflowableObjectNames =
                    componentWizard.getComponentController().getObjectNames(refresh);
            if (Utils.isNotEmpty(workflowableObjectNames)) {
                for (String workflowableObjectName : workflowableObjectNames) {
                    getWorkflowWizardComposite().getCmbObjects().add(workflowableObjectName);
                }
                getWorkflowWizardComposite().getCmbObjects().select(0);
            } else {
                logger.warn("No workflowable objects found");
            }
        } catch (Exception e) {
            logger.warn("Unable to load workflow objects", ForceExceptionUtils.getRootCause(e));
            Utils.openWarning(ForceExceptionUtils.getRootCause(e), true, "Unable to load Workflow objects");
        }
    }

    @Override
    protected void refreshObjects() {
        loadObjects(true);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            validateUserInput();
        }
        super.setVisible(visible);
    }

    @Override
    public void validateUserInput() {
        if (componentWizard.getContainer() instanceof WizardDialog) {
            if (((WizardDialog) componentWizard.getContainer()).getReturnCode() == Window.CANCEL) {
                return;
            }
        }

        status = null;

        if (!initialDialogChanged(this)) {
            return;
        }

        if (getComponentWizardModel().getProject() == null) {
            updateErrorStatus(UIMessages.getString("NewComponent.ProjectRequired.message"));
            return;
        }

        // name is the filename (developer name)
        String componentName = getBaseComponentWizardComposite().getObjectName();
        getComponentWizardModel().setFullName(componentName);
        if (Utils.isEmpty(componentName)) {
            updateInfoStatus(UIMessages.getString("NewWorkflowComponent.WorkflowObjectRequired.message"));
            setPageComplete(false);
            return;
        }

        if (!getComponentController().isNameUniqueLocalCheck()) {
            StringBuffer strBuff = new StringBuffer(getComponent().getDefaultFolder());
            strBuff.append("/").append(componentName).append(".").append(getComponent().getFileExtension());
            updateErrorStatus(UIMessages.getString("NewWorkflowComponent.WorkflowExists.message",
                new Object[] { strBuff.toString() }));
            setPageComplete(false);
            return;
        }

        updateInfoStatus(status);

        setComplete(true);
    }

    public WorkflowWizardComposite getWorkflowWizardComposite() {
        return (WorkflowWizardComposite) componentWizardComposite;
    }

    public WorkflowWizard getWorkflowWizard() {
        return (WorkflowWizard) componentWizard;
    }

    @Override
    public void saveUserInput() throws InstantiationException, IllegalAccessException {
        if (componentWizardComposite == null) {
            throw new IllegalArgumentException("Component composite cannot be null");
        }

        WorkflowWizardComposite workflowWizardComposite = (WorkflowWizardComposite) componentWizardComposite;
        WorkflowModel workflowWizardModel = (WorkflowModel) componentWizard.getComponentWizardModel();
        Component component = workflowWizardModel.getComponent();

        // create metadata instance and save metadata input values
        com.salesforce.ide.api.metadata.types.Workflow workflow =
                (com.salesforce.ide.api.metadata.types.Workflow) component.getDefaultMetadataExtInstance();
        workflow.setFullName(workflowWizardComposite.getCmbObjects().getText());
        workflowWizardModel.setFullName(workflowWizardComposite.getCmbObjects().getText());

        if (logger.isDebugEnabled()) {
            logger.debug("Created and loaded instance of '" + workflow.getClass().getName() + "' with user input");
        }
    }
}
