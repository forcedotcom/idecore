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

import junit.framework.TestCase;

import com.salesforce.ide.api.metadata.types.LogCategoryExt;
import com.salesforce.ide.api.metadata.types.LogCategoryLevelExt;


/**
 *
 * Testing LoggingInfo
 *
 * @author fchang
 */
public class LoggingInfoTest_unit extends TestCase {

    public void testLoggingInfo_getLoggingInfo() {
        // setup
        LogCategoryExt categoryExt = null;
        LogCategoryLevelExt levelExt = null;
        for (int categorySelection = 0; categorySelection < LoggingInfo.getLogCategories().length; categorySelection++) {
            categoryExt = LoggingInfo.getLogCategories()[categorySelection];

            for (int levelSelection = 0; levelSelection < LoggingInfo.getLoggingLevelRange(categoryExt); levelSelection++) {
                levelExt = LoggingInfo.getLogCategoryLevelFrom(categoryExt, levelSelection);

                // execute
                LoggingInfo loggingInfo = LoggingInfo.getLoggingInfo(levelSelection, categoryExt.getExternalValue());

                // assert
                if (levelSelection == 0) { // None level is selected
                    assertNull(loggingInfo.getLogInfo());
                } else {
                    assertEquals(categoryExt.getInternalValue(), loggingInfo.getLogInfo().getCategory().name());
                    assertEquals(levelExt.getInternalValue(), loggingInfo.getLogInfo().getLevel().name());
                }
                assertEquals(categoryExt.getExternalValue(), loggingInfo.getCategoryExt().getExternalValue());
                assertEquals(categoryExt.getInternalValue(), loggingInfo.getCategoryExt().getInternalValue());
                assertEquals(levelExt.getInternalValue(), loggingInfo.getLevelExt().getInternalValue());
                assertEquals(levelExt.getExternalValue(), loggingInfo.getLevelLabelText());
                assertEquals(categorySelection, loggingInfo.getCategorySelection());
                assertEquals(levelSelection, loggingInfo.getLevelScaleSelection());
            }
        }
    }

}
