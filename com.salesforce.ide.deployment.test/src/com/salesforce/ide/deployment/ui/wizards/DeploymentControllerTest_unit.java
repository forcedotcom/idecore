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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;

import com.salesforce.ide.core.services.PackageDeployService;
import com.sforce.soap.metadata.DeployOptions;

public class DeploymentControllerTest_unit extends TestCase {
	
    public void testmakeDeployOptions() throws Exception {
        final PackageDeployService packageDeployService = mock(PackageDeployService.class);
        final DeployOptions deployOptions = mock(DeployOptions.class);
        when(packageDeployService.getDeployOptions(eq(false))).thenReturn(deployOptions);

        DeploymentController controller = new DeploymentController(mock(IProject.class));
        final DeployOptions makeDefaultDeployOptions = controller.makeDefaultDeployOptions(false, packageDeployService);
        assertEquals(deployOptions, makeDefaultDeployOptions);
        verify(deployOptions, times(1)).setIgnoreWarnings(true);
    }
}
