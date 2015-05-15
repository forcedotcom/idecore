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
package com.salesforce.ide.ui.actions;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import com.salesforce.ide.core.services.PackageDeployService;
import com.sforce.soap.metadata.DeployOptions;

public class SaveToServerActionControllerTest_unit extends TestCase {
    public void testMakeDefaultOptions() throws Exception {
        SaveToServerActionController controller = new SaveToServerActionController();
        PackageDeployService deployService = mock(PackageDeployService.class);
        final DeployOptions deployOptions = mock(DeployOptions.class);

        when(deployService.makeDefaultDeployOptions(eq(false))).thenReturn(deployOptions);

        final DeployOptions result = controller.makeDeployOptions(deployService);

        verify(deployService, times(1)).makeDefaultDeployOptions(false);
        verify(deployOptions, times(1)).setIgnoreWarnings(true);
        assertSame(deployOptions, result);
    }
}
