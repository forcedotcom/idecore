package com.salesforce.ide.test;

import junit.framework.Test;

public class MainAllPdeUITests extends BaseTestSuite {

	public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
	
	public static Test suite() {
		return new MainAllPdeUITests();
	}
	
	public MainAllPdeUITests() {
		addTest(new com.salesforce.ide.ui.test.AllPdeUITests());
	}
}
