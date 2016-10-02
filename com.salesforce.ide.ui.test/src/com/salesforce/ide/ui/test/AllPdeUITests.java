package com.salesforce.ide.ui.test;

import junit.framework.Test;

import com.salesforce.ide.test.common.utils.SimpleTestSuite;

public class AllPdeUITests extends SimpleTestSuite {

	public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
	
	public static Test suite() {
		return new AllPdeUITests();
	}
	
	public AllPdeUITests() {
		addTest(new com.salesforce.ide.ui.views.runtest.test.AllPdeUITests());
		addTest(new com.salesforce.ide.ui.wizard.components.apex.test.AllPdeUITests());
	}
}
