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
package com.salesforce.ide.ui.wizards.components.apex.trigger;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.salesforce.ide.core.internal.components.apex.trigger.ApexTriggerModel;
import com.salesforce.ide.core.internal.utils.ForceExceptionUtils;
import com.salesforce.ide.core.internal.utils.Utils;
import com.salesforce.ide.core.model.ApexTrigger;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.ui.wizards.components.ComponentWizard;
import com.salesforce.ide.ui.wizards.components.IComponentWizardPage;
import com.salesforce.ide.ui.wizards.components.apex.CodeWizardPage;

public class ApexTriggerWizardPage extends CodeWizardPage {

    private static final Logger logger = Logger.getLogger(ApexTriggerWizardPage.class);

    private Set<String> selectedTriggerOperations = null;

    public ApexTriggerWizardPage(ComponentWizard componentWizard) {
        super(componentWizard);
    }

    //  M E T H O D S
    @Override
    public String getComponentName() {
        return componentWizardComposite.getComponentName();
    }

    @Override
    protected void additionalInitialize(Composite parent) {
        super.additionalInitialize(parent);
        // capture selected operations
        selectedTriggerOperations = new TreeSet<>();
        loadObjects(false);
    }

    @Override
    protected void selectedProjectChanged(IProject project) {
        super.selectedProjectChanged(project);
        loadObjects(false);
    }

    // load available/trigger-able objects
    private void loadObjects(boolean refresh) {
        if (getApexTriggerWizardComposite().getCmbObjects() != null) {
            getApexTriggerWizardComposite().getCmbObjects().removeAll();
        }

        if (checkEnabled()) {
            try {
                SortedSet<String> triggerableObjectNames =
                        componentWizard.getComponentController().getObjectNames(refresh);
                if (Utils.isNotEmpty(triggerableObjectNames)) {
                    for (String triggerableObjectName : triggerableObjectNames) {
                        getApexTriggerWizardComposite().getCmbObjects().add(triggerableObjectName);
                    }

                    getApexTriggerWizardComposite().getCmbObjects().select(0);
                } else {
                    logger.warn("No triggerable objects found");
                }
            } catch (Exception e) {
                logger.warn("Unable to load trigger objects", ForceExceptionUtils.getRootCause(e));
                Utils.openWarning(ForceExceptionUtils.getRootCause(e), true, "Unable to load Workflow objects");
            }
        }
    }

    @Override
    protected void refreshObjects() {
        loadObjects(true);
    }

    @Override
    public void createComposite(Composite parent) {
        // load trigger operation options
        ApexTrigger apexTrigger = (ApexTrigger) componentWizard.getComponentWizardModel().getComponent();

        componentWizardComposite =
                new ApexTriggerWizardComposite(parent, SWT.NULL, apexTrigger.getDisplayName(), apexTrigger
                        .getSupportedApiVersions(), apexTrigger.getOperationOptions());
        componentWizardComposite.setComponentWizardPage(this);
    }

    @Override
    protected boolean finalDialogChanged(IComponentWizardPage componentWizardPage) {
        String triggerObject = getApexTriggerWizardComposite().getObjectName();
        if (Utils.isEmpty(triggerObject)) {
            updateInfoStatus("Trigger object must be selected.");
            return false;
        }

        List<Button> checkboxOperationButtons = getApexTriggerWizardComposite().getCheckboxOperationButtons();
        boolean isButtonChecked = false;
        for (Button checkboxOperationButton : checkboxOperationButtons) {
            if (checkboxOperationButton.getSelection()) {
                isButtonChecked = true;
            }
        }

        if (!isButtonChecked) {
            updateInfoStatus("You must select at least one operation on which to fire the trigger.");
            setPageComplete(false);
            return false;
        }

        // everything checked out okay, set selected trigger operations
        selectedTriggerOperations.clear();
        for (Button checkboxOperationButton : checkboxOperationButtons) {
            if (checkboxOperationButton.getSelection()) {
                selectedTriggerOperations.add(checkboxOperationButton.getText().toLowerCase());
            }
        }

        return true;
    }

    @Override
    public void saveUserInput() throws InstantiationException, IllegalAccessException {
        if (componentWizardComposite == null) {
            throw new IllegalArgumentException("Component composite cannot be null");
        }

        ApexTriggerWizardComposite apexTriggerWizardComposite = (ApexTriggerWizardComposite) componentWizardComposite;
        ApexTriggerModel apexTriggerWizardModel = (ApexTriggerModel) componentWizard.getComponentWizardModel();
        Component component = apexTriggerWizardModel.getComponent();

        // create metadata instance and save metadata input values
        com.salesforce.ide.api.metadata.types.ApexTrigger apexTrigger =
                (com.salesforce.ide.api.metadata.types.ApexTrigger) component.getDefaultMetadataExtInstance();

        // save non-xml attributes
        apexTriggerWizardModel.setObjectName(apexTriggerWizardComposite.getObjectName());
        apexTriggerWizardModel.setOperations(selectedTriggerOperations);

        if (logger.isDebugEnabled()) {
            logger.debug("Created and loaded instance of '" + apexTrigger.getClass().getName() + "'");
        }
    }

    private ApexTriggerWizardComposite getApexTriggerWizardComposite() {
        return (ApexTriggerWizardComposite) componentWizardComposite;
    }
}
