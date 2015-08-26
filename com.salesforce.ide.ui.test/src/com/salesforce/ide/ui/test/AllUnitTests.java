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
package com.salesforce.ide.ui.test;

import junit.framework.Test;

import com.salesforce.ide.test.common.utils.SimpleTestSuite;

public class AllUnitTests extends SimpleTestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new AllUnitTests();
    }

    public AllUnitTests() {
        addTest(new com.salesforce.ide.ui.actions.AllUnitTests());
        addTest(new com.salesforce.ide.ui.packagemanifest.AllUnitTests());
        addTest(new com.salesforce.ide.ui.views.log.AllUnitTests());
        addTest(new com.salesforce.ide.ui.views.runtest.test.AllUnitTests());
    }
}
