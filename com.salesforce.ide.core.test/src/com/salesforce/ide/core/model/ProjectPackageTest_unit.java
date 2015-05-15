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
package com.salesforce.ide.core.model;

import junit.framework.TestCase;

import com.salesforce.ide.test.common.utils.IdeTestUtil;
import com.sforce.soap.partner.sobject.wsc.SObject;

public class ProjectPackageTest_unit extends TestCase {

    public void testParseSObject_WhenManaged() throws Exception {
        ProjectPackage projectPackage = IdeTestUtil.getProjectPackageFactory().getProjectPackageInstance();
        assertNotNull("Project package should not be null", projectPackage);
        projectPackage.setOrgId("1234567ABC");

        SObject sobject = new SObject();
        sobject.setId("123456");
        sobject.addField("Source", "1234567");
        sobject.addField("Name", "Whatever");
        sobject.addField("Description", "Whatever description");
        sobject.addField("IsManaged", "true");
        sobject.addField("Status", "ACTIVE");

        projectPackage.parseInput(sobject);
        assertFalse("Project package should not be a referenced package (aka managed, installed)", projectPackage
                .isInstalled());
    }

    public void testParseSObject_WhenManagedInstalled() throws Exception {
        ProjectPackage projectPackage = IdeTestUtil.getProjectPackageFactory().getProjectPackageInstance();
        assertNotNull("Project package should not be null", projectPackage);
        projectPackage.setOrgId("1234567");

        SObject sobject = new SObject();
        sobject.setId("123456");
        sobject.addField("Source", "1234568");
        sobject.addField("Name", "Whatever");
        sobject.addField("Description", "Whatever description");
        sobject.addField("IsManaged", "true");
        sobject.addField("Status", "ACTIVE");
        sobject.addField("VersionName", "abc");

        projectPackage.parseInput(sobject);
        assertTrue("Project package should be a referenced package (aka managed, installed)", projectPackage
                .isInstalled());
    }
}
