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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.jface.viewers.CheckStateChangedEvent;

import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.deployment.internal.DeploymentComponent;
import com.salesforce.ide.deployment.internal.DeploymentComponentSet;
import com.salesforce.ide.deployment.internal.DeploymentPayload;

public class DeploymentPlanCheckStateListenerTest_unit extends TestCase {

    public void testMetadataComponentDeployStateMirrorsAssociatedComponent() throws Exception {
        CheckStateChangedEvent event = mock(CheckStateChangedEvent.class);

        DeploymentComponent topLevelMetadataComponent = mock(DeploymentComponent.class);
        DeploymentComponent associatedMetadataComponent = mock(DeploymentComponent.class);

        DeploymentPlanPage page = mock(DeploymentPlanPage.class);
        DeploymentController controller = mock(DeploymentController.class);
        DeploymentPayload payload = mock(DeploymentPayload.class);

        DeploymentComponentSet components = mock(DeploymentComponentSet.class);

        Component topLevelComponent = mock(Component.class);
        Component associatedComponent = mock(Component.class);

        boolean enable = false;
        String name = "component-name";

        when(event.getElement()).thenReturn(topLevelMetadataComponent);
        when(components.iterator()).thenReturn(
            Arrays.asList(topLevelMetadataComponent, associatedMetadataComponent).iterator());
        when(page.getController()).thenReturn(controller);
        when(controller.getDeploymentPayload()).thenReturn(payload);
        when(payload.getDeploymentComponents()).thenReturn(components);
        when(topLevelMetadataComponent.getComponent()).thenReturn(topLevelComponent);
        when(associatedMetadataComponent.getComponent()).thenReturn(associatedComponent);
        when(topLevelComponent.getName()).thenReturn(name);
        when(associatedComponent.getName()).thenReturn(name);
        when(associatedComponent.isMetadataInstance()).thenReturn(true);
        when(event.getChecked()).thenReturn(enable);
        doNothing().when(page).setEnableButton();

        DeploymentPlanCheckStateListener listener = new DeploymentPlanCheckStateListener(page);

        listener.checkStateChanged(event);

        verify(topLevelMetadataComponent, times(1)).setDeploy(enable);

        verify(associatedMetadataComponent, times(1)).setDeploy(enable);
    }
}
