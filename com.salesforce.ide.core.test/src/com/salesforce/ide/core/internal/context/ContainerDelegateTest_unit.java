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
package com.salesforce.ide.core.internal.context;

import com.salesforce.ide.test.common.NoOrgSetupTest;

@SuppressWarnings("deprecation")
public class ContainerDelegateTest_unit extends NoOrgSetupTest {

    public void testContainerDelegate() {
        logStart("testContainerDelegate");
        try {
            ContainerDelegate containerDelegate = ContainerDelegate.getInstance();
            assertNotNull("ContainerDelegate should not be null", containerDelegate);
            assertNotNull("ServiceLocator should not be null", containerDelegate.getServiceLocator());
            assertNotNull("FactoryLocator should not be null", containerDelegate.getFactoryLocator());
            assertNotNull("ComponentFactory should not be null", containerDelegate.getBean("componentFactory"));
        } catch (Exception e) {
            handleFailure("Unable to test container delegate", e);
        } finally {
            logEnd("testContainerDelegate");
        }
    }
}
