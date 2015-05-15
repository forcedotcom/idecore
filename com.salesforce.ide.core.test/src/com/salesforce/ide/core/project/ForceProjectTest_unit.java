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
package com.salesforce.ide.core.project;

import com.salesforce.ide.test.common.NoOrgSetupTest;

@SuppressWarnings("deprecation")
public class ForceProjectTest_unit extends NoOrgSetupTest {

    public void testForceProject_equals() {
        logStart("testForceProject_equals");
        try {
            ForceProject forceProject1 = getDefaultDevForceProjectInstance();
            ForceProject forceProject2 = getDefaultDevForceProjectInstance();
            assertTrue("ForceProjects should be the same", forceProject1.equals(forceProject2));
            assertTrue("ForceProjects should be the same (equalsFull)", forceProject1.equals(forceProject2));

        } catch (Exception e) {
            handleFailure("Unable to test force project equals", e);
        } finally {
            logEnd("testForceProject_equals");
        }
    }

    public void testForceProject_hasChanged() {
        logStart("testForceProject_hasChanged");
        try {
            ForceProject forceProject1 = getDefaultDevForceProjectInstance();
            ForceProject forceProject2 = getDefaultDevForceProjectInstance();

            forceProject1.setKeepEndpoint(!forceProject1.isKeepEndpoint());
            forceProject1.isOrgChange(forceProject2);
            assertTrue("ForceProjects org should have changed (keep endpoint)",
                    forceProject1.isOrgChange(forceProject2));
        } catch (Exception e) {
            handleFailure("Unable to create new force project", e);
        } finally {
            logEnd("testForceProject_hasChanged");
        }
    }
}
