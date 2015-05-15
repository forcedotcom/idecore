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

import com.salesforce.ide.core.internal.utils.Constants;
import com.salesforce.ide.core.model.Component;
import com.salesforce.ide.test.common.NoOrgSetupTest;

@SuppressWarnings("deprecation")
public class BuilderExceptionTest_unit extends NoOrgSetupTest {
    
    public void testBuilderException() {
        logStart("testBuilderException");
        try {
        	assertNotNull(new BuilderException("Whatever"));
			assertNotNull(new BuilderException("Whatever", new ForceProjectException("Whatever")));
			Component component = getComponentFactory().getComponentByComponentType(Constants.SCONTROL);
			BuilderException builderException = new BuilderException(component, null, new ForceProjectException("Whatever"));
			assertNotNull(builderException);
			assertNotNull(builderException.getComponent());
			assertNull(builderException.getFile());
        } catch (Exception e) {
            handleFailure("Unable to load payload", e);
        } finally {
        	logEnd("testBuilderException");
        }    
    }
}
