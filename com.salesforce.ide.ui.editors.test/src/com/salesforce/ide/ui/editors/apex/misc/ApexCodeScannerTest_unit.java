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
package com.salesforce.ide.ui.editors.apex.misc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;

import com.salesforce.ide.core.remote.registries.DescribeObjectRegistry;
import com.salesforce.ide.ui.editors.apex.assistance.ApexCodeScanner;

/**
 * 
 * @author nchen
 * 
 */
public class ApexCodeScannerTest_unit extends TestCase {

    // See W-2298351
    public void testApexCodeScannerDoesntCallBlockingSObjectRetrieve() throws Exception {
        ApexCodeScanner apexCodeScanner = new ApexCodeScanner();
        ApexCodeScanner spiedApexCodeScanner = spy(apexCodeScanner);
        DescribeObjectRegistry mockDescribeObjectRegistry = mock(DescribeObjectRegistry.class);
        IProject mockProject = mock(IProject.class);

        spiedApexCodeScanner.init(mockProject);
        when(mockProject.getName()).thenReturn("SampleProject");
        when(mockDescribeObjectRegistry.getCachedDescribeSObjectResultsIfAny(mockProject)).thenReturn(null);
        when(spiedApexCodeScanner.getDescribeObjectRegistry()).thenReturn(mockDescribeObjectRegistry);

        spiedApexCodeScanner.generateKeywordRule(mockProject);

        verify(mockDescribeObjectRegistry, never()).getCachedDescribeSObjects(mockProject);
        verify(mockDescribeObjectRegistry, times(1)).getCachedDescribeSObjectResultsIfAny(mockProject);
    }
}
