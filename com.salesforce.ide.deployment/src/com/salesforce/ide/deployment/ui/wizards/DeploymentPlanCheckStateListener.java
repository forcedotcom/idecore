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
/**
 * 
 */
package com.salesforce.ide.deployment.ui.wizards;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;

import com.salesforce.ide.deployment.internal.DeploymentComponent;
import com.salesforce.ide.deployment.internal.DeploymentComponentSet;

public final class DeploymentPlanCheckStateListener implements ICheckStateListener {
    private final DeploymentPlanPage page;

    public DeploymentPlanCheckStateListener(DeploymentPlanPage page) {
        this.page = page;
    }

    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
        // check/uncheck element
        if (event.getElement() != null) {
            DeploymentComponent deploymentComponent = (DeploymentComponent) event.getElement();
            deploymentComponent.setDeploy(event.getChecked());

            DeploymentComponentSet components = page.getController().getDeploymentPayload().getDeploymentComponents();
            for (DeploymentComponent component : components) {
                if (component.getComponent().getName().equals(deploymentComponent.getComponent().getName())
                        && component.getComponent().isMetadataInstance()) {
                    component.setDeploy(event.getChecked());
                }
            }
        }
        page.setEnableButton();
    }
}
