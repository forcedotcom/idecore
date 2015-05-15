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
package com.salesforce.ide.test;

import junit.framework.Test;

public class MainAllUnitTests extends BaseTestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new MainAllUnitTests();
    }

    public MainAllUnitTests() {
        addTest(new com.salesforce.ide.core.test.AllUnitTests());
        addTest(new com.salesforce.ide.ui.test.AllUnitTests());
        addTest(new com.salesforce.ide.ui.editors.test.AllUnitTests());
        addTest(new com.salesforce.ide.deployment.test.AllUnitTests());
        addTest(new com.salesforce.ide.upgrade.test.AllUnitTests());
    }
}
