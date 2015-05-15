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
package com.salesforce.ide.core.internal.utils;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import com.salesforce.ide.test.common.NoOrgSetupTest;

@SuppressWarnings("deprecation")
public class ZipUtilsTest_unit extends NoOrgSetupTest {

    public void testZipUtils_handleDuplicateEntryException() {
        logStart("testZipUtils_handleDuplicateEntryException");
        try {
            ZipEntry dir = new ZipEntry("test");
            ZipException zipException = new ZipException("duplicate entry: ");

            try {
                ZipUtils.handleDuplicateEntryException(dir, zipException);
            } catch (IOException e) {
                fail("Exception not expected - zip dup exception should have been handled");
            }

        } catch (Exception e) {
            handleFailure("Unable to test zip", e);
        } finally {
            logEnd("testZipUtils_handleDuplicateEntryException");
        }
    }
}
